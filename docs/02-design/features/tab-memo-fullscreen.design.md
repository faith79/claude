# Design: tab-memo-fullscreen

## Context Anchor
WHY: BottomSheet 불안정 + 탭 높이 낭비 | SCOPE: 4파일 변경

## 변경 설계 (Option C — Pragmatic Balance)

### 1. NavigationBar 라벨 제거 + 높이 압축
```
NavigationBar(modifier = Modifier.height(56.dp)) {
    NavigationBarItem(icon=DateRange, selected, onClick)   // label 없음
    NavigationBarItem(icon=Description, selected, onClick) // label 없음
}
```

### 2. Screen.kt — MemoEditor 라우트 추가
```
object MemoEditor : Screen("memo_editor?id={id}") {
    fun createRoute(id: String = "") = "memo_editor?id=$id"
}
```

### 3. MemoEditorScreen (MemoScreen.kt에 추가, MemoEditorSheet 교체)
- `Scaffold` + `TopAppBar(뒤로가기 + "저장" TextButton)`
- 콘텐츠: `Column(Modifier.verticalScroll(rememberScrollState()))`
- Activity-scoped MemoViewModel + AuthViewModel 사용

### 4. NavGraph.kt — HomeScreen 콜백 + MemoEditor composable
```
HomeScreen(onAddMemo = { navController.navigate(Screen.MemoEditor.createRoute()) },
           onEditMemo = { id -> navController.navigate(Screen.MemoEditor.createRoute(id)) })

composable(Screen.MemoEditor.route) {
    val memoViewModel = hiltViewModel<MemoViewModel>(activity)
    MemoEditorScreen(memoId=id, onBack={ navController.popBackStack() }, memoViewModel=memoViewModel)
}
```

### 5. HomeScreen.kt
- `onAddMemo`, `onEditMemo` 콜백 파라미터 추가
- `showMemoEditor`, `editingMemo` 상태 제거
- `MemoEditorSheet` 블록 제거
- FAB → `onAddMemo()` 호출
- MemoListContent `onEditMemo` → `{ memo -> onEditMemo(memo.id) }`
