package com.example.diaryapp.data.repository

import android.net.Uri
import com.example.diaryapp.data.model.DiaryEntry
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

interface DiaryRepository {
    fun getDiariesByMonth(userId: String, yearMonth: YearMonth): Flow<List<DiaryEntry>>
    suspend fun getDiaryByDate(userId: String, date: String): DiaryEntry?
    suspend fun saveDiary(entry: DiaryEntry): Result<String>
    suspend fun updateDiary(entry: DiaryEntry): Result<Unit>
    // Design Ref: §4.2 — Firestore 삭제 후 Storage 삭제 순서 보장
    suspend fun deleteDiaryWithImages(entry: DiaryEntry): Result<Unit>
    suspend fun searchDiaries(userId: String, query: String): List<DiaryEntry>
    suspend fun uploadImage(userId: String, diaryId: String, uri: Uri): Result<String>
    suspend fun uploadImages(userId: String, diaryId: String, byteArrays: List<ByteArray>): Result<List<String>>
    suspend fun deleteImage(imageUrl: String): Result<Unit>
}
