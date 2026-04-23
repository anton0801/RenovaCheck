package com.apprenova.renovacheck.ui.screens.checklists

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.apprenova.renovacheck.data.model.*
import com.apprenova.renovacheck.ui.components.*
import com.apprenova.renovacheck.ui.theme.*
import com.apprenova.renovacheck.viewmodel.RenovaViewModel

// ── Checklists Screen ─────────────────────────────────────────────────────────
@Composable
fun ChecklistsScreen(
    viewModel: RenovaViewModel,
    onBack: () -> Unit,
    onChecklistClick: (String) -> Unit,
    onCreateCustom: () -> Unit
) {
    val defaultChecklists = remember { viewModel.getDefaultChecklists() }
    val customChecklists by viewModel.checklists.collectAsState()

    Scaffold(
        containerColor = RenovaBackground,
        topBar = { RenovaTopBar("Checklists", onBack = onBack) },
        floatingActionButton = { RenovaFab(onCreateCustom, text = "Custom") }
    ) { pv ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(pv),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { SectionHeader("DEFAULT CHECKLISTS") }
            items(defaultChecklists) { cl ->
                ChecklistCard(cl, onClick = { onChecklistClick(cl.id) })
            }
            if (customChecklists.isNotEmpty()) {
                item { SectionHeader("CUSTOM CHECKLISTS") }
                items(customChecklists, key = { it.id }) { cl ->
                    ChecklistCard(cl, onClick = { onChecklistClick(cl.id) })
                }
            }
            item { Spacer(Modifier.height(72.dp)) }
        }
    }
}

@Composable
private fun ChecklistCard(checklist: Checklist, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp)
                    .background(RenovaPrimary.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(checklist.category.icon, fontSize = 24.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(checklist.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text("${checklist.items.size} items",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (checklist.isDefault) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = RenovaAccent.copy(alpha = 0.15f)
                ) {
                    Text("Default", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = RenovaSecondary, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ── Custom Checklist Creator ───────────────────────────────────────────────────
@Composable
fun CustomChecklistScreen(viewModel: RenovaViewModel, onBack: () -> Unit) {
    var checklistName by remember { mutableStateOf("") }
    var items by remember { mutableStateOf(listOf("")) }
    var error by remember { mutableStateOf("") }

    Scaffold(
        containerColor = RenovaBackground,
        topBar = { RenovaTopBar("Create Checklist", onBack = onBack) }
    ) { pv ->
        Column(
            modifier = Modifier.fillMaxSize().padding(pv)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RenovaTextField(
                value = checklistName, onValueChange = { checklistName = it },
                label = "Checklist Name *",
                leadingIcon = { Icon(Icons.Default.List, null, tint = RenovaPrimary) }
            )

            Text("CHECKLIST ITEMS", style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold, letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            items.forEachIndexed { index, item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${index + 1}.", style = MaterialTheme.typography.labelLarge,
                        color = RenovaPrimary, modifier = Modifier.width(28.dp),
                        fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = item,
                        onValueChange = { items = items.toMutableList().also { list -> list[index] = it } },
                        label = { Text("Item ${index + 1}") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RenovaPrimary, focusedLabelColor = RenovaPrimary
                        )
                    )
                    if (items.size > 1) {
                        IconButton(onClick = {
                            items = items.toMutableList().also { list -> list.removeAt(index) }
                        }) {
                            Icon(Icons.Default.RemoveCircleOutline, null, tint = RenovaRed)
                        }
                    }
                }
            }

            TextButton(onClick = { items = items + "" }) {
                Icon(Icons.Default.AddCircleOutline, null, tint = RenovaAccent)
                Spacer(Modifier.width(4.dp))
                Text("Add Item", color = RenovaAccent)
            }

            if (error.isNotEmpty()) Text(error, color = RenovaRed, style = MaterialTheme.typography.labelSmall)

            RenovaPrimaryButton("Save Checklist", onClick = {
                if (checklistName.isBlank()) { error = "Name required"; return@RenovaPrimaryButton }
                val filtered = items.filter { it.isNotBlank() }
                if (filtered.isEmpty()) { error = "Add at least one item"; return@RenovaPrimaryButton }
                viewModel.createCustomChecklist(checklistName.trim(), filtered)
                onBack()
            })
        }
    }
}

// ── Inspection Screen ─────────────────────────────────────────────────────────
@Composable
fun InspectionScreen(
    roomId: String,
    checklistId: String,
    viewModel: RenovaViewModel,
    onBack: () -> Unit,
    onComplete: (String) -> Unit,
    onAddIssue: (String) -> Unit
) {
    val allRooms by viewModel.rooms.collectAsState()
    val room = allRooms.firstOrNull { it.id == roomId } ?: return

    // Load checklist — default if not in saved list
    val savedChecklists by viewModel.checklists.collectAsState()
    val defaultChecklists = remember { viewModel.getDefaultChecklists() }
    val baseChecklist = savedChecklists.firstOrNull { it.id == checklistId }
        ?: defaultChecklists.firstOrNull { it.id == checklistId }
        ?: return

    var checklist by remember { mutableStateOf(baseChecklist.copy(
        id = baseChecklist.id,
        items = baseChecklist.items.map { it.copy(id = java.util.UUID.randomUUID().toString()) }
    )) }

    val projects by viewModel.projects.collectAsState()
    val project = projects.firstOrNull { it.id == room.projectId }

    // Start inspection record
    val inspection = remember {
        viewModel.startInspection(room.projectId, roomId, checklistId)
    }

    val passedCount = checklist.items.count { it.isChecked && !it.isFailed }
    val failedCount = checklist.items.count { it.isFailed }
    val totalCount = checklist.items.size
    val progress = if (totalCount > 0) (passedCount + failedCount).toFloat() / totalCount else 0f

    Scaffold(
        containerColor = RenovaBackground,
        topBar = {
            RenovaTopBar("Inspection", "${checklist.name} · ${room.name}", onBack = onBack)
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("$passedCount passed · $failedCount failed · ${totalCount - passedCount - failedCount} remaining",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold, color = RenovaPrimary)
                    }
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                        color = RenovaAccent, trackColor = RenovaDivider
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { onAddIssue(room.projectId) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, RenovaRed),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RenovaRed)
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Issue")
                        }
                        Button(
                            onClick = {
                                viewModel.completeInspection(inspection, passedCount, failedCount)
                                onComplete(inspection.id)
                            },
                            modifier = Modifier.weight(2f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RenovaPrimary)
                        ) {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Complete Inspection", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    ) { pv ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(pv),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(checklist.items, key = { it.id }) { item ->
                CheckItemCard(
                    item = item,
                    onChecked = { checked ->
                        checklist = checklist.copy(items = checklist.items.map {
                            if (it.id == item.id) it.copy(isChecked = checked, isFailed = if (checked) false else it.isFailed)
                            else it
                        })
                    },
                    onFailed = { failed ->
                        checklist = checklist.copy(items = checklist.items.map {
                            if (it.id == item.id) it.copy(isFailed = failed, isChecked = if (failed) false else it.isChecked)
                            else it
                        })
                    }
                )
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun CheckItemCard(
    item: ChecklistItem,
    onChecked: (Boolean) -> Unit,
    onFailed: (Boolean) -> Unit
) {
    val bgColor = when {
        item.isFailed  -> RenovaRed.copy(alpha = 0.06f)
        item.isChecked -> RenovaSuccess.copy(alpha = 0.06f)
        else           -> MaterialTheme.colorScheme.surface
    }
    val borderColor = when {
        item.isFailed  -> RenovaRed.copy(alpha = 0.3f)
        item.isChecked -> RenovaSuccess.copy(alpha = 0.3f)
        else           -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (item.isChecked || item.isFailed) FontWeight.Medium else FontWeight.Normal,
                    color = when {
                        item.isFailed  -> RenovaRed
                        item.isChecked -> RenovaSuccess
                        else           -> MaterialTheme.colorScheme.onSurface
                    }
                )
                if (item.description.isNotEmpty()) {
                    Text(item.description, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.width(8.dp))
            // Pass button
            IconButton(
                onClick = { onChecked(!item.isChecked) },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    if (item.isChecked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Pass",
                    tint = if (item.isChecked) RenovaSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
            }
            // Fail button
            IconButton(
                onClick = { onFailed(!item.isFailed) },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    if (item.isFailed) Icons.Default.Cancel else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Fail",
                    tint = if (item.isFailed) RenovaRed else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

// ── Inspection Result ─────────────────────────────────────────────────────────
@Composable
fun InspectionResultScreen(
    inspectionId: String,
    viewModel: RenovaViewModel,
    onBack: () -> Unit,
    onAddIssue: (String) -> Unit
) {
    val inspections by viewModel.inspections.collectAsState()
    val inspection = inspections.firstOrNull { it.id == inspectionId } ?: return
    val allRooms by viewModel.rooms.collectAsState()
    val room = allRooms.firstOrNull { it.id == inspection.roomId }

    val isPassed = inspection.status == InspectionStatus.PASSED
    val resultColor = if (isPassed) RenovaSuccess else RenovaRed
    val resultEmoji = if (isPassed) "✅" else "❌"
    val resultText = if (isPassed) "Work Accepted" else "Rework Required"

    Scaffold(
        containerColor = RenovaBackground,
        topBar = { RenovaTopBar("Inspection Result", onBack = onBack) }
    ) { pv ->
        Column(
            modifier = Modifier.fillMaxSize().padding(pv)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            // Result badge
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(resultColor.copy(alpha = 0.1f), RoundedCornerShape(30.dp))
                    .border(2.dp, resultColor.copy(alpha = 0.3f), RoundedCornerShape(30.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(resultEmoji, fontSize = 56.sp)
            }

            Spacer(Modifier.height(20.dp))
            Text(resultText, style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold, color = resultColor)
            room?.let {
                Text(it.name, style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(32.dp))
            ScoreRing(score = inspection.score, size = 150.dp, strokeWidth = 14.dp)

            Spacer(Modifier.height(32.dp))

            // Stats
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ResultRow("Total Items", "${inspection.totalItems}", MaterialTheme.colorScheme.onSurface)
                    ResultRow("Passed", "${inspection.passedItems}", RenovaSuccess)
                    ResultRow("Failed", "${inspection.failedItems}", RenovaRed)
                    ResultRow("Score", "${inspection.score}%", resultColor)
                    ResultRow("Status", inspection.status.label, resultColor)
                }
            }

            Spacer(Modifier.height(24.dp))

            if (!isPassed) {
                Button(
                    onClick = { room?.let { onAddIssue(it.projectId) } },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RenovaRed)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Report Issues", fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(10.dp))
            }

            RenovaOutlineButton("Back to Room", onClick = onBack)
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}
