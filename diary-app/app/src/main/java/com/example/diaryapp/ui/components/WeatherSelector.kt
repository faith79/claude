package com.example.diaryapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.diaryapp.data.model.WeatherTag

// Design Ref: §4.4 — 감정 태그 아래 배치, 파스텔 선택 칩 (SC-03, FR-04, FR-05)
@Composable
fun WeatherSelector(
    selected: WeatherTag?,
    onSelect: (WeatherTag) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "날씨",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(WeatherTag.entries) { weather ->
                val isSelected = selected == weather
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        // 같은 항목 재클릭 시 선택 해제
                        onSelect(weather)
                    },
                    label = {
                        Text("${weather.emoji} ${weather.label}")
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    }
}
