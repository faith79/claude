<!-- Generated: 2026-06-06 | Updated: 2026-06-06 -->

# claude

## Purpose
Monorepo containing two applications: a Kotlin/Compose Android diary app (`diary-app/`) and a vanilla JS todo web app (root-level). PDCA development workflow is tracked in `docs/` using the bkit framework.

## Key Files

| File | Description |
|------|-------------|
| `app.js` | Todo web app — state mutations, filter logic, localStorage persistence |
| `index.html` | Todo web app — HTML entry point |
| `style.css` | Todo web app — styles |
| `CLAUDE.md` | Claude Code instructions for this repo |
| `README.md` | Project overview |

## Subdirectories

| Directory | Purpose |
|-----------|---------|
| `diary-app/` | Android diary app — Kotlin, Jetpack Compose, Firebase (see `diary-app/AGENTS.md`) |
| `docs/` | PDCA documentation — plans, designs, analyses, reports (see `docs/AGENTS.md`) |
| `.claude/` | Claude Code configuration — agents, skills, commands, settings |
| `.bkit/` | bkit framework state — audit logs, snapshots, PDCA state |
| `.omc/` | oh-my-claudecode state — session data, HUD state |

## For AI Agents

### Working In This Directory
- The root-level `app.js`/`index.html`/`style.css` form a standalone todo web app; no build step needed
- The main production app lives in `diary-app/` — always work there for Android/Firebase changes
- Run `docs/` plans before implementing features; check gap analyses after

### Testing Requirements
- Todo web app: open `index.html` in a browser
- Diary app: build via `./gradlew assembleDebug` inside `diary-app/`

### Common Patterns
- PDCA cycle: plan → design → implement → gap-analysis → report
- Feature branches named after feature slugs (e.g. `joyary-upgrade-v10`)
- APK committed to repo after each push (per project convention)

## Dependencies

### External
- Firebase / Firestore — backend for diary-app
- Hilt — dependency injection (diary-app)
- Jetpack Compose — UI toolkit (diary-app)
- bkit — PDCA workflow framework (dev tooling)
- oh-my-claudecode — Claude Code enhancement plugin

<!-- MANUAL: -->
