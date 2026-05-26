import logging
import os
from collections import OrderedDict

import faiss
import numpy as np
import torch
from PIL import Image
from transformers import AutoProcessor, CLIPModel

from db_utils import delete_cache_file, fetch_image_from_url, load_index, load_json, save_index, save_json


logger = logging.getLogger(__name__)

IMAGE_INDEX_FILE = "image.index"
IMAGE_POST_IDS_FILE = "image_post_ids.json"
CLIP_MODEL_NAME = "openai/clip-vit-base-patch32"
IMAGE_SEARCH_MIN_SIMILARITY = float(os.getenv("IMAGE_SEARCH_MIN_SIMILARITY", "0.18"))
FAILED_IMAGE_SAMPLE_LIMIT = 5

image_model = None
image_processor = None
image_index = None
image_post_ids = []
device = None


def initialize_image_search() -> None:
    global image_index, image_post_ids

    load_image_model()
    image_index = load_index(IMAGE_INDEX_FILE)
    image_post_ids = load_json(IMAGE_POST_IDS_FILE, [])
    if image_index is not None and image_post_ids:
        logger.info("loaded cached image index with %s image embeddings", len(image_post_ids))


def build_image_index(posts: list[dict]) -> dict:
    global image_index, image_post_ids

    load_image_model()

    embeddings = []
    image_post_ids = []
    failed_image_count = 0
    failed_image_samples = []

    for post in posts:
        image_urls = post.get("image_urls") or []
        for image_url in image_urls:
            image = fetch_image_from_url(image_url)
            if image is None:
                failed_image_count += 1
                if len(failed_image_samples) < FAILED_IMAGE_SAMPLE_LIMIT:
                    failed_image_samples.append(image_url)
                continue
            embedding = encode_image(image)
            embeddings.append(embedding)
            image_post_ids.append(post["id"])

    if embeddings:
        embedding_array = np.asarray(embeddings, dtype="float32")
        image_index = faiss.IndexFlatIP(embedding_array.shape[1])
        image_index.add(embedding_array)
        save_index(image_index, IMAGE_INDEX_FILE)
    else:
        image_index = None
        delete_cache_file(IMAGE_INDEX_FILE)

    save_json(image_post_ids, IMAGE_POST_IDS_FILE)
    logger.info(
        "rebuilt image index with %s image embeddings and %s failed images",
        len(image_post_ids),
        failed_image_count
    )
    if failed_image_samples:
        logger.warning("sample failed image urls during index build: %s", failed_image_samples)
    return {
        "success": True,
        "message": "image index rebuilt",
        "count": len(image_post_ids),
        "failed_image_count": failed_image_count,
        "failed_image_samples": failed_image_samples
    }


def image_search(image_path: str, top_k: int = 5) -> list[dict]:
    if image_index is None or not image_post_ids:
        raise RuntimeError("image index is not ready")

    image = Image.open(image_path).convert("RGB")
    query_embedding = encode_image(image).reshape(1, -1)

    candidate_count = min(max(top_k * 5, top_k), len(image_post_ids))
    distance_values, index_values = image_index.search(query_embedding, candidate_count)

    ranked_posts = OrderedDict()
    threshold_filtered = 0
    for index_position, similarity in zip(index_values[0], distance_values[0]):
        if index_position < 0:
            continue
        if float(similarity) < IMAGE_SEARCH_MIN_SIMILARITY:
            threshold_filtered += 1
            continue
        post_id = image_post_ids[index_position]
        if post_id in ranked_posts:
            continue
        ranked_posts[post_id] = float(similarity)
        if len(ranked_posts) >= top_k:
            break

    if not ranked_posts:
        logger.info(
            "image search returned no ranked posts: threshold=%s, filtered_candidates=%s, candidate_count=%s",
            IMAGE_SEARCH_MIN_SIMILARITY,
            threshold_filtered,
            candidate_count
        )
    else:
        logger.info("image search top matches: %s", list(ranked_posts.items()))

    return [
        {
            "post_id": post_id,
            "similarity": similarity
        }
        for post_id, similarity in ranked_posts.items()
    ]


def encode_image(image: Image.Image) -> np.ndarray:
    processed = image_processor(images=image, return_tensors="pt")
    processed = {key: value.to(device) for key, value in processed.items()}
    with torch.no_grad():
        embedding = image_model.get_image_features(**processed)
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
