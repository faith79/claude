package com.example.diaryapp.data.model

data class MemoEntry(
    val id: String = "",
    val userId: String = "",
    val type: MemoType = MemoType.TEXT,
    val title: String = "",
    val content: String = "",
    val todos: List<TodoItem> = emptyList(),
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

data class TodoItem(
    val id: String = "",
    val text: String = "",
    val isDone: Boolean = false
)

enum class MemoType { TEXT, TODO }
