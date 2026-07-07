const messagesEl = document.getElementById("messages");
const pageTitle = document.getElementById("page-title");

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

function renderMessages(messages) {
  clearMessages();
  messages.forEach(renderMessage);
  scrollToBottom();
}

function scrollToBottom() {
  messagesEl.scrollTop = messagesEl.scrollHeight;
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
    const response = await fetch("/api/chat");
    if (!response.ok) {
      throw new Error(`Request failed with status ${response.status}`);
    }

    const data = await response.json();
    const messages = normalizeMessages(data).slice(-20);
    if (messages.length > 0) {
      renderMessages(messages);
      scrollToBottom();
      return;
    }
  } catch (error) {
    console.warn("Unable to load live chat messages:", error);
  }

  clearMessages();
  scrollToBottom();
}

function activateView(view) {
  document.querySelectorAll(".nav-item").forEach((item) => {
    const isActive = item.dataset.view === view;
    item.setAttribute("data-selected", isActive ? "true" : "false");
  });

  document.querySelectorAll(".view-panel").forEach((panel) => {
    const isActive = panel.dataset.viewPanel === view;
    panel.hidden = !isActive;
    panel.classList.toggle("view-panel--active", isActive);
  });

  const titles = {
    map: "Map",
    chat: "Create: Assembly Line SMP",
    season: "Season 1",
  };
  pageTitle.textContent = titles[view] || "Create: Assembly Line SMP";
}

// Nav rail selection and panel switching
document.querySelectorAll(".nav-item").forEach((item) => {
  item.addEventListener("click", () => {
    activateView(item.dataset.view);
  });
});

activateView("map");
loadLatestChat();
startChatPolling();
