# Plan: diary-bg-color-sync

## Context Anchor
- **WHY**: 글쓰기/수정 화면과 읽기 화면의 배경색이 달라 UX 불일치
- **WHO**: 조이어리 앱 사용자
- **RISK**: 없음 (containerColor 단순 추가, 기존 로직 변경 없음)
- **SUCCESS**: 에디터 전체 배경 = 읽기 화면 배경과 동일
- **SCOPE**: DiaryEditorScreen.kt 1개 파일

[CP-1 Auto] 요구사항 확인됨  
[CP-2 Auto] 합리적 기본값 적용

## 원인 분석

| 화면 | Scaffold containerColor | Column background |
|------|------------------------|-------------------|
| 읽기(DiaryDetailScreen) | `diaryBg` 명시 | 없음 |
| 쓰기/수정(DiaryEditorScreen) | 없음 (시스템 기본) | `diaryBg` |

읽기 화면은 Scaffold 전체(앱바 포함)에 diaryBg 적용.  
쓰기/수정 화면은 Content 영역에만 diaryBg 적용 → 앱바 영역 색상 불일치.

## 요구사항
- **FR-01**: DiaryEditorScreen Scaffold에 `containerColor = diaryBg` 추가
- **FR-02**: Column의 `.background(diaryBg)` 제거 (중복, containerColor가 담당)
- **FR-03**: `val diaryBg` 선언을 Scaffold 이전으로 이동

## Success Criteria
- SC-01: DiaryEditorScreen Scaffold.containerColor = LocalThemeColors.current.diaryBg
- SC-02: Column modifier에서 .background(diaryBg) 제거
- SC-03: diaryBg 변수 Scaffold 바깥에 선언
