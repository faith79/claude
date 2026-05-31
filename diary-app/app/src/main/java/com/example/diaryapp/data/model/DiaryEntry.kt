package com.example.diaryapp.data.model

// Design Ref: §3.1 — title 제거(v0.1.0 Firestore 호환), imageUrls(최대 3장), weather 추가
// Design Ref: multi-emotion-weather-select §CHANGE-01 — emotions/weathers List로 다중 선택 지원
data class DiaryEntry(
    val id: String = "",
    val userId: String = "",
    val content: String = "",
    val date: String = "",          // "yyyy-MM-dd"
    val emotions: List<EmotionTag> = emptyList(),
    val weathers: List<WeatherTag> = emptyList(),
    val imageUrls: List<String> = emptyList(),
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
