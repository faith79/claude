package com.example.diaryapp.data.source

import com.example.diaryapp.data.model.DiaryEntry
import com.example.diaryapp.data.model.EmotionTag
import com.example.diaryapp.data.model.WeatherTag
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class FirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("diaries")

    fun getDiariesByMonth(userId: String, yearMonth: YearMonth): Flow<List<DiaryEntry>> =
        callbackFlow {
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE
            val startDate = yearMonth.atDay(1).format(formatter)
            val endDate = yearMonth.atEndOfMonth().format(formatter)

            val listener = collection
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) { close(error); return@addSnapshotListener }
                    val entries = snapshot?.documents
                        ?.mapNotNull { doc ->
                            doc.toObject(DiaryEntryDto::class.java)?.toDomain(doc.id)
                        } ?: emptyList()
                    trySend(entries)
                }
            awaitClose { listener.remove() }
        }

    suspend fun getDiaryByDate(userId: String, date: String): DiaryEntry? =
        collection
            .whereEqualTo("userId", userId)
            .whereEqualTo("date", date)
            .get().await()
            .documents.firstOrNull()
            ?.let { it.toObject(DiaryEntryDto::class.java)?.toDomain(it.id) }

    suspend fun saveDiary(entry: DiaryEntry): String =
        collection.add(DiaryEntryDto.fromDomain(entry)).await().id

    suspend fun updateDiary(entry: DiaryEntry) =
        collection.document(entry.id).set(DiaryEntryDto.fromDomain(entry)).await()

    suspend fun deleteDiary(diaryId: String) =
        collection.document(diaryId).delete().await()

    suspend fun searchDiaries(userId: String, query: String): List<DiaryEntry> =
        collection
            .whereEqualTo("userId", userId)
            .get().await()
            .documents
            .mapNotNull { it.toObject(DiaryEntryDto::class.java)?.toDomain(it.id) }
            .filter { it.content.contains(query, ignoreCase = true) }

    // Design Ref: §3.4 — v0.1.0 하위 호환: imageUrl fallback, title 무시, weather nullable
    // Design Ref: multi-emotion-weather-select §CHANGE-02 — emotions/weathers List, legacy fallback
    data class DiaryEntryDto(
        val userId: String = "",
        val content: String = "",
        val date: String = "",
        val emotions: List<String> = emptyList(),  // 신규 다중 필드
        val emotion: String? = null,               // 레거시 단일 필드 — 읽기 전용
        val weathers: List<String> = emptyList(),  // 신규 다중 필드
        val weather: String? = null,               // 레거시 단일 필드 — 읽기 전용
        val imageUrls: List<String> = emptyList(),
        val imageUrl: String? = null,              // 레거시 필드 — 읽기 전용
        val createdAt: Long = 0L,
        val updatedAt: Long = 0L
    ) {
        fun toDomain(id: String) = DiaryEntry(
            id = id,
            userId = userId,
            content = content,
            date = date,
            // emotions 우선, 비어있으면 legacy emotion 단일 필드 fallback
            emotions = emotions.mapNotNull { runCatching { EmotionTag.valueOf(it) }.getOrNull() }
                .ifEmpty { emotion?.let { runCatching { EmotionTag.valueOf(it) }.getOrNull() }?.let { listOf(it) } ?: emptyList() },
            // weathers 우선, 비어있으면 legacy weather 단일 필드 fallback
            weathers = weathers.mapNotNull { runCatching { WeatherTag.valueOf(it) }.getOrNull() }
                .ifEmpty { weather?.let { runCatching { WeatherTag.valueOf(it) }.getOrNull() }?.let { listOf(it) } ?: emptyList() },
            // Plan SC: imageUrl(구버전) → imageUrls[0] fallback
            imageUrls = imageUrls.ifEmpty { imageUrl?.let { listOf(it) } ?: emptyList() },
            createdAt = createdAt,
            updatedAt = updatedAt
        )

        companion object {
            fun fromDomain(e: DiaryEntry) = DiaryEntryDto(
                userId = e.userId,
                content = e.content,
                date = e.date,
                emotions = e.emotions.map { it.name },
                weathers = e.weathers.map { it.name },
                imageUrls = e.imageUrls,
                createdAt = e.createdAt,
                updatedAt = e.updatedAt
            )
        }
    }
}
