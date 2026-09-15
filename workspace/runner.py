import subprocess
from pathlib import Path

def redact(text):
    if text is None: return ""
    out=str(text)
    home=str(Path.home())
    if home: out=out.replace(home,"~")
    for marker in ("/storage/emulated/0","/sdcard"):
        out=out.replace(marker,"[shared-storage]")
    return out

def run_command(argv,cwd=None,timeout=30):
    if not isinstance(argv,(list,tuple)) or not argv or not all(isinstance(x,str) for x in argv):
        raise ValueError("argv must be a non-empty string list")
    try:
        p=subprocess.run(list(argv),cwd=cwd,timeout=timeout,capture_output=True,text=True,shell=False)
        return {"ok":p.returncode==0,"code":p.returncode,"stdout":redact(p.stdout),"stderr":redact(p.stderr),"timed_out":False}
    except subprocess.TimeoutExpired as e:
        return {"ok":False,"code":None,"stdout":redact(e.stdout or ""),"stderr":redact(e.stderr or ""),"timed_out":True}
    except OSError as e:
        return {"ok":False,"code":None,"stdout":"","stderr":redact(str(e)),"timed_out":False}
