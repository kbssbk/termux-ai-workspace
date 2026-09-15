import hashlib
import json
import shutil
from pathlib import Path

from .runner import run_command

ARTIFACTS = ("main.js", "manifest.json", "styles.css")
VERIFICATION_FILE = ".ai-workspace-verified.json"


def _cwd(project):
    return str(Path(project["repo_path"]).expanduser())


def _not_ready(action, reason):
    return {"ok": False, "action": action, "status": "not_ready", "message": reason}


def verify_artifacts(root):
    root = Path(root)
    missing = [name for name in ARTIFACTS if not (root / name).is_file() or (root / name).stat().st_size == 0]
    if missing:
        return {"ok": False, "status": "not_ready", "missing": missing}
    return {"ok": True, "status": "ready", "artifacts": list(ARTIFACTS)}


def artifact_fingerprints(root):
    root = Path(root)
    result = {}
    for name in ARTIFACTS:
        path = root / name
        if not path.is_file() or path.stat().st_size == 0:
            raise FileNotFoundError(name)
        result[name] = hashlib.sha256(path.read_bytes()).hexdigest()
    return result


def write_verification(root):
    root = Path(root)
    check = verify_artifacts(root)
    if not check["ok"]:
        raise FileNotFoundError(", ".join(check["missing"]))
    marker = root / VERIFICATION_FILE
    marker.write_text(json.dumps({"artifacts": artifact_fingerprints(root)}, sort_keys=True, indent=2) + "\n")
    return marker


def invalidate_verification(root):
    marker = Path(root) / VERIFICATION_FILE
    try:
        marker.unlink()
    except FileNotFoundError:
        pass


def execute_action(project, action):
    if action not in project.get("actions", []):
        return {"ok": False, "action": action, "status": "rejected", "message": "Action is not allowed"}
    cwd = _cwd(project)
    if action == "status":
        return {**run_command(["git", "status", "--short", "--branch"], cwd=cwd), "action": action, "status": "done"}
    if action == "pull":
        invalidate_verification(cwd)
        return {**run_command(["git", "pull", "--ff-only"], cwd=cwd, timeout=60), "action": action, "status": "done"}
    if action in {"build", "test"}:
        if not shutil.which("npm") or not Path(cwd, "package.json").exists():
            return _not_ready(action, "Node/npm or package.json is not ready")
        if action == "build":
            invalidate_verification(cwd)
        result = run_command(["npm", "run", action], cwd=cwd, timeout=180)
        if not result.get("ok"):
            if action == "test":
                invalidate_verification(cwd)
            return {**result, "action": action, "status": "done"}
        check = verify_artifacts(cwd)
        if not check["ok"]:
            invalidate_verification(cwd)
            return {**check, "action": action, "message": "Required build artifacts are missing or empty"}
        if action == "test":
            write_verification(cwd)
            return {**result, "action": action, "status": "done", "verification": "ready", "artifacts": list(ARTIFACTS)}
        return {**result, "action": action, "status": "done", "artifacts": list(ARTIFACTS)}
    if action == "deploy":
        return _not_ready(action, "Deploy adapter is intentionally disabled until verified artifacts are wired to the Vault")
    if action == "open":
        return _not_ready(action, "Obsidian deep-link target is not configured")
    return {"ok": False, "action": action, "status": "rejected", "message": "Unknown action"}
