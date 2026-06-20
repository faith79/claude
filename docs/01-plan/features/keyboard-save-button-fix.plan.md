# Plan: keyboard-save-button-fix

## Context Anchor
- **WHY**: 키보드가 올라오면 창 전체가 위로 pan되어 TopAppBar(저장 버튼)가 화면 밖으로 사라짐
- **WHO**: 일기/메모 작성 중인 사용자
- **RISK**: windowSoftInputMode 변경이 다른 화면에도 영향 → 모든 화면에서 edge-to-edge + imePadding 조합이 이미 준비되어 있으므로 안전
- **SUCCESS**: 키보드가 올라와도 저장 버튼이 항상 보임, 내용 영역은 스크롤 가능
- **SCOPE**: AndroidManifest.xml windowSoftInputMode 설정 추가 (1줄 변경)

## Root Cause
- `<activity>`에 `android:windowSoftInputMode` 미설정 → 기본값 `adjustPan`
- `adjustPan`: Android가 창 전체를 물리적으로 위로 이동시킴 → TopAppBar가 화면 밖으로 나감
- `enableEdgeToEdge()` + `imePadding()` 조합은 `adjustResize`를 전제로 설계됨

## Target Screens
- `DiaryEditorScreen.kt` — 일기 작성 (TopAppBar Check 아이콘 = 저장)
- `MemoEditorScreen.kt` — 메모 작성 (TopAppBar "저장" TextButton)

## Solution
`android:windowSoftInputMode="adjustResize"` 추가:
- 창이 pan되지 않고 사용 가능한 높이가 줄어듦
- Scaffold의 TopAppBar는 항상 상단에 고정
- `imePadding()`이 키보드 높이만큼 하단 패딩 추가
- `verticalScroll`로 내용 스크롤 가능

## Files Changed
- `diary-app/app/src/main/AndroidManifest.xml` (1줄 추가)

[CP-1 Auto] 요구사항 확인됨
[CP-2 Auto] 합리적 기본값 적용
