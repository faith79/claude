package com.example.diaryapp.data.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

// Design Ref: §4.1 — 1MB 초과 시 quality 90→10 감소 loop
// Design Ref: joyary-upgrade-v5 §7.2 — 300KB 한도로 하향 (FR-02, KD-04)
@Singleton
class ImageCompressor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val maxSizeBytes = 307_200L // 300KB (300 × 1024)

    fun compress(uri: Uri): ByteArray {
        val options = BitmapFactory.Options().apply { inSampleSize = 1 }
        val bitmap = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: return ByteArray(0)

        var quality = 90
        var output: ByteArray
        do {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            output = stream.toByteArray()
            quality -= 10
        } while (output.size > maxSizeBytes && quality > 10)

        bitmap.recycle()
        return output
    }
}
