---
name: joey
classification: workflow
classification-reason: Quality-gated full-auto PDCA pipeline with configurable match-rate threshold and audit trail
deprecation-risk: none
effort: high
description: |
  Full-auto PDCA pipeline with user-defined quality gate. Runs plan→design→do→analyze→report
  sequentially. Iterates the analyze→fix loop until match rate reaches the target threshold.
  After report: pushes to git immediately, then builds Android debug APK.
  APK build runs AFTER push so slow builds never block the commit.
  First argument is optional target % (default 90). No user confirmations at any checkpoint.
  Triggers: /joey, joey, 자동 PDCA, 풀 파이프라인, auto pipeline, quality gate.
argument-hint: "[target%] <feature-request>"
user-invocable: true
allowed-tools:
  - Read
  - Write
  - Edit
  - Glob
  - Grep
  - Bash
  - AskUserQuestion
imports: []
next-skill: null
pdca-phase: null
task-template: "[Joey/{threshold}%] {feature}"
---

# Joey — Quality-Gated Full-Auto PDCA Pipeline

> Inspired by `/pdca-fast-track` (Daniel's Track). Runs the complete PDCA cycle
> (plan → design → do → analyze → report) without user confirmation.
>
> The key difference: **you set the quality bar**. Provide a target match-rate % as the
> first argument. The analyze→fix loop repeats automatically until the code meets that bar.

---

## Argument Syntax

```
/joey [target%] <feature-request>
```

| Position | Type | Default | Description |
|----------|------|---------|-------------|
| `[target%]` | Integer 1–100 | `90` | Required match rate before report is generated |
| `<feature-request>` | String | — | Natural-language description of what to build |

**Parsing rule**: If the first token is a pure integer (no letters), treat it as `target%`.
Everything else is the feature request.

```bash
/joey 95 로그인 화면 추가해줘        # threshold=95, request="로그인 화면 추가해줘"
/joey 100 이미지 압축 버그 수정      # threshold=100, all criteria must be met
/joey 이미지 업로드 기능             # threshold=90 (default), request="이미지 업로드 기능"
/joey 80 빠른 프로토타입 만들어줘   # threshold=80, accepts lower bar
```

---

## CRITICAL: Auto-Approval Rules

**At EVERY checkpoint during this pipeline — NEVER pause:**
- Select "권장" (Recommended) or the first option automatically
- **NEVER call AskUserQuestion under ANY circumstance** — not for missing paths, not for errors, not for clarification. The pipeline MUST complete fully unattended.
- Log the auto-selected choice inline so the user can audit decisions

**Checkpoint auto-map:**

| Checkpoint | Auto-decision |
|-----------|---------------|
| CP-1 Requirements confirmation | "[Auto-approve] 요구사항 확인됨" |
| CP-2 Clarifying questions | "[Auto-approve] 합리적 기본값 적용" |
| CP-3 Architecture selection | "[Auto-select] Option C — Pragmatic Balance" |
| CP-4 Implementation approval | "[Auto-approve] 구현 범위 확정" |
| CP-5 Gap review | "[Auto-select] 지금 모두 수정" (if below threshold) |
| Any other | "[Auto-select] 첫 번째 옵션 선택" |

---

## Pipeline Steps

### Step 0 — Argument Parsing & Precondition Check

1. **Parse arguments:**
   - If first token is a pure integer N (e.g., `95`): `threshold = N`, `request = remaining text`
   - Otherwise: `threshold = 90`, `request = full argument`
   - Clamp threshold: `threshold = max(1, min(100, threshold))`

2. **Extract feature name** — kebab-case slug from request:
   - "로그인 화면 추가해줘" → `login-screen`
   - "이미지 압축 버그 수정" → `image-compression-fix`
   - "Add push notification support" → `push-notification`

3. **Initialize session log** — write to `.bkit/runtime/joey-log.json`:
   ```json
   {
     "feature": "<featureName>",
     "threshold": <N>,
     "startedAt": "<ISO timestamp>",
     "decisions": [],
     "iterations": [],
     "finalMatchRate": null,
     "status": "running"
   }
   ```

4. **Print banner:**
   ```
   ╔══════════════════════════════════════════════════════════╗
   ║  Joey Auto-PDCA  |  Quality Gate: {threshold}%          ║
   ║  Feature: {featureName}                                 ║
   ║  Mode: Full-Auto — no confirmations                     ║
   ╚══════════════════════════════════════════════════════════╝
   ```

---

### Step 1 — PLAN Phase

1. Check if `docs/00-pm/{featureName}.prd.md` exists → read as context if found
2. Generate Plan document WITHOUT pausing at CP-1 or CP-2
3. Auto-log each bypassed checkpoint:
   ```
   [CP-1 Auto] 요구사항 확인됨 → 계속 진행
   [CP-2 Auto] 명확화 질문 생략 → 합리적 기본값 적용
   ```
4. Write Plan to `docs/01-plan/features/{featureName}.plan.md`
5. Generate Context Anchor (WHY/WHO/RISK/SUCCESS/SCOPE) and embed
6. Append to session log `decisions[]`: `{checkpoint: 1, decision: "auto-approve", reason: "joey-mode"}`

**Progress:**
```
[1/5] PLAN ✅  {featureName}.plan.md
      Threshold: {threshold}% | Decisions auto-approved: CP-1, CP-2
```

---

### Step 2 — DESIGN Phase

1. Read Plan document fully
2. Generate 3 architecture options (A: Minimal, B: Clean, C: Pragmatic Balance)
3. Auto-select **Option C** (or Option A if only 2 options exist):
   ```
   [CP-3 Auto] Option C — Pragmatic Balance 선택됨
   ```
4. Write Design to `docs/02-design/features/{featureName}.design.md`
5. Append to session log: `{checkpoint: 3, decision: "option-c", reason: "pragmatic-default"}`

**Progress:**
```
[2/5] DESIGN ✅  {featureName}.design.md
      Architecture: Option C (Pragmatic Balance) | CP-3 auto-selected
```

---

### Step 3 — DO Phase (Implementation)

1. Read Design document **in full** (do not skip sections)
2. Load full upstream chain: PRD → Plan → Design
3. Auto-approve implementation scope:
   ```
   [CP-4 Auto] 구현 범위 확정 — 즉시 시작
   ```
4. Implement ALL code changes from Design (no --scope filtering)
5. Add `// Design Ref: §{section}` comments for key decisions
6. Append to session log: `{checkpoint: 4, decision: "auto-approve", filesChanged: N}`

**Progress:**
```
[3/5] DO ✅  {N} files modified, {M} files created
      CP-4 auto-approved | Design Ref comments added
```

---

### Step 4 — ANALYZE Phase (Quality-Gated Iteration Loop)

This is the core quality gate. The loop runs until match rate ≥ `{threshold}` or max iterations reached.

#### Iteration Loop

```
iteration = 0
maxIterations = 5

LOOP:
  iteration += 1
  
  Run static gap analysis (Structural + Functional + Contract axes):
    - Structural: file existence, route coverage, component list
    - Functional: logic completeness, placeholder detection, UI checklist
    - Contract: API type consistency, callback signatures, state flow
  
  Calculate matchRate:
    Overall = (Structural × 0.2) + (Functional × 0.4) + (Contract × 0.4)
  
  Append to session log iterations[]:
    { iteration, matchRate, gaps: [...], fixedAt: ISO }
  
  Print progress:
    [Iter {iteration}/{maxIterations}] Match Rate: {matchRate}% (target: {threshold}%)
  
  IF matchRate >= threshold:
    Print "[Quality Gate PASSED] {matchRate}% ≥ {threshold}% — 기준 충족"
    BREAK → proceed to Step 5
  
  IF iteration >= maxIterations:
    Print "[Max Iterations] {maxIterations}회 완료. 현재 {matchRate}% (목표 {threshold}%)"
    Print "⚠️  목표 미달 — 리포트에 미해결 항목 기록 후 진행"
    BREAK → proceed to Step 5 with warning
  
  Auto-select CP-5: "[CP-5 Auto] 지금 모두 수정 선택됨"
  Fix ALL identified gaps:
    - For each gap: read affected file → apply minimal targeted fix
    - Do NOT refactor unrelated code
  
  CONTINUE LOOP
```

**Write final analysis to:** `docs/03-analysis/{featureName}.analysis.md`

**Progress:**
```
[4/5] ANALYZE ✅  Match Rate: {matchRate}% (target: {threshold}%)
      Iterations: {N} | Gaps fixed: {M} | Status: PASSED / WARNING
```

---

### Step 5 — REPORT Phase

1. Load ALL upstream documents: PRD → Plan → Design → Analysis
2. Generate completion report
3. If report reveals remaining unresolved issues → fix them, update analysis, regenerate
4. Finalize session log:
   ```json
   {
     "finalMatchRate": <matchRate>,
     "status": "completed" | "completed-with-warning",
     "completedAt": "<ISO timestamp>"
   }
   ```
5. Write report to `docs/04-report/features/{featureName}.report.md`
6. Update `.bkit/state/pdca-status.json` → phase = "completed"

**Progress:**
```
[5/5] REPORT ✅  {featureName}.report.md
```

---

### Step 6 — Git Push

> **Why first?** APK builds can take 1–5 minutes. Pushing source code immediately after
> REPORT guarantees the commit lands even if the APK build fails or hangs.

#### 6-1. Get Current Branch

```bash
git branch --show-current
```

Store result as `{branch}`. If empty (detached HEAD), use `git rev-parse --abbrev-ref HEAD`.

#### 6-2. Stage All Changes (Windows-safe)

Stage only source files and docs — **never use `git add -u`** (it stages tracked build artifacts):

```bash
# Stage only safe source directories (no build artifacts)
git add docs/
git add .bkit/runtime/joey-log.json
git add .claude/skills/
git add diary-app/app/src/
git add CLAUDE.md
```

Safety net — in case build artifacts were previously staged, unstage them:
```bash
git restore --staged diary-app/app/build/ 2>/dev/null || true
git restore --staged diary-app/.gradle/ 2>/dev/null || true
```

Then verify:
```bash
git status --short
```

**If nothing is staged AND `git log origin/{branch}..HEAD` shows 0 commits ahead:**
```
[Git] 변경사항 없음, 미push 커밋도 없음 — push 생략
```
Log `gitStatus: "nothing-to-commit"` and proceed to Step 7 (APK build).

**If nothing is staged BUT there ARE unpushed commits** (git log shows ahead):
- Skip commit step, go directly to 6-4 Push.

#### 6-3. Commit

Note: APK status is NOT included in the commit message because the APK builds after this commit.

```bash
git commit -m "feat({featureName}): Joey auto-PDCA complete [{finalMatchRate}%]

- PDCA pipeline: plan → design → do → analyze → report
- Quality gate: {finalMatchRate}% (target: {threshold}%)
- Iterations: {N}/{maxIterations}

Co-Authored-By: Joey Auto-PDCA <joey@bkit>"
```

#### 6-4. Push to Current Branch

Always push explicitly to the current branch on `origin`:

```bash
git push origin {branch}
```

**On success:**
```
[Git Push ✅] {branch} → origin/{branch}
```
Log `gitStatus: "pushed"`, `gitBranch: "{branch}"`

**On failure — upstream not set:**
```bash
git push --set-upstream origin {branch}
```
If this succeeds, treat as success and log `gitStatus: "pushed-with-upstream"`.

**On failure — diverged (non-fast-forward):**
```bash
git pull --rebase origin {branch}
git push origin {branch}
```
If this succeeds, treat as success and log `gitStatus: "pushed-after-rebase"`.

**On any other failure:**
- Print the error output
- Print:
  ```
  [Git Push ⚠️] Push 실패. 아래를 확인해주세요:
    git remote -v                              # 원격 저장소 연결 확인
    git push origin {branch}                  # 현재 브랜치로 직접 push
    git push --set-upstream origin {branch}   # upstream 미설정 시
    git pull --rebase origin {branch} && git push origin {branch}  # 충돌 시
  ```
- Log `gitStatus: "failed"`, `gitError: "{error-summary}"`
- **Still proceed to Step 7 (APK build)** regardless of push result

**Progress:**
```
[6/7] GIT PUSH ✅/⚠️  {branch} → origin/{branch}
```

---

### Step 7 — Android Debug APK Build

After git push (Step 6), attempt to build the Android debug APK.
The push is already done, so APK build time does not affect source delivery.

#### 7-1. Detect Android Project

Search for Android project root (directory containing `gradlew` or `gradlew.bat`):
```bash
Glob: **/gradlew.bat
Glob: **/build.gradle.kts
Glob: **/build.gradle
```

If **no Android project found**:
- Print:
  ```
  [APK Build] Android 프로젝트 없음 — APK 빌드 스킵 (소스코드는 push 완료)
  ```
- Log `apkStatus: "skipped-no-project"`
- **Do NOT call AskUserQuestion** — just skip and show Final Summary.

#### 7-2. Run Gradle Build

Set `JAVA_HOME` to Android Studio's bundled JBR if `java` is not in PATH:
```powershell
# Windows — detect Android Studio JBR automatically
$studioJbr = "C:\Program Files\Android\Android Studio\jbr"
if (Test-Path "$studioJbr\bin\java.exe") {
    $env:JAVA_HOME = $studioJbr
    $env:PATH = "$studioJbr\bin;$env:PATH"
}
```

Then run the build:
```powershell
# Windows: use gradlew.bat from the detected androidProjectRoot
cd {androidProjectRoot}
.\gradlew.bat assembleDebug --no-daemon 2>&1 | Select-Object -Last 60
```

**On success** (exit code 0):
- Find APK path via PowerShell: `Get-ChildItem "{androidProjectRoot}" -Recurse -Filter "app-debug.apk"`
- Print:
  ```
  [APK Build ✅] Debug APK 생성 완료
  경로: {apkPath}
  ```
- Log `apkStatus: "success"`, `apkPath: "{apkPath}"`

**On failure** (non-zero exit or build error):
- Print error output summary (last 30 lines)
- Print:
  ```
  [APK Build ⚠️] 빌드 실패 (소스 코드는 이미 push 완료)

  수동 빌드 방법:
  방법 1 (터미널):
    cd {androidProjectRoot}
    .\gradlew.bat assembleDebug

  방법 2 (Android Studio):
    1. Build → Make Project (Ctrl+F9)
    2. Build → Build Bundle(s) / APK(s) → Build APK(s)
    3. 생성 위치: app/build/outputs/apk/debug/app-debug.apk

  방법 3 (문제 해결):
    .\gradlew.bat assembleDebug --info   # 상세 로그 확인
    .\gradlew.bat clean assembleDebug   # 클린 후 재빌드
  ```
- Log `apkStatus: "failed"`, `apkError: "{first-error-line}"`

**Progress:**
```
[7/7] APK BUILD ✅/⚠️  {apkPath or "failed"}
```

---

### Final Summary

```
╔══════════════════════════════════════════════════════════════════╗
║  Joey Pipeline Complete!                                        ║
╠══════════════════════════════════════════════════════════════════╣
║  Feature:       {featureName}                                   ║
║  Target:        {threshold}%   Actual: {finalMatchRate}%        ║
║  Status:        PASSED ✅  /  WARNING ⚠️                        ║
║  Iterations:    {N} / {maxIterations}                           ║
╠══════════════════════════════════════════════════════════════════╣
║  Plan:    docs/01-plan/features/{featureName}.plan.md           ║
║  Design:  docs/02-design/features/{featureName}.design.md      ║
║  Report:  docs/04-report/features/{featureName}.report.md      ║
║  Log:     .bkit/runtime/joey-log.json                          ║
╠══════════════════════════════════════════════════════════════════╣
║  Git:     {branch} → origin/{branch}  {gitStatus}    [Step 6] ║
║  APK:     {apkPath or "failed/skipped"}               [Step 7] ║
╚══════════════════════════════════════════════════════════════════╝
```

---

## Session Log Schema (`.bkit/runtime/joey-log.json`)

```json
{
  "feature": "login-screen",
  "threshold": 95,
  "startedAt": "2026-05-23T10:00:00.000Z",
  "completedAt": "2026-05-23T10:42:00.000Z",
  "decisions": [
    { "checkpoint": 1, "decision": "auto-approve", "reason": "joey-mode" },
    { "checkpoint": 2, "decision": "auto-approve", "reason": "joey-mode" },
    { "checkpoint": 3, "decision": "option-c",     "reason": "pragmatic-default" },
    { "checkpoint": 4, "decision": "auto-approve",  "filesChanged": 4 },
    { "checkpoint": 5, "decision": "fix-all",       "iteration": 1 }
  ],
  "iterations": [
    { "iteration": 1, "matchRate": 82, "gapsFound": 3, "gapsFixed": 3 },
    { "iteration": 2, "matchRate": 96, "gapsFound": 0, "gapsFixed": 0 }
  ],
  "finalMatchRate": 96,
  "status": "completed",
  "gitStatus": "pushed",
  "gitBranch": "main",
  "apkStatus": "success",
  "apkPath": "app/build/outputs/apk/debug/app-debug.apk"
}
```

---

## Error Handling

| Situation | Action |
|-----------|--------|
| threshold out of range | Clamp to [1, 100]; log warning |
| Plan template missing | Use built-in structure; continue |
| Design template missing | Use built-in structure; continue |
| Code error introduced by fix | Revert that fix; mark gap as "manual-required"; continue |
| matchRate < threshold after 5 iterations | Proceed with ⚠️ warning; list unresolved gaps in report |
| File write error | Retry once; if fails, report error and stop |

---

## Quality Gate Reference

| threshold | When to use |
|-----------|------------|
| `80` | 빠른 프로토타입 — 대략적 동작 확인 |
| `90` | 기본값 — 일반 기능 개발 (권장) |
| `95` | 중요 기능 — 결제, 인증 등 |
| `100` | 완전 검증 — 모든 SC 충족 필수 |

---

## Examples

```bash
# 기본 품질 기준 (90%)
/joey 조이어리 앱에 다크모드 추가

# 95% 기준 — 중요 기능
/joey 95 로그인 화면 추가해줘

# 100% 기준 — 모든 항목 충족 필수
/joey 100 이미지 압축 버그 수정

# 낮은 기준 — 빠른 프로토타입
/joey 80 간단한 설정 화면 추가
```

ARGUMENTS:
