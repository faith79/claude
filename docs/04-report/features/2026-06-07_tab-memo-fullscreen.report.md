# Report: tab-memo-fullscreen

## Executive Summary

| 관점 | 내용 |
|------|------|
| Problem | 하단 탭 라벨 높이 낭비 + ModalBottomSheet 불안정 닫힘 |
| Solution | 탭 라벨 제거·56dp + 전체화면 Scaffold 에디터 + verticalScroll |
| UX Effect | 달력 더 넓게, 메모 편집 일기와 동일한 전체화면 UX |
| Core Value | 일관된 편집 경험, 긴 메모도 스크롤로 편리하게 작성 |

## Success Criteria

| 기준 | 상태 | 근거 |
|------|------|------|
| 탭 라벨('일기','메모장') 없음 | ✅ | NavigationBarItem label 파라미터 제거 |
| 탭 높이 56dp | ✅ | NavigationBar(modifier=Modifier.height(56.dp)) |
| 에디터 전체화면 | ✅ | ModalBottomSheet → Scaffold + TopAppBar |
| 에디터 스크롤 | ✅ | Column(Modifier.verticalScroll(rememberScrollState())) |
| APK 빌드 성공 | ✅ | BUILD SUCCESSFUL in 11s |

Overall: **5/5 (100%)** ✅

## Files Changed

| 파일 | 변경 내용 |
|------|----------|
| `navigation/Screen.kt` | MemoEditor 라우트 추가 |
| `ui/memo/MemoScreen.kt` | MemoEditorSheet 제거 → MemoEditorScreen 전체화면 추가 |
| `ui/home/HomeScreen.kt` | 탭 라벨 제거·56dp, onAddMemo/onEditMemo 콜백, MemoEditorSheet 제거 |
| `navigation/NavGraph.kt` | HomeScreen 콜백 연결, MemoEditor composable 추가 |
