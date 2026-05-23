package com.example.diaryapp.data.util

import android.content.Context
import com.example.diaryapp.data.model.DiaryEntry
import com.example.diaryapp.data.model.EmotionTag
import com.example.diaryapp.data.model.WeatherTag
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

// Design Ref: joyary-upgrade-v8 §3 — 파일 기반 24h 디스크 캐시 (SC-01)
@Singleton
class DiaryLocalCache @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val cacheRoot = File(context.cacheDir, "joyary_cache").also { it.mkdirs() }
    private val TTL_MS = 24L * 60 * 60 * 1000

    private fun safeKey(key: String) = key.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")

    private fun isExpired(file: File): Boolean =
        System.currentTimeMillis() - file.lastModified() >= TTL_MS

    // Design Ref: §3.3 — TTL 체크 후 읽기, 만료 시 파일 삭제
    private fun readValidFile(file: File): String? {
        if (!file.exists()) return null
        if (isExpired(file)) { file.delete(); return null }
        return try { file.readText() } catch (e: Exception) { null }
    }

    // ── Month cache ──────────────────────────────────────────────────────────

    fun getMonth(key: String): List<DiaryEntry>? {
        val file = File(cacheRoot, "month_${safeKey(key)}.json")
        val json = readValidFile(file) ?: return null
        return try {
            val arr = JSONObject(json).getJSONArray("entries")
            (0 until arr.length()).map { entryFromJson(arr.getJSONObject(it)) }
        } catch (e: Exception) { null }
    }

    fun putMonth(key: String, entries: List<DiaryEntry>) {
        val file = File(cacheRoot, "month_${safeKey(key)}.json")
        try {
            val arr = JSONArray()
            entries.forEach { arr.put(entryToJson(it)) }
            file.writeText(JSONObject().put("entries", arr).toString())
        } catch (e: Exception) { /* I/O 실패 시 무시 — Firestore가 원본 */ }
    }

    fun removeMonth(key: String) {
        File(cacheRoot, "month_${safeKey(key)}.json").delete()
    }

    // ── Entry cache ───────────────────────────────────────────────────────────
    // Pair<Boolean, DiaryEntry?>: null = 캐시 없음, Pair(true, entry) or Pair(false, null) = 캐시 있음

    fun getEntry(key: String): Pair<Boolean, DiaryEntry?>? {
        val file = File(cacheRoot, "entry_${safeKey(key)}.json")
        val json = readValidFile(file) ?: return null
        return try {
            val obj = JSONObject(json)
            val hasValue = obj.getBoolean("hasValue")
            if (hasValue) Pair(true, entryFromJson(obj.getJSONObject("entry")))
            else Pair(false, null)
        } catch (e: Exception) { null }
    }

    fun putEntry(key: String, entry: DiaryEntry?) {
        val file = File(cacheRoot, "entry_${safeKey(key)}.json")
        try {
            val obj = JSONObject()
            if (entry != null) {
                obj.put("hasValue", true).put("entry", entryToJson(entry))
            } else {
                obj.put("hasValue", false)
            }
            file.writeText(obj.toString())
        } catch (e: Exception) { /* I/O 실패 시 무시 */ }
    }

    fun removeEntry(key: String) {
        File(cacheRoot, "entry_${safeKey(key)}.json").delete()
    }

    // Design Ref: §3.3 — 앱 시작 시 만료 파일 일괄 삭제 (SC-05)
    fun cleanupExpired() {
        try {
            cacheRoot.listFiles()?.forEach { if (isExpired(it)) it.delete() }
        } catch (e: Exception) { /* 정리 실패 시 무시 */ }
    }

    // ── Serialization ─────────────────────────────────────────────────────────

    private fun entryToJson(entry: DiaryEntry): JSONObject {
        val urlArr = JSONArray()
        entry.imageUrls.forEach { urlArr.put(it) }
        return JSONObject()
            .put("id", entry.id)
            .put("userId", entry.userId)
            .put("content", entry.content)
            .put("date", entry.date)
            .put("emotion", entry.emotion?.name ?: JSONObject.NULL)
            .put("weather", entry.weather?.name ?: JSONObject.NULL)
            .put("imageUrls", urlArr)
            .put("createdAt", entry.createdAt)
            .put("updatedAt", entry.updatedAt)
    }

    private fun entryFromJson(obj: JSONObject): DiaryEntry {
        val urlArr = obj.optJSONArray("imageUrls")
        val imageUrls = if (urlArr != null) {
            (0 until urlArr.length()).map { urlArr.getString(it) }
        } else emptyList()
        return DiaryEntry(
            id = obj.optString("id", ""),
            userId = obj.optString("userId", ""),
            content = obj.optString("content", ""),
            date = obj.optString("date", ""),
            emotion = obj.optString("emotion", "").takeIf { it.isNotEmpty() }
                ?.let { runCatching { EmotionTag.valueOf(it) }.getOrNull() },
            weather = obj.optString("weather", "").takeIf { it.isNotEmpty() }
                ?.let { runCatching { WeatherTag.valueOf(it) }.getOrNull() },
            imageUrls = imageUrls,
            createdAt = obj.optLong("createdAt", 0L),
            updatedAt = obj.optLong("updatedAt", 0L)
        )
    }
}
