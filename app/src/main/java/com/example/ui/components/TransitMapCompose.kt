package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun TransitMapCompose(
    selectedLgaId: String,
    originMarket: String,
    destinationMarket: String,
    onMarketSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1.0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.7f, 3.0f)
        offset += offsetChange
    }

    val textMeasurer = rememberTextMeasurer()

    var isAnimationActive by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(200)
        isAnimationActive = true
    }

    // Smooth pulse animation for active Keke tricycles
    val infiniteTransition = rememberInfiniteTransition(label = "keke_pulse")
    val pulseRadiusAnim by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_radius"
    )
    val pulseRadius = if (isAnimationActive) pulseRadiusAnim else 8f

    // Pre-cache transit node names and layouts outside onDraw to prevent SurfaceSyncGroup frame drops
    val cachedLabels = remember(scale) {
        val fontSize = (10f * scale).coerceIn(9f, 13f).sp
        listOf(
            "Isi-Gate Central",
            "Ubani Market",
            "Osisioma Flyover",
            "Ariaria Market",
            "Bata Junction",
            "Ahia Ohuru"
        ).associateWith { name ->
            textMeasurer.measure(
                text = name,
                style = TextStyle(
                    color = HighContrastWhite,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(AsphaltBlack)
            .transformable(state = transformState)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val centerX = width / 2f + offset.x
            val centerY = height / 2f + offset.y

            // Draw Transit Grid Mesh
            val gridSize = 45f * scale
            val gridColor = Color(0xFF1E1E1E)
            var x = (centerX % gridSize)
            while (x < width) {
                drawLine(gridColor, Offset(x, 0f), Offset(x, height), strokeWidth = 1f)
                x += gridSize
            }
            var y = (centerY % gridSize)
            while (y < height) {
                drawLine(gridColor, Offset(0f, y), Offset(width, y), strokeWidth = 1f)
                y += gridSize
            }

            // Primary Highway Path (Enugu - Port Harcourt Expressway via Umuahia & Aba)
            val primaryHwyPath = Path().apply {
                moveTo(centerX - 120f * scale, centerY - 140f * scale)
                lineTo(centerX - 30f * scale, centerY - 40f * scale)   // Isi-Gate
                lineTo(centerX + 10f * scale, centerY + 30f * scale)   // Osisioma
                lineTo(centerX + 80f * scale, centerY + 120f * scale)  // Bata
            }

            drawPath(
                path = primaryHwyPath,
                color = SafetyAmberDark.copy(alpha = 0.3f),
                style = Stroke(width = 8f * scale)
            )
            drawPath(
                path = primaryHwyPath,
                color = SafetyAmber,
                style = Stroke(width = 3.5f * scale)
            )

            // Feeder corridor: Aba to Ikot Ekpene Road (Ogbor Hill / Obingwa)
            val feederPath = Path().apply {
                moveTo(centerX + 10f * scale, centerY + 30f * scale)
                lineTo(centerX + 105f * scale, centerY + 40f * scale)
            }
            drawPath(
                path = feederPath,
                color = Color(0xFF616161),
                style = Stroke(width = 2f * scale)
            )

            // Transit Hubs
            val hubs = listOf(
                TransitNode("Isi-Gate Central", centerX - 30f * scale, centerY - 40f * scale, true),
                TransitNode("Ubani Market", centerX - 10f * scale, centerY - 10f * scale, false),
                TransitNode("Osisioma Flyover", centerX + 10f * scale, centerY + 30f * scale, false),
                TransitNode("Ariaria Market", centerX + 50f * scale, centerY + 70f * scale, true),
                TransitNode("Bata Junction", centerX + 80f * scale, centerY + 110f * scale, true),
                TransitNode("Ahia Ohuru", centerX + 95f * scale, centerY + 125f * scale, false)
            )

            hubs.forEach { node ->
                val isSelected = node.name == originMarket || node.name == destinationMarket
                val nodeColor = if (isSelected) EmeraldGreen else if (node.isMajor) SafetyAmber else Color.White

                drawCircle(
                    color = nodeColor.copy(alpha = 0.25f),
                    radius = if (isSelected) 16f * scale else 9f * scale,
                    center = Offset(node.x, node.y)
                )

                drawCircle(
                    color = nodeColor,
                    radius = if (isSelected) 6f * scale else 3.5f * scale,
                    center = Offset(node.x, node.y)
                )

                // Use precomputed layout to avoid UI thread allocations
                cachedLabels[node.name]?.let { layoutResult ->
                    drawText(
                        textLayoutResult = layoutResult,
                        topLeft = Offset(node.x + 8f, node.y - 8f)
                    )
                }
            }

            // Real-Time Keke Fleet pulses
            val kekePositions = listOf(
                Offset(centerX + 35f * scale, centerY + 55f * scale),
                Offset(centerX + 65f * scale, centerY + 90f * scale),
                Offset(centerX - 15f * scale, centerY - 25f * scale)
            )

            kekePositions.forEach { pos ->
                drawCircle(
                    color = SafetyAmber.copy(alpha = 0.35f),
                    radius = pulseRadius * scale,
                    center = pos
                )
                drawCircle(
                    color = SafetyAmber,
                    radius = 3f * scale,
                    center = pos
                )
            }
        }

        // Map Overlay Badges
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = AsphaltDark.copy(alpha = 0.9f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SafetyAmberDark)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(EmeraldGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ABIA CORRIDOR: 480+ KEKES LIVE",
                        color = HighContrastWhite,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Quick GPS Reset Button
        IconButton(
            onClick = {
                scale = 1.0f
                offset = Offset.Zero
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .size(36.dp)
                .background(AsphaltCard, RoundedCornerShape(8.dp))
        ) {
            Icon(
                imageVector = Icons.Default.GpsFixed,
                contentDescription = "Center Map",
                tint = SafetyAmber
            )
        }
    }
}

private data class TransitNode(
    val name: String,
    val x: Float,
    val y: Float,
    val isMajor: Boolean
)
