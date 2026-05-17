# todo-app Planning Document

> **Summary**: HTML/CSS/JS + localStorage 기반 Todo 앱 — CRUD, 필터, 인라인 수정
>
> **Project**: claude
> **Version**: 0.1.0
> **Author**: faith79@jobkorea.co.kr
> **Date**: 2026-05-04
> **Status**: Draft

---

## Executive Summary

| Perspective | Content |
|-------------|---------|
| **Problem** | 할 일 목록을 간단하게 관리할 수 있는 도구가 없다 |
| **Solution** | 백엔드 없이 브라우저 localStorage만으로 동작하는 경량 Todo 앱 |
| **Function/UX Effect** | 할 일 추가·수정·삭제·완료 토글, All/Active/Completed 필터, 일괄 삭제로 빠른 할 일 관리 |
| **Core Value** | 설치 없이 즉시 사용 가능한 단순하고 직관적인 할 일 관리 |

---

## Context Anchor

> Auto-generated from Executive Summary. Propagated to Design/Do documents for context continuity.

| Key | Value |
|-----|-------|
| **WHY** | 백엔드 없이 브라우저만으로 동작하는 경량 Todo 관리 도구가 필요하다 |
| **WHO** | 간단한 할 일 관리가 필요한 일반 사용자 |
| **RISK** | localStorage 용량 제한(5MB), 브라우저 데이터 삭제 시 데이터 손실 |
| **SUCCESS** | CRUD + 필터 + 인라인 수정 + 일괄 삭제 모두 동작, localStorage 영속성 확인 |
| **SCOPE** | Phase 1 — 단일 HTML 파일 구현 (HTML + CSS + JS), 백엔드 없음 |

---

## 1. Overview

### 1.1 Purpose

할 일(Todo) 항목을 추가·수정·삭제하고, 완료 상태를 관리할 수 있는 단순한 웹 앱.
설치 없이 브라우저에서 바로 사용할 수 있어야 한다.

### 1.2 Background

백엔드나 프레임워크 없이 순수 HTML/CSS/JS로 구현하는 Starter 수준의 프로젝트.
데이터는 브라우저 localStorage에 영속적으로 저장한다.

### 1.3 Related Documents

- Requirements: 본 문서
- References: 없음

---

## 2. Scope

### 2.1 In Scope

- [x] Todo 항목 추가 (Enter 키 또는 버튼)
- [x] Todo 항목 삭제 (개별)
- [x] Todo 항목 인라인 수정 (더블클릭)
- [x] 완료 상태 토글 (체크박스)
- [x] 목록 조회 (전체)
- [x] 필터: All / Active / Completed
- [x] 완료된 항목 일괄 삭제 (Clear completed)
- [x] localStorage 영속성

### 2.2 Out of Scope

- 사용자 인증/계정
- 백엔드 API 연동
- 카테고리 / 태그
- 우선순위 / 마감일
- 드래그&드롭 정렬
- 다크 모드 토글

---

## 3. Requirements

### 3.1 Functional Requirements

| ID | Requirement | Priority | Status |
|----|-------------|----------|--------|
| FR-01 | 텍스트 입력 후 Enter 또는 Add 버튼으로 Todo 추가 | High | Pending |
| FR-02 | 체크박스 클릭으로 완료/미완료 토글 | High | Pending |
| FR-03 | 각 항목의 삭제 버튼(×)으로 개별 삭제 | High | Pending |
| FR-04 | 항목 더블클릭으로 인라인 텍스트 수정, Enter/blur로 저장 | High | Pending |
| FR-05 | All / Active / Completed 필터 탭 | Medium | Pending |
| FR-06 | "Clear completed" 버튼으로 완료 항목 일괄 삭제 | Medium | Pending |
| FR-07 | 남은 항목 수 표시 ("N items left") | Low | Pending |
| FR-08 | localStorage에 저장하여 새로고침 후에도 데이터 유지 | High | Pending |

### 3.2 Non-Functional Requirements

| Category | Criteria | Measurement Method |
|----------|----------|-------------------|
| Performance | 초기 로드 < 1초 | 브라우저 Network 탭 |
| Accessibility | 키보드로 추가/삭제/수정 가능 | 수동 테스트 |
| Compatibility | 최신 Chrome/Firefox/Safari | 브라우저 수동 테스트 |

---

## 4. Success Criteria

### 4.1 Definition of Done

- [ ] FR-01~FR-08 모두 구현 및 동작 확인
- [ ] localStorage 저장/불러오기 정상 동작
- [ ] 필터 전환 시 올바른 항목만 표시
- [ ] 더블클릭 인라인 수정 동작 (Enter/blur 저장)
- [ ] 빈 입력값으로 추가 시도 시 무시
- [ ] 빈 수정값 저장 시도 시 삭제 또는 원복

### 4.2 Quality Criteria

- [ ] 단일 파일(index.html) 또는 최소 파일 구조
- [ ] 외부 의존성 없음 (CDN 허용)
- [ ] 반응형 레이아웃 (모바일 기본 대응)

---

## 5. Risks and Mitigation

| Risk | Impact | Likelihood | Mitigation |
|------|--------|------------|------------|
| localStorage 브라우저 삭제 시 데이터 손실 | Medium | Medium | 사용자에게 데이터 휘발 가능성 안내 (UI 메시지) |
| localStorage 5MB 용량 초과 | Low | Low | Todo 항목 수 제한 없음, 실용적 범위에서 무시 |
| 수정 중 포커스 이탈로 데이터 손실 | Low | Medium | blur 이벤트에서 자동 저장 처리 |

---

## 6. Impact Analysis

### 6.1 Changed Resources

| Resource | Type | Change Description |
|----------|------|--------------------|
| localStorage `todos` key | Browser Storage | Todo 배열 JSON 신규 생성 |

### 6.2 Current Consumers

| Resource | Operation | Code Path | Impact |
|----------|-----------|-----------|--------|
| localStorage `todos` | READ | 초기 로드 시 | None (신규) |
| localStorage `todos` | WRITE | Todo 변경 시마다 | None (신규) |

### 6.3 Verification

- [ ] 신규 프로젝트이므로 기존 소비자 없음

---

## 7. Architecture Considerations

### 7.1 Project Level Selection

| Level | Characteristics | Recommended For | Selected |
|-------|-----------------|-----------------|:--------:|
| **Starter** | Simple structure (`components/`, `lib/`, `types/`) | Static sites, portfolios, landing pages | ✅ |
| **Dynamic** | Feature-based modules, BaaS integration (bkend.ai) | Web apps with backend, SaaS MVPs, fullstack apps | ☐ |
| **Enterprise** | Strict layer separation, DI, microservices | High-traffic systems, complex architectures | ☐ |

### 7.2 Key Architectural Decisions

| Decision | Options | Selected | Rationale |
|----------|---------|----------|-----------|
| Framework | HTML/CSS/JS / React / Vue | HTML/CSS/JS | 의존성 없는 경량 구현 |
| State Management | 전역 변수 배열 | `todos[]` 배열 | 단순 앱에 충분 |
| Storage | localStorage / 메모리 | localStorage | 새로고침 후 데이터 유지 |
| Styling | Tailwind CDN / 직접 작성 CSS | 직접 작성 CSS | 외부 의존성 최소화 |

### 7.3 Clean Architecture Approach

```
Selected Level: Starter

File Structure:
┌─────────────────────────────────────────────────────┐
│ index.html  — 마크업 + 스타일 + 스크립트 (단일 파일)  │
│   또는                                               │
│ index.html                                           │
│ style.css                                            │
│ app.js                                               │
└─────────────────────────────────────────────────────┘

JS 구조 (app.js 또는 인라인):
  - todos[]         : 상태 배열
  - render()        : DOM 렌더링
  - saveTodos()     : localStorage 저장
  - loadTodos()     : localStorage 불러오기
  - 이벤트 핸들러   : add / toggle / delete / edit / filter
```

---

## 8. Convention Prerequisites

### 8.1 Existing Project Conventions

- [ ] `CLAUDE.md` 존재 (신규 저장소, 코딩 컨벤션 미정의)
- [ ] ESLint / Prettier 미설정 (HTML/CSS/JS 프로젝트)

### 8.2 Conventions to Define/Verify

| Category | Current State | To Define | Priority |
|----------|---------------|-----------|:--------:|
| Naming | missing | camelCase JS 변수, kebab-case CSS 클래스 | High |
| Folder structure | missing | 단일 파일 또는 3-파일 분리 | High |

### 8.3 Environment Variables Needed

없음 (순수 프론트엔드, 서버 없음)

---

## 9. Next Steps

1. [ ] Design 문서 작성 (`todo-app.design.md`) — `/pdca design todo-app`
2. [ ] HTML/CSS/JS 구현
3. [ ] 브라우저에서 동작 확인

---

## Version History

| Version | Date | Changes | Author |
|---------|------|---------|--------|
| 0.1 | 2026-05-04 | Initial draft | faith79@jobkorea.co.kr |
