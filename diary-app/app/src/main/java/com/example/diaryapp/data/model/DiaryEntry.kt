package com.example.diaryapp.data.model

// Design Ref: §3.1 — title 제거(v0.1.0 Firestore 호환), imageUrls(최대 3장), weather 추가
data class DiaryEntry(
    val id: String = "",
    val userId: String = "",
    val content: String = "",
    val date: String = "",          // "yyyy-MM-dd"
    val emotion: EmotionTag? = null,
    val weather: WeatherTag? = null,
    val imageUrls: List<String> = emptyList(),
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
