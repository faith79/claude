# Design: diary-ux-fixes

## Option C — Pragmatic Balance

### SC-01: 화면 회전 잠금
`AndroidManifest.xml` MainActivity에 `android:screenOrientation="portrait"` 추가.

### SC-02: 저장 후 Home 이동 + VM 스코프 통일

**onSaved 변경:**
```kotlin
// Before
onSaved = { navController.popBackStack() }

// After  
onSaved = { navController.popBackStack(Screen.Home.route, inclusive = false) }
```
- `popBackStack(route, false)`: Home까지 전부 팝, Home은 유지

**HomeScreen Activity-스코프 VM:**
```kotlin
composable(Screen.Home.route) {
    val activity = LocalContext.current as ComponentActivity
    val diaryViewModel: DiaryViewModel = hiltViewModel(activity)
    HomeScreen(..., diaryViewModel = diaryViewModel)
}
```
- Detail·Editor와 동일 인스턴스 → invalidateCache가 HomeScreen VM에 적용

### SC-03: invalidateCache 내 Firestore 재로딩

```kotlin
private fun invalidateCache(userId: String, date: String) {
    val yearMonthStr = date.substring(0, 7)
    val yearMonth = YearMonth.of(date.substring(0, 4).toInt(), date.substring(5, 7).toInt())
    val monthKey = "${userId}_${yearMonthStr}"
    val entryKey = "${userId}_${date}"
    memMonthCache.remove(monthKey)
    memEntryCache.remove(entryKey)
    localCache.removeMonth(monthKey)
    localCache.removeEntry(entryKey)
    _entryMap.value = _entryMap.value - date
    _monthlyDiaryMap.value = _monthlyDiaryMap.value - monthKey
    loadMonth(userId, yearMonth)  // 캐시 클리어 후 Firestore 즉시 재조회
}
```

**캐시 히트 없이 Firestore로 가는 이유:**
- `memMonthCache.remove(monthKey)` → L1 비움
- `localCache.removeMonth(monthKey)` → L2 비움
- 따라서 `loadMonth` 호출 시 L1/L2 미스 → 새 스냅샷 리스너 시작 → 신규 일기 포함된 최신 데이터 수신
