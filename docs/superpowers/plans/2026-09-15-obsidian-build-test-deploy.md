# Obsidian Build → Test → Deploy Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a fast, transparent, fail-closed path from Obsidian plugin source to verified installation in the Android Vault.

**Architecture:** `Obsidian-for-Ai` owns the plugin/build artifacts; `termux-ai-workspace` orchestrates and verifies them. A local fingerprint marker gates deploy so only the exact artifacts that passed Test can be installed.

**Tech Stack:** TypeScript, Obsidian API, esbuild, Node/npm, Python 3 stdlib, unittest, localhost Web UI.

**Spec:** `docs/superpowers/specs/2026-09-15-obsidian-build-test-deploy-design.md`

## Global Constraints
- Principle 1: Speed.
- Principle 2: Transparency.
- Deployable artifact allowlist is exactly `main.js`, `manifest.json`, `styles.css`.
- No recursive deletion of Vault/plugin contents.
- Never expose the real Vault name in API/UI logs.
- Pull/Build/failed Test invalidate deploy verification.
- Real-device verification is separate from automated test success.

---

### Task 1: Minimal Obsidian plugin build
**Repository:** `Obsidian-for-Ai`
**Files:** Create `src/main.ts`, `manifest.json`, `styles.css`, `package.json`, `tsconfig.json`, `esbuild.config.mjs`, tests/build checks.
**Produces:** `npm run build`, `npm test`, and the three-file artifact contract.

- [ ] Write a failing scaffold/build-contract test first.
- [ ] Run it and observe RED on Termux or an equivalent Node environment.
- [ ] Add the minimal plugin scaffold and esbuild configuration.
- [ ] Run install/build/test and observe GREEN.
- [ ] Verify `main.js`, `manifest.json`, `styles.css` are non-empty.
- [ ] Commit the independently working plugin scaffold.

### Task 2: Workspace verification state and artifact validator
**Repository:** `termux-ai-workspace`
**Files:** Modify `workspace/actions.py`; add focused tests in `test_actions.py` or a new pipeline test module.
**Produces:** artifact validation/fingerprinting and a local deploy-verification marker.

- [ ] Write failing tests for artifact allowlist, missing/empty artifacts, fingerprint creation, and invalidation.
- [ ] Run tests and observe RED.
- [ ] Implement minimal validator and marker logic using Python stdlib hashing/JSON.
- [ ] Run tests and observe GREEN.
- [ ] Commit.

### Task 3: Real Build and Test actions
**Repository:** `termux-ai-workspace`
**Files:** Modify `workspace/actions.py`, project config if needed, and tests.
**Consumes:** Task 1 npm scripts; Task 2 marker helpers.
**Produces:** deterministic Build/Test actions with timeouts and transparent results.

- [ ] Write failing tests for build command, test command, timeout/failure, and marker invalidation/generation.
- [ ] Run RED.
- [ ] Implement Build and Test minimally.
- [ ] Preserve existing Pull behavior as a regression test.
- [ ] Run GREEN.
- [ ] Commit.

### Task 4: Fail-closed Deploy
**Repository:** `termux-ai-workspace`
**Files:** Modify `workspace/actions.py`, config as needed, and tests.
**Produces:** deployment of only verified artifacts through the existing Vault alias.

- [ ] Write failing tests: no marker → blocked; fingerprint mismatch → blocked; missing Vault alias → blocked; valid state → copies only three allowlisted files.
- [ ] Run RED.
- [ ] Implement non-destructive staged copy/replace without recursive deletion.
- [ ] Ensure returned/logged data uses a generic Vault target label rather than real path/name.
- [ ] Run GREEN.
- [ ] Commit.

### Task 5: Dashboard readiness and transparent progress
**Repository:** `termux-ai-workspace`
**Files:** Modify `web/app.js`, `web/i18n.js`, possibly `web/styles.css`; update frontend tests.
**Produces:** Build/Test/Deploy messages that clearly show readiness and action stage without raw JSON-first UX.

- [ ] Write failing frontend assertions for readiness/progress copy in Korean and English.
- [ ] Run RED.
- [ ] Implement minimal UI changes while preserving STEP 01–04 layout.
- [ ] Run GREEN.
- [ ] Commit.

### Task 6: Integrated verification
**Repositories:** both.

- [ ] Run all automated tests with visible output.
- [ ] On Termux, verify Node/npm explicitly with visible `OK/FAIL` before dependency install.
- [ ] Pull both feature branches with numbered progress output and timeouts.
- [ ] Run STEP 02 Build and confirm artifacts.
- [ ] Run STEP 03 Test and confirm deploy becomes verified.
- [ ] Run STEP 04 Deploy and verify only the expected plugin files exist/changed at the target.
- [ ] Open/reload Obsidian and verify the minimal plugin loads.
- [ ] Record final status explicitly as implemented / automated-tests-passed / real-device-verified.
