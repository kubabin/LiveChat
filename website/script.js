const messagesEl = document.getElementById("messages");
const pageTitle = document.getElementById("page-title");
const themeToggle = document.getElementById("theme-toggle");
const themeIcon = document.getElementById("theme-icon");
const scrollDownButton = document.getElementById("scroll-down-button");
const chatApiUrl = window.location.hostname === "localhost" || window.location.hostname === "127.0.0.1"
  ? "/api/chat"
  : "https://api.kubabin.dev/chat";
const SHOW_SEASON_TAB = false;
let currentView = "map";

function applyTheme(theme) {
  document.documentElement.setAttribute("data-theme", theme);
  const isDark = theme === "dark";
  themeIcon.textContent = isDark ? "light_mode" : "dark_mode";
  themeToggle.setAttribute("aria-label", isDark ? "Switch to light mode" : "Switch to dark mode");
  localStorage.setItem("theme", theme);
}

function initializeTheme() {
  const savedTheme = localStorage.getItem("theme");
  const prefersDark = window.matchMedia("(prefers-color-scheme: dark)").matches;
  const initialTheme = savedTheme || (prefersDark ? "dark" : "light");
  applyTheme(initialTheme);
}

function getPlayerHeadUrl(username) {
  const safeName = encodeURIComponent(username.trim());
  return `https://mc-heads.net/avatar/${safeName}/64.png`;
}

function renderMessage({ author, text }) {
  const wrap = document.createElement("div");
  wrap.className = "message";

  const authorEl = document.createElement("p");
  authorEl.className = "message__author";
  authorEl.textContent = author;

  const row = document.createElement("div");
  row.className = "message__row";

  const avatar = document.createElement("img");
  avatar.className = "message__avatar";
  avatar.src = getPlayerHeadUrl(author);
  avatar.alt = `${author} head`;

  const bubble = document.createElement("div");
  bubble.className = "message__bubble";
  bubble.textContent = text;

  row.append(avatar, bubble);
  wrap.append(authorEl, row);
  messagesEl.appendChild(wrap);
}

function clearMessages() {
  messagesEl.innerHTML = "";
}

function shouldAutoScroll() {
  const threshold = 80;
  return messagesEl.scrollHeight - messagesEl.scrollTop - messagesEl.clientHeight <= threshold;
}

function renderMessages(messages) {
  const wasNearBottom = shouldAutoScroll();
  clearMessages();
  messages.forEach(renderMessage);
  if (wasNearBottom) {
    scrollToBottom();
  }
}

function scrollToBottom() {
  messagesEl.scrollTop = messagesEl.scrollHeight;
  updateScrollButton();
}

function updateScrollButton() {
  if (!messagesEl || !scrollDownButton) return;
  const canScroll = messagesEl.scrollHeight > messagesEl.clientHeight + 1;
  const isNearBottom = shouldAutoScroll();
  scrollDownButton.classList.toggle("scroll-down-button--visible", canScroll && !isNearBottom);
}

function handleMessagesScroll() {
  updateScrollButton();
}

function startChatPolling() {
  setInterval(() => {
    loadLatestChat();
  }, 2000);
}

function normalizeMessage(entry) {
  if (!entry || typeof entry !== "object") return null;

  const candidateText =
    entry.text ||
    entry.message ||
    entry.content ||
    entry.body ||
    entry.value ||
    "";
  const candidateAuthor =
    entry.player ||
    entry.author ||
    entry.username ||
    entry.user ||
    entry.name ||
    entry.sender ||
    "Unknown";

  const text = String(candidateText).trim();
  if (!text) return null;

  return {
    author: String(candidateAuthor),
    text,
  };
}

function normalizeMessages(payload) {
  if (Array.isArray(payload)) {
    return payload.map(normalizeMessage).filter(Boolean);
  }

  if (payload && typeof payload === "object") {
    const candidateArrays = [
      payload.messages,
      payload.chat,
      payload.data,
      payload.results,
      payload.items,
      payload.history,
    ].filter(Array.isArray);

    if (candidateArrays.length > 0) {
      return candidateArrays[0].map(normalizeMessage).filter(Boolean);
    }

    const nested = normalizeMessage(payload);
    if (nested) return [nested];
  }

  return [];
}

async function loadLatestChat() {
  try {
    const response = await fetch(chatApiUrl);
    if (!response.ok) {
      throw new Error(`Request failed with status ${response.status}`);
    }

    const data = await response.json();
    const messages = normalizeMessages(data).slice(-20);
    if (messages.length > 0) {
      renderMessages(messages);
      return;
    }
  } catch (error) {
    console.warn("Unable to load live chat messages:", error);
  }

  if (shouldAutoScroll()) {
    clearMessages();
    scrollToBottom();
  } else {
    clearMessages();
  }
}

function updateSeasonTabVisibility() {
  const seasonNavItem = document.querySelector('.nav-item[data-view="season"]');
  const seasonPanel = document.querySelector('.view-panel[data-view-panel="season"]');

  if (!seasonNavItem || !seasonPanel) return;

  const shouldShow = SHOW_SEASON_TAB;
  seasonNavItem.hidden = !shouldShow;
  seasonNavItem.disabled = !shouldShow;
  seasonNavItem.setAttribute("aria-hidden", shouldShow ? "false" : "true");
  seasonNavItem.tabIndex = shouldShow ? 0 : -1;
  seasonNavItem.style.pointerEvents = shouldShow ? "auto" : "none";
  seasonNavItem.style.display = shouldShow ? "flex" : "none";

  seasonPanel.hidden = !shouldShow;
  seasonPanel.setAttribute("aria-hidden", shouldShow ? "false" : "true");
  seasonPanel.inert = !shouldShow;
  seasonPanel.style.display = shouldShow ? "flex" : "none";
}

function activateView(view) {
  if (!SHOW_SEASON_TAB && view === "season") {
    view = "map";
  }

  currentView = view;

  document.querySelectorAll(".nav-item").forEach((item) => {
    const isActive = item.dataset.view === view;
    item.setAttribute("data-selected", isActive ? "true" : "false");
  });

  document.querySelectorAll(".view-panel").forEach((panel) => {
    const isActive = panel.dataset.viewPanel === view;
    panel.hidden = !isActive;
    panel.classList.toggle("view-panel--active", isActive);
  });

  if (view === "chat") {
    window.requestAnimationFrame(updateScrollButton);
  }

  const titles = {
    map: "Create: Assembly Line SMP",
    chat: "Create: Assembly Line SMP",
    season: "Create: Assembly Line SMP",
  };
  pageTitle.textContent = titles[view] || "Create: Assembly Line SMP";
}

themeToggle.addEventListener("click", () => {
  const nextTheme = document.documentElement.getAttribute("data-theme") === "dark" ? "light" : "dark";
  applyTheme(nextTheme);
});

scrollDownButton.addEventListener("click", () => {
  scrollToBottom();
});

messagesEl.addEventListener("scroll", handleMessagesScroll);
window.addEventListener("resize", updateScrollButton);

// Nav rail selection and panel switching
document.querySelectorAll(".nav-item").forEach((item) => {
  item.addEventListener("click", () => {
    activateView(item.dataset.view);
  });
});

initializeTheme();
updateSeasonTabVisibility();
activateView("map");
loadLatestChat();
updateScrollButton();
startChatPolling();
