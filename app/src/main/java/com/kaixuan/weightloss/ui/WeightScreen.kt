package com.kaixuan.weightloss.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kaixuan.weightloss.TimeRange
import com.kaixuan.weightloss.WeightViewModel
import com.kaixuan.weightloss.data.WeightRecord
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightScreen(viewModel: WeightViewModel) {
    val records by viewModel.records.collectAsState()
    val selectedRange by viewModel.selectedRange.collectAsState()
    var weightInput by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("体重记录") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 输入体重卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "记录今日体重",
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = {
                            weightInput = it
                            showError = false
                        },
                        label = { Text("体重 (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = showError,
                        supportingText = if (showError) {
                            { Text("请输入有效的体重") }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            val weight = weightInput.toFloatOrNull()
                            if (weight != null && weight > 0) {
                                viewModel.addRecord(weight)
                                weightInput = ""
                                showError = false
                            } else {
                                showError = true
                            }
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("添加记录")
                    }
                }
            }

            // 时间范围选择器
            TimeRangeSelector(
                selectedRange = selectedRange,
                onRangeSelected = { viewModel.selectTimeRange(it) }
            )

            // 图表卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "体重趋势",
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (records.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "暂无数据",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        WeightChart(records = records)
                        Spacer(modifier = Modifier.height(8.dp))
                        WeightStats(records = records)
                    }
                }
            }
        }
    }
}

@Composable
fun TimeRangeSelector(
    selectedRange: TimeRange,
    onRangeSelected: (TimeRange) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "时间范围",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TimeRange.entries.forEach { range ->
                    FilterChip(
                        selected = selectedRange == range,
                        onClick = { onRangeSelected(range) },
                        label = {
                            Text(
                                text = range.label,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        modifier = Modifier.wrapContentWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun WeightStats(records: List<WeightRecord>) {
    if (records.isEmpty()) return

    val maxWeight = records.maxOf { it.weight }
    val minWeight = records.minOf { it.weight }
    val avgWeight = records.map { it.weight }.average().toFloat()
    val latestWeight = records.lastOrNull()?.weight ?: 0f
    val firstWeight = records.firstOrNull()?.weight ?: 0f
    val change = latestWeight - firstWeight

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "统计信息",
                style = MaterialTheme.typography.titleSmall
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("最高", "%.1f kg".format(maxWeight))
                StatItem("最低", "%.1f kg".format(minWeight))
                StatItem("平均", "%.1f kg".format(avgWeight))
                StatItem(
                    "变化",
                    "%+.1f kg".format(change),
                    color = if (change < 0) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: androidx.compose.ui.graphics.Color? = null) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = color ?: MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}
