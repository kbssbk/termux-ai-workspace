import json
from urllib.request import Request, urlopen

HOSTAI_URL = "http://127.0.0.1:8080/v1/chat/completions"
MODEL = "gemma-4-E4B-it.litertlm"
MAX_LOG_CHARS = 12000


def _prompt(action, result):
    stdout = str(result.get("stdout", ""))[-MAX_LOG_CHARS:]
    stderr = str(result.get("stderr", ""))[-MAX_LOG_CHARS:]
    return (
        "You are a local development failure analyzer. "
        "Do not execute commands. Diagnose only from the supplied log. "
        "Return JSON only with exactly these keys: "
        '"cause" (short string), "action" (safe recommended next step), '
        '"retry" (boolean).\n'
        f"Failed step: {action}\nSTDOUT:\n{stdout}\nSTDERR:\n{stderr}"
    )


def analyze_failure(action, result, timeout=45):
    payload = {
        "model": MODEL,
        "messages": [
            {"role": "system", "content": "Analyze local build/test failures. Return valid JSON only."},
            {"role": "user", "content": _prompt(action, result)},
        ],
        "temperature": 0,
    }
    request = Request(
        HOSTAI_URL,
        data=json.dumps(payload).encode("utf-8"),
        headers={"Content-Type": "application/json"},
        method="POST",
    )
    try:
        with urlopen(request, timeout=timeout) as response:
            outer = json.loads(response.read().decode("utf-8"))
        content = outer["choices"][0]["message"]["content"]
        analysis = json.loads(content)
        if set(analysis) != {"cause", "action", "retry"} or not isinstance(analysis["retry"], bool):
            raise ValueError("invalid diagnostic schema")
        return {"ok": True, "status": "done", "analysis": analysis}
    except (OSError, ValueError, TypeError, KeyError, IndexError, json.JSONDecodeError):
        return {"ok": False, "status": "unavailable", "message": "Local Gemma diagnosis is unavailable"}
