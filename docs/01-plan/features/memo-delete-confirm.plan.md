# Plan: memo-delete-confirm

## Context Anchor
- **WHY**: 리스트에서 실수로 삭제하는 UX 문제 해소 + 상세보기에서 삭제 위치 이동으로 의도적 삭제 유도
- **WHO**: 조이어리 앱 메모 탭 사용자
- **RISK**: MemoListContent의 onDeleteMemo 파라미터 제거 시 HomeScreen 컴파일 오류 가능
- **SUCCESS**: 리스트 카드에 휴지통 없음 / 상세보기 TopAppBar에 휴지통 있음 / 삭제 확인 AlertDialog 노출
- **SCOPE**: MemoScreen.kt, HomeScreen.kt, NavGraph.kt 3개 파일만 수정

## Requirements
- FR-01: MemoCard (리스트 아이템)에서 삭제 IconButton 제거
- FR-02: MemoDetailScreen TopAppBar actions에 삭제 IconButton(휴지통) 추가
- FR-03: 삭제 버튼 클릭 시 AlertDialog 표시 — "정말 삭제하시겠습니까?" + 예/아니오
- FR-04: "예" 클릭 시 memoViewModel.deleteMemo 호출 후 뒤로 이동
- FR-05: "아니오" / 바깥 클릭 시 다이얼로그 닫기만

## Affected Files
| File | Change |
|------|--------|
| MemoScreen.kt | MemoCard delete 제거 / MemoDetailScreen onDelete+dialog 추가 |
| HomeScreen.kt | onDeleteMemo 파라미터 제거 |
| NavGraph.kt | MemoDetailScreen onDelete 콜백 추가 |
