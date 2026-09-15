#!/data/data/com.termux/files/usr/bin/bash
check(){ command -v "$1" >/dev/null 2>&1 && echo "$2=OK" || echo "$2=MISSING"; }
check python PYTHON
check git GIT
check ssh SSH
check curl CURL
check jq JQ
check termux-battery-status TERMUX_API
curl --max-time 2 -fsS http://127.0.0.1:8080/health >/dev/null 2>&1 && echo "HOSTAI=OK" || echo "HOSTAI=OFFLINE"
