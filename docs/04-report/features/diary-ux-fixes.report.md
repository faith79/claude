# Report: diary-ux-fixes — PASSED ✅ (100%)

## 수정된 3가지 이슈

### Issue 1: 저장 후 달력 이동
- **파일**: `NavGraph.kt`
- **변경**: `onSaved = { navController.popBackStack() }` → `navController.popBackStack(Screen.Home.route, inclusive = false)`
- **효과**: Home → Editor 경로든, Home → Detail → Editor 경로든 항상 달력(Home)으로 복귀

### Issue 2: 신규 일기 미표시 (캐시 문제)
- **파일**: `NavGraph.kt` + `DiaryViewModel.kt`
- **근본 원인**: HomeScreen이 NavBackStackEntry-스코프 VM을 사용해서 Editor(Activity-스코프 VM)의 `invalidateCache` 호출이 HomeScreen VM에 미적용
- **변경 1** (`NavGraph.kt`): HomeScreen도 Activity-스코프 VM 주입 → 모든 화면이 동일 VM 인스턴스 공유
- **변경 2** (`DiaryViewModel.kt`): `invalidateCache` 내에 `loadMonth(userId, yearMonth)` 추가 → L1/L2 클리어 직후 Firestore 재조회 시작

### Issue 3: 화면 회전
- **파일**: `AndroidManifest.xml`
- **변경**: `<activity>` 태그에 `android:screenOrientation="portrait"` 추가
- **효과**: 기기를 가로로 돌려도 앱은 세로 고정
