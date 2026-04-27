const STORAGE_KEYS = {
  user: "curator_user",
  posts: "curator_posts_v2",
  bookmarks: "curator_bookmarks_v2",
  likes: "curator_likes_v2",
  loaded: "curator_seeded_v2",
};

const artLibrary = [
  "art-swirl",
  "art-forest",
  "art-workspace",
  "art-interior",
  "art-copper",
  "art-frame",
];

const seedPosts = [
  {
    id: 101,
    title: "The Evolution of Organic Geometry in Web 3.0",
    description:
      "A fluid abstract discovery that feels like it belongs in a curated digital gallery. The visual language is expressive, premium, and built for conversation.",
    category: "Digital Art",
    price: "Curated",
    location: "Remote Collection",
    tags: ["#motion", "#curation", "#digital"],
    authorName: "Julian Vesper",
    authorRole: "Principal Architect @ Vesper Collective",
    avatar: "JV",
    likes: 2400,
    comments: [
      {
        user: "Marcus Thorne",
        avatar: "MT",
        text: "The shape language here feels intentional. I would absolutely save this for future inspiration.",
        time: "2 hours ago",
      },
      {
        user: "Elena Rossi",
        avatar: "ER",
        text: "Strong curation. The color tension gives it a premium editorial tone.",
        time: "5 hours ago",
      },
      {
        user: "Arthur Vance",
        avatar: "AV",
        text: "This belongs on the explore page. The composition is instantly memorable.",
        time: "Yesterday",
      },
    ],
    saves: 128,
    reposts: 23,
    views: 4200,
    coverType: "art-swirl",
    gallery: [{ type: "art", value: "art-swirl" }, { type: "art", value: "art-copper" }, { type: "art", value: "art-frame" }],
    createdAt: Date.now() - 86400000 * 3,
    origin: "seed",
  },
  {
    id: 102,
    title: "Regenerative Design: More Than Just a Trend",
    description:
      "A forest-driven study in atmosphere, sustainability, and spatial emotion. A socially successful post because the image is strong and the narrative is clear.",
    category: "Sustainability",
    price: "Curated",
    location: "Northern Woods",
    tags: ["#nature", "#sustainable", "#retreat"],
    authorName: "Alex Rivera",
    authorRole: "Curator Gold",
    avatar: "AR",
    likes: 1800,
    comments: [
      {
        user: "Mina Lo",
        avatar: "ML",
        text: "This is the sort of post that gets reposted because it is both beautiful and useful.",
        time: "3 hours ago",
      },
      {
        user: "Sora Lin",
        avatar: "SL",
        text: "Would love a tagged location list of similar spaces and stores.",
        time: "7 hours ago",
      },
    ],
    saves: 84,
    reposts: 15,
    views: 3800,
    coverType: "art-forest",
    gallery: [{ type: "art", value: "art-forest" }, { type: "art", value: "art-frame" }, { type: "art", value: "art-copper" }],
    createdAt: Date.now() - 86400000 * 2,
    origin: "seed",
  },
  {
    id: 103,
    title: "Curating the Perfect Developer Sanctuary",
    description:
      "How physical space influences cognitive flow and creative momentum. This long-form find merges high-end visual identity with practical value.",
    category: "Tech Editorial",
    price: "$1,450 / hr",
    location: "Zurich, CH",
    tags: ["#workspace", "#focus", "#editorial"],
    authorName: "Marcus Chen",
    authorRole: "Editorial Director",
    avatar: "MC",
    likes: 1204,
    comments: [
      {
        user: "Julian Vesper",
        avatar: "JV",
        text: "The lighting balance here is incredible. Very strong material palette as well.",
        time: "1 hour ago",
      },
      {
        user: "Elena Rossi",
        avatar: "ER",
        text: "This has that calm premium feeling the platform should encourage.",
        time: "4 hours ago",
      },
      {
        user: "Marcus Thorne",
        avatar: "MT",
        text: "Would save this instantly for studio planning.",
        time: "Yesterday",
      },
    ],
    saves: 432,
    reposts: 31,
    views: 5200,
    coverType: "art-workspace",
    gallery: [{ type: "art", value: "art-workspace" }, { type: "art", value: "art-copper" }, { type: "art", value: "art-frame" }],
    createdAt: Date.now() - 86400000,
    origin: "seed",
  },
  {
    id: 104,
    title: "The Ethereal Pavilion: A Study in Light & Glass",
    description:
      "A pristine interior study with soft daylight, a framed forest view, and a spatial calm that feels designed for slow luxury living.",
    category: "Interior Design",
    price: "$1,450 / hr",
    location: "Zurich, CH",
    tags: ["#minimalism", "#glasshouse", "#sustainable", "#nature"],
    authorName: "Julian Vesper",
    authorRole: "Principal Architect @ Vesper Collective",
    avatar: "JV",
    likes: 1204,
    comments: [
      {
        user: "Marcus Thorne",
        avatar: "MT",
        text: "The way the light interacts with the double-height ceiling is absolutely breathtaking. Did you use specific glass coatings for UV protection or just standard tempered?",
        time: "2 hours ago",
      },
      {
        user: "Elena Rossi",
        avatar: "ER",
        text: "This is exactly the inspiration I needed for my lakeside project. The structural honesty is so refreshing.",
        time: "5 hours ago",
      },
      {
        user: "Arthur Vance",
        avatar: "AV",
        text: "Stunning work, Julian. The cantilevered section looks like it’s floating. I’d love to know more about the structural steel weight.",
        time: "Yesterday",
      },
    ],
    saves: 432,
    reposts: 41,
    views: 6400,
    coverType: "art-interior",
    gallery: [{ type: "art", value: "art-interior" }, { type: "art", value: "art-copper" }, { type: "art", value: "art-frame" }],
    createdAt: Date.now() - 3600000 * 12,
    origin: "seed",
  },
];

const state = {
  user: null,
  posts: [],
  bookmarks: [],
  likes: [],
  currentView: "feed",
  currentTrend: "daily",
  currentDetailId: null,
  currentDetailImageIndex: 0,
  feedFilter: "all",
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

let commentDisplayCount = 3;
let pendingUploads = [];

initialize();

function initialize() {
  seedDataIfNeeded();
  hydrateState();
  bindEvents();
  renderApp();
}

function seedDataIfNeeded() {
  if (!localStorage.getItem(STORAGE_KEYS.loaded)) {
    localStorage.setItem(STORAGE_KEYS.posts, JSON.stringify(seedPosts));
    localStorage.setItem(STORAGE_KEYS.bookmarks, JSON.stringify([104]));
    localStorage.setItem(STORAGE_KEYS.likes, JSON.stringify([]));
    localStorage.setItem(STORAGE_KEYS.loaded, "true");
  }
}

function hydrateState() {
  state.user =
    readStorage(STORAGE_KEYS.user) || {
      name: "Alex Rivera",
      username: "alexrivera",
      email: "alex@curator.gold",
      role: "Curator Gold",
      bio: "Curating premium interiors, digital artifacts, and spatial references worth revisiting.",
    };
  state.posts = readStorage(STORAGE_KEYS.posts) || [...seedPosts];
  state.bookmarks = readStorage(STORAGE_KEYS.bookmarks) || [104];
  state.likes = readStorage(STORAGE_KEYS.likes) || [];
}

function bindEvents() {
  authForm.addEventListener("submit", handleSignup);
  demoLogin.addEventListener("click", handleDemoLogin);
  togglePassword.addEventListener("click", togglePasswordVisibility);
  navLinks.forEach((link) => link.addEventListener("click", () => switchView(link.dataset.view)));
  openAnalytics.addEventListener("click", () => switchView("analytics"));
  trendTabs.forEach((tab) => {
    tab.addEventListener("click", () => {
      state.currentTrend = tab.dataset.trend;
      trendTabs.forEach((node) => node.classList.remove("is-active"));
      tab.classList.add("is-active");
      renderTrending();
    });
  });
  feedFilters.forEach((chip) => {
    chip.addEventListener("click", () => {
      state.feedFilter = chip.dataset.feed;
      feedFilters.forEach((node) => node.classList.remove("is-active"));
      chip.classList.add("is-active");
      renderCommunityList(filterPosts(globalSearch.value));
    });
  });
  globalSearch.addEventListener("input", () => renderAppViews());
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

function renderApp() {
  const isAuthenticated = Boolean(state.user);
  authScreen.classList.toggle("hidden", isAuthenticated);
  appScreen.classList.toggle("hidden", !isAuthenticated);
  detailScreen.classList.add("hidden");

  if (!isAuthenticated) return;

  applyUserToUI();
  switchView(state.currentView);
  renderAppViews();
}

function renderAppViews() {
  const filteredPosts = filterPosts(globalSearch.value);
  renderFeedGrid(filteredPosts);
  renderCommunityList(filteredPosts);
  renderExplore(filteredPosts);
  renderBookmarks();
  renderAnalytics(filteredPosts);
  renderTrending();
}

function applyUserToUI() {
  const initials = getInitials(state.user.name);
  sidebarName.textContent = state.user.name;
  sidebarRole.textContent = state.user.role || "Curator";
  sidebarAvatar.textContent = initials;
  detailUserAvatar.textContent = initials;
  commentUserAvatar.textContent = initials;
  greetingTitle.textContent = `Good morning, ${firstName(state.user.name)}.`;
  greetingSubtitle.textContent = "Discover what’s trending in your curation circles.";
  settingsName.value = state.user.name || "";
  settingsUsername.value = state.user.username || "";
  settingsEmail.value = state.user.email || "";
  settingsBio.value = state.user.bio || "";
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
  const source = [...posts].sort((a, b) => b.createdAt - a.createdAt);
  const primary = source[0];
  const secondary = source[1];
  const featured = source[2] || source[0];

  feedGrid.innerHTML = "";

  if (primary) feedGrid.appendChild(createFeedCard(primary));
  if (secondary) feedGrid.appendChild(createFeedCard(secondary));
  if (featured) {
    const feature = document.createElement("article");
    feature.className = "feature-card";
    feature.innerHTML = `
      <div class="feature-visual ${featured.coverType} ${featured.gallery[0].type === "image" ? "image-cover" : ""}" ${backgroundForAsset(featured.gallery[0])}></div>
      <div class="feature-content">
        <div>
          <span class="micro-label">${featured.category}</span>
          <h3>${escapeHtml(featured.title)}</h3>
          <p>${escapeHtml(featured.description.slice(0, 120))}...</p>
        </div>
        <div class="card-footer">
          <div class="card-meta">
            <div class="avatar small">${escapeHtml(featured.avatar || getInitials(featured.authorName))}</div>
            <div>
              <strong>${escapeHtml(featured.authorName)}</strong>
              <span>${escapeHtml(featured.authorRole)}</span>
            </div>
          </div>
          <button class="icon-button open-feature-detail" data-id="${featured.id}" type="button">→</button>
        </div>
      </div>
    `;
    feedGrid.appendChild(feature);
    feature.querySelector(".open-feature-detail").addEventListener("click", () => openDetailView(featured.id));
  }
}

function createFeedCard(post) {
  const card = document.createElement("article");
  card.className = "feed-card";
  card.innerHTML = `
    <div class="card-art ${post.coverType} ${post.gallery[0].type === "image" ? "image-cover" : ""}" ${backgroundForAsset(post.gallery[0])}>
      <span class="category-pill">${escapeHtml(post.category)}</span>
    </div>
    <div class="card-body">
      <h3>${escapeHtml(post.title)}</h3>
      <div class="card-footer">
        <div class="curator-avatars">
          <div class="avatar small">${escapeHtml(post.avatar || getInitials(post.authorName))}</div>
          <div class="avatar small">+${Math.max(2, Math.floor(post.likes / 600))}</div>
        </div>
        <div class="stat-line">
          <span>♥ ${formatCount(post.likes)}</span>
          <span>◼ ${post.comments.length}</span>
        </div>
      </div>
    </div>
  `;
  card.addEventListener("click", () => openDetailView(post.id));
  return card;
}

function renderCommunityList(posts) {
  let list = [...posts].sort((a, b) => b.createdAt - a.createdAt);

  if (state.feedFilter === "uploaded") {
    list = list.filter((post) => post.origin === "user");
  }
  if (state.feedFilter === "reposted") {
    list = list.filter((post) => post.origin === "repost");
  }

  communityList.innerHTML = list
    .map(
      (post) => `
        <article class="list-card">
          <div class="mini-cover ${post.coverType} ${post.gallery[0].type === "image" ? "image-cover" : ""}" ${backgroundForAsset(post.gallery[0])}></div>
          <div class="list-card-body">
            <div class="panel-title-row compact">
              <div>
                <span class="micro-label">${escapeHtml(post.category)}</span>
                <h3>${escapeHtml(post.title)}</h3>
              </div>
              <button class="secondary-button open-post" type="button" data-id="${post.id}">View</button>
            </div>
            <p>${escapeHtml(post.description)}</p>
            <div class="detail-author-row">
              <div class="card-meta">
                <div class="avatar small">${escapeHtml(post.avatar || getInitials(post.authorName))}</div>
                <div>
                  <strong>${escapeHtml(post.authorName)}</strong>
                  <span>${escapeHtml(post.authorRole)}</span>
                </div>
              </div>
              <div class="stat-line">
                <span>♥ ${formatCount(post.likes)}</span>
                <span>↺ ${post.reposts}</span>
                <span>⌁ ${post.saves}</span>
              </div>
            </div>
            <div class="action-row">
              <button class="action-pill ${isLiked(post.id) ? "is-active" : ""}" data-action="like" data-id="${post.id}" type="button">
                Like
              </button>
              <button class="action-pill ${isBookmarked(post.id) ? "is-active" : ""}" data-action="save" data-id="${post.id}" type="button">
                Save
              </button>
              <button class="action-pill" data-action="repost" data-id="${post.id}" type="button">
                Repost
              </button>
              <button class="action-pill" data-action="comment" data-id="${post.id}" type="button">
                Comment
              </button>
            </div>
          </div>
        </article>
      `
    )
    .join("");

  communityList.querySelectorAll(".open-post").forEach((button) => {
    button.addEventListener("click", () => openDetailView(Number(button.dataset.id)));
  });

  communityList.querySelectorAll(".action-pill").forEach((button) => {
    button.addEventListener("click", (event) => {
      event.stopPropagation();
      handlePostAction(button.dataset.action, Number(button.dataset.id));
    });
  });
}

function renderExplore(posts) {
  exploreGrid.innerHTML = posts
    .slice()
    .sort((a, b) => b.likes + b.saves - (a.likes + a.saves))
    .map(
      (post) => `
        <article class="explore-card">
          <div class="explore-cover ${post.coverType} ${post.gallery[0].type === "image" ? "image-cover" : ""}" ${backgroundForAsset(post.gallery[0])}></div>
          <div class="explore-body">
            <span class="micro-label">${escapeHtml(post.location)}</span>
            <h3>${escapeHtml(post.title)}</h3>
            <p>${escapeHtml(post.description.slice(0, 90))}...</p>
            <div class="bookmark-meta">
              <span>♥ ${formatCount(post.likes)}</span>
              <button class="text-button open-explore" type="button" data-id="${post.id}">Open</button>
            </div>
          </div>
        </article>
      `
    )
    .join("");

  exploreGrid.querySelectorAll(".open-explore").forEach((button) => {
    button.addEventListener("click", () => openDetailView(Number(button.dataset.id)));
  });
}

function renderBookmarks() {
  const bookmarked = state.posts.filter((post) => isBookmarked(post.id));

  bookmarkGrid.innerHTML = bookmarked.length
    ? bookmarked
        .map(
          (post) => `
            <article class="bookmark-card">
              <div class="bookmark-cover ${post.coverType} ${post.gallery[0].type === "image" ? "image-cover" : ""}" ${backgroundForAsset(post.gallery[0])}></div>
              <div class="bookmark-body">
                <h3>${escapeHtml(post.title)}</h3>
                <p>${escapeHtml(post.location)} · ${escapeHtml(post.price)}</p>
                <div class="bookmark-meta">
                  <span>${post.tags.slice(0, 2).map(escapeHtml).join(" ")}</span>
                  <button class="text-button open-bookmark" data-id="${post.id}" type="button">View</button>
                </div>
              </div>
            </article>
          `
        )
        .join("")
    : `<article class="stat-card"><span class="micro-label">Empty</span><h3>0</h3><p>You have not bookmarked any curated finds yet.</p></article>`;

  bookmarkGrid.querySelectorAll(".open-bookmark").forEach((button) => {
    button.addEventListener("click", () => openDetailView(Number(button.dataset.id)));
  });
}

function renderAnalytics(posts) {
  const totalLikes = posts.reduce((sum, post) => sum + post.likes, 0);
  const totalComments = posts.reduce((sum, post) => sum + post.comments.length, 0);
  const totalReposts = posts.reduce((sum, post) => sum + post.reposts, 0);
  const totalViews = posts.reduce((sum, post) => sum + post.views, 0);

  analyticsGrid.innerHTML = `
    <article class="stat-card">
      <span class="micro-label">Reach</span>
      <h3>${formatCount(totalViews)}</h3>
      <p>Total views across all published and reposted collections.</p>
    </article>
    <article class="stat-card">
      <span class="micro-label">Likes</span>
      <h3>${formatCount(totalLikes)}</h3>
      <p>Signals of appreciation collected by your visible content.</p>
    </article>
    <article class="stat-card">
      <span class="micro-label">Comments</span>
      <h3>${formatCount(totalComments)}</h3>
      <p>Conversation depth around your curation and uploads.</p>
    </article>
    <article class="stat-card">
      <span class="micro-label">Reposts</span>
      <h3>${formatCount(totalReposts)}</h3>
      <p>How often the network is redistributing your discoveries.</p>
    </article>
  `;
}

function renderTrending() {
  const ranked = [...state.posts]
    .sort((a, b) => trendScore(b, state.currentTrend) - trendScore(a, state.currentTrend))
    .slice(0, 3);

  trendingList.innerHTML = ranked
    .map(
      (post, index) => `
        <article class="trend-item">
          <div class="trend-index">0${index + 1}</div>
          <div class="trend-copy">
            <strong>${escapeHtml(post.title)}</strong>
            <span>${escapeHtml(post.category)} · ${formatCount(trendScore(post, state.currentTrend))} curated</span>
          </div>
        </article>
      `
    )
    .join("");
}

function openDetailView(postId) {
  const post = findPost(postId);
  if (!post) return;

  state.currentDetailId = postId;
  state.currentDetailImageIndex = 0;
  commentDisplayCount = 3;

  appScreen.classList.add("hidden");
  detailScreen.classList.remove("hidden");

  renderDetail(post);
}

function closeDetailView() {
  detailScreen.classList.add("hidden");
  appScreen.classList.remove("hidden");
  state.currentDetailId = null;
}

function renderDetail(post) {
  renderDetailHero(post);
  renderDetailInfo(post);
  renderComments(post);
}

function renderDetailHero(post) {
  const currentAsset = post.gallery[state.currentDetailImageIndex] || post.gallery[0];
  detailHero.className = "detail-hero";
  detailHero.removeAttribute("style");

  if (currentAsset.type === "image") {
    detailHero.classList.add("image-cover");
    detailHero.style.backgroundImage = `url("${currentAsset.value}")`;
  } else {
    detailHero.classList.add(currentAsset.value);
  }

  thumbnailRow.innerHTML = post.gallery
    .map((asset, index) => {
      const imageClass = asset.type === "image" ? "image-cover" : asset.value;
      const style = asset.type === "image" ? `style="background-image:url('${escapeAttribute(asset.value)}')"` : "";
      return `<button class="thumb ${imageClass} ${index === state.currentDetailImageIndex ? "is-active" : ""}" data-index="${index}" ${style} type="button"></button>`;
    })
    .join("");

  thumbnailRow.querySelectorAll(".thumb").forEach((button) => {
    button.addEventListener("click", () => {
      state.currentDetailImageIndex = Number(button.dataset.index);
      renderDetailHero(post);
    });
  });
}

function renderDetailInfo(post) {
  const canDelete =
    post.origin === "user" ||
    post.origin === "repost" ||
    post.authorName === state.user.name;

  detailInfoCard.innerHTML = `
    <div class="interaction-row">
      <div>
        <span class="micro-label">${escapeHtml(post.category)}</span>
        <span class="micro-label">Private</span>
      </div>
      <div class="interaction-row">
        <button class="icon-button detail-like ${isLiked(post.id) ? "active" : ""}" data-id="${post.id}" type="button">♥</button>
        <button class="icon-button detail-save ${isBookmarked(post.id) ? "active" : ""}" data-id="${post.id}" type="button">⌑</button>
      </div>
    </div>

    <h2>${escapeHtml(post.title)}</h2>

    <div class="detail-author-row">
      <div class="card-meta">
        <div class="avatar small">${escapeHtml(post.avatar || getInitials(post.authorName))}</div>
        <div>
          <strong>${escapeHtml(post.authorName)}</strong>
          <span>${escapeHtml(post.authorRole)}</span>
        </div>
      </div>
      <button class="follow-button" type="button">Follow</button>
    </div>

    <div class="detail-meta-grid">
      <article>
        <small>Price</small>
        <strong>${escapeHtml(post.price || "N/A")}</strong>
      </article>
      <article>
        <small>Location</small>
        <strong>${escapeHtml(post.location)}</strong>
      </article>
    </div>

    <small>Associated tags</small>
    <div class="tag-row">
      ${post.tags.map((tag) => `<span class="tag-pill">${escapeHtml(tag)}</span>`).join("")}
    </div>

    <p>${escapeHtml(post.description)}</p>

    <div class="detail-bottom-row">
      <span>${formatCount(post.likes)} likes</span>
      <span>${post.saves} saves</span>
      <span>${post.reposts} reposts</span>
      <button class="detail-link-action danger" data-detail-action="report" data-id="${post.id}" type="button">Report</button>
      <button class="detail-link-action" data-detail-action="delete" data-id="${post.id}" type="button" ${canDelete ? "" : "disabled"}>
        Delete
      </button>
    </div>

    <div class="action-row">
      <button class="action-pill ${isLiked(post.id) ? "is-active" : ""}" data-action="like" data-id="${post.id}" type="button">Like</button>
      <button class="action-pill ${isBookmarked(post.id) ? "is-active" : ""}" data-action="save" data-id="${post.id}" type="button">Save</button>
      <button class="action-pill" data-action="repost" data-id="${post.id}" type="button">Repost</button>
    </div>
  `;

  detailInfoCard.querySelectorAll("[data-action]").forEach((button) => {
    button.addEventListener("click", () => {
      handlePostAction(button.dataset.action, Number(button.dataset.id));
      renderDetail(findPost(Number(button.dataset.id)));
    });
  });

  detailInfoCard.querySelector(".detail-like").addEventListener("click", () => {
    handlePostAction("like", post.id);
    renderDetail(findPost(post.id));
  });

  detailInfoCard.querySelector(".detail-save").addEventListener("click", () => {
    handlePostAction("save", post.id);
    renderDetail(findPost(post.id));
  });

  detailInfoCard.querySelectorAll("[data-detail-action]").forEach((button) => {
    button.addEventListener("click", () => {
      const action = button.dataset.detailAction;
      if (action === "report") {
        showToast("Post reported for review.");
      }
      if (action === "delete") {
        if (!canDelete) {
          showToast("Only your own uploaded or reposted content can be deleted.");
          return;
        }
        deletePost(post.id);
      }
    });
  });
}

function renderComments(post) {
  const commentsToShow = post.comments.slice(0, commentDisplayCount);
  commentCount.textContent = post.comments.length;
  commentList.innerHTML = commentsToShow
    .map(
      (comment) => `
        <article class="comment-item">
          <div class="avatar small">${escapeHtml(comment.avatar || getInitials(comment.user))}</div>
          <div>
            <strong>${escapeHtml(comment.user)}</strong>
            <div class="comment-meta">${escapeHtml(comment.time)}</div>
            <p>${escapeHtml(comment.text)}</p>
            <div class="comment-actions">
              <span>♥ ${Math.max(2, comment.text.length % 27)}</span>
              <span>Reply</span>
            </div>
          </div>
        </article>
      `
    )
    .join("");

  loadMoreComments.classList.toggle("hidden", commentDisplayCount >= post.comments.length);
}

function handleSignup(event) {
  event.preventDefault();
  const user = {
    name: document.querySelector("#signup-name").value.trim(),
    email: document.querySelector("#signup-email").value.trim(),
    username: document.querySelector("#signup-username").value.trim(),
    role: "Curator Gold",
    bio: "Building a curated library of unique finds worth saving, sharing, and discussing.",
  };

  state.user = user;
  persistState();
  showToast("Account created. Welcome to The Curator.");
  renderApp();
}

function handleDemoLogin() {
  state.user = {
    name: "Alex Rivera",
    email: "alex@curator.gold",
    username: "alexrivera",
    role: "Curator Gold",
    bio: "Curating premium interiors, digital artifacts, and spatial references worth revisiting.",
  };
  persistState();
  showToast("Logged in with the demo curator account.");
  renderApp();
}

function togglePasswordVisibility() {
  const show = signupPassword.type === "password";
  signupPassword.type = show ? "text" : "password";
  togglePassword.textContent = show ? "Hide" : "Show";
}

function saveSettings(event) {
  event.preventDefault();
  state.user = {
    ...state.user,
    name: settingsName.value.trim(),
    username: settingsUsername.value.trim(),
    email: settingsEmail.value.trim(),
    bio: settingsBio.value.trim(),
  };
  persistState();
  applyUserToUI();
  showToast("Profile updated.");
}

function logout() {
  localStorage.removeItem(STORAGE_KEYS.user);
  state.user = null;
  renderApp();
}

function openCreatePostModal() {
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
  pendingUploads = await Promise.all(files.map(fileToDataUrl));
  uploadPreview.innerHTML = pendingUploads
    .map((src) => `<div class="preview-item" style="background-image:url('${escapeAttribute(src)}')"></div>`)
    .join("");
}

function handleCreatePost(event) {
  event.preventDefault();

  const title = document.querySelector("#post-title").value.trim();
  const category = document.querySelector("#post-category").value.trim();
  const price = document.querySelector("#post-price").value.trim() || "Curated";
  const location = document.querySelector("#post-location").value.trim();
  const description = document.querySelector("#post-description").value.trim();
  const tags = document
    .querySelector("#post-tags")
    .value.split(",")
    .map((tag) => tag.trim())
    .filter(Boolean);

  const gallery =
    pendingUploads.length > 0
      ? pendingUploads.map((src) => ({ type: "image", value: src }))
      : [{ type: "art", value: randomArt() }];

  const post = {
    id: Date.now(),
    title,
    description,
    category,
    price,
    location,
    tags: tags.length ? tags : ["#curated", "#newpost"],
    authorName: state.user.name,
    authorRole: state.user.role || "Curator",
    avatar: getInitials(state.user.name),
    likes: 0,
    comments: [],
    saves: 0,
    reposts: 0,
    views: 0,
    coverType: gallery[0].type === "art" ? gallery[0].value : randomArt(),
    gallery,
    createdAt: Date.now(),
    origin: "user",
  };

  state.posts.unshift(post);
  persistState();
  closeCreatePostModal();
  renderAppViews();
  switchView("feed");
  showToast("Post published and added to your curated feed.");
}

function handleCommentSubmit(event) {
  event.preventDefault();
  if (!state.currentDetailId) return;
  const value = commentInput.value.trim();
  if (!value) return;

  const post = findPost(state.currentDetailId);
  post.comments.unshift({
    user: state.user.name,
    avatar: getInitials(state.user.name),
    text: value,
    time: "Just now",
  });
  persistState();
  commentInput.value = "";
  commentDisplayCount += 1;
  renderComments(post);
  renderCommunityList(filterPosts(globalSearch.value));
  showToast("Comment posted.");
}

function showMoreComments() {
  if (!state.currentDetailId) return;
  commentDisplayCount += 3;
  renderComments(findPost(state.currentDetailId));
}

function handlePostAction(action, postId) {
  const post = findPost(postId);
  if (!post) return;

  if (action === "like") {
    toggleLike(post);
  }
  if (action === "save") {
    toggleBookmark(post.id);
  }
  if (action === "repost") {
    repostPost(post);
  }
  if (action === "comment") {
    openDetailView(post.id);
    commentInput.focus();
    return;
  }

  persistState();
  renderAppViews();
}

function deletePost(postId) {
  state.posts = state.posts.filter((post) => post.id !== postId);
  state.bookmarks = state.bookmarks.filter((id) => id !== postId);
  state.likes = state.likes.filter((id) => id !== postId);
  persistState();
  closeDetailView();
  renderAppViews();
  showToast("Post deleted from your curator space.");
}

function toggleLike(post) {
  if (isLiked(post.id)) {
    state.likes = state.likes.filter((id) => id !== post.id);
    post.likes = Math.max(0, post.likes - 1);
    showToast("Like removed.");
  } else {
    state.likes.push(post.id);
    post.likes += 1;
    showToast("Post liked.");
  }
}

function toggleBookmark(postId) {
  if (isBookmarked(postId)) {
    state.bookmarks = state.bookmarks.filter((id) => id !== postId);
    const post = findPost(postId);
    post.saves = Math.max(0, post.saves - 1);
    showToast("Removed from bookmarks.");
  } else {
    state.bookmarks.push(postId);
    findPost(postId).saves += 1;
    showToast("Saved to bookmarks.");
  }
}

function repostPost(post) {
  const repost = {
    ...structuredClone(post),
    id: Date.now(),
    authorName: state.user.name,
    authorRole: `${state.user.role || "Curator"} · Reposted`,
    avatar: getInitials(state.user.name),
    title: `Reposted: ${post.title}`,
    description: `Re-shared from ${post.authorName}. ${post.description}`,
    likes: 0,
    comments: [],
    saves: 0,
    reposts: 0,
    views: 0,
    createdAt: Date.now(),
    origin: "repost",
  };

  post.reposts += 1;
  state.posts.unshift(repost);
  showToast("Post reposted to your curated stream.");
}

function filterPosts(query = "") {
  const normalized = query.trim().toLowerCase();
  if (!normalized) return [...state.posts];

  return state.posts.filter((post) =>
    [
      post.title,
      post.description,
      post.category,
      post.location,
      post.authorName,
      ...post.tags,
    ]
      .join(" ")
      .toLowerCase()
      .includes(normalized)
  );
}

function persistState() {
  localStorage.setItem(STORAGE_KEYS.user, JSON.stringify(state.user));
  localStorage.setItem(STORAGE_KEYS.posts, JSON.stringify(state.posts));
  localStorage.setItem(STORAGE_KEYS.bookmarks, JSON.stringify(state.bookmarks));
  localStorage.setItem(STORAGE_KEYS.likes, JSON.stringify(state.likes));
}

function readStorage(key) {
  try {
    const raw = localStorage.getItem(key);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

function findPost(postId) {
  return state.posts.find((post) => post.id === postId);
}

function getInitials(name = "") {
  return name
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0].toUpperCase())
    .join("");
}

function firstName(name = "") {
  return name.split(" ")[0] || name;
}

function formatCount(value) {
  if (value >= 1000) return `${(value / 1000).toFixed(1)}k`;
  return String(value);
}

function trendScore(post, window) {
  const base = post.likes + post.saves * 2 + post.reposts * 3 + post.comments.length * 2;
  if (window === "daily") return Math.round(base * 0.36);
  if (window === "weekly") return Math.round(base * 0.72);
  return base;
}

function isBookmarked(postId) {
  return state.bookmarks.includes(postId);
}

function isLiked(postId) {
  return state.likes.includes(postId);
}

function randomArt() {
  return artLibrary[Math.floor(Math.random() * artLibrary.length)];
}

function backgroundForAsset(asset) {
  if (!asset || asset.type !== "image") return "";
  return `style="background-image:url('${escapeAttribute(asset.value)}')"`;
}

function fileToDataUrl(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(reader.result);
    reader.onerror = reject;
    reader.readAsDataURL(file);
  });
}

function showToast(message) {
  toast.textContent = message;
  toast.classList.remove("hidden");
  clearTimeout(showToast.timer);
  showToast.timer = setTimeout(() => toast.classList.add("hidden"), 2200);
}

function escapeHtml(value = "") {
  return value
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}

function escapeAttribute(value = "") {
  return String(value).replaceAll('"', "&quot;").replaceAll("'", "&#39;");
}
