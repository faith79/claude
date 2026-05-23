# joyary-upgrade-v10 Design

> **Feature**: joyary-upgrade-v10
> **Date**: 2026-05-23
> **Architecture**: Option A — Minimal (1개 파일, 2줄 변경)

---

## Context Anchor

| 항목 | 내용 |
|------|------|
| **WHY** | DiaryDetailScreen·EditorScreen이 별도 ViewModel 인스턴스를 사용해 캐시 무효화가 Detail에 전달되지 않음 |
| **SCOPE** | NavGraph.kt만 수정. 다른 파일 변경 없음 |

---

## 1. Architecture Decision

### Option A — Minimal (선택)
Activity 스코프 ViewModel만 변경. 기존 v9 로직(invalidateCache, force-refresh, skeleton) 100% 재사용.

**변경 위치**: `NavGraph.kt` — DiaryDetail · DiaryEditor composable 내부

```kotlin
// 기존:
DiaryDetailScreen(..., diaryViewModel = hiltViewModel(), ...)
DiaryEditorScreen(..., diaryViewModel = hiltViewModel(), ...)

// 변경:
val activity = LocalContext.current as ComponentActivity
val diaryViewModel: DiaryViewModel = hiltViewModel(activity)
DiaryDetailScreen(..., diaryViewModel = diaryViewModel, ...)
DiaryEditorScreen(..., diaryViewModel = diaryViewModel, ...)
```

### Option B — NavGraph 상위 hiltViewModel (기각)
NavGraph 상위 composable에서 단일 hiltViewModel 생성 후 전달. NavGraph가 DiaryViewModel 의존성을 직접 보유하게 되어 불필요한 결합.

### Option C — SavedStateHandle/EventBus (기각)
EditorScreen → DetailScreen으로 저장 완료 이벤트를 전달하는 별도 채널 구현. 코드량 증가, 단방향 데이터 흐름 복잡화.

---

## 2. Data Flow (수정 후)

```
DiaryEditorScreen (수정 저장)
  └─ vm_shared.saveDiary().onSuccess
        ├─ invalidateCache(userId, date)     ← 공유 L1 memEntryCache 제거
        ├─ _selectedEntry = null             ← 공유 StateFlow (DetailScreen 관찰중)
        ├─ _isDetailLoading = true           ← 스켈레톤 트리거
        ├─ launch { getDiaryByDate → _selectedEntry = fresh }
        └─ _uiState = Success
              └─ LaunchedEffect → onSaved() → popBackStack()
                    └─ DiaryDetailScreen 복귀
                          ├─ isDetailLoading = true  → DiaryDetailSkeleton 표시
                          └─ (background) _selectedEntry = fresh → 최신 데이터 표시
```

---

## 3. Implementation

### 3.1 NavGraph.kt 수정

`DiaryDetail` composable:
```kotlin
composable(
    route = Screen.DiaryDetail.route,
    arguments = listOf(navArgument("date") { type = NavType.StringType })
) { backStack ->
    val date = backStack.arguments?.getString("date") ?: return@composable
    val activity = LocalContext.current as ComponentActivity
    val diaryViewModel: DiaryViewModel = hiltViewModel(activity)
    DiaryDetailScreen(
        date = date,
        onEdit = { d, id -> navController.navigate(Screen.DiaryEditor.createRoute(d, id)) },
        onBack = { navController.popBackStack() },
        onDeleted = { navController.popBackStack() },
        onAddDiary = { d -> navController.navigate(Screen.DiaryEditor.createRoute(d)) },
        diaryViewModel = diaryViewModel
    )
}
```

`DiaryEditor` composable:
```kotlin
composable(
    route = Screen.DiaryEditor.route,
    arguments = listOf(
        navArgument("date") { type = NavType.StringType },
        navArgument("id") { type = NavType.StringType; defaultValue = "" }
    )
) { backStack ->
    val date = backStack.arguments?.getString("date") ?: return@composable
    val id = backStack.arguments?.getString("id") ?: ""
    val activity = LocalContext.current as ComponentActivity
    val diaryViewModel: DiaryViewModel = hiltViewModel(activity)
    DiaryEditorScreen(
        date = date,
        existingId = id,
        onSaved = { navController.popBackStack() },
        onBack = { navController.popBackStack() },
        diaryViewModel = diaryViewModel
    )
}
```

### 3.2 Import 추가

```kotlin
import androidx.activity.ComponentActivity
```

(이미 SettingsScreen composable에서 import되어 있으므로 중복 없음)

---

## 4. Success Criteria Mapping

| SC | 구현 |
|----|------|
| SC-01 | Activity 스코프 공유 ViewModel → saveDiary force-refresh가 DetailScreen에 전달 |
| SC-02 | v9의 isDetailLoading/skeleton 로직 그대로 사용 |
| SC-03 | DiaryEditorScreen.LaunchedEffect(existingId) → 공유 vm.loadDiaryByDate 호출 → 정상 동작 |
| SC-04 | NavGraph.kt 이외 파일 변경 없음 |
