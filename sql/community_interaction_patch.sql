SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS unique_finds
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE unique_finds;

CREATE TABLE IF NOT EXISTS post_likes (
  user_id BIGINT UNSIGNED NOT NULL COMMENT 'user id',
  post_id BIGINT UNSIGNED NOT NULL COMMENT 'post id',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'like time',
  PRIMARY KEY (user_id, post_id),
  KEY idx_post_likes_post_time (post_id, created_at),
  CONSTRAINT fk_post_likes_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_post_likes_post
    FOREIGN KEY (post_id) REFERENCES posts(id)
    ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='post like relation table';

CREATE TABLE IF NOT EXISTS post_favorites (
  user_id BIGINT UNSIGNED NOT NULL COMMENT 'user id',
  post_id BIGINT UNSIGNED NOT NULL COMMENT 'post id',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'favorite time',
  PRIMARY KEY (user_id, post_id),
  KEY idx_post_fav_post_time (post_id, created_at),
  CONSTRAINT fk_post_fav_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_post_fav_post
    FOREIGN KEY (post_id) REFERENCES posts(id)
    ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='post favorite relation table';

CREATE TABLE IF NOT EXISTS comments (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'comment id',
  post_id BIGINT UNSIGNED NOT NULL COMMENT 'post id',
  user_id BIGINT UNSIGNED NOT NULL COMMENT 'comment user id',
  parent_id BIGINT UNSIGNED DEFAULT NULL COMMENT 'parent comment id',
  root_id BIGINT UNSIGNED DEFAULT NULL COMMENT 'root comment id',
  reply_to_user_id BIGINT UNSIGNED DEFAULT NULL COMMENT 'reply target user id',
  content VARCHAR(1000) NOT NULL COMMENT 'comment content',
  status ENUM('VISIBLE','HIDDEN','DELETED') NOT NULL DEFAULT 'VISIBLE' COMMENT 'comment status',
  like_count BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'comment like count',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  PRIMARY KEY (id),
  KEY idx_comments_post_time (post_id, created_at),
  KEY idx_comments_user_time (user_id, created_at),
  KEY idx_comments_parent (parent_id),
  KEY idx_comments_root (root_id),
  CONSTRAINT fk_comments_post
    FOREIGN KEY (post_id) REFERENCES posts(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_comments_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_comments_parent
    FOREIGN KEY (parent_id) REFERENCES comments(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_comments_root
    FOREIGN KEY (root_id) REFERENCES comments(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_comments_reply_to_user
    FOREIGN KEY (reply_to_user_id) REFERENCES users(id)
    ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='comment table';

DROP TRIGGER IF EXISTS trg_comments_after_insert;
DROP TRIGGER IF EXISTS trg_comments_after_update;
DROP TRIGGER IF EXISTS trg_comments_after_delete;
DROP TRIGGER IF EXISTS trg_post_likes_after_insert;
DROP TRIGGER IF EXISTS trg_post_likes_after_delete;
DROP TRIGGER IF EXISTS trg_post_favorites_after_insert;
DROP TRIGGER IF EXISTS trg_post_favorites_after_delete;

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

DELIMITER ;
