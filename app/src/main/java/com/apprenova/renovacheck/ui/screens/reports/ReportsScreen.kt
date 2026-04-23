package com.apprenova.renovacheck.ui.screens.reports

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.apprenova.renovacheck.data.model.*
import com.apprenova.renovacheck.ui.components.*
import com.apprenova.renovacheck.ui.theme.*
import com.apprenova.renovacheck.viewmodel.RenovaViewModel

@Composable
fun ReportsScreen(
    projectId: String,
    viewModel: RenovaViewModel,
    onBack: () -> Unit
) {
    val projects by viewModel.projects.collectAsState()
    val project = projects.firstOrNull { it.id == projectId } ?: return
    val allRooms by viewModel.rooms.collectAsState()
    val rooms = remember(allRooms) { allRooms.filter { it.projectId == projectId } }
    val allIssues by viewModel.issues.collectAsState()
    val issues = remember(allIssues) { allIssues.filter { it.projectId == projectId } }
    val inspections by viewModel.inspections.collectAsState()
    val projectInspections = remember(inspections) { inspections.filter { it.projectId == projectId } }

    var showExportDialog by remember { mutableStateOf(false) }
    var reportText by remember { mutableStateOf("") }

    val avgScore = if (rooms.isNotEmpty()) rooms.sumOf { it.score.toLong() }.toInt() / rooms.size else 0
    val openIssues = issues.filter { !it.isResolved }
    val passedRooms = rooms.count { it.status == InspectionStatus.PASSED }

    Scaffold(
        containerColor = RenovaBackground,
        topBar = {
            RenovaTopBar("Report", project.name, onBack = onBack, actions = {
                IconButton(onClick = {
                    reportText = viewModel.generateReportSummary(projectId)
                    showExportDialog = true
                }) {
                    Icon(Icons.Default.Share, contentDescription = "Export",
                        tint = MaterialTheme.colorScheme.onSurface)
                }
            })
        }
    ) { pv ->
        Column(
            modifier = Modifier.fillMaxSize().padding(pv)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Overall score card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(
                            Brush.linearGradient(listOf(RenovaGradientStart, RenovaGradientEnd)),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Overall Quality", style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.8f))
                            Spacer(Modifier.height(4.dp))
                            Text(project.name, style = MaterialTheme.typography.titleLarge,
                                color = Color.White, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            val verdict = when {
                                avgScore >= 80 -> "✅ Accepted"
                                avgScore >= 60 -> "⚠️ Conditional"
                                else           -> "❌ Rejected"
                            }
                            Text(verdict, style = MaterialTheme.typography.labelLarge,
                                color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        ScoreRing(score = avgScore, size = 100.dp, strokeWidth = 8.dp)
                    }
                }
            }

            // Summary stats
            Text("SUMMARY", style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold, letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ReportStatCard("Rooms\nChecked", "$passedRooms / ${rooms.size}", RenovaSuccess, Modifier.weight(1f))
                ReportStatCard("Open\nIssues", "${openIssues.size}", if (openIssues.isEmpty()) RenovaSuccess else RenovaRed, Modifier.weight(1f))
                ReportStatCard("Inspections\nDone", "${projectInspections.size}", RenovaPrimary, Modifier.weight(1f))
            }

            // Issues by severity
            if (issues.isNotEmpty()) {
                Text("ISSUES BREAKDOWN", style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold, letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp))

                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        IssueSeverity.entries.forEach { s ->
                            val total = issues.count { it.severity == s }
                            val resolved = issues.count { it.severity == s && it.isResolved }
                            if (total > 0) {
                                SeverityBreakdownRow(s, total, resolved)
                            }
                        }
                    }
                }
            }

            // Rooms breakdown
            if (rooms.isNotEmpty()) {
                Text("ROOMS", style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold, letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp))

                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        rooms.forEach { room ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.MeetingRoom, null,
                                        tint = RenovaPrimary, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(room.name, style = MaterialTheme.typography.bodyMedium)
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("${room.score}%",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            room.score >= 80 -> RenovaSuccess
                                            room.score >= 60 -> SeverityMedium
                                            else -> RenovaRed
                                        })
                                    StatusChip(room.status)
                                }
                            }
                            if (rooms.last() != room) {
                                HorizontalDivider(color = RenovaDivider, thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Export button
            Button(
                onClick = {
                    reportText = viewModel.generateReportSummary(projectId)
                    showExportDialog = true
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RenovaPrimary)
            ) {
                Icon(Icons.Default.PictureAsPdf, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Export Report", fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(24.dp))
        }

        // Export dialog
        if (showExportDialog) {
            AlertDialog(
                onDismissRequest = { showExportDialog = false },
                title = { Text("Report Preview", fontWeight = FontWeight.Bold) },
                text = {
                    Box(
                        modifier = Modifier.height(320.dp)
                            .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(reportText, style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF5CB88A),
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    }
                },
                confirmButton = {
                    Button(onClick = { showExportDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = RenovaPrimary)) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

@Composable
private fun ReportStatCard(label: String, value: String, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(value, style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SeverityBreakdownRow(severity: IssueSeverity, total: Int, resolved: Int) {
    val color = Color(severity.color)
    val progress = if (total > 0) resolved.toFloat() / total else 0f
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
                Spacer(Modifier.width(8.dp))
                Text(severity.label, style = MaterialTheme.typography.bodyMedium)
            }
            Text("$resolved / $total resolved", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.15f)
        )
    }
}
