---
name: joey
classification: workflow
classification-reason: Quality-gated full-auto PDCA pipeline with configurable match-rate threshold and audit trail
deprecation-risk: none
effort: high
description: |
  Full-auto PDCA pipeline with user-defined quality gate. Runs plan→design→do→analyze→report
  sequentially. Iterates the analyze→fix loop until match rate reaches the target threshold.
  No git push after report.
  First argument is optional target % (default 100). No user confirmations at any checkpoint.
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
| `[target%]` | Integer 1–100 | `100` | Required match rate before report is generated |
| `<feature-request>` | String | — | Natural-language description of what to build |

**Parsing rule**: If the first token is a pure integer (no letters), treat it as `target%`.
Everything else is the feature request.

```bash
/joey 95 로그인 화면 추가해줘        # threshold=95, request="로그인 화면 추가해줘"
/joey 이미지 업로드 기능             # threshold=100 (default), request="이미지 업로드 기능"
/joey 80 빠른 프로토타입 만들어줘   # threshold=80, accepts lower bar
```

---

## CRITICAL: Auto-Approval Rules

**At EVERY checkpoint during this pipeline — NEVER pause:**
- Select "권장" (Recommended) or the first option automatically
- **NEVER call AskUserQuestion** — except for the ONE permitted case below
- Log the auto-selected choice inline so the user can audit decisions

### ONE Permitted User Interaction

**Step 0.5 UI Design Picker** is the ONLY allowed `AskUserQuestion` call.
It fires automatically when the feature request contains any of:
`UI`, `ui`, `디자인`, `design`, `화면`, `스타일`, `세련`, `개선`, `모던`, `modern`

All other checkpoints (CP-1 through CP-5) remain fully automatic.

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
   - Otherwise: `threshold = 100`, `request = full argument`
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

### Step 0.5 — UI Design Picker (UI 모드일 때만 실행)

**Trigger:** feature request에 `UI`, `디자인`, `화면`, `스타일`, `세련`, `개선`, `모던`, `design` 포함 시 자동 실행.

**Action:** `AskUserQuestion`으로 4가지 디자인 패키지를 제시한다. 사용자가 선택하면 그 스펙이 Plan/Design/Do 단계의 구현 기준이 된다.

```
Question: "어떤 UI 디자인 방향으로 개선할까요?"
Header: "디자인 선택"
```

**4가지 패키지 (ASCII 프리뷰 포함):**

#### 패키지 A: Material You 다이나믹 (2025 최신 Android 스타일)
```
┌─────────────────────────────┐
│  조이어리                   │
│  2026년 5월        ⚙️       │
├─────────────────────────────┤
│  월  화  수  목  금  토  일 │
│   1   2   3   4   5   6   7 │
│  15  16  17  18 ●19  20  21 │
│              ↑오늘:solid원형│
├─────────────────────────────┤
│  📅 19일 일기 내용 미리보기  │
└──────────[✏️ 오늘 일기 쓰기]┘
   ← Extended FAB (텍스트포함)
```
구현 스펙:
- `TopAppBar` → `LargeTopAppBar` + `exitUntilCollapsedScrollBehavior()`
- `Scaffold(modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection))`
- `FloatingActionButton` → `ExtendedFloatingActionButton(icon=Add, text="오늘 일기 쓰기")`
- `DayCell.isToday`: bg=`colorScheme.primary`, textColor=`colorScheme.onPrimary`

#### 패키지 B: 이모지 감정 캘린더
```
┌─────────────────────────────┐
│  5월 ←  →           ⚙️     │
│  이번달: 😊×5 😢×2 😡×1    │
├─────────────────────────────┤
│  월   화   수   목   금     │
│       😊   😢        😊    │
│  1    2    3    4    5      │
│  😊        😡              │
│  8    9   10   11   12      │
└─────────────────────────────┘
  ↑ 이모지 크게 + 날짜 작게
```
구현 스펙:
- `DayCell`: emotion 있으면 emoji 32sp 중앙, 날짜 10sp 좌상단 오버레이
- `CalendarHeader` 하단에 이번달 감정 분포 통계 한 줄 추가
- `diaryMap`에서 emotion count 집계 → `"😊×N 😢×N"` 표시

#### 패키지 C: 보텀 네비게이션 미니멀
```
┌─────────────────────────────┐
│                       ＋    │
│   2026년                    │
│   5월              Bold 32sp│
├─────────────────────────────┤
│  월  화  수  목  금  토  일 │
│   1   2   3   4   5   6   7 │
│  15  16  17  18  19  20  21 │
│  22  23  24  25  26  27  28 │
├─────────────────────────────┤
│  🏠 홈    │  ⚙️ 설정        │
└─────────────────────────────┘
  ↑ NavigationBar 하단 고정
```
구현 스펙:
- `TopAppBar` 제거 → 달력 헤더에 월/년 Bold 32sp + 설정 아이콘
- `NavigationBar` 하단 추가: 홈 / 설정 탭
- `CalendarHeader`: year/month `headlineLarge` 폰트
- `Scaffold.bottomBar = { NavigationBar {...} }`

#### 패키지 D: 스크롤 카드 리스트
```
┌─────────────────────────────┐
│  조이어리              ⚙️   │
├───── 미니 달력 (3줄) ───────┤
│ 1  2  3  4  5  6  7  8  9  │
│10 11 12 13 14 15 16 17 18  │
│19●20 21 22 23 24 25 26 27  │
├───── 이번달 일기 ───────────┤
│ 😊 5월19일            오늘  │
│ 오늘은 정말 좋은 하루...    │
├─────────────────────────────┤
│ 😢 5월12일                  │
│ 비가 많이 와서 우울했던...   │
└─────────────────────────────┘
  ↑ 카드 스크롤 리스트
```
구현 스펙:
- `LazyVerticalGrid` 7열 → 가로 스크롤 `LazyRow` 미니 달력 (3행)
- 달력 아래: 해당 월 일기 `LazyColumn` 카드 리스트
- 카드: emotion emoji + 날짜 + 내용 첫 줄 미리보기
- 날짜 탭 → 해당 카드로 스크롤

**After user selects a package:**
- `uiDesignChoice = "{A|B|C|D}: {packageName}"`
- Log `{ checkpoint: 0.5, decision: "{A|B|C|D}", reason: "user-selected" }`
- Proceed to Step 1 with the selected package's 구현 스펙 as the implementation guide

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
  "status": "completed"
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
| `90` | 일반 기능 개발 |
| `95` | 중요 기능 — 결제, 인증 등 |
| `100` | 기본값 — 완전 검증 (권장) |

---

## Examples

```bash
# 기본 품질 기준 (100%)
/joey 조이어리 앱에 다크모드 추가

# 95% 기준 — 중요 기능
/joey 95 로그인 화면 추가해줘

# 100% 기준 — 모든 항목 충족 필수
/joey 100 이미지 압축 버그 수정

# 낮은 기준 — 빠른 프로토타입
/joey 80 간단한 설정 화면 추가

# UI 디자인 모드 — 디자인 피커 자동 실행
/joey 홈 화면 UI 개선해줘        # → Step 0.5 디자인 피커 실행 (A/B/C/D 선택)
/joey 95 설정 화면 디자인 세련되게  # → 피커 후 95% 기준으로 진행
/joey UI 전체 화면 모던하게 바꿔줘  # → 피커 후 전체 적용
```

## UI Design Mode — 빠른 참조

| 패키지 | 특징 | 주요 변경 파일 |
|--------|------|---------------|
| A: Material You 다이나믹 | LargeTopAppBar + ExtendedFAB + solid 오늘 원형 | HomeScreen.kt |
| B: 이모지 감정 캘린더 | 날짜 셀에 emoji 크게 + 감정 통계 헤더 | HomeScreen.kt |
| C: 보텀 네비게이션 미니멀 | NavigationBar 하단 + Bold 월/년 헤더 | HomeScreen.kt |
| D: 스크롤 카드 리스트 | 미니 달력 + 일기 카드 리스트 | HomeScreen.kt |

ARGUMENTS:
