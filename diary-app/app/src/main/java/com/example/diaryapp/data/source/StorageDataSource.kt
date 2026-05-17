package com.example.diaryapp.data.source

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// Design Ref: §4.2 — 다중 업로드(ByteArray), URL 기반 단일/다중 삭제
class StorageDataSource @Inject constructor(
    private val storage: FirebaseStorage
) {
    // v0.1.0 호환 단일 업로드 (Uri)
    suspend fun uploadImage(userId: String, diaryId: String, uri: Uri): String {
        val ref = storage.reference.child("images/$userId/$diaryId.jpg")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    // v0.2.0 다중 업로드 (ByteArray — 압축 완료 후 전달)
    suspend fun uploadImages(userId: String, diaryId: String, byteArrays: List<ByteArray>): List<String> =
        byteArrays.mapIndexed { index, bytes ->
            val ref = storage.reference.child("images/$userId/${diaryId}_$index.jpg")
            ref.putBytes(bytes).await()
            ref.downloadUrl.await().toString()
        }

    // Plan SC: Storage 삭제 — URL에서 Storage 경로 추출 후 삭제
    suspend fun deleteImage(imageUrl: String): Result<Unit> = runCatching {
        storage.getReferenceFromUrl(imageUrl).delete().await()
    }

    suspend fun deleteImages(imageUrls: List<String>): Result<Unit> {
        if (imageUrls.isEmpty()) return Result.success(Unit)
        return imageUrls.map { deleteImage(it) }
            .firstOrNull { it.isFailure } ?: Result.success(Unit)
    }
}
