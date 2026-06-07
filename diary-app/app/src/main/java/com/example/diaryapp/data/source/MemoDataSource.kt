package com.example.diaryapp.data.source

import com.example.diaryapp.data.model.MemoEntry
import com.example.diaryapp.data.model.MemoType
import com.example.diaryapp.data.model.TodoItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun col(userId: String) =
        firestore.collection("users").document(userId).collection("memos")

    suspend fun getMemos(userId: String): List<MemoEntry> =
        col(userId)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .get().await()
            .documents.mapNotNull { doc ->
                MemoDto.fromSnapshot(doc.data ?: return@mapNotNull null, doc.id)
            }

    suspend fun saveMemo(userId: String, memo: MemoEntry): String =
        if (memo.id.isEmpty()) {
            col(userId).add(MemoDto.fromDomain(memo)).await().id
        } else {
            col(userId).document(memo.id).set(MemoDto.fromDomain(memo)).await()
            memo.id
        }

    suspend fun deleteMemo(userId: String, memoId: String) {
        col(userId).document(memoId).delete().await()
    }
}

private object MemoDto {
    fun fromDomain(memo: MemoEntry): Map<String, Any> = mapOf(
        "userId"    to memo.userId,
        "type"      to memo.type.name,
        "title"     to memo.title,
        "content"   to memo.content,
        "todos"     to memo.todos.map { mapOf("id" to it.id, "text" to it.text, "isDone" to it.isDone) },
        "createdAt" to memo.createdAt,
        "updatedAt" to memo.updatedAt
    )

    @Suppress("UNCHECKED_CAST")
    fun fromSnapshot(data: Map<String, Any>, id: String): MemoEntry = MemoEntry(
        id        = id,
        userId    = data["userId"] as? String ?: "",
        type      = runCatching { MemoType.valueOf(data["type"] as? String ?: "") }.getOrDefault(MemoType.TEXT),
        title     = data["title"] as? String ?: "",
        content   = data["content"] as? String ?: "",
        todos     = (data["todos"] as? List<Map<String, Any>>)?.map { m ->
            TodoItem(
                id     = m["id"] as? String ?: "",
                text   = m["text"] as? String ?: "",
                isDone = m["isDone"] as? Boolean ?: false
            )
        } ?: emptyList(),
        createdAt = data["createdAt"] as? Long ?: 0L,
        updatedAt = data["updatedAt"] as? Long ?: 0L
    )
}
