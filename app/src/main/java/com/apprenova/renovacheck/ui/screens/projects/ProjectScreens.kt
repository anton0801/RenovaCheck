package com.apprenova.renovacheck.ui.screens.projects

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.apprenova.renovacheck.data.model.*
import com.apprenova.renovacheck.ui.components.*
import com.apprenova.renovacheck.ui.theme.*
import com.apprenova.renovacheck.viewmodel.RenovaViewModel

// ── Projects List ─────────────────────────────────────────────────────────────
@Composable
fun ProjectsScreen(
    viewModel: RenovaViewModel,
    onAddProject: () -> Unit,
    onProjectClick: (String) -> Unit
) {
    val projects by viewModel.projects.collectAsState()

    Scaffold(
        containerColor = RenovaBackground,
        topBar = { RenovaTopBar("Projects", "${projects.size} projects") },
        floatingActionButton = { RenovaFab(onAddProject, text = "Add Project") }
    ) { pv ->
        if (projects.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(pv), contentAlignment = Alignment.Center) {
                EmptyState("🏗️", "No Projects Yet", "Add your first renovation project to start inspecting.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(pv),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(projects, key = { it.id }) { project ->
                    AnimatedVisibility(visible = true, enter = fadeIn() + slideInVertically()) {
                        ProjectCard(
                            project = project,
                            onClick = {
                                viewModel.selectProject(project.id)
                                onProjectClick(project.id)
                            }
                        )
                    }
                }
                item { Spacer(Modifier.height(72.dp)) }
            }
        }
    }
}

// ── Add Project ───────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProjectScreen(
    viewModel: RenovaViewModel,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var contractor by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ProjectType.APARTMENT) }
    var typeMenuExpanded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    Scaffold(
        containerColor = RenovaBackground,
        topBar = { RenovaTopBar("New Project", onBack = onBack) }
    ) { pv ->
        Column(
            modifier = Modifier.fillMaxSize().padding(pv)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RenovaTextField(
                value = name, onValueChange = { name = it; error = "" },
                label = "Project Name *",
                leadingIcon = { Icon(Icons.Default.HomeWork, null, tint = RenovaPrimary) },
                placeholder = "e.g. Apartment Renovation 2024"
            )

            // Type selector
            ExposedDropdownMenuBox(
                expanded = typeMenuExpanded,
                onExpandedChange = { typeMenuExpanded = it }
            ) {
                OutlinedTextField(
                    value = "${selectedType.icon} ${selectedType.label}",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Project Type") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeMenuExpanded) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RenovaPrimary, focusedLabelColor = RenovaPrimary
                    )
                )
                ExposedDropdownMenu(expanded = typeMenuExpanded, onDismissRequest = { typeMenuExpanded = false }) {
                    ProjectType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text("${type.icon} ${type.label}") },
                            onClick = { selectedType = type; typeMenuExpanded = false }
                        )
                    }
                }
            }

            RenovaTextField(
                value = address, onValueChange = { address = it },
                label = "Address",
                leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = RenovaPrimary) },
                placeholder = "Optional"
            )

            RenovaTextField(
                value = contractor, onValueChange = { contractor = it },
                label = "Contractor Name",
                leadingIcon = { Icon(Icons.Default.Person, null, tint = RenovaPrimary) },
                placeholder = "Optional"
            )

            if (error.isNotEmpty()) {
                Text(error, color = RenovaRed, style = MaterialTheme.typography.labelSmall)
            }

            Spacer(Modifier.height(8.dp))

            RenovaPrimaryButton("Create Project", onClick = {
                if (name.isBlank()) { error = "Project name is required"; return@RenovaPrimaryButton }
                viewModel.createProject(name.trim(), selectedType, address.trim(), contractor.trim())
                onBack()
            })
        }
    }
}

// ── Project Detail ────────────────────────────────────────────────────────────
@Composable
fun ProjectDetailScreen(
    projectId: String,
    viewModel: RenovaViewModel,
    onBack: () -> Unit,
    onRooms: () -> Unit,
    onIssues: () -> Unit,
    onReports: () -> Unit,
    onTimeline: () -> Unit,
    onTasks: () -> Unit,
    onContractorNotes: () -> Unit
) {
    val projects by viewModel.projects.collectAsState()
    val project = projects.firstOrNull { it.id == projectId } ?: return

    Scaffold(
        containerColor = RenovaBackground,
        topBar = {
            RenovaTopBar(project.name, project.type.label, onBack = onBack,
                actions = {
                    IconButton(onClick = onReports) {
                        Icon(Icons.Default.Description, null, tint = MaterialTheme.colorScheme.onSurface)
                    }
                })
        }
    ) { pv ->
        Column(
            modifier = Modifier.fillMaxSize().padding(pv)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(androidx.compose.ui.graphics.Brush.linearGradient(
                        listOf(RenovaGradientStart, RenovaGradientEnd)))
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(project.type.icon, fontSize = 32.sp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(project.name, style = MaterialTheme.typography.headlineMedium,
                                color = Color.White, fontWeight = FontWeight.Bold)
                            if (project.address.isNotEmpty()) {
                                Text(project.address, style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.7f))
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        QuickStat("${project.totalRooms}", "Rooms", Color.White)
                        QuickStat("${project.issueCount}", "Issues", if (project.issueCount > 0) Color(0xFFFFD166) else Color.White)
                        QuickStat("${project.score}%", "Score", if (project.score >= 70) Color(0xFF5CB88A) else Color(0xFFFF6B6B))
                    }
                }
            }

            // Quick Actions
            Column(modifier = Modifier.padding(16.dp)) {
                Text("QUICK ACTIONS", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold, letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 12.dp))

                val actions = listOf(
                    Triple(Icons.Default.MeetingRoom, "Rooms", onRooms),
                    Triple(Icons.Default.Warning, "Issues", onIssues),
                    Triple(Icons.Default.Task, "Tasks", onTasks),
                    Triple(Icons.Default.Description, "Reports", onReports),
                    Triple(Icons.Default.Timeline, "Timeline", onTimeline),
                    Triple(Icons.Default.Note, "Contractor Notes", onContractorNotes)
                )

                actions.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { (icon, label, action) ->
                            ActionCard(icon, label, action, Modifier.weight(1f))
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }

            // Progress
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Inspection Progress", style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    val progress = if (project.totalRooms > 0) project.completedRooms.toFloat() / project.totalRooms else 0f
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = RenovaAccent, trackColor = RenovaDivider
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("${project.completedRooms} of ${project.totalRooms} rooms completed",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun QuickStat(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.7f))
    }
}

@Composable
private fun ActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick).height(72.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(icon, contentDescription = null, tint = RenovaPrimary, modifier = Modifier.size(22.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
