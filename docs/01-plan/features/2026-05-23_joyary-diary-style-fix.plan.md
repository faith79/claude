# Plan: joyary-diary-style-fix (v2)

## Context Anchor
- **WHY**: 에디터 화면이 다크모드에서 밝은 크림 배경을 강제 적용하여 상세보기와 시각적 불일치 발생. 설정화면 콘텐츠가 길어도 스크롤 불가.
- **WHO**: 조이어리 앱 사용자 (다크모드 사용자 포함)
- **RISK**: OutlinedTextField 텍스트색 하드코딩 제거 시 테마 색상 자동 적용 — 다크모드에서 밝은 텍스트로 자동 변환
- **SUCCESS**: 에디터 배경이 상세보기와 동일하게 시스템 테마 따라감. 설정화면에서 스크롤 + 스크롤바 표시
- **SCOPE**: DiaryEditorScreen.kt, SettingsScreen.kt 2파일 수정

## Requirements

### SC-01: 에디터 배경색 = 상세보기 배경색
- 상세보기(DiaryDetailScreen) 내부 Scaffold는 containerColor 미설정 → MaterialTheme.colorScheme.background 사용
- 에디터(DiaryEditorScreen) Scaffold에서 `containerColor = diaryBg` 제거
- OutlinedTextField의 하드코딩된 `Color(0xFF212121)` 텍스트 색상 제거 → 테마 자동 적용
- 미사용 import 제거: LocalThemeColors, Color

### SC-02: 설정화면 스크롤바
- Scaffold 콘텐츠 Column에 `rememberScrollState()` + `verticalScroll()` 추가
- `drawWithContent`로 오른쪽 가장자리에 스크롤바 인디케이터 렌더링
- 스크롤 중: alpha 0.7, 정지 중: alpha 0.3
- 스크롤 불필요할 경우(maxValue=0) 표시 안 함

## Changes
- `DiaryEditorScreen.kt`: containerColor=diaryBg 제거, OutlinedTextField colors 제거, 미사용 import 정리
- `SettingsScreen.kt`: verticalScroll + scrollState + drawWithContent scrollbar 추가
