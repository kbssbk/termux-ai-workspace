# Sentence Library V3 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 기존 앱과 충돌 없이 설치되는 장기 사용형 네이티브 Android 독서 기록 앱 V3를 만들고, 로컬 원본 데이터·자유로운 설정·복구·JW.ORG 목차 동기화·A4 PDF/DOCX·CSV/JSON 내보내기까지 한 APK에 제공한다.

**Architecture:** 새 `sentence-library-v3/` Android 프로젝트를 기존 V2와 병행해 만들고, Kotlin + Jetpack Compose UI와 Room(SQLite)을 로컬 원본으로 사용한다. 공식 목차 데이터, 개인 독서 기록, 설정/복구 이력을 분리하며 V2 JSON은 명시적 가져오기 경로로 변환한다. 문서 내보내기는 앱 화면 캡처가 아니라 저장 데이터를 별도 A4 렌더러로 생성한다.

**Tech Stack:** Kotlin, Jetpack Compose, Room, kotlinx.serialization, OkHttp, Jsoup, Android `PdfDocument`, `ZipOutputStream` 기반 최소 OOXML DOCX writer, JUnit, GitHub Actions, Java 17.

**Spec:** `docs/superpowers/specs/2026-09-26-sentence-library-v3-design.md` + `docs/superpowers/specs/2026-09-26-sentence-library-v3-install-identity-amendment.md`

## Global Constraints

- Android `applicationId`와 namespace는 영구적으로 `com.beomsoo.sentencelibrary`를 사용한다.
- 앱 표시 이름은 `문장 라이브러리`, 최초 버전은 `versionName=3.0.0`, `versionCode=30000`이다.
- `minSdk=23`, `compileSdk=35`, `targetSdk=35`, Java/Kotlin JVM target 17을 사용한다.
- Room(SQLite)이 원본 데이터 저장소이며 네트워크가 없어도 핵심 기록/검색/대시보드/내보내기가 동작해야 한다.
- 공식 자료 네트워크 입력은 `jw.org`, `wol.jw.org`, 공식 JW CDN에서 파생된 주소만 허용한다.
- 공식 목차 새로고침은 개인 진행률·문장·메모·요약·느낀점·적용점을 덮어쓰면 안 된다.
- 기본 상태는 `미완=0%`, `진행중=25%(1~99 조절)`, `완성=100%`이며 사용자가 상태 정의를 추가/수정/정렬할 수 있다.
- 기본 태그는 `여호와, 예수, 성령, 관계, 도움, 발전`이며 사용자가 추가/수정/정렬할 수 있다.
- 기사 기록은 문장, 태그, 위치, 개인 메모, 기사 요약, 느낀점, 적용점을 지원한다.
- 전체/기사 단위 내보내기는 PDF(A4), DOCX, CSV, JSON을 지원한다.
- 개인 서명키와 비밀번호는 공개 저장소에 커밋하지 않는다.
- Canva 디자인 `DAHWQc2ntZU`는 UI/UX 시각 참고 자료이며 앱 데이터 의존성으로 사용하지 않는다.

## Review Focus

1. 기존 `com.beomsoo.sentenceapp` 앱이 설치된 기기에서도 V3가 별도 앱으로 설치돼야 한다 — Task 1 manifest/build 검증에 고정 테스트를 둔다.
2. V2 JSON에 알 수 없는 필드/누락된 선택 필드가 있어도 가져오기가 가능한 한 진행되고 오류 항목을 리포트해야 한다 — Task 3 importer 테스트에 포함한다.
3. 사용자 상태/태그 정의를 바꾸거나 복구해도 기존 기사/문장 데이터가 삭제되면 안 된다 — Task 4 복구 테스트에 포함한다.
4. 공식 목차 제목/링크가 갱신돼도 개인 진행률과 기록을 보존해야 한다 — Task 8 merge 테스트에 포함한다.
5. 매우 긴 한글 문장과 여러 페이지 기사도 A4 PDF/DOCX가 잘리지 않고 다음 페이지로 넘어가야 한다 — Task 7 pagination 테스트에 포함한다.

---

### Task 1: Native project shell, unique install identity, CI

**Files:**
- Create: `sentence-library-v3/settings.gradle.kts`
- Create: `sentence-library-v3/build.gradle.kts`
- Create: `sentence-library-v3/gradle.properties`
- Create: `sentence-library-v3/app/build.gradle.kts`
- Create: `sentence-library-v3/app/src/main/AndroidManifest.xml`
- Create: `sentence-library-v3/app/src/main/java/com/beomsoo/sentencelibrary/MainActivity.kt`
- Create: `sentence-library-v3/app/src/test/java/com/beomsoo/sentencelibrary/InstallIdentityTest.kt`
- Create: `.github/workflows/sentence-library-v3.yml`

**Interfaces:**
- Produces: installable application identity `com.beomsoo.sentencelibrary` and a Compose app entry point used by all later tasks.

- [ ] **Step 1: Write the failing install identity test**

```kotlin
@Test fun v3_identity_is_stable_and_does_not_reuse_v2_package() {
    assertEquals("com.beomsoo.sentencelibrary", BuildConfig.APPLICATION_ID)
    assertNotEquals("com.beomsoo.sentenceapp", BuildConfig.APPLICATION_ID)
}
```

- [ ] **Step 2: Run the unit test and confirm it fails before the V3 module exists**

Run: `gradle -p sentence-library-v3 :app:testDebugUnitTest`
Expected: FAIL because the project/module is not yet configured.

- [ ] **Step 3: Create the minimal Android/Compose project with the exact global identity/version values**

Use Kotlin + Compose, Room/kapt dependencies prepared but no feature code yet. `MainActivity` launches a placeholder `SentenceLibraryApp()` composable.

- [ ] **Step 4: Add GitHub Actions build/test workflow and APK manifest identity check**

Run after assemble: `apkanalyzer manifest application-id app/build/outputs/apk/debug/app-debug.apk` and assert exact output `com.beomsoo.sentencelibrary`.

- [ ] **Step 5: Run tests and assemble debug APK**

Run: `gradle -p sentence-library-v3 :app:testDebugUnitTest :app:assembleDebug --no-daemon`
Expected: PASS / BUILD SUCCESSFUL.

- [ ] **Step 6: Commit**

```bash
git add sentence-library-v3 .github/workflows/sentence-library-v3.yml
git commit -m "feat: scaffold sentence library v3 native app"
```

### Task 2: Room schema, repository, progress rules

**Files:**
- Create: `sentence-library-v3/app/src/main/java/com/beomsoo/sentencelibrary/data/AppDatabase.kt`
- Create: `sentence-library-v3/app/src/main/java/com/beomsoo/sentencelibrary/data/Entities.kt`
- Create: `sentence-library-v3/app/src/main/java/com/beomsoo/sentencelibrary/data/Daos.kt`
- Create: `sentence-library-v3/app/src/main/java/com/beomsoo/sentencelibrary/data/ReadingRepository.kt`
- Create: `sentence-library-v3/app/src/main/java/com/beomsoo/sentencelibrary/domain/ProgressRules.kt`
- Test: `sentence-library-v3/app/src/test/java/com/beomsoo/sentencelibrary/domain/ProgressRulesTest.kt`

**Interfaces:**
- Produces: Room entities/DAOs for Publication, Issue, Article, Quote, Tag, QuoteTagCrossRef, StatusDefinition, CustomFieldDefinition, CustomFieldValue, AppPreference, RecoverySnapshot, SyncMetadata.
- Produces: `ProgressRules.normalize(status: StatusDefinition, requested: Int): Int` and aggregate helpers.

- [ ] **Step 1: Write failing tests for default states and issue/overall average progress**

```kotlin
@Test fun default_statuses_map_to_expected_progress() {
    assertEquals(0, defaults["미완"]!!.defaultProgress)
    assertEquals(25, defaults["진행중"]!!.defaultProgress)
    assertEquals(100, defaults["완성"]!!.defaultProgress)
}
```

- [ ] **Step 2: Run tests and verify failure**

Run: `gradle -p sentence-library-v3 :app:testDebugUnitTest --tests '*ProgressRulesTest*'`
Expected: FAIL because progress/domain types do not exist.

- [ ] **Step 3: Implement entities, DAOs, repository boundaries and default bootstrap data**

Keep official metadata fields separate from personal note/progress fields. Use foreign keys/cascade only where deleting the parent is explicitly intended.

- [ ] **Step 4: Implement progress rules and aggregate calculations**

Custom states may declare default progress, adjustable flag, completion flag and dashboard visibility; article progress remains an independent persisted integer 0..100.

- [ ] **Step 5: Run unit tests and Room schema export**

Run: `gradle -p sentence-library-v3 :app:testDebugUnitTest`
Expected: PASS and Room schema JSON generated into version-controlled schema directory.

- [ ] **Step 6: Commit**

```bash
git add sentence-library-v3/app/src sentence-library-v3/app/schemas
git commit -m "feat: add durable reading database and progress rules"
```

### Task 3: Open backup format and V2 import

**Files:**
- Create: `sentence-library-v3/app/src/main/java/com/beomsoo/sentencelibrary/backup/BackupModels.kt`
- Create: `sentence-library-v3/app/src/main/java/com/beomsoo/sentencelibrary/backup/JsonBackupService.kt`
- Create: `sentence-library-v3/app/src/main/java/com/beomsoo/sentencelibrary/backup/V2JsonImporter.kt`
- Create: `sentence-library-v3/app/src/main/java/com/beomsoo/sentencelibrary/backup/CsvExportService.kt`
- Create: `sentence-library-v3/docs/sentence-library-backup-v3.schema.json`
- Test: `sentence-library-v3/app/src/test/java/com/beomsoo/sentencelibrary/backup/V2JsonImporterTest.kt`

**Interfaces:**
- Consumes: `ReadingRepository` from Task 2.
- Produces: `V2JsonImporter.parse(json: String): ImportReport`, `JsonBackupService.export(): BackupV3`, `CsvExportService.exportZip(): ByteArray`.

- [ ] **Step 1: Write failing V2 compatibility tests using representative V2 issue/article/quote JSON**

Assertions: progress/status/tags/quotes are retained; unknown fields are ignored; missing optional note fields become null/empty; malformed individual records appear in `ImportReport.errors` without discarding valid records.

- [ ] **Step 2: Run importer test and confirm failure**

Run: `gradle -p sentence-library-v3 :app:testDebugUnitTest --tests '*V2JsonImporterTest*'`
Expected: FAIL.

- [ ] **Step 3: Implement V3 JSON schema/models and deterministic JSON export**

Backup includes schemaVersion, exportedAt, all entities, user settings and status/tag definitions; excludes Android filesystem paths and device IDs.

- [ ] **Step 4: Implement V2 JSON conversion and transactional import**

Create a pre-import recovery snapshot before mutating Room; return counts for imported/skipped/error records.

- [ ] **Step 5: Implement CSV ZIP export with documented table names**

Files: `publications.csv`, `issues.csv`, `articles.csv`, `quotes.csv`, `tags.csv`, `quote_tags.csv`, `custom_fields.csv`.

- [ ] **Step 6: Run tests and round-trip export/import fixture check**

Expected: PASS and exported JSON re-imports to equivalent logical records.

- [ ] **Step 7: Commit**

```bash
git add sentence-library-v3/app/src sentence-library-v3/docs
git commit -m "feat: add open backups and v2 migration"
```

### Task 4: Customization and recovery center

**Files:**
- Create: `sentence-library-v3/app/src/main/java/com/beomsoo/sentencelibrary/recovery/RecoveryService.kt`
- Create: `sentence-library-v3/app/src/main/java/com/beomsoo/sentencelibrary/settings/SettingsRepository.kt`
- Test: `sentence-library-v3/app/src/test/java/com/beomsoo/sentencelibrary/recovery/RecoveryServiceTest.kt`

**Interfaces:**
- Consumes: Room/repository and backup models.
- Produces: `createSnapshot(reason)`, `restoreSnapshot(id, scope)`, `integrityCheck()`, settings/status/tag CRUD APIs.

- [ ] **Step 1: Write failing tests proving status/tag changes and rollback never delete article/quote records**

Assertions compare primary entity counts and IDs before/after settings restore.

- [ ] **Step 2: Run tests and verify failure**

- [ ] **Step 3: Implement snapshot creation with default retention of 10 snapshots**

Create snapshots before bulk import, status structure changes, tag bulk changes, custom-field schema changes and official full refresh.

- [ ] **Step 4: Implement restore scopes: settings-only, selected entity group, full**

`기본값 복원` defaults to settings-only and must not erase reading records.

- [ ] **Step 5: Implement integrity checks**

Check orphan references, invalid progress bounds, duplicate official keys and missing required default definitions; provide repair suggestions/results.

- [ ] **Step 6: Run tests and commit**

```bash
git add sentence-library-v3/app/src
git commit -m "feat: add customization safety and recovery snapshots"
```

### Task 5: Compose UI/UX redesign from Canva concept

**Files:**
- Create: `sentence-library-v3/app/src/main/java/com/beomsoo/sentencelibrary/ui/SentenceLibraryApp.kt`
- Create: `sentence-library-v3/app/src/main/java/com/beomsoo/sentencelibrary/ui/theme/Theme.kt`
- Create: `sentence-library-v3/app/src/main/java/com/beomsoo/sentencelibrary/ui/home/HomeScreen.kt`
- Create: `sentence-library-v3/app/src/main/java/com/beomsoo/sentencelibrary/ui/library/PublicationsScreen.kt`
- Create: `sentence-library-v3/app/src/main/java/com/beomsoo/sentencelibrary/ui/article/ArticleScreen.kt`
- Create: `sentence-library-v3/app/src/main/java/com/beomsoo/sentencelibrary/ui/dashboard/DashboardScreen.kt`
- Create: `sentence-library-v3/app/src/main/java/com/beomsoo/sentencelibrary/ui/search/SearchScreen.kt`
- Create: corresponding ViewModels in each feature package
- Test: `sentence-library-v3/app/src/test/java/com/beomsoo/sentencelibrary/ui/DashboardViewModelTest.kt`

**Interfaces:**
- Consumes: ReadingRepository, SettingsRepository.
- Produces: four-bottom-tab navigation `홈 / 출판물 / 대시보드 / 검색` plus Article detail route.

- [ ] **Step 1: Write failing ViewModel tests for home resume item and dashboard totals/year rows**

Assertions: latest in-progress article wins `이어 읽기`; dashboard counts and average match repository fixture; newest year first.

- [ ] **Step 2: Run tests and verify failure**

- [ ] **Step 3: Implement bright, clean Compose theme and app navigation**

Use Canva `DAHWQc2ntZU` as visual reference: light surfaces, restrained navy/cobalt accents, readable Korean typography, generous touch targets. Avoid copying Canva assets into app dependencies.

- [ ] **Step 4: Implement Home and Publications flows**

Home prioritizes `이어 읽기`, quick quote, recent activity. Publications supports year/status/publication filters, sort, card/compact mode, fully tappable issue rows.

- [ ] **Step 5: Implement Article workspace**

Status chooser, 1..99 progress slider for adjustable states, large quick `문장 추가`, quote list with tags/location/note, collapsible summary/느낀점/적용점/custom fields.

- [ ] **Step 6: Implement Dashboard and Search**

Dashboard: overall counts/progress, year progress, recent activity, estimated completion. Search spans article title, quote text, tag and note.

- [ ] **Step 7: Run tests + compile and commit**

```bash
gradle -p sentence-library-v3 :app:testDebugUnitTest :app:assembleDebug
git add sentence-library-v3/app/src
git commit -m "feat: redesign reading workflow with compose"
```

### Task 6: Settings, flexible definitions, and recovery UI

**Files:**
- Create: `sentence-library-v3/app/src/main/java/com/beomsoo/sentencelibrary/ui/settings/SettingsScreen.kt`
- Create: `sentence-library-v3/app/src/main/java/com/beomsoo/sentencelibrary/ui/settings/StatusEditorScreen.kt`
- Create: `sentence-library-v3/app/src/main/java/com/beomsoo/sentencelibrary/ui/settings/TagEditorScreen.kt`
- Create: `sentence-library-v3/app/src/main/java/com/beomsoo/sentencelibrary/ui/settings/CustomFieldEditorScreen.kt`
- Create: `sentence-library-v3/app/src/main/java/com/beomsoo/sentencelibrary/ui/recovery/RecoveryScreen.kt`
- Test: `sentence-library-v3/app/src/test/java/com/beomsoo/sentencelibrary/settings/SettingsRepositoryTest.kt`

**Interfaces:**
- Consumes: SettingsRepository and RecoveryService.
- Produces: user-facing configuration for statuses/tags/custom fields/start screen/display density/sort/dashboard cards/backups and rollback controls.

- [ ] **Step 1: Write failing tests for reorder/rename/add status and tags while preserving record references**
- [ ] **Step 2: Implement settings navigation and editors with validation**
- [ ] **Step 3: Implement recovery center UI: snapshot list, undo latest, settings-only restore, integrity check, safe mode**
- [ ] **Step 4: Run tests and commit**

```bash
git add sentence-library-v3/app/src
git commit -m "feat: add flexible settings and recovery center"
```

### Task 7: A4 PDF and DOCX export

**Files:**
- Create: `sentence-library-v3/app/src/main/java/com/beomsoo/sentencelibrary/export/ExportModels.kt`
- Create: `sentence-library-v3/app/src/main/java/com/beomsoo/sentencelibrary/export/A4LayoutEngine.kt`
- Create: `sentence-library-v3/app/src/main/java/com/beomsoo/sentencelibrary/export/PdfExportService.kt`
- Create: `sentence-library-v3/app/src/main/java/com/beomsoo/sentencelibrary/export/DocxExportService.kt`
- Create: `sentence-library-v3/app/src/main/java/com/beomsoo/sentencelibrary/ui/export/ExportScreen.kt`
- Test: `sentence-library-v3/app/src/test/java/com/beomsoo/sentencelibrary/export/A4LayoutEngineTest.kt`
- Test: `sentence-library-v3/app/src/test/java/com/beomsoo/sentencelibrary/export/DocxExportServiceTest.kt`

**Interfaces:**
- Consumes: repository snapshot/query DTOs.
- Produces: article-only or full-record A4 PDF/DOCX; reuses Task 3 CSV/JSON exporters.

- [ ] **Step 1: Write failing pagination tests with long Korean paragraphs/quotes**

Assertions: no line exceeds content width, items continue on next A4 page, page count > 1 for long fixture, heading is repeated/kept with following content where possible.

- [ ] **Step 2: Implement shared A4 layout model**

Exact page target: 595x842 logical points with margins, header/footer and optional page numbers.

- [ ] **Step 3: Implement Android PdfDocument renderer**

Article export order: publication/issue/article → status/progress → summary → 느낀점 → 적용점 → quotes with location/tags/note → export date.

- [ ] **Step 4: Implement minimal standards-compliant DOCX writer with ZipOutputStream**

Generate required OOXML parts and A4 section properties; use Korean-capable font metadata without bundling font files.

- [ ] **Step 5: Implement ExportScreen and Android share/save flow**

Allow `현재 기사 / 현재 호수 / 전체 기록` scope and `PDF / DOCX / CSV / JSON` format.

- [ ] **Step 6: Run tests, unzip generated DOCX and validate required XML entries, then commit**

```bash
git add sentence-library-v3/app/src
git commit -m "feat: add portable a4 and data exports"
```

### Task 8: Native JW.ORG official metadata sync

**Files:**
- Create: `sentence-library-v3/app/src/main/java/com/beomsoo/sentencelibrary/sync/OfficialHostPolicy.kt`
- Create: `sentence-library-v3/app/src/main/java/com/beomsoo/sentencelibrary/sync/JwOrgClient.kt`
- Create: `sentence-library-v3/app/src/main/java/com/beomsoo/sentencelibrary/sync/JwOrgParser.kt`
- Create: `sentence-library-v3/app/src/main/java/com/beomsoo/sentencelibrary/sync/OfficialMergeService.kt`
- Test: `sentence-library-v3/app/src/test/java/com/beomsoo/sentencelibrary/sync/OfficialMergeServiceTest.kt`
- Test: `sentence-library-v3/app/src/test/java/com/beomsoo/sentencelibrary/sync/OfficialHostPolicyTest.kt`

**Interfaces:**
- Consumes: Room repository.
- Produces: official issue/article discovery, retries, cached sync metadata and merge without personal-data overwrite.

- [ ] **Step 1: Write failing host allowlist and merge-preservation tests**

Assertions: unrelated domains rejected; title/source URL updates accepted; existing status/progress/quotes/summary/reflection/application remain unchanged.

- [ ] **Step 2: Port official URL/parser rules from V2 tests into Kotlin fixtures**

Historical naming must be stored as the official page presents it; do not force old issues to `파수대—연구용`.

- [ ] **Step 3: Implement native network client with timeout, two-worker concurrency and bounded retry/backoff**

Persist per-issue success/failure and support failure-only retry.

- [ ] **Step 4: Add sync UI actions to Publications/Settings without blocking local usage**
- [ ] **Step 5: Add CI smoke test for a small representative official-page set; keep deterministic parser fixtures as the required gate**
- [ ] **Step 6: Run tests and commit**

```bash
git add sentence-library-v3/app/src sentence-library-v3/app/src/test
git commit -m "feat: add safe native jw official metadata sync"
```

### Task 9: Release signing, installability, end-to-end verification

**Files:**
- Create: `sentence-library-v3/keystore.properties.example`
- Create: `sentence-library-v3/scripts/create-release-keystore.sh`
- Create: `sentence-library-v3/scripts/sign-release.sh`
- Create: `sentence-library-v3/README.md`
- Modify: `sentence-library-v3/app/build.gradle.kts`
- Modify: `.github/workflows/sentence-library-v3.yml`

**Interfaces:**
- Produces: unsigned release artifact from CI, locally/user-signed final APK, user-owned keystore backup and verification commands.

- [ ] **Step 1: Add release signing configuration that reads external keystore properties only when present**

Never commit keystore bytes or passwords. CI always builds/tests debug plus unsigned release.

- [ ] **Step 2: Add keystore creation/sign scripts and README warning**

Generate one V3-only key; document that losing it prevents seamless updates for `com.beomsoo.sentencelibrary`.

- [ ] **Step 3: Run full verification matrix**

Run:
`gradle -p sentence-library-v3 :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease --no-daemon`
Expected: all tests pass, both artifacts produced.

- [ ] **Step 4: Verify APK identity, version, archive integrity and signature**

Check exact package `com.beomsoo.sentencelibrary`, version `3.0.0 (30000)`, ZIP integrity, and signing certificate after final signing.

- [ ] **Step 5: Verify migration/export fixtures end-to-end**

Import V2 fixture → edit progress/quote → create recovery snapshot → export JSON/CSV/PDF/DOCX → restore snapshot → assert logical record equality.

- [ ] **Step 6: Build final install APK with stable V3 signing key and copy final artifacts**

Deliver APK plus a separately named keystore backup for the user to archive privately; do not put the keystore in GitHub.

- [ ] **Step 7: Commit final scripts/docs**

```bash
git add sentence-library-v3 .github/workflows/sentence-library-v3.yml
git commit -m "build: prepare stable v3 release and install verification"
```

## Completion Gate

Before claiming completion:

- GitHub Actions for V3 must be green.
- Unit/parser/backup/export/recovery tests must be green.
- Final APK package must be `com.beomsoo.sentencelibrary`, not the V2 package.
- Final APK must be signed and ZIP/signature checks must pass.
- V2 JSON fixture import and V3 JSON/CSV/PDF/DOCX exports must be verified.
- Final APK path must exist before presenting a download link.
- Device-specific installation cannot be claimed unless actually observed on the user's device; package/signature checks are the automated installability evidence.
