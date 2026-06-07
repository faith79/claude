<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-06 | Updated: 2026-06-06 -->

# docs

## Purpose
PDCA (Plan-Do-Check-Act) documentation for the diary-app project, managed by the bkit framework. Contains feature plans, design specs, gap analysis results, and completion reports organized by cycle phase.

## Subdirectories

| Directory | Purpose |
|-----------|---------|
| `01-plan/` | Feature plans — requirements, scope, success criteria (see `01-plan/AGENTS.md`) |
| `02-design/` | Technical design specs — architecture decisions, component changes (see `02-design/AGENTS.md`) |
| `03-analysis/` | Gap analysis results — design vs implementation comparison (see `03-analysis/AGENTS.md`) |
| `04-report/` | Completion reports — what was done, lessons learned (see `04-report/AGENTS.md`) |
| `archive/` | Older completed cycles archived by month |

## For AI Agents

### Working In This Directory
- Every feature starts with a plan in `01-plan/features/<slug>.plan.md`
- Design documents in `02-design/` must exist before implementation begins
- After implementation, run gap analysis — results go in `03-analysis/<slug>.analysis.md`
- Match rate < 90% triggers iteration; ≥ 90% proceeds to `04-report/`
- File naming: always use the feature slug (e.g. `joyary-upgrade-v10`)

### Common Patterns
- Feature slugs are kebab-case and version-suffixed (e.g. `joyary-upgrade-v10`)
- Plans reference design IDs (e.g. `§3.1`) that design docs define
- Design docs reference implementation via `Design Ref:` comments in source code

<!-- MANUAL: -->
