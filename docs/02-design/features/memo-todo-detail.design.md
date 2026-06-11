# Design: memo-todo-detail

## Architecture: Option C — Pragmatic Balance

기존 네비게이션 패턴(Screen sealed class + NavGraph composable)을 그대로 유지하며 최소 변경으로 요구사항 구현.

---

## §1. Screen.kt 변경

```kotlin
object MemoDetail : Screen("memo_detail/{id}") {
    fun createRoute(id: String) = "memo_detail/$id"
}
```

---

## §2. MemoViewModel.kt — toggleTodoItem 추가

optimistic update 방식: 로컬 상태 먼저 업데이트 후 Firestore 저장. 실패 시 원본으로 복원.

```kotlin
fun toggleTodoItem(userId: String, memoId: String, todoId: String) {
    viewModelScope.launch {
        val memo = _memos.value.find { it.id == memoId } ?: return@launch
        val updated = memo.copy(
            todos = memo.todos.map { if (it.id == todoId) it.copy(isDone = !it.isDone) else it },
            updatedAt = System.currentTimeMillis()
        )
        _memos.value = _memos.value.map { if (it.id == memoId) updated else it }
        try {
            memoRepository.saveMemo(userId, updated)
        } catch (e: Exception) {
            _memos.value = _memos.value.map { if (it.id == memoId) memo else it }
            _error.value = "저장 실패: ${e.localizedMessage ?: e.javaClass.simpleName}"
        }
    }
}
```

---

## §3. MemoCard — 제목만 표시 (FR-01)

- content 미리보기 블록 제거
- todos 미리보기 블록 제거
- 제목 없는 경우: TEXT→"(내용 없음)", TODO→"${todos.size}개 항목"

```
[TODO] 오늘 할 일          [🗑]
[메모] 회의록              [🗑]
[메모] (내용 없음)         [🗑]
```

---

## §4. MemoDetailScreen (FR-02, FR-03, FR-04, FR-05)

### 화면 구조

```
TopAppBar: [← 뒤로] "메모 상세" / "TODO 상세"    [✏️ 편집]
─────────────────────────────────────────────────
[메모] 또는 [TODO] 배지
제목 (titleLarge)

── TEXT 타입 ──
내용 (bodyMedium, scrollable, selectable)

── TODO 타입 ──
LazyColumn:
  Row: Checkbox + Text (item.text, strikethrough if done)
  ...
  완료 N/전체 M 요약
```

### 컴포넌트 시그니처

```kotlin
@Composable
fun MemoDetailScreen(
    memoId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    memoViewModel: MemoViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
)
```

- memos 로드: `LaunchedEffect(userId)` — 목록 비어있을 때만 loadMemos
- `memo = memos.find { it.id == memoId }` — null이면 "메모를 찾을 수 없습니다" 표시

---

## §5. NavGraph.kt 변경

1. `onEditMemo` → `Screen.MemoDetail.createRoute(id)` 로 변경 (HomeScreen 호출부)
2. `MemoDetail` composable destination 추가:
   - 경로: `memo_detail/{id}`
   - `onEdit = { id -> navController.navigate(Screen.MemoEditor.createRoute(id)) }`
   - `onBack = { navController.popBackStack() }`

---

## §6. 파일별 변경 요약

| 파일 | 변경 라인 수 (예상) |
|------|-----------------|
| Screen.kt | +4 |
| MemoViewModel.kt | +18 |
| MemoScreen.kt (MemoCard) | -20 (내용 미리보기 제거) |
| MemoScreen.kt (MemoDetailScreen) | +90 (신규) |
| NavGraph.kt | +20 |
