package com.example.diaryapp.ui.memo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diaryapp.data.model.MemoEntry
import com.example.diaryapp.data.model.MemoType
import com.example.diaryapp.data.model.TodoItem
import java.util.UUID

// Design Ref: diary-tab-memo §FR-03 — 메모 목록 (탭 1 콘텐츠, FAB 없음 — HomeScreen Scaffold에서 관리)
@Composable
fun MemoListContent(
    memos: List<MemoEntry>,
    onDeleteMemo: (String) -> Unit,
    onEditMemo: (MemoEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    if (memos.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("메모가 없습니다", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("+ 버튼으로 추가하세요", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(memos, key = { it.id }) { memo ->
                MemoCard(
                    memo = memo,
                    onDelete = { onDeleteMemo(memo.id) },
                    onClick = { onEditMemo(memo) }
                )
            }
        }
    }
}

@Composable
private fun MemoCard(
    memo: MemoEntry,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val isTodo = memo.type == MemoType.TODO
                    Surface(
                        color = if (isTodo) MaterialTheme.colorScheme.tertiary
                                else MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            if (isTodo) "TODO" else "메모",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isTodo) MaterialTheme.colorScheme.onTertiary
                                    else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    if (memo.title.isNotEmpty()) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            memo.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                if (memo.type == MemoType.TEXT) {
                    Text(
                        memo.content.take(80),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                } else {
                    val doneCount = memo.todos.count { it.isDone }
                    Text(
                        "${memo.todos.size}개 항목 (완료 $doneCount/${memo.todos.size})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    memo.todos.take(3).forEach { item ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(if (item.isDone) "☑" else "☐", fontSize = 14.sp)
                            Spacer(Modifier.width(4.dp))
                            Text(item.text, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                        }
                    }
                    if (memo.todos.size > 3) {
                        Text("…외 ${memo.todos.size - 3}개", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "삭제", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

// Design Ref: diary-tab-memo §FR-04/05 — 메모 타입 선택 + TODO 항목 편집
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoEditorSheet(
    initial: MemoEntry?,
    onDismiss: () -> Unit,
    onSave: (MemoEntry) -> Unit
) {
    var type    by remember { mutableStateOf(initial?.type ?: MemoType.TEXT) }
    var title   by remember { mutableStateOf(initial?.title ?: "") }
    var content by remember { mutableStateOf(initial?.content ?: "") }
    var todos   by remember { mutableStateOf(initial?.todos ?: emptyList<TodoItem>()) }
    var newTodo by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                if (initial == null) "새 메모" else "메모 편집",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))

            // 타입 선택
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = type == MemoType.TEXT,
                    onClick = { type = MemoType.TEXT },
                    label = { Text("메모") }
                )
                FilterChip(
                    selected = type == MemoType.TODO,
                    onClick = { type = MemoType.TODO },
                    label = { Text("TODO") }
                )
            }
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("제목 (선택)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))

            if (type == MemoType.TEXT) {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("내용") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    maxLines = 8
                )
            } else {
                // TODO 항목 목록
                todos.forEachIndexed { index, item ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = item.isDone,
                            onCheckedChange = { checked ->
                                todos = todos.toMutableList().also { it[index] = item.copy(isDone = checked) }
                            }
                        )
                        OutlinedTextField(
                            value = item.text,
                            onValueChange = { text ->
                                todos = todos.toMutableList().also { it[index] = item.copy(text = text) }
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        IconButton(onClick = {
                            todos = todos.toMutableList().also { it.removeAt(index) }
                        }) {
                            Icon(Icons.Default.Delete, "항목 삭제", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                // 새 항목 추가
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newTodo,
                        onValueChange = { newTodo = it },
                        label = { Text("항목 추가") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    IconButton(onClick = {
                        if (newTodo.isNotBlank()) {
                            todos = todos + TodoItem(id = UUID.randomUUID().toString(), text = newTodo.trim())
                            newTodo = ""
                        }
                    }) {
                        Icon(Icons.Default.Add, "추가")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    val now = System.currentTimeMillis()
                    onSave(
                        (initial ?: MemoEntry()).copy(
                            type      = type,
                            title     = title.trim(),
                            content   = content,
                            todos     = todos,
                            updatedAt = now,
                            createdAt = if (initial == null) now else initial.createdAt
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("저장")
            }
        }
    }
}
