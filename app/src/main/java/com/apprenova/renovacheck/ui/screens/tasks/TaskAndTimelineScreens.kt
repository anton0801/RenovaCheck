package com.apprenova.renovacheck.ui.screens.tasks

import androidx.compose.animation.*
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.*
import com.apprenova.renovacheck.data.model.*
import com.apprenova.renovacheck.ui.components.*
import com.apprenova.renovacheck.ui.theme.*
import com.apprenova.renovacheck.viewmodel.RenovaViewModel
import java.text.SimpleDateFormat
import java.util.*

// ── Tasks Screen ──────────────────────────────────────────────────────────────
@Composable
fun TasksScreen(
    projectId: String,
    viewModel: RenovaViewModel,
    onBack: () -> Unit
) {
    val allTasks by viewModel.tasks.collectAsState()
    val tasks = remember(allTasks) { allTasks.filter { it.projectId == projectId } }
    var showAddDialog by remember { mutableStateOf(false) }
    var showCompleted by remember { mutableStateOf(false) }

    val filtered = if (showCompleted) tasks else tasks.filter { !it.isCompleted }

    Scaffold(
        containerColor = RenovaBackground,
        topBar = { RenovaTopBar("Tasks", "${tasks.count { !it.isCompleted }} pending", onBack = onBack) },
        floatingActionButton = {
            RenovaFab(onClick = { showAddDialog = true }, text = "Add Task")
        }
    ) { pv ->
        Column(modifier = Modifier.fillMaxSize().padding(pv)) {
            // Toggle
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Show completed", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = showCompleted,
                    onCheckedChange = { showCompleted = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = RenovaPrimary)
                )
            }

            if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState("✅", "All Done!", "No pending tasks for this project.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            onComplete = { viewModel.completeTask(task.id) }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }

        if (showAddDialog) {
            AddTaskDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { title, desc, priority ->
                    viewModel.createTask(title, desc, projectId, priority = priority)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
private fun TaskCard(task: Task, onComplete: () -> Unit) {
    val color = Color(task.priority.color)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted)
                MaterialTheme.colorScheme.surfaceVariant
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (task.isCompleted) 0.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { if (!task.isCompleted) onComplete() },
                colors = CheckboxDefaults.colors(
                    checkedColor = RenovaSuccess,
                    uncheckedColor = color
                )
            )
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface
                )
                if (task.description.isNotEmpty()) {
                    Text(task.description, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2)
                }
                if (task.assignee.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(2.dp))
                        Text(task.assignee, style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            SeverityBadge(task.priority)
        }
    }
}

@Composable
private fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, IssueSeverity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc  by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(IssueSeverity.MEDIUM) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Task", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text("Task title *") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc, onValueChange = { desc = it },
                    label = { Text("Description") },
                    maxLines = 3,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Priority", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IssueSeverity.entries.forEach { s ->
                        val c = Color(s.color)
                        FilterChip(
                            selected = priority == s,
                            onClick = { priority = s },
                            label = { Text(s.label, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = c, selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onAdd(title.trim(), desc.trim(), priority) },
                colors = ButtonDefaults.buttonColors(containerColor = RenovaPrimary)
            ) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ── Timeline Screen ───────────────────────────────────────────────────────────
@Composable
fun TimelineScreen(
    projectId: String,
    viewModel: RenovaViewModel,
    onBack: () -> Unit
) {
    val activity by viewModel.activity.collectAsState()
    val events = remember(activity) { activity.filter { it.projectId == projectId } }

    Scaffold(
        containerColor = RenovaBackground,
        topBar = { RenovaTopBar("Timeline", "${events.size} events", onBack = onBack) }
    ) { pv ->
        if (events.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(pv), contentAlignment = Alignment.Center) {
                EmptyState("📅", "No History Yet", "Project activity will appear here.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(pv),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            ) {
                itemsIndexed(events) { index, event ->
                    TimelineEventItem(event, isLast = index == events.lastIndex)
                }
            }
        }
    }
}

@Composable
private fun TimelineEventItem(event: ActivityEvent, isLast: Boolean) {
    val iconRes = when (event.type) {
        "project"    -> Icons.Default.HomeWork
        "issue"      -> Icons.Default.Warning
        "inspection" -> Icons.Default.Assignment
        "task"       -> Icons.Default.Task
        else         -> Icons.Default.Info
    }
    val iconColor = when (event.type) {
        "issue"      -> RenovaGold
        "inspection" -> RenovaPrimary
        "task"       -> RenovaSuccess
        else         -> RenovaAccent
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        // Timeline spine
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(40.dp)) {
            Box(
                modifier = Modifier.size(32.dp).background(iconColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(iconRes, null, tint = iconColor, modifier = Modifier.size(16.dp))
            }
            if (!isLast) {
                Box(modifier = Modifier.width(2.dp).height(40.dp).background(RenovaDivider))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
                .padding(bottom = if (isLast) 0.dp else 8.dp)
        ) {
            Text(event.description, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface)
            Text(
                SimpleDateFormat("MMM d, yyyy · HH:mm", Locale.getDefault())
                    .format(Date(event.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Activity History (global) ─────────────────────────────────────────────────
@Composable
fun ActivityScreen(
    viewModel: RenovaViewModel,
    onBack: () -> Unit
) {
    val activity by viewModel.activity.collectAsState()

    Scaffold(
        containerColor = RenovaBackground,
        topBar = { RenovaTopBar("Activity History", "${activity.size} events", onBack = onBack) }
    ) { pv ->
        if (activity.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(pv), contentAlignment = Alignment.Center) {
                EmptyState("📋", "No Activity Yet", "Actions across all projects will appear here.")
            }
        } else {
            // Group by date
            val grouped = remember(activity) {
                activity.groupBy {
                    SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date(it.timestamp))
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(pv),
                contentPadding = PaddingValues(16.dp)
            ) {
                grouped.forEach { (date, events) ->
                    item {
                        Text(date, style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp))
                    }
                    itemsIndexed(events) { i, event ->
                        TimelineEventItem(event, isLast = i == events.lastIndex)
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}
