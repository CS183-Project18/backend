SET @add_notifications_metadata = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.columns
      WHERE table_schema = DATABASE()
        AND table_name = 'notifications'
        AND column_name = 'metadata'
    ),
    'SELECT 1',
    'ALTER TABLE notifications ADD COLUMN metadata JSON NULL COMMENT ''optional structured notification metadata'' AFTER post_id'
  )
);
PREPARE stmt_add_notifications_metadata FROM @add_notifications_metadata;
EXECUTE stmt_add_notifications_metadata;
DEALLOCATE PREPARE stmt_add_notifications_metadata;

SET @add_notifications_event_index = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.statistics
      WHERE table_schema = DATABASE()
        AND table_name = 'notifications'
        AND index_name = 'idx_notifications_recipient_event_time'
    ),
    'SELECT 1',
    'CREATE INDEX idx_notifications_recipient_event_time ON notifications (recipient_user_id, event_type, created_at)'
  )
);
PREPARE stmt_add_notifications_event_index FROM @add_notifications_event_index;
EXECUTE stmt_add_notifications_event_index;
DEALLOCATE PREPARE stmt_add_notifications_event_index;
