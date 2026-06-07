package com.example.diaryapp.data.repository

import com.example.diaryapp.data.model.MemoEntry
import com.example.diaryapp.data.source.MemoDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoRepositoryImpl @Inject constructor(
    private val dataSource: MemoDataSource
) : MemoRepository {
    override suspend fun getMemos(userId: String) = dataSource.getMemos(userId)
    override suspend fun saveMemo(userId: String, memo: MemoEntry) = dataSource.saveMemo(userId, memo)
    override suspend fun deleteMemo(userId: String, memoId: String) { dataSource.deleteMemo(userId, memoId) }
}
