SET @sql = IF(
    EXISTS(
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'reports'
          AND COLUMN_NAME = 'resolution_action'
    ),
    'SELECT 1',
    'ALTER TABLE reports ADD COLUMN resolution_action VARCHAR(32) NULL COMMENT ''final moderation action for report closure'' AFTER status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS(
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'reports'
          AND COLUMN_NAME = 'resolution_note'
    ),
    'SELECT 1',
    'ALTER TABLE reports ADD COLUMN resolution_note VARCHAR(500) NULL COMMENT ''final moderation note for report closure'' AFTER resolution_action'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS(
        SELECT 1
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'reports'
          AND INDEX_NAME = 'idx_reports_status_handled_time'
    ),
    'SELECT 1',
    'ALTER TABLE reports ADD INDEX idx_reports_status_handled_time (status, handled_at)'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS(
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'interaction_events'
          AND COLUMN_NAME = 'event_type'
          AND DATA_TYPE = 'varchar'
    ),
    'SELECT 1',
    'ALTER TABLE interaction_events MODIFY COLUMN event_type VARCHAR(40) NOT NULL COMMENT ''event type'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS(
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'interaction_events'
          AND COLUMN_NAME = 'comment_id'
    ),
    'SELECT 1',
    'ALTER TABLE interaction_events ADD COLUMN comment_id BIGINT UNSIGNED NULL COMMENT ''comment id'' AFTER post_id'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS(
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'interaction_events'
          AND COLUMN_NAME = 'target_type'
    ),
    'SELECT 1',
    'ALTER TABLE interaction_events ADD COLUMN target_type VARCHAR(20) NULL COMMENT ''target type'' AFTER comment_id'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS(
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'interaction_events'
          AND COLUMN_NAME = 'target_id'
    ),
    'SELECT 1',
    'ALTER TABLE interaction_events ADD COLUMN target_id BIGINT UNSIGNED NULL COMMENT ''target id'' AFTER target_type'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS(
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'interaction_events'
          AND COLUMN_NAME = 'metadata'
    ),
    'SELECT 1',
    'ALTER TABLE interaction_events ADD COLUMN metadata JSON NULL COMMENT ''structured event metadata'' AFTER event_value'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS(
        SELECT 1
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'interaction_events'
          AND INDEX_NAME = 'idx_events_target_time'
    ),
    'SELECT 1',
    'ALTER TABLE interaction_events ADD INDEX idx_events_target_time (target_type, target_id, event_time)'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS(
        SELECT 1
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'interaction_events'
          AND INDEX_NAME = 'idx_events_comment_time'
    ),
    'SELECT 1',
    'ALTER TABLE interaction_events ADD INDEX idx_events_comment_time (comment_id, event_time)'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
