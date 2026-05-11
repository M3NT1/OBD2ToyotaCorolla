package com.obd2corolla.ui.screens.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.obd2corolla.domain.model.SensorData
import com.obd2corolla.ui.theme.ErrorRed
import com.obd2corolla.ui.theme.SensorBattery
import com.obd2corolla.ui.theme.SensorFuel
import com.obd2corolla.ui.theme.SensorRpm
import com.obd2corolla.ui.theme.SensorSpeed
import com.obd2corolla.ui.theme.SuccessGreen
import com.obd2corolla.ui.theme.WarningOrange

@Composable
fun SensorCard(
    sensor: SensorData,
    modifier: Modifier = Modifier
) {
    val animatedValue by animateFloatAsState(
        targetValue = sensor.value,
        animationSpec = tween(durationMillis = 500),
        label = "sensor_value"
    )

    val thresholdColor = getThresholdColor(sensor)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedGaugeArc(
                value = animatedValue,
                maxValue = sensor.maxValue,
                minValue = sensor.minValue,
                size = 100.dp,
                strokeWidth = 10.dp,
                color = thresholdColor,
                label = sensor.name,
                unit = sensor.unit
            )

            Text(
                text = sensor.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Min: ${sensor.minValue.toInt()} | Max: ${sensor.maxValue.toInt()}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun AnimatedGaugeArc(
    value: Float,
    maxValue: Float,
    minValue: Float,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    strokeWidth: Dp = 12.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    label: String = "",
    unit: String = ""
) {
    val progress = ((value - minValue) / (maxValue - minValue)).coerceIn(0f, 1f)
    val sweepAngle = 240f * progress

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val arcSize = Size(size.toPx() - strokeWidth.toPx(), size.toPx() - strokeWidth.toPx())
            val topLeft = Offset(strokeWidth.toPx() / 2, strokeWidth.toPx() / 2)

            drawArc(
                color = backgroundColor,
                startAngle = 150f,
                sweepAngle = 240f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round),
                size = arcSize,
                topLeft = topLeft
            )

            drawArc(
                color = color,
                startAngle = 150f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round),
                size = arcSize,
                topLeft = topLeft
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "%.0f".format(value),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (unit.isNotEmpty()) {
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun getThresholdColor(sensor: SensorData): Color {
    val value = sensor.value
    return when {
        sensor.name.contains("RPM", ignoreCase = true) -> when {
            value >= 7000 -> ErrorRed
            value >= 6500 -> WarningOrange
            else -> SensorRpm
        }
        sensor.name.contains("Speed", ignoreCase = true) -> when {
            value >= 140 -> ErrorRed
            value >= 120 -> WarningOrange
            else -> SensorSpeed
        }
        sensor.name.contains("Coolant", ignoreCase = true) ||
        sensor.name.contains("Temp", ignoreCase = true) -> when {
            value >= 110 -> ErrorRed
            value >= 100 -> WarningOrange
            else -> Color(0xFFFF5722)
        }
        sensor.name.contains("Fuel", ignoreCase = true) -> when {
            value <= 5 -> ErrorRed
            value <= 10 -> WarningOrange
            else -> Color(0xFFFFEB3B)
        }
        sensor.name.contains("Voltage", ignoreCase = true) ||
        sensor.name.contains("Battery", ignoreCase = true) -> when {
            value < 10 -> ErrorRed
            value < 11 -> WarningOrange
            else -> SensorBattery
        }
        sensor.name.contains("Throttle", ignoreCase = true) -> Color(0xFF9C27B0)
        else -> SuccessGreen
    }
}