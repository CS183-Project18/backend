SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS unique_finds
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE unique_finds;

CREATE TABLE IF NOT EXISTS moderation_logs (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'moderation log id',
  target_type ENUM('POST','COMMENT','USER') NOT NULL COMMENT 'moderation target type',
  target_id BIGINT UNSIGNED NOT NULL COMMENT 'moderation target id',
  moderator_id BIGINT UNSIGNED NOT NULL COMMENT 'moderator user id',
  action ENUM('APPROVE','REJECT','HIDE','UNHIDE','DELETE','BAN_USER','UNBAN_USER') NOT NULL COMMENT 'moderation action',
  reason VARCHAR(255) DEFAULT NULL COMMENT 'moderation reason',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  PRIMARY KEY (id),
  KEY idx_moderation_target (target_type, target_id, created_at),
  KEY idx_moderation_moderator_time (moderator_id, created_at),
  CONSTRAINT fk_moderation_moderator
    FOREIGN KEY (moderator_id) REFERENCES users(id)
    ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='moderation behavior log table';

CREATE TABLE IF NOT EXISTS reports (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'report id',
  reporter_id BIGINT UNSIGNED NOT NULL COMMENT 'reporter user id',
  target_type ENUM('POST','COMMENT','USER') NOT NULL COMMENT 'report target type',
  target_id BIGINT UNSIGNED NOT NULL COMMENT 'report target id',
  reason_type ENUM('SPAM','ILLEGAL','ABUSE','PORN','MISLEADING','OTHER') NOT NULL COMMENT 'report reason type',
  reason_detail VARCHAR(500) DEFAULT NULL COMMENT 'additional reason detail',
  status ENUM('PENDING','PROCESSING','RESOLVED','REJECTED') NOT NULL DEFAULT 'PENDING' COMMENT 'report status',
  handled_by BIGINT UNSIGNED DEFAULT NULL COMMENT 'handler user id',
  handled_at DATETIME DEFAULT NULL COMMENT 'handle time',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  PRIMARY KEY (id),
  KEY idx_reports_target_status (target_type, target_id, status),
  KEY idx_reports_reporter_time (reporter_id, created_at),
  KEY idx_reports_handled_by (handled_by),
  CONSTRAINT fk_reports_reporter
    FOREIGN KEY (reporter_id) REFERENCES users(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_reports_handler
    FOREIGN KEY (handled_by) REFERENCES users(id)
    ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='user report table';
