# diary-app Planning Document

> **Summary**: Kotlin + Jetpack Compose 기반 안드로이드 일기 앱 — Firebase Auth/Firestore, 감정 태그, 이미지, 검색, 알림
>
> **Project**: claude
> **Version**: 0.1.0
> **Author**: kanggaru@nate.com
> **Date**: 2026-05-04
> **Status**: Draft

---

## Executive Summary

| Perspective | Content |
|-------------|---------|
| **Problem** | 개인 일기를 안전하게 클라우드에 저장하고, 감정·이미지와 함께 기록하는 Android 앱이 없다 |
| **Solution** | Firebase Auth/Firestore 기반 로그인·클라우드 저장 + 감정 태그·이미지 첨부·검색·알림을 갖춘 Kotlin + Jetpack Compose 일기 앱 |
| **Function/UX Effect** | 날짜별 일기 CRUD, 감정 태그로 감정 추적, 이미지 첨부, 글자 검색, 매일 알림 리마인더로 꾸준한 일기 습관 형성 |
| **Core Value** | 로그인 기반 개인 공간에서 감정과 사진을 함께 기록하는 나만의 클라우드 일기장 |

---

## Context Anchor

| Key | Value |
|-----|-------|
| **WHY** | 개인 일기를 클라우드에 안전하게 저장하고 감정·이미지와 함께 기록하고 싶다 |
| **WHO** | 매일 일기를 쓰거나 감정을 기록하고 싶은 Android 사용자 |
| **RISK** | Firebase 비용(무료 한도 초과), 이미지 업로드 Storage 용량, 오프라인 동기화 복잡성 |
| **SUCCESS** | 로그인·로그아웃, 일기 CRUD, 감정 태그 저장, 이미지 첨부, 검색, 일일 알림 모두 동작 |
| **SCOPE** | Phase 1 — Auth + 일기 CRUD + 감정 태그 + 이미지 + 검색 + 알림 (Android 단독) |

---

## 1. Overview

### 1.1 Purpose

사용자가 매일 일기를 작성하고 감정 태그와 이미지를 첨부하여 클라우드에 저장할 수 있는 Android 앱.
Firebase Auth로 개인 공간을 보호하고 Firestore로 기기 간 동기화를 지원한다.

### 1.2 Background

Kotlin + Jetpack Compose를 사용하는 Modern Android 개발 방식으로 구현.
Firebase를 BaaS로 사용하여 백엔드 서버 없이 인증·데이터베이스·스토리지를 처리한다.

### 1.3 Related Documents

- Requirements: 본 문서
- References: Firebase Android SDK 공식 문서

---

## 2. Scope

### 2.1 In Scope

- [x] Firebase Auth (이메일/패스워드 회원가입, 로그인, 로그아웃)
- [x] 일기 작성 (제목 + 내용 + 날짜 자동 설정)
- [x] 일기 수정
- [x] 일기 삭제
- [x] 날짜별 일기 목록 조회
- [x] 감정 태그 (행복/슬픔/분노/평온/설렘 등 5-7가지)
- [x] 이미지 첨부 (1장, Firebase Storage)
- [x] 글자 검색 (제목/내용 로컬 필터)
- [x] 일일 알림 (WorkManager 기반 리마인더)
- [x] Jetpack Compose UI

### 2.2 Out of Scope

- iOS 지원
- 소셜 로그인 (Google/Kakao)
- 다중 이미지 첨부
- 일기 공유/내보내기
- 오프라인 완전 지원 (기본 캐시만)
- 잠금 화면 / 생체 인증

---

## 3. Requirements

### 3.1 Functional Requirements

| ID | Requirement | Priority | Status |
|----|-------------|----------|--------|
| FR-01 | 이메일/패스워드 회원가입 | High | Pending |
| FR-02 | 이메일/패스워드 로그인/로그아웃 | High | Pending |
| FR-03 | 일기 작성 (제목, 내용, 날짜 자동) | High | Pending |
| FR-04 | 일기 수정 | High | Pending |
| FR-05 | 일기 삭제 (확인 다이얼로그) | High | Pending |
| FR-06 | 날짜 내림차순 일기 목록 | High | Pending |
| FR-07 | 감정 태그 선택 (작성/수정 시) | Medium | Pending |
| FR-08 | 이미지 1장 첨부 (갤러리/카메라 선택) | Medium | Pending |
| FR-09 | 이미지 Firebase Storage 업로드/다운로드 | Medium | Pending |
| FR-10 | 제목/내용 글자 검색 (로컬 필터) | Medium | Pending |
| FR-11 | 일일 알림 리마인더 (시간 설정 가능) | Low | Pending |
| FR-12 | 로그인 상태 유지 (앱 재시작 시) | High | Pending |

### 3.2 Non-Functional Requirements

| Category | Criteria | Measurement Method |
|----------|----------|-------------------|
| Performance | 목록 로드 < 2초 | Android Profiler |
| Security | Firebase Rules로 본인 데이터만 접근 | Firestore Rules 검토 |
| UX | Material Design 3 준수 | 시각적 확인 |
| Compatibility | Android 8.0 (API 26) 이상 | `minSdk = 26` |

---

## 4. Success Criteria

### 4.1 Definition of Done

- [ ] FR-01~FR-12 모두 구현 및 에뮬레이터 동작 확인
- [ ] Firebase Auth 로그인·로그아웃 정상 동작
- [ ] Firestore CRUD 정상 동작 (본인 데이터만 접근)
- [ ] 이미지 업로드/표시 정상 동작
- [ ] 감정 태그 저장/표시 정상 동작
- [ ] 검색 필터 동작
- [ ] 일일 알림 수신 확인

### 4.2 Quality Criteria

- [ ] Firestore Security Rules 설정 (인증된 사용자 본인 데이터만)
- [ ] 이미지 로딩 실패 시 placeholder 표시
- [ ] 네트워크 오류 시 사용자 피드백 (Toast/Snackbar)

---

## 5. Risks and Mitigation

| Risk | Impact | Likelihood | Mitigation |
|------|--------|------------|------------|
| Firebase 무료 한도 초과 (Firestore reads) | Medium | Low | 페이지네이션으로 쿼리 최소화 |
| 이미지 Storage 비용 | Medium | Medium | 이미지 압축 후 업로드, 1장 제한 |
| 오프라인 시 데이터 불일치 | Medium | Medium | Firestore 캐시 활성화 |
| Firebase Security Rules 설정 오류 | High | Low | Rules 단위 테스트 작성 |
| Android 권한 (카메라/갤러리) 거부 | Low | Medium | 권한 거부 시 graceful fallback |

---

## 6. Impact Analysis

### 6.1 Changed Resources

| Resource | Type | Change Description |
|----------|------|--------------------|
| Firestore `diaries` collection | Cloud DB | 신규 컬렉션 생성 |
| Firebase Storage `images/` | Cloud Storage | 이미지 버킷 신규 |
| Firebase Auth | Authentication | 신규 앱 등록 |

### 6.2 Current Consumers

신규 프로젝트이므로 기존 소비자 없음.

### 6.3 Verification

- [ ] Firestore Security Rules: `userId == request.auth.uid` 확인
- [ ] Storage Rules: 인증된 사용자만 자신의 폴더 접근

---

## 7. Architecture Considerations

### 7.1 Project Level Selection

| Level | Characteristics | Selected |
|-------|-----------------|:--------:|
| **Starter** | 단순 구조 | ☐ |
| **Dynamic** | Feature 기반 모듈, BaaS 연동 | ✅ |
| **Enterprise** | 레이어 분리, DI, 마이크로서비스 | ☐ |

### 7.2 Key Architectural Decisions

| Decision | Options | Selected | Rationale |
|----------|---------|----------|-----------|
| UI | XML / Jetpack Compose | Jetpack Compose | Modern Android 권장 |
| Architecture | MVC / MVVM / MVI | MVVM | Compose + ViewModel 궁합 |
| State | LiveData / StateFlow | StateFlow | Compose와 자연스러운 연동 |
| DI | Hilt / Koin / Manual | Hilt | Android 공식 권장 |
| Image Loading | Coil / Glide | Coil | Compose 친화적 |
| Backend | Firebase / 커스텀 | Firebase (Auth + Firestore + Storage) | BaaS 빠른 구축 |
| Notification | AlarmManager / WorkManager | WorkManager | 배터리 최적화 |

### 7.3 Clean Architecture Approach

```
Selected Level: Dynamic (MVVM + Firebase)

Package Structure:
app/
├── ui/
│   ├── auth/          ← 로그인/회원가입 화면
│   ├── home/          ← 일기 목록 화면
│   ├── editor/        ← 일기 작성/수정 화면
│   └── detail/        ← 일기 상세 화면
├── viewmodel/
│   ├── AuthViewModel
│   ├── DiaryListViewModel
│   └── DiaryEditorViewModel
├── data/
│   ├── model/         ← DiaryEntry, EmotionTag
│   ├── repository/    ← DiaryRepository, AuthRepository
│   └── firebase/      ← FirestoreDataSource, StorageDataSource
├── notification/      ← DailyReminderWorker
└── di/                ← Hilt Modules
```

---

## 8. Convention Prerequisites

### 8.1 Existing Project Conventions

- [ ] Android 신규 프로젝트 (기존 컨벤션 없음)
- [ ] Kotlin 코딩 컨벤션 적용 예정
- [ ] Material Design 3 가이드라인 준수

### 8.2 Conventions to Define/Verify

| Category | To Define | Priority |
|----------|-----------|:--------:|
| Naming | ViewModel suffix, Repository suffix | High |
| State | UiState sealed class | High |
| Coroutines | viewModelScope + Flow | High |
| Error Handling | Result<T> 래핑 | Medium |

### 8.3 Environment Variables / Config

| Variable | Purpose | 방법 |
|----------|---------|------|
| `google-services.json` | Firebase 프로젝트 연결 | Firebase Console 다운로드 |
| Firebase Project ID | 앱 식별 | Console에서 확인 |

---

## 9. Next Steps

1. [ ] Firebase 프로젝트 생성 및 `google-services.json` 준비
2. [ ] Design 문서 작성 → `/pdca design diary-app`
3. [ ] Android 프로젝트 생성 (Android Studio)
4. [ ] 구현 시작 → `/pdca do diary-app`

---

## Version History

| Version | Date | Changes | Author |
|---------|------|---------|--------|
| 0.1 | 2026-05-04 | Initial draft | faith79@jobkorea.co.kr |
