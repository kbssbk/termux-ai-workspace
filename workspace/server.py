import json, mimetypes
from collections import deque
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import urlparse
from .config import load_projects,get_project,public_project
from .health import get_system_status
from .actions import execute_action

ROOT=Path(__file__).resolve().parent.parent
WEB=ROOT/"web"; CONFIG=ROOT/"config"/"projects.json"
ACTIVITY=deque(maxlen=30)

class Handler(BaseHTTPRequestHandler):
    def log_message(self,fmt,*args): pass
    def _json(self,obj,status=200):
        raw=json.dumps(obj,ensure_ascii=False).encode(); self.send_response(status); self.send_header("Content-Type","application/json; charset=utf-8"); self.send_header("Content-Length",str(len(raw))); self.end_headers(); self.wfile.write(raw)
    def do_GET(self):
        path=urlparse(self.path).path
        if path=="/api/status": return self._json(get_system_status())
        if path=="/api/projects": return self._json([public_project(p) for p in load_projects(CONFIG)])
        if path=="/api/activity": return self._json(list(ACTIVITY))
        if path=="/": return self._static("index.html")
        if path.startswith("/static/"): return self._static(path[len("/static/"):])
        return self._json({"error":"not_found"},404)
    def do_POST(self):
        path=urlparse(self.path).path.strip("/").split("/")
        if len(path)==5 and path[0]=="api" and path[1]=="projects" and path[3]=="actions":
            project=get_project(load_projects(CONFIG),path[2])
            if not project: return self._json({"error":"project_not_found"},404)
            result=execute_action(project,path[4]); ACTIVITY.appendleft({"project":project["name"],"action":path[4],"ok":bool(result.get("ok"))}); return self._json(result,200 if result.get("status")!="rejected" else 403)
        return self._json({"error":"not_found"},404)
    def _static(self,name):
        target=(WEB/name).resolve()
        try: target.relative_to(WEB.resolve())
        except ValueError: return self._json({"error":"not_found"},404)
        if not target.is_file(): return self._json({"error":"not_found"},404)
        raw=target.read_bytes(); ctype=mimetypes.guess_type(str(target))[0] or "application/octet-stream"; self.send_response(200); self.send_header("Content-Type",ctype); self.send_header("Content-Length",str(len(raw))); self.end_headers(); self.wfile.write(raw)

def serve(port=8765):
    server=ThreadingHTTPServer(("127.0.0.1",port),Handler); print(f"AI Workspace: http://127.0.0.1:{port}"); server.serve_forever()
