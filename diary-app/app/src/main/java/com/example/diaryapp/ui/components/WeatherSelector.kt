package com.example.diaryapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.diaryapp.data.model.WeatherTag

// Design Ref: §4.4 — 감정 태그 아래 배치, 파스텔 선택 칩 (SC-03, FR-04, FR-05)
// Design Ref: multi-emotion-weather-select §CHANGE-06 — Set 기반 다중 선택
// Design Ref: emotion-weather-limit-required §CHANGE-05 — maxReached, 카운트 헤더, disabled
@Composable
fun WeatherSelector(
    selected: Set<WeatherTag>,
    onSelect: (WeatherTag) -> Unit,
    modifier: Modifier = Modifier,
    maxReached: Boolean = false
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "날씨",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "(${selected.size}/3)",
                style = MaterialTheme.typography.labelSmall,
                color = if (selected.isEmpty()) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(WeatherTag.entries) { weather ->
                val isSelected = weather in selected
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelect(weather) },
                    enabled = isSelected || !maxReached,
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
