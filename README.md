# Unique Finds Backend

Unique Finds is a discovery and sharing platform for interesting products found in physical stores. This repository contains the Java backend, database scripts, demo data, Docker deployment files, and the internal `ai-search` service used for semantic text search and image search.

## Current Backend Scope

- User registration and login
- User profile query and update
- Public user profile and public user post listing
- Post creation, update, delete, detail, public listing, and personal listing
- Post comments, likes, favorites, reports, moderation flow, and share links
- Comment likes and single-comment pinning per post
- Notification center for interaction and moderation events
- Public keyword search and trending posts
- Local image upload with public access URLs
- Lightweight interaction event ledger for post/comment/report/share/search activity
- Report audit fields for moderation outcome tracking
- Swagger / OpenAPI export for Apifox
- Docker Compose startup for MySQL, backend, internal AI search service, frontend, and mobile UI

## Project Structure

- `src/`: Java backend source code
- `sql/`: schema, patch, and demo seed scripts
- `ai-search/`: Python AI search service for semantic text search and image search
- `frontend/`: static frontend files
- `mobile/`: static mobile UI files
- `docker-compose.yml`: local multi-service startup file

## Local Run

### 1. Prepare database

For a brand-new database, import the full schema first:

```bash
mysql -u <username> -p < sql/unique_finds_full_schema.sql
```

Then start the backend once so Flyway can register and validate incremental migrations from `src/main/resources/db/migration`.
After Flyway finishes, import the demo seed data:

```bash
mysql -u <username> -p unique_finds < sql/demo_seed_data.sql
```

The current backend also expects Flyway to apply the latest governance/event migration so that:

- `reports` includes `resolutionAction` and `resolutionNote`
- `interaction_events` can store search, report-close, and share-link events with structured metadata

For an existing database that already has the base schema, keep the data in place and let Flyway baseline it on startup. If the environment is still missing the community interaction objects, you can apply the reference patch once before the first startup:

```bash
mysql -u <username> -p unique_finds < sql/community_interaction_patch.sql
mysql -u <username> -p unique_finds < sql/community_interaction_validation.sql
```

### 2. Configure local secrets

Create `src/main/resources/application-dev.yml` based on your own local environment. The repository ignores this file by default.

### 3. Start backend

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

### 4. Verify services

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Actuator health: `http://localhost:8080/actuator/health`
- Actuator info: `http://localhost:8080/actuator/info`
- Uploaded images: `http://localhost:8080/uploads/images/<fileName>`

## Docker Run

### 1. Prepare env file

Copy `.env.example` to `.env` and update values if needed.

### 2. Start all services

```bash
docker compose up --build
```

This will start:

- MySQL
- Java backend
- AI search service
- Static frontend UI
- Static mobile UI

After startup, open:

- Frontend UI: `http://localhost:5500`
- Mobile UI: `http://localhost:5501`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

The frontend and mobile pages call the backend at `http://localhost:8080` by default. The MySQL container uses port `3306` inside Docker only and is not exposed on the host, which avoids conflicts with a local MySQL installation.

## AI Search Architecture

- The Java backend is the only public API boundary. Frontend clients do not call `ai-search` directly.
- `GET /api/posts/search` keeps the same public contract, but now uses AI semantic search first when `keyword` is present and falls back to SQL search automatically when AI search is unavailable.
- `POST /api/posts/search/image` accepts a multipart image upload and returns the standard paginated post response.
- Image search degrades gracefully. When AI image search is unavailable, the backend returns a successful response with an empty `items` list and a readable message.
- The backend rebuilds the AI indices on startup and after searchable post content changes.

## Demo Accounts

The demo seed script creates the following accounts:

- `demo_alice`
- `demo_brian`
- `demo_cathy`
- `demo_derek`
- `demo_admin`

Default password for the seeded demo users:

```text
Password123
```

## Demo API Highlights

- `GET /api/posts/published`
- `GET /api/posts/{postId}`
- `POST /api/posts/{postId}/share`
- `GET /api/posts/search`
- `POST /api/posts/search/image`
- `GET /api/posts/trending`
- `GET /api/posts/tags/suggest`
- `GET /api/posts/{postId}/comments`
- `POST /api/comments/{commentId}/like`
- `POST /api/comments/{commentId}/pin`
- `GET /api/users/{username}/profile`
- `GET /api/users/{username}/posts`
- `GET /api/notifications`
- `GET /api/notifications/unread-count`
- `POST /api/files/images`
- `GET /api/admin/moderation/reports`
- `GET /api/admin/moderation/logs`
- `GET /api/admin/moderation/posts/pending`
- `GET /api/admin/analytics/overview`
- `GET /api/admin/analytics/consistency`

## Moderation and Audit Notes

- Guest users can read public post detail and public share metadata, but hidden, rejected, or deleted content is still blocked.
- Report responses now expose `resolutionAction`, `resolutionNote`, `targetStatus`, and `targetSummary`.
- `GET /api/admin/moderation/logs` returns audit rows with `moderatorUsername`, `targetSummary`, `reason`, and `createdAt`, and supports `targetType`, `targetId`, `moderatorId`, `action`, `startTime`, and `endTime` filters.
- User profile responses now expose `postCount`, `publishedPostCount`, `commentCount`, and `favoriteCount`.
- Uploads require both an allowed image MIME type and a matching filename extension.

## Media and Edit Policy

- Image upload responses include `url`, `thumbnailUrl`, `contentType`, `size`, `width`, and `height` when dimensions can be read by the JDK image readers.
- The backend keeps `thumbnailUrl` stable. If a real thumbnail is not generated, it falls back to the original image URL.
- Each post supports up to 9 images. The first image is treated as the cover, and post updates replace the full image list and sort order.
- Editing a `PUBLISHED` post keeps it published and immediately triggers search index sync. Editing `PENDING_REVIEW`, `REJECTED`, or `HIDDEN` posts keeps their existing moderation state.

## Interaction API Contract

### Public endpoints

- `GET /api/posts/{postId}`
- `GET /api/posts/{postId}/comments`
- `GET /api/users/{username}/profile`
- `GET /api/users/{username}/posts`
- `POST /api/posts/{postId}/share`

### Authenticated user endpoints

- `POST /api/comments/{commentId}/like`
- `DELETE /api/comments/{commentId}/like`
- `GET /api/notifications`
- `GET /api/notifications/unread-count`
- `POST /api/notifications/{notificationId}/read`
- `POST /api/notifications/read-all`

`GET /api/notifications` also supports the optional query parameter `eventType`.
Notification items include `message`, `targetSummary`, and `metadata` so the frontend can render cards without extra lookups.

### Post owner or admin endpoints

- `POST /api/comments/{commentId}/pin`
- `DELETE /api/comments/{commentId}/pin`

### Response fields added in this phase

- Post response: `shareUrl`
- Comment response: `likeCount`, `likedByCurrentUser`, `pinned`
- Common result wrapper: `code`
- Page response: optional `metadata` for search source, trending fallback window, and image-search degradation details

### Notification event types

- `POST_LIKED`
- `POST_FAVORITED`
- `COMMENT_REPLIED`
- `COMMENT_LIKED`
- `COMMENT_PINNED`
- `POST_MODERATED`
- `COMMENT_MODERATED`
- `REPORT_RESOLVED`
- `REPORT_REJECTED`

## Analytics Notes

- `GET /api/admin/analytics/overview` returns real database counters for users, created posts, published posts, comments, favorites, reports, pending reports, search requests, share events, interaction events, report resolution hours, and moderation action counts.
- `GET /api/admin/analytics/trends` and `GET /api/admin/analytics/distribution` keep the existing trend and ranking contract.
- `GET /api/admin/analytics/consistency` exposes lightweight data-quality counters for orphan images/tags and missing public content references.
- `GET /api/posts/tags/suggest` is a rule-based backend suggestion endpoint for V2. It is intentionally not presented as AI image/text understanding; a future version can connect it to an AI model.

## AI Search Notes

- `ai-search` is an internal service intended to run behind the backend on the Docker network.
- `POST /build_index` rebuilds both semantic and image indices from backend-provided published post data.
- `GET /semantic_search` returns AI-ranked post ids for text queries.
- `POST /image_search` returns AI-ranked post ids for uploaded images.

### First-run dependency and model downloads

- The AI search container installs large Python packages, including `torch`, `transformers`, `sentence-transformers`, and `faiss-cpu`.
- On first startup, the semantic and image search modules may also download pretrained models from Hugging Face, including `sentence-transformers/all-MiniLM-L6-v2` and `openai/clip-vit-base-patch32`.
- If the network cannot reach PyPI or Hugging Face, the AI search service may take a long time to build/start or may be unavailable.
- The Java backend is designed to degrade gracefully: keyword search falls back to SQL search, and image search returns an empty successful response with a readable message when the AI service is unavailable.
