# Design: memo-delete-confirm

## Architecture: Option C — Pragmatic Balance

### MemoScreen.kt

#### MemoListContent (변경)
- `onDeleteMemo: (String) -> Unit` 파라미터 **제거**
- `MemoCard` 호출에서 `onDelete` 제거

#### MemoCard (변경)
- `onDelete: () -> Unit` 파라미터 **제거**
- `IconButton(Delete)` 블록 **제거**
- 카드 전체 클릭(onClick)만 유지

#### MemoDetailScreen (변경)
- 파라미터 추가: `onDelete: () -> Unit`
- 상태 추가: `var showDeleteDialog by remember { mutableStateOf(false) }`
- TopAppBar actions:
  ```
  IconButton(onClick = { showDeleteDialog = true }) {
      Icon(Icons.Default.Delete, "삭제", tint = colorScheme.error)
  }
  TextButton("편집")  ← 기존 유지
  ```
- AlertDialog (showDeleteDialog == true 시):
  ```
  title = "삭제"
  text  = "정말 삭제하시겠습니까?"
  confirmButton = TextButton("예") → deleteMemo + onDelete()
  dismissButton = TextButton("아니오") → showDeleteDialog = false
  onDismissRequest → showDeleteDialog = false
  ```
  - memo == null 이면 삭제 버튼 비노출 (기존 편집 버튼과 동일 조건)

### HomeScreen.kt
- `MemoListContent` 호출에서 `onDeleteMemo = ...` 줄 **제거**

### NavGraph.kt
- MemoDetail composable: `onDelete = { navController.popBackStack() }` 추가

## Key Decisions
- 삭제 로직(memoViewModel.deleteMemo)은 MemoDetailScreen 내부에서 직접 호출 — ViewModel/userId 이미 보유
- onDelete 콜백은 "삭제 완료 후 뒤로가기" 신호만 담당
