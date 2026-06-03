SET @schema_name = DATABASE();

SET @sql = (
    SELECT IF(
        EXISTS(
            SELECT 1
            FROM information_schema.statistics
            WHERE table_schema = @schema_name
              AND table_name = 'reports'
              AND index_name = 'uk_reports_open_submission'
        ),
        'SELECT 1',
        'ALTER TABLE reports ADD UNIQUE KEY uk_reports_open_submission (reporter_id, target_type, target_id, status)'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        EXISTS(
            SELECT 1
            FROM information_schema.statistics
            WHERE table_schema = @schema_name
              AND table_name = 'reports'
              AND index_name = 'idx_reports_status_created'
        ),
        'SELECT 1',
        'ALTER TABLE reports ADD KEY idx_reports_status_created (status, created_at)'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        EXISTS(
            SELECT 1
            FROM information_schema.statistics
            WHERE table_schema = @schema_name
              AND table_name = 'reports'
              AND index_name = 'idx_reports_reporter_target_status'
        ),
        'SELECT 1',
        'ALTER TABLE reports ADD KEY idx_reports_reporter_target_status (reporter_id, target_type, target_id, status)'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        EXISTS(
            SELECT 1
            FROM information_schema.statistics
            WHERE table_schema = @schema_name
              AND table_name = 'posts'
              AND index_name = 'idx_posts_status_category_pubtime'
        ),
        'SELECT 1',
        'ALTER TABLE posts ADD KEY idx_posts_status_category_pubtime (status, category_id, published_at)'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        EXISTS(
            SELECT 1
            FROM information_schema.statistics
            WHERE table_schema = @schema_name
              AND table_name = 'posts'
              AND index_name = 'idx_posts_status_store_pubtime'
        ),
        'SELECT 1',
        'ALTER TABLE posts ADD KEY idx_posts_status_store_pubtime (status, store_id, published_at)'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
