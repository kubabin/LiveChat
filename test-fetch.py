import requests

API_URL = "https://mc-api.kubabin.dev/chat"



try:
    response = requests.get(API_URL, timeout=10)
    response.raise_for_status()
    data = response.json()
except requests.RequestException as e:
    print(f"Failed to fetch chat: {e}")


for msg in data.get("messages", []):
    print(f"<{msg['player']}> {msg['content']}")
