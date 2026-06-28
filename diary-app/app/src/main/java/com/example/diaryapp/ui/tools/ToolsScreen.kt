package com.example.diaryapp.ui.tools

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Design Ref: kbo-standings-ui-fix §변경2 — 아이콘 슬롯을 composable 람다로 교체
@Composable
fun ToolsContent(
    onNavigateToLadder: () -> Unit,
    onNavigateToKbo: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val tools = listOf(
        Triple<@Composable () -> Unit, String, () -> Unit>(
            { Text("🪜", fontSize = 40.sp) },
            "사다리 게임",
            onNavigateToLadder
        ),
        Triple<@Composable () -> Unit, String, () -> Unit>(
            { Text("⚾", fontSize = 40.sp) },
            "프로야구 순위",
            onNavigateToKbo
        )
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(tools.size) { idx ->
            val (iconContent, title, onClick) = tools[idx]
            ToolCard(iconContent = iconContent, title = title, onClick = onClick)
        }
    }
}

@Composable
private fun ToolCard(
    iconContent: @Composable () -> Unit,
    title: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            iconContent()
            Spacer(Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}
