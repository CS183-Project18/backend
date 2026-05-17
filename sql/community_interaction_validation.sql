-- Author: Kaijie Zhu
-- Date: 2026-05-14
-- Purpose: Validate interaction schema objects, trigger-maintained counters, pinned ordering, and notification writes.

USE unique_finds;

SET @validation_owner_id = 910001;
SET @validation_actor_id = 910002;
SET @validation_admin_id = 910003;
SET @validation_post_id = 910001;
SET @validation_comment_a_id = 910001;
SET @validation_comment_b_id = 910002;

DELETE FROM notifications WHERE id >= 910001;
DELETE FROM comment_likes WHERE comment_id IN (@validation_comment_a_id, @validation_comment_b_id);
DELETE FROM comments WHERE id IN (@validation_comment_a_id, @validation_comment_b_id);
DELETE FROM post_likes WHERE post_id = @validation_post_id;
DELETE FROM post_favorites WHERE post_id = @validation_post_id;
DELETE FROM moderation_logs WHERE target_id IN (@validation_post_id, @validation_comment_a_id, @validation_comment_b_id);
DELETE FROM posts WHERE id = @validation_post_id;
DELETE FROM users WHERE id IN (@validation_owner_id, @validation_actor_id, @validation_admin_id);

INSERT INTO users (id, username, email, password_hash, nickname, role, status, email_verified)
VALUES
  (@validation_owner_id, 'validation_owner', 'validation_owner@example.com', '$2a$10$abcdefghijklmnopqrstuv', 'Validation Owner', 'USER', 'ACTIVE', 1),
  (@validation_actor_id, 'validation_actor', 'validation_actor@example.com', '$2a$10$abcdefghijklmnopqrstuv', 'Validation Actor', 'USER', 'ACTIVE', 1),
  (@validation_admin_id, 'validation_admin', 'validation_admin@example.com', '$2a$10$abcdefghijklmnopqrstuv', 'Validation Admin', 'ADMIN', 'ACTIVE', 1);

INSERT INTO posts (
  id, user_id, title, description, currency, status, like_count, favorite_count, comment_count, published_at
) VALUES (
  @validation_post_id, @validation_owner_id, 'Validation Post', 'Validation Post Body', 'CNY', 'PUBLISHED', 0, 0, 0, NOW()
);

INSERT INTO comments (
  id, post_id, user_id, parent_id, root_id, reply_to_user_id, content, status, like_count, is_pinned
) VALUES
  (@validation_comment_a_id, @validation_post_id, @validation_actor_id, NULL, NULL, NULL, 'first visible comment', 'VISIBLE', 0, 0),
  (@validation_comment_b_id, @validation_post_id, @validation_owner_id, NULL, NULL, NULL, 'second visible comment', 'VISIBLE', 0, 0);

SELECT 'schema_presence' AS check_name,
       EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'comments' AND column_name = 'is_pinned') AS has_is_pinned,
       EXISTS(SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'comment_likes') AS has_comment_likes_table,
       EXISTS(SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'notifications') AS has_notifications_table;

SELECT 'comment_count_after_insert' AS check_name,
       comment_count AS actual_comment_count
FROM posts
WHERE id = @validation_post_id;

INSERT INTO comment_likes (user_id, comment_id)
VALUES (@validation_admin_id, @validation_comment_a_id);

SELECT 'comment_like_after_insert' AS check_name,
       like_count AS actual_like_count
FROM comments
WHERE id = @validation_comment_a_id;

DELETE FROM comment_likes
WHERE user_id = @validation_admin_id
  AND comment_id = @validation_comment_a_id;

SELECT 'comment_like_after_delete' AS check_name,
       like_count AS actual_like_count
FROM comments
WHERE id = @validation_comment_a_id;

UPDATE comments
SET status = 'HIDDEN'
WHERE id = @validation_comment_b_id;

SELECT 'comment_count_after_hide' AS check_name,
       comment_count AS actual_comment_count
FROM posts
WHERE id = @validation_post_id;

UPDATE comments
SET status = 'VISIBLE',
    is_pinned = 1
WHERE id = @validation_comment_b_id;

SELECT 'pinned_comment_first' AS check_name,
       id,
       is_pinned
FROM comments
WHERE post_id = @validation_post_id
  AND status = 'VISIBLE'
ORDER BY is_pinned DESC, created_at ASC, id ASC;

INSERT INTO notifications (
  id, recipient_user_id, actor_user_id, event_type, target_type, target_id, post_id, is_read
) VALUES
  (910001, @validation_owner_id, @validation_actor_id, 'POST_LIKED', 'POST', @validation_post_id, @validation_post_id, 0),
  (910002, @validation_owner_id, @validation_actor_id, 'POST_FAVORITED', 'POST', @validation_post_id, @validation_post_id, 0),
  (910003, @validation_actor_id, @validation_owner_id, 'COMMENT_REPLIED', 'COMMENT', @validation_comment_a_id, @validation_post_id, 0),
  (910004, @validation_actor_id, @validation_admin_id, 'COMMENT_LIKED', 'COMMENT', @validation_comment_a_id, @validation_post_id, 0),
  (910005, @validation_actor_id, @validation_owner_id, 'COMMENT_PINNED', 'COMMENT', @validation_comment_b_id, @validation_post_id, 0),
  (910006, @validation_owner_id, @validation_admin_id, 'POST_MODERATED', 'POST', @validation_post_id, @validation_post_id, 0),
  (910007, @validation_actor_id, @validation_admin_id, 'COMMENT_MODERATED', 'COMMENT', @validation_comment_a_id, @validation_post_id, 0);

SELECT 'notification_event_types' AS check_name,
       event_type,
       COUNT(*) AS event_count
FROM notifications
WHERE id BETWEEN 910001 AND 910007
GROUP BY event_type
ORDER BY event_type;

DELETE FROM notifications WHERE id BETWEEN 910001 AND 910007;
DELETE FROM comment_likes WHERE comment_id IN (@validation_comment_a_id, @validation_comment_b_id);
DELETE FROM comments WHERE id IN (@validation_comment_a_id, @validation_comment_b_id);
DELETE FROM post_likes WHERE post_id = @validation_post_id;
DELETE FROM post_favorites WHERE post_id = @validation_post_id;
DELETE FROM moderation_logs WHERE target_id IN (@validation_post_id, @validation_comment_a_id, @validation_comment_b_id);
DELETE FROM posts WHERE id = @validation_post_id;
DELETE FROM users WHERE id IN (@validation_owner_id, @validation_actor_id, @validation_admin_id);
