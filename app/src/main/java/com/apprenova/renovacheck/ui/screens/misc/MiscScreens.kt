package com.apprenova.renovacheck.ui.screens.misc

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
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
import java.text.SimpleDateFormat
import java.util.*

// ── Contractor Notes ──────────────────────────────────────────────────────────
@Composable
fun ContractorNotesScreen(
    projectId: String,
    viewModel: RenovaViewModel,
    onBack: () -> Unit
) {
    val notes = remember { viewModel.getNotesForProject(projectId) }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = RenovaBackground,
        topBar = { RenovaTopBar("Contractor Notes", onBack = onBack) },
        floatingActionButton = { RenovaFab(onClick = { showAddDialog = true }, text = "Add Note") }
    ) { pv ->
        if (notes.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(pv), contentAlignment = Alignment.Center) {
                EmptyState("📝", "No Notes Yet", "Add notes about your contractors' work.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(pv),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notes, key = { it.id }) { note ->
                    ContractorNoteCard(note)
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }

        if (showAddDialog) {
            AddNoteDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { name, note, rating ->
                    viewModel.addContractorNote(name, note, projectId, rating)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
private fun ContractorNoteCard(note: ContractorNote) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(40.dp)
                            .background(RenovaPrimary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            note.contractorName.firstOrNull()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = RenovaPrimary
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(note.contractorName, style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold)
                        Text(
                            SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                                .format(Date(note.createdAt)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (note.rating > 0) {
                    StarRating(note.rating)
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(note.note, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun StarRating(rating: Int) {
    Row {
        repeat(5) { i ->
            Icon(
                if (i < rating) Icons.Default.Star else Icons.Default.StarOutline,
                contentDescription = null,
                tint = RenovaGold,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun AddNoteDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, Int) -> Unit
) {
    var name   by remember { mutableStateOf("") }
    var note   by remember { mutableStateOf("") }
    var rating by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Contractor Note", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("Contractor Name *") }, singleLine = true,
                    shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = note, onValueChange = { note = it },
                    label = { Text("Note") }, maxLines = 4,
                    shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth())
                Text("Rating", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row {
                    repeat(5) { i ->
                        IconButton(onClick = { rating = i + 1 }, modifier = Modifier.size(36.dp)) {
                            Icon(
                                if (i < rating) Icons.Default.Star else Icons.Default.StarOutline,
                                null, tint = RenovaGold, modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank() && note.isNotBlank()) onAdd(name.trim(), note.trim(), rating) },
                colors = ButtonDefaults.buttonColors(containerColor = RenovaPrimary)
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// ── Room Score Screen ─────────────────────────────────────────────────────────
@Composable
fun RoomScoreScreen(
    roomId: String,
    viewModel: RenovaViewModel,
    onBack: () -> Unit
) {
    val allRooms by viewModel.rooms.collectAsState()
    val room = allRooms.firstOrNull { it.id == roomId } ?: return
    val allIssues by viewModel.issues.collectAsState()
    val roomIssues = remember(allIssues) { allIssues.filter { it.roomId == roomId } }

    val inspections by viewModel.inspections.collectAsState()
    val roomInspections = remember(inspections) { inspections.filter { it.roomId == roomId } }

    Scaffold(
        containerColor = RenovaBackground,
        topBar = { RenovaTopBar("Room Score", room.name, onBack = onBack) }
    ) { pv ->
        Column(
            modifier = Modifier.fillMaxSize().padding(pv)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            ScoreRing(score = room.score, size = 160.dp, strokeWidth = 14.dp)
            Spacer(Modifier.height(8.dp))

            StatusChip(room.status)

            // Score breakdown
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Score Breakdown", style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold)
                    ScoreRow("Inspections Completed", "${roomInspections.count { it.status == InspectionStatus.PASSED || it.status == InspectionStatus.FAILED }}", RenovaPrimary)
                    ScoreRow("Passed Inspections", "${roomInspections.count { it.status == InspectionStatus.PASSED }}", RenovaSuccess)
                    ScoreRow("Issues Found", "${roomIssues.size}", if (roomIssues.isEmpty()) RenovaSuccess else RenovaRed)
                    ScoreRow("Resolved Issues", "${roomIssues.count { it.isResolved }}", RenovaAccent)
                }
            }

            // Grade label
            val grade = when {
                room.score >= 90 -> "A" to RenovaSuccess
                room.score >= 80 -> "B" to RenovaAccent
                room.score >= 70 -> "C" to RenovaGold
                room.score >= 60 -> "D" to SeverityHigh
                else -> "F" to RenovaRed
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = grade.second.copy(alpha = 0.08f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Quality Grade", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            when (grade.first) {
                                "A" -> "Excellent — Accept immediately"
                                "B" -> "Good — Minor touch-ups only"
                                "C" -> "Average — Address non-critical issues"
                                "D" -> "Below average — Significant rework needed"
                                else -> "Fail — Reject and redo"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        grade.first,
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = grade.second
                    )
                }
            }
        }
    }
}

@Composable
private fun ScoreRow(label: String, value: String, color: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = color)
    }
}

// ── Notifications Screen ──────────────────────────────────────────────────────
@Composable
fun NotificationsScreen(
    viewModel: RenovaViewModel,
    onBack: () -> Unit
) {
    val allIssues by viewModel.issues.collectAsState()
    val criticalIssues = remember(allIssues) { allIssues.filter { it.severity == IssueSeverity.CRITICAL && !it.isResolved } }
    val allTasks by viewModel.tasks.collectAsState()
    val overdueTasks = remember(allTasks) { allTasks.filter {
        !it.isCompleted && it.dueDate != null && it.dueDate < System.currentTimeMillis()
    }}

    Scaffold(
        containerColor = RenovaBackground,
        topBar = { RenovaTopBar("Notifications", onBack = onBack) }
    ) { pv ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(pv),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (criticalIssues.isEmpty() && overdueTasks.isEmpty()) {
                item {
                    Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyState("🔔", "All Clear!", "No urgent notifications at this time.")
                    }
                }
            }

            if (criticalIssues.isNotEmpty()) {
                item {
                    NotifSectionHeader("🚨 CRITICAL ISSUES", RenovaRed)
                }
                items(criticalIssues, key = { it.id }) { issue ->
                    NotificationItem(
                        icon = Icons.Default.Warning,
                        iconColor = RenovaRed,
                        title = issue.title,
                        subtitle = "Location: ${issue.location.ifEmpty { "—" }}",
                        time = java.text.SimpleDateFormat("MMM d", Locale.getDefault())
                            .format(Date(issue.createdAt))
                    )
                }
            }

            if (overdueTasks.isNotEmpty()) {
                item { NotifSectionHeader("⏰ OVERDUE TASKS", RenovaGold) }
                items(overdueTasks, key = { it.id }) { task ->
                    NotificationItem(
                        icon = Icons.Default.Schedule,
                        iconColor = RenovaGold,
                        title = task.title,
                        subtitle = task.description.ifEmpty { "Task overdue" },
                        time = task.dueDate?.let {
                            java.text.SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(it))
                        } ?: ""
                    )
                }
            }
        }
    }
}

@Composable
private fun NotifSectionHeader(text: String, color: Color) {
    Text(text, style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold, color = color,
        modifier = Modifier.padding(vertical = 4.dp))
}

@Composable
private fun NotificationItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    time: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).background(iconColor.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(time, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
