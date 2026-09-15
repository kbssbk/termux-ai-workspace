import shutil, sys, json
from urllib.request import urlopen

def _command(name): return shutil.which(name) is not None

def _hostai():
    try:
        with urlopen("http://127.0.0.1:8080/health",timeout=1.5) as r:
            data=json.loads(r.read().decode())
        return {"ready":data.get("status")=="ok","model_loaded":bool(data.get("model_loaded"))}
    except Exception:
        return {"ready":False,"model_loaded":False}

def get_system_status():
    return {"python":{"ready":True,"version":f"{sys.version_info.major}.{sys.version_info.minor}"},"git":{"ready":_command("git")},"ssh":{"ready":_command("ssh")},"termux_api":{"ready":_command("termux-battery-status")},"gemma":_hostai()}
