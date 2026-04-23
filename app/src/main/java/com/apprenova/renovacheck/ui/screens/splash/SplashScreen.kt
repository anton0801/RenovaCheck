package com.apprenova.renovacheck.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.apprenova.renovacheck.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.*

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    // Animations
    val logoScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "logo"
    )
    val logoAlpha  = remember { Animatable(0f) }
    val textAlpha  = remember { Animatable(0f) }
    val textOffset = remember { Animatable(30f) }
    val ringScale  = remember { Animatable(0f) }
    val particleProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        logoAlpha.animateTo(1f, tween(600))
        ringScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow))
        delay(200)
        textAlpha.animateTo(1f, tween(400))
        textOffset.animateTo(0f, tween(400, easing = FastOutSlowInEasing))
        particleProgress.animateTo(1f, tween(800))
        delay(600)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF245E47), Color(0xFF1B4D3E), Color(0xFF0D2E20)),
                    center = Offset(0.5f, 0.4f),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Particle canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawParticles(particleProgress.value)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Logo container with ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(130.dp)
                    .scale(ringScale.value)
                    .alpha(logoAlpha.value)
            ) {
                // Outer ring
                Canvas(Modifier.size(130.dp)) {
                    drawCircle(color = Color.White.copy(alpha = 0.1f), radius = size.minDimension / 2f)
                    drawCircle(color = Color.White.copy(alpha = 0.06f), radius = size.minDimension / 2.2f)
                }
                // Icon box
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                        .border(1.5.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // House + checkmark logo drawn with Canvas
                    Canvas(Modifier.size(48.dp)) {
                        val w = size.width
                        val h = size.height
                        // Roof
                        val roofPath = Path().apply {
                            moveTo(w * 0.5f, h * 0.05f)
                            lineTo(w * 0.92f, h * 0.42f)
                            lineTo(w * 0.78f, h * 0.42f)
                            lineTo(w * 0.78f, h * 0.95f)
                            lineTo(w * 0.22f, h * 0.95f)
                            lineTo(w * 0.22f, h * 0.42f)
                            lineTo(w * 0.08f, h * 0.42f)
                            close()
                        }
                        drawPath(roofPath, Color.White.copy(alpha = 0.9f))
                        // Checkmark on door
                        val checkPath = Path().apply {
                            moveTo(w * 0.34f, h * 0.66f)
                            lineTo(w * 0.46f, h * 0.78f)
                            lineTo(w * 0.66f, h * 0.55f)
                        }
                        drawPath(
                            checkPath, color = Color(0xFF5CB88A),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = w * 0.085f,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // App name
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .alpha(textAlpha.value)
                    .offset(y = textOffset.value.dp)
            ) {
                Text(
                    "RENOVA",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 8.sp
                )
                Text(
                    "CHECK",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Light,
                    color = RenovaAccent,
                    letterSpacing = 10.sp
                )
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(2.dp)
                        .background(
                            Brush.horizontalGradient(listOf(Color.Transparent, RenovaAccent, Color.Transparent))
                        )
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "Check your repair quality",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                    letterSpacing = 1.sp
                )
            }
        }

        // Bottom loading indicator
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = RenovaAccent.copy(alpha = textAlpha.value),
                strokeWidth = 2.dp
            )
        }
    }
}

private fun DrawScope.drawParticles(progress: Float) {
    val particles = listOf(
        Offset(0.15f, 0.2f), Offset(0.85f, 0.15f), Offset(0.9f, 0.7f),
        Offset(0.1f, 0.75f), Offset(0.5f, 0.08f), Offset(0.3f, 0.88f),
        Offset(0.72f, 0.85f), Offset(0.05f, 0.5f), Offset(0.95f, 0.45f)
    )
    particles.forEachIndexed { i, p ->
        val alpha = (progress * 2f - i * 0.15f).coerceIn(0f, 1f) * 0.4f
        val r = (3f + i % 3 * 2f)
        drawCircle(
            color = Color.White.copy(alpha = alpha),
            radius = r,
            center = Offset(p.x * size.width, p.y * size.height)
        )
    }
}
