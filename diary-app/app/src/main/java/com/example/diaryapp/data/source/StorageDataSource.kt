package com.example.diaryapp.data.source

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class StorageDataSource @Inject constructor(
    private val storage: FirebaseStorage
) {
    suspend fun uploadImage(userId: String, diaryId: String, uri: Uri): String {
        val ref = storage.reference.child("images/$userId/$diaryId.jpg")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }
}
