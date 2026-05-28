"""Author: Shuying Liang
Date: 2026-05-27
Purpose: Shared cache and index persistence helpers for the AI search service.
"""

import json
import logging
import os
import urllib.request
from io import BytesIO

import faiss
import numpy as np
from PIL import Image


logger = logging.getLogger(__name__)

CACHE_DIR = os.path.join(os.path.dirname(__file__), "cache")

os.makedirs(CACHE_DIR, exist_ok=True)


def save_index(index, file_name: str) -> None:
    faiss.write_index(index, os.path.join(CACHE_DIR, file_name))


def load_index(file_name: str):
    path = os.path.join(CACHE_DIR, file_name)
    if not os.path.exists(path):
        return None
    return faiss.read_index(path)


def save_json(data, file_name: str) -> None:
    with open(os.path.join(CACHE_DIR, file_name), "w", encoding="utf-8") as file:
        json.dump(data, file, ensure_ascii=False)


def load_json(file_name: str, default=None):
    path = os.path.join(CACHE_DIR, file_name)
    if not os.path.exists(path):
        return default
    with open(path, "r", encoding="utf-8") as file:
        return json.load(file)


def delete_cache_file(file_name: str) -> None:
    path = os.path.join(CACHE_DIR, file_name)
    if os.path.exists(path):
        os.remove(path)


def save_numpy_array(array: np.ndarray, file_name: str) -> None:
    """Persist dense embedding arrays that need to stay aligned with FAISS row order."""
    np.save(os.path.join(CACHE_DIR, file_name), array)


def load_numpy_array(file_name: str):
    """Load one persisted embedding array from the cache directory if it exists."""
    path = os.path.join(CACHE_DIR, file_name)
    if not os.path.exists(path):
        return None
    return np.load(path)


def fetch_image_from_url(image_url: str, timeout: int = 10):
    try:
        request = urllib.request.Request(
            image_url,
            headers={
                "User-Agent": "unique-finds-ai-search/1.0"
            }
        )
        with urllib.request.urlopen(request, timeout=timeout) as response:
            return Image.open(BytesIO(response.read())).convert("RGB")
    except Exception as exc:
        logger.warning("failed to fetch image from url: %s", image_url, exc_info=exc)
        return None
