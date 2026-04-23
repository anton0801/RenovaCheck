package com.apprenova.renovacheck.ui.screens.issues

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.apprenova.renovacheck.data.model.*
import com.apprenova.renovacheck.ui.components.*
import com.apprenova.renovacheck.ui.theme.*
import com.apprenova.renovacheck.viewmodel.RenovaViewModel

// ── Issues List ───────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssuesScreen(
    projectId: String,
    viewModel: RenovaViewModel,
    onBack: () -> Unit,
    onAddIssue: () -> Unit,
    onIssueClick: (String) -> Unit
) {
    val allIssues by viewModel.issues.collectAsState()
    val issues = remember(allIssues) { allIssues.filter { it.projectId == projectId } }
    var filterSeverity by remember { mutableStateOf<IssueSeverity?>(null) }
    var showResolved by remember { mutableStateOf(false) }

    val filtered = issues.filter { issue ->
        (filterSeverity == null || issue.severity == filterSeverity) &&
        (showResolved || !issue.isResolved)
    }

    Scaffold(
        containerColor = RenovaBackground,
        topBar = { RenovaTopBar("Issues", "${issues.count { !it.isResolved }} open", onBack = onBack) },
        floatingActionButton = { RenovaFab(onAddIssue, text = "Add Issue") }
    ) { pv ->
        Column(modifier = Modifier.fillMaxSize().padding(pv)) {
            // Filters
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = filterSeverity == null && !showResolved,
                        onClick = { filterSeverity = null; showResolved = false },
                        label = { Text("All Open") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RenovaPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
                items(IssueSeverity.entries) { s ->
                    FilterChip(
                        selected = filterSeverity == s,
                        onClick = { filterSeverity = if (filterSeverity == s) null else s },
                        label = { Text(s.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(s.color),
                            selectedLabelColor = Color.White
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = showResolved,
                        onClick = { showResolved = !showResolved; filterSeverity = null },
                        label = { Text("Resolved") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RenovaSuccess,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState("✅", "No Issues Found", "Great! No open issues in this category.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered, key = { it.id }) { issue ->
                        IssueCard(
                            issue = issue,
                            onClick = { onIssueClick(issue.id) },
                            onResolve = { viewModel.resolveIssue(issue.id) }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

// ── Add Issue ─────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIssueScreen(
    projectId: String,
    viewModel: RenovaViewModel,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var severity by remember { mutableStateOf(IssueSeverity.MEDIUM) }
    var error by remember { mutableStateOf("") }

    val allRooms by viewModel.rooms.collectAsState()
    val rooms = remember(allRooms) { allRooms.filter { it.projectId == projectId } }
    var selectedRoomId by remember { mutableStateOf("") }
    var roomMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = RenovaBackground,
        topBar = { RenovaTopBar("Report Issue", onBack = onBack) }
    ) { pv ->
        Column(
            modifier = Modifier.fillMaxSize().padding(pv)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RenovaTextField(
                value = title, onValueChange = { title = it; error = "" },
                label = "Problem / Title *",
                leadingIcon = { Icon(Icons.Default.Warning, null, tint = RenovaRed) },
                placeholder = "e.g. Crack in wall near window"
            )

            // Room selector
            if (rooms.isNotEmpty()) {
                ExposedDropdownMenuBox(expanded = roomMenuExpanded, onExpandedChange = { roomMenuExpanded = it }) {
                    OutlinedTextField(
                        value = rooms.firstOrNull { it.id == selectedRoomId }?.name ?: "Select Room",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Room") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(roomMenuExpanded) },
                        leadingIcon = { Icon(Icons.Default.MeetingRoom, null, tint = RenovaPrimary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RenovaPrimary, focusedLabelColor = RenovaPrimary
                        )
                    )
                    ExposedDropdownMenu(expanded = roomMenuExpanded, onDismissRequest = { roomMenuExpanded = false }) {
                        rooms.forEach { room ->
                            DropdownMenuItem(
                                text = { Text(room.name) },
                                onClick = { selectedRoomId = room.id; roomMenuExpanded = false }
                            )
                        }
                    }
                }
            }

            RenovaTextField(
                value = location, onValueChange = { location = it },
                label = "Exact Location",
                leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = RenovaPrimary) },
                placeholder = "e.g. North wall, 120cm from floor"
            )

            RenovaTextField(
                value = description, onValueChange = { description = it },
                label = "Description",
                leadingIcon = { Icon(Icons.Default.Notes, null, tint = RenovaPrimary) },
                singleLine = false, maxLines = 4,
                placeholder = "Describe the issue in detail"
            )

            // Severity
            Text("SEVERITY", style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold, letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IssueSeverity.entries.forEach { s ->
                    val selected = severity == s
                    val color = Color(s.color)
                    Surface(
                        modifier = Modifier.weight(1f).clickable { severity = s },
                        shape = RoundedCornerShape(10.dp),
                        color = if (selected) color else color.copy(alpha = 0.1f),
                        border = if (selected) BorderStroke(2.dp, color) else null
                    ) {
                        Text(
                            s.label,
                            modifier = Modifier.padding(vertical = 10.dp).fillMaxWidth(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selected) Color.White else color,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            if (error.isNotEmpty()) Text(error, color = RenovaRed, style = MaterialTheme.typography.labelSmall)

            Spacer(Modifier.height(8.dp))
            RenovaPrimaryButton("Report Issue", onClick = {
                if (title.isBlank()) { error = "Title required"; return@RenovaPrimaryButton }
                viewModel.createIssue(
                    title = title.trim(),
                    description = description.trim(),
                    location = location.trim(),
                    severity = severity,
                    projectId = projectId,
                    roomId = selectedRoomId
                )
                onBack()
            })
        }
    }
}

// ── Issue Detail ──────────────────────────────────────────────────────────────
@Composable
fun IssueDetailScreen(
    issueId: String,
    viewModel: RenovaViewModel,
    onBack: () -> Unit
) {
    val allIssues by viewModel.issues.collectAsState()
    val issue = allIssues.firstOrNull { it.id == issueId } ?: return
    val allRooms by viewModel.rooms.collectAsState()
    val room = allRooms.firstOrNull { it.id == issue.roomId }
    val severityColor = Color(issue.severity.color)

    Scaffold(
        containerColor = RenovaBackground,
        topBar = { RenovaTopBar("Issue Details", onBack = onBack) }
    ) { pv ->
        Column(
            modifier = Modifier.fillMaxSize().padding(pv)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header card
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row {
                        SeverityBadge(issue.severity)
                        Spacer(Modifier.width(8.dp))
                        if (issue.isResolved) ResolvedBadge()
                    }
                    Text(issue.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    if (issue.location.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(issue.location, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    room?.let {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MeetingRoom, null, tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(it.name, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            if (issue.description.isNotEmpty()) {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Description", style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Text(issue.description, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Details
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Details", style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    DetailRow("Status", issue.status)
                    DetailRow("Severity", issue.severity.label)
                    DetailRow("Reported", java.text.SimpleDateFormat("MMM d, yyyy HH:mm", java.util.Locale.getDefault())
                        .format(java.util.Date(issue.createdAt)))
                    if (issue.isResolved && issue.resolvedAt != null) {
                        DetailRow("Resolved", java.text.SimpleDateFormat("MMM d, yyyy HH:mm", java.util.Locale.getDefault())
                            .format(java.util.Date(issue.resolvedAt)))
                    }
                }
            }

            if (!issue.isResolved) {
                Button(
                    onClick = { viewModel.resolveIssue(issueId) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RenovaSuccess)
                ) {
                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Mark as Resolved", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}
