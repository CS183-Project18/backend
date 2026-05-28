"""Author: Shuying Liang
Date: 2026-05-27
Purpose: FastAPI entrypoint for internal semantic and image search services.
"""

import logging
import os
import tempfile
from datetime import datetime
from functools import lru_cache

from fastapi import FastAPI, File, Form, Request, UploadFile
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field

logging.basicConfig(
    level=logging.INFO,
    format="[%(asctime)s] [%(levelname)s] %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S"
)
logger = logging.getLogger(__name__)

SEARCH_IMPORT_ERROR = None
SEARCH_RUNTIME_ERROR = None

try:
    from image_search import build_image_index, image_search, initialize_image_search
    from semantic import build_semantic_index, initialize_semantic_search, semantic_search
except Exception as exc:  # pragma: no cover - startup guard
    import_error = exc
    SEARCH_IMPORT_ERROR = import_error
    logger.exception("failed to import ai-search backends")

    def initialize_image_search():
        raise RuntimeError(f"image search backend import failed: {import_error}")

    def build_image_index(_posts):
        raise RuntimeError(f"image search backend import failed: {import_error}")

    def image_search(_image_path, top_k=5):
        raise RuntimeError(f"image search backend import failed: {import_error}")

    def initialize_semantic_search():
        raise RuntimeError(f"semantic search backend import failed: {import_error}")

    def build_semantic_index(_posts):
        raise RuntimeError(f"semantic search backend import failed: {import_error}")

    def semantic_search(_query, top_k=5):
        raise RuntimeError(f"semantic search backend import failed: {import_error}")

app = FastAPI(
    title="Unique Finds AI Search",
    description="Internal semantic and image search service for Unique Finds."
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "webp"}
MAX_FILE_SIZE = 10 * 1024 * 1024


class PostItem(BaseModel):
    id: int = Field(..., description="Post id")
    title: str = Field(..., description="Post title")
    description: str = Field("", description="Post description")
    image_urls: list[str] = Field(default_factory=list, description="Post image URLs")
    tags: list[str] = Field(default_factory=list, description="Post tag names")
    category_name: str = Field("", description="Post category name")
    store_name: str = Field("", description="Post store name")
    location_text: str = Field("", description="Post location text")


class BuildIndexRequest(BaseModel):
    posts: list[PostItem] = Field(default_factory=list, description="Published posts to index")


def success_response(data=None, message: str = "success") -> dict:
    return {
        "code": 200,
        "data": data,
        "message": message
    }


def error_response(code: int, message: str) -> dict:
    return {
        "code": code,
        "data": None,
        "message": message
    }


def validate_top_k(top_k: int) -> bool:
    return 1 <= top_k <= 100


@app.on_event("startup")
async def startup_event():
    global SEARCH_RUNTIME_ERROR
    if SEARCH_IMPORT_ERROR is not None:
        SEARCH_RUNTIME_ERROR = SEARCH_IMPORT_ERROR
        logger.error("ai-search service started in unhealthy state because imports failed")
        return
    try:
        initialize_semantic_search()
        initialize_image_search()
        logger.info("ai-search service started and all search backends are ready")
    except Exception as exc:
        SEARCH_RUNTIME_ERROR = exc
        logger.exception("ai-search backend self-check failed during startup")


@app.post("/build_index")
async def api_build_index(request: BuildIndexRequest):
    if SEARCH_RUNTIME_ERROR is not None:
        return error_response(503, f"ai-search backend is not ready: {SEARCH_RUNTIME_ERROR}")
    posts = [
        {
            "id": post.id,
            "title": post.title,
            "description": post.description,
            "image_urls": post.image_urls,
            "tags": post.tags,
            "category_name": post.category_name,
            "store_name": post.store_name,
            "location_text": post.location_text,
        }
        for post in request.posts
    ]

    try:
        semantic_result = build_semantic_index(posts)
        image_result = build_image_index(posts)
        cached_semantic_search.cache_clear()
    except Exception as exc:
        logger.exception("failed to rebuild indices")
        return error_response(500, f"failed to rebuild indices: {exc}")

    return success_response(
        {
            "success": semantic_result.get("success", False) and image_result.get("success", False),
            "message": "indices rebuilt",
            "count": len(posts),
            "semantic_count": semantic_result.get("count", 0),
            "image_count": image_result.get("count", 0),
            "failed_image_count": image_result.get("failed_image_count", 0),
        },
        "indices rebuilt"
    )


@lru_cache(maxsize=128)
def cached_semantic_search(query: str, top_k: int) -> list:
    return semantic_search(query, top_k=top_k)


@app.get("/semantic_search")
def api_semantic_search(q: str, top_k: int = 5):
    if SEARCH_RUNTIME_ERROR is not None:
        return error_response(503, f"semantic backend is not ready: {SEARCH_RUNTIME_ERROR}")
    if not q or not q.strip():
        return error_response(400, "query parameter q must not be blank")
    if not validate_top_k(top_k):
        return error_response(400, "top_k must be between 1 and 100")

    try:
        cache_info_before = cached_semantic_search.cache_info()
        post_ids = [result["post_id"] for result in cached_semantic_search(q, top_k)]
        cache_info_after = cached_semantic_search.cache_info()
        return success_response(
            {
                "query": q,
                "post_ids": post_ids,
                "cached": cache_info_after.hits > cache_info_before.hits,
            }
        )
    except RuntimeError as exc:
        return error_response(500, str(exc))
    except Exception as exc:
        logger.exception("semantic search failed")
        return error_response(500, f"semantic search failed: {exc}")


@app.post("/image_search")
async def api_image_search(request: Request, file: UploadFile = File(...), top_k: int | None = Form(None)):
    if SEARCH_RUNTIME_ERROR is not None:
        return error_response(503, f"image backend is not ready: {SEARCH_RUNTIME_ERROR}")
    if not file.filename:
        return error_response(400, "image file name is required")

    # Prefer multipart form-data top_k from the Java client, but keep query-param fallback for compatibility.
    query_top_k = request.query_params.get("top_k")
    if top_k is None and query_top_k is not None:
        try:
            top_k = int(query_top_k)
        except ValueError:
            return error_response(400, "top_k must be an integer")
    resolved_top_k = top_k if top_k is not None else 5
    if not validate_top_k(resolved_top_k):
        return error_response(400, "top_k must be between 1 and 100")

    extension = file.filename.rsplit(".", 1)[-1].lower() if "." in file.filename else ""
    if extension not in ALLOWED_EXTENSIONS:
        return error_response(400, "only jpg, jpeg, png, and webp images are supported")

    content = await file.read()
    if len(content) > MAX_FILE_SIZE:
        return error_response(400, "image file size must not exceed 10MB")

    temp_path = None
    try:
        with tempfile.NamedTemporaryFile(suffix=f".{extension}", delete=False) as temp_file:
            temp_file.write(content)
            temp_path = temp_file.name
        post_ids = [result["post_id"] for result in image_search(temp_path, top_k=resolved_top_k)]
        return success_response({"post_ids": post_ids})
    except RuntimeError as exc:
        return error_response(500, str(exc))
    except Exception as exc:
        logger.exception("image search failed")
        return error_response(500, f"image search failed: {exc}")
    finally:
        if temp_path and os.path.exists(temp_path):
            os.unlink(temp_path)


@app.get("/health")
def health_check():
    if SEARCH_IMPORT_ERROR is not None:
        return error_response(503, f"dependency import failed: {SEARCH_IMPORT_ERROR}")
    if SEARCH_RUNTIME_ERROR is not None:
        return error_response(503, f"backend self-check failed: {SEARCH_RUNTIME_ERROR}")
    return success_response(
        {
            "status": "healthy",
            "service": "Unique Finds AI Search",
            "timestamp": datetime.now().isoformat(),
        }
    )


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=8000)
