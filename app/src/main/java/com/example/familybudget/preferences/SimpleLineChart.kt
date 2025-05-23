package com.example.familybudget.preferences

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.familybudget.dto.TransactionDto
import org.threeten.bp.LocalDateTime
import kotlin.math.abs
import kotlin.math.max

@Composable
fun SimpleLineChart(transactions: List<TransactionDto>, isIncome: Boolean) {
    val sortedTransactions = remember(transactions) {
        transactions.sortedBy {
            try {
                LocalDateTime.parse(it.date)
            } catch (_: Exception) {
                LocalDateTime.MIN
            }
        }
    }

    val points = sortedTransactions.map { abs(it.amount.toFloat()) }
    val dates = sortedTransactions.map { it.date.take(10) }

    val progress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "ChartProgress"
    )

    var selectedPointIndex by remember { mutableStateOf(-1) }

    val colorScheme = MaterialTheme.colorScheme
    val highlightColor = if (isIncome) Color(0xFF4CAF50) else Color(0xFFF44336)
    val isDark = isSystemInDarkTheme()

    val labelColor = if (isDark) Color.LightGray else Color.DarkGray
    val gridLineColor = colorScheme.onSurface.copy(alpha = 0.15f)
    val shadowColor = Color.Black.copy(alpha = 0.08f)
    val surfaceColor = colorScheme.surface
    val onSurface = colorScheme.onSurface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp)
            .pointerInput(points) {
                detectTapGestures { tapOffset ->
                    if (points.size > 1) {
                        val stepX = size.width / (points.size - 1)
                        selectedPointIndex = points.indices.minByOrNull { i ->
                            val x = i * stepX
                            abs(x - tapOffset.x)
                        } ?: -1
                    } else {
                        selectedPointIndex = -1
                    }
                }
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 40.dp, end = 16.dp, top = 32.dp, bottom = 40.dp)
        ) {
            if (points.size < 2) return@Canvas

            val maxY = points.maxOrNull() ?: 0f
            val minY = points.minOrNull() ?: 0f
            val rangeY = (maxY - minY).takeIf { it > 0f } ?: 1f
            val stepX = size.width / (points.size - 1)
            val yAxisSteps = 4

            val coords = points.mapIndexed { index, y ->
                val x = index * stepX
                val scaledY = size.height - ((y - minY) / rangeY) * size.height
                Offset(x, scaledY)
            }

            // Y axis
            for (i in 0..yAxisSteps) {
                val y = size.height * i / yAxisSteps
                val labelValue = maxY - (rangeY * i / yAxisSteps)

                drawLine(
                    color = gridLineColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )

                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        isAntiAlias = true
                        textSize = 28f
                        color = labelColor.toArgb()
                        setShadowLayer(4f, 0f, 0f, android.graphics.Color.argb(50, 0, 0, 0))
                    }
                    drawText(String.format("%.0f ₽", labelValue), -36f, y + 10f, paint)
                }
            }

            val path = Path().apply {
                moveTo(coords[0].x, coords[0].y)
                for (i in 1 until coords.size) {
                    val prev = coords[i - 1]
                    val curr = coords[i]
                    val midX = (prev.x + curr.x) / 2
                    cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                }
            }

            val fillPath = Path().apply {
                addPath(path)
                lineTo(coords.last().x, size.height)
                lineTo(coords.first().x, size.height)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(highlightColor.copy(alpha = 0.15f), Color.Transparent),
                    startY = 0f,
                    endY = size.height
                ),
                alpha = progress
            )

            drawPath(
                path = path,
                color = shadowColor,
                style = Stroke(width = 8f, cap = StrokeCap.Round),
                alpha = progress
            )

            drawPath(
                path = path,
                brush = Brush.horizontalGradient(
                    colors = listOf(highlightColor, highlightColor.copy(alpha = 0.85f))
                ),
                style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                alpha = progress
            )

            coords.forEach { point ->
                drawCircle(Color.White, radius = 9f, center = point, alpha = progress)
                drawCircle(highlightColor, radius = 5f, center = point, alpha = progress)
                drawCircle(highlightColor.copy(alpha = 0.2f), radius = 12f, center = point, alpha = progress)
            }

            // X axis (dates)
            val maxLabels = 4
            val labelIndices = if (points.size <= maxLabels) {
                coords.indices.toList()
            } else {
                List(maxLabels) { i -> ((points.size - 1) * i / (maxLabels - 1)).coerceAtMost(points.size - 1) }
            }

            var textSize = when {
                points.size <= 4 -> 24f
                points.size <= 8 -> 20f
                else -> 16f
            }

            labelIndices.forEach { i ->
                val label = dates.getOrNull(i)?.replace("-", ".") ?: ""
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        isAntiAlias = true
                        this.textSize = textSize
                        color = labelColor.toArgb()
                        setShadowLayer(3f, 0f, 0f, android.graphics.Color.argb(80, 0, 0, 0))
                    }
                    drawText(label, coords[i].x - textSize, size.height + 28f, paint)
                }
            }

            // Tooltip
            if (selectedPointIndex in coords.indices) {
                val point = coords[selectedPointIndex]
                val amount = points[selectedPointIndex]

                val tooltipWidth = 124f
                val tooltipHeight = 50f
                val offsetX = when {
                    point.x + tooltipWidth / 2f > size.width -> size.width - tooltipWidth
                    point.x - tooltipWidth / 2f < 0f -> 0f
                    else -> point.x - tooltipWidth / 2f
                }

                val offsetY = (point.y - tooltipHeight - 16f).coerceAtLeast(0f)

                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.2f),
                    topLeft = Offset(offsetX, offsetY),
                    size = Size(tooltipWidth, tooltipHeight),
                    cornerRadius = CornerRadius(14f, 14f)
                )

                drawRoundRect(
                    color = surfaceColor,
                    topLeft = Offset(offsetX - 2f, offsetY - 2f),
                    size = Size(tooltipWidth, tooltipHeight),
                    cornerRadius = CornerRadius(14f, 14f)
                )

                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        isAntiAlias = true
                        textSize = 28f
                        color = onSurface.toArgb()
                        setShadowLayer(4f, 0f, 0f, android.graphics.Color.argb(80, 0, 0, 0))
                    }
                    drawText(String.format("%.2f ₽", amount), offsetX + 14f, offsetY + 32f, paint)
                }
            }
        }
    }
}







