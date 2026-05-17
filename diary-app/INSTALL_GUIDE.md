# 📱 내 일기장 앱 설치 가이드

> 이 설명서를 따라하면 누구나 스마트폰에 일기장 앱을 설치할 수 있어요!

---

## 🧰 시작하기 전에 준비물 확인

아래 4가지가 모두 있어야 해요.

| 준비물 | 설명 |
|---|---|
| 💻 윈도우 컴퓨터 | 앱을 만들 컴퓨터 |
| 📱 안드로이드 스마트폰 | 갤럭시, LG 등 안드로이드 폰 (아이폰은 안 돼요!) |
| 🔌 USB 케이블 | 폰과 컴퓨터를 연결하는 줄 |
| 🌐 인터넷 연결 | 와이파이나 유선 인터넷 |

---

## 1단계: 안드로이드 스튜디오 설치하기

> 안드로이드 스튜디오는 앱을 만드는 특별한 프로그램이에요.

1. 인터넷 브라우저(크롬 등)를 열어요.
2. 주소창에 `developer.android.com/studio` 를 입력하고 Enter를 눌러요.
3. 초록색 **Download Android Studio** 버튼을 클릭해요.
4. 다운로드된 파일(`.exe`)을 더블클릭해서 설치를 시작해요.
5. 설치 중에 나오는 창은 모두 **Next** → **Next** → **Install** → **Finish** 를 눌러요.
6. 설치가 끝나면 안드로이드 스튜디오가 자동으로 열려요.

> ⏳ 안드로이드 스튜디오 설치는 10~20분 정도 걸려요. 기다려요!

---

## 2단계: Firebase 설정하기 (앱의 서버 연결)

> Firebase는 우리 일기를 안전하게 저장해주는 구글의 서비스예요.
> 구글 계정(Gmail)이 필요해요!

### 2-1. Firebase 프로젝트 만들기

1. `console.firebase.google.com` 에 접속해요.
2. 구글 계정으로 로그인해요.
3. **프로젝트 만들기** 버튼을 클릭해요.
4. 프로젝트 이름에 `my-diary-app` 이라고 입력하고 **계속** 을 눌러요.
5. Google 애널리틱스는 **사용 안함**으로 끄고 **프로젝트 만들기** 를 눌러요.
6. 프로젝트가 만들어지면 **계속** 을 눌러요.

### 2-2. 안드로이드 앱 등록하기

1. 프로젝트 홈에서 안드로이드 모양 아이콘 **</>** 을 클릭해요.
2. **Android 패키지 이름** 칸에 아래를 정확히 입력해요:
   ```
   com.example.diaryapp
   ```
3. 앱 닉네임에 `내 일기장` 이라고 입력해요.
4. **앱 등록** 버튼을 클릭해요.

### 2-3. google-services.json 파일 받기

1. **google-services.json 다운로드** 버튼이 나타나면 클릭해서 파일을 받아요.
2. 다운로드된 `google-services.json` 파일을 아래 폴더에 복사해요:
   ```
   diary-app\app\
   ```
   > 📁 탐색기에서 `D:\GIT\claude\diary-app\app\` 폴더를 열고 파일을 붙여넣기 해요.

3. Firebase 설정 창에서 **다음** → **다음** → **콘솔로 이동** 을 눌러요.

### 2-4. Firebase 인증(로그인 기능) 켜기

1. Firebase 콘솔 왼쪽 메뉴에서 **Authentication** 을 클릭해요.
2. **시작하기** 버튼을 클릭해요.
3. **이메일/비밀번호** 를 클릭해요.
4. 첫 번째 스위치를 **켜기** 로 바꾸고 **저장** 을 클릭해요.

### 2-5. Firebase 데이터베이스(저장소) 켜기

1. 왼쪽 메뉴에서 **Firestore Database** 를 클릭해요.
2. **데이터베이스 만들기** 를 클릭해요.
3. **테스트 모드에서 시작** 을 선택하고 **다음** 을 눌러요.
4. 위치는 `asia-northeast3 (서울)` 을 선택하고 **완료** 를 눌러요.

### 2-6. Firebase 저장소(사진) 켜기

1. 왼쪽 메뉴에서 **Storage** 를 클릭해요.
2. **시작하기** 버튼을 클릭해요.
3. **테스트 모드에서 시작** 을 선택하고 **다음** → **완료** 를 눌러요.

---

## 3단계: 프로젝트 열기

1. 안드로이드 스튜디오를 실행해요.
2. **Open** 버튼을 클릭해요.
3. 탐색기에서 `D:\GIT\claude\diary-app` 폴더를 선택하고 **OK** 를 눌러요.
4. 아래쪽에 파란색 진행바가 움직이면 기다려요. (Gradle 동기화 중)

> ⏳ 처음 열 때는 5~15분 정도 걸려요. 진행바가 멈출 때까지 기다려요!

---

## 💻 에뮬레이터로 테스트하기 (실제 폰 없이 바로 실행!)

> 에뮬레이터는 컴퓨터 안에 만들어진 **가상 스마트폰**이에요.
> 실제 폰 없이도 앱이 잘 작동하는지 먼저 확인할 수 있어요!
> 아래 설명은 **Android Studio Panda 4 (2025.3.4)** 기준이에요.

### ① 가상 폰(에뮬레이터) 만들기

1. 안드로이드 스튜디오 오른쪽 세로 메뉴에서 **Device Manager** 아이콘을 클릭해요.

   ```
   오른쪽 세로 패널 아이콘들 중에서 📱 모양을 찾아요.
   안 보이면 위쪽 메뉴 Tools → Device Manager 를 클릭해요.
   ```

2. **Device Manager** 패널이 열리면 왼쪽 위 **＋ (Add)** 버튼을 클릭해요.

3. **Create Virtual Device** 창이 열려요:
   - 왼쪽 Category 에서 **Phone** 이 선택됐는지 확인해요.
   - 목록에서 **Pixel 8** 을 선택해요. (없으면 Pixel 7도 괜찮아요!)
   - **Next** 를 클릭해요.

4. 시스템 이미지 선택 화면이 나와요:
   - **Recommended** 탭에서 맨 위에 있는 항목(API 35 또는 최신)을 선택해요.
   - 옆에 **Download** 링크가 보이면 클릭해서 다운로드해요.
     > ⏳ 다운로드는 5~10분 정도 걸려요!
   - 다운로드가 끝나면 해당 항목을 선택하고 **Next** 를 클릭해요.

5. 가상 폰 이름 확인 화면이 나와요:
   - 이름은 그대로 두고 **Finish** 를 클릭해요.
   - 이제 Device Manager 목록에 가상 폰이 생겼어요! ✅

### ② 에뮬레이터 켜기

1. Device Manager 목록에서 방금 만든 **Pixel 8** 옆에 있는 ▶ (세모) 버튼을 클릭해요.
2. 잠깐 기다리면 컴퓨터 화면에 스마트폰 모양 창이 열려요.

   > ⏳ 처음 켤 때는 1~3분 정도 걸려요!

3. 에뮬레이터 화면에 안드로이드 홈 화면이 보이면 준비 완료예요. 📱

### ③ 앱 실행하기

1. 안드로이드 스튜디오 위쪽 툴바를 봐요:

   ```
   [app ▼]  [Pixel 8 API 35 ▼]  [▶ 실행]  [🐛 디버그]
               ↑ 여기가 에뮬레이터 이름이에요
   ```

2. 가운데 드롭다운에 **Pixel 8** 이 선택돼 있는지 확인해요.
   - 다른 이름이 적혀 있으면 클릭해서 **Pixel 8** 로 바꿔요.

3. 초록색 **▶ (Run)** 버튼을 클릭해요.

4. 아래쪽 **Build** 탭에 진행바가 돌아가요. 기다려요!
   > ⏳ 처음 실행은 2~5분 걸려요.

5. 빌드가 끝나면 에뮬레이터 화면에 **내 일기장** 앱이 자동으로 열려요! 🎉

### ④ 에뮬레이터에서 앱 사용해보기

1. 앱 화면에서 **회원가입** 을 클릭해요.
2. 이메일과 비밀번호를 입력하고 **가입하기** 를 눌러요.
3. 달력 화면이 나오면 성공이에요!
4. 오른쪽 아래 **＋** 버튼을 눌러서 일기를 써봐요.

> 💡 **마우스로 터치처럼 클릭**하면 돼요. 스크롤은 마우스 휠로 할 수 있어요!

### ⚠️ 에뮬레이터 사용 시 주의사항

| 상황 | 설명 |
|---|---|
| 컴퓨터가 느려져요 | 에뮬레이터는 메모리를 많이 써요. 다른 프로그램을 꺼봐요. |
| 검은 화면이 계속 떠요 | 에뮬레이터를 끄고 다시 켜봐요 (▶ 버튼 다시 클릭) |
| RAM이 8GB 이하예요 | 에뮬레이터 대신 실제 폰을 USB로 연결해서 테스트해요 |
| 카메라 기능이 없어요 | 에뮬레이터는 카메라 대신 가상 이미지를 사용해요 |

---

## 4단계: APK 파일 만들기 (앱 파일 생성)

> APK는 안드로이드 앱 설치 파일이에요. (윈도우의 .exe 같은 거예요!)
> 아래 설명은 **Android Studio Panda 4 (2025.3.4)** 기준이에요.

### 4-1. 프로젝트가 준비됐는지 확인하기

1. 안드로이드 스튜디오 위쪽을 보면 망치(🔨) 모양 아이콘이 있어요.
2. 그 옆에 초록색 세모(▶) 버튼이 있으면 준비된 거예요.
3. 아직 아래쪽 **Build** 탭에 파란 진행바가 돌고 있으면 멈출 때까지 기다려요.

### 4-2. APK 빌드하기

1. 화면 맨 위 메뉴에서 **Build** 를 클릭해요.

   ```
   File  Edit  View  Navigate  Code  Analyze  Refactor  Build  Run  Tools  Help
                                                                  ↑ 여기!
   ```

2. 메뉴가 펼쳐지면 **Generate Signed App Bundle or APK...** 를 클릭해요.

   > ⚠️ Panda 버전에서는 메뉴 이름이 바뀌었어요. 예전 버전의 "Build APK(s)" 대신 이걸 눌러요!

3. 팝업 창이 열리면 **APK** 를 선택하고 **Next** 를 눌러요.

4. **키 저장소** 화면이 나와요. 처음이라면:
   - **Create new...** 버튼을 클릭해요.
   - **Key store path** 옆 폴더 아이콘을 눌러 저장 위치를 정해요. (바탕화면 추천)
   - 파일 이름에 `my-diary-key` 라고 입력하고 **OK** 를 눌러요.
   - **Password** 두 칸에 같은 비밀번호를 입력해요. (예: `diary1234`)
   - **Alias** 칸에 `diary` 라고 입력해요.
   - **Key Password** 두 칸에도 같은 비밀번호를 입력해요.
   - 이름(**First and Last Name**)에 아무 이름이나 입력하고 **OK** 를 눌러요.

5. **Next** 를 클릭해요.

6. 빌드 유형 선택 화면이 나와요:
   - **debug** 를 선택해요. (테스트용으로 가장 쉬워요!)
   - **Create** 또는 **Finish** 버튼을 클릭해요.

7. 아래쪽 **Build** 탭에 진행바가 돌아가면 기다려요.

   > ⏳ 처음 빌드는 3~10분 걸릴 수 있어요!

8. 빌드가 끝나면 오른쪽 아래에 풍선 알림이 떠요:

   ```
   ✅ Generate Signed APK
      Module 'app': APK(s) generated successfully for variant 'debug'
                                            [locate] 👈 이걸 클릭!
   ```

9. **locate** 링크를 클릭하면 APK 파일이 있는 폴더가 자동으로 열려요.

### 4-3. APK 파일 위치 확인

빌드된 APK 파일은 아래 위치에 있어요:

```
diary-app\app\release\app-release.apk    ← release 선택했을 때
diary-app\app\debug\app-debug.apk        ← debug 선택했을 때
```

> 💡 **locate 링크가 안 보여요!** 라면?
> 탐색기에서 `D:\GIT\claude\diary-app\app\` 폴더를 직접 열어서
> `debug` 또는 `release` 폴더 안에 있는 `.apk` 파일을 찾아요.

---

## 5단계: APK 파일을 스마트폰으로 옮기기

아래 두 가지 방법 중 편한 걸 선택해요!

### 방법 A: USB 케이블로 옮기기 (추천!)

1. USB 케이블로 스마트폰과 컴퓨터를 연결해요.
2. 폰 화면에 **"USB로 연결"** 알림이 뜨면 탭해요.
3. **파일 전송** 또는 **MTP** 를 선택해요.
4. 컴퓨터 탐색기를 열면 스마트폰이 드라이브로 보여요.
5. APK 파일(`app-debug.apk`)을 복사해서 스마트폰의 **Download** 폴더에 붙여넣기 해요.

### 방법 B: 카카오톡으로 옮기기

1. 컴퓨터 카카오톡을 열어요.
2. 나에게 보내기(내 채팅방)를 열어요.
3. 파일 첨부 버튼(📎)을 눌러 APK 파일을 전송해요.
4. 스마트폰 카카오톡에서 파일을 다운로드해요.

---

## 6단계: 스마트폰에서 설치 허용하기

> 폰에서 외부 앱을 설치하려면 먼저 허락을 해줘야 해요.

### 갤럭시(삼성) 폰의 경우

1. **설정** 앱을 열어요.
2. **앱** 을 탭해요.
3. 오른쪽 위 **⋮ (점 세 개)** 를 탭해요.
4. **특별한 앱 액세스** 를 탭해요.
5. **알 수 없는 앱 설치** 를 탭해요.
6. **내 파일** 앱을 선택해요.
7. **이 출처 허용** 스위치를 켜요. ✅

### 다른 안드로이드 폰의 경우

1. **설정** → **보안** → **알 수 없는 출처** 스위치를 켜요. ✅

---

## 7단계: APK 설치하기

1. 스마트폰에서 **내 파일** (또는 파일 관리자) 앱을 열어요.
2. **Download** 폴더를 열어요.
3. `app-debug.apk` 파일을 탭해요.
4. **설치** 버튼을 탭해요.
5. 잠깐 기다리면 **"앱이 설치되었습니다"** 메시지가 나와요.
6. **열기** 를 탭하면 앱이 실행돼요! 🎉

---

## 8단계: 앱 사용하기

1. 앱이 열리면 **회원가입** 버튼을 탭해요.
2. 이메일 주소와 비밀번호(6자 이상)를 입력해요.
3. **가입하기** 를 탭해요.
4. 로그인이 되면 달력 화면이 나와요.
5. 오른쪽 아래 **＋ 버튼** 을 탭해서 오늘 일기를 써요!

---

## ❓ 문제가 생겼을 때

| 증상 | 해결 방법 |
|---|---|
| 빌드 중 빨간 오류 | `google-services.json` 파일이 `diary-app\app\` 폴더 안에 있는지 확인해요 |
| "Gradle sync failed" 오류 | 인터넷 연결을 확인하고 **File → Sync Project with Gradle Files** 를 클릭해요 |
| APK 설치가 안 될 때 | 6단계에서 "알 수 없는 앱 설치" 허용을 다시 확인해요 |
| 앱이 튕길 때 | Firebase 설정(2단계)을 다시 확인해요 |
| 로그인이 안 될 때 | Firebase Authentication이 켜져 있는지 확인해요 |

---

## 💥 앱이 실행하자마자 꺼질 때 (크래시 진단 가이드)

> 앱이 켜지자마자 바로 꺼지는 건 **크래시(Crash)** 라고 해요.
> 안드로이드 스튜디오의 **Logcat** 을 보면 정확한 원인을 알 수 있어요!

---

### 1단계: Logcat 열어서 오류 찾기

> Logcat은 앱이 무슨 문제로 꺼졌는지 기록을 보여주는 창이에요.

1. 안드로이드 스튜디오 아래쪽 탭에서 **Logcat** 을 클릭해요.
   - 안 보이면 메뉴 **View → Tool Windows → Logcat** 을 클릭해요.

2. 에뮬레이터에서 앱을 다시 실행해요 (▶ 버튼).

3. 앱이 꺼지는 순간, Logcat에 빨간 글씨들이 쏟아져요.

4. 빨간 글씨 중에서 **FATAL EXCEPTION** 또는 **AndroidRuntime** 이라고 적힌 부분을 찾아요.

   ```
   E  AndroidRuntime: FATAL EXCEPTION: main
   E  AndroidRuntime: Process: com.example.diaryapp
   E  AndroidRuntime: java.lang.RuntimeException: ← 이 줄이 핵심 오류예요!
   ```

5. **핵심 오류 메시지**를 복사해서 아래 표에서 찾아요.

---

### 2단계: 오류 메시지별 해결 방법

#### 🔴 오류 1: google-services.json 관련

```
FirebaseApp initialization unsuccessful
또는
Default FirebaseApp is not initialized
```

**원인:** `google-services.json` 파일이 없거나 위치가 틀렸어요.

**해결 방법:**
1. Firebase 콘솔(`console.firebase.google.com`)에서 `google-services.json` 을 다시 다운로드해요.
2. 탐색기에서 `D:\GIT\claude\diary-app\app\` 폴더 안에 붙여넣기 해요.
   ```
   diary-app\
   └── app\
       └── google-services.json  ← 여기에 있어야 해요!
   ```
3. **File → Sync Project with Gradle Files** 를 클릭해요.
4. 다시 ▶ 실행해요.

---

#### 🔴 오류 2: Hilt 의존성 주입 관련

```
Hilt_DiaryApp is not found
또는
MissingBinding / DaggerHiltAndroidApp
또는
Application class not set
```

**원인:** `DiaryApp` 클래스가 `AndroidManifest.xml` 에 제대로 등록되지 않았어요.

**해결 방법:**
1. `diary-app\app\src\main\AndroidManifest.xml` 파일을 열어요.
2. `<application` 태그에 아래가 있는지 확인해요:
   ```xml
   android:name=".DiaryApp"
   ```
3. 없으면 추가하고 저장해요.
4. **Build → Clean Project** → **Build → Rebuild Project** 순으로 클릭해요.

---

#### 🔴 오류 3: WorkManager 관련

```
WorkManager is not initialized properly
또는
IllegalStateException: WorkManager is already initialized
```

**원인:** WorkManager 초기화가 중복됐어요.

**해결 방법:**
1. `AndroidManifest.xml` 에 아래 코드가 있는지 확인해요:
   ```xml
   <provider
       android:name="androidx.startup.InitializationProvider"
       android:authorities="${applicationId}.androidx-startup"
       android:exported="false"
       tools:node="merge">
       <meta-data
           android:name="androidx.work.WorkManagerInitializer"
           android:value="androidx.startup"
           tools:node="remove" />
   </provider>
   ```
2. 없으면 `</application>` 태그 바로 위에 추가해요.
3. 다시 **Build → Rebuild Project** 를 클릭해요.

---

#### 🔴 오류 4: 인터넷 권한 관련

```
SecurityException: Permission denied (missing INTERNET permission?)
```

**원인:** 인터넷 권한이 빠져 있어요.

**해결 방법:**
1. `AndroidManifest.xml` 파일 맨 위 `<manifest>` 바로 아래에 있는지 확인해요:
   ```xml
   <uses-permission android:name="android.permission.INTERNET" />
   ```
2. 없으면 추가하고 저장 후 다시 실행해요.

---

#### 🔴 오류 5: 메모리 부족 관련

```
OutOfMemoryError
또는
GC overhead limit exceeded
```

**원인:** 에뮬레이터나 Gradle 에 메모리가 부족해요.

**해결 방법:**
1. `diary-app\gradle.properties` 파일을 열어요.
2. 아래 줄을 찾아요:
   ```
   org.gradle.jvmargs=-Xmx4g
   ```
3. 컴퓨터 RAM이 **8GB 이하** 라면 `4g` → `2g` 로 바꿔요:
   ```
   org.gradle.jvmargs=-Xmx2g -XX:MaxMetaspaceSize=512m
   ```
4. 에뮬레이터도 RAM 설정을 낮춰요:
   - **Device Manager** → 가상 폰 옆 ✏️ (연필) 아이콘 클릭
   - **Show Advanced Settings** 클릭
   - **RAM** 을 `2048 MB` → `1536 MB` 로 줄여요.

---

#### 🔴 오류 6: 테마/리소스 관련

```
android.content.res.Resources$NotFoundException
또는
Resource ID not found
```

**원인:** 리소스 파일(아이콘, 색상 등)이 없어요.

**해결 방법:**
1. **Build → Clean Project** 를 클릭해요.
2. **File → Invalidate Caches** 를 클릭해요.
3. **Invalidate and Restart** 를 클릭해요.
4. 안드로이드 스튜디오가 재시작되면 다시 ▶ 실행해요.

---

### 3단계: 그래도 모르겠을 때 — 오류 메시지 통째로 복사하기

1. Logcat에서 빨간 글씨 영역을 마우스로 드래그해서 전체 선택해요.
2. 오른쪽 클릭 → **Copy** 를 눌러요.
3. 복사한 내용을 Claude(AI)에게 붙여넣기하고 물어봐요:
   ```
   안드로이드 앱이 이런 오류로 꺼져요. 해결 방법 알려줘:
   [여기에 오류 붙여넣기]
   ```

> 💡 **Logcat 필터 팁:** 오류가 너무 많이 나오면 Logcat 검색창에
> `com.example.diaryapp` 을 입력하면 우리 앱 오류만 걸러져요!

---

## 🎊 완성!

이제 세상에 하나뿐인 나만의 일기장 앱을 스마트폰에서 사용할 수 있어요!

매일 감정 이모지와 함께 일기를 기록하고, 사진도 붙여보세요. 📔✨
