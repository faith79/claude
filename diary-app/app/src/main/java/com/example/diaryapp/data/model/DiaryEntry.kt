package com.example.diaryapp.data.model

data class DiaryEntry(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val date: String = "",          // "yyyy-MM-dd"
    val emotion: EmotionTag? = null,
    val imageUrl: String? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
