# Plan: firestore-permission-fix

## WHY
Firebase Firestore/Storage test mode rules가 30일 후 만료되어 모든 쓰기 요청이 `PERMISSION_DENIED`로 거부됨.
앱 내 일기 저장(신규/수정) 및 이미지 업로드가 전혀 동작하지 않음.

## WHO
- 조이어리 앱 사용자 (로그인 후 일기 저장 시도)

## RISK
- 규칙 배포 없이는 저장 기능 완전 불능
- 너무 느슨한 규칙(allow all) 적용 시 데이터 노출 위험

## SUCCESS
- 인증된 사용자가 자신의 일기만 CRUD 가능
- 인증된 사용자가 자신의 이미지만 업로드/삭제 가능
- 다른 사용자 데이터 접근 차단

## SCOPE
- firestore.rules 신규 생성
- storage.rules 신규 생성
- firebase.json 신규 생성 (Firebase CLI 배포용)
- 코드 변경 없음 (앱 로직은 정상)

## SC
- SC-01: Firestore rules — diaries 컬렉션, userId == request.auth.uid 검증
- SC-02: Storage rules — images/{userId}/** 경로, auth.uid == userId 검증
- SC-03: firebase.json — 로컬 rules 파일 연결
