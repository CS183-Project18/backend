from fastapi import FastAPI
from pydantic import BaseModel


app = FastAPI(title="Unique Finds AI Service Placeholder", version="0.1.0")


class SemanticSearchRequest(BaseModel):
    query: str
    page: int = 1
    pageSize: int = 20


class MultimodalSearchRequest(BaseModel):
    query: str | None = None
    imageUrl: str | None = None
    page: int = 1
    pageSize: int = 20


@app.get("/health")
def health():
    return {"status": "ok", "message": "AI placeholder service is running"}


@app.post("/api/search/semantic")
def semantic_search(_: SemanticSearchRequest):
    return {
        "implemented": False,
        "message": "Semantic search service is reserved for the AI teammate implementation.",
        "items": [],
    }


@app.post("/api/search/multimodal")
def multimodal_search(_: MultimodalSearchRequest):
    return {
        "implemented": False,
        "message": "Multimodal search service is reserved for the AI teammate implementation.",
        "items": [],
    }
