# diary-tab-memo Design

## Context Anchor

| Key | Value |
|-----|-------|
| WHY | 일기 앱에 메모·할일 통합 |
| WHO | 기존 조이어리 사용자 |
| RISK | HomeScreen 구조 변경 → 일기 기능 회귀 주의 |
| SUCCESS | 탭 전환 + 메모 CRUD 완전 동작 |
| SCOPE | HomeScreen, Memo 레이어 신규 |

## Architecture: Option C — Pragmatic Balance

### 변경 범위

**신규 파일:**
- `data/model/MemoEntry.kt` — MemoEntry, TodoItem, MemoType
- `data/source/MemoDataSource.kt` — Firestore CRUD
- `data/repository/MemoRepository.kt` — 인터페이스
- `data/repository/MemoRepositoryImpl.kt` — 구현체
- `viewmodel/MemoViewModel.kt` — 메모 상태 관리
- `ui/memo/MemoScreen.kt` — MemoListContent, MemoEditorSheet, MemoCard

**수정 파일:**
- `di/RepositoryModule.kt` — MemoRepository 바인딩 추가
- `ui/home/HomeScreen.kt` — NavigationBar + 탭 전환 + MemoViewModel 주입

### Firestore 구조

```
users/{userId}/memos/{memoId}
  - userId: String
  - type: "TEXT" | "TODO"
  - title: String
  - content: String
  - todos: List<Map> [{id, text, isDone}]
  - createdAt: Long
  - updatedAt: Long
```

### HomeScreen 구조

```
Scaffold
  topBar: TopAppBar (조이어리 + 설정)
  bottomBar: NavigationBar
    - 일기 (DateRange 아이콘)
    - 메모장 (Description 아이콘)
  floatingActionButton: tab별 FAB
    - tab 0: 오늘 일기 쓰기 (기존)
    - tab 1: 메모 추가
  content:
    - tab 0: Calendar content (기존 그대로)
    - tab 1: MemoListContent
  overlay: MemoEditorSheet (ModalBottomSheet)
```

### MemoEditorSheet

- FilterChip: 메모 / TODO 타입 선택
- 제목 입력 (선택)
- TEXT 타입: 멀티라인 OutlinedTextField
- TODO 타입: 항목 목록 (Checkbox + 텍스트 + 삭제) + 항목 추가 Row
- 저장 버튼
