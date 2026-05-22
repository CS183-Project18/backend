const API_BASE = window.CURATOR_API_BASE || localStorage.getItem("curator_api_base") || "http://localhost:8080";
const STORAGE_KEYS = {
  token: "curator_token_v1",
  auth: "curator_auth_v1",
};

const artLibrary = [
  "art-swirl",
  "art-forest",
  "art-workspace",
  "art-interior",
  "art-copper",
  "art-frame",
];

const state = {
  token: "",
  auth: null,
  profile: null,
  posts: [],
  myPosts: [],
  favoritePosts: [],
  trendingPosts: [],
  categories: [],
  tags: [],
  currentView: "feed",
  currentTrend: "daily",
  currentDetailPost: null,
  currentComments: [],
  currentCommentsPage: 1,
  currentCommentsTotal: 0,
  currentDetailImageIndex: 0,
  feedFilter: "all",
  lastSearchKeyword: "",
  searchMode: "text",
  imageSearchMessage: "",
  imageSearchInFlight: false,
  loading: false,
};

const authScreen = document.querySelector("#auth-screen");
const appScreen = document.querySelector("#app-screen");
const detailScreen = document.querySelector("#detail-screen");
const authForm = document.querySelector("#auth-form");
const demoLogin = document.querySelector("#demo-login");
const togglePassword = document.querySelector("#toggle-password");
const signupPassword = document.querySelector("#signup-password");
const sidebarName = document.querySelector("#sidebar-name");
const sidebarRole = document.querySelector("#sidebar-role");
const sidebarAvatar = document.querySelector("#sidebar-avatar");
const detailUserAvatar = document.querySelector("#detail-user-avatar");
const commentUserAvatar = document.querySelector("#comment-user-avatar");
const greetingTitle = document.querySelector("#greeting-title");
const greetingSubtitle = document.querySelector("#greeting-subtitle");
const navLinks = document.querySelectorAll(".nav-link");
const viewNodes = {
  feed: document.querySelector("#view-feed"),
  explore: document.querySelector("#view-explore"),
  bookmarks: document.querySelector("#view-bookmarks"),
  analytics: document.querySelector("#view-analytics"),
  settings: document.querySelector("#view-settings"),
};
const feedGrid = document.querySelector("#feed-grid");
const communityList = document.querySelector("#community-list");
const exploreGrid = document.querySelector("#explore-grid");
const bookmarkGrid = document.querySelector("#bookmark-grid");
const analyticsGrid = document.querySelector("#analytics-grid");
const trendingList = document.querySelector("#trending-list");
const openAnalytics = document.querySelector("#open-analytics");
const globalSearch = document.querySelector("#global-search");
const detailSearchInput = document.querySelector("#detail-search-input");
const imageSearchTrigger = document.querySelector("#image-search-trigger");
const imageSearchInput = document.querySelector("#image-search-input");
const trendTabs = document.querySelectorAll(".trend-tab");
const feedFilters = document.querySelectorAll(".filter-chip");
const settingsForm = document.querySelector("#settings-form");
const settingsName = document.querySelector("#settings-name");
const settingsUsername = document.querySelector("#settings-username");
const settingsEmail = document.querySelector("#settings-email");
const settingsBio = document.querySelector("#settings-bio");
const logoutButton = document.querySelector("#logout-button");
const openCreatePost = document.querySelector("#open-create-post");
const createModal = document.querySelector("#create-modal");
const closeModalBackdrop = document.querySelector("#close-modal-backdrop");
const closeCreateModal = document.querySelector("#close-create-modal");
const cancelCreatePost = document.querySelector("#cancel-create-post");
const createPostForm = document.querySelector("#create-post-form");
const postImagesInput = document.querySelector("#post-images");
const uploadPreview = document.querySelector("#upload-preview");
const detailHero = document.querySelector("#detail-hero");
const thumbnailRow = document.querySelector("#thumbnail-row");
const detailInfoCard = document.querySelector("#detail-info-card");
const backToFeed = document.querySelector("#back-to-feed");
const commentForm = document.querySelector("#comment-form");
const commentInput = document.querySelector("#comment-input");
const commentList = document.querySelector("#comment-list");
const commentCount = document.querySelector("#comment-count");
const loadMoreComments = document.querySelector("#load-more-comments");
const toast = document.querySelector("#toast");

let pendingUploads = [];
let searchTimer = null;

initialize().catch((error) => {
  console.error(error);
  showToast(error.message || "Failed to initialize frontend.");
});

async function initialize() {
  hydrateAuthState();
  bindEvents();
  renderApp();
  await loadReferenceData();
  await loadSessionIfPossible();
  await loadDashboardData();
  renderApp();
}

function hydrateAuthState() {
  state.token = localStorage.getItem(STORAGE_KEYS.token) || "";
  state.auth = readStorage(STORAGE_KEYS.auth);
}

function bindEvents() {
  authForm.addEventListener("submit", handleSignup);
  demoLogin.addEventListener("click", handlePasswordLogin);
  togglePassword.addEventListener("click", togglePasswordVisibility);
  navLinks.forEach((link) => link.addEventListener("click", () => switchView(link.dataset.view)));
  openAnalytics.addEventListener("click", () => switchView("analytics"));
  trendTabs.forEach((tab) => {
    tab.addEventListener("click", async () => {
      state.currentTrend = tab.dataset.trend;
      trendTabs.forEach((node) => node.classList.remove("is-active"));
      tab.classList.add("is-active");
      await loadTrendingPosts();
      renderTrending();
    });
  });
  feedFilters.forEach((chip) => {
    chip.addEventListener("click", () => {
      state.feedFilter = chip.dataset.feed;
      feedFilters.forEach((node) => node.classList.remove("is-active"));
      chip.classList.add("is-active");
      renderAppViews();
    });
  });
  globalSearch.addEventListener("input", handleSearchInput);
  if (detailSearchInput) {
    detailSearchInput.addEventListener("input", () => {
      globalSearch.value = detailSearchInput.value;
      handleSearchInput();
    });
  }
  if (imageSearchTrigger && imageSearchInput) {
    imageSearchTrigger.addEventListener("click", () => imageSearchInput.click());
    imageSearchInput.addEventListener("change", handleImageSearch);
  }
  settingsForm.addEventListener("submit", saveSettings);
  logoutButton.addEventListener("click", logout);
  openCreatePost.addEventListener("click", openCreatePostModal);
  closeModalBackdrop.addEventListener("click", closeCreatePostModal);
  closeCreateModal.addEventListener("click", closeCreatePostModal);
  cancelCreatePost.addEventListener("click", closeCreatePostModal);
  postImagesInput.addEventListener("change", handleImageSelection);
  createPostForm.addEventListener("submit", handleCreatePost);
  backToFeed.addEventListener("click", closeDetailView);
  commentForm.addEventListener("submit", handleCommentSubmit);
  loadMoreComments.addEventListener("click", showMoreComments);
}

async function loadReferenceData() {
  try {
    const [categories, tags] = await Promise.all([
      apiRequest("/api/categories"),
      apiRequest("/api/tags"),
    ]);
    state.categories = Array.isArray(categories) ? categories : [];
    state.tags = Array.isArray(tags) ? tags : [];
  } catch (error) {
    console.warn("Failed to load reference data.", error);
  }
}

async function loadSessionIfPossible() {
  if (!state.token) {
    return;
  }

  try {
    const [authInfo, profile] = await Promise.all([
      apiRequest("/api/auth/me", { auth: true }),
      apiRequest("/api/users/me/profile", { auth: true }),
    ]);
    state.auth = authInfo;
    state.profile = profile;
    persistAuthState();
  } catch (error) {
    console.warn("Stored token is invalid.", error);
    clearAuthState();
  }
}

async function loadDashboardData() {
  state.loading = true;
  try {
    await Promise.all([
      loadPublishedPosts(),
      loadMyPosts(),
      loadTrendingPosts(),
      loadFavoritePosts(),
    ]);
  } finally {
    state.loading = false;
  }
}

async function loadPublishedPosts(keyword = "") {
  const query = new URLSearchParams({
    page: "1",
    pageSize: "20",
  });

  const trimmedKeyword = keyword.trim();
  if (trimmedKeyword) {
    query.set("keyword", trimmedKeyword);
    state.searchMode = "text";
    state.lastSearchKeyword = trimmedKeyword;
  } else {
    state.searchMode = "feed";
    state.lastSearchKeyword = "";
    state.imageSearchMessage = "";
  }

  const path = trimmedKeyword ? `/api/posts/search?${query.toString()}` : `/api/posts/published?${query.toString()}`;
  const pageData = await apiRequest(path, { auth: Boolean(state.token) });
  state.posts = normalizePosts(pageData?.items || []);
}

async function loadFavoritePosts() {
  if (!state.token) {
    state.favoritePosts = [];
    return;
  }

  try {
    const pageData = await apiRequest("/api/posts/favorites/mine?page=1&pageSize=20", { auth: true });
    state.favoritePosts = normalizePosts(pageData?.items || []);
  } catch (error) {
    console.warn("Failed to load favorites.", error);
    state.favoritePosts = [];
  }
}

async function loadMyPosts() {
  if (!state.token) {
    state.myPosts = [];
    return;
  }

  try {
    const pageData = await apiRequest("/api/posts/mine?page=1&pageSize=20", { auth: true });
    state.myPosts = normalizePosts(pageData?.items || []);
  } catch (error) {
    console.warn("Failed to load my posts.", error);
    state.myPosts = [];
  }
}

async function loadTrendingPosts() {
  const query = new URLSearchParams({
    window: state.currentTrend,
    page: "1",
    pageSize: "10",
  });
  const pageData = await apiRequest(`/api/posts/trending?${query.toString()}`, { auth: Boolean(state.token) });
  state.trendingPosts = normalizePosts(pageData?.items || []);
}

function renderApp() {
  const isAuthenticated = Boolean(state.token && state.auth);
  authScreen.classList.toggle("hidden", isAuthenticated);
  appScreen.classList.toggle("hidden", !isAuthenticated);
  if (!isAuthenticated) {
    detailScreen.classList.add("hidden");
    return;
  }
  applyUserToUI();
  switchView(state.currentView);
  renderAppViews();
}

function renderAppViews() {
  const visiblePosts = getVisiblePosts();
  renderFeedGrid(visiblePosts);
  renderCommunityList(visiblePosts);
  renderExplore(visiblePosts);
  renderBookmarks();
  renderAnalytics(visiblePosts);
  renderTrending();
}

function applyUserToUI() {
  const displayName = getDisplayName();
  const initials = getInitials(displayName || state.auth?.username || "CU");
  sidebarName.textContent = displayName || state.auth?.username || "Curator";
  sidebarRole.textContent = state.profile?.role || state.auth?.role || "USER";
  sidebarAvatar.textContent = initials;
  detailUserAvatar.textContent = initials;
  commentUserAvatar.textContent = initials;
  greetingTitle.textContent = `Good morning, ${firstName(displayName || state.auth?.username || "Curator")}.`;
  greetingSubtitle.textContent = state.imageSearchMessage || "Discover what is trending in your curation circles.";
  settingsName.value = state.profile?.nickname || "";
  settingsUsername.value = state.profile?.username || state.auth?.username || "";
  settingsEmail.value = state.auth?.email || "";
  settingsBio.value = state.profile?.bio || "";
  settingsUsername.disabled = true;
  settingsEmail.disabled = true;
  settingsEmail.placeholder = "Email is not editable in current backend API";
}

function switchView(view) {
  state.currentView = view;
  Object.entries(viewNodes).forEach(([key, node]) => {
    node.classList.toggle("is-visible", key === view);
  });
  navLinks.forEach((link) => {
    link.classList.toggle("is-active", link.dataset.view === view);
  });
}

function renderFeedGrid(posts) {
  const source = [...posts];
  const primary = source[0];
  const secondary = source[1];
  const featured = source[2] || source[0];
  feedGrid.innerHTML = "";
  if (primary) {
    feedGrid.appendChild(createFeedCard(primary));
  }
  if (secondary) {
    feedGrid.appendChild(createFeedCard(secondary));
  }
  if (featured) {
    const feature = document.createElement("article");
    const asset = currentAsset(featured, 0);
    feature.className = "feature-card";
    feature.innerHTML = `<div class="feature-visual ${asset.className}" ${asset.style}></div><div class="feature-content"><div><span class="micro-label">${escapeHtml(featured.category)}</span><h3>${escapeHtml(featured.title)}</h3><p>${escapeHtml(truncate(featured.description, 140))}</p></div><div class="card-footer"><div class="card-meta"><div class="avatar small">${escapeHtml(featured.avatar)}</div><div><strong>${escapeHtml(featured.authorName)}</strong><span>${escapeHtml(featured.authorRole)}</span></div></div><button class="icon-button open-feature-detail" data-id="${featured.id}" type="button">Open</button></div></div>`;
    feedGrid.appendChild(feature);
    feature.querySelector(".open-feature-detail").addEventListener("click", () => openDetailView(featured.id));
  }
  if (!primary && !secondary && !featured) {
    feedGrid.innerHTML = createEmptyState("No posts yet.", "Start by creating a post or try another search keyword.");
  }
}

function createFeedCard(post) {
  const card = document.createElement("article");
  const asset = currentAsset(post, 0);
  card.className = "feed-card";
  card.innerHTML = `<div class="card-art ${asset.className}" ${asset.style}><span class="category-pill">${escapeHtml(post.category)}</span></div><div class="card-body"><h3>${escapeHtml(post.title)}</h3><div class="card-footer"><div class="curator-avatars"><div class="avatar small">${escapeHtml(post.avatar)}</div><div class="avatar small">+${Math.max(2, Math.floor((post.likes || 0) / 50) || 2)}</div></div><div class="stat-line"><span>Like ${formatCount(post.likes)}</span><span>Comment ${formatCount(post.commentsCount)}</span></div></div></div>`;
  card.addEventListener("click", () => openDetailView(post.id));
  return card;
}

function renderCommunityList(posts) {
  let list = [...posts];
  if (state.feedFilter === "uploaded") {
    list = [...state.myPosts];
  }
  if (state.feedFilter === "reposted") {
    list = [];
  }
  if (!list.length) {
    communityList.innerHTML = createEmptyState("No community posts found.", "Try a broader search or switch back to All.");
    return;
  }

  communityList.innerHTML = list.map((post) => {
    const asset = currentAsset(post, 0);
    return `<article class="list-card"><div class="mini-cover ${asset.className}" ${asset.style}></div><div class="list-card-body"><div class="panel-title-row compact"><div><span class="micro-label">${escapeHtml(post.category)}</span><h3>${escapeHtml(post.title)}</h3></div><button class="secondary-button open-post" type="button" data-id="${post.id}">View</button></div><p>${escapeHtml(post.description)}</p><div class="detail-author-row"><div class="card-meta"><div class="avatar small">${escapeHtml(post.avatar)}</div><div><strong>${escapeHtml(post.authorName)}</strong><span>${escapeHtml(post.authorRole)}</span></div></div><div class="stat-line"><span>Like ${formatCount(post.likes)}</span><span>Favorite ${formatCount(post.favorites)}</span><span>Comment ${formatCount(post.commentsCount)}</span></div></div><div class="action-row"><button class="action-pill ${post.likedByCurrentUser ? "is-active" : ""}" data-action="like" data-id="${post.id}" type="button">Like</button><button class="action-pill ${post.favoritedByCurrentUser ? "is-active" : ""}" data-action="save" data-id="${post.id}" type="button">Save</button><button class="action-pill" data-action="share" data-id="${post.id}" type="button">Share</button><button class="action-pill" data-action="comment" data-id="${post.id}" type="button">Comment</button></div></div></article>`;
  }).join("");

  communityList.querySelectorAll(".open-post").forEach((button) => {
    button.addEventListener("click", () => openDetailView(Number(button.dataset.id)));
  });

  communityList.querySelectorAll(".action-pill").forEach((button) => {
    button.addEventListener("click", async (event) => {
      event.stopPropagation();
      await handlePostAction(button.dataset.action, Number(button.dataset.id));
    });
  });
}

function renderExplore(posts) {
  const list = [...posts].sort((left, right) => (right.likes + right.favorites) - (left.likes + left.favorites));
  if (!list.length) {
    exploreGrid.innerHTML = createEmptyState("No explore content yet.", "Run a text search or try image search.");
    return;
  }

  exploreGrid.innerHTML = list.map((post) => {
    const asset = currentAsset(post, 0);
    return `<article class="explore-card"><div class="explore-cover ${asset.className}" ${asset.style}></div><div class="explore-body"><span class="micro-label">${escapeHtml(post.location)}</span><h3>${escapeHtml(post.title)}</h3><p>${escapeHtml(truncate(post.description, 100))}</p><div class="bookmark-meta"><span>Like ${formatCount(post.likes)}</span><button class="text-button open-explore-post" data-id="${post.id}" type="button">Open</button></div></div></article>`;
  }).join("");

  exploreGrid.querySelectorAll(".open-explore-post").forEach((button) => {
    button.addEventListener("click", () => openDetailView(Number(button.dataset.id)));
  });
}

function renderBookmarks() {
  if (!state.favoritePosts.length) {
    bookmarkGrid.innerHTML = createEmptyState("No bookmarks yet.", "Use Save on any post to populate this view.");
    return;
  }

  bookmarkGrid.innerHTML = state.favoritePosts.map((post) => {
    const asset = currentAsset(post, 0);
    return `<article class="bookmark-card"><div class="bookmark-cover ${asset.className}" ${asset.style}></div><div class="bookmark-body"><span class="micro-label">${escapeHtml(post.category)}</span><h3>${escapeHtml(post.title)}</h3><p>${escapeHtml(truncate(post.description, 100))}</p><div class="bookmark-meta"><span>${escapeHtml(post.location)}</span><button class="secondary-button open-bookmark-post" type="button" data-id="${post.id}">View</button></div></div></article>`;
  }).join("");

  bookmarkGrid.querySelectorAll(".open-bookmark-post").forEach((button) => {
    button.addEventListener("click", () => openDetailView(Number(button.dataset.id)));
  });
}

function renderAnalytics(posts) {
  const totalLikes = posts.reduce((sum, post) => sum + (post.likes || 0), 0);
  const totalFavorites = posts.reduce((sum, post) => sum + (post.favorites || 0), 0);
  const totalComments = posts.reduce((sum, post) => sum + (post.commentsCount || 0), 0);
  const publishedCount = posts.length;
  analyticsGrid.innerHTML = [
    createStatCard("Visible Posts", publishedCount, "Current feed or search result size"),
    createStatCard("Total Likes", totalLikes, "Combined like count across visible posts"),
    createStatCard("Total Favorites", totalFavorites, "Combined favorite count across visible posts"),
    createStatCard("Total Comments", totalComments, "Combined comment count across visible posts"),
  ].join("");
}

function renderTrending() {
  if (!state.trendingPosts.length) {
    trendingList.innerHTML = createEmptyState("No trending data.", "The backend has not returned trending posts yet.");
    return;
  }

  trendingList.innerHTML = state.trendingPosts.map((post, index) => {
    return `<button class="trend-item" type="button" data-id="${post.id}"><span class="trend-rank">0${index + 1}</span><div><strong>${escapeHtml(post.title)}</strong><small>${escapeHtml(post.category)}</small></div></button>`;
  }).join("");

  trendingList.querySelectorAll(".trend-item").forEach((button) => {
    button.addEventListener("click", () => openDetailView(Number(button.dataset.id)));
  });
}

async function handleSignup(event) {
  event.preventDefault();
  const fullName = document.querySelector("#signup-name").value.trim();
  const email = document.querySelector("#signup-email").value.trim();
  const username = document.querySelector("#signup-username").value.trim();
  const password = signupPassword.value;

  try {
    const loginResponse = await apiRequest("/api/auth/register", {
      method: "POST",
      body: {
        username,
        email,
        password,
      },
    });
    applyLogin(loginResponse);
    await loadSessionIfPossible();
    if (fullName) {
      await updateProfileQuietly({ nickname: fullName });
    }
    await loadDashboardData();
    renderApp();
    showToast("Account created successfully.");
  } catch (error) {
    showToast(error.message || "Registration failed.");
  }
}

async function handlePasswordLogin() {
  const account = document.querySelector("#signup-username").value.trim() || document.querySelector("#signup-email").value.trim();
  const password = signupPassword.value;
  if (!account || !password) {
    showToast("Enter username or email plus password to log in.");
    return;
  }

  try {
    const loginResponse = await apiRequest("/api/auth/login/password", {
      method: "POST",
      body: {
        account,
        password,
      },
    });
    applyLogin(loginResponse);
    await loadSessionIfPossible();
    await loadDashboardData();
    renderApp();
    showToast("Logged in successfully.");
  } catch (error) {
    showToast(error.message || "Login failed.");
  }
}

function applyLogin(loginResponse) {
  state.token = loginResponse.token;
  state.auth = {
    userId: loginResponse.userId,
    username: loginResponse.username,
    role: loginResponse.role,
    email: loginResponse.email,
  };
  persistAuthState();
}

async function saveSettings(event) {
  event.preventDefault();
  if (!ensureAuthenticated("Log in before editing your profile.")) {
    return;
  }

  try {
    await updateProfileQuietly({
      nickname: settingsName.value.trim() || null,
      bio: settingsBio.value.trim() || null,
    });
    renderApp();
    showToast("Profile updated. Username and email stay read-only in current backend.");
  } catch (error) {
    showToast(error.message || "Failed to update profile.");
  }
}

function logout() {
  clearAuthState();
  state.profile = null;
  state.myPosts = [];
  state.favoritePosts = [];
  state.currentDetailPost = null;
  globalSearch.value = "";
  if (detailSearchInput) {
    detailSearchInput.value = "";
  }
  state.lastSearchKeyword = "";
  state.imageSearchMessage = "";
  loadDashboardData()
    .then(() => {
      state.currentView = "feed";
      renderApp();
      showToast("Logged out.");
    })
    .catch((error) => {
      console.warn(error);
      renderApp();
    });
}

function openCreatePostModal() {
  if (!ensureAuthenticated("Log in before creating posts.")) {
    return;
  }
  createModal.classList.remove("hidden");
}

function closeCreatePostModal() {
  createModal.classList.add("hidden");
  createPostForm.reset();
  uploadPreview.innerHTML = "";
  pendingUploads = [];
}

async function handleImageSelection(event) {
  const files = Array.from(event.target.files || []).slice(0, 4);
  pendingUploads = files;
  const previews = await Promise.all(files.map(fileToDataUrl));
  uploadPreview.innerHTML = previews.map((src) => `<div class="preview-item" style="background-image:url('${escapeAttribute(src)}')"></div>`).join("");
}

async function handleCreatePost(event) {
  event.preventDefault();
  if (!ensureAuthenticated("Log in before creating posts.")) {
    return;
  }

  try {
    const title = document.querySelector("#post-title").value.trim();
    const categoryName = document.querySelector("#post-category").value.trim();
    const priceInput = document.querySelector("#post-price").value.trim();
    const locationText = document.querySelector("#post-location").value.trim();
    const description = document.querySelector("#post-description").value.trim();
    const rawTagNames = document.querySelector("#post-tags").value.trim();
    const { priceMin, priceMax, currency } = parsePriceInput(priceInput);
    const imagePayload = await uploadSelectedImages();
    const requestBody = {
      title,
      description,
      categoryId: resolveCategoryId(categoryName),
      locationText: locationText || null,
      priceMin,
      priceMax,
      currency,
      tagIds: resolveTagIds(rawTagNames),
      images: imagePayload,
    };

    await apiRequest("/api/posts", {
      method: "POST",
      auth: true,
      body: requestBody,
    });

    await Promise.all([
      loadPublishedPosts(state.lastSearchKeyword),
      loadMyPosts(),
      loadFavoritePosts(),
      loadTrendingPosts(),
    ]);
    closeCreatePostModal();
    renderApp();
    showToast("Post submitted successfully.");
  } catch (error) {
    showToast(error.message || "Failed to create post.");
  }
}

async function openDetailView(postId) {
  try {
    const [postData, commentsPage] = await Promise.all([
      apiRequest(`/api/posts/${postId}`, { auth: Boolean(state.token) }),
      apiRequest(`/api/posts/${postId}/comments?page=1&pageSize=20`, { auth: Boolean(state.token) }),
    ]);
    state.currentDetailPost = normalizePost(postData);
    state.currentComments = commentsPage?.items || [];
    state.currentCommentsPage = 1;
    state.currentCommentsTotal = Number(commentsPage?.total || 0);
    state.currentDetailImageIndex = 0;
    appScreen.classList.add("hidden");
    detailScreen.classList.remove("hidden");
    renderDetail();
  } catch (error) {
    showToast(error.message || "Failed to load post detail.");
  }
}

function closeDetailView() {
  detailScreen.classList.add("hidden");
  appScreen.classList.remove("hidden");
  state.currentDetailPost = null;
  state.currentComments = [];
  state.currentCommentsPage = 1;
  state.currentCommentsTotal = 0;
}

function renderDetail() {
  if (!state.currentDetailPost) {
    return;
  }
  renderDetailHero();
  renderDetailInfo();
  renderComments();
}

function renderDetailHero() {
  const post = state.currentDetailPost;
  const asset = currentAsset(post, state.currentDetailImageIndex);
  detailHero.className = `detail-hero ${asset.className}`;
  if (asset.style) {
    detailHero.setAttribute("style", asset.style.replace(/^style="/, "").replace(/"$/, ""));
  } else {
    detailHero.removeAttribute("style");
  }
  thumbnailRow.innerHTML = post.gallery.map((image, index) => {
    const item = assetForGallery(image);
    return `<button class="thumb ${item.className} ${index === state.currentDetailImageIndex ? "is-active" : ""}" data-index="${index}" ${item.style} type="button"></button>`;
  }).join("");
  thumbnailRow.querySelectorAll(".thumb").forEach((button) => {
    button.addEventListener("click", () => {
      state.currentDetailImageIndex = Number(button.dataset.index);
      renderDetailHero();
    });
  });
}

function renderDetailInfo() {
  const post = state.currentDetailPost;
  const canDelete = post.ownedByCurrentUser;
  detailInfoCard.innerHTML = `<div class="interaction-row"><div><span class="micro-label">${escapeHtml(post.category)}</span><span class="micro-label">${escapeHtml(post.status)}</span></div><div class="interaction-row"><button class="icon-button detail-like ${post.likedByCurrentUser ? "active" : ""}" data-id="${post.id}" type="button">Like</button><button class="icon-button detail-save ${post.favoritedByCurrentUser ? "active" : ""}" data-id="${post.id}" type="button">Save</button></div></div><h2>${escapeHtml(post.title)}</h2><div class="detail-author-row"><div class="card-meta"><div class="avatar small">${escapeHtml(post.avatar)}</div><div><strong>${escapeHtml(post.authorName)}</strong><span>${escapeHtml(post.authorRole)}</span></div></div><button class="follow-button" type="button" disabled>Follow</button></div><div class="detail-meta-grid"><article><small>Price</small><strong>${escapeHtml(post.price)}</strong></article><article><small>Location</small><strong>${escapeHtml(post.location)}</strong></article></div><small>Associated tags</small><div class="tag-row">${post.tags.map((tag) => `<span class="tag-pill">${escapeHtml(tag)}</span>`).join("") || "<span class=\"tag-pill\">No tags</span>"}</div><p>${escapeHtml(post.description)}</p><div class="detail-bottom-row"><span>${formatCount(post.likes)} likes</span><span>${formatCount(post.favorites)} saves</span><span>${formatCount(post.commentsCount)} comments</span><button class="detail-link-action" data-detail-action="share" data-id="${post.id}" type="button">Share</button><button class="detail-link-action danger" data-detail-action="delete" data-id="${post.id}" type="button" ${canDelete ? "" : "disabled"}>Delete</button></div><div class="action-row"><button class="action-pill ${post.likedByCurrentUser ? "is-active" : ""}" data-action="like" data-id="${post.id}" type="button">Like</button><button class="action-pill ${post.favoritedByCurrentUser ? "is-active" : ""}" data-action="save" data-id="${post.id}" type="button">Save</button><button class="action-pill" data-action="share" data-id="${post.id}" type="button">Share</button></div>`;

  detailInfoCard.querySelectorAll("[data-action]").forEach((button) => {
    button.addEventListener("click", async () => {
      await handlePostAction(button.dataset.action, Number(button.dataset.id), true);
    });
  });
  detailInfoCard.querySelector(".detail-like").addEventListener("click", async () => {
    await handlePostAction("like", post.id, true);
  });
  detailInfoCard.querySelector(".detail-save").addEventListener("click", async () => {
    await handlePostAction("save", post.id, true);
  });
  detailInfoCard.querySelectorAll("[data-detail-action]").forEach((button) => {
    button.addEventListener("click", async () => {
      if (button.dataset.detailAction === "share") {
        await handlePostAction("share", Number(button.dataset.id), true);
        return;
      }
      if (button.dataset.detailAction === "delete") {
        await deletePost(Number(button.dataset.id));
      }
    });
  });
}

function renderComments() {
  commentCount.textContent = String(state.currentCommentsTotal);
  if (!state.currentComments.length) {
    commentList.innerHTML = createEmptyState("No comments yet.", "Be the first one to add feedback.");
  } else {
    commentList.innerHTML = state.currentComments.map((comment) => {
      return `<article class="comment-item"><div class="avatar small">${escapeHtml(getInitials(comment.username || "CU"))}</div><div><strong>${escapeHtml(comment.username || "Unknown")}</strong><div class="comment-meta">${escapeHtml(formatDateTime(comment.createdAt))}</div><p>${escapeHtml(comment.content || "")}</p><div class="comment-actions"><span>Like ${formatCount(comment.likeCount || 0)}</span><span>${comment.pinned ? "Pinned" : "Comment"}</span></div></div></article>`;
    }).join("");
  }
  loadMoreComments.classList.toggle("hidden", state.currentComments.length >= state.currentCommentsTotal);
}

async function handleCommentSubmit(event) {
  event.preventDefault();
  if (!state.currentDetailPost) {
    return;
  }
  if (!ensureAuthenticated("Log in before posting comments.")) {
    return;
  }

  const content = commentInput.value.trim();
  if (!content) {
    return;
  }

  try {
    await apiRequest(`/api/posts/${state.currentDetailPost.id}/comments`, {
      method: "POST",
      auth: true,
      body: { content },
    });
    commentInput.value = "";
    await refreshCurrentDetail();
    showToast("Comment posted.");
  } catch (error) {
    showToast(error.message || "Failed to post comment.");
  }
}

async function showMoreComments() {
  if (!state.currentDetailPost) {
    return;
  }
  const nextPage = state.currentCommentsPage + 1;
  try {
    const commentsPage = await apiRequest(`/api/posts/${state.currentDetailPost.id}/comments?page=${nextPage}&pageSize=20`, {
      auth: Boolean(state.token),
    });
    state.currentCommentsPage = nextPage;
    state.currentCommentsTotal = Number(commentsPage?.total || state.currentCommentsTotal);
    state.currentComments = state.currentComments.concat(commentsPage?.items || []);
    renderComments();
  } catch (error) {
    showToast(error.message || "Failed to load more comments.");
  }
}

async function handlePostAction(action, postId, refreshDetail = false) {
  try {
    if (action === "comment") {
      await openDetailView(postId);
      commentInput.focus();
      return;
    }
    if (action === "share") {
      await sharePost(postId);
      return;
    }
    if (!ensureAuthenticated("Log in before using this action.")) {
      return;
    }

    const post = findVisiblePost(postId);
    if (action === "like") {
      if (post?.likedByCurrentUser) {
        await apiRequest(`/api/posts/${postId}/like`, { method: "DELETE", auth: true });
      } else {
        await apiRequest(`/api/posts/${postId}/like`, { method: "POST", auth: true });
      }
    }
    if (action === "save") {
      if (post?.favoritedByCurrentUser) {
        await apiRequest(`/api/posts/${postId}/favorite`, { method: "DELETE", auth: true });
      } else {
        await apiRequest(`/api/posts/${postId}/favorite`, { method: "POST", auth: true });
      }
    }

    await Promise.all([
      loadPublishedPosts(state.lastSearchKeyword),
      loadMyPosts(),
      loadFavoritePosts(),
      loadTrendingPosts(),
    ]);
    if (refreshDetail || (state.currentDetailPost && state.currentDetailPost.id === postId)) {
      await refreshCurrentDetail();
    }
    renderApp();
  } catch (error) {
    showToast(error.message || "Action failed.");
  }
}

async function deletePost(postId) {
  if (!ensureAuthenticated("Log in before deleting posts.")) {
    return;
  }

  try {
    await apiRequest(`/api/posts/${postId}`, {
      method: "DELETE",
      auth: true,
    });
    await Promise.all([
      loadPublishedPosts(state.lastSearchKeyword),
      loadMyPosts(),
      loadFavoritePosts(),
      loadTrendingPosts(),
    ]);
    closeDetailView();
    renderApp();
    showToast("Post deleted.");
  } catch (error) {
    showToast(error.message || "Failed to delete post.");
  }
}

async function sharePost(postId) {
  try {
    const shareData = await apiRequest(`/api/posts/${postId}/share`, {
      method: "POST",
      auth: Boolean(state.token),
    });
    if (shareData?.shareUrl && navigator.clipboard?.writeText) {
      await navigator.clipboard.writeText(shareData.shareUrl);
      showToast("Share link copied.");
      return;
    }
    showToast(shareData?.shareUrl || "Share link generated.");
  } catch (error) {
    showToast(error.message || "Failed to generate share link.");
  }
}

function handleSearchInput() {
  if (detailSearchInput && detailSearchInput.value !== globalSearch.value) {
    detailSearchInput.value = globalSearch.value;
  }
  clearTimeout(searchTimer);
  searchTimer = setTimeout(async () => {
    try {
      await loadPublishedPosts(globalSearch.value);
      renderApp();
    } catch (error) {
      showToast(error.message || "Search failed.");
    }
  }, 300);
}

async function handleImageSearch(event) {
  const file = event.target.files?.[0];
  if (!file) {
    return;
  }

  state.imageSearchInFlight = true;
  try {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("page", "1");
    formData.append("pageSize", "20");
    const envelope = await apiRequest("/api/posts/search/image", {
      method: "POST",
      auth: Boolean(state.token),
      formData,
      returnEnvelope: true,
    });
    state.posts = normalizePosts(envelope?.data?.items || []);
    state.imageSearchMessage = envelope?.message || "Showing image search results.";
    state.searchMode = "image";
    switchView("explore");
    renderApp();
    showToast(state.posts.length ? "Image search complete." : "Image search returned no posts.");
  } catch (error) {
    showToast(error.message || "Image search failed.");
  } finally {
    state.imageSearchInFlight = false;
    event.target.value = "";
  }
}

async function refreshCurrentDetail() {
  if (!state.currentDetailPost) {
    return;
  }
  await openDetailView(state.currentDetailPost.id);
}

function getVisiblePosts() {
  return state.posts;
}

function findVisiblePost(postId) {
  return [...state.posts, ...state.favoritePosts, ...state.trendingPosts].find((post) => post.id === postId) || state.currentDetailPost;
}

function normalizePosts(posts) {
  return posts.map(normalizePost);
}

function normalizePost(post) {
  const images = Array.isArray(post.images) ? post.images : [];
  const gallery = images.length
    ? images.map((image) => ({
        type: "image",
        value: image.imageUrl,
      }))
    : [{
        type: "art",
        value: randomArt(post.id),
      }];

  return {
    id: post.id,
    title: post.title || "Untitled Post",
    description: post.description || "",
    category: post.categorySummary?.name || "Uncategorized",
    location: post.locationText || post.storeSummary?.name || "Unknown Location",
    price: formatPrice(post.priceMin, post.priceMax, post.currency),
    tags: Array.isArray(post.tags) ? post.tags.map((tag) => `#${tag.name}`) : [],
    authorName: post.authorUsername || "Unknown User",
    authorRole: post.status || "PUBLISHED",
    avatar: getInitials(post.authorUsername || "CU"),
    likes: Number(post.likeCount || 0),
    favorites: Number(post.favoriteCount || 0),
    commentsCount: Number(post.commentCount || 0),
    views: Number(post.viewCount || 0),
    gallery,
    createdAt: post.createdAt || post.publishedAt || new Date().toISOString(),
    updatedAt: post.updatedAt || post.createdAt || new Date().toISOString(),
    status: post.status || "PUBLISHED",
    likedByCurrentUser: Boolean(post.likedByCurrentUser),
    favoritedByCurrentUser: Boolean(post.favoritedByCurrentUser),
    shareUrl: post.shareUrl || "",
    ownedByCurrentUser: Boolean(state.auth?.userId && post.userId === state.auth.userId),
  };
}

function currentAsset(post, index) {
  const image = post.gallery[index] || post.gallery[0] || { type: "art", value: randomArt(post.id) };
  return assetForGallery(image);
}

function assetForGallery(image) {
  if (image.type === "image") {
    return {
      className: "image-cover",
      style: `style="background-image:url('${escapeAttribute(image.value)}')"`,
    };
  }
  return {
    className: image.value,
    style: "",
  };
}

function createStatCard(label, value, caption) {
  return `<article class="analytics-card"><span class="micro-label">${escapeHtml(label)}</span><strong>${escapeHtml(String(value))}</strong><p>${escapeHtml(caption)}</p></article>`;
}

function createEmptyState(title, description) {
  return `<article class="panel" style="padding:24px;"><h3>${escapeHtml(title)}</h3><p>${escapeHtml(description)}</p></article>`;
}

function togglePasswordVisibility() {
  const showPassword = signupPassword.type === "password";
  signupPassword.type = showPassword ? "text" : "password";
  togglePassword.textContent = showPassword ? "Hide" : "Show";
}

function persistAuthState() {
  localStorage.setItem(STORAGE_KEYS.token, state.token);
  localStorage.setItem(STORAGE_KEYS.auth, JSON.stringify(state.auth));
}

function clearAuthState() {
  state.token = "";
  state.auth = null;
  localStorage.removeItem(STORAGE_KEYS.token);
  localStorage.removeItem(STORAGE_KEYS.auth);
}

function readStorage(key) {
  try {
    const raw = localStorage.getItem(key);
    return raw ? JSON.parse(raw) : null;
  } catch (error) {
    console.warn("Storage parse failed.", error);
    return null;
  }
}

function getDisplayName() {
  return state.profile?.nickname || state.profile?.username || state.auth?.username || "";
}

function firstName(name) {
  return String(name || "").trim().split(/\s+/)[0] || "Curator";
}

function getInitials(name) {
  const tokens = String(name || "").trim().split(/\s+/).filter(Boolean);
  if (!tokens.length) {
    return "CU";
  }
  return tokens.slice(0, 2).map((token) => token[0].toUpperCase()).join("");
}

function truncate(value, length) {
  if (!value || value.length <= length) {
    return value || "";
  }
  return `${value.slice(0, length - 3)}...`;
}

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}

function escapeAttribute(value) {
  return escapeHtml(value).replaceAll("`", "&#96;");
}

function showToast(message) {
  toast.textContent = message;
  toast.classList.remove("hidden");
  window.clearTimeout(showToast.timerId);
  showToast.timerId = window.setTimeout(() => {
    toast.classList.add("hidden");
  }, 2600);
}

function formatCount(value) {
  return new Intl.NumberFormat("en-US", { notation: "compact", maximumFractionDigits: 1 }).format(Number(value || 0));
}

function formatDateTime(value) {
  if (!value) {
    return "Just now";
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return "Just now";
  }
  return date.toLocaleString();
}

function formatPrice(priceMin, priceMax, currency) {
  if (priceMin == null && priceMax == null) {
    return "N/A";
  }

  const normalizedCurrency = currency || "USD";
  const formatter = new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: normalizedCurrency,
    maximumFractionDigits: 2,
  });
  if (priceMin != null && priceMax != null && Number(priceMin) !== Number(priceMax)) {
    return `${formatter.format(Number(priceMin))} - ${formatter.format(Number(priceMax))}`;
  }
  return formatter.format(Number(priceMin ?? priceMax));
}

function randomArt(seed = Date.now()) {
  return artLibrary[Math.abs(Number(seed)) % artLibrary.length];
}

function parsePriceInput(value) {
  if (!value) {
    return { priceMin: null, priceMax: null, currency: null };
  }

  const matches = value.match(/-?\d+(\.\d+)?/g) || [];
  const symbol = value.trim()[0];
  const currency = symbol === "￥" ? "CNY" : symbol === "€" ? "EUR" : symbol === "£" ? "GBP" : "USD";
  if (!matches.length) {
    return { priceMin: null, priceMax: null, currency };
  }
  if (matches.length === 1) {
    return { priceMin: Number(matches[0]), priceMax: Number(matches[0]), currency };
  }
  return {
    priceMin: Number(matches[0]),
    priceMax: Number(matches[1]),
    currency,
  };
}

function resolveCategoryId(name) {
  if (!name) {
    return null;
  }
  const category = state.categories.find((item) => String(item.name).toLowerCase() === name.toLowerCase());
  return category?.id || null;
}

function resolveTagIds(rawTagNames) {
  if (!rawTagNames) {
    return [];
  }
  return rawTagNames
    .split(",")
    .map((tag) => tag.trim().replace(/^#/, ""))
    .filter(Boolean)
    .map((tagName) => state.tags.find((tag) => String(tag.name).toLowerCase() === tagName.toLowerCase())?.id)
    .filter(Boolean);
}

async function uploadSelectedImages() {
  if (!pendingUploads.length) {
    return [];
  }

  const uploads = [];
  for (const file of pendingUploads) {
    const formData = new FormData();
    formData.append("file", file);
    const response = await apiRequest("/api/files/images", {
      method: "POST",
      auth: true,
      formData,
    });
    uploads.push({
      imageUrl: response.url,
      imageKey: response.fileName,
      thumbnailUrl: response.url,
      fileSize: response.size,
      mimeType: response.contentType,
    });
  }
  return uploads;
}

async function updateProfileQuietly(body) {
  state.profile = await apiRequest("/api/users/me/profile", {
    method: "PUT",
    auth: true,
    body,
  });
}

function fileToDataUrl(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(String(reader.result));
    reader.onerror = () => reject(reader.error || new Error("Failed to read file."));
    reader.readAsDataURL(file);
  });
}

function ensureAuthenticated(message) {
  if (state.token && state.auth) {
    return true;
  }
  showToast(message);
  return false;
}

async function apiRequest(path, options = {}) {
  const {
    method = "GET",
    auth = false,
    body,
    formData,
    returnEnvelope = false,
  } = options;

  const headers = {};
  if (auth && state.token) {
    headers.Authorization = `Bearer ${state.token}`;
  }
  if (!formData) {
    headers["Content-Type"] = "application/json";
  }

  const response = await fetch(`${API_BASE}${path}`, {
    method,
    headers,
    body: formData || (body ? JSON.stringify(body) : undefined),
  });

  let payload = null;
  try {
    payload = await response.json();
  } catch (error) {
    payload = null;
  }

  if (!response.ok) {
    throw new Error(payload?.message || `Request failed with status ${response.status}.`);
  }
  if (!payload?.success) {
    throw new Error(payload?.message || "Business request failed.");
  }
  return returnEnvelope ? payload : payload.data;
}
