package com.kaixuan.weightloss.ui

import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kaixuan.weightloss.data.WeightRecord
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineSpec
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.shader.verticalGradient
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.shader.DynamicShader
import com.patrykandpatrick.vico.core.common.shape.Shape
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WeightChart(records: List<WeightRecord>) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val surfaceColor = MaterialTheme.colorScheme.surface

    val modelProducer = remember { CartesianChartModelProducer.build() }

    val dateFormat = remember { SimpleDateFormat("MM/dd", Locale.getDefault()) }

    remember(records) {
        if (records.isNotEmpty()) {
            modelProducer.tryRunTransaction {
                lineSeries {
                    series(records.map { it.weight.toDouble() })
                }
            }
        }
    }

    // 创建 Marker 用于显示点击时的数据
    val marker = rememberDefaultCartesianMarker(
        label = rememberTextComponent(
            color = surfaceColor,
            padding = Dimensions(8f),
            background = rememberShapeComponent(
                shape = Shape.rounded(allPercent = 25),
                color = primaryColor,
            ),
        ),
        valueFormatter = { _, targets ->
            val index = targets.firstOrNull()?.x?.toInt() ?: return@rememberDefaultCartesianMarker ""
            if (index in records.indices) {
                val record = records[index]
                val date = dateFormat.format(Date(record.date))
                "$date: %.1f kg".format(record.weight)
            } else ""
        },
    )

    val chart = rememberCartesianChart(
        rememberLineCartesianLayer(
            lines = listOf(
                rememberLineSpec(
                    shader = DynamicShader.verticalGradient(arrayOf(primaryColor, primaryColor)),
                    backgroundShader = DynamicShader.verticalGradient(
                        arrayOf(primaryColor.copy(alpha = 0.2f), Color.Transparent)
                    ),
                    point = rememberShapeComponent(
                        shape = Shape.Pill,
                        color = primaryColor,
                        strokeColor = surfaceColor,
                        strokeWidth = 2.dp
                    ),
                    pointSize = 8.dp,
                )
            ),
            spacing = 16.dp,
        ),
        startAxis = rememberStartAxis(
            label = rememberTextComponent(
                color = onSurfaceColor.copy(alpha = 0.6f),
            ),
            guideline = null,
        ),
        bottomAxis = rememberBottomAxis(
            label = rememberTextComponent(
                color = onSurfaceColor.copy(alpha = 0.6f),
            ),
            valueFormatter = { value, _, _ ->
                if (value.toInt() in records.indices) {
                    dateFormat.format(Date(records[value.toInt()].date))
                } else {
                    ""
                }
            },
            guideline = null,
        ),
    )

    CartesianChartHost(
        chart = chart,
        modelProducer = modelProducer,
        modifier = Modifier.height(250.dp),
        marker = marker,
    )
}
