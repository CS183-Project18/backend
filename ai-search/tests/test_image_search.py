# Author: Shuying Liang. Purpose: Unit tests for image-search feature extraction and fallback behavior.
import asyncio
import tempfile
import unittest
from io import BytesIO
from pathlib import Path
from unittest.mock import patch

import numpy as np
from PIL import Image
from starlette.datastructures import UploadFile
from starlette.requests import Request

import image_search
import main


def create_temp_image() -> str:
    with tempfile.NamedTemporaryFile(suffix=".png", delete=False) as file:
        Image.new("RGB", (8, 8), color="white").save(file.name)
        return file.name


class DummyIndex:

    def __init__(self, distances, indexes):
        self.distances = np.asarray([distances], dtype="float32")
        self.indexes = np.asarray([indexes], dtype="int64")

    def search(self, _query_embedding, _candidate_count):
        return self.distances, self.indexes


class ImageSearchRankingTest(unittest.TestCase):

    def tearDown(self):
        image_search.image_index = None
        image_search.image_post_ids = []
        image_search.image_metadata = []
        image_search.image_text_embeddings = None

    def test_deduplicates_by_best_scoring_image_for_post(self):
        image_search.image_index = DummyIndex([0.80, 0.70, 0.75], [0, 1, 2])
        image_search.image_metadata = [
            {"post_id": 1, "image_url": "a", "semantic_text": "warm lamp"},
            {"post_id": 1, "image_url": "b", "semantic_text": "warm lamp"},
            {"post_id": 2, "image_url": "c", "semantic_text": "desk decor"},
        ]
        image_search.image_post_ids = [1, 1, 2]
        image_search.image_text_embeddings = np.asarray([
            [0.2, 0.0],
            [1.0, 0.0],
            [0.1, 0.0],
        ], dtype="float32")

        image_path = create_temp_image()
        try:
            with patch.object(image_search, "encode_image", return_value=np.asarray([1.0, 0.0], dtype="float32")):
                results = image_search.image_search(image_path, top_k=2)
        finally:
            Path(image_path).unlink(missing_ok=True)

        self.assertEqual([1, 2], [row["post_id"] for row in results])
        self.assertGreater(results[0]["similarity"], results[1]["similarity"])

    def test_falls_back_when_threshold_filters_every_candidate(self):
        image_search.image_index = DummyIndex([0.10, 0.08], [0, 1])
        image_search.image_metadata = [
            {"post_id": 5, "image_url": "a", "semantic_text": "blue vase"},
            {"post_id": 6, "image_url": "b", "semantic_text": "green vase"},
        ]
        image_search.image_post_ids = [5, 6]
        image_search.image_text_embeddings = np.asarray([
            [1.0, 0.0],
            [0.4, 0.0],
        ], dtype="float32")

        image_path = create_temp_image()
        try:
            with patch.object(image_search, "encode_image", return_value=np.asarray([1.0, 0.0], dtype="float32")):
                results = image_search.image_search(image_path, top_k=2)
        finally:
            Path(image_path).unlink(missing_ok=True)

        self.assertEqual([5, 6], [row["post_id"] for row in results])

    def test_hybrid_rerank_can_promote_better_text_match(self):
        image_search.image_index = DummyIndex([0.90, 0.88], [0, 1])
        image_search.image_metadata = [
            {"post_id": 10, "image_url": "a", "semantic_text": "plain object"},
            {"post_id": 11, "image_url": "b", "semantic_text": "matching object"},
        ]
        image_search.image_post_ids = [10, 11]
        image_search.image_text_embeddings = np.asarray([
            [0.0, 1.0],
            [1.0, 0.0],
        ], dtype="float32")

        image_path = create_temp_image()
        try:
            with patch.object(image_search, "encode_image", return_value=np.asarray([1.0, 0.0], dtype="float32")):
                results = image_search.image_search(image_path, top_k=1)
        finally:
            Path(image_path).unlink(missing_ok=True)

        self.assertEqual([11], [row["post_id"] for row in results])


class ImageSearchApiTest(unittest.TestCase):

    def test_image_search_accepts_top_k_from_multipart_form(self):
        recorded = {}

        def fake_image_search(_path, top_k=5):
            recorded["top_k"] = top_k
            return [{"post_id": 99, "similarity": 0.9}]

        with patch.object(main, "initialize_semantic_search"), \
                patch.object(main, "initialize_image_search"), \
                patch.object(main, "image_search", side_effect=fake_image_search):
            main.SEARCH_RUNTIME_ERROR = None
            request = Request({"type": "http", "query_string": b""})
            file = UploadFile(
                filename="query.png",
                file=BytesIO(b"fake-image"),
            )
            response = asyncio.run(main.api_image_search(request=request, file=file, top_k=7))

        self.assertEqual(7, recorded["top_k"])
        self.assertEqual([99], response["data"]["post_ids"])


if __name__ == "__main__":
    unittest.main()
