package com.example.diaryapp.data.model

enum class EmotionTag(val emoji: String, val label: String) {
    HAPPY("😊", "행복"),
    SAD("😢", "슬픔"),
    ANGRY("😠", "분노"),
    CALM("😌", "평온"),
    EXCITED("🥰", "설렘"),
    ANXIOUS("😰", "불안"),
    TIRED("😴", "피곤")
}
