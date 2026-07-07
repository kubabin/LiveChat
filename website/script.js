const AVATAR_URL =
  "https://api.dicebear.com/9.x/notionists/svg?seed=createview";

const seedMessages = [
  { author: "createview", text: "hello world" },
  { author: "createview", text: "hello world" },
  { author: "createview", text: "hello world" },
];

const messagesEl = document.getElementById("messages");
const composer = document.getElementById("composer");
const input = document.getElementById("composer-input");

function renderMessage({ author, text }) {
  const wrap = document.createElement("div");
  wrap.className = "message";

  const authorEl = document.createElement("p");
  authorEl.className = "message__author";
  authorEl.textContent = author;

  const row = document.createElement("div");
  row.className = "message__row";

  const avatar = document.createElement("div");
  avatar.className = "message__avatar";
  avatar.style.backgroundImage = `url("${AVATAR_URL}")`;

  const bubble = document.createElement("div");
  bubble.className = "message__bubble";
  bubble.textContent = text;

  row.append(avatar, bubble);
  wrap.append(authorEl, row);
  messagesEl.appendChild(wrap);
}

function scrollToBottom() {
  messagesEl.scrollTop = messagesEl.scrollHeight;
}

seedMessages.forEach(renderMessage);
scrollToBottom();

composer.addEventListener("submit", (e) => {
  e.preventDefault();
  const text = input.value.trim();
  if (!text) return;

  renderMessage({ author: "You", text });
  input.value = "";
  scrollToBottom();
});

// Nav rail selection (front-end only demo behavior)
document.querySelectorAll(".nav-item").forEach((item) => {
  item.addEventListener("click", () => {
    document
      .querySelectorAll(".nav-item")
      .forEach((el) => el.setAttribute("data-selected", "false"));
    item.setAttribute("data-selected", "true");
  });
});
