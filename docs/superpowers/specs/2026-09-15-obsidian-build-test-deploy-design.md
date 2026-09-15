# Obsidian Build → Test → Deploy Design

## Principles
1. **Speed** — shortest safe path from GitHub code to a running Obsidian plugin.
2. **Transparency** — every potentially slow operation shows its stage and outcome; implementation, automated verification, and real-device verification are reported separately.

## Goal
Connect the existing four-step AI Workspace workflow to a real Obsidian plugin delivery pipeline while keeping Markdown/Vault data safe and avoiding unnecessary AI involvement in deterministic operations.

## Architecture
Two repositories keep separate responsibilities. `Obsidian-for-Ai` owns plugin source, TypeScript/esbuild configuration, tests, and the deployable artifacts `main.js`, `manifest.json`, and `styles.css`. `termux-ai-workspace` owns orchestration: it runs the project's build/test commands, validates artifacts, and copies only allowlisted artifacts to the already-established Vault plugin alias.

The normal path remains deterministic: Pull → Build → Test → Deploy. Gemma is not on the critical path; it can later analyze failures on demand.

## Obsidian-for-Ai scaffold
Create a minimal Obsidian plugin scaffold with `src/main.ts`, `manifest.json`, `styles.css`, `package.json`, `tsconfig.json`, an esbuild build script, and focused tests. V1 of the plugin only needs to load/unload safely; product features remain separate work.

Build produces `main.js` at the repository root. `manifest.json` and `styles.css` are source-controlled deployable files. The deploy contract is exactly these three filenames.

## Workspace pipeline
`Build` checks that Node/npm and the plugin project configuration exist, then runs the configured build command with a timeout and visible stage reporting.

`Test` runs project tests and validates that all three deploy artifacts exist and are non-empty. A failed test or artifact check returns a clear failure and prevents a trusted deploy state.

`Deploy` revalidates the three artifacts immediately before copying. It targets only the established Vault symlink alias and only replaces the allowlisted plugin files. It does not delete unrelated files or expose the real Vault name in API responses/UI logs.

## State and safety
A successful Test creates a local verification marker containing artifact fingerprints. Deploy requires that marker and requires the current artifacts to match it, preventing changed/unverified output from being installed. Pull and Build invalidate the marker. A failed Test invalidates it as well.

No broad recursive delete is used. Missing project prerequisites, missing Vault alias, missing artifacts, timeout, test failure, or fingerprint mismatch fail closed.

## UX
The existing STEP 01–04 buttons remain. Each action returns a short human result plus optional details. Slow work reports named stages rather than appearing frozen. Korean and English remain supported. Deploy should visibly remain unavailable/not-ready until verification succeeds.

## Verification
Development follows TDD. Automated tests cover command selection, marker invalidation, artifact validation/fingerprints, deploy gating, allowlisted copying, and privacy-safe responses. The existing Pull behavior is a regression test. Completion is reported in three explicit levels: `implemented`, `automated tests passed`, and `Android/Termux real-device verified`.

## Non-goals
No arbitrary browser-to-shell terminal, no Gemma in routine deployment, no Community Plugins publishing, no Vault content migration, and no plugin product features in this pipeline task.
