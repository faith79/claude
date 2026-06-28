# Plan: login-screen-scroll-title

## Context Anchor
- **WHY**: 키보드 표시 시 로그인 버튼이 가려지는 UX 문제 + 앱 브랜드명 반영
- **WHO**: 일반 사용자 (로그인 화면 진입 시)
- **RISK**: verticalScroll + imePadding 순서 오류 시 패딩 계산 잘못될 수 있음
- **SUCCESS**: 키보드 올라와도 로그인/지문 버튼 스크롤로 접근 가능, 제목 "조이어리" 표시
- **SCOPE**: LoginScreen.kt 단일 파일

## Requirements

| # | 요구사항 | 파일 |
|---|---------|------|
| R-01 | TopAppBar 제목 "내 일기장" → "조이어리" | LoginScreen.kt |
| R-02 | 키보드 등장 시 Column 영역이 스크롤 가능하도록 verticalScroll 추가 | LoginScreen.kt |
| R-03 | 키보드 높이만큼 Column 하단 패딩 추가 (imePadding) | LoginScreen.kt |

## Implementation

1. `import androidx.compose.foundation.rememberScrollState` 추가
2. `import androidx.compose.foundation.verticalScroll` 추가
3. `Text("내 일기장")` → `Text("조이어리")`
4. Column modifier에 `.imePadding().verticalScroll(rememberScrollState())` 추가
5. `verticalArrangement = Arrangement.Center` 제거 (verticalScroll 환경에서 무의미)

[CP-1 Auto] 요구사항 확인됨 → 계속 진행
[CP-2 Auto] 명확화 질문 생략 → 합리적 기본값 적용
