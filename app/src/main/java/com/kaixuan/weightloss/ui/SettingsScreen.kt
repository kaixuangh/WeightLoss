package com.kaixuan.weightloss.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.kaixuan.weightloss.WeightViewModel
import com.kaixuan.weightloss.data.WeightUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: WeightViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val latestWeight by viewModel.latestWeight.collectAsState()
    val context = LocalContext.current

    var targetWeightInput by remember(settings.targetWeight, settings.weightUnit) {
        val displayValue = if (settings.targetWeight > 0) {
            viewModel.convertWeight(settings.targetWeight, settings.weightUnit)
        } else 0f
        mutableStateOf(if (displayValue > 0) "%.1f".format(displayValue) else "")
    }

    var heightInput by remember(settings.height) {
        mutableStateOf(if (settings.height > 0) "%.1f".format(settings.height) else "")
    }

    var reminderEnabled by remember(settings.reminderEnabled) {
        mutableStateOf(settings.reminderEnabled)
    }

    var showTimePicker by remember { mutableStateOf(false) }
    var selectedHour by remember(settings.reminderHour) { mutableStateOf(settings.reminderHour) }
    var selectedMinute by remember(settings.reminderMinute) { mutableStateOf(settings.reminderMinute) }

    // 通知权限
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            reminderEnabled = true
            viewModel.updateReminder(true, selectedHour, selectedMinute)
        }
    }

    // 计算 BMI
    val currentWeight = latestWeight ?: 0f
    val bmi = viewModel.calculateBMI(currentWeight, settings.height)
    val bmiStatus = viewModel.getBMIStatus(bmi)

    // 处理返回键
    BackHandler {
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
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
            // 身高设置卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "身高设置",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = heightInput,
                            onValueChange = { heightInput = it },
                            label = { Text("身高 (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                heightInput.toFloatOrNull()?.let { viewModel.updateHeight(it) }
                            }
                        ) {
                            Text("保存")
                        }
                    }

                    // BMI 显示
                    if (bmi > 0) {
                        HorizontalDivider()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("当前 BMI")
                            Text(
                                text = "%.1f ($bmiStatus)".format(bmi),
                                style = MaterialTheme.typography.titleMedium,
                                color = when {
                                    bmi < 18.5 || bmi >= 28 -> MaterialTheme.colorScheme.error
                                    bmi >= 24 -> MaterialTheme.colorScheme.tertiary
                                    else -> MaterialTheme.colorScheme.primary
                                }
                            )
                        }
                    }
                }
            }

            // 目标体重卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "目标体重",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = targetWeightInput,
                            onValueChange = { targetWeightInput = it },
                            label = { Text("目标 (${settings.weightUnit.label})") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                targetWeightInput.toFloatOrNull()?.let { viewModel.updateTargetWeight(it) }
                            }
                        ) {
                            Text("保存")
                        }
                    }

                    if (settings.targetWeight > 0 && latestWeight != null) {
                        val currentDisplay = viewModel.convertWeight(latestWeight!!, settings.weightUnit)
                        val targetDisplay = viewModel.convertWeight(settings.targetWeight, settings.weightUnit)
                        val diff = currentDisplay - targetDisplay

                        HorizontalDivider()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("距离目标")
                            Text(
                                text = if (diff > 0) "还需减 %.1f ${settings.weightUnit.label}".format(diff)
                                else "已达成目标",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (diff > 0) MaterialTheme.colorScheme.tertiary
                                else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // 单位设置卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "体重单位",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        WeightUnit.entries.forEach { unit ->
                            FilterChip(
                                selected = settings.weightUnit == unit,
                                onClick = { viewModel.updateWeightUnit(unit) },
                                label = { Text(unit.label) }
                            )
                        }
                    }
                }
            }

            // 提醒设置卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "每日提醒",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("开启提醒")
                        Switch(
                            checked = reminderEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    // 检查通知权限 (Android 13+)
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        when (ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.POST_NOTIFICATIONS
                                        )) {
                                            PackageManager.PERMISSION_GRANTED -> {
                                                reminderEnabled = true
                                                viewModel.updateReminder(true, selectedHour, selectedMinute)
                                            }
                                            else -> {
                                                notificationPermissionLauncher.launch(
                                                    Manifest.permission.POST_NOTIFICATIONS
                                                )
                                            }
                                        }
                                    } else {
                                        reminderEnabled = true
                                        viewModel.updateReminder(true, selectedHour, selectedMinute)
                                    }
                                } else {
                                    reminderEnabled = false
                                    viewModel.updateReminder(false, selectedHour, selectedMinute)
                                }
                            }
                        )
                    }

                    if (reminderEnabled) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("提醒时间")
                            TextButton(onClick = { showTimePicker = true }) {
                                Text("%02d:%02d".format(selectedHour, selectedMinute))
                            }
                        }
                    }
                }
            }
        }
    }

    // 时间选择器
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedHour,
            initialMinute = selectedMinute
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("选择提醒时间") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedHour = timePickerState.hour
                        selectedMinute = timePickerState.minute
                        viewModel.updateReminder(reminderEnabled, selectedHour, selectedMinute)
                        showTimePicker = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("取消")
                }
            }
        )
    }
}
