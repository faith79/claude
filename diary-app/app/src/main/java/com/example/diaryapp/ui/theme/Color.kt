package com.example.diaryapp.ui.theme

import androidx.compose.ui.graphics.Color

// Design Ref: §4.1 — SkyBlue 파스텔 팔레트 (FR-01)
val SkyBlue          = Color(0xFF7EC8E3)  // Primary
val SkyBlueLight     = Color(0xFFB3E5FC)  // PrimaryContainer
val SkyMint          = Color(0xFFA8DADC)  // Secondary
val SkyBluePale      = Color(0xFFE1F5FE)  // SecondaryContainer
val SkyLavender      = Color(0xFFD4E6F1)  // Tertiary

// 배경색 — 달력과 앱 배경 구분 (FR-07)
val SkyBackground    = Color(0xFFF0F8FF)  // 앱 배경 (AliceBlue)
val SkySurface       = Color(0xFFFFFFFF)  // 일반 Surface
// Design Ref: joyary-upgrade-v3 §3.2 — FR-01: #E8F4FD→#8EC6E6 (평일 글씨 가시성 향상)
val SkyCalendarBg    = Color(0xFF8EC6E6)  // 달력 카드 배경 (진한 하늘색)

// Design Ref: joyary-upgrade-v3 §3.3 — 테마 색상 팔레트 (FR-03, FR-04, FR-05)
val CalendarBgPalette = listOf(
    Color(0xFF8EC6E6), Color(0xFFB3D9F0), Color(0xFF6BB4DC),
    Color(0xFF5AAAC8), Color(0xFF3D98BA), Color(0xFF2E86AB),
    Color(0xFFD1EAF8), Color(0xFFA0CBDF), Color(0xFF7ABCD6),
    Color(0xFFE8F4FD)
)

val AppBgPalette = listOf(
    Color(0xFFF0F8FF), Color(0xFFE8F4FD), Color(0xFFDDF0FB),
    Color(0xFFD0E8F5), Color(0xFFC2E0EF), Color(0xFFB0D4E8),
    Color(0xFFF5FBFF), Color(0xFFEBF6FC), Color(0xFFE0F1FA),
    Color(0xFFFFFFFF)
)

val TodayBgPalette = listOf(
    Color(0xFF7EC8E3), Color(0xFF5BB8D4), Color(0xFF3DA8C5),
    Color(0xFF2998B6), Color(0xFF1588A7), Color(0xFF81D4FA),
    Color(0xFF4FC3F7), Color(0xFF29B6F6), Color(0xFF03A9F4),
    Color(0xFF0288D1)
)

val SkyOnPrimary     = Color(0xFFFFFFFF)
val SkyDeepBlue      = Color(0xFF1565C0)  // OnPrimaryContainer
val SkyOnSurface     = Color(0xFF1A2A3A)  // 텍스트
val SkyOnSurfaceVar  = Color(0xFF4A6072)
val SkyError         = Color(0xFFE57373)

// 달력 특수 날짜 색상 (FR-08)
val DateSaturday     = Color(0xFF1565C0)  // 토요일
val DateSunday       = Color(0xFFD32F2F)  // 일요일

// Dark 모드
val SkyDarkPrimary   = Color(0xFF81D4FA)
val SkyDarkContainer = Color(0xFF01579B)
val SkyDarkSurface   = Color(0xFF1A2535)
val SkyDarkBackground = Color(0xFF121C28)
val SkyDarkOnSurface = Color(0xFFD0E8F5)

// Design Ref: diary-editor-bg-setting §FR-02 — DiaryBgPalette 확장 (light 5 + medium 5 + dark 5)
object DiaryBgPalette {
    val colors = listOf(
        // Light
        Color(0xFFFFFFFF),  // 흰색
        Color(0xFFFFF8F0),  // 크림 (기본)
        Color(0xFFFFF0F5),  // 연분홍
        Color(0xFFF5F0FF),  // 연보라
        Color(0xFFF0FAF6),  // 민트
        // Medium
        Color(0xFF8EC6E6),  // 하늘
        Color(0xFFB4A0D8),  // 라벤더
        Color(0xFFC8A888),  // 모카
        Color(0xFF95C09A),  // 세이지
        Color(0xFFF09090),  // 코랄
        // Dark
        Color(0xFF2C3E50),  // 슬레이트
        Color(0xFF0D1E3A),  // 곤색
        Color(0xFF2A1408),  // 다크브라운
        Color(0xFF1E1A2E),  // 미드나잇
        Color(0xFF1A2E1C),  // 다크그린
        Color(0xFF000000),  // 검정
    )
    val labels = listOf(
        "흰색", "크림", "연분홍", "연보라", "민트",
        "하늘", "라벤더", "모카", "세이지", "코랄",
        "슬레이트", "곤색", "다크브라운", "미드나잇", "다크그린", "검정"
    )
}

// Design Ref: joyary-upgrade-v5 §3.2 — 평일 글씨색 팔레트 (FR-06, Option C: object 네임스페이스)
object WeekdayColorPalette {
    val colors = listOf(
        Color(0xFF424242),  // 0: 진회색 (기본)
        Color(0xFF37474F),  // 1: 차콜
        Color(0xFF1A237E),  // 2: 네이비
        Color(0xFF1B5E20),  // 3: 딥그린
        Color(0xFF4A148C),  // 4: 딥퍼플
        Color(0xFF0D47A1),  // 5: 로얄블루
        Color(0xFF004D40),  // 6: 딥틸
        Color(0xFF3E2723),  // 7: 딥브라운
        Color(0xFFBF360C),  // 8: 딥오렌지
        Color(0xFF546E7A),  // 9: 블루그레이
        Color(0xFFFFFFFF),  // 10: 하얀색
        Color(0xFF000000),  // 11: 검정
        Color(0xFF1A2E1A),  // 12: 다크그린
    )
    val labels = listOf(
        "진회색", "차콜", "네이비", "딥그린", "딥퍼플",
        "로얄블루", "딥틸", "딥브라운", "딥오렌지", "블루그레이",
        "하얀색", "검정", "다크그린"
    )
}
