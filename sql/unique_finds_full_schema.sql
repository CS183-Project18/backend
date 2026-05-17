/* ============================================================
   Unique Finds - Full Database Schema (MySQL 8.0+)
   目标：用户鉴权 + 帖子CRUD + 社区互动 + 热榜 + AI搜索预留
   作者建议：直接作为 V1 基线库
   ============================================================ */

-- 可按需开启严格模式
SET NAMES utf8mb4;
SET time_zone = '+00:00';

CREATE DATABASE IF NOT EXISTS unique_finds
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE unique_finds;

/* ============================================================
   0) 通用说明
   - 所有时间使用 DATETIME（UTC）
   - 业务删除优先软删（status），极端场景再物理删除
   - 互动表使用唯一约束防重复点赞/收藏
   ============================================================ */


/* ============================================================
   1) 用户与鉴权模块
   ============================================================ */

CREATE TABLE IF NOT EXISTS users (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  username VARCHAR(50) NOT NULL COMMENT '用户名，登录名之一',
  email VARCHAR(120) NOT NULL COMMENT '邮箱，登录名之一',
  password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希（BCrypt/Argon2）',
  nickname VARCHAR(80) DEFAULT NULL COMMENT '昵称',
  avatar_url VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
  bio VARCHAR(500) DEFAULT NULL COMMENT '个性签名',
  role ENUM('USER','MODERATOR','ADMIN') NOT NULL DEFAULT 'USER' COMMENT '角色',
  status ENUM('ACTIVE','BANNED','DELETED') NOT NULL DEFAULT 'ACTIVE' COMMENT '账号状态',
  email_verified TINYINT(1) NOT NULL DEFAULT 0 COMMENT '邮箱是否验证',
  last_login_at DATETIME DEFAULT NULL COMMENT '最后登录时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_username (username),
  UNIQUE KEY uk_users_email (email),
  KEY idx_users_role_status (role, status),
  KEY idx_users_created_at (created_at)
) ENGINE=InnoDB COMMENT='用户主表';

CREATE TABLE IF NOT EXISTS user_auth_identities (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '认证凭据ID',
  user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  identity_type ENUM('USERNAME','EMAIL') NOT NULL COMMENT '凭据类型',
  identity_value VARCHAR(150) NOT NULL COMMENT '凭据值，如username/email',
  is_primary TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否主凭据',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_auth_identity_type_value (identity_type, identity_value),
  KEY idx_auth_identity_user (user_id),
  CONSTRAINT fk_auth_identity_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='用户登录凭据映射（可扩展多种登录方式）';

CREATE TABLE IF NOT EXISTS verification_codes (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '验证码记录ID',
  user_id BIGINT UNSIGNED DEFAULT NULL COMMENT '关联用户ID（可空：注册前发码）',
  target VARCHAR(150) NOT NULL COMMENT '发送目标（邮箱）',
  channel ENUM('EMAIL') NOT NULL DEFAULT 'EMAIL' COMMENT '验证码通道',
  purpose ENUM('REGISTER','LOGIN','RESET_PASSWORD','BIND_EMAIL') NOT NULL COMMENT '验证码用途',
  code VARCHAR(12) NOT NULL COMMENT '验证码明文/哈希（建议生产存哈希）',
  expires_at DATETIME NOT NULL COMMENT '过期时间',
  used_at DATETIME DEFAULT NULL COMMENT '使用时间',
  attempt_count INT NOT NULL DEFAULT 0 COMMENT '校验尝试次数',
  max_attempts INT NOT NULL DEFAULT 5 COMMENT '最大尝试次数',
  status ENUM('PENDING','USED','EXPIRED','LOCKED') NOT NULL DEFAULT 'PENDING' COMMENT '状态',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_codes_target_purpose (target, purpose),
  KEY idx_codes_status_expires (status, expires_at),
  KEY idx_codes_user_id (user_id),
  CONSTRAINT fk_codes_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='验证码表（登录/注册/找回密码）';

CREATE TABLE IF NOT EXISTS user_sessions (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  refresh_token_hash VARCHAR(255) NOT NULL COMMENT 'RefreshToken哈希',
  device_id VARCHAR(100) DEFAULT NULL COMMENT '设备标识',
  user_agent VARCHAR(500) DEFAULT NULL COMMENT 'UA',
  ip_address VARCHAR(64) DEFAULT NULL COMMENT 'IP地址',
  login_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  expires_at DATETIME NOT NULL COMMENT '会话过期时间',
  revoked_at DATETIME DEFAULT NULL COMMENT '注销时间',
  revoke_reason VARCHAR(120) DEFAULT NULL COMMENT '注销原因',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sessions_refresh_hash (refresh_token_hash),
  KEY idx_sessions_user_id (user_id),
  KEY idx_sessions_expires_at (expires_at),
  CONSTRAINT fk_sessions_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='登录会话表（支持多端登录与强制下线）';

CREATE TABLE IF NOT EXISTS auth_audit_logs (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '审计日志ID',
  user_id BIGINT UNSIGNED DEFAULT NULL COMMENT '用户ID（匿名时为空）',
  action ENUM('REGISTER','LOGIN_SUCCESS','LOGIN_FAIL','LOGOUT','TOKEN_REFRESH','PASSWORD_RESET') NOT NULL COMMENT '鉴权动作',
  identifier VARCHAR(150) DEFAULT NULL COMMENT '登录标识（用户名/邮箱）',
  ip_address VARCHAR(64) DEFAULT NULL COMMENT 'IP地址',
  user_agent VARCHAR(500) DEFAULT NULL COMMENT 'UA',
  result_code VARCHAR(50) DEFAULT NULL COMMENT '结果码',
  result_message VARCHAR(255) DEFAULT NULL COMMENT '结果描述',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发生时间',
  PRIMARY KEY (id),
  KEY idx_auth_audit_user_time (user_id, created_at),
  KEY idx_auth_audit_action_time (action, created_at)
) ENGINE=InnoDB COMMENT='鉴权审计日志';


/* ============================================================
   2) 店铺、分类、标签、帖子（核心内容）
   ============================================================ */

CREATE TABLE IF NOT EXISTS stores (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '店铺ID',
  name VARCHAR(150) NOT NULL COMMENT '店铺名称',
  branch_name VARCHAR(150) DEFAULT NULL COMMENT '分店名',
  city VARCHAR(80) DEFAULT NULL COMMENT '城市',
  district VARCHAR(80) DEFAULT NULL COMMENT '区县',
  address VARCHAR(255) DEFAULT NULL COMMENT '详细地址',
  latitude DECIMAL(10,7) DEFAULT NULL COMMENT '纬度',
  longitude DECIMAL(10,7) DEFAULT NULL COMMENT '经度',
  phone VARCHAR(30) DEFAULT NULL COMMENT '联系方式',
  business_hours VARCHAR(120) DEFAULT NULL COMMENT '营业时间',
  status ENUM('ACTIVE','HIDDEN','CLOSED') NOT NULL DEFAULT 'ACTIVE' COMMENT '店铺状态',
  created_by BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_store_name_addr (name, address),
  KEY idx_store_city_district (city, district),
  KEY idx_store_status (status),
  KEY idx_store_geo (latitude, longitude),
  CONSTRAINT fk_store_creator
    FOREIGN KEY (created_by) REFERENCES users(id)
    ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='实体店铺信息';

CREATE TABLE IF NOT EXISTS categories (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  parent_id BIGINT UNSIGNED DEFAULT NULL COMMENT '父级分类ID',
  name VARCHAR(80) NOT NULL COMMENT '分类名称',
  level TINYINT NOT NULL DEFAULT 1 COMMENT '层级（1/2/3）',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序值',
  is_active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_category_parent_name (parent_id, name),
  KEY idx_category_parent (parent_id),
  KEY idx_category_active_sort (is_active, sort_order),
  CONSTRAINT fk_category_parent
    FOREIGN KEY (parent_id) REFERENCES categories(id)
    ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='商品分类（支持树形）';

CREATE TABLE IF NOT EXISTS tags (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  name VARCHAR(60) NOT NULL COMMENT '标签名',
  heat_score DECIMAL(12,4) NOT NULL DEFAULT 0 COMMENT '标签热度（可选冗余）',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_tags_name (name)
) ENGINE=InnoDB COMMENT='标签字典';

CREATE TABLE IF NOT EXISTS posts (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
  user_id BIGINT UNSIGNED NOT NULL COMMENT '发布者用户ID',
  store_id BIGINT UNSIGNED DEFAULT NULL COMMENT '关联店铺ID',
  category_id BIGINT UNSIGNED DEFAULT NULL COMMENT '主分类ID',
  title VARCHAR(200) NOT NULL COMMENT '标题',
  description TEXT NOT NULL COMMENT '正文描述',
  price_min DECIMAL(10,2) DEFAULT NULL COMMENT '最低价',
  price_max DECIMAL(10,2) DEFAULT NULL COMMENT '最高价',
  currency CHAR(3) NOT NULL DEFAULT 'CNY' COMMENT '币种',
  location_text VARCHAR(255) DEFAULT NULL COMMENT '发布时填写的位置文本',
  status ENUM('PENDING_REVIEW','PUBLISHED','HIDDEN','REJECTED','DELETED') NOT NULL DEFAULT 'PENDING_REVIEW' COMMENT '帖子状态',
  moderation_reason VARCHAR(255) DEFAULT NULL COMMENT '审核拒绝/屏蔽原因',

  -- 冗余计数，提升读性能
  view_count BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '浏览数',
  like_count BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '点赞数',
  favorite_count BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '收藏数',
  comment_count BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '评论数',

  published_at DATETIME DEFAULT NULL COMMENT '发布时间（审核通过后）',
  deleted_at DATETIME DEFAULT NULL COMMENT '软删时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  PRIMARY KEY (id),
  KEY idx_posts_user (user_id),
  KEY idx_posts_store (store_id),
  KEY idx_posts_category (category_id),
  KEY idx_posts_status_pubtime (status, published_at),
  KEY idx_posts_created (created_at),
  FULLTEXT KEY ftx_posts_title_desc (title, description),

  CONSTRAINT fk_posts_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_posts_store
    FOREIGN KEY (store_id) REFERENCES stores(id)
    ON DELETE SET NULL,
  CONSTRAINT fk_posts_category
    FOREIGN KEY (category_id) REFERENCES categories(id)
    ON DELETE SET NULL,

  CONSTRAINT chk_posts_price_range
    CHECK (
      (price_min IS NULL AND price_max IS NULL)
      OR (price_min IS NOT NULL AND price_max IS NULL)
      OR (price_min IS NULL AND price_max IS NOT NULL)
      OR (price_min <= price_max)
    )
) ENGINE=InnoDB COMMENT='好物帖子主表';

CREATE TABLE IF NOT EXISTS post_images (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '图片ID',
  post_id BIGINT UNSIGNED NOT NULL COMMENT '帖子ID',
  image_url VARCHAR(500) NOT NULL COMMENT '原图URL',
  image_key VARCHAR(255) DEFAULT NULL COMMENT '对象存储Key',
  thumbnail_url VARCHAR(500) DEFAULT NULL COMMENT '缩略图URL',
  width INT DEFAULT NULL COMMENT '宽',
  height INT DEFAULT NULL COMMENT '高',
  file_size BIGINT UNSIGNED DEFAULT NULL COMMENT '文件大小Bytes',
  mime_type VARCHAR(80) DEFAULT NULL COMMENT '图片类型',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序（封面=最小）',
  is_cover TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否封面',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_post_images_post_sort (post_id, sort_order),
  KEY idx_post_images_cover (post_id, is_cover),
  CONSTRAINT fk_post_images_post
    FOREIGN KEY (post_id) REFERENCES posts(id)
    ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='帖子图片表（支持多图）';

CREATE TABLE IF NOT EXISTS post_tags (
  post_id BIGINT UNSIGNED NOT NULL COMMENT '帖子ID',
  tag_id BIGINT UNSIGNED NOT NULL COMMENT '标签ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (post_id, tag_id),
  KEY idx_post_tags_tag (tag_id),
  CONSTRAINT fk_post_tags_post
    FOREIGN KEY (post_id) REFERENCES posts(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_post_tags_tag
    FOREIGN KEY (tag_id) REFERENCES tags(id)
    ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='帖子-标签多对多关系';


/* ============================================================
   3) 社区互动（点赞/收藏/评论/浏览）
   ============================================================ */

CREATE TABLE IF NOT EXISTS post_likes (
  user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  post_id BIGINT UNSIGNED NOT NULL COMMENT '帖子ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (user_id, post_id),
  KEY idx_post_likes_post_time (post_id, created_at),
  CONSTRAINT fk_post_likes_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_post_likes_post
    FOREIGN KEY (post_id) REFERENCES posts(id)
    ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='帖子点赞关系（防重复点赞）';

CREATE TABLE IF NOT EXISTS post_favorites (
  user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  post_id BIGINT UNSIGNED NOT NULL COMMENT '帖子ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (user_id, post_id),
  KEY idx_post_fav_post_time (post_id, created_at),
  CONSTRAINT fk_post_fav_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_post_fav_post
    FOREIGN KEY (post_id) REFERENCES posts(id)
    ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='帖子收藏关系（防重复收藏）';

CREATE TABLE IF NOT EXISTS comments (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  post_id BIGINT UNSIGNED NOT NULL COMMENT '帖子ID',
  user_id BIGINT UNSIGNED NOT NULL COMMENT '评论用户ID',
  parent_id BIGINT UNSIGNED DEFAULT NULL COMMENT '父评论ID（回复）',
  root_id BIGINT UNSIGNED DEFAULT NULL COMMENT '根评论ID（便于楼中楼）',
  reply_to_user_id BIGINT UNSIGNED DEFAULT NULL COMMENT '被回复用户ID',
  content VARCHAR(1000) NOT NULL COMMENT '评论内容',
  status ENUM('VISIBLE','HIDDEN','DELETED') NOT NULL DEFAULT 'VISIBLE' COMMENT '评论状态',
  like_count BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '评论点赞数（可选）',
  is_pinned TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否置顶',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
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
) ENGINE=InnoDB COMMENT='评论表（支持回复与楼中楼）';

CREATE TABLE IF NOT EXISTS comment_likes (
  user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  comment_id BIGINT UNSIGNED NOT NULL COMMENT '评论ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (user_id, comment_id),
  KEY idx_comment_likes_comment_time (comment_id, created_at),
  CONSTRAINT fk_comment_likes_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_comment_likes_comment
    FOREIGN KEY (comment_id) REFERENCES comments(id)
    ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='评论点赞关系表';

CREATE TABLE IF NOT EXISTS post_views (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '浏览事件ID',
  post_id BIGINT UNSIGNED NOT NULL COMMENT '帖子ID',
  user_id BIGINT UNSIGNED DEFAULT NULL COMMENT '用户ID（游客为空）',
  session_id VARCHAR(100) DEFAULT NULL COMMENT '会话标识（游客去重辅助）',
  ip_hash CHAR(64) DEFAULT NULL COMMENT 'IP哈希（隐私合规，不存明文）',
  viewed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
  PRIMARY KEY (id),
  KEY idx_post_views_post_time (post_id, viewed_at),
  KEY idx_post_views_user_time (user_id, viewed_at),
  KEY idx_post_views_session_time (session_id, viewed_at),
  CONSTRAINT fk_post_views_post
    FOREIGN KEY (post_id) REFERENCES posts(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_post_views_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='帖子浏览日志（用于热度计算与分析）';


/* ============================================================
   4) 内容审核与举报
   ============================================================ */

CREATE TABLE IF NOT EXISTS moderation_logs (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '审核日志ID',
  target_type ENUM('POST','COMMENT','USER') NOT NULL COMMENT '审核对象类型',
  target_id BIGINT UNSIGNED NOT NULL COMMENT '审核对象ID',
  moderator_id BIGINT UNSIGNED NOT NULL COMMENT '审核员ID',
  action ENUM('APPROVE','REJECT','HIDE','UNHIDE','DELETE','BAN_USER','UNBAN_USER') NOT NULL COMMENT '审核动作',
  reason VARCHAR(255) DEFAULT NULL COMMENT '审核原因',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
  PRIMARY KEY (id),
  KEY idx_moderation_target (target_type, target_id, created_at),
  KEY idx_moderation_moderator_time (moderator_id, created_at),
  CONSTRAINT fk_moderation_moderator
    FOREIGN KEY (moderator_id) REFERENCES users(id)
    ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='审核行为日志';

CREATE TABLE IF NOT EXISTS reports (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '举报ID',
  reporter_id BIGINT UNSIGNED NOT NULL COMMENT '举报人ID',
  target_type ENUM('POST','COMMENT','USER') NOT NULL COMMENT '举报对象类型',
  target_id BIGINT UNSIGNED NOT NULL COMMENT '举报对象ID',
  reason_type ENUM('SPAM','ILLEGAL','ABUSE','PORN','MISLEADING','OTHER') NOT NULL COMMENT '举报原因类型',
  reason_detail VARCHAR(500) DEFAULT NULL COMMENT '补充说明',
  status ENUM('PENDING','PROCESSING','RESOLVED','REJECTED') NOT NULL DEFAULT 'PENDING' COMMENT '处理状态',
  handled_by BIGINT UNSIGNED DEFAULT NULL COMMENT '处理人ID',
  handled_at DATETIME DEFAULT NULL COMMENT '处理时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
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
) ENGINE=InnoDB COMMENT='用户举报表';

CREATE TABLE IF NOT EXISTS notifications (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  recipient_user_id BIGINT UNSIGNED NOT NULL COMMENT '接收用户ID',
  actor_user_id BIGINT UNSIGNED DEFAULT NULL COMMENT '操作者用户ID',
  event_type VARCHAR(40) NOT NULL COMMENT '通知事件类型',
  target_type ENUM('POST','COMMENT') NOT NULL COMMENT '目标类型',
  target_id BIGINT UNSIGNED NOT NULL COMMENT '目标ID',
  post_id BIGINT UNSIGNED DEFAULT NULL COMMENT '关联帖子ID',
  metadata JSON DEFAULT NULL COMMENT 'optional structured notification metadata',
  is_read TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_notifications_recipient_time (recipient_user_id, created_at),
  KEY idx_notifications_recipient_read (recipient_user_id, is_read),
  KEY idx_notifications_recipient_event_time (recipient_user_id, event_type, created_at),
  CONSTRAINT fk_notifications_recipient
    FOREIGN KEY (recipient_user_id) REFERENCES users(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_notifications_actor
    FOREIGN KEY (actor_user_id) REFERENCES users(id)
    ON DELETE SET NULL,
  CONSTRAINT fk_notifications_post
    FOREIGN KEY (post_id) REFERENCES posts(id)
    ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='站内通知表';


/* ============================================================
   5) 热度与排行榜
   ============================================================ */

CREATE TABLE IF NOT EXISTS interaction_events (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '互动事件ID',
  event_type ENUM(
    'POST_CREATE',
    'POST_VIEW',
    'POST_LIKE',
    'POST_UNLIKE',
    'POST_FAVORITE',
    'POST_UNFAVORITE',
    'COMMENT_CREATE',
    'COMMENT_DELETE'
  ) NOT NULL COMMENT '事件类型',
  user_id BIGINT UNSIGNED DEFAULT NULL COMMENT '触发用户ID',
  post_id BIGINT UNSIGNED DEFAULT NULL COMMENT '帖子ID',
  store_id BIGINT UNSIGNED DEFAULT NULL COMMENT '店铺ID（冗余）',
  category_id BIGINT UNSIGNED DEFAULT NULL COMMENT '分类ID（冗余）',
  event_value DECIMAL(12,4) NOT NULL DEFAULT 1 COMMENT '事件权重值',
  event_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '事件时间',
  PRIMARY KEY (id),
  KEY idx_events_type_time (event_type, event_time),
  KEY idx_events_post_time (post_id, event_time),
  KEY idx_events_store_time (store_id, event_time),
  KEY idx_events_category_time (category_id, event_time),
  CONSTRAINT fk_events_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE SET NULL,
  CONSTRAINT fk_events_post
    FOREIGN KEY (post_id) REFERENCES posts(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_events_store
    FOREIGN KEY (store_id) REFERENCES stores(id)
    ON DELETE SET NULL,
  CONSTRAINT fk_events_category
    FOREIGN KEY (category_id) REFERENCES categories(id)
    ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='互动事件流水（热度计算基础）';

CREATE TABLE IF NOT EXISTS trending_snapshots (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '快照ID',
  window_type ENUM('DAY','WEEK','MONTH') NOT NULL COMMENT '时间窗口',
  window_start DATE NOT NULL COMMENT '窗口起始日期',
  window_end DATE NOT NULL COMMENT '窗口结束日期',
  dimension_type ENUM('POST','STORE','CATEGORY','TAG') NOT NULL COMMENT '榜单维度',
  dimension_id BIGINT UNSIGNED NOT NULL COMMENT '维度对象ID',
  score DECIMAL(18,6) NOT NULL COMMENT '热度分',
  rank_no INT NOT NULL COMMENT '排名（1开始）',
  metrics_json JSON DEFAULT NULL COMMENT '指标明细JSON（views/likes/comments等）',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '快照生成时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_trending_unique (
    window_type, window_start, window_end, dimension_type, dimension_id
  ),
  KEY idx_trending_query (
    window_type, dimension_type, window_start, rank_no
  )
) ENGINE=InnoDB COMMENT='排行榜快照（前台查询直接读）';


/* ============================================================
   6) 搜索与AI模块预留
   ============================================================ */

CREATE TABLE IF NOT EXISTS search_queries (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '搜索记录ID',
  user_id BIGINT UNSIGNED DEFAULT NULL COMMENT '用户ID（游客为空）',
  query_text VARCHAR(300) NOT NULL COMMENT '搜索词',
  query_type ENUM('KEYWORD','SEMANTIC','IMAGE','MULTIMODAL') NOT NULL DEFAULT 'KEYWORD' COMMENT '搜索类型',
  result_count INT NOT NULL DEFAULT 0 COMMENT '结果数量',
  latency_ms INT DEFAULT NULL COMMENT '耗时毫秒',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '搜索时间',
  PRIMARY KEY (id),
  KEY idx_search_queries_user_time (user_id, created_at),
  KEY idx_search_queries_type_time (query_type, created_at),
  CONSTRAINT fk_search_queries_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='搜索日志（优化召回、分析用）';

CREATE TABLE IF NOT EXISTS ai_embeddings (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '向量记录ID',
  entity_type ENUM('POST_TEXT','POST_IMAGE','QUERY_TEXT','QUERY_IMAGE') NOT NULL COMMENT '向量对象类型',
  entity_id BIGINT UNSIGNED NOT NULL COMMENT '对象ID（如post_id / post_image_id）',
  model_name VARCHAR(100) NOT NULL COMMENT '模型名称',
  vector_dim INT NOT NULL COMMENT '向量维度',
  vector_json LONGTEXT NOT NULL COMMENT '向量JSON（V1方案；后续可迁向量库）',
  version_tag VARCHAR(40) DEFAULT 'v1' COMMENT '版本标签',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_ai_embedding_unique (entity_type, entity_id, model_name, version_tag),
  KEY idx_ai_embedding_entity (entity_type, entity_id),
  KEY idx_ai_embedding_model (model_name)
) ENGINE=InnoDB COMMENT='AI向量存储（过渡方案）';

CREATE TABLE IF NOT EXISTS image_search_tasks (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '图搜图任务ID',
  user_id BIGINT UNSIGNED DEFAULT NULL COMMENT '发起用户ID',
  image_url VARCHAR(500) NOT NULL COMMENT '查询图片URL',
  text_hint VARCHAR(300) DEFAULT NULL COMMENT '文字提示（多模态）',
  status ENUM('PENDING','RUNNING','SUCCESS','FAILED') NOT NULL DEFAULT 'PENDING' COMMENT '任务状态',
  result_json JSON DEFAULT NULL COMMENT '返回结果JSON',
  error_message VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  finished_at DATETIME DEFAULT NULL COMMENT '完成时间',
  PRIMARY KEY (id),
  KEY idx_img_task_user_time (user_id, created_at),
  KEY idx_img_task_status_time (status, created_at),
  CONSTRAINT fk_img_task_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='图像检索异步任务表';


/* ============================================================
   7) 触发器（自动维护计数，减少业务漏更新）
   注意：上线前请在测试环境验证触发器性能与并发行为
   ============================================================ */

DELIMITER $$

-- 为了支持重复执行脚本，先删除已有触发器
DROP TRIGGER IF EXISTS trg_comments_after_insert$$
DROP TRIGGER IF EXISTS trg_comments_after_update$$
DROP TRIGGER IF EXISTS trg_comments_after_delete$$
DROP TRIGGER IF EXISTS trg_post_likes_after_insert$$
DROP TRIGGER IF EXISTS trg_post_likes_after_delete$$
DROP TRIGGER IF EXISTS trg_post_favorites_after_insert$$
DROP TRIGGER IF EXISTS trg_post_favorites_after_delete$$
DROP TRIGGER IF EXISTS trg_comment_likes_after_insert$$
DROP TRIGGER IF EXISTS trg_comment_likes_after_delete$$

-- 评论新增 -> posts.comment_count +1
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

-- 评论从可见变为删除/隐藏 -> comment_count -1
CREATE TRIGGER trg_comments_after_update
AFTER UPDATE ON comments
FOR EACH ROW
BEGIN
  IF OLD.status = 'VISIBLE' AND NEW.status IN ('HIDDEN','DELETED') THEN
    UPDATE posts
       SET comment_count = IF(comment_count > 0, comment_count - 1, 0)
     WHERE id = NEW.post_id;
  ELSEIF OLD.status IN ('HIDDEN','DELETED') AND NEW.status = 'VISIBLE' THEN
    UPDATE posts
       SET comment_count = comment_count + 1
     WHERE id = NEW.post_id;
  END IF;
END$$

-- 评论被物理删除且原本可见 -> comment_count -1
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

-- 点赞新增 -> like_count +1
CREATE TRIGGER trg_post_likes_after_insert
AFTER INSERT ON post_likes
FOR EACH ROW
BEGIN
  UPDATE posts
     SET like_count = like_count + 1
   WHERE id = NEW.post_id;
END$$

-- 点赞取消 -> like_count -1
CREATE TRIGGER trg_post_likes_after_delete
AFTER DELETE ON post_likes
FOR EACH ROW
BEGIN
  UPDATE posts
     SET like_count = IF(like_count > 0, like_count - 1, 0)
   WHERE id = OLD.post_id;
END$$

-- 收藏新增 -> favorite_count +1
CREATE TRIGGER trg_post_favorites_after_insert
AFTER INSERT ON post_favorites
FOR EACH ROW
BEGIN
  UPDATE posts
     SET favorite_count = favorite_count + 1
   WHERE id = NEW.post_id;
END$$

-- 收藏取消 -> favorite_count -1
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


/* ============================================================
   8) 初始化基础数据（可选）
   ============================================================ */

INSERT INTO categories (parent_id, name, level, sort_order, is_active)
VALUES
  (NULL, '家居好物', 1, 10, 1),
  (NULL, '美妆个护', 1, 20, 1),
  (NULL, '食品饮料', 1, 30, 1),
  (NULL, '数码配件', 1, 40, 1),
  (NULL, '礼物精选', 1, 50, 1)
ON DUPLICATE KEY UPDATE
  is_active = VALUES(is_active);

INSERT INTO tags (name)
VALUES
  ('复古风'), ('高颜值'), ('平价'), ('送礼'), ('健康'), ('实用'), ('探店推荐')
ON DUPLICATE KEY UPDATE
  name = VALUES(name);

/* ============================================================
   End
   ============================================================ */
