# Claude AI로 만드는 앱 모음

Claude AI(claude.ai/code)를 활용해 개발한 앱들을 모아놓은 저장소입니다.

---

## 앱 목록

### 1. Diary App (`diary-app/`)

감정과 이미지를 함께 기록하는 **Android 일기 앱**입니다.

| 항목 | 내용 |
|------|------|
| 플랫폼 | Android (minSdk 26 / targetSdk 35) |
| 버전 | v0.1.0 |
| 언어 | Kotlin |
| UI | Jetpack Compose + Material 3 |
| 백엔드 | Firebase Auth · Firestore · Storage |
| 아키텍처 | MVVM + Hilt (DI) + Repository 패턴 |

**주요 기능**

- 이메일 회원가입 / 로그인
- 일기 작성·조회·수정·삭제 (CRUD)
- 감정 태그 선택 (행복 / 슬픔 / 분노 / 평온 / 설렘 / 불안 / 피곤)
- 이미지 첨부 (Firebase Storage)
- 매일 알림 (WorkManager 기반 리마인더)
- 설정 화면 (알림 시간 등 사용자 설정)

---

## 기술 스택 공통 사항

- **개발 도구**: Claude Code CLI (claude.ai/code)
- **버전 관리**: Git + GitHub