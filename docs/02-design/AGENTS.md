<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-06 | Updated: 2026-06-06 -->

# 02-design

## Purpose
Technical design specifications for each feature. Defines the exact changes to make — which files, which classes, which APIs — before a single line of production code is written.

## Subdirectories

| Directory | Purpose |
|-----------|---------|
| `features/` | One `.design.md` per feature slug |

## For AI Agents

### Working In This Directory
- Design documents must exist before implementation
- Design IDs (e.g. `§3.1`, `SC-08`) are referenced by `Design Ref:` comments in source code
- After implementation, run gap analysis (`03-analysis/`) to verify coverage
- Naming: `<feature-slug>.design.md`

<!-- MANUAL: -->
