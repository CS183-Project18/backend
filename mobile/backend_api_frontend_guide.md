# Unique Finds 后端接口接入说明

这份文档给前端接入使用，默认后端地址为：

```text
http://localhost:8080
```

## 1. 通用规则

### 1.1 统一响应结构

所有业务接口默认返回 `Result<T>`：

```json
{
  "success": true,
  "code": "OK",
  "message": "ok",
  "data": {}
}
```

前端建议只在 `success === true` 时读取 `data`。如果 `success === false`，优先展示 `message`。

分页接口的 `data` 通常是 `PageResponse<T>`：

```json
{
  "total": 100,
  "page": 1,
  "pageSize": 20,
  "items": [],
  "metadata": {}
}
```

### 1.2 鉴权规则

登录成功后，后端会返回 JWT token。需要登录的接口统一加请求头：

```http
Authorization: Bearer <token>
```

公开接口不需要 token，但如果前端已经有 token，也可以带上。带 token 时，部分接口会返回 `likedByCurrentUser`、`favoritedByCurrentUser` 等当前用户状态。

### 1.3 前端请求封装建议

```js
const API_BASE = window.CURATOR_API_BASE || "http://localhost:8080";

async function apiRequest(path, { method = "GET", auth = false, body, formData } = {}) {
  const headers = {};
  if (auth) {
    const token = localStorage.getItem("curator_token_v1");
    if (token) headers.Authorization = `Bearer ${token}`;
  }
  if (!formData) headers["Content-Type"] = "application/json";

  const response = await fetch(`${API_BASE}${path}`, {
    method,
    headers,
    body: formData || (body ? JSON.stringify(body) : undefined),
  });

  const payload = await response.json().catch(() => null);
  if (!response.ok || payload?.success === false) {
    if (response.status === 401 || response.status === 403) {
      localStorage.removeItem("curator_token_v1");
      localStorage.removeItem("curator_auth_v1");
    }
    throw new Error(payload?.message || `Request failed: ${response.status}`);
  }
  return payload?.data;
}
```

### 1.4 Demo 账号

用于前端快速测试：

```text
普通用户：demo_alice / Password123
管理员：demo_admin / Password123
```

## 2. 认证与当前用户

### 注册

```http
POST /api/auth/register
```

公开接口，不需要 token。

请求体：

```json
{
  "username": "demo_user",
  "email": "demo@example.com",
  "password": "Password123"
}
```

返回 `data`：

```json
{
  "userId": 1,
  "username": "demo_user",
  "email": "demo@example.com",
  "role": "USER",
  "token": "jwt..."
}
```

前端注册成功后应保存 `token`，然后跳转主页面。

### 密码登录

```http
POST /api/auth/login/password
```

公开接口，不需要 token。`account` 可以是用户名或邮箱。

请求体：

```json
{
  "account": "demo_alice",
  "password": "Password123"
}
```

返回 `data` 同注册接口。

### 发送邮箱验证码

```http
POST /api/auth/code/send
```

请求体：

```json
{
  "email": "user@example.com"
}
```

用途：验证码登录前发送邮件。如果本地邮箱配置不可用，前端可以先只接密码登录。

### 验证码登录

```http
POST /api/auth/login/code
```

请求体：

```json
{
  "email": "user@example.com",
  "code": "123456"
}
```

返回 `data` 同注册接口。

### 获取当前登录用户

```http
GET /api/auth/me
```

需要 token。

返回 `data`：

```json
{
  "userId": 1001,
  "username": "demo_alice",
  "role": "USER"
}
```

前端启动 dashboard 时建议先调用这个接口校验 token 是否有效。

## 3. 用户资料与公开主页

### 获取我的资料

```http
GET /api/users/me/profile
```

需要 token。

返回 `data` 常用字段：

```json
{
  "userId": 1001,
  "username": "demo_alice",
  "nickname": "Alice Chen",
  "avatarUrl": "https://...",
  "bio": "Loves gift hunting...",
  "role": "USER",
  "postCount": 10,
  "publishedPostCount": 8,
  "commentCount": 5,
  "favoriteCount": 3
}
```

### 更新我的资料

```http
PUT /api/users/me/profile
```

需要 token。

请求体：

```json
{
  "nickname": "Alice Chen",
  "avatarUrl": "https://example.com/avatar.jpg",
  "bio": "Short profile bio"
}
```

### 获取公开主页资料

```http
GET /api/users/{username}/profile
```

公开接口，不需要 token。

### 获取公开主页帖子

```http
GET /api/users/{username}/posts?page=1&pageSize=20
```

公开接口，只返回该用户已发布帖子。

## 4. 帖子与媒体

### 帖子返回结构

`PostResponse` 常用字段：

```json
{
  "id": 1,
  "userId": 1001,
  "authorUsername": "demo_alice",
  "storeId": 1,
  "categoryId": 1,
  "title": "Hand-painted retro ceramic mug",
  "description": "Found a warm retro mug...",
  "priceMin": 12.99,
  "priceMax": 18.99,
  "currency": "USD",
  "locationText": "Portland, OR",
  "storeSummary": {},
  "categorySummary": {},
  "tags": [],
  "status": "PUBLISHED",
  "moderationReason": null,
  "viewCount": 10,
  "likeCount": 3,
  "favoriteCount": 2,
  "commentCount": 1,
  "likedByCurrentUser": false,
  "favoritedByCurrentUser": false,
  "shareUrl": "http://...",
  "publishedAt": "2026-05-23T10:00:00",
  "createdAt": "2026-05-23T09:00:00",
  "updatedAt": "2026-05-23T09:30:00",
  "images": []
}
```

图片字段 `images` 的每一项：

```json
{
  "id": 1,
  "imageUrl": "http://localhost:8080/uploads/images/xxx.jpg",
  "imageKey": "xxx.jpg",
  "thumbnailUrl": "http://localhost:8080/uploads/images/xxx.jpg",
  "width": 800,
  "height": 600,
  "fileSize": 123456,
  "mimeType": "image/jpeg",
  "sortOrder": 0,
  "isCover": 1
}
```

前端展示图片建议优先用 `thumbnailUrl || imageUrl`。

### 上传图片

```http
POST /api/files/images
```

需要 token。使用 `multipart/form-data`。

FormData：

```text
file: 图片文件
```

返回 `data`：

```json
{
  "url": "http://localhost:8080/uploads/images/xxx.jpg",
  "thumbnailUrl": "http://localhost:8080/uploads/images/xxx.jpg",
  "fileName": "xxx.jpg",
  "contentType": "image/jpeg",
  "size": 123456,
  "width": 800,
  "height": 600
}
```

### 创建帖子

```http
POST /api/posts
```

需要 token。

请求体：

```json
{
  "title": "Hand-painted retro ceramic mug",
  "description": "Found a warm retro ceramic mug...",
  "storeId": 1,
  "categoryId": 1,
  "priceMin": 12.99,
  "priceMax": 18.99,
  "currency": "USD",
  "locationText": "Portland, OR",
  "tagIds": [1, 2],
  "images": [
    {
      "imageUrl": "http://localhost:8080/uploads/images/xxx.jpg",
      "imageKey": "xxx.jpg",
      "thumbnailUrl": "http://localhost:8080/uploads/images/xxx.jpg",
      "width": 800,
      "height": 600,
      "fileSize": 123456,
      "mimeType": "image/jpeg"
    }
  ]
}
```

返回 `PostResponse`。新创建帖子通常进入待审核状态，审核通过后才出现在公开列表。

### 编辑帖子

```http
PUT /api/posts/{postId}
```

需要 token，只有作者可以编辑。请求体同创建帖子。

### 删除帖子

```http
DELETE /api/posts/{postId}
```

需要 token，只有作者可以删除。

### 帖子详情

```http
GET /api/posts/{postId}
```

公开接口。游客可看公开帖子；带 token 时会返回当前用户点赞/收藏状态。

### 公开帖子列表

```http
GET /api/posts/published?page=1&pageSize=20
```

公开接口。返回 `PageResponse<PostResponse>`。

### 我的帖子

```http
GET /api/posts/mine?page=1&pageSize=20
```

需要 token。返回当前用户自己创建的未删除帖子，包括待审核、已发布等状态。

## 5. 搜索与发现

### 文本搜索

```http
GET /api/posts/search
```

公开接口。Query 参数：

```text
keyword: 可选，搜索关键词
categoryId: 可选
storeId: 可选
tagIds: 可选，可传多个 tagIds=1&tagIds=2
priceMin: 可选
priceMax: 可选
sort: 可选，例如 latest / popular 等后端支持值
page: 默认 1
pageSize: 默认 20，最大 100
```

示例：

```http
GET /api/posts/search?keyword=mug&categoryId=1&page=1&pageSize=20
```

注意：`keyword` 非空时，后端会优先走 AI 语义搜索；如果 AI 不可用，会自动回退 SQL 搜索。前端不需要自己判断 AI 状态。

### 图片搜索

```http
POST /api/posts/search/image
```

公开接口。使用 `multipart/form-data`。

FormData：

```text
file: 图片文件，必填
categoryId: 可选
storeId: 可选
tagIds: 可选
priceMin: 可选
priceMax: 可选
page: 默认 1
pageSize: 默认 20
```

返回 `PageResponse<PostResponse>`。如果 AI 图片搜索不可用，后端会尽量返回成功响应和空结果，`message` 会提示图片搜索暂不可用。

### Trending 帖子

```http
GET /api/posts/trending?window=daily&page=1&pageSize=20
```

公开接口。`window` 可用：

```text
daily
weekly
monthly
```

### 标签建议

```http
GET /api/posts/tags/suggest?title=...&description=...&categoryId=1&limit=5
```

公开接口。返回 `List<TagResponse>`。这是规则版标签建议，不是真正 AI 生成标签。

### 分类、标签、店铺

```http
GET /api/categories
GET /api/categories/tree
GET /api/tags
GET /api/stores
GET /api/stores/{storeId}
```

这些都是公开接口，常用于发帖表单、筛选器和详情展示。

### Discovery 排行

```http
GET /api/discovery/trending/categories?limit=10
GET /api/discovery/trending/tags?limit=10
GET /api/discovery/trending/stores?limit=10
```

公开接口。返回排行榜列表，可用于发现页侧栏或热门筛选入口。

## 6. 互动与评论

### 点赞 / 取消点赞

```http
POST /api/posts/{postId}/like
DELETE /api/posts/{postId}/like
```

需要 token。

### 收藏 / 取消收藏

```http
POST /api/posts/{postId}/favorite
DELETE /api/posts/{postId}/favorite
```

需要 token。

### 查询某帖当前用户互动状态

```http
GET /api/posts/{postId}/interaction-status
```

需要 token。

返回：

```json
{
  "postId": 1,
  "liked": true,
  "favorited": false
}
```

### 我的收藏

```http
GET /api/posts/favorites/mine?page=1&pageSize=20
```

需要 token。返回 `PageResponse<PostResponse>`。

### 分享帖子

```http
POST /api/posts/{postId}/share
```

公开接口。

返回：

```json
{
  "postId": 1,
  "shareUrl": "http://localhost:8080/posts/1"
}
```

### 评论列表

```http
GET /api/posts/{postId}/comments?page=1&pageSize=20
```

公开接口。

`CommentResponse` 常用字段：

```json
{
  "id": 1,
  "postId": 1,
  "postTitle": "Post title",
  "userId": 1001,
  "username": "demo_alice",
  "parentId": null,
  "rootId": null,
  "replyToUserId": null,
  "replyToUsername": null,
  "content": "Nice find!",
  "status": "VISIBLE",
  "likeCount": 2,
  "likedByCurrentUser": false,
  "pinned": false,
  "deleted": false,
  "ownedByCurrentUser": true,
  "createdAt": "2026-05-23T10:00:00",
  "updatedAt": "2026-05-23T10:00:00"
}
```

### 发表评论 / 回复

```http
POST /api/posts/{postId}/comments
```

需要 token。

请求体：

```json
{
  "parentId": null,
  "content": "Nice find!"
}
```

回复评论时传 `parentId`。

### 删除自己的评论

```http
DELETE /api/posts/{postId}/comments/{commentId}
```

需要 token，只有评论作者可删除。

### 评论点赞 / 取消点赞

```http
POST /api/comments/{commentId}/like
DELETE /api/comments/{commentId}/like
```

需要 token。

### 评论置顶 / 取消置顶

```http
POST /api/comments/{commentId}/pin
DELETE /api/comments/{commentId}/pin
```

需要 token。通常只有帖子作者或有权限用户能置顶。

### 我的评论

```http
GET /api/comments/mine?page=1&pageSize=20
```

需要 token。

## 7. 举报与通知

### 举报帖子

```http
POST /api/posts/{postId}/report
```

需要 token。

请求体：

```json
{
  "reasonType": "SPAM",
  "reasonDetail": "Repeated advertising content"
}
```

### 举报评论

```http
POST /api/comments/{commentId}/report
```

需要 token。请求体同举报帖子。

常见 `reasonType` 可以使用：

```text
SPAM
PORN
ABUSE
ILLEGAL
OTHER
```

### 通知列表

```http
GET /api/notifications?page=1&pageSize=20&eventType=LIKE
```

需要 token。`eventType` 可选。

`NotificationResponse` 常用字段：

```json
{
  "id": 1,
  "eventType": "POST_LIKED",
  "actorUserId": 1002,
  "actorUsername": "demo_brian",
  "targetType": "POST",
  "targetId": 1,
  "postId": 1,
  "message": "demo_brian liked your post.",
  "targetSummary": "Hand-painted retro ceramic mug",
  "metadata": {},
  "read": false,
  "createdAt": "2026-05-23T10:00:00"
}
```

前端展示优先使用 `message`，辅助展示 `targetSummary`。

### 未读通知数

```http
GET /api/notifications/unread-count
```

需要 token。

### 标记单条通知已读

```http
POST /api/notifications/{notificationId}/read
```

需要 token。

### 全部标记已读

```http
POST /api/notifications/read-all
```

需要 token。

## 8. 管理端接口

所有 `/api/admin/**` 接口都需要管理员 token。普通用户会返回 403。

### 管理员当前用户

```http
GET /api/admin/auth/me
```

需要管理员 token。

### 待审核帖子

```http
GET /api/admin/moderation/posts/pending?page=1&pageSize=20
```

返回 `PageResponse<AdminPostModerationResponse>`。

### 审核帖子

```http
POST /api/admin/moderation/posts/{postId}/approve
POST /api/admin/moderation/posts/{postId}/reject
POST /api/admin/moderation/posts/{postId}/hide
```

`reject` 和 `hide` 请求体：

```json
{
  "reason": "Content does not meet community guidelines"
}
```

`approve` 不需要请求体。

### 管理评论

```http
POST /api/admin/moderation/comments/{commentId}/hide
POST /api/admin/moderation/comments/{commentId}/delete
```

请求体：

```json
{
  "reason": "Inappropriate comment"
}
```

### 举报列表与处理

```http
GET /api/admin/moderation/reports?targetType=POST&status=PENDING&page=1&pageSize=20
POST /api/admin/moderation/reports/{reportId}/resolve
POST /api/admin/moderation/reports/{reportId}/reject
```

处理举报请求体可选：

```json
{
  "reason": "Handled by admin"
}
```

### 审计日志

```http
GET /api/admin/moderation/logs
```

Query 参数：

```text
targetType: 可选，例如 POST / COMMENT / REPORT
targetId: 可选
moderatorId: 可选
action: 可选
startTime: 可选，ISO 时间
endTime: 可选，ISO 时间
page: 默认 1
pageSize: 默认 20
```

### 管理分类

```http
POST /api/admin/categories
PUT /api/admin/categories/{categoryId}
PUT /api/admin/categories/{categoryId}/active?active=true
```

创建/更新请求体：

```json
{
  "parentId": null,
  "name": "Ceramic Mugs",
  "sortOrder": 1,
  "level": 1
}
```

### 管理店铺

```http
POST /api/admin/stores
PUT /api/admin/stores/{storeId}
PUT /api/admin/stores/{storeId}/status?status=ACTIVE
```

创建/更新请求体：

```json
{
  "name": "Woodland Mercantile",
  "branchName": "Main",
  "city": "Portland",
  "district": "Downtown",
  "address": "123 Market Street",
  "latitude": 45.5122,
  "longitude": -122.6587,
  "phone": "123456789",
  "businessHours": "10:00-20:00"
}
```

### 管理标签

```http
POST /api/admin/tags
PUT /api/admin/tags/{tagId}
```

创建请求体：

```json
{
  "name": "handmade"
}
```

更新请求体：

```json
{
  "name": "handmade",
  "heatScore": 10.5
}
```

### Analytics

```http
GET /api/admin/analytics/overview
GET /api/admin/analytics/trends?window=weekly
GET /api/admin/analytics/distribution
GET /api/admin/analytics/consistency
```

这些接口用于管理端数据看板。返回结构已经按页面展示聚合好，前端通常直接读 `data` 渲染即可。

## 9. 前端推荐接入顺序

1. 先接 `POST /api/auth/login/password`、`POST /api/auth/register`、`GET /api/auth/me`。
2. 再接 `GET /api/posts/published`、`GET /api/posts/search`、`GET /api/posts/{postId}`。
3. 再接评论列表、发表评论、点赞、收藏、分享。
4. 再接图片上传、创建帖子、编辑帖子、删除帖子。
5. 再接通知、举报、公开用户主页。
6. 最后接 `/api/admin/**` 管理端接口。

## 10. 常见接入注意事项

- `GET /api/posts/{postId}`、`GET /api/posts/{postId}/comments`、公开搜索和公开列表支持游客访问。
- 点赞、收藏、发帖、评论、举报、通知、我的资料、我的帖子都需要 token。
- 图片上传和图片搜索都是 `multipart/form-data`，不要手动设置 `Content-Type`，让浏览器自动带 boundary。
- 图片搜索 AI 不可用时，不一定抛错，可能返回空 `items` 和提示信息。
- 文本搜索 `keyword` 非空时后端 AI 优先，失败自动 SQL 兜底，前端只需要调用一个接口。
- 管理端接口必须使用 `role=ADMIN` 的 token，例如 `demo_admin / Password123`。
- 统一处理 401/403：清理本地 token，跳回登录页。
