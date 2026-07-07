#!/usr/bin/env python3
"""
Simple HTTP server for hosting the website.
Serves the website folder on localhost:8000 and proxies /api/chat.
"""

import http.server
import mimetypes
import os
import socketserver
import urllib.request
from pathlib import Path


website_dir = Path(__file__).parent / "website"
os.chdir(website_dir)
PORT = 8000


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


print(f"Serving from: {website_dir}")
print(f"Starting server at http://localhost:{PORT}")
print("Press Ctrl+C to stop the server")

# Fix the address already in use error
socketserver.TCPServer.allow_reuse_address = True

try:
    with socketserver.TCPServer(("", PORT), LiveChatHandler) as httpd:
        httpd.serve_forever()
except KeyboardInterrupt:
    print("\nServer stopped.")