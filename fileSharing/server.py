import http.server
import socketserver
import sys
PORT = int(sys.argv[1]) if len(sys.argv) > 1 else 8000
class MiniHTTPRequestHandler(http.server.SimpleHTTPRequestHandler):
    pass
with socketserver.TCPServer(("", PORT), MiniHTTPRequestHandler) as httpd:
    print(f"Serving at http://localhost:{PORT}/")
    httpd.serve_forever()