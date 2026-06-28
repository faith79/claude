# Analysis: keyboard-save-button-fix

## Match Rate: 100% ✅ (Iteration 1)

## Structural (0.2 weight): 100%
- [x] AndroidManifest.xml에 `android:windowSoftInputMode="adjustResize"` 추가 확인 (line 24)

## Functional (0.4 weight): 100%
- [x] DiaryEditorScreen: Scaffold topBar(TopAppBar 저장 아이콘) + Column imePadding() + verticalScroll 구조 확인
- [x] MemoEditorScreen: Scaffold topBar(TopAppBar 저장 TextButton) + Column imePadding() + verticalScroll 구조 확인
- [x] 키보드 출현 시 창이 pan되지 않고 resize됨 → TopAppBar 화면 상단 고정

## Contract (0.4 weight): 100%
- [x] enableEdgeToEdge() + adjustResize 조합 호환 확인
- [x] imePadding() 이 키보드 높이만큼 하단 패딩 추가 (기존 코드 활용)
- [x] verticalScroll이 내용 영역 스크롤 허용 (기존 코드 활용)

## Gaps Found: 0
## Gaps Fixed: 0

## Result: PASSED
