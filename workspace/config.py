import json
from pathlib import Path

ALLOWED_ACTIONS={"status","pull","build","test","deploy","open"}
REQUIRED={"id","name","type","repo_path","actions"}

def load_projects(path):
    data=json.loads(Path(path).read_text(encoding="utf-8"))
    if not isinstance(data,list): raise ValueError("projects must be a list")
    seen=set()
    for p in data:
        if not isinstance(p,dict) or not REQUIRED <= p.keys(): raise ValueError("invalid project")
        if not isinstance(p["id"],str) or not p["id"] or p["id"] in seen: raise ValueError("invalid project id")
        if not isinstance(p["actions"],list) or not set(p["actions"]) <= ALLOWED_ACTIONS: raise ValueError("invalid actions")
        seen.add(p["id"])
    return data

def get_project(projects, project_id):
    return next((p for p in projects if p["id"]==project_id), None)

def public_project(p):
    return {k:p[k] for k in ("id","name","type","actions")}
