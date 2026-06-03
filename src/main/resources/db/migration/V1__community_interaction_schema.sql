SET @add_comments_is_pinned = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.columns
      WHERE table_schema = DATABASE()
        AND table_name = 'comments'
        AND column_name = 'is_pinned'
    ),
    'SELECT 1',
    'ALTER TABLE comments ADD COLUMN is_pinned TINYINT(1) NOT NULL DEFAULT 0 COMMENT ''whether the comment is pinned'' AFTER like_count'
  )
);
PREPARE stmt_add_comments_is_pinned FROM @add_comments_is_pinned;
EXECUTE stmt_add_comments_is_pinned;
DEALLOCATE PREPARE stmt_add_comments_is_pinned;

CREATE TABLE IF NOT EXISTS comment_likes (
  user_id BIGINT UNSIGNED NOT NULL COMMENT 'user id',
  comment_id BIGINT UNSIGNED NOT NULL COMMENT 'comment id',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'like time',
  PRIMARY KEY (user_id, comment_id),
  KEY idx_comment_likes_comment_time (comment_id, created_at),
  CONSTRAINT fk_comment_likes_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_comment_likes_comment
    FOREIGN KEY (comment_id) REFERENCES comments(id)
    ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='comment like relation table';

CREATE TABLE IF NOT EXISTS notifications (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'notification id',
  recipient_user_id BIGINT UNSIGNED NOT NULL COMMENT 'recipient user id',
  actor_user_id BIGINT UNSIGNED DEFAULT NULL COMMENT 'actor user id',
  event_type VARCHAR(40) NOT NULL COMMENT 'notification event type',
  target_type ENUM('POST','COMMENT') NOT NULL COMMENT 'target type',
  target_id BIGINT UNSIGNED NOT NULL COMMENT 'target id',
  post_id BIGINT UNSIGNED DEFAULT NULL COMMENT 'related post id',
  is_read TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'read status',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  PRIMARY KEY (id),
  KEY idx_notifications_recipient_time (recipient_user_id, created_at),
  KEY idx_notifications_recipient_read (recipient_user_id, is_read),
  CONSTRAINT fk_notifications_recipient
    FOREIGN KEY (recipient_user_id) REFERENCES users(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_notifications_actor
    FOREIGN KEY (actor_user_id) REFERENCES users(id)
    ON DELETE SET NULL,
  CONSTRAINT fk_notifications_post
    FOREIGN KEY (post_id) REFERENCES posts(id)
    ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='notification table';

DROP TRIGGER IF EXISTS trg_comments_after_insert;
DROP TRIGGER IF EXISTS trg_comments_after_update;
DROP TRIGGER IF EXISTS trg_comments_after_delete;
DROP TRIGGER IF EXISTS trg_post_likes_after_insert;
DROP TRIGGER IF EXISTS trg_post_likes_after_delete;
DROP TRIGGER IF EXISTS trg_post_favorites_after_insert;
DROP TRIGGER IF EXISTS trg_post_favorites_after_delete;
DROP TRIGGER IF EXISTS trg_comment_likes_after_insert;
DROP TRIGGER IF EXISTS trg_comment_likes_after_delete;

DELIMITER $$

CREATE TRIGGER trg_comments_after_insert
AFTER INSERT ON comments
FOR EACH ROW
BEGIN
  IF NEW.status = 'VISIBLE' THEN
    UPDATE posts
       SET comment_count = comment_count + 1
     WHERE id = NEW.post_id;
  END IF;
END$$

CREATE TRIGGER trg_comments_after_update
AFTER UPDATE ON comments
FOR EACH ROW
BEGIN
  IF OLD.status = 'VISIBLE' AND NEW.status IN ('HIDDEN', 'DELETED') THEN
    UPDATE posts
       SET comment_count = IF(comment_count > 0, comment_count - 1, 0)
     WHERE id = NEW.post_id;
  ELSEIF OLD.status IN ('HIDDEN', 'DELETED') AND NEW.status = 'VISIBLE' THEN
    UPDATE posts
       SET comment_count = comment_count + 1
     WHERE id = NEW.post_id;
  END IF;
END$$

CREATE TRIGGER trg_comments_after_delete
AFTER DELETE ON comments
FOR EACH ROW
BEGIN
  IF OLD.status = 'VISIBLE' THEN
    UPDATE posts
       SET comment_count = IF(comment_count > 0, comment_count - 1, 0)
     WHERE id = OLD.post_id;
  END IF;
END$$

CREATE TRIGGER trg_post_likes_after_insert
AFTER INSERT ON post_likes
FOR EACH ROW
BEGIN
  UPDATE posts
     SET like_count = like_count + 1
   WHERE id = NEW.post_id;
END$$

CREATE TRIGGER trg_post_likes_after_delete
AFTER DELETE ON post_likes
FOR EACH ROW
BEGIN
  UPDATE posts
     SET like_count = IF(like_count > 0, like_count - 1, 0)
   WHERE id = OLD.post_id;
END$$

CREATE TRIGGER trg_post_favorites_after_insert
AFTER INSERT ON post_favorites
FOR EACH ROW
BEGIN
  UPDATE posts
     SET favorite_count = favorite_count + 1
   WHERE id = NEW.post_id;
END$$

CREATE TRIGGER trg_post_favorites_after_delete
AFTER DELETE ON post_favorites
FOR EACH ROW
BEGIN
  UPDATE posts
     SET favorite_count = IF(favorite_count > 0, favorite_count - 1, 0)
   WHERE id = OLD.post_id;
END$$

CREATE TRIGGER trg_comment_likes_after_insert
AFTER INSERT ON comment_likes
FOR EACH ROW
BEGIN
  UPDATE comments
     SET like_count = like_count + 1
   WHERE id = NEW.comment_id;
END$$

CREATE TRIGGER trg_comment_likes_after_delete
AFTER DELETE ON comment_likes
FOR EACH ROW
BEGIN
  UPDATE comments
     SET like_count = IF(like_count > 0, like_count - 1, 0)
   WHERE id = OLD.comment_id;
END$$

DELIMITER ;
