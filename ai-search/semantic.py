"""Author: Shuying Liang
Date: 2026-05-27
Purpose: Build and query the semantic text search index for AI retrieval.
"""

import logging
from typing import Any

import faiss
import numpy as np
from sentence_transformers import SentenceTransformer

from db_utils import delete_cache_file, load_index, load_json, save_index, save_json


logger = logging.getLogger(__name__)

SEMANTIC_INDEX_FILE = "semantic.index"
SEMANTIC_POST_IDS_FILE = "semantic_post_ids.json"

semantic_model = None
semantic_index = None
semantic_post_ids = []


def initialize_semantic_search() -> None:
    global semantic_index, semantic_post_ids

    load_semantic_model()
    semantic_index = load_index(SEMANTIC_INDEX_FILE)
    semantic_post_ids = load_json(SEMANTIC_POST_IDS_FILE, [])
    if semantic_index is not None and semantic_post_ids:
        logger.info("loaded cached semantic index with %s posts", len(semantic_post_ids))


def build_semantic_index(posts: list[dict]) -> dict:
    global semantic_index, semantic_post_ids

    load_semantic_model()
    semantic_post_ids = [post["id"] for post in posts]

    if not posts:
        semantic_index = None
        delete_cache_file(SEMANTIC_INDEX_FILE)
        save_json([], SEMANTIC_POST_IDS_FILE)
        return {
            "success": True,
            "message": "semantic index cleared",
            "count": 0
        }

    texts = [build_semantic_text(post) for post in posts]
    embeddings = semantic_model.encode(texts, normalize_embeddings=True)
    embedding_array = np.asarray(embeddings, dtype="float32")

    semantic_index = faiss.IndexFlatIP(embedding_array.shape[1])
    semantic_index.add(embedding_array)

    save_index(semantic_index, SEMANTIC_INDEX_FILE)
    save_json(semantic_post_ids, SEMANTIC_POST_IDS_FILE)

    logger.info("rebuilt semantic index with %s posts", len(semantic_post_ids))
    return {
        "success": True,
        "message": "semantic index rebuilt",
        "count": len(semantic_post_ids)
    }


def semantic_search(query: str, top_k: int = 5) -> list[dict]:
    if semantic_model is None or semantic_index is None or not semantic_post_ids:
        raise RuntimeError("semantic index is not ready")

    normalized_query = normalize_semantic_query(query)
    query_embedding = semantic_model.encode([normalized_query], normalize_embeddings=True)
    distance_values, index_values = semantic_index.search(np.asarray(query_embedding, dtype="float32"), top_k)

    results = []
    for index_position, similarity in zip(index_values[0], distance_values[0]):
        if index_position < 0:
            continue
        results.append({
            "post_id": semantic_post_ids[index_position],
            "similarity": float(similarity)
        })
    return results


def build_semantic_text(post: dict) -> str:
    title = normalize_text(post.get("title", ""))
    description = normalize_text(post.get("description", ""))
    tags = " ".join(normalize_text(tag) for tag in post.get("tags", []) if normalize_text(tag))
    category_name = normalize_text(post.get("category_name", ""))
    store_name = normalize_text(post.get("store_name", ""))
    location_text = normalize_text(post.get("location_text", ""))

    parts = []
    if title:
        parts.extend([f"title {title}", title])
    if tags:
        parts.extend([f"tags {tags}", tags])
    if description:
        parts.append(f"description {description}")
    if category_name:
        parts.append(f"category {category_name}")
    if store_name:
        parts.append(f"store {store_name}")
    if location_text:
        parts.append(f"location {location_text}")

    return " ".join(part.strip() for part in parts if part and part.strip())


def normalize_semantic_query(query: str) -> str:
    normalized = normalize_text(query)
    return normalized.casefold()


def normalize_text(value: Any) -> str:
    if value is None:
        return ""
    return str(value).strip()


def load_semantic_model() -> None:
    global semantic_model
    if semantic_model is None:
        logger.info("loading semantic model")
        semantic_model = SentenceTransformer("all-MiniLM-L6-v2")
