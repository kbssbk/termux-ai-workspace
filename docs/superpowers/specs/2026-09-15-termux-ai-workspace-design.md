# Termux AI Workspace V1 Design

## Goal
Build a reusable, mobile-first local development dashboard that runs from Termux and controls deterministic project actions while using local Gemma only when AI judgment is useful.

## Principles
- High-speed development and operation.
- UI/UX is a core feature, not decoration.
- Local-first: bind the dashboard to 127.0.0.1 by default.
- Termux is the execution engine; the browser UI is the cockpit.
- Deterministic actions first; Gemma is used for diagnosis, summarization, and later natural-language intent.
- No arbitrary browser-to-shell execution in V1.
- Reusable across projects; Obsidian-for-Ai is the first adapter.
- Korean and English UI strings are separated from application logic.

## Architecture
A Python standard-library HTTP server runs inside Termux and serves a responsive HTML/CSS/JavaScript dashboard. The backend exposes a small allowlisted JSON API for status and project actions. Project definitions describe their repository location and supported actions. Action implementations execute fixed subprocess argument arrays rather than shell strings.

The system is split into focused modules: configuration/project registry, command runner, system health probes, Git actions, Gemma/HostAI probe, Termux:API probe, HTTP API, and static UI. The same action engine remains callable from a CLI so the dashboard is never a single point of failure.

## V1 UI
The visual system uses low-saturation graphite/charcoal surfaces, restrained contrast, comfortable typography, generous spacing, large mobile touch targets, subtle motion, and muted semantic status colors. The home screen shows overall readiness, project cards, service status, recent activity, and a primary project action. A project detail screen exposes Pull, Build/Test, Deploy, and Open only when the project adapter supports them. Logs are readable in a dedicated panel rather than dominating the home screen.

The interface is mobile-first but responsive for tablet/desktop browsers. Korean and English copy are represented in an i18n dictionary with Auto/Korean/English language selection planned from the beginning.

## V1 Data and APIs
Project configuration is JSON and contains a stable id, display name, type, repository path, and enabled actions. No secrets or SSH keys are stored in project configuration.

Initial local API endpoints:
- GET /api/status: aggregate Git, SSH prerequisites, Python, Termux:API and HostAI/Gemma health without exposing private paths.
- GET /api/projects: sanitized project metadata and supported actions.
- POST /api/projects/{id}/actions/{action}: execute an allowlisted action and return structured output.
- GET /api/activity: recent in-memory/local activity summaries with sensitive paths redacted.

## First Project Adapter
Obsidian-for-Ai is registered as the first project. Initially, pull is usable immediately. Build/test/deploy actions report not-ready until the plugin repository contains the required build files and Node/npm are available. Deploy will eventually copy only validated build artifacts to the fixed Vault alias rather than discovering or exposing the Vault name.

## Gemma
HostAI at localhost is treated as an optional local AI service. V1 health checks verify availability and model loading. Normal pull/build/deploy operations do not require Gemma. Later diagnosis can send bounded/redacted logs to Gemma and require structured results.

## Security and Failure Handling
The server listens on 127.0.0.1 only. Browser requests cannot submit raw shell commands. Every action maps to backend-owned command argument arrays and has timeouts. Destructive actions are excluded from V1. API responses redact home/Vault paths and avoid returning SSH identity material. A failed build/test must prevent deployment.

## Testing
Use Python unittest so V1 needs no extra Python packages. Tests cover project configuration validation, allowlist rejection, command timeout/failure handling, path redaction, health response shape, and HTTP action routing. Frontend smoke checks validate that core controls and bilingual strings exist. Runtime verification on Termux confirms localhost binding, status probes, Git pull, HostAI health, and Termux:API health.

## Explicitly Out of Scope for V1
External network exposure, authentication/accounts, arbitrary web terminal, NPU work, autonomous code modification by Gemma, Accessibility/UIAutomator automation, and complex background Android service lifecycle management.
