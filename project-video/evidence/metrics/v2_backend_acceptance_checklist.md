# V2 Backend Acceptance Checklist

Use this checklist in Apifox after the backend is running with the `dev` profile and `sql/demo_seed_data.sql` has been imported after Flyway migrations.

## Guest Flow

- `GET /api/posts/published?page=1&pageSize=10` returns published posts with image metadata and thumbnail URLs.
- `GET /api/posts/{postId}` works for published posts and rejects hidden, rejected, or deleted posts.
- `GET /api/posts/search?keyword=mug&page=1&pageSize=10` returns a normal page and includes `metadata.searchSource`.
- `GET /api/posts/trending?window=daily&page=1&pageSize=10` returns demo trending data and includes requested/effective window metadata.
- `GET /api/users/demo_alice/profile` and `GET /api/users/demo_alice/posts` return public profile data and published posts only.

## User Flow

- Register or log in with a demo user, then call authenticated endpoints with `Authorization: Bearer <token>`.
- `POST /api/files/images` with jpeg/png/webp returns `url`, `thumbnailUrl`, `size`, `width`, and `height`.
- `POST /api/posts` creates a `PENDING_REVIEW` post with up to 9 images.
- `PUT /api/posts/{postId}` replaces the whole image list; published posts stay `PUBLISHED`, while pending/rejected/hidden posts keep their current status.
- `POST /api/posts/{postId}/like`, `DELETE /api/posts/{postId}/like`, `POST /api/posts/{postId}/favorite`, and `DELETE /api/posts/{postId}/favorite` are idempotent enough for repeated UI clicks.
- `POST /api/posts/{postId}/report` and `POST /api/comments/{commentId}/report` create report rows.

## Notification Flow

- `GET /api/notifications` returns items with `message`, `targetSummary`, and `metadata`.
- `GET /api/notifications/unread-count` matches unread notification rows.
- `POST /api/notifications/{notificationId}/read` marks one item as read.
- `POST /api/notifications/read-all` clears the unread count.

## Author Flow

- Post authors can edit or delete their own posts.
- Post authors can pin and unpin one visible comment under their own post.
- Non-authors cannot edit another user's post or pin comments on another user's post.

## Admin Flow

- Log in as `demo_admin` from the seed data.
- `GET /api/admin/moderation/posts/pending` returns the pending demo post.
- `POST /api/admin/moderation/posts/{postId}/approve`, `/reject`, and `/hide` produce moderation logs and notifications.
- `GET /api/admin/moderation/reports` returns post and comment reports with `targetSummary`.
- `POST /api/admin/moderation/reports/{reportId}/resolve` and `/reject` close reports and notify reporters.
- `GET /api/admin/moderation/logs` supports filtering by `targetType`, `targetId`, `moderatorId`, `action`, `startTime`, and `endTime`.

## Analytics Flow

- `GET /api/admin/analytics/overview` returns non-empty demo counts for posts, reports, search requests, shares, interactions, and moderation actions.
- `GET /api/admin/analytics/trends?window=weekly` returns dated post/comment/favorite/report series.
- `GET /api/admin/analytics/distribution` returns report reasons and top category/tag/store rankings.
- `GET /api/admin/analytics/consistency` returns consistency counters without throwing errors.

## AI Boundary

- `GET /api/posts/tags/suggest` returns rule-based suggestions only.
- `GET /api/posts/search` should still work if `ai-search` is unavailable by falling back to SQL.
- `POST /api/posts/search/image` should return success with empty items and clear metadata if image search is unavailable.
