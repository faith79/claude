# Plan: memo-todo-detail

## Context Anchor
- **WHY**: 메모 리스트가 내용을 노출해 복잡해 보임. 클릭 시 바로 편집 모드가 열려 실수로 수정될 위험. TODO 완료 체크를 상세보기에서 빠르게 할 수 없음.
- **WHO**: 조이어리 앱 사용자 (메모/할 일 관리)
- **RISK**: 기존 `onEditMemo` 콜백이 여러 곳에서 사용 중 — NavGraph 수정 범위 제한
- **SUCCESS**: (1) 리스트=제목만, (2) 클릭→상세화면, (3) 상세에서 TODO 체크 가능
- **SCOPE**: MemoScreen.kt, Screen.kt, NavGraph.kt, MemoViewModel.kt

## Requirements

| # | 요구사항 | 수용 기준 |
|---|---------|---------|
| FR-01 | 메모 리스트 카드에 제목만 표시 | content 미리보기 및 todo 항목 미리보기 제거 |
| FR-02 | 메모/투두 카드 클릭 시 상세 화면 이동 | `MemoDetailScreen` 신규 화면으로 내비게이션 |
| FR-03 | 상세 화면에서 TEXT 메모 전체 내용 표시 | 스크롤 가능한 읽기 전용 뷰 |
| FR-04 | 상세 화면에서 TODO 항목 완료 체크 가능 | Checkbox 토글 → Firestore 즉시 저장 |
| FR-05 | 상세 화면에서 편집 버튼으로 에디터 이동 | TopAppBar 편집 버튼 → MemoEditorScreen |

## Affected Files

| 파일 | 변경 유형 | 내용 |
|------|---------|------|
| `navigation/Screen.kt` | 수정 | `MemoDetail` route 추가 |
| `navigation/NavGraph.kt` | 수정 | MemoDetail composable destination 추가, onEditMemo → Detail |
| `ui/memo/MemoScreen.kt` | 수정 | MemoCard 제목만 표시, MemoDetailScreen 신규 추가 |
| `viewmodel/MemoViewModel.kt` | 수정 | `toggleTodoItem` 함수 추가 (optimistic update) |
