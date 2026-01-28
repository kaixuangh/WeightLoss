package com.kaixuan.weightloss.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightScreen(
    viewModel: WeightViewModel,
    onSettingsClick: () -> Unit
) {
    val records by viewModel.records.collectAsState()
    val selectedRange by viewModel.selectedRange.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val latestWeight by viewModel.latestWeight.collectAsState()

    var weightInput by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    val unit = settings.weightUnit

    // 计算目标差距
    val currentWeightDisplay = latestWeight?.let { viewModel.convertWeight(it, unit) }
    val targetWeightDisplay = if (settings.targetWeight > 0) {
        viewModel.convertWeight(settings.targetWeight, unit)
    } else null
    val weightDiff = if (currentWeightDisplay != null && targetWeightDisplay != null && targetWeightDisplay > 0) {
        currentWeightDisplay - targetWeightDisplay
    } else null

    // 计算 BMI
    val bmi = latestWeight?.let { viewModel.calculateBMI(it, settings.height) } ?: 0f
    val bmiStatus = viewModel.getBMIStatus(bmi)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("体重记录") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "设置",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
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
            // 当前状态卡片 (BMI + 目标)
            if (latestWeight != null && (bmi > 0 || weightDiff != null)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "当前状态",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // 当前体重
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "当前体重",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "%.1f ${unit.label}".format(currentWeightDisplay),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }

                            // BMI
                            if (bmi > 0) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "BMI",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = "%.1f".format(bmi),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        text = bmiStatus,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = when {
                                            bmi < 18.5 || bmi >= 28 -> MaterialTheme.colorScheme.error
                                            bmi >= 24 -> MaterialTheme.colorScheme.tertiary
                                            else -> MaterialTheme.colorScheme.primary
                                        }
                                    )
                                }
                            }

                            // 距离目标
                            if (weightDiff != null) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "距离目标",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = if (weightDiff > 0) "-%.1f ${unit.label}".format(weightDiff)
                                        else "已达成",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = if (weightDiff > 0) MaterialTheme.colorScheme.tertiary
                                        else MaterialTheme.colorScheme.primary
                                    )
                                    if (weightDiff > 0) {
                                        Text(
                                            text = "目标: %.1f ${unit.label}".format(targetWeightDisplay),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

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
                        label = { Text("体重 (${unit.label})") },
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
                        WeightChart(records = records, weightUnit = unit)
                        Spacer(modifier = Modifier.height(8.dp))
                        WeightStats(records = records, weightUnit = unit)
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
fun WeightStats(records: List<WeightRecord>, weightUnit: com.kaixuan.weightloss.data.WeightUnit) {
    if (records.isEmpty()) return

    val factor = weightUnit.factor
    val maxWeight = records.maxOf { it.weight } * factor
    val minWeight = records.minOf { it.weight } * factor
    val avgWeight = records.map { it.weight }.average().toFloat() * factor
    val latestWeight = (records.lastOrNull()?.weight ?: 0f) * factor
    val firstWeight = (records.firstOrNull()?.weight ?: 0f) * factor
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
                StatItem("最高", "%.1f ${weightUnit.label}".format(maxWeight))
                StatItem("最低", "%.1f ${weightUnit.label}".format(minWeight))
                StatItem("平均", "%.1f ${weightUnit.label}".format(avgWeight))
                StatItem(
                    "变化",
                    "%+.1f ${weightUnit.label}".format(change),
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
