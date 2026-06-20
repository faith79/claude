# Design: keyboard-save-button-fix

## Architecture: Option C — Pragmatic Balance

### 핵심 변경 (1줄)

**`AndroidManifest.xml` `<activity>` 블록:**
```xml
android:windowSoftInputMode="adjustResize"
```

### 동작 원리

```
adjustPan (현재 문제):
  키보드 출현 → 창 전체가 위로 이동 → TopAppBar 화면 밖으로 나감
  
adjustResize (수정 후):
  키보드 출현 → 창 높이가 줄어듦 → TopAppBar 상단 고정 유지
              → imePadding()이 키보드 높이 = 하단 패딩
              → verticalScroll로 내용 영역 스크롤 가능
```

### Scaffold 레이아웃 (변경 없음)
```
┌─────────────────────────────┐  ← 항상 보임 (고정)
│  ← 뒤로      제목    저장  │  ← TopAppBar (Scaffold pinned)
├─────────────────────────────┤
│  날짜                       │
│  감정 선택                  │  ← 스크롤 가능 (Column + verticalScroll)
│  날씨 선택                  │
│  [텍스트 필드               │
│                             │
│                          ]  │
├─────────────────────────────┤
│   ← 키보드 높이 패딩        │  ← imePadding() 처리
└─────────────────────────────┘
│  [키보드]                   │
└─────────────────────────────┘
```

### 파일별 변경 사항

#### AndroidManifest.xml
- 위치: `<activity android:name=".MainActivity" ...>` 블록
- 추가: `android:windowSoftInputMode="adjustResize"`

#### DiaryEditorScreen.kt — 변경 없음
현재 구조가 이미 올바름:
- Scaffold topBar = TopAppBar (저장 아이콘)
- Column: imePadding() + verticalScroll

#### MemoEditorScreen.kt — 변경 없음
현재 구조가 이미 올바름:
- Scaffold topBar = TopAppBar (저장 TextButton)
- Column: imePadding() + verticalScroll

### 검증 기준
- [ ] 일기 작성 화면: 키보드 출현 시 TopAppBar 저장 버튼 보임
- [ ] 메모 작성 화면: 키보드 출현 시 TopAppBar 저장 버튼 보임
- [ ] 텍스트 입력 중 내용 영역 스크롤 가능
- [ ] 다른 화면(홈, 설정, 로그인) 레이아웃 정상
