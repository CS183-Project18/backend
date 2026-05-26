USE unique_finds;

SELECT 'active category tree roots' AS check_name, id, parent_id, name, level, is_active
FROM categories
WHERE parent_id IS NULL
ORDER BY sort_order, id;

SELECT 'active stores' AS check_name, id, name, branch_name, status
FROM stores
WHERE status = 'ACTIVE'
ORDER BY id;

SELECT 'hidden stores excluded from public store list' AS check_name, COUNT(1) AS hidden_store_count
FROM stores
WHERE status = 'HIDDEN';

SELECT 'search by structured category' AS check_name, id, title, category_id, store_id, price_min, price_max
FROM posts
WHERE status = 'PUBLISHED'
  AND category_id = 112
ORDER BY published_at DESC, id DESC;

SELECT 'search by active store' AS check_name, id, title, category_id, store_id
FROM posts
WHERE status = 'PUBLISHED'
  AND store_id = 203
ORDER BY published_at DESC, id DESC;

SELECT 'search by tag gift-friendly' AS check_name, p.id, p.title
FROM posts p
INNER JOIN post_tags pt ON pt.post_id = p.id
WHERE p.status = 'PUBLISHED'
  AND pt.tag_id = 302
ORDER BY p.published_at DESC, p.id DESC;

SELECT 'report target summary post' AS check_name, r.id, r.target_type, p.title AS target_summary
FROM reports r
LEFT JOIN posts p ON p.id = r.target_id AND r.target_type = 'POST'
WHERE r.target_type = 'POST';

SELECT 'report target summary comment' AS check_name, r.id, r.target_type, c.content AS target_summary
FROM reports r
LEFT JOIN comments c ON c.id = r.target_id AND r.target_type = 'COMMENT'
WHERE r.target_type = 'COMMENT';

SELECT 'top active categories only' AS check_name, c.id, c.name, COUNT(1) AS post_count
FROM posts p
INNER JOIN categories c ON c.id = p.category_id
WHERE p.status = 'PUBLISHED'
  AND c.is_active = 1
GROUP BY c.id, c.name
ORDER BY post_count DESC, c.name ASC
LIMIT 10;

SELECT 'top active stores only' AS check_name, s.id, s.name, s.status, COUNT(1) AS post_count
FROM posts p
INNER JOIN stores s ON s.id = p.store_id
WHERE p.status = 'PUBLISHED'
  AND s.status = 'ACTIVE'
GROUP BY s.id, s.name, s.status
ORDER BY post_count DESC, s.name ASC
LIMIT 10;
