# Unique Finds Video Supplementary Materials

## Project

- Project: Unique Finds
- Team ID: T18
- Project ID: P18
- Mentor: Wang Congcong
- Repository: https://github.com/CS183-Project18/backend
- Version 1: https://github.com/CS183-Project18/backend/tree/v1
- Version 2: https://github.com/CS183-Project18/backend/tree/v2

Unique Finds is a discovery and sharing platform for useful products found in
physical stores. Users can publish finds, browse posts, interact with content,
and search for relevant items.

## Team Members

| Member | MU ID | Main responsibility |
| --- | --- | --- |
| Kaijie Zhu | 25124455 | Team leadership, authentication, profiles, database design, backend integration |
| Shuying Liang | 25124480 | AI semantic search, image search, FAISS indexing and FastAPI service |
| Linghang Sun | 25124595 | PRD, interaction design, frontend prototype and responsive flow |
| Enqi Guo | 25126571 | Comments, reports, notifications, moderation and analytics |
| Xuehan Wang | 25125192 | Post details, create-post flow, image upload and interaction UI |
| Chenqian Fu | 25125150 | Frontend shell, authentication UI, navigation and shared pages |

## Submitted Videos

The MP4 files are uploaded directly through the course Microsoft Form and are
not stored in GitHub.

| Deliverable | Recommended filename |
| --- | --- |
| Final project video | `T18_P18_Unique_Finds_Project_Report.mp4` |
| CS concept/algorithm clip | `T18_P18_CS_Concept_AI_Semantic_Search.mp4` |
| Version 1 demo clip | `T18_P18_V1_Demo.mp4` |
| Version 2 demo clip | `T18_P18_V2_Demo.mp4` |

## CS Concept Explained

The algorithm clip explains the semantic text-search path used in Version 2:

1. A natural-language query reaches the Spring Boot Search API.
2. A Sentence Transformer converts the query into a dense embedding.
3. FAISS compares the query vector with indexed post vectors.
4. The AI service returns the Top-K post IDs.
5. The backend retrieves complete post data from MySQL.
6. Ranked results are returned to the user.
7. If the AI service is unavailable, the backend falls back to SQL keyword
   search.

The editable animation source is available at
[`source/T18_CS_Concept_AI_Search_Final.pptx`](source/T18_CS_Concept_AI_Search_Final.pptx).

## Version 1 Summary

Version 1 established the initial product concept and baseline features:

- Static frontend prototype for browsing, post details, creation and interaction.
- Basic Spring Boot APIs for authentication, posts, comments, likes and favourites.
- Frontend prototype data stored in browser local storage.
- Frontend and backend were implemented separately and were not fully integrated.
- No production AI-search service, mobile interface, notification workflow,
  reporting workflow or moderation analytics.

## Version 2 Summary

Version 2 turns the separate baseline components into a broader integrated
system:

- Frontend and mobile pages call the Spring Boot backend.
- Docker Compose starts MySQL, backend, AI search, web frontend and mobile UI.
- Semantic text search uses Sentence Transformers and FAISS.
- Image similarity search uses CLIP embeddings and FAISS.
- Search falls back to SQL when the AI service is unavailable.
- Post images, profiles, stores, categories, tags and trending discovery were added.
- Reports, notifications, moderation logs and administrator analytics were added.
- Swagger/OpenAPI documentation and broader automated tests were added.

## Evidence Used

### Screenshots

- [AI-search final animation frame](evidence/screenshots/01_ai_search_final_frame.png)
- [V1 authentication prototype](evidence/screenshots/02_v1_authentication_ui.png)
- [V1 create-post prototype](evidence/screenshots/03_v1_create_post_prototype.png)
- [V2 authentication interface](evidence/screenshots/04_v2_authentication_ui.png)
- [V2 backend-connected feed](evidence/screenshots/05_v2_backend_connected_feed.png)
- [V2 post interaction flow](evidence/screenshots/06_v2_post_interaction.png)
- [V2 mobile layout](evidence/screenshots/07_v2_mobile_layout.png)

### Diagrams and Validation

- [AI semantic-search workflow](evidence/diagrams/ai_semantic_search_workflow.png)
- [V1/V2 repository comparison](evidence/metrics/version_comparison.csv)
- [V2 backend acceptance checklist](evidence/metrics/v2_backend_acceptance_checklist.md)

The repository counts are descriptive evidence of scope and test coverage. They
are not presented as runtime performance measurements.

## Source Material

- [`source/T18_CS_Concept_AI_Search_Final.pptx`](source/T18_CS_Concept_AI_Search_Final.pptx):
  editable algorithm animation.
- [`source/ai_search_narration.txt`](source/ai_search_narration.txt):
  narration and timing guide.
- [`source/ai_search_subtitles.srt`](source/ai_search_subtitles.srt):
  subtitles for the algorithm clip.
- [`reflection.md`](reflection.md):
  short design and trade-off reflection.

## Running Version 2

Docker Compose is the recommended review route:

Windows PowerShell:

```powershell
Copy-Item .env.example .env
docker compose up -d --build
```

macOS or Linux:

```bash
cp .env.example .env
docker compose up -d --build
```

The example environment values are sufficient for local review. Email delivery
requires valid mail credentials, but the core project can run without them.

On first startup, Docker must download the base images and AI models, so the AI
service may take several minutes to become healthy. Monitor it with:

```bash
docker compose logs -f ai-search
```

After the AI service reports that all search backends are ready, verify the
containers:

```bash
docker compose ps
```

Open:

- Web UI: http://localhost:5500
- Mobile UI: http://localhost:5501
- Swagger UI: http://localhost:8080/swagger-ui/index.html

The main repository README contains the full Docker and non-Docker setup
instructions.

## Submission Notes

- The supplementary-material folder is submitted through the GitHub repository.
- The four MP4 deliverables are uploaded separately through the Microsoft Form.
- The local `project-video/videos/` directory is intentionally ignored by Git.
- The repository tags identify the assessed versions: `v1` and `v2`.
- All claims in this folder refer to repository evidence, recorded demonstrations
  or the linked acceptance checklist.
