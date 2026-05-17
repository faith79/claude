package com.example.diaryapp.data.source

import com.example.diaryapp.data.model.DiaryEntry
import com.example.diaryapp.data.model.EmotionTag
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
            .filter {
                it.title.contains(query, ignoreCase = true) ||
                it.content.contains(query, ignoreCase = true)
            }

    data class DiaryEntryDto(
        val userId: String = "",
        val title: String = "",
        val content: String = "",
        val date: String = "",
        val emotion: String? = null,
        val imageUrl: String? = null,
        val createdAt: Long = 0L,
        val updatedAt: Long = 0L
    ) {
        fun toDomain(id: String) = DiaryEntry(
            id = id,
            userId = userId,
            title = title,
            content = content,
            date = date,
            emotion = emotion?.let { runCatching { EmotionTag.valueOf(it) }.getOrNull() },
            imageUrl = imageUrl,
            createdAt = createdAt,
            updatedAt = updatedAt
        )

        companion object {
            fun fromDomain(e: DiaryEntry) = DiaryEntryDto(
                userId = e.userId,
                title = e.title,
                content = e.content,
                date = e.date,
                emotion = e.emotion?.name,
                imageUrl = e.imageUrl,
                createdAt = e.createdAt,
                updatedAt = e.updatedAt
            )
        }
    }
}
