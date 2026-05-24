# Plan: skill-test

## Context Anchor
- **WHY**: Joey 파이프라인 end-to-end 검증 (plan→design→do→analyze→report→push→APK)
- **WHO**: 개발자 (파이프라인 자동화 신뢰성 확인)
- **RISK**: 없음 — 소규모 UI 추가
- **SUCCESS**: 파이프라인 전 단계 완주, push 및 APK 빌드 성공
- **SCOPE**: DiaryEditorScreen.kt 한 파일, 글자수 카운터 1줄 추가

## Requirements

### FR-01: 글자수 카운터 표시
- 글쓰기/수정 화면 OutlinedTextField 하단 오른쪽에 현재 글자수 표시
- 형식: `{n}자`
- 색상: `onSurfaceVariant`

## Scope
- **Modified**: `DiaryEditorScreen.kt`
- **Created**: docs 3종
