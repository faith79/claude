# Analysis: diary-ux-fixes

## Match Rate: 100% ✅

| Axis | Score | Notes |
|------|-------|-------|
| Structural (20%) | 100% | 3개 파일 모두 정확히 수정 |
| Functional (40%) | 100% | 3가지 이슈 모두 해결 |
| Contract (40%) | 100% | API 시그니처, route 값, key 포맷 일치 |
| **Overall** | **100%** | |

## 수정 요약

| Issue | 원인 | 수정 |
|-------|------|------|
| 1. 저장 후 달력 이동 | `popBackStack()` → 이전 화면(Detail)으로 복귀 | `popBackStack(Screen.Home.route, false)` → 항상 Home 복귀 |
| 2. 신규 일기 미표시 | HomeScreen VM(NavEntry) ≠ Editor VM(Activity) → invalidateCache 미적용 + loadMonth 미호출 | HomeScreen도 Activity-스코프 VM 사용 + invalidateCache 내 loadMonth 추가 |
| 3. 화면 회전 | Manifest에 screenOrientation 미설정 | `android:screenOrientation="portrait"` 추가 |

## 변경 파일
- `AndroidManifest.xml`: screenOrientation="portrait"
- `NavGraph.kt`: HomeScreen Activity-scoped VM, onSaved → popBackStack(Home)
- `DiaryViewModel.kt`: invalidateCache + loadMonth 재조회
