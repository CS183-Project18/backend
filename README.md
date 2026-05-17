# Unique Finds Backend

Unique Finds is a discovery and sharing platform for interesting products found in physical stores. This repository contains the Java backend, database scripts, demo data, Docker deployment files, and a lightweight AI service placeholder used for local end-to-end demonstration.

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
- Docker Compose startup for MySQL, backend, and AI placeholder service

## Project Structure

- `src/`: Java backend source code
- `sql/`: schema, patch, and demo seed scripts
- `ai-service/`: Python AI placeholder service reserved for teammate implementation
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
- AI placeholder service

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

## AI Placeholder Note

The `ai-service/` folder currently contains a lightweight placeholder service that only keeps the Docker and local demo chain complete. The real semantic and multimodal search implementation is reserved for the teammate responsible for AI work.
