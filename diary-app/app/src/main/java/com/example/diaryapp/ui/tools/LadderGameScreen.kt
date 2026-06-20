package com.example.diaryapp.ui.tools

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

// Design Ref: tools-tab-ladder-game §LadderGameScreen
private enum class LadderPhase { INPUT, NAMING, REVEALING }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LadderGameScreen(onBack: () -> Unit) {
    // INPUT state
    var inputs by remember { mutableStateOf(listOf("", "")) }

    // GAME state
    var phase by remember { mutableStateOf(LadderPhase.INPUT) }
    var items by remember { mutableStateOf<List<String>>(emptyList()) }
    var rungs by remember { mutableStateOf<List<Set<Int>>>(emptyList()) }
    var names by remember { mutableStateOf<List<String?>>(emptyList()) }
    var revealedIndex by remember { mutableStateOf<Int?>(null) }
    var revealedPath by remember { mutableStateOf<List<Int>>(emptyList()) }

    // Name dialog
    var dialogIndex by remember { mutableStateOf<Int?>(null) }
    var dialogText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("사다리 게임") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (phase != LadderPhase.INPUT) {
                            phase = LadderPhase.INPUT
                            revealedIndex = null
                            revealedPath = emptyList()
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로")
                    }
                }
            )
        }
    ) { padding ->
        when (phase) {
            LadderPhase.INPUT -> LadderInputContent(
                inputs = inputs,
                onInputChange = { idx, v ->
                    inputs = inputs.toMutableList().also { it[idx] = v }
                },
                onAddInput = { if (inputs.size < 10) inputs = inputs + "" },
                onRemoveInput = { idx ->
                    if (inputs.size > 2) inputs = inputs.toMutableList().also { it.removeAt(idx) }
                },
                onStart = {
                    val valid = inputs.filter { it.isNotBlank() }
                    if (valid.size >= 2) {
                        items = valid.shuffled()
                        rungs = generateLadder(valid.size)
                        names = List(valid.size) { null }
                        revealedIndex = null
                        revealedPath = emptyList()
                        phase = LadderPhase.NAMING
                    }
                },
                modifier = Modifier.padding(padding)
            )
            else -> LadderGameContent(
                names = names,
                items = items,
                rungs = rungs,
                isNaming = phase == LadderPhase.NAMING,
                revealedIndex = revealedIndex,
                revealedPath = revealedPath,
                onNameClick = { idx ->
                    if (phase == LadderPhase.NAMING) {
                        dialogIndex = idx
                        dialogText = names.getOrNull(idx) ?: ""
                    } else {
                        revealedIndex = idx
                        revealedPath = tracePath(idx, rungs)
                    }
                },
                modifier = Modifier.padding(padding)
            )
        }
    }

    // Name entry dialog
    dialogIndex?.let { idx ->
        AlertDialog(
            onDismissRequest = { dialogIndex = null },
            title = { Text("이름 입력") },
            text = {
                OutlinedTextField(
                    value = dialogText,
                    onValueChange = { dialogText = it },
                    label = { Text("${idx + 1}번 슬롯") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (dialogText.isNotBlank()) {
                        val newNames = names.toMutableList().also { it[idx] = dialogText.trim() }
                        names = newNames
                        dialogIndex = null
                        dialogText = ""
                        // Design Ref: §state-machine — 모든 이름 입력 완료 시 REVEALING 전환
                        if (newNames.all { !it.isNullOrBlank() }) {
                            phase = LadderPhase.REVEALING
                        }
                    }
                }) { Text("확인") }
            },
            dismissButton = {
                TextButton(onClick = { dialogIndex = null }) { Text("취소") }
            }
        )
    }
}

// Design Ref: §generateLadder — 인접 가로줄 방지 + Random.nextBoolean() 50% 확률
private fun generateLadder(n: Int, rows: Int = 12): List<Set<Int>> =
    (0 until rows).map {
        val row = mutableSetOf<Int>()
        (0 until n - 1).forEach { col ->
            if (col - 1 !in row && Random.nextBoolean()) row.add(col)
        }
        row
    }

// Design Ref: §tracePath — path[i] = i번 세그먼트에서의 열 위치 (size = rungs.size + 1)
private fun tracePath(startCol: Int, rungs: List<Set<Int>>): List<Int> {
    var col = startCol
    val path = mutableListOf(col)
    for (rowRungs in rungs) {
        when {
            col in rowRungs -> col++
            col > 0 && col - 1 in rowRungs -> col--
        }
        path.add(col)
    }
    return path
}

@Composable
private fun LadderInputContent(
    inputs: List<String>,
    onInputChange: (Int, String) -> Unit,
    onAddInput: () -> Unit,
    onRemoveInput: (Int) -> Unit,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val validCount = inputs.count { it.isNotBlank() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "참가 항목 입력 (2~10개)",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))

        inputs.forEachIndexed { idx, value ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = value,
                    onValueChange = { onInputChange(idx, it) },
                    label = { Text("항목 ${idx + 1}") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                if (inputs.size > 2) {
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = { onRemoveInput(idx) }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "삭제",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        if (inputs.size < 10) {
            OutlinedButton(
                onClick = onAddInput,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("항목 추가")
            }
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = onStart,
            enabled = validCount >= 2,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("사다리 만들기")
        }

        if (validCount < 2) {
            Text(
                "최소 2개 항목이 필요합니다",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun LadderGameContent(
    names: List<String?>,
    items: List<String>,
    rungs: List<Set<Int>>,
    isNaming: Boolean,
    revealedIndex: Int?,
    revealedPath: List<Int>,
    onNameClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val n = items.size
    val resultCol = revealedPath.lastOrNull()
    val fontSize = if (n > 6) 10.sp else 12.sp

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        Spacer(Modifier.height(6.dp))

        // Design Ref: §status-text — 단계별 안내 문구
        when {
            isNaming -> Text(
                "이름을 입력하세요 (${names.count { !it.isNullOrBlank() }}/$n)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            revealedIndex == null -> Text(
                "이름을 클릭하면 결과를 확인할 수 있어요",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            else -> Text(
                "🎯 ${names[revealedIndex]} → ${items.getOrElse(resultCol ?: 0) { "" }}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(8.dp))

        // Design Ref: §top-name-buttons — 이름 입력/선택 버튼
        Row(modifier = Modifier.fillMaxWidth()) {
            names.forEachIndexed { idx, name ->
                val isFilled = !name.isNullOrBlank()
                val isSelected = revealedIndex == idx
                val bgColor = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isFilled  -> MaterialTheme.colorScheme.primaryContainer
                    else      -> MaterialTheme.colorScheme.surfaceVariant
                }
                val textColor = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    isFilled  -> MaterialTheme.colorScheme.onPrimaryContainer
                    else      -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                Surface(
                    onClick = { onNameClick(idx) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = bgColor
                ) {
                    Text(
                        text = if (isFilled) name!! else "${idx + 1}",
                        modifier = Modifier
                            .padding(vertical = 10.dp, horizontal = 2.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontSize = fontSize,
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Design Ref: §ladder-canvas — 사다리 캔버스 (중간 숨김/공개)
        LadderCanvas(
            n = n,
            rungs = rungs,
            isHidden = revealedIndex == null,
            path = if (revealedIndex != null && revealedPath.isNotEmpty()) revealedPath else null,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        Spacer(Modifier.height(8.dp))

        // Design Ref: §bottom-items — 하단 항목 (결과 강조)
        Row(modifier = Modifier.fillMaxWidth()) {
            items.forEachIndexed { idx, item ->
                val isResult = !isNaming && resultCol == idx && revealedIndex != null
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = if (isResult) MaterialTheme.colorScheme.tertiary
                            else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = item,
                        modifier = Modifier
                            .padding(vertical = 10.dp, horizontal = 2.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontSize = fontSize,
                        color = if (isResult) MaterialTheme.colorScheme.onTertiary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = if (isResult) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
    }
}

// Design Ref: §LadderCanvas — Canvas 드로잉: 세로줄 + 가로줄 + 경로 + 중간 오버레이
@Composable
private fun LadderCanvas(
    n: Int,
    rungs: List<Set<Int>>,
    isHidden: Boolean,
    path: List<Int>? = null,
    modifier: Modifier = Modifier
) {
    val outlineColor = MaterialTheme.colorScheme.outline
    val pathColor = MaterialTheme.colorScheme.tertiary
    val overlayColor = MaterialTheme.colorScheme.surfaceVariant
    val overlayTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (n <= 0 || rungs.isEmpty()) return@Canvas

            val colW = size.width / n
            // Design Ref: §canvas-layout — rowH = height / (rungs.size + 1), 첫 가로줄 = rowH
            val rowH = size.height / (rungs.size + 1)
            val stroke = 3.dp.toPx()

            // 세로줄: 항상 전체 그리기
            for (col in 0 until n) {
                val x = colW * col + colW / 2f
                drawLine(
                    color = outlineColor,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
            }

            // 가로줄 + 경로: 숨김 상태가 아닐 때만
            if (!isHidden) {
                // 가로줄 (rungs)
                rungs.forEachIndexed { row, rowRungs ->
                    val y = (row + 1) * rowH
                    rowRungs.forEach { col ->
                        drawLine(
                            color = outlineColor,
                            start = Offset(colW * col + colW / 2f, y),
                            end = Offset(colW * (col + 1) + colW / 2f, y),
                            strokeWidth = stroke,
                            cap = StrokeCap.Round
                        )
                    }
                }

                // 경로 강조
                if (path != null && path.size == rungs.size + 1) {
                    val pathStroke = 5.dp.toPx()
                    // Design Ref: §path-drawing — 세그먼트 i: y = i*rowH → (i+1)*rowH, col = path[i]
                    for (i in path.indices) {
                        val col = path[i]
                        val x = colW * col + colW / 2f
                        val y1 = i * rowH
                        val y2 = ((i + 1) * rowH).coerceAtMost(size.height)
                        // 세로 세그먼트
                        drawLine(
                            pathColor,
                            Offset(x, y1),
                            Offset(x, y2),
                            pathStroke,
                            cap = StrokeCap.Round
                        )
                        // 방향 전환 시 가로 세그먼트
                        if (i < path.size - 1 && path[i] != path[i + 1]) {
                            val x2 = colW * path[i + 1] + colW / 2f
                            drawLine(
                                pathColor,
                                Offset(x, y2),
                                Offset(x2, y2),
                                pathStroke,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }
            }
        }

        // 중간 오버레이 (가로줄 숨김)
        if (isHidden) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .fillMaxHeight(0.6f)
                    .align(Alignment.Center)
                    .background(overlayColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔒", fontSize = 28.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "사다리 결과는 비밀!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = overlayTextColor
                    )
                }
            }
        }
    }
}
