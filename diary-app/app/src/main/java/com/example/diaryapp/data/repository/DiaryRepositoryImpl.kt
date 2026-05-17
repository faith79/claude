package com.example.diaryapp.data.repository

import android.net.Uri
import com.example.diaryapp.data.model.DiaryEntry
import com.example.diaryapp.data.source.FirestoreDataSource
import com.example.diaryapp.data.source.StorageDataSource
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth
import javax.inject.Inject

class DiaryRepositoryImpl @Inject constructor(
    private val firestoreDataSource: FirestoreDataSource,
    private val storageDataSource: StorageDataSource
) : DiaryRepository {
    override fun getDiariesByMonth(userId: String, yearMonth: YearMonth): Flow<List<DiaryEntry>> =
        firestoreDataSource.getDiariesByMonth(userId, yearMonth)

    override suspend fun getDiaryByDate(userId: String, date: String): DiaryEntry? =
        firestoreDataSource.getDiaryByDate(userId, date)

    override suspend fun saveDiary(entry: DiaryEntry): Result<String> =
        runCatching { firestoreDataSource.saveDiary(entry) }

    override suspend fun updateDiary(entry: DiaryEntry): Result<Unit> =
        runCatching { firestoreDataSource.updateDiary(entry) }

    // Plan SC: Firestore 삭제 성공 후에만 Storage 삭제 (SC-09)
    override suspend fun deleteDiaryWithImages(entry: DiaryEntry): Result<Unit> {
        val deleteFirestore = runCatching { firestoreDataSource.deleteDiary(entry.id) }.map { Unit }
        if (deleteFirestore.isFailure) return deleteFirestore
        if (entry.imageUrls.isNotEmpty()) {
            storageDataSource.deleteImages(entry.imageUrls)
        }
        return Result.success(Unit)
    }

    override suspend fun searchDiaries(userId: String, query: String): List<DiaryEntry> =
        firestoreDataSource.searchDiaries(userId, query)

    override suspend fun uploadImage(userId: String, diaryId: String, uri: Uri): Result<String> =
        runCatching { storageDataSource.uploadImage(userId, diaryId, uri) }

    override suspend fun uploadImages(userId: String, diaryId: String, byteArrays: List<ByteArray>): Result<List<String>> =
        runCatching { storageDataSource.uploadImages(userId, diaryId, byteArrays) }

    override suspend fun deleteImage(imageUrl: String): Result<Unit> =
        storageDataSource.deleteImage(imageUrl)
}
