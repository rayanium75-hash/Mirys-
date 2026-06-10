package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun BrandLogo(
    modifier: Modifier = Modifier,
    size: Dp = 80.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.22f))
            .background(Color(0xFF060B13))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.toPx()
            val height = size.toPx()

            // Outer cyan glowing border
            drawRoundRect(
                color = Color(0xFF1AA3FF).copy(alpha = 0.8f),
                style = Stroke(width = width * 0.035f),
                cornerRadius = CornerRadius(width * 0.22f)
            )

            // Draw Bottom Horizon Glow Curve
            val glowPath = Path().apply {
                moveTo(0f, height * 0.85f)
                quadraticTo(width * 0.5f, height * 0.95f, width, height * 0.85f)
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }
            drawPath(
                path = glowPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0x001AA3FF), Color(0xAA39B5FF), Color(0xFF3DF5FF))
                )
            )

            // Draw a shiny sharp horizon line
            val linePath = Path().apply {
                moveTo(0f, height * 0.88f)
                quadraticTo(width * 0.5f, height * 0.96f, width, height * 0.88f)
            }
            drawPath(
                path = linePath,
                color = Color(0xFF3DF5FF),
                style = Stroke(width = width * 0.015f, cap = StrokeCap.Round)
            )

            // Left Wing
            val leftWingPath = Path().apply {
                moveTo(width * 0.35f, height * 0.50f)
                quadraticTo(width * 0.20f, height * 0.44f, width * 0.14f, height * 0.46f)
                quadraticTo(width * 0.18f, height * 0.56f, width * 0.35f, height * 0.62f)
                close()
            }
            drawPath(
                path = leftWingPath,
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF80E5FF), Color(0xFF1AA3FF))
                )
            )

            val leftWingPath2 = Path().apply {
                moveTo(width * 0.35f, height * 0.58f)
                quadraticTo(width * 0.18f, height * 0.54f, width * 0.16f, height * 0.58f)
                quadraticTo(width * 0.22f, height * 0.66f, width * 0.35f, height * 0.68f)
                close()
            }
            drawPath(
                path = leftWingPath2,
                color = Color(0xFF0073E6)
            )

            // Right Wing
            val rightWingPath = Path().apply {
                moveTo(width * 0.65f, height * 0.50f)
                quadraticTo(width * 0.80f, height * 0.44f, width * 0.86f, height * 0.46f)
                quadraticTo(width * 0.82f, height * 0.56f, width * 0.65f, height * 0.62f)
                close()
            }
            drawPath(
                path = rightWingPath,
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF1AA3FF), Color(0xFF80E5FF))
                )
            )

            val rightWingPath2 = Path().apply {
                moveTo(width * 0.65f, height * 0.58f)
                quadraticTo(width * 0.82f, height * 0.54f, width * 0.84f, height * 0.58f)
                quadraticTo(width * 0.78f, height * 0.66f, width * 0.65f, height * 0.68f)
                close()
            }
            drawPath(
                path = rightWingPath2,
                color = Color(0xFF0073E6)
            )

            // Draw original stylized letter body M with gradient
            val mPath = Path().apply {
                moveTo(width * 0.34f, height * 0.74f)
                lineTo(width * 0.42f, height * 0.74f)
                lineTo(width * 0.44f, height * 0.55f)
                lineTo(width * 0.50f, height * 0.65f)
                lineTo(width * 0.56f, height * 0.55f)
                lineTo(width * 0.58f, height * 0.74f)
                lineTo(width * 0.66f, height * 0.74f)
                lineTo(width * 0.70f, height * 0.48f)
                lineTo(width * 0.62f, height * 0.48f)
                lineTo(width * 0.50f, height * 0.59f)
                lineTo(width * 0.38f, height * 0.48f)
                lineTo(width * 0.30f, height * 0.48f)
                close()
            }
            drawPath(
                path = mPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFB3F0FF), Color(0xFF33C3FF), Color(0xFF1188FF))
                )
            )

            // Sparkly Star in the center-gap of "M"
            val starPath = Path().apply {
                val cx = width * 0.50f
                val cy = height * 0.43f
                val rh = width * 0.035f
                val rv = height * 0.055f
                moveTo(cx, cy - rv)
                quadraticTo(cx, cy, cx + rh, cy)
                quadraticTo(cx, cy, cx, cy + rv)
                quadraticTo(cx, cy, cx - rh, cy)
                quadraticTo(cx, cy, cx, cy - rv)
                close()
            }
            drawPath(path = starPath, color = Color.White)

            // Draw Royal Crown Base
            val baseLeft = width * 0.39f
            val baseTop = height * 0.33f
            val baseWidth = width * 0.22f
            val baseHeight = height * 0.03f
            val crownBase = Path().apply {
                moveTo(baseLeft, baseTop)
                lineTo(baseLeft + baseWidth, baseTop)
                lineTo(baseLeft + baseWidth - width * 0.02f, baseTop + baseHeight)
                lineTo(baseLeft + width * 0.02f, baseTop + baseHeight)
                close()
            }
            drawPath(
                path = crownBase,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF80E5FF), Color(0xFF3DF5FF))
                )
            )

            // Crown Spires
            val crownSpires = Path().apply {
                moveTo(width * 0.40f, height * 0.33f)
                lineTo(width * 0.42f, height * 0.24f)
                lineTo(width * 0.47f, height * 0.29f)
                lineTo(width * 0.50f, height * 0.17f)
                lineTo(width * 0.53f, height * 0.29f)
                lineTo(width * 0.58f, height * 0.24f)
                lineTo(width * 0.60f, height * 0.33f)
                close()
            }
            drawPath(
                path = crownSpires,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFB3F0FF), Color(0xFF33C3FF), Color(0xFF0073E6))
                )
            )

            // Pearls on tips
            drawCircle(color = Color.White, center = Offset(width * 0.42f, height * 0.24f), radius = width * 0.015f)
            drawCircle(color = Color.White, center = Offset(width * 0.50f, height * 0.16f), radius = width * 0.022f)
            drawCircle(color = Color.White, center = Offset(width * 0.58f, height * 0.24f), radius = width * 0.015f)
        }
    }
}
