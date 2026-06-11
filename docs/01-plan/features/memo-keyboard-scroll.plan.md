# Plan: memo-keyboard-scroll

## WHY
키보드가 올라올 때 MemoEditorScreen / MemoDetailScreen 하단 콘텐츠가 가려짐.
`enableEdgeToEdge()` 적용 상태에서 IME 인셋을 레이아웃이 처리하지 않기 때문.

## WHO
메모 작성 및 상세보기 사용 유저 전체

## RISK
- `imePadding()` 적용 순서가 잘못되면 스크롤 영역이 잘못 계산됨
- Scaffold `contentWindowInsets`와 충돌 가능 → verticalScroll 앞에 배치해 해결

## SUCCESS
- 키보드 올라올 때 TextField 및 콘텐츠가 키보드 위로 스크롤 가능
- 기존 스크롤 동작 유지

## SCOPE
- `MemoEditorScreen` Column modifier에 `imePadding()` 추가
- `MemoDetailScreen` TEXT 타입 Column modifier에 `imePadding()` 추가
- AndroidManifest, MainActivity 변경 없음
