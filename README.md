# Termux AI Workspace

A local-first mobile development cockpit. Termux executes the work; the browser provides the interface.

## Start

```bash
git clone git@github.com:kbssbk/termux-ai-workspace.git
cd termux-ai-workspace
python run.py status
python run.py serve
```

Open `http://127.0.0.1:8765` on the same Android device.

## Safety

The server binds to localhost only. The browser cannot submit arbitrary shell commands. Project actions are backend-owned and allowlisted. API responses omit repository paths and redact common private storage paths.

## First project

`Obsidian-for-Ai` is registered at `~/obsidian-for-ai`. Pull/status work when that checkout exists. Build/test require Node/npm plus the project's package scripts. Deploy remains intentionally disabled until validated plugin artifacts are defined.

## Diagnostics

```bash
bash scripts/doctor.sh
python -m unittest discover -s tests -v
```
