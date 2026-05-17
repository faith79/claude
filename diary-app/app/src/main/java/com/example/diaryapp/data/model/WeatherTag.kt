package com.example.diaryapp.data.model

enum class WeatherTag(val emoji: String, val label: String) {
    SUNNY("☀️", "맑음"),
    PARTLY_CLOUDY("⛅", "구름조금"),
    CLOUDY("☁️", "흐림"),
    RAINY("🌧️", "비"),
    SNOWY("❄️", "눈")
}
