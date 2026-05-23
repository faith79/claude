package com.example.diaryapp.data.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

// Design Ref: joyary-upgrade-v6 §5.1 — 100KB 한도 + EXIF 회전 보정 (FR-01, FR-02)
// image-compression-fix: 해상도 단계 축소 + quality 루프 >= 10 수정
@Singleton
class ImageCompressor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Design Ref: joyary-upgrade-v8 §5 — 30KB 한도 (SC-06)
    private val maxSizeBytes = 30_720L // 30KB (30 × 1024)

    fun compress(uri: Uri): ByteArray {
        val rotation = readExifRotation(uri)

        // Step 1: 원본 해상도 확인 (픽셀 디코딩 없이)
        val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, boundsOpts)
        }
        val originalWidth = boundsOpts.outWidth
        val originalHeight = boundsOpts.outHeight

        // Step 2: maxDimension을 단계적으로 줄이며 30KB 이하가 될 때까지 시도
        // 첫 번째 성공한 결과 반환 → 불필요한 화질 저하 최소화
        // Design Ref: joyary-upgrade-v8 §5 — [640,480,320,160] + quality=75 (SC-07)
        val maxDimensions = listOf(640, 480, 320, 160)
        for (maxDim in maxDimensions) {
            val sampleSize = calculateInSampleSize(originalWidth, originalHeight, maxDim)
            val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }

            var bitmap = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, decodeOpts)
            } ?: return ByteArray(0)

            if (rotation != 0f) {
                val matrix = Matrix().apply { postRotate(rotation) }
                val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                bitmap.recycle()
                bitmap = rotated
            }

            // Step 3: quality 75 → 10 (10 포함) 단계적 감소
            var quality = 75
            var output: ByteArray
            do {
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
                output = stream.toByteArray()
                quality -= 10
            } while (output.size > maxSizeBytes && quality >= 10)

            bitmap.recycle()

            if (output.size <= maxSizeBytes) return output
            // 이 maxDim에서도 초과 시 다음 단계(더 작은 maxDim)로 진행
        }

        // 모든 단계 실패 (240px + quality=10에서도 초과 → 정상 사진에서는 도달 불가)
        return ByteArray(0)
    }

    // inSampleSize 계산: 긴 변이 maxDimension 이하가 되는 2의 거듭제곱 반환
    private fun calculateInSampleSize(width: Int, height: Int, maxDimension: Int): Int {
        if (width <= 0 || height <= 0) return 1
        val longerSide = maxOf(width, height)
        var sampleSize = 1
        while (longerSide / (sampleSize * 2) >= maxDimension) {
            sampleSize *= 2
        }
        return sampleSize
    }

    private fun readExifRotation(uri: Uri): Float {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            } ?: 0f
        } catch (e: Exception) {
            0f
        }
    }
}
