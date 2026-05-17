SET NAMES utf8mb4;
USE unique_finds;

INSERT INTO users (
  id, username, email, password_hash, nickname, avatar_url, bio,
  role, status, email_verified, last_login_at
) VALUES
  (1001, 'demo_alice', 'alice@uniquefinds.demo', '$2a$10$Kyi0VpSyAqAiyiT5o.b4WuqS6.R1ekZpQ1GT4YiK2X9jJwo9M0Sr.', 'Alice Chen', 'https://picsum.photos/seed/demo-alice/200/200', 'Loves gift hunting and cozy home finds.', 'USER', 'ACTIVE', 1, '2026-05-10 20:00:00'),
  (1002, 'demo_brian', 'brian@uniquefinds.demo', '$2a$10$Kyi0VpSyAqAiyiT5o.b4WuqS6.R1ekZpQ1GT4YiK2X9jJwo9M0Sr.', 'Brian Wu', 'https://picsum.photos/seed/demo-brian/200/200', 'Always looking for practical gadgets in local stores.', 'USER', 'ACTIVE', 1, '2026-05-10 19:00:00'),
  (1003, 'demo_cathy', 'cathy@uniquefinds.demo', '$2a$10$Kyi0VpSyAqAiyiT5o.b4WuqS6.R1ekZpQ1GT4YiK2X9jJwo9M0Sr.', 'Cathy Lin', 'https://picsum.photos/seed/demo-cathy/200/200', 'Focuses on aesthetic lifestyle products and stationery.', 'USER', 'ACTIVE', 1, '2026-05-09 21:30:00'),
  (1004, 'demo_derek', 'derek@uniquefinds.demo', '$2a$10$Kyi0VpSyAqAiyiT5o.b4WuqS6.R1ekZpQ1GT4YiK2X9jJwo9M0Sr.', 'Derek Sun', 'https://picsum.photos/seed/demo-derek/200/200', 'Enjoys digital accessories and useful travel finds.', 'USER', 'ACTIVE', 1, '2026-05-08 18:20:00'),
  (1005, 'demo_admin', 'admin@uniquefinds.demo', '$2a$10$Kyi0VpSyAqAiyiT5o.b4WuqS6.R1ekZpQ1GT4YiK2X9jJwo9M0Sr.', 'Admin Demo', 'https://picsum.photos/seed/demo-admin/200/200', 'Admin account for moderation demo.', 'ADMIN', 'ACTIVE', 1, '2026-05-10 22:00:00')
ON DUPLICATE KEY UPDATE
  email = VALUES(email),
  password_hash = VALUES(password_hash),
  nickname = VALUES(nickname),
  avatar_url = VALUES(avatar_url),
  bio = VALUES(bio),
  role = VALUES(role),
  status = VALUES(status),
  email_verified = VALUES(email_verified),
  last_login_at = VALUES(last_login_at);

INSERT INTO stores (
  id, name, branch_name, city, district, address, latitude, longitude, phone, business_hours, status, created_by
) VALUES
  (201, 'North Lane Gift House', NULL, 'San Francisco', 'Mission', '220 Valencia St', 37.7694000, -122.4217000, '415-555-0101', '10:00-20:00', 'ACTIVE', 1001),
  (202, 'Paper & Bloom', NULL, 'San Francisco', 'Hayes Valley', '521 Hayes St', 37.7765000, -122.4242000, '415-555-0102', '11:00-19:00', 'ACTIVE', 1003),
  (203, 'Tiny Tech Corner', NULL, 'San Francisco', 'SoMa', '88 4th St', 37.7857000, -122.4038000, '415-555-0103', '10:30-21:00', 'ACTIVE', 1002),
  (204, 'Weekend Market Collective', NULL, 'San Francisco', 'Sunset', '1290 Irving St', 37.7633000, -122.4723000, '415-555-0104', '09:00-18:00', 'ACTIVE', 1004),
  (205, 'Oak & Clay Home Shop', NULL, 'San Francisco', 'Noe Valley', '4024 24th St', 37.7514000, -122.4318000, '415-555-0105', '10:00-19:30', 'ACTIVE', 1001)
ON DUPLICATE KEY UPDATE
  city = VALUES(city),
  district = VALUES(district),
  address = VALUES(address),
  status = VALUES(status);

INSERT INTO categories (id, parent_id, name, level, sort_order, is_active)
VALUES
  (101, NULL, 'Home Decor', 1, 10, 1),
  (102, NULL, 'Stationery', 1, 20, 1),
  (103, NULL, 'Digital Accessories', 1, 30, 1),
  (104, NULL, 'Kitchen & Dining', 1, 40, 1),
  (105, NULL, 'Beauty & Wellness', 1, 50, 1),
  (106, NULL, 'Gift Ideas', 1, 60, 1)
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  sort_order = VALUES(sort_order),
  is_active = VALUES(is_active);

INSERT INTO tags (id, name, heat_score)
VALUES
  (301, 'retro', 0),
  (302, 'gift-friendly', 0),
  (303, 'practical', 0),
  (304, 'cozy', 0),
  (305, 'travel', 0),
  (306, 'minimal', 0)
ON DUPLICATE KEY UPDATE
  name = VALUES(name);

INSERT INTO posts (
  id, user_id, store_id, category_id, title, description,
  price_min, price_max, currency, location_text, status, moderation_reason,
  view_count, like_count, favorite_count, comment_count, published_at, deleted_at, created_at, updated_at
) VALUES
  (301, 1001, 205, 104, 'Hand-painted retro ceramic mug', 'Found a hand-painted ceramic mug with a warm retro palette. Great for a thoughtful housewarming gift and feels much more premium than its price.', 18.00, 24.00, 'USD', 'Noe Valley', 'PUBLISHED', NULL, 42, 0, 0, 0, '2026-05-10 10:00:00', NULL, '2026-05-10 09:30:00', '2026-05-10 10:00:00'),
  (302, 1002, 203, 103, 'Foldable magnetic phone stand', 'Small metal stand that folds flat and sticks to the back of a phone case. Practical for travel and video calls.', 12.00, 16.00, 'USD', 'SoMa', 'PUBLISHED', NULL, 67, 0, 0, 0, '2026-05-10 11:00:00', NULL, '2026-05-10 10:40:00', '2026-05-10 11:00:00'),
  (303, 1003, 202, 102, 'Floral weekly planner with thick paper', 'This planner uses thick paper that does not bleed through and has a gentle floral cover that feels gift-friendly.', 22.00, 28.00, 'USD', 'Hayes Valley', 'PUBLISHED', NULL, 38, 0, 0, 0, '2026-05-09 14:00:00', NULL, '2026-05-09 13:20:00', '2026-05-09 14:00:00'),
  (304, 1004, 204, 106, 'Compact lavender sleep spray gift set', 'A small wellness gift set with lavender spray and eye mask. Suitable for older family members or coworkers.', 26.00, 32.00, 'USD', 'Sunset', 'PUBLISHED', NULL, 49, 0, 0, 0, '2026-05-09 16:30:00', NULL, '2026-05-09 16:00:00', '2026-05-09 16:30:00'),
  (305, 1001, 201, 101, 'Mini mushroom table lamp', 'A tiny mushroom-shaped lamp with a soft glow. Works well for dorm desks and cozy bedside corners.', 29.00, 35.00, 'USD', 'Mission', 'PUBLISHED', NULL, 54, 0, 0, 0, '2026-05-08 19:20:00', NULL, '2026-05-08 18:50:00', '2026-05-08 19:20:00'),
  (306, 1002, 203, 103, 'Cable organizer pouch for travel', 'Slim pouch with elastic loops for chargers, earphones, and adapters. Great for practical travelers.', 15.00, 19.00, 'USD', 'SoMa', 'PUBLISHED', NULL, 24, 0, 0, 0, '2026-05-08 12:00:00', NULL, '2026-05-08 11:30:00', '2026-05-08 12:00:00'),
  (307, 1003, 202, 102, 'Soft-tone gel pen set', 'Muted color gel pens that write surprisingly smoothly. Nice for journaling and casual note-taking.', 9.00, 12.00, 'USD', 'Hayes Valley', 'PUBLISHED', NULL, 33, 0, 0, 0, '2026-05-07 15:10:00', NULL, '2026-05-07 14:30:00', '2026-05-07 15:10:00'),
  (308, 1004, 204, 105, 'Portable neck massager', 'Compact heated neck massager spotted at a weekend market booth. Feels like a useful wellness gift for parents.', 48.00, 58.00, 'USD', 'Sunset', 'PUBLISHED', NULL, 71, 0, 0, 0, '2026-05-07 17:40:00', NULL, '2026-05-07 17:00:00', '2026-05-07 17:40:00'),
  (309, 1001, 205, 104, 'Matte stoneware ramen bowl', 'Wide matte bowl with a speckled finish. Looks minimal but still warm and handmade.', 21.00, 27.00, 'USD', 'Noe Valley', 'PUBLISHED', NULL, 29, 0, 0, 0, '2026-05-06 13:00:00', NULL, '2026-05-06 12:25:00', '2026-05-06 13:00:00'),
  (310, 1002, 203, 103, 'Pocket Bluetooth tracker', 'A tiny tracker for keys and bags. Helpful for forgetful people and good as a practical small gift.', 19.00, 25.00, 'USD', 'SoMa', 'PUBLISHED', NULL, 58, 0, 0, 0, '2026-05-05 18:00:00', NULL, '2026-05-05 17:20:00', '2026-05-05 18:00:00'),
  (311, 1003, 202, 106, 'Letterpress thank-you card set', 'Premium-feeling thank-you cards with a subtle texture and clean typography.', 14.00, 18.00, 'USD', 'Hayes Valley', 'PUBLISHED', NULL, 17, 0, 0, 0, '2026-05-04 11:40:00', NULL, '2026-05-04 11:10:00', '2026-05-04 11:40:00'),
  (312, 1004, 204, 101, 'Woven picnic basket with leather strap', 'A woven basket that feels decorative enough to display at home but still practical for day trips.', 52.00, 65.00, 'USD', 'Sunset', 'PUBLISHED', NULL, 44, 0, 0, 0, '2026-05-03 15:20:00', NULL, '2026-05-03 14:40:00', '2026-05-03 15:20:00'),
  (313, 1001, 201, 104, 'Glass honey pot with wooden dipper', 'Cute but still functional. A nice tiny kitchen gift for someone who likes hosting brunch.', 16.00, 20.00, 'USD', 'Mission', 'PUBLISHED', NULL, 26, 0, 0, 0, '2026-05-02 10:30:00', NULL, '2026-05-02 10:05:00', '2026-05-02 10:30:00'),
  (314, 1002, 203, 103, 'Aluminum laptop riser', 'Minimal laptop riser with good airflow and surprisingly sturdy build for the price.', 34.00, 39.00, 'USD', 'SoMa', 'PUBLISHED', NULL, 61, 0, 0, 0, '2026-05-01 19:00:00', NULL, '2026-05-01 18:20:00', '2026-05-01 19:00:00'),
  (315, 1003, 202, 102, 'Transparent stamp storage case', 'A clear storage case for stamps and stickers that keeps small stationery items organized.', 11.00, 14.00, 'USD', 'Hayes Valley', 'PUBLISHED', NULL, 19, 0, 0, 0, '2026-04-29 14:00:00', NULL, '2026-04-29 13:25:00', '2026-04-29 14:00:00'),
  (316, 1004, 204, 105, 'Herbal hand cream trio', 'Three small hand creams with clean scents and travel-friendly packaging.', 24.00, 30.00, 'USD', 'Sunset', 'PUBLISHED', NULL, 36, 0, 0, 0, '2026-04-28 12:30:00', NULL, '2026-04-28 12:00:00', '2026-04-28 12:30:00'),
  (317, 1001, 205, 106, 'Mini tea sampler for gifting', 'Six mini tea tins with labels that feel polished enough to give as a quick host gift.', 20.00, 26.00, 'USD', 'Noe Valley', 'PUBLISHED', NULL, 22, 0, 0, 0, '2026-04-25 16:00:00', NULL, '2026-04-25 15:20:00', '2026-04-25 16:00:00'),
  (318, 1002, 203, 103, 'Clip-on book light', 'Simple rechargeable reading light with warm and cool modes. Good for bedside reading or flights.', 13.00, 17.00, 'USD', 'SoMa', 'PUBLISHED', NULL, 31, 0, 0, 0, '2026-04-22 18:40:00', NULL, '2026-04-22 18:00:00', '2026-04-22 18:40:00'),
  (319, 1003, 202, 102, 'Pressed flower bookmark set', 'Lightweight bookmarks that look handmade and pair nicely with notebooks or gift books.', 8.00, 10.00, 'USD', 'Hayes Valley', 'PUBLISHED', NULL, 14, 0, 0, 0, '2026-04-20 11:00:00', NULL, '2026-04-20 10:35:00', '2026-04-20 11:00:00'),
  (320, 1004, 204, 101, 'Ceramic incense holder in pebble shape', 'A smooth pebble-shaped incense holder with a calm, minimal look.', 18.00, 22.00, 'USD', 'Sunset', 'PUBLISHED', NULL, 27, 0, 0, 0, '2026-04-18 13:20:00', NULL, '2026-04-18 12:50:00', '2026-04-18 13:20:00'),
  (321, 1001, 201, 106, 'Embroidered canvas tote for teachers', 'Thoughtful tote with embroidered message. Draft post kept pending for review demo.', 24.00, 29.00, 'USD', 'Mission', 'PENDING_REVIEW', NULL, 0, 0, 0, 0, NULL, NULL, '2026-05-10 20:00:00', '2026-05-10 20:00:00'),
  (322, 1002, 203, 103, 'LED backpack clip light', 'Rejected example for moderation flow demo.', 7.00, 10.00, 'USD', 'SoMa', 'REJECTED', 'Low-quality duplicate post for moderation demo.', 0, 0, 0, 0, NULL, NULL, '2026-05-09 19:15:00', '2026-05-09 19:15:00'),
  (323, 1003, 202, 102, 'Vintage paper sticker box', 'Hidden example for moderation flow demo.', 12.00, 16.00, 'USD', 'Hayes Valley', 'HIDDEN', 'Temporarily hidden during moderation walkthrough.', 0, 0, 0, 0, '2026-05-06 10:00:00', NULL, '2026-05-06 09:30:00', '2026-05-06 10:00:00')
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  description = VALUES(description),
  status = VALUES(status),
  moderation_reason = VALUES(moderation_reason),
  view_count = VALUES(view_count),
  published_at = VALUES(published_at),
  updated_at = VALUES(updated_at);

INSERT INTO post_images (
  id, post_id, image_url, image_key, thumbnail_url, width, height, file_size, mime_type, sort_order, is_cover
) VALUES
  (401, 301, 'https://picsum.photos/seed/uf-mug/960/720', NULL, NULL, 960, 720, 0, 'image/jpeg', 0, 1),
  (402, 302, 'https://picsum.photos/seed/uf-stand/960/720', NULL, NULL, 960, 720, 0, 'image/jpeg', 0, 1),
  (403, 303, 'https://picsum.photos/seed/uf-planner/960/720', NULL, NULL, 960, 720, 0, 'image/jpeg', 0, 1),
  (404, 304, 'https://picsum.photos/seed/uf-sleep-spray/960/720', NULL, NULL, 960, 720, 0, 'image/jpeg', 0, 1),
  (405, 305, 'https://picsum.photos/seed/uf-lamp/960/720', NULL, NULL, 960, 720, 0, 'image/jpeg', 0, 1),
  (406, 308, 'https://picsum.photos/seed/uf-massager/960/720', NULL, NULL, 960, 720, 0, 'image/jpeg', 0, 1),
  (407, 314, 'https://picsum.photos/seed/uf-riser/960/720', NULL, NULL, 960, 720, 0, 'image/jpeg', 0, 1),
  (408, 320, 'https://picsum.photos/seed/uf-incense/960/720', NULL, NULL, 960, 720, 0, 'image/jpeg', 0, 1),
  (409, 301, 'https://picsum.photos/seed/uf-mug-side/960/720', NULL, NULL, 960, 720, 0, 'image/jpeg', 1, 0),
  (410, 305, 'https://picsum.photos/seed/uf-lamp-glow/960/720', NULL, NULL, 960, 720, 0, 'image/jpeg', 1, 0)
ON DUPLICATE KEY UPDATE
  image_url = VALUES(image_url),
  sort_order = VALUES(sort_order),
  is_cover = VALUES(is_cover);

INSERT IGNORE INTO post_tags (post_id, tag_id) VALUES
  (301, 301), (301, 302), (302, 303), (303, 302), (304, 302), (304, 303),
  (305, 304), (306, 305), (308, 303), (309, 306), (314, 306), (317, 302);

INSERT IGNORE INTO post_likes (user_id, post_id) VALUES
  (1002, 301), (1003, 301), (1004, 301),
  (1001, 302), (1003, 302), (1004, 302),
  (1001, 304), (1002, 304),
  (1001, 305), (1002, 305), (1003, 305),
  (1001, 308), (1002, 308), (1003, 308), (1005, 308),
  (1001, 314), (1003, 314), (1004, 314),
  (1002, 316), (1003, 316),
  (1004, 317),
  (1001, 320), (1005, 320);

INSERT IGNORE INTO post_favorites (user_id, post_id) VALUES
  (1002, 301), (1003, 301),
  (1001, 302), (1004, 302),
  (1001, 304), (1003, 304),
  (1001, 305), (1004, 305),
  (1002, 308), (1003, 308), (1005, 308),
  (1001, 314), (1004, 314),
  (1002, 317),
  (1003, 320);

INSERT INTO comments (
  id, post_id, user_id, parent_id, root_id, reply_to_user_id, content, status, like_count, created_at, updated_at
) VALUES
  (501, 301, 1002, NULL, NULL, NULL, 'This mug looks much better than the mass-market ones I usually see.', 'VISIBLE', 0, '2026-05-10 12:00:00', '2026-05-10 12:00:00'),
  (502, 301, 1001, 501, 501, 1002, 'Yes, the glaze felt much more handmade in person.', 'VISIBLE', 0, '2026-05-10 12:10:00', '2026-05-10 12:10:00'),
  (503, 302, 1003, NULL, NULL, NULL, 'This feels like a solid practical gift for students.', 'VISIBLE', 0, '2026-05-10 12:30:00', '2026-05-10 12:30:00'),
  (504, 304, 1001, NULL, NULL, NULL, 'Exactly the kind of healthy gift idea I was looking for.', 'VISIBLE', 0, '2026-05-09 18:30:00', '2026-05-09 18:30:00'),
  (505, 305, 1004, NULL, NULL, NULL, 'The lamp shape is adorable. Was the light warm or neutral?', 'VISIBLE', 0, '2026-05-08 20:00:00', '2026-05-08 20:00:00'),
  (506, 305, 1001, 505, 505, 1004, 'Warm, and surprisingly soft for a small lamp.', 'VISIBLE', 0, '2026-05-08 20:08:00', '2026-05-08 20:08:00'),
  (507, 308, 1005, NULL, NULL, NULL, 'Good wellness post for the gift-use-case demo.', 'VISIBLE', 0, '2026-05-07 18:10:00', '2026-05-07 18:10:00'),
  (508, 314, 1003, NULL, NULL, NULL, 'This one is genuinely useful for work-from-home setups.', 'VISIBLE', 0, '2026-05-01 20:00:00', '2026-05-01 20:00:00'),
  (509, 320, 1002, NULL, NULL, NULL, 'The shape reads very calm and minimal.', 'VISIBLE', 0, '2026-04-18 15:00:00', '2026-04-18 15:00:00'),
  (510, 320, 1001, 509, 509, 1002, 'Agreed. It also felt heavier and more stable than expected.', 'VISIBLE', 0, '2026-04-18 15:12:00', '2026-04-18 15:12:00')
ON DUPLICATE KEY UPDATE
  content = VALUES(content),
  status = VALUES(status),
  updated_at = VALUES(updated_at);

INSERT INTO reports (
  id, reporter_id, target_type, target_id, reason_type, reason_detail, status, handled_by, handled_at, created_at
) VALUES
  (601, 1001, 'POST', 322, 'MISLEADING', 'Seed report for the rejected-post moderation walkthrough.', 'RESOLVED', 1005, '2026-05-10 21:00:00', '2026-05-10 20:40:00'),
  (602, 1002, 'COMMENT', 507, 'OTHER', 'Seed report for comment moderation list demo.', 'PENDING', NULL, NULL, '2026-05-10 21:20:00')
ON DUPLICATE KEY UPDATE
  reason_detail = VALUES(reason_detail),
  status = VALUES(status),
  handled_by = VALUES(handled_by),
  handled_at = VALUES(handled_at);

INSERT INTO moderation_logs (
  id, target_type, target_id, moderator_id, action, reason, created_at
) VALUES
  (701, 'POST', 322, 1005, 'REJECT', 'Seed moderation log for rejected demo post.', '2026-05-10 21:00:00'),
  (702, 'POST', 323, 1005, 'HIDE', 'Seed moderation log for hidden demo post.', '2026-05-10 21:05:00')
ON DUPLICATE KEY UPDATE
  reason = VALUES(reason),
  created_at = VALUES(created_at);
