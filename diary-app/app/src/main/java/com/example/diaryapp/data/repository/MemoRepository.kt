package com.example.diaryapp.data.repository

import com.example.diaryapp.data.model.MemoEntry

interface MemoRepository {
    suspend fun getMemos(userId: String): List<MemoEntry>
    suspend fun saveMemo(userId: String, memo: MemoEntry): String
    suspend fun deleteMemo(userId: String, memoId: String)
}
