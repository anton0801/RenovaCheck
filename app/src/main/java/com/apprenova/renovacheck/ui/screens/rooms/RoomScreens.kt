package com.apprenova.renovacheck.ui.screens.rooms

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

@Composable
fun RoomsScreen(
    projectId: String,
    viewModel: RenovaViewModel,
    onBack: () -> Unit,
    onAddRoom: () -> Unit,
    onRoomClick: (String) -> Unit
) {
    val allRooms by viewModel.rooms.collectAsState()
    val rooms = remember(allRooms) { allRooms.filter { it.projectId == projectId } }
    val projects by viewModel.projects.collectAsState()
    val project = projects.firstOrNull { it.id == projectId }

    Scaffold(
        containerColor = RenovaBackground,
        topBar = { RenovaTopBar("Rooms", project?.name ?: "", onBack = onBack) },
        floatingActionButton = { RenovaFab(onAddRoom, text = "Add Room") }
    ) { pv ->
        if (rooms.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(pv), contentAlignment = Alignment.Center) {
                EmptyState("🚪", "No Rooms Yet", "Add rooms to start your inspection checklist.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(pv),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Summary
                item {
                    RoomsSummaryCard(rooms)
                    Spacer(Modifier.height(4.dp))
                }
                items(rooms, key = { it.id }) { room ->
                    RoomCard(room, onClick = {
                        viewModel.selectRoom(room.id)
                        onRoomClick(room.id)
                    })
                }
                item { Spacer(Modifier.height(72.dp)) }
            }
        }
    }
}

@Composable
private fun RoomsSummaryCard(rooms: List<Room>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = RenovaPrimary.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MiniStat("${rooms.size}", "Total", RenovaPrimary)
            VerticalDivider()
            MiniStat("${rooms.count { it.status == InspectionStatus.PASSED }}", "Passed", RenovaSuccess)
            VerticalDivider()
            MiniStat("${rooms.count { it.status == InspectionStatus.FAILED }}", "Failed", RenovaRed)
            VerticalDivider()
            MiniStat("${rooms.count { it.status == InspectionStatus.PENDING }}", "Pending", RenovaTextSecondary)
        }
    }
}

@Composable
private fun MiniStat(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun VerticalDivider() {
    Box(modifier = Modifier.width(1.dp).height(32.dp).background(RenovaDivider))
}

// ── Add Room ──────────────────────────────────────────────────────────────────
@Composable
fun AddRoomScreen(
    projectId: String,
    viewModel: RenovaViewModel,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    val presets = listOf("Living Room", "Bedroom", "Kitchen", "Bathroom", "Hallway", "Dining Room", "Office", "Balcony")

    Scaffold(
        containerColor = RenovaBackground,
        topBar = { RenovaTopBar("Add Room", onBack = onBack) }
    ) { pv ->
        Column(
            modifier = Modifier.fillMaxSize().padding(pv)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RenovaTextField(
                value = name, onValueChange = { name = it; error = "" },
                label = "Room Name *",
                leadingIcon = { Icon(Icons.Default.MeetingRoom, null, tint = RenovaPrimary) },
                placeholder = "e.g. Living Room"
            )

            // Quick pick
//            Text("Quick Select", style = MaterialTheme.typography.labelMedium,
//                color = MaterialTheme.colorScheme.onSurfaceVariant)
//            androidx.compose.foundation.layout.FlowRow(
//                horizontalArrangement = Arrangement.spacedBy(8.dp),
//                verticalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                presets.forEach { preset ->
//                    FilterChip(
//                        selected = name == preset,
//                        onClick = { name = preset },
//                        label = { Text(preset, style = MaterialTheme.typography.labelMedium) },
//                        colors = FilterChipDefaults.filterChipColors(
//                            selectedContainerColor = RenovaPrimary,
//                            selectedLabelColor = Color.White
//                        )
//                    )
//                }
//            }

            RenovaTextField(
                value = area, onValueChange = { area = it },
                label = "Area (m²)",
                leadingIcon = { Icon(Icons.Default.SquareFoot, null, tint = RenovaPrimary) },
                placeholder = "Optional"
            )

            if (error.isNotEmpty()) {
                Text(error, color = RenovaRed, style = MaterialTheme.typography.labelSmall)
            }

            Spacer(Modifier.height(8.dp))
            RenovaPrimaryButton("Add Room", onClick = {
                if (name.isBlank()) { error = "Room name is required"; return@RenovaPrimaryButton }
                viewModel.createRoom(name.trim(), projectId, area.toFloatOrNull() ?: 0f)
                onBack()
            })
        }
    }
}

// ── Room Detail ───────────────────────────────────────────────────────────────
@Composable
fun RoomDetailScreen(
    roomId: String,
    viewModel: RenovaViewModel,
    onBack: () -> Unit,
    onStartInspection: (String, String) -> Unit,
    onIssues: () -> Unit,
    onScore: () -> Unit
) {
    val allRooms by viewModel.rooms.collectAsState()
    val room = allRooms.firstOrNull { it.id == roomId } ?: return
    val defaultChecklists = remember { viewModel.getDefaultChecklists() }
    var showChecklistDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = RenovaBackground,
        topBar = { RenovaTopBar(room.name, room.status.label, onBack = onBack) }
    ) { pv ->
        Column(
            modifier = Modifier.fillMaxSize().padding(pv)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Score card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Quality Score", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        StatusChip(room.status)
                        if (room.area > 0f) {
                            Spacer(Modifier.height(4.dp))
                            Text("${room.area} m²", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    ScoreRing(score = room.score)
                }
            }

            // Start Inspection
            Text("START INSPECTION", style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold, letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            defaultChecklists.forEach { checklist ->
                ChecklistPickCard(checklist) {
                    onStartInspection(roomId, checklist.id)
                }
            }

            // Actions
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onIssues,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, RenovaPrimary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RenovaPrimary)
                ) {
                    Icon(Icons.Default.Warning, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Issues")
                }
                OutlinedButton(
                    onClick = onScore,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, RenovaAccent),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RenovaSecondary)
                ) {
                    Icon(Icons.Default.Star, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Score")
                }
            }
        }
    }
}

@Composable
private fun ChecklistPickCard(checklist: Checklist, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(checklist.category.icon, fontSize = 28.sp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(checklist.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text("${checklist.items.size} check points",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = RenovaAccent)
        }
    }
}
