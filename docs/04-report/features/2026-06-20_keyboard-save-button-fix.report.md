# Report: keyboard-save-button-fix

## 결과 요약
- **Match Rate**: 100% ✅
- **Status**: PASSED
- **변경 파일**: 1개 (AndroidManifest.xml)

## 문제 원인
`<activity>`에 `android:windowSoftInputMode` 미설정 → 기본값 `adjustPan`  
→ 키보드 출현 시 창 전체가 물리적으로 위로 이동  
→ TopAppBar(저장 버튼)가 화면 상단 밖으로 밀려나 보이지 않음

## 수정 내용

### AndroidManifest.xml
```xml
<activity
    android:name=".MainActivity"
    android:windowSoftInputMode="adjustResize"   ← 추가
    ...>
```

`adjustResize`:
- 창이 pan되지 않고 사용 가능한 높이가 줄어듦
- Scaffold TopAppBar 항상 상단 고정
- `imePadding()` + `verticalScroll()` 이 내용 영역 처리 (기존 코드 활용)

## 개선 효과
| Before | After |
|--------|-------|
| 키보드 올라오면 저장 버튼 화면 밖으로 나감 | 키보드 올라와도 저장 버튼 항상 상단 고정 |
| adjustPan — 창 전체 이동 | adjustResize — 창 높이 축소 |

## 영향 범위
- DiaryEditorScreen (일기 작성) ✅
- MemoEditorScreen (메모 작성) ✅
- 다른 화면: 키보드를 사용하지 않거나 이미 올바르게 동작 중
