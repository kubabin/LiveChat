#!/usr/bin/env python3
"""
Simple HTTP server for hosting the website.
Serves the website folder on localhost:8000 and proxies /api/chat.
Supports hot-reloading by restarting itself when watched files change.
"""

import http.server
import mimetypes
import os
import socketserver
import sys
import threading
import time
import urllib.request
from pathlib import Path


website_dir = Path(__file__).resolve().parent / "website"
server_file = Path(__file__).resolve()
os.chdir(website_dir)
PORT = 8000
WATCH_INTERVAL_SECONDS = 1.0
WATCHED_PATHS = [server_file, website_dir]


class LiveChatHandler(http.server.SimpleHTTPRequestHandler):
    def do_GET(self):
        if self.path == "/api/chat":
            self.serve_chat_proxy()
            return

        self.serve_static_file()

    def serve_static_file(self):
        request_path = self.path.split("?", 1)[0]
        if request_path in {"", "/"}:
            target_path = website_dir / "index.html"
        else:
            target_path = website_dir / request_path.lstrip("/")

        if not target_path.exists() or not target_path.is_file():
            self.send_error(404, "File not found")
            return

        mime_type, _ = mimetypes.guess_type(str(target_path))
        if mime_type is None:
            mime_type = "application/octet-stream"

        self.send_response(200)
        self.send_header("Content-Type", mime_type)
        self.send_header("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0")
        self.send_header("Pragma", "no-cache")
        self.send_header("Expires", "0")
        self.send_header("Content-Length", str(target_path.stat().st_size))
        self.end_headers()

        with target_path.open("rb") as file_obj:
            self.wfile.write(file_obj.read())

    def serve_chat_proxy(self):
        try:
            req = urllib.request.Request(
                "https://api.kubabin.dev/chat",
                headers={"User-Agent": "Mozilla/5.0"},
            )
            with urllib.request.urlopen(req, timeout=15) as response:
                body = response.read()
                self.send_response(200)
                self.send_header("Content-Type", "application/json")
                self.send_header("Access-Control-Allow-Origin", "*")
                self.send_header("Content-Length", str(len(body)))
                self.end_headers()
                self.wfile.write(body)
        except (BrokenPipeError, ConnectionResetError):
            return
        except Exception as exc:
            error_body = str(exc).encode("utf-8")
            try:
                self.send_response(502)
                self.send_header("Content-Type", "text/plain; charset=utf-8")
                self.send_header("Access-Control-Allow-Origin", "*")
                self.send_header("Content-Length", str(len(error_body)))
                self.end_headers()
                self.wfile.write(error_body)
            except (BrokenPipeError, ConnectionResetError):
                return


def collect_file_states(paths):
    states = {}
    for path in paths:
        if not path.exists():
            continue

        if path.is_file():
            states[path.resolve()] = (path.stat().st_mtime_ns, path.stat().st_size)
            continue

        for root, dirs, files in os.walk(path):
            dirs[:] = [name for name in dirs if name != "__pycache__"]
            for filename in files:
                file_path = Path(root, filename).resolve()
                try:
                    states[file_path] = (file_path.stat().st_mtime_ns, file_path.stat().st_size)
                except FileNotFoundError:
                    continue

    return states


def watch_for_changes():
    previous_states = collect_file_states(WATCHED_PATHS)

    while True:
        time.sleep(WATCH_INTERVAL_SECONDS)
        current_states = collect_file_states(WATCHED_PATHS)

        if current_states != previous_states:
            print("Detected changes, restarting server...")
            os.execv(sys.executable, [sys.executable, str(server_file)] + sys.argv[1:])

        previous_states = current_states


def main():
    print(f"Serving from: {website_dir}")
    print(f"Starting server at http://localhost:{PORT}")
    print("Press Ctrl+C to stop the server")

    socketserver.TCPServer.allow_reuse_address = True

    try:
        with socketserver.TCPServer(("", PORT), LiveChatHandler) as httpd:
            watcher = threading.Thread(target=watch_for_changes, daemon=True)
            watcher.start()
            httpd.serve_forever()
    except KeyboardInterrupt:
        print("\nServer stopped.")


if __name__ == "__main__":
    main()
