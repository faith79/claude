# todo-app Design Document

> **Summary**: HTML/CSS/JS 3파일 분리 구조의 Todo 앱 — localStorage 영속성, 인라인 수정, 필터
>
> **Project**: claude
> **Version**: 0.1.0
> **Author**: faith79@jobkorea.co.kr
> **Date**: 2026-05-04
> **Status**: Draft
> **Planning Doc**: [todo-app.plan.md](../01-plan/features/todo-app.plan.md)

### Pipeline References

| Phase | Document | Status |
|-------|----------|--------|
| Phase 1 | Schema Definition | N/A |
| Phase 2 | Coding Conventions | N/A |
| Phase 3 | Mockup | N/A |
| Phase 4 | API Spec | N/A |

---

## Context Anchor

> Copied from Plan document. Ensures strategic context survives Design→Do handoff.

| Key | Value |
|-----|-------|
| **WHY** | 백엔드 없이 브라우저만으로 동작하는 경량 Todo 관리 도구가 필요하다 |
| **WHO** | 간단한 할 일 관리가 필요한 일반 사용자 |
| **RISK** | localStorage 용량 제한(5MB), 브라우저 데이터 삭제 시 데이터 손실 |
| **SUCCESS** | CRUD + 필터 + 인라인 수정 + 일괄 삭제 모두 동작, localStorage 영속성 확인 |
| **SCOPE** | Phase 1 — 3파일 분리 (index.html + style.css + app.js), 백엔드 없음 |

---

## 1. Overview

### 1.1 Design Goals

- 외부 의존성 없이 순수 HTML/CSS/JS만으로 완전한 Todo 앱 구현
- 파일 역할을 명확하게 분리하여 가독성과 유지보수성 확보
- 최소한의 코드로 FR-01~FR-08 모든 기능 구현

### 1.2 Design Principles

- **단일 책임**: index.html은 마크업만, style.css는 스타일만, app.js는 로직만
- **함수 단위 분리**: 각 기능(add, toggle, delete, edit, filter, render, save)을 독립 함수로 구성
- **상태 중앙화**: `todos[]` 배열이 단일 진실 공급원(Single Source of Truth)

---

## 2. Architecture Options

### 2.0 Architecture Comparison

| 기준 | Option A: 단일 파일 | Option B: 3파일 분리 | Option C: 2파일 균형 |
|------|:-:|:-:|:-:|
| **파일 수** | 1 | 3 | 2 |
| **복잡도** | 매우 낮음 | 낮음 | 낮음 |
| **유지보수** | 어려움 | 좋음 | 좋음 |
| **공유 편의성** | 최고 | 보통 | 좋음 |
| **Recommendation** | 빠른 데모 | **선택됨** | 기본값 |

**Selected**: **Option B — 3파일 분리** — 코드 탐색과 유지보수가 가장 용이하고, HTML/CSS/JS 역할을 명확하게 분리

### 2.1 Component Diagram

```
┌──────────────────────────────────────────────┐
│               Browser                         │
│                                               │
│  index.html ──── style.css                    │
│      │                                        │
│      └────── app.js                           │
│                 │                             │
│                 ├── todos[] (state)            │
│                 ├── render()                  │
│                 ├── saveTodos() ──▶ localStorage │
│                 └── loadTodos() ◀── localStorage │
└──────────────────────────────────────────────┘
```

### 2.2 Data Flow

```
User Input
  → Event Handler (add/toggle/delete/edit/filter)
    → State Update (todos[])
      → saveTodos() → localStorage
        → render() → DOM Update
```

### 2.3 Dependencies

| Component | Depends On | Purpose |
|-----------|-----------|---------|
| app.js | DOM (index.html) | 요소 참조 및 이벤트 바인딩 |
| app.js | localStorage API | 데이터 영속성 |
| index.html | style.css | 스타일 적용 |
| index.html | app.js | 앱 로직 실행 |

---

## 3. Data Model

### 3.1 Entity Definition

```javascript
// Todo 항목 구조
{
  id: number,        // Date.now() 기반 고유 ID
  text: string,      // 할 일 내용
  completed: boolean // 완료 여부
}

// 전역 상태
let todos = [];          // Todo 배열 (Single Source of Truth)
let currentFilter = 'all'; // 현재 필터 ('all' | 'active' | 'completed')
```

### 3.2 localStorage Schema

```
Key: 'todos'
Value: JSON.stringify(todos[])

Example:
[
  { "id": 1714800000000, "text": "장보기", "completed": false },
  { "id": 1714800001000, "text": "코드 리뷰", "completed": true }
]
```

---

## 4. API Specification

N/A — 순수 프론트엔드 앱, 외부 API 없음

localStorage 접근 패턴:
- **READ**: `JSON.parse(localStorage.getItem('todos') || '[]')`
- **WRITE**: `localStorage.setItem('todos', JSON.stringify(todos))`

---

## 5. UI/UX Design

### 5.1 Screen Layout

```
┌─────────────────────────────────┐
│  h1: todos                      │
├─────────────────────────────────┤
│  [input placeholder="할 일 입력"] [Add] │
├─────────────────────────────────┤
│  ☐ 장보기                    [×] │
│  ☑ 코드 리뷰 (취소선)         [×] │
│  ☐ 운동하기                   [×] │
├─────────────────────────────────┤
│  2 items left                   │
│  [All] [Active] [Completed]     │
│              [Clear completed]  │
└─────────────────────────────────┘
```

### 5.2 User Flow

```
페이지 로드
  → loadTodos() (localStorage 읽기)
  → render() (초기 목록 표시)

할 일 추가:
  입력창 텍스트 입력 → Enter / Add 버튼
  → addTodo() → todos[] 업데이트 → saveTodos() → render()

완료 토글:
  체크박스 클릭 → toggleTodo(id) → saveTodos() → render()

개별 삭제:
  × 버튼 클릭 → deleteTodo(id) → saveTodos() → render()

인라인 수정:
  항목 텍스트 더블클릭 → <input> 활성화
  Enter / blur → editTodo(id, newText) → saveTodos() → render()
  빈 텍스트 + Enter / blur → deleteTodo(id)

필터 전환:
  All / Active / Completed 탭 클릭 → currentFilter 변경 → render()

일괄 삭제:
  Clear completed 클릭 → clearCompleted() → saveTodos() → render()
```

### 5.3 Component List

| Element / 함수 | 파일 | 역할 |
|---------------|------|------|
| `#todo-input` | index.html | 할 일 입력창 |
| `#add-btn` | index.html | 추가 버튼 |
| `#todo-list` | index.html | Todo 항목 목록 컨테이너 |
| `#items-left` | index.html | 남은 항목 수 표시 |
| `.filter-btn` | index.html | All/Active/Completed 필터 탭 (×3) |
| `#clear-btn` | index.html | Clear completed 버튼 |
| `render()` | app.js | currentFilter에 따라 목록 렌더링 |
| `addTodo()` | app.js | 새 Todo 추가 |
| `toggleTodo(id)` | app.js | 완료 상태 토글 |
| `deleteTodo(id)` | app.js | 항목 삭제 |
| `editTodo(id, text)` | app.js | 항목 텍스트 수정 |
| `clearCompleted()` | app.js | 완료 항목 일괄 삭제 |
| `saveTodos()` | app.js | localStorage 저장 |
| `loadTodos()` | app.js | localStorage 불러오기 |

### 5.4 Page UI Checklist

#### 메인 페이지 (index.html)

- [ ] Input: 할 일 텍스트 입력창 (`#todo-input`, placeholder 있음)
- [ ] Button: Add 추가 버튼 (`#add-btn`)
- [ ] List: Todo 항목 목록 (`#todo-list`)
  - [ ] Checkbox: 완료 토글 체크박스 (각 항목)
  - [ ] Text: 할 일 내용 (완료 시 취소선 스타일)
  - [ ] Button: 개별 삭제 버튼 `×` (각 항목, hover 시 표시)
  - [ ] Input: 인라인 수정 입력창 (더블클릭 시 활성화)
- [ ] Counter: "N items left" 남은 항목 수 (`#items-left`)
- [ ] Filter: All 탭 (`[data-filter="all"]`)
- [ ] Filter: Active 탭 (`[data-filter="active"]`)
- [ ] Filter: Completed 탭 (`[data-filter="completed"]`)
- [ ] Button: "Clear completed" 일괄 삭제 버튼 (`#clear-btn`, 완료 항목 없을 시 숨김)

---

## 6. Error Handling

### 6.1 엣지 케이스 처리

| 상황 | 처리 방법 |
|------|----------|
| 빈 문자열 Todo 추가 시도 | trim() 후 빈 값이면 무시 (추가 안 함) |
| 빈 문자열로 수정 저장 | deleteTodo() 호출 (항목 삭제) |
| localStorage 파싱 오류 | try/catch 후 빈 배열([])로 초기화 |
| 완료 항목 없을 때 Clear 버튼 | `#clear-btn` hidden 처리 |

### 6.2 localStorage 오류

```javascript
function loadTodos() {
  try {
    return JSON.parse(localStorage.getItem('todos') || '[]');
  } catch {
    return [];
  }
}
```

---

## 7. Security Considerations

- [ ] XSS 방지: `textContent` 사용 (innerHTML 금지), 사용자 입력을 DOM 텍스트로만 삽입
- [ ] 입력값 trim 처리로 공백 전용 입력 방지
- HTTPS, 인증, SQL Injection은 해당 없음 (순수 클라이언트 앱)

---

## 8. Test Plan

### 8.1 Test Scope

| Type | Target | Tool | Phase |
|------|--------|------|-------|
| L2: UI Action | 각 기능 동작 확인 | 브라우저 수동 테스트 | Do |
| L3: E2E Scenario | 전체 사용 흐름 | 브라우저 수동 테스트 | Do |

> API(L1)는 없음. L2/L3는 브라우저에서 직접 수동 테스트.

### 8.2 L2: UI Action Test Scenarios

| # | Action | Expected Result |
|---|--------|----------------|
| 1 | 텍스트 입력 후 Enter | 목록에 새 항목 추가, 입력창 초기화 |
| 2 | 텍스트 입력 후 Add 클릭 | 목록에 새 항목 추가, 입력창 초기화 |
| 3 | 빈 칸 Enter | 추가 안 됨 |
| 4 | 체크박스 클릭 | 완료 상태 토글, 취소선 표시/해제 |
| 5 | × 버튼 클릭 | 해당 항목 삭제 |
| 6 | 항목 더블클릭 | 인라인 입력창 활성화 |
| 7 | 수정 후 Enter | 수정 내용 저장, 표시 모드 복귀 |
| 8 | 수정창 blur | 수정 내용 저장 |
| 9 | 수정창 빈 값 Enter | 항목 삭제 |
| 10 | Active 필터 | 미완료 항목만 표시 |
| 11 | Completed 필터 | 완료 항목만 표시 |
| 12 | All 필터 | 전체 항목 표시 |
| 13 | Clear completed 클릭 | 완료 항목 모두 삭제 |
| 14 | 새로고침 | 이전 데이터 유지 (localStorage) |

### 8.3 L3: E2E Scenario Test Scenarios

| # | Scenario | Steps | Success Criteria |
|---|----------|-------|-----------------|
| 1 | 기본 CRUD 흐름 | 추가 → 완료 토글 → 수정 → 삭제 | 각 단계 정상 동작 |
| 2 | 필터 흐름 | 여러 항목 추가 → Active → Completed → All 순 전환 | 올바른 항목만 표시 |
| 3 | 영속성 확인 | 항목 추가 → 새로고침 → 데이터 유지 확인 | 새로고침 후 동일 데이터 |
| 4 | Clear completed | 일부 완료 → Clear completed → 미완료만 남음 | 완료 항목만 삭제 |

---

## 9. Clean Architecture

### 9.1 Layer Structure (Starter Level)

| Layer | Location | Content |
|-------|----------|---------|
| Presentation | `index.html` | DOM 마크업, `style.css` 참조 |
| Style | `style.css` | 모든 CSS 규칙 |
| Logic/State | `app.js` | 상태, 렌더링, 이벤트, 스토리지 |

### 9.2 app.js 내부 구조

```
app.js
├── 상태 변수
│   ├── let todos = []
│   └── let currentFilter = 'all'
│
├── 스토리지 함수
│   ├── loadTodos()
│   └── saveTodos()
│
├── 상태 변경 함수
│   ├── addTodo(text)
│   ├── toggleTodo(id)
│   ├── deleteTodo(id)
│   ├── editTodo(id, text)
│   └── clearCompleted()
│
├── 렌더링 함수
│   └── render()
│
└── 초기화
    ├── DOMContentLoaded 이벤트 바인딩
    └── loadTodos() + render() 호출
```

---

## 10. Coding Convention Reference

### 10.1 Naming Conventions

| Target | Rule | Example |
|--------|------|---------|
| JS 함수 | camelCase | `addTodo()`, `saveTodos()` |
| JS 변수 | camelCase | `todos`, `currentFilter` |
| HTML id | kebab-case | `todo-input`, `add-btn` |
| CSS class | kebab-case | `todo-item`, `filter-btn` |
| CSS modifier | `--` 구분자 | `todo-item--completed` |

### 10.2 This Feature's Conventions

| Item | Convention |
|------|-----------|
| 상태 접근 | `todos[]` 직접 접근 (단일 파일 범위) |
| DOM 삽입 | `textContent` 사용, `innerHTML` 금지 |
| 이벤트 | `addEventListener` (인라인 `onclick` 금지) |
| 저장 시점 | 상태 변경 함수마다 `saveTodos()` 호출 |

---

## 11. Implementation Guide

### 11.1 File Structure

```
D:\GIT\claude\
├── index.html      ← 마크업만 (스크립트/스타일 링크)
├── style.css       ← 모든 CSS
└── app.js          ← 모든 JS 로직
```

### 11.2 Implementation Order

1. [ ] `index.html` — HTML 골격 작성 (입력창, 목록, 필터, 카운터)
2. [ ] `style.css` — 기본 레이아웃 및 컴포넌트 스타일
3. [ ] `app.js` — 상태 변수 및 스토리지 함수 (`loadTodos`, `saveTodos`)
4. [ ] `app.js` — `render()` 함수 구현
5. [ ] `app.js` — 추가/토글/삭제 이벤트 핸들러
6. [ ] `app.js` — 인라인 수정 (더블클릭 → 입력창 → blur/Enter)
7. [ ] `app.js` — 필터 탭 및 Clear completed
8. [ ] `app.js` — 초기화 (`DOMContentLoaded`)
9. [ ] 브라우저 수동 테스트 (§8 Test Plan 기준)

### 11.3 Session Guide

> 단순 Starter 프로젝트이므로 단일 세션 구현 권장.

#### Module Map

| Module | Scope Key | Description | Estimated Turns |
|--------|-----------|-------------|:---------------:|
| HTML + CSS | `module-1` | index.html 마크업 + style.css 스타일 | 5-8 |
| Core Logic | `module-2` | app.js 상태/스토리지/렌더링/CRUD | 10-15 |
| Filter + Clear | `module-3` | 필터 탭, Clear completed, 카운터 | 5-8 |

#### Recommended Session Plan

| Session | Phase | Scope | Turns |
|---------|-------|-------|:-----:|
| Session 1 | Plan + Design | 전체 | 완료 |
| Session 2 | Do + Check | `--scope module-1,module-2,module-3` | 25-35 |

---

## Version History

| Version | Date | Changes | Author |
|---------|------|---------|--------|
| 0.1 | 2026-05-04 | Initial draft (Option B 선택) | faith79@jobkorea.co.kr |
