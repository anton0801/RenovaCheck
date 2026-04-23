package com.apprenova.renovacheck.ui.screens.welcome

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.apprenova.renovacheck.ui.components.*
import com.apprenova.renovacheck.ui.theme.*
import kotlinx.coroutines.launch

// ── Welcome Screen ────────────────────────────────────────────────────────────
@Composable
fun WelcomeScreen(
    onStart: () -> Unit,
    onLogin: () -> Unit
) {
    val enterAnim = remember { Animatable(0f) }
    val titleOffset = remember { Animatable(60f) }
    val btnAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        enterAnim.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
        titleOffset.animateTo(0f, tween(600, easing = FastOutSlowInEasing))
        btnAlpha.animateTo(1f, tween(500, delayMillis = 300))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RenovaBackground)
    ) {
        // Decorative top arc
        Canvas(Modifier.fillMaxWidth().height(320.dp)) {
            drawArc(
                brush = Brush.linearGradient(
                    listOf(RenovaGradientStart, RenovaGradientEnd),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height)
                ),
                startAngle = 180f, sweepAngle = 180f, useCenter = true
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(80.dp))

            // Logo mark
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .scale(enterAnim.value)
                    .alpha(enterAnim.value)
                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(22.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.size(50.dp)) {
                    val w = size.width; val h = size.height
                    val path = Path().apply {
                        moveTo(w * 0.5f, h * 0.05f); lineTo(w * 0.92f, h * 0.42f)
                        lineTo(w * 0.78f, h * 0.42f); lineTo(w * 0.78f, h * 0.95f)
                        lineTo(w * 0.22f, h * 0.95f); lineTo(w * 0.22f, h * 0.42f)
                        lineTo(w * 0.08f, h * 0.42f); close()
                    }
                    drawPath(path, Color.White)
                    val check = Path().apply {
                        moveTo(w * 0.34f, h * 0.66f); lineTo(w * 0.46f, h * 0.78f)
                        lineTo(w * 0.66f, h * 0.55f)
                    }
                    drawPath(check, RenovaAccent, style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = w * 0.09f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "Renova Check",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.offset(y = (-titleOffset.value).dp).alpha(enterAnim.value)
            )
            Text(
                "Professional Repair Inspection",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.75f),
                modifier = Modifier.alpha(enterAnim.value)
            )

            Spacer(Modifier.height(100.dp))

            // Cards row
            Row(
                modifier = Modifier.fillMaxWidth().alpha(enterAnim.value),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FeatureCard("🔍", "Smart\nChecklists", Modifier.weight(1f))
                FeatureCard("📸", "Photo\nEvidence", Modifier.weight(1f))
                FeatureCard("📊", "Quality\nScores", Modifier.weight(1f))
            }

            Spacer(Modifier.weight(1f))

            Column(
                modifier = Modifier.alpha(btnAlpha.value),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RenovaPrimaryButton("Get Started", onClick = onStart)
                RenovaOutlineButton("Log In", onClick = onLogin)
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun FeatureCard(emoji: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 28.sp)
            Spacer(Modifier.height(6.dp))
            Text(label, style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

// ── Onboarding ────────────────────────────────────────────────────────────────
data class OnboardingPage(
    val emoji: String,
    val title: String,
    val subtitle: String,
    val gradient: List<Color>
)

private val pages = listOf(
    OnboardingPage(
        emoji = "🏗️",
        title = "Inspect Repair Work",
        subtitle = "Walk through every room with professional checklists covering walls, painting, tiles, electrical, and plumbing.",
        gradient = listOf(Color(0xFF1B4D3E), Color(0xFF2E7D5B))
    ),
    OnboardingPage(
        emoji = "🔍",
        title = "Find Mistakes Easily",
        subtitle = "Spot defects instantly. Log issues with photos, severity levels, and exact locations so nothing gets missed.",
        gradient = listOf(Color(0xFF1A3A5C), Color(0xFF2E5F8A))
    ),
    OnboardingPage(
        emoji = "📋",
        title = "Custom Checklists",
        subtitle = "Use our expert-crafted checklists or build your own. Every inspection standard, tailored to your project.",
        gradient = listOf(Color(0xFF4A2B6B), Color(0xFF7B4DB0))
    ),
    OnboardingPage(
        emoji = "✅",
        title = "Accept Work With Confidence",
        subtitle = "Generate professional reports, track quality scores, and sign off on each stage knowing everything is verified.",
        gradient = listOf(Color(0xFF1B4D3E), Color(0xFF34A853))
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val pagerState = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(state = pagerState) { page ->
            OnboardingPage(pages[page])
        }

        // Bottom controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 28.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Dots
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(pages.size) { i ->
                    val width by animateDpAsState(
                        targetValue = if (pagerState.currentPage == i) 24.dp else 8.dp,
                        animationSpec = tween(250),
                        label = "dot"
                    )
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(if (pagerState.currentPage == i) Color.White else Color.White.copy(alpha = 0.35f))
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            if (pagerState.currentPage == pages.size - 1) {
                Button(
                    onClick = onFinished,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = RenovaPrimary)
                ) {
                    Text("Start Inspecting", fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onFinished) {
                        Text("Skip", color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.labelLarge)
                    }
                    Button(
                        onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = RenovaPrimary)
                    ) {
                        Text("Next", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPage(page: OnboardingPage) {
    val enterAnim = remember { Animatable(0f) }
    val floatAnim = rememberInfiniteTransition(label = "float")
    val floatY by floatAnim.animateFloat(
        initialValue = 0f, targetValue = 12f,
        animationSpec = infiniteRepeatable(tween(2400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "floatY"
    )

    LaunchedEffect(page) {
        enterAnim.snapTo(0f)
        enterAnim.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(page.gradient))
    ) {
        // Background pattern dots
        Canvas(Modifier.fillMaxSize()) {
            repeat(20) { i ->
                val x = (i % 5) * size.width / 4f + size.width * 0.1f
                val y = (i / 5) * size.height / 5f + size.height * 0.05f
                drawCircle(Color.White.copy(alpha = 0.04f), radius = 80f, center = Offset(x, y))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Illustration
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .offset(y = floatY.dp)
                    .scale(enterAnim.value)
                    .alpha(enterAnim.value)
                    .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(40.dp))
                    .border(1.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(40.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(page.emoji, fontSize = 80.sp)
            }

            Spacer(Modifier.height(56.dp))

            Text(
                text = page.title,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(enterAnim.value)
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = page.subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                lineHeight = 26.sp,
                modifier = Modifier.alpha(enterAnim.value)
            )
        }
    }
}
