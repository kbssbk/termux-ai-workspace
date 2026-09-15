# Termux AI Workspace V1 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a reusable mobile-first localhost dashboard in Termux that safely runs project actions and surfaces Git, Android, and local Gemma status.

**Architecture:** A Python standard-library server owns project configuration, health probes, allowlisted subprocess execution, and JSON APIs. A dependency-free responsive web frontend consumes those APIs. The same core actions are exposed through a small CLI for recovery and automation.

**Tech Stack:** Python 3 standard library, HTML5, CSS, vanilla JavaScript, JSON, Git/SSH, Termux:API, HostAI HTTP API.

**Spec:** `docs/superpowers/specs/2026-09-15-termux-ai-workspace-design.md`

## Global Constraints
- Bind to `127.0.0.1` by default.
- No arbitrary browser-to-shell execution.
- No secrets, SSH keys, Vault names, or private paths in API output.
- Python standard library only for V1 runtime and tests.
- Korean and English UI strings must be separated from application logic.
- Obsidian-for-Ai is the first project adapter, but core modules must remain reusable.
- Failed validation/build/test must block deployment.

---

### Task 1: Core configuration and safe command runner

**Files:**
- Create: `workspace/__init__.py`
- Create: `workspace/config.py`
- Create: `workspace/runner.py`
- Create: `config/projects.json`
- Create: `tests/test_config.py`
- Create: `tests/test_runner.py`

**Interfaces:**
- Produces: `load_projects(path) -> list[dict]`, `get_project(projects, project_id) -> dict`, `run_command(argv, cwd=None, timeout=30) -> dict`, `redact(text) -> str`.

- [ ] Write tests proving malformed/duplicate project ids are rejected and the Obsidian adapter loads with only allowlisted action names.
- [ ] Run `python -m unittest tests.test_config -v` and confirm failure before implementation.
- [ ] Implement strict JSON project loading with required `id`, `name`, `type`, `repo_path`, `actions` fields and action allowlist `{status,pull,build,test,deploy,open}`.
- [ ] Write runner tests for success, nonzero exit, timeout, and redaction of home/storage paths.
- [ ] Run runner tests and confirm failure before implementation.
- [ ] Implement `subprocess.run` with argument arrays only, timeout, captured text output, no `shell=True`, and structured `{ok,code,stdout,stderr,timed_out}` result.
- [ ] Run `python -m unittest tests.test_config tests.test_runner -v` and require PASS.
- [ ] Commit with `feat: add safe workspace core`.

### Task 2: Health probes and project actions

**Files:**
- Create: `workspace/health.py`
- Create: `workspace/actions.py`
- Create: `tests/test_health.py`
- Create: `tests/test_actions.py`

**Interfaces:**
- Consumes: `run_command`, `redact`, loaded project dictionaries.
- Produces: `get_system_status() -> dict`, `execute_action(project, action) -> dict`.

- [ ] Write health tests using mocks for Git, SSH executable presence, Python, Termux:API command availability, and HostAI `/health` response.
- [ ] Run health tests and confirm failure.
- [ ] Implement bounded health probes; HostAI uses `urllib.request` against `http://127.0.0.1:8080/health` with a short timeout and returns only readiness/model-loaded state.
- [ ] Write action tests proving unknown actions are rejected, `pull` maps to `git pull --ff-only`, and unavailable build/test/deploy actions return a safe not-ready result rather than guessing commands.
- [ ] Run action tests and confirm failure.
- [ ] Implement fixed action dispatch without accepting command text from callers.
- [ ] Run `python -m unittest tests.test_health tests.test_actions -v` and require PASS.
- [ ] Commit with `feat: add health probes and project actions`.

### Task 3: Local JSON API and CLI

**Files:**
- Create: `workspace/server.py`
- Create: `workspace/cli.py`
- Create: `run.py`
- Create: `tests/test_server.py`
- Create: `tests/test_cli.py`

**Interfaces:**
- Consumes: project registry, `get_system_status`, `execute_action`.
- Produces: localhost HTTP routes `/api/status`, `/api/projects`, `/api/activity`, `/api/projects/{id}/actions/{action}` and CLI commands `serve`, `status`, `projects`, `action`.

- [ ] Write HTTP routing tests proving GET routes return JSON, unknown routes are 404, raw command routes do not exist, and POST action routing accepts only registered project/action pairs.
- [ ] Run server tests and confirm failure.
- [ ] Implement `ThreadingHTTPServer` bound by default to `127.0.0.1`, sanitized JSON responses, bounded request bodies, and an in-memory recent activity buffer.
- [ ] Write CLI tests for `status`, `projects`, and action argument validation.
- [ ] Run CLI tests and confirm failure.
- [ ] Implement argparse CLI and `run.py` entry point; `serve` accepts an optional port but host remains localhost in V1.
- [ ] Run `python -m unittest tests.test_server tests.test_cli -v` and require PASS.
- [ ] Commit with `feat: add localhost api and cli`.

### Task 4: Premium mobile dashboard

**Files:**
- Create: `web/index.html`
- Create: `web/styles.css`
- Create: `web/app.js`
- Create: `web/i18n.js`
- Create: `tests/test_frontend.py`

**Interfaces:**
- Consumes: V1 JSON API.
- Produces: responsive dashboard with system status, project cards, project actions, recent activity, logs/results panel, and Auto/Korean/English copy structure.

- [ ] Write smoke tests that assert viewport metadata, core landmark ids, all required action labels, and both `ko` and `en` translation dictionaries exist.
- [ ] Run frontend tests and confirm failure.
- [ ] Implement semantic HTML shell with home header, readiness summary, services row, projects section, activity section, bottom navigation, and accessible result sheet.
- [ ] Implement graphite/charcoal design tokens, restrained semantic states, minimum 44px touch targets, responsive tablet layout, reduced-motion support, and readable monospace result styling.
- [ ] Implement frontend API client, loading/error states, project action buttons generated only from server-supported actions, result sheet, refresh behavior, and language selection.
- [ ] Run `python -m unittest tests.test_frontend -v` and require PASS.
- [ ] Commit with `feat: add mobile workspace dashboard`.

### Task 5: Static serving, first-project integration, and verification

**Files:**
- Modify: `workspace/server.py`
- Modify: `config/projects.json`
- Create: `README.md`
- Create: `scripts/doctor.sh`
- Create: `tests/test_integration.py`

**Interfaces:**
- Produces: `python run.py serve` serving both UI and API, plus `scripts/doctor.sh` returning compact OK/MISSING diagnostics.

- [ ] Write integration tests proving `/` serves the dashboard, static paths cannot escape `web/`, project API does not expose `repo_path`, and action results are redacted.
- [ ] Run integration tests and confirm failure.
- [ ] Add safe static-file serving with explicit MIME types and path containment checks.
- [ ] Register `Obsidian-for-Ai` using `~/obsidian-for-ai` internally while ensuring that path is never returned by public API serialization.
- [ ] Add `scripts/doctor.sh` checks for Python, Git, SSH, curl, jq, Termux:API and HostAI health; output only component status.
- [ ] Write README commands for clone, `python run.py status`, `python run.py serve`, and local browser access; document that Node/npm are only needed by projects that require them.
- [ ] Run `python -m unittest discover -s tests -v` and require all tests PASS.
- [ ] Run `python -m py_compile run.py workspace/*.py` and require success.
- [ ] Commit with `feat: complete Termux AI Workspace v1`.
