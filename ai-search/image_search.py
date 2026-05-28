"""Author: Shuying Liang
Date: 2026-05-27
Purpose: Build and query the CLIP-based image search index for AI retrieval.
"""

import logging
import os

import faiss
import numpy as np
import torch
from PIL import Image
from transformers import AutoProcessor, CLIPModel

from db_utils import (
    delete_cache_file,
    fetch_image_from_url,
    load_index,
    load_json,
    load_numpy_array,
    save_index,
    save_json,
    save_numpy_array,
)
from semantic import build_semantic_text


logger = logging.getLogger(__name__)

IMAGE_INDEX_FILE = "image.index"
IMAGE_POST_IDS_FILE = "image_post_ids.json"
IMAGE_METADATA_FILE = "image_metadata.json"
IMAGE_TEXT_EMBEDDINGS_FILE = "image_text_embeddings.npy"
CLIP_MODEL_NAME = "openai/clip-vit-base-patch32"
IMAGE_SEARCH_MIN_SIMILARITY = float(os.getenv("IMAGE_SEARCH_MIN_SIMILARITY", "0.18"))
FAILED_IMAGE_SAMPLE_LIMIT = 5
IMAGE_SEARCH_IMAGE_WEIGHT = 0.82
IMAGE_SEARCH_TEXT_WEIGHT = 0.18
IMAGE_SEARCH_CANDIDATE_MULTIPLIER = 12
IMAGE_SEARCH_MIN_CANDIDATES = 50

image_model = None
image_processor = None
image_index = None
image_post_ids = []
image_metadata = []
image_text_embeddings = None
device = None


def initialize_image_search() -> None:
    """Load the persisted image index, metadata, and paired text embeddings into memory."""
    global image_index, image_post_ids, image_metadata, image_text_embeddings

    load_image_model()
    image_index = load_index(IMAGE_INDEX_FILE)
    image_post_ids = load_json(IMAGE_POST_IDS_FILE, [])
    image_metadata = load_json(IMAGE_METADATA_FILE, [])
    image_text_embeddings = load_numpy_array(IMAGE_TEXT_EMBEDDINGS_FILE)

    if not is_index_cache_consistent():
        logger.warning("image cache is inconsistent, clearing in-memory image search state")
        reset_image_search_state(clear_cache_files=False)
        return

    if image_index is not None and image_metadata:
        logger.info("loaded cached image index with %s image embeddings", len(image_metadata))


def build_image_index(posts: list[dict]) -> dict:
    """Rebuild the image index and persist the metadata needed for hybrid reranking."""
    global image_index, image_post_ids, image_metadata, image_text_embeddings

    load_image_model()

    embeddings = []
    metadata = []
    text_embeddings = []
    failed_image_count = 0
    failed_image_samples = []

    for post in posts:
        semantic_text = build_semantic_text(post) or "item"
        text_embedding = encode_text(semantic_text)
        image_urls = post.get("image_urls") or []
        for image_url in image_urls:
            image = fetch_image_from_url(image_url)
            if image is None:
                failed_image_count += 1
                if len(failed_image_samples) < FAILED_IMAGE_SAMPLE_LIMIT:
                    failed_image_samples.append(image_url)
                continue
            embeddings.append(encode_image(image))
            text_embeddings.append(text_embedding)
            metadata.append({
                "post_id": post["id"],
                "image_url": image_url,
                "semantic_text": semantic_text,
            })

    if embeddings:
        embedding_array = np.asarray(embeddings, dtype="float32")
        text_embedding_array = np.asarray(text_embeddings, dtype="float32")
        image_index = faiss.IndexFlatIP(embedding_array.shape[1])
        image_index.add(embedding_array)
        image_metadata = metadata
        image_post_ids = [item["post_id"] for item in metadata]
        image_text_embeddings = text_embedding_array
        save_index(image_index, IMAGE_INDEX_FILE)
        save_json(image_post_ids, IMAGE_POST_IDS_FILE)
        save_json(image_metadata, IMAGE_METADATA_FILE)
        save_numpy_array(image_text_embeddings, IMAGE_TEXT_EMBEDDINGS_FILE)
    else:
        reset_image_search_state(clear_cache_files=True)

    logger.info(
        "rebuilt image index with %s image embeddings and %s failed images",
        len(image_metadata),
        failed_image_count
    )
    if failed_image_samples:
        logger.warning("sample failed image urls during index build: %s", failed_image_samples)
    return {
        "success": True,
        "message": "image index rebuilt",
        "count": len(image_metadata),
        "failed_image_count": failed_image_count,
        "failed_image_samples": failed_image_samples
    }


def image_search(image_path: str, top_k: int = 5) -> list[dict]:
    """Search by image using FAISS recall first, then rerank with image and text similarity."""
    if image_index is None or image_text_embeddings is None or not image_metadata:
        raise RuntimeError("image index is not ready")

    image = Image.open(image_path).convert("RGB")
    query_embedding = encode_image(image).reshape(1, -1)

    # Pull a wider candidate set first so post-level reranking is not dominated by a few early hits.
    candidate_count = min(
        max(top_k * IMAGE_SEARCH_CANDIDATE_MULTIPLIER, IMAGE_SEARCH_MIN_CANDIDATES, top_k),
        len(image_metadata)
    )
    distance_values, index_values = image_index.search(query_embedding, candidate_count)

    candidate_rows = []
    threshold_filtered = 0
    for index_position, image_similarity in zip(index_values[0], distance_values[0]):
        if index_position < 0:
            continue
        text_similarity = float(np.dot(query_embedding[0], image_text_embeddings[index_position]))
        combined_similarity = (
            float(image_similarity) * IMAGE_SEARCH_IMAGE_WEIGHT
            + text_similarity * IMAGE_SEARCH_TEXT_WEIGHT
        )
        passes_threshold = float(image_similarity) >= IMAGE_SEARCH_MIN_SIMILARITY
        if not passes_threshold:
            threshold_filtered += 1
        metadata = image_metadata[index_position]
        candidate_rows.append({
            "post_id": metadata["post_id"],
            "image_url": metadata["image_url"],
            "semantic_text": metadata["semantic_text"],
            "image_similarity": float(image_similarity),
            "text_similarity": text_similarity,
            "combined_similarity": combined_similarity,
            "passes_threshold": passes_threshold,
        })

    if not candidate_rows:
        logger.info("image search returned no candidates from faiss")
        return []

    effective_candidates = [row for row in candidate_rows if row["passes_threshold"]]
    if not effective_candidates:
        # Fall back to the best combined matches when the image-only threshold would otherwise yield nothing.
        effective_candidates = candidate_rows
        logger.info(
            "image search threshold filtered all candidates, using fallback ranking: threshold=%s, candidate_count=%s",
            IMAGE_SEARCH_MIN_SIMILARITY,
            candidate_count
        )

    best_candidate_by_post = {}
    for row in effective_candidates:
        # Keep only the strongest match per post so multi-image posts do not crowd out the result list.
        current_best = best_candidate_by_post.get(row["post_id"])
        if current_best is None or compare_candidate_rows(row, current_best) > 0:
            best_candidate_by_post[row["post_id"]] = row

    ranked_rows = sorted(
        best_candidate_by_post.values(),
        key=lambda row: (
            row["combined_similarity"],
            row["image_similarity"],
            row["text_similarity"],
        ),
        reverse=True
    )[:top_k]

    if not ranked_rows:
        logger.info(
            "image search returned no ranked posts: threshold=%s, filtered_candidates=%s, candidate_count=%s",
            IMAGE_SEARCH_MIN_SIMILARITY,
            threshold_filtered,
            candidate_count
        )
    else:
        logger.info(
            "image search top matches: %s",
            [(row["post_id"], row["combined_similarity"]) for row in ranked_rows]
        )

    return [
        {
            "post_id": row["post_id"],
            "similarity": row["combined_similarity"]
        }
        for row in ranked_rows
    ]


def compare_candidate_rows(left: dict, right: dict) -> int:
    left_score = (
        left["combined_similarity"],
        left["image_similarity"],
        left["text_similarity"],
    )
    right_score = (
        right["combined_similarity"],
        right["image_similarity"],
        right["text_similarity"],
    )
    return (left_score > right_score) - (left_score < right_score)


def encode_image(image: Image.Image) -> np.ndarray:
    processed = image_processor(images=image, return_tensors="pt")
    processed = {key: value.to(device) for key, value in processed.items()}
    with torch.no_grad():
        embedding = image_model.get_image_features(**processed)
    return normalize_embedding(embedding)


def encode_text(text: str) -> np.ndarray:
    """Encode one post-level semantic text string for hybrid image-to-post scoring."""
    processed = image_processor(text=[text], return_tensors="pt", padding=True, truncation=True)
    processed = {key: value.to(device) for key, value in processed.items()}
    with torch.no_grad():
        embedding = image_model.get_text_features(**processed)
    return normalize_embedding(embedding)


def normalize_embedding(embedding: torch.Tensor) -> np.ndarray:
    embedding_array = embedding.cpu().numpy().astype("float32")
    faiss.normalize_L2(embedding_array)
    return embedding_array[0]


def load_image_model() -> None:
    global image_model, image_processor, device
    if image_model is None or image_processor is None:
        device = "cuda" if torch.cuda.is_available() else "cpu"
        logger.info("loading clip image model on device: %s", device)
        image_processor = AutoProcessor.from_pretrained(CLIP_MODEL_NAME)
        image_model = CLIPModel.from_pretrained(CLIP_MODEL_NAME).to(device)
        image_model.eval()


def is_index_cache_consistent() -> bool:
    """Verify that the FAISS index, metadata, and text embedding cache describe the same rows."""
    if image_index is None:
        return not image_metadata and image_text_embeddings is None and not image_post_ids
    if not image_metadata or image_text_embeddings is None:
        return False
    if len(image_metadata) != len(image_post_ids):
        return False
    if len(image_metadata) != image_index.ntotal:
        return False
    if len(image_metadata) != len(image_text_embeddings):
        return False
    return True


def reset_image_search_state(clear_cache_files: bool) -> None:
    """Clear in-memory image search state and optionally remove the persisted cache files."""
    global image_index, image_post_ids, image_metadata, image_text_embeddings

    image_index = None
    image_post_ids = []
    image_metadata = []
    image_text_embeddings = None

    if clear_cache_files:
        delete_cache_file(IMAGE_INDEX_FILE)
        delete_cache_file(IMAGE_POST_IDS_FILE)
        delete_cache_file(IMAGE_METADATA_FILE)
        delete_cache_file(IMAGE_TEXT_EMBEDDINGS_FILE)
