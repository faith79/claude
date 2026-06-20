package com.example.diaryapp.ui.tools

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random
import kotlinx.coroutines.launch

private enum class LadderPhase { INPUT, NAMING, REVEALING }

private val LADDER_PATH_COLORS = listOf(
    Color(0xFFE53935), Color(0xFF1E88E5), Color(0xFF43A047),
    Color(0xFFFB8C00), Color(0xFF8E24AA), Color(0xFF00ACC1),
    Color(0xFFE91E63), Color(0xFF6D4C41), Color(0xFF3949AB), Color(0xFF7CB342)
)

private fun Modifier.verticalScrollbar(state: ScrollState): Modifier =
    this.drawWithContent {
        drawContent()
        val scrollMax = state.maxValue.toFloat()
        if (scrollMax > 0f) {
            val viewH = size.height
            val thumbH = (viewH * viewH / (viewH + scrollMax)).coerceAtLeast(40f)
            val thumbY = (state.value.toFloat() / scrollMax) * (viewH - thumbH)
            val bw = 4.dp.toPx()
            drawRect(
                color = Color(0xFF9E9E9E).copy(alpha = if (state.isScrollInProgress) 0.9f else 0.5f),
                topLeft = Offset(size.width - bw - 2.dp.toPx(), thumbY),
                size = Size(bw, thumbH)
            )
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LadderGameScreen(onBack: () -> Unit) {
    var inputs by remember { mutableStateOf(listOf("꽝", "꽝")) }

    var phase by remember { mutableStateOf(LadderPhase.INPUT) }
    var items by remember { mutableStateOf<List<String>>(emptyList()) }
    var rungs by remember { mutableStateOf<List<Set<Int>>>(emptyList()) }
    var names by remember { mutableStateOf<List<String?>>(emptyList()) }

    var revealedPaths by remember { mutableStateOf<Map<Int, List<Int>>>(emptyMap()) }

    val scope = rememberCoroutineScope()
    var animProgress by remember { mutableStateOf(0f) }
    var animatingIndex by remember { mutableStateOf<Int?>(null) }
    var animatingPath by remember { mutableStateOf<List<Int>>(emptyList()) }
    var isAnimatingAll by remember { mutableStateOf(false) }
    var allAnimPaths by remember { mutableStateOf<Map<Int, List<Int>>>(emptyMap()) }
    var lastRevealedIndex by remember { mutableStateOf<Int?>(null) }
    var showResultsDialog by remember { mutableStateOf(false) }

    var dialogIndex by remember { mutableStateOf<Int?>(null) }
    var dialogText by remember { mutableStateOf("") }

    val isHidden = revealedPaths.isEmpty() && animatingIndex == null && !isAnimatingAll

    fun buildItems() = inputs.map { if (it.isBlank()) "꽝" else it.trim() }

    fun startSingleReveal(idx: Int) {
        if (animatingIndex != null || isAnimatingAll) return
        val path = tracePath(idx, rungs)
        animatingIndex = idx
        animatingPath = path
        lastRevealedIndex = idx
        animProgress = 0f
        scope.launch {
            animate(0f, 1f, animationSpec = tween(durationMillis = rungs.size * 200)) { v, _ ->
                animProgress = v
            }
            revealedPaths = revealedPaths + (idx to path)
            animatingIndex = null
            animatingPath = emptyList()
        }
    }

    fun startAllReveal() {
        if (animatingIndex != null || isAnimatingAll) return
        val paths = names.indices.associateWith { tracePath(it, rungs) }
        allAnimPaths = paths
        isAnimatingAll = true
        animProgress = 0f
        scope.launch {
            animate(0f, 1f, animationSpec = tween(durationMillis = rungs.size * 200)) { v, _ ->
                animProgress = v
            }
            revealedPaths = paths
            isAnimatingAll = false
            allAnimPaths = emptyMap()
            showResultsDialog = true
        }
    }

    // Design Ref: §F3 — 다시 하기: 새 사다리, 이름 유지, 바로 REVEALING (게임 가능 상태)
    fun restart() {
        val allItems = buildItems()
        items = allItems.shuffled()
        rungs = generateLadder(allItems.size)
        // names 유지 (이전 이름 그대로, 즉시 게임 가능)
        revealedPaths = emptyMap()
        animatingIndex = null
        animatingPath = emptyList()
        isAnimatingAll = false
        allAnimPaths = emptyMap()
        lastRevealedIndex = null
        showResultsDialog = false
        phase = LadderPhase.REVEALING
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("사다리 게임") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (phase != LadderPhase.INPUT) {
                            phase = LadderPhase.INPUT
                            revealedPaths = emptyMap()
                            animatingIndex = null
                            animatingPath = emptyList()
                            isAnimatingAll = false
                            allAnimPaths = emptyMap()
                            lastRevealedIndex = null
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
                onAddInput = { if (inputs.size < 10) inputs = inputs + "꽝" },
                onRemoveInput = { idx ->
                    if (inputs.size > 2) inputs = inputs.toMutableList().also { it.removeAt(idx) }
                },
                onStart = {
                    if (inputs.size >= 2) {
                        val allItems = buildItems()
                        items = allItems.shuffled()
                        rungs = generateLadder(allItems.size)
                        names = List(allItems.size) { null }
                        revealedPaths = emptyMap()
                        animatingIndex = null
                        isAnimatingAll = false
                        lastRevealedIndex = null
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
                isHidden = isHidden,
                revealedPaths = revealedPaths,
                lastRevealedIndex = lastRevealedIndex,
                animatingIndex = animatingIndex,
                animatingPath = animatingPath,
                isAnimatingAll = isAnimatingAll,
                allAnimPaths = allAnimPaths,
                animProgress = animProgress,
                onNameClick = { idx ->
                    if (phase == LadderPhase.NAMING) {
                        dialogIndex = idx
                        dialogText = names.getOrNull(idx) ?: "${idx + 1}"
                    } else {
                        if (idx in revealedPaths) {
                            lastRevealedIndex = idx
                        } else {
                            startSingleReveal(idx)
                        }
                    }
                },
                onRevealAll = { startAllReveal() },
                onRestart = { restart() },
                // Design Ref: §F2 — NAMING 단계 바로 시작: 숫자 자동 설정 후 REVEALING 진입
                onQuickStart = {
                    names = List(items.size) { "${it + 1}" }
                    phase = LadderPhase.REVEALING
                },
                modifier = Modifier.padding(padding)
            )
        }
    }

    dialogIndex?.let { idx ->
        AlertDialog(
            onDismissRequest = { dialogIndex = null },
            title = { Text("이름 입력") },
            text = {
                OutlinedTextField(
                    value = dialogText,
                    onValueChange = { dialogText = it },
                    label = { Text("${idx + 1}번 슬롯") },
                    placeholder = { Text("꽝") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val name = if (dialogText.isBlank()) "꽝" else dialogText.trim()
                    val newNames = names.toMutableList().also { it[idx] = name }
                    names = newNames
                    dialogIndex = null
                    dialogText = ""
                    if (newNames.all { !it.isNullOrBlank() }) {
                        phase = LadderPhase.REVEALING
                    }
                }) { Text("확인") }
            },
            dismissButton = {
                TextButton(onClick = { dialogIndex = null }) { Text("취소") }
            }
        )
    }

    if (showResultsDialog) {
        AlertDialog(
            onDismissRequest = { showResultsDialog = false },
            title = { Text("사다리 결과 🎯", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    names.indices.forEach { idx ->
                        val path = revealedPaths[idx]
                        val result = path?.let { items.getOrElse(it.last()) { "?" } } ?: "?"
                        val color = LADDER_PATH_COLORS[idx % LADDER_PATH_COLORS.size]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(modifier = Modifier.size(14.dp).background(color, CircleShape))
                            Text(
                                text = "${names[idx] ?: "${idx + 1}"} → $result",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { restart() }) { Text("다시 하기") }
            },
            confirmButton = {
                TextButton(onClick = { showResultsDialog = false }) { Text("닫기") }
            }
        )
    }
}

// Design Ref: §F4 — rows=20(기존 12), probability=0.75f(기존 50%) → 더 복잡한 사다리
private fun generateLadder(n: Int, rows: Int = 20): List<Set<Int>> =
    (0 until rows).map {
        val row = mutableSetOf<Int>()
        (0 until n - 1).forEach { col ->
            if (col - 1 !in row && Random.nextFloat() < 0.75f) row.add(col)
        }
        row
    }

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

private fun computeDotOffset(
    path: List<Int>, progress: Float, colW: Float, rowH: Float, canvasH: Float = 0f
): Offset {
    val numSegs = path.size - 1
    if (numSegs <= 0) return Offset(path[0] * colW + colW / 2f, 0f)
    val raw = (progress * numSegs).coerceIn(0f, numSegs.toFloat())
    val seg = raw.toInt().coerceIn(0, numSegs - 1)
    val frac = raw - seg
    val col = path[seg]
    val next = path.getOrElse(seg + 1) { col }
    val x1 = col * colW + colW / 2f
    val y1 = seg * rowH
    val x2 = next * colW + colW / 2f
    val y2 = if (canvasH > 0f && seg == numSegs - 1 && col == next) canvasH
             else (seg + 1) * rowH
    return if (col == next) {
        Offset(x1, y1 + (y2 - y1) * frac)
    } else {
        val vf = 0.7f
        if (frac <= vf) Offset(x1, y1 + ((seg + 1) * rowH - y1) * (frac / vf))
        else Offset(x1 + (x2 - x1) * ((frac - vf) / (1f - vf)), (seg + 1) * rowH)
    }
}

private fun DrawScope.drawLadderPath(
    path: List<Int>, color: Color, colW: Float, rowH: Float, strokeW: Float
) {
    for (i in 0 until path.size - 1) {
        val col = path[i]; val next = path[i + 1]
        val x = col * colW + colW / 2f
        val y1 = i * rowH
        val y2 = (i + 1) * rowH
        drawLine(color, Offset(x, y1), Offset(x, y2), strokeW, cap = StrokeCap.Round)
        if (col != next) {
            drawLine(color, Offset(x, y2), Offset(next * colW + colW / 2f, y2), strokeW, cap = StrokeCap.Round)
        }
    }
    val finalX = path.last() * colW + colW / 2f
    val lastSegEndY = (path.size - 1) * rowH
    if (lastSegEndY < size.height) {
        drawLine(color, Offset(finalX, lastSegEndY), Offset(finalX, size.height), strokeW, cap = StrokeCap.Round)
    }
}

private fun DrawScope.drawPartialLadderPath(
    path: List<Int>, progress: Float, color: Color, colW: Float, rowH: Float, strokeW: Float
) {
    val numSegs = path.size - 1
    if (numSegs <= 0) return
    val raw = (progress * numSegs).coerceIn(0f, numSegs.toFloat())
    val curSeg = raw.toInt().coerceIn(0, numSegs - 1)
    val frac = raw - curSeg

    for (i in 0 until curSeg) {
        val col = path[i]; val next = path[i + 1]
        val x = col * colW + colW / 2f
        val y1 = i * rowH
        val y2 = (i + 1) * rowH
        drawLine(color, Offset(x, y1), Offset(x, y2), strokeW, cap = StrokeCap.Round)
        if (col != next) {
            drawLine(color, Offset(x, y2), Offset(next * colW + colW / 2f, y2), strokeW, cap = StrokeCap.Round)
        }
    }

    val dotPos = computeDotOffset(path, progress, colW, rowH, size.height)
    val col = path[curSeg]; val next = path.getOrElse(curSeg + 1) { col }
    val x1 = col * colW + colW / 2f
    val y1 = curSeg * rowH
    val rungY = (curSeg + 1) * rowH
    val y2 = if (curSeg == numSegs - 1 && col == next) size.height else rungY

    if (frac <= 0.7f || col == next) {
        drawLine(color, Offset(x1, y1), Offset(x1, dotPos.y.coerceIn(y1, y2)), strokeW, cap = StrokeCap.Round)
    } else {
        drawLine(color, Offset(x1, y1), Offset(x1, rungY), strokeW, cap = StrokeCap.Round)
        drawLine(color, Offset(x1, rungY), Offset(dotPos.x, rungY), strokeW, cap = StrokeCap.Round)
    }
}

// Design Ref: §F1 — imePadding + IME 높이 감지 자동 하단 스크롤
@Composable
private fun LadderInputContent(
    inputs: List<String>,
    onInputChange: (Int, String) -> Unit,
    onAddInput: () -> Unit,
    onRemoveInput: (Int) -> Unit,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val imeBottom = WindowInsets.ime.getBottom(density)

    // 항목 추가 시 하단 스크롤
    LaunchedEffect(inputs.size) {
        kotlinx.coroutines.delay(50)
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    // 키보드 올라올 때 자동 하단 스크롤
    LaunchedEffect(imeBottom) {
        if (imeBottom > 0) {
            kotlinx.coroutines.delay(100)
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScrollbar(scrollState)
            .verticalScroll(scrollState)
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
                        Icon(Icons.Default.Close, "삭제", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        if (inputs.size < 10) {
            OutlinedButton(onClick = onAddInput, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("항목 추가")
            }
        }

        Spacer(Modifier.height(8.dp))
        Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) {
            Text("사다리 만들기")
        }
    }
}

@Composable
private fun LadderGameContent(
    names: List<String?>,
    items: List<String>,
    rungs: List<Set<Int>>,
    isNaming: Boolean,
    isHidden: Boolean,
    revealedPaths: Map<Int, List<Int>>,
    lastRevealedIndex: Int?,
    animatingIndex: Int?,
    animatingPath: List<Int>,
    isAnimatingAll: Boolean,
    allAnimPaths: Map<Int, List<Int>>,
    animProgress: Float,
    onNameClick: (Int) -> Unit,
    onRevealAll: () -> Unit,
    onRestart: () -> Unit,
    onQuickStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val n = items.size
    val fontSize = if (n > 6) 10.sp else 12.sp
    val isAnimating = animatingIndex != null || isAnimatingAll

    val itemColorMap = revealedPaths.entries.associate { (nameIdx, path) -> path.last() to nameIdx }

    val lastPath = lastRevealedIndex?.let { revealedPaths[it] }
    val lastResult = lastPath?.let { items.getOrElse(it.last()) { "" } }

    Column(modifier = modifier.fillMaxSize().padding(horizontal = 12.dp)) {
        Spacer(Modifier.height(6.dp))

        when {
            isNaming -> Text(
                "이름을 입력하세요 (${names.count { !it.isNullOrBlank() }}/$n)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
            )
            isAnimating -> Text(
                "사다리 이동 중...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
            )
            isHidden -> Text(
                "이름을 클릭하면 결과를 확인할 수 있어요",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
            )
            lastRevealedIndex != null && lastResult != null -> Text(
                "🎯 ${names[lastRevealedIndex]} → $lastResult",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LADDER_PATH_COLORS[lastRevealedIndex % LADDER_PATH_COLORS.size],
                modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            names.forEachIndexed { idx, name ->
                val isFilled = !name.isNullOrBlank()
                val isRevealed = idx in revealedPaths
                val isCurrAnim = animatingIndex == idx || isAnimatingAll
                val pathColor = LADDER_PATH_COLORS[idx % LADDER_PATH_COLORS.size]
                val bgColor = when {
                    isCurrAnim || isRevealed -> pathColor
                    isFilled -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
                val textColor = when {
                    isCurrAnim || isRevealed -> Color.White
                    isFilled -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                Surface(
                    onClick = { onNameClick(idx) },
                    modifier = Modifier.weight(1f).padding(horizontal = 2.dp),
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
                        fontWeight = if (isRevealed || isCurrAnim) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // Design Ref: §F2 — NAMING 단계: 바로 시작 버튼 가운데 표시
        if (isNaming) {
            Spacer(Modifier.height(6.dp))
            Button(
                onClick = onQuickStart,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("바로 시작")
            }
        }

        if (!isNaming) {
            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onRestart,
                    enabled = !isAnimating,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("다시 하기", fontSize = 13.sp)
                }
                OutlinedButton(
                    onClick = onRevealAll,
                    enabled = !isAnimating,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("모두 보기", fontSize = 13.sp)
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        LadderCanvas(
            n = n,
            rungs = rungs,
            isHidden = isHidden,
            revealedPaths = revealedPaths,
            animatingIndex = animatingIndex,
            animatingPath = animatingPath,
            isAnimatingAll = isAnimatingAll,
            allAnimPaths = allAnimPaths,
            animProgress = animProgress,
            modifier = Modifier.fillMaxWidth().weight(1f)
        )

        Spacer(Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            items.forEachIndexed { idx, item ->
                val nameIdx = itemColorMap[idx]
                val itemColor = nameIdx?.let { LADDER_PATH_COLORS[it % LADDER_PATH_COLORS.size] }
                Surface(
                    modifier = Modifier.weight(1f).padding(horizontal = 2.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = itemColor ?: MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = item,
                        modifier = Modifier
                            .padding(vertical = 10.dp, horizontal = 2.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontSize = fontSize,
                        color = if (itemColor != null) Color.White
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = if (itemColor != null) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun LadderCanvas(
    n: Int,
    rungs: List<Set<Int>>,
    isHidden: Boolean,
    revealedPaths: Map<Int, List<Int>>,
    animatingIndex: Int?,
    animatingPath: List<Int>,
    isAnimatingAll: Boolean,
    allAnimPaths: Map<Int, List<Int>>,
    animProgress: Float,
    modifier: Modifier = Modifier
) {
    val outlineColor = MaterialTheme.colorScheme.outline
    val overlayColor = MaterialTheme.colorScheme.surfaceVariant
    val overlayTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (n <= 0 || rungs.isEmpty()) return@Canvas
            val colW = size.width / n
            val rowH = size.height / (rungs.size + 1)
            val stroke = 3.dp.toPx()
            val pathStroke = 5.dp.toPx()
            val dotR = 8.dp.toPx()

            for (col in 0 until n) {
                val x = col * colW + colW / 2f
                drawLine(outlineColor, Offset(x, 0f), Offset(x, size.height), stroke, cap = StrokeCap.Round)
            }

            if (!isHidden) {
                rungs.forEachIndexed { row, rowRungs ->
                    val y = (row + 1) * rowH
                    rowRungs.forEach { col ->
                        drawLine(
                            outlineColor,
                            Offset(col * colW + colW / 2f, y),
                            Offset((col + 1) * colW + colW / 2f, y),
                            stroke, cap = StrokeCap.Round
                        )
                    }
                }

                revealedPaths.forEach { (nameIdx, path) ->
                    drawLadderPath(
                        path, LADDER_PATH_COLORS[nameIdx % LADDER_PATH_COLORS.size],
                        colW, rowH, pathStroke
                    )
                }

                if (animatingIndex != null && animatingPath.size >= 2) {
                    val color = LADDER_PATH_COLORS[animatingIndex % LADDER_PATH_COLORS.size]
                    drawPartialLadderPath(animatingPath, animProgress, color, colW, rowH, pathStroke)
                    val dot = computeDotOffset(animatingPath, animProgress, colW, rowH, size.height)
                    drawCircle(color, dotR, dot)
                    drawCircle(Color.White, dotR * 0.45f, dot)
                }

                if (isAnimatingAll) {
                    allAnimPaths.forEach { (nameIdx, path) ->
                        if (path.size >= 2) {
                            val color = LADDER_PATH_COLORS[nameIdx % LADDER_PATH_COLORS.size]
                            drawPartialLadderPath(path, animProgress, color, colW, rowH, pathStroke)
                            val dot = computeDotOffset(path, animProgress, colW, rowH, size.height)
                            drawCircle(color, dotR, dot)
                            drawCircle(Color.White, dotR * 0.45f, dot)
                        }
                    }
                }
            }
        }

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
