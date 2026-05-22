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
- Docker Compose startup for MySQL, backend, and internal AI search service

## Project Structure

- `src/`: Java backend source code
- `sql/`: schema, patch, and demo seed scripts
- `ai-search/`: Python AI search service for semantic text search and image search
- `frontend/`: static frontend files
- `docker-compose.yml`: local multi-service startup file

## Local Run

### 1. Prepare database

For a brand-new database, import the full schema and demo seed data first:

```bash
mysql -u <username> -p < sql/unique_finds_full_schema.sql
mysql -u <username> -p < sql/demo_seed_data.sql
```

Then start the backend once so Flyway can register and validate incremental migrations from `src/main/resources/db/migration`.

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
- `GET /api/posts/{postId}/comments`
- `POST /api/comments/{commentId}/like`
- `POST /api/comments/{commentId}/pin`
- `GET /api/users/{username}/profile`
- `GET /api/users/{username}/posts`
- `GET /api/notifications`
- `GET /api/notifications/unread-count`
- `POST /api/files/images`
- `GET /api/admin/moderation/reports`
- `GET /api/admin/moderation/posts/pending`

## Moderation and Audit Notes

- Guest users can read public post detail and public share metadata, but hidden, rejected, or deleted content is still blocked.
- Report responses now expose `resolutionAction`, `resolutionNote`, and `targetStatus`.
- User profile responses now expose `postCount`, `publishedPostCount`, `commentCount`, and `favoriteCount`.
- Uploads require both an allowed image MIME type and a matching filename extension.

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

### Post owner or admin endpoints

- `POST /api/comments/{commentId}/pin`
- `DELETE /api/comments/{commentId}/pin`

### Response fields added in this phase

- Post response: `shareUrl`
- Comment response: `likeCount`, `likedByCurrentUser`, `pinned`
- Common result wrapper: `code`

### Notification event types

- `POST_LIKED`
- `POST_FAVORITED`
- `COMMENT_REPLIED`
- `COMMENT_LIKED`
- `COMMENT_PINNED`
- `POST_MODERATED`
- `COMMENT_MODERATED`

## AI Search Notes

- `ai-search` is an internal service intended to run behind the backend on the Docker network.
- `POST /build_index` rebuilds both semantic and image indices from backend-provided published post data.
- `GET /semantic_search` returns AI-ranked post ids for text queries.
- `POST /image_search` returns AI-ranked post ids for uploaded images.
