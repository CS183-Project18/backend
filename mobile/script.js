// Owners: Chenqian Fu, Xuehan Wang
// Purpose: V2 mobile authentication, API integration, session handling, and upload helpers.

// Chenqian Fu: API configuration and storage constants
const API_BASE = window.CURATOR_API_BASE || "http://localhost:8080";
const STORAGE_KEYS = {
  token: "curator_token_v1",
  auth: "curator_auth_v1",
  isGuest: "curator_guest_v1",
};

// Chenqian Fu: Authentication state management
const state = {
  authMode: "register",
};

// Chenqian Fu: DOM element references for authentication
const authForm = document.querySelector("#auth-form");
const authTitle = document.querySelector("#auth-title");
const authSubtitle = document.querySelector("#auth-subtitle");
const authSubmit = document.querySelector("#auth-submit");
const authModeButtons = document.querySelectorAll("[data-auth-mode]");
const registerOnlyFields = document.querySelectorAll(".auth-register-only");
const togglePassword = document.querySelector("#toggle-password");
const passwordInput = document.querySelector("#auth-password");
const guestLogin = document.querySelector("#guest-login");
const toast = document.querySelector("#toast");

initialize();

// Chenqian Fu: Initialize authentication module
function initialize() {
  bindEvents();
  switchAuthMode("register");
}

// Chenqian Fu: Bind authentication form events
function bindEvents() {
  authForm?.addEventListener("submit", handleAuthSubmit);
  authModeButtons.forEach((button) => {
    button.addEventListener("click", () => switchAuthMode(button.dataset.authMode || "register"));
  });
  togglePassword?.addEventListener("click", togglePasswordVisibility);
  guestLogin?.addEventListener("click", handleGuestLogin);
  document.querySelectorAll("[data-oauth]").forEach((button) => {
    button.addEventListener("click", () => showToast(`${button.dataset.oauth} login is not configured for this demo.`));
  });

  // Xuehan Wang: Custom file upload button for create post form
  document.querySelector("#choose-post-images")?.addEventListener("click", () => {
    document.querySelector("#post-images")?.click();
  });
}

// Chenqian Fu: Switch between login and register mode
function switchAuthMode(mode) {
  state.authMode = mode === "login" ? "login" : "register";
  authModeButtons.forEach((button) => {
    button.classList.toggle("is-active", button.dataset.authMode === state.authMode);
  });
  registerOnlyFields.forEach((node) => node.classList.toggle("hidden", state.authMode === "login"));
  if (authTitle) authTitle.textContent = state.authMode === "login" ? "Welcome back" : "Create your account";
  if (authSubtitle) authSubtitle.textContent = state.authMode === "login" ? "Log in with your username or email." : "Enter your details to start your journey.";
  if (authSubmit) authSubmit.textContent = state.authMode === "login" ? "Log In" : "Create Account";
}

// Chenqian Fu: Handle authentication form submission
async function handleAuthSubmit(event) {
  event.preventDefault();
  try {
    const response = state.authMode === "login" ? await login() : await register();
    saveSession(response);
    window.location.href = `./dashboard.html?v=${Date.now()}#feed`;
  } catch (error) {
    showToast(error.message || "Authentication failed.");
  }
}

// Chenqian Fu: User registration function
async function register() {
  const username = document.querySelector("#auth-username")?.value.trim();
  const email = document.querySelector("#auth-email")?.value.trim();
  const password = passwordInput?.value || "";
  const fullName = document.querySelector("#auth-name")?.value.trim();
  const agree = document.querySelector("#auth-agree");
  if (!username || !email || !password) throw new Error("Please fill in username, email, and password.");
  if (agree && !agree.checked) throw new Error("Please agree to the Terms of Service first.");
  const loginResponse = await apiRequest("/api/auth/register", {
    method: "POST",
    body: { username, email, password },
  });
  saveSession(loginResponse);
  if (fullName) {
    await apiRequest("/api/users/me/profile", {
      method: "PUT",
      auth: true,
      body: { nickname: fullName },
    }).catch(() => null);
  }
  return loginResponse;
}

// Chenqian Fu: User login function
async function login() {
  const account = document.querySelector("#auth-username")?.value.trim() || document.querySelector("#auth-email")?.value.trim();
  const password = passwordInput?.value || "";
  if (!account || !password) throw new Error("Please enter account and password.");
  return apiRequest("/api/auth/login/password", {
    method: "POST",
    body: { account, password },
  });
}

// Chenqian Fu: Guest login function
function handleGuestLogin() {
  localStorage.removeItem(STORAGE_KEYS.token);
  localStorage.removeItem(STORAGE_KEYS.auth);
  localStorage.setItem(STORAGE_KEYS.isGuest, "true");
  window.location.href = `./dashboard.html?v=${Date.now()}#feed`;
}

// Chenqian Fu: Save user session to localStorage
function saveSession(loginResponse) {
  if (!loginResponse?.token) throw new Error("Login response did not include a token.");
  const auth = {
    userId: loginResponse.userId,
    username: loginResponse.username,
    role: loginResponse.role,
    email: loginResponse.email,
  };
  localStorage.removeItem(STORAGE_KEYS.isGuest);
  localStorage.setItem(STORAGE_KEYS.token, loginResponse.token);
  localStorage.setItem(STORAGE_KEYS.auth, JSON.stringify(auth));
}

// Chenqian Fu: Toggle password visibility
function togglePasswordVisibility() {
  if (!passwordInput) return;
  const shouldShow = passwordInput.type === "password";
  passwordInput.type = shouldShow ? "text" : "password";
  if (togglePassword) togglePassword.textContent = shouldShow ? "Hide" : "Show";
}

// Chenqian Fu: Generic API request helper function
async function apiRequest(path, options = {}) {
  const headers = {};
  if (options.auth) {
    const token = localStorage.getItem(STORAGE_KEYS.token);
    if (token) headers.Authorization = `Bearer ${token}`;
  }
  if (!options.formData && options.body) headers["Content-Type"] = "application/json";
  const response = await fetch(`${API_BASE}${path}`, {
    method: options.method || "GET",
    headers,
    body: options.formData || (options.body ? JSON.stringify(options.body) : undefined),
  });
  const payload = await response.json().catch(() => null);
  if (!response.ok || payload?.success === false) {
    if (response.status === 401 || response.status === 403) {
      localStorage.removeItem(STORAGE_KEYS.token);
      localStorage.removeItem(STORAGE_KEYS.auth);
    }
    throw new Error(payload?.message || `Request failed with status ${response.status}.`);
  }
  return payload?.data;
}

// Xuehan Wang: Toast notification utility
function showToast(message) {
  if (!toast) {
    console.log(message);
    return;
  }
  toast.textContent = message;
  toast.classList.remove("hidden");
  window.clearTimeout(showToast.timerId);
  showToast.timerId = window.setTimeout(() => toast.classList.add("hidden"), 2800);
}
