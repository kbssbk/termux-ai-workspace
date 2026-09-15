import shutil
from pathlib import Path
from .runner import run_command

def _cwd(project): return str(Path(project["repo_path"]).expanduser())
def _not_ready(action,reason): return {"ok":False,"action":action,"status":"not_ready","message":reason}

def execute_action(project,action):
    if action not in project.get("actions",[]): return {"ok":False,"action":action,"status":"rejected","message":"Action is not allowed"}
    cwd=_cwd(project)
    if action=="status": return {**run_command(["git","status","--short","--branch"],cwd=cwd),"action":action,"status":"done"}
    if action=="pull": return {**run_command(["git","pull","--ff-only"],cwd=cwd,timeout=60),"action":action,"status":"done"}
    if action in {"build","test"}:
        if not shutil.which("npm") or not Path(cwd,"package.json").exists(): return _not_ready(action,"Node/npm or package.json is not ready")
        return {**run_command(["npm","run",action],cwd=cwd,timeout=180),"action":action,"status":"done"}
    if action=="deploy": return _not_ready(action,"Deploy adapter is intentionally disabled until validated build artifacts exist")
    if action=="open": return _not_ready(action,"Obsidian deep-link target is not configured")
    return {"ok":False,"action":action,"status":"rejected","message":"Unknown action"}
