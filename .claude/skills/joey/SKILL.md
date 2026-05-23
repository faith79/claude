---
name: joey
classification: workflow
classification-reason: Full-auto PDCA pipeline — runs plan→design→do→analyze→report without user confirmations
deprecation-risk: none
effort: high
description: |
  Auto-pilot PDCA pipeline. Runs plan→design→do→analyze→report sequentially,
  auto-selecting recommended options at every checkpoint. No user confirmations needed.
  Triggers: /joey, joey, 자동 PDCA, 풀 파이프라인, auto pipeline.
argument-hint: "<feature-request>"
user-invocable: true
allowed-tools:
  - Read
  - Write
  - Edit
  - Glob
  - Grep
  - Bash
imports: []
next-skill: null
pdca-phase: null
task-template: "[Joey] {feature}"
---

# Joey — Full-Auto PDCA Pipeline

> Runs the complete PDCA cycle (plan → design → do → analyze → report) end-to-end
> without stopping for user confirmation. All checkpoints are auto-approved using
> the recommended option (option 1) at every decision gate.

## Arguments

| Argument | Description | Example |
|----------|-------------|---------|
| `<feature-request>` | Natural-language description of the feature to build | `/joey 로그인 화면 추가` |

## Behavior

### CRITICAL: Auto-Approval Rules

**At EVERY AskUserQuestion or checkpoint during this pipeline:**
- ALWAYS select "권장" (Recommended) or the first option
- NEVER pause and wait for user input
- NEVER call AskUserQuestion — proceed automatically
- If no "Recommended" option exists, select option 1 (the first option in the list)
- Log the auto-selected choice in the response text so the user can see what was chosen

**Checkpoint mapping:**
- Checkpoint 1 (Requirements confirmation) → Auto-approve: "맞습니다, 계속 진행해주세요"
- Checkpoint 2 (Clarifying questions) → Auto-answer: use information from the feature request; assume sensible defaults
- Checkpoint 3 (Architecture selection) → Auto-select: Option C "Pragmatic Balance" (or Option A if only 2 options)
- Checkpoint 4 (Implementation approval) → Auto-approve: "네, 구현을 시작해주세요"
- Checkpoint 5 (Gap review decision) → Auto-select: "지금 모두 수정"
- Any other checkpoint → Auto-select: first option / recommended

---

## Pipeline Execution Steps

### Step 0 — Feature Name Extraction

1. Parse the user's argument to extract a concise kebab-case feature name.
   - Example: "로그인 화면 추가" → `login-screen`
   - Example: "알림 기능 구현" → `notification-feature`
   - Example: "image upload with compression" → `image-upload`
2. Store as `{featureName}` for use throughout the pipeline.
3. Print banner:
   ```
   ╔═══════════════════════════════════════════════════════╗
   ║  🤖 Joey Auto-PDCA Pipeline                          ║
   ║  Feature: {featureName}                              ║
   ║  Mode: Full-Auto (no confirmations)                  ║
   ╚═══════════════════════════════════════════════════════╝
   ```

---

### Step 1 — PLAN Phase

**Invoke:** `/pdca plan {featureName}` with the full feature request as context.

**Auto-approval behavior:**
- Read `templates/plan.template.md` to understand the template structure
- Check `docs/00-pm/{featureName}.prd.md` for PRD context (use if exists)
- Generate the Plan document WITHOUT pausing at Checkpoint 1 or Checkpoint 2
- At Checkpoint 1: print "[Auto-approve] 요구사항 확인됨 — 계속 진행" and proceed
- At Checkpoint 2: print "[Auto-approve] 명확화 질문 생략 — 합리적 기본값 적용" and proceed
- Write the Plan document to `docs/01-plan/features/{featureName}.plan.md`
- Generate Context Anchor (WHY/WHO/RISK/SUCCESS/SCOPE) and embed in the Plan

**Output:** Plan document at `docs/01-plan/features/{featureName}.plan.md`

**Progress indicator:**
```
[1/5] PLAN ✅ — docs/01-plan/features/{featureName}.plan.md
```

---

### Step 2 — DESIGN Phase

**Invoke:** `/pdca design {featureName}` using the Plan document just created.

**Auto-approval behavior:**
- Read the Plan document fully
- Generate 3 architecture options (A: Minimal, B: Clean, C: Pragmatic)
- At Checkpoint 3: print "[Auto-select] Option C — Pragmatic Balance 선택됨" and proceed
- If only 2 options exist: auto-select Option A
- Write the Design document to `docs/02-design/features/{featureName}.design.md`
- Generate Session Guide in §11.3 if multiple modules identified

**Output:** Design document at `docs/02-design/features/{featureName}.design.md`

**Progress indicator:**
```
[2/5] DESIGN ✅ — docs/02-design/features/{featureName}.design.md
```

---

### Step 3 — DO Phase (Implementation)

**Invoke:** `/pdca do {featureName}` using the Design document just created.

**Auto-approval behavior:**
- Read Design document fully (all sections)
- Load full upstream context: PRD → Plan → Design
- Display Decision Record Chain
- Display Success Criteria checklist
- At Checkpoint 4: print "[Auto-approve] 구현 범위 승인 — 즉시 시작" and proceed
- Implement ALL code changes specified in the Design document
- Add Design Ref comments as specified in the DO phase rules
- Implement the full scope (no --scope filtering)

**Output:** All code files modified/created as specified in Design

**Progress indicator:**
```
[3/5] DO ✅ — {N} files modified, {M} files created
```

---

### Step 4 — ANALYZE Phase (Check)

**Invoke:** `/pdca analyze {featureName}` to run gap analysis.

**Auto-approval behavior:**
- Load full upstream context: PRD → Plan → Design → Implementation
- Run static gap analysis (Structural + Functional + Contract)
- Calculate Match Rate
- At Checkpoint 5:
  - If Match Rate < 90%: print "[Auto-select] 지금 모두 수정 선택됨" → fix ALL gaps → re-analyze
  - If Match Rate ≥ 90%: print "[Auto-approve] Match Rate {X}% — 기준 충족, 다음 단계로 진행"
- Write analysis to `docs/03-analysis/{featureName}.analysis.md`

**Auto-iteration rules:**
- Maximum 3 iteration rounds (fix → analyze → check)
- Each round: fix all identified gaps, then re-run static analysis
- Stop when Match Rate ≥ 90% or 3 rounds exhausted
- If still < 90% after 3 rounds: proceed to report with a warning note

**Progress indicator:**
```
[4/5] ANALYZE ✅ — Match Rate: {X}% (after {N} iteration(s))
```

---

### Step 5 — REPORT Phase

**Invoke:** `/pdca report {featureName}` to generate the completion report.

**Auto-approval behavior:**
- Load ALL upstream documents: PRD → Plan → Design → Analysis
- Generate comprehensive completion report
- If report identifies remaining issues: auto-fix them, then regenerate
- Write report to `docs/04-report/features/{featureName}.report.md`
- Update `.bkit/state/pdca-status.json` to phase = "completed"

**Output:** Report at `docs/04-report/features/{featureName}.report.md`

**Progress indicator:**
```
[5/5] REPORT ✅ — docs/04-report/features/{featureName}.report.md
```

---

### Final Summary

After all 5 steps complete, print:

```
╔═══════════════════════════════════════════════════════════════╗
║  ✅ Joey Pipeline Complete!                                   ║
╠═══════════════════════════════════════════════════════════════╣
║  Feature:     {featureName}                                  ║
║  Match Rate:  {X}%                                           ║
║  Iterations:  {N}                                            ╠═══════════════════════════════════════════════════════════════╣
║  Plan:    docs/01-plan/features/{featureName}.plan.md        ║
║  Design:  docs/02-design/features/{featureName}.design.md   ║
║  Report:  docs/04-report/features/{featureName}.report.md   ║
╚═══════════════════════════════════════════════════════════════╝
```

---

## Error Handling

| Situation | Action |
|-----------|--------|
| Plan template missing | Use built-in plan structure; continue |
| Design template missing | Use built-in design structure; continue |
| Code compilation error | Fix the error automatically; do NOT stop the pipeline |
| Match Rate < 90% after 3 iterations | Proceed to report with note: "수동 검토 필요한 항목이 있습니다" |
| File write error | Retry once; if fails, report the error and stop |

## Examples

```bash
# Full auto pipeline for a new feature
/joey 로그인 화면 추가해줘

# English request works too
/joey Add push notification support for daily reminders

# Complex multi-part request
/joey 이미지 업로드 기능: 100KB 압축, EXIF 회전 보정, 갤러리 표시
```

## Important Notes

- This skill bypasses ALL user confirmation gates. Use when you trust the AI to make reasonable architectural decisions.
- If you need to review decisions at any step, use `/pdca plan`, `/pdca design`, etc. individually instead.
- The generated code follows the project's existing conventions and design patterns.
- All auto-selected choices are logged in the response so you can audit the decisions made.

ARGUMENTS:
