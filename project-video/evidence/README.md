# Evidence Index

This directory contains supporting material used by the project video and the
V1-to-V2 comparison.

## Screenshots

| File | Evidence shown |
| --- | --- |
| `01_ai_search_final_frame.png` | Final semantic-search workflow used in the CS concept clip |
| `02_v1_authentication_ui.png` | V1 authentication and interface prototype |
| `03_v1_create_post_prototype.png` | V1 local create-post interaction |
| `04_v2_authentication_ui.png` | Updated V2 authentication interface |
| `05_v2_backend_connected_feed.png` | V2 feed operating with backend data |
| `06_v2_post_interaction.png` | V2 post-detail and interaction workflow |
| `07_v2_mobile_layout.png` | Responsive mobile interface included in V2 |

The screenshots are frames from the submitted demonstration videos. They are
included for review convenience and do not replace the videos.

## Diagram

`diagrams/ai_semantic_search_workflow.png` shows the V2 search path from the
Spring Boot API to embedding generation, FAISS retrieval, MySQL data retrieval
and the SQL fallback.

## Metrics and Validation

- `metrics/version_comparison.csv` records reproducible repository-scope counts.
- `metrics/v2_backend_acceptance_checklist.md` records backend workflow checks.
- `metrics/README.md` explains how the comparison figures were produced.
