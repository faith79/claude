# Report: memo-keyboard-scroll

## Summary
키보드가 올라올 때 메모 작성/상세보기 화면에서 하단 콘텐츠가 가려지는 문제를 수정.
`imePadding()` 추가로 IME 인셋을 레이아웃이 처리하도록 변경.

## Root Cause
`enableEdgeToEdge()` 환경에서 Scaffold는 시스템 바 인셋을 처리하지만 IME(키보드) 인셋은 자동 처리하지 않음.
`verticalScroll` 만으로는 키보드가 올라와도 스크롤 영역이 줄어들지 않아 TextField가 가려짐.

## Changes
| File | Location | Change |
|------|----------|--------|
| MemoScreen.kt | MemoEditorScreen Column (line 192) | `.imePadding()` 추가 (verticalScroll 앞) |
| MemoScreen.kt | MemoDetailScreen TEXT Column (line 366) | `.imePadding()` 추가 (verticalScroll 앞) |

## Quality Gate
- Match Rate: 100% ✅
- Iterations: 1
- Build: (see apkBuild below)

## Status: PASSED ✅
