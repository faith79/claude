# memo-save-fix Design

## Fix Design

### MemoViewModel.saveMemo — Before vs After

**Before (fire-and-forget, silent catch):**
```kotlin
fun saveMemo(userId: String, memo: MemoEntry) {
    viewModelScope.launch {
        try {
            memoRepository.saveMemo(userId, memo)
            loadMemos(userId)          // ← fire-and-forget 새 코루틴
        } catch (_: Exception) { }    // ← 전부 무시
    }
}
```

**After (inline reload, error 노출):**
```kotlin
fun saveMemo(userId: String, memo: MemoEntry) {
    viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            memoRepository.saveMemo(userId, memo)
            _memos.value = memoRepository.getMemos(userId)   // inline: 순차 보장
        } catch (e: Exception) {
            _error.value = "저장 실패: ${e.localizedMessage ?: e.javaClass.simpleName}"
        } finally {
            _isLoading.value = false
        }
    }
}
```

### 추가 StateFlow

```kotlin
private val _error = MutableStateFlow<String?>(null)
val error: StateFlow<String?> = _error.asStateFlow()
fun clearError() { _error.value = null }
```

### HomeScreen Snackbar 연결

```kotlin
val error by memoViewModel.error.collectAsStateWithLifecycle()
val snackbarHostState = remember { SnackbarHostState() }

LaunchedEffect(error) {
    error?.let {
        snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
        memoViewModel.clearError()
    }
}

Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }, ...)
```
