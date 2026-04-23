package com.apprenova.renovacheck.ui.screens.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.apprenova.renovacheck.data.model.*
import com.apprenova.renovacheck.ui.components.*
import com.apprenova.renovacheck.ui.theme.*
import com.apprenova.renovacheck.viewmodel.RenovaViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: RenovaViewModel,
    onNavigateToProjects: () -> Unit,
    onNavigateToIssues: (String) -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    val stats by viewModel.dashboardStats.collectAsState()
    val projects by viewModel.projects.collectAsState()
    val activity by viewModel.activity.collectAsState()
    val profile = viewModel.userProfile

    // Stagger animations
    val headerAnim = remember { Animatable(0f) }
    val statsAnim  = remember { Animatable(0f) }
    val listAnim   = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        headerAnim.animateTo(1f, tween(500))
        statsAnim.animateTo(1f, tween(400, delayMillis = 150))
        listAnim.animateTo(1f, tween(400, delayMillis = 300))
    }

    Scaffold(
        containerColor = RenovaBackground,
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                        val greeting = when {
                            hour < 12 -> "Good morning"
                            hour < 18 -> "Good afternoon"
                            else -> "Good evening"
                        }
                        Text(greeting, style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            if (profile.name.isNotEmpty()) profile.name else "Inspector",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = onNavigateToNotifications) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Stats row
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(statsAnim.value)
                        .padding(top = 16.dp)
                ) {
                    SectionHeader("OVERVIEW")
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            label = "Active\nProjects",
                            value = "${stats.activeProjects}",
                            icon = { Icon(Icons.Default.HomeWork, null, tint = RenovaPrimary, modifier = Modifier.size(20.dp)) },
                            color = RenovaPrimary,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToProjects
                        )
                        StatCard(
                            label = "Open\nIssues",
                            value = "${stats.totalIssues}",
                            icon = { Icon(Icons.Default.Warning, null, tint = RenovaGold, modifier = Modifier.size(20.dp)) },
                            color = RenovaGold,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "Rooms\nChecked",
                            value = "${stats.roomsChecked}",
                            icon = { Icon(Icons.Default.CheckCircle, null, tint = RenovaSuccess, modifier = Modifier.size(20.dp)) },
                            color = RenovaSuccess,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            label = "Active\nInspections",
                            value = "${stats.activeInspections}",
                            icon = { Icon(Icons.Default.Search, null, tint = Color(0xFF1A3A5C), modifier = Modifier.size(20.dp)) },
                            color = Color(0xFF1A3A5C),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "Resolved\nIssues",
                            value = "${stats.resolvedIssues}",
                            icon = { Icon(Icons.Default.CheckCircle, null, tint = RenovaAccent, modifier = Modifier.size(20.dp)) },
                            color = RenovaAccent,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "Completed\nInspections",
                            value = "${stats.completedInspections}",
                            icon = { Icon(Icons.Default.Assignment, null, tint = Color(0xFF6B3FA0), modifier = Modifier.size(20.dp)) },
                            color = Color(0xFF6B3FA0),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Recent Projects
            if (projects.isNotEmpty()) {
                item {
                    SectionHeader("RECENT PROJECTS", "See all", onNavigateToProjects)
                }
                items(projects.take(3)) { project ->
                    ProjectCard(
                        project = project,
                        onClick = {
                            viewModel.selectProject(project.id)
                            onNavigateToProjects()
                        },
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .alpha(listAnim.value)
                    )
                }
            } else {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🏗️", fontSize = 40.sp)
                            Spacer(Modifier.height(12.dp))
                            Text("No projects yet", style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold)
                            Text("Create your first project to start inspecting",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = onNavigateToProjects,
                                colors = ButtonDefaults.buttonColors(containerColor = RenovaPrimary)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("New Project")
                            }
                        }
                    }
                }
            }

            // Recent Activity
            if (activity.isNotEmpty()) {
                item { SectionHeader("RECENT ACTIVITY") }
                items(activity.take(5)) { event ->
                    ActivityItem(event, modifier = Modifier.alpha(listAnim.value))
                }
            }
        }
    }
}

@Composable
private fun ActivityItem(event: ActivityEvent, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp)
                .background(RenovaPrimary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            val icon = when (event.type) {
                "project"    -> Icons.Default.HomeWork
                "issue"      -> Icons.Default.Warning
                "inspection" -> Icons.Default.Assignment
                else         -> Icons.Default.Info
            }
            Icon(icon, contentDescription = null, tint = RenovaPrimary, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(event.description, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface)
            Text(
                SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(event.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
