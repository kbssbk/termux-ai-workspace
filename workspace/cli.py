import argparse,json
from .config import load_projects,get_project,public_project
from .health import get_system_status
from .actions import execute_action
from .server import serve,CONFIG

def main(argv=None):
    p=argparse.ArgumentParser(prog="ai-workspace"); sub=p.add_subparsers(dest="cmd",required=True)
    s=sub.add_parser("serve"); s.add_argument("--port",type=int,default=8765)
    sub.add_parser("status"); sub.add_parser("projects")
    a=sub.add_parser("action"); a.add_argument("project"); a.add_argument("action")
    ns=p.parse_args(argv)
    if ns.cmd=="serve": return serve(ns.port)
    if ns.cmd=="status": data=get_system_status()
    elif ns.cmd=="projects": data=[public_project(x) for x in load_projects(CONFIG)]
    else:
        pr=get_project(load_projects(CONFIG),ns.project); data=execute_action(pr,ns.action) if pr else {"ok":False,"error":"project_not_found"}
    print(json.dumps(data,ensure_ascii=False,indent=2)); return 0
