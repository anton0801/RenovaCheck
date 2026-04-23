package com.apprenova.renovacheck.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.*
import androidx.navigation.compose.*
import com.apprenova.renovacheck.data.model.Checklist
import com.apprenova.renovacheck.ui.components.RenovaTopBar
import com.apprenova.renovacheck.ui.screens.checklists.*
import com.apprenova.renovacheck.ui.screens.dashboard.DashboardScreen
import com.apprenova.renovacheck.ui.screens.issues.*
import com.apprenova.renovacheck.ui.screens.misc.*
import com.apprenova.renovacheck.ui.screens.photos.*
import com.apprenova.renovacheck.ui.screens.profile.*
import com.apprenova.renovacheck.ui.screens.projects.*
import com.apprenova.renovacheck.ui.screens.reports.ReportsScreen
import com.apprenova.renovacheck.ui.screens.rooms.*
import com.apprenova.renovacheck.ui.screens.splash.SplashScreen
import com.apprenova.renovacheck.ui.screens.tasks.*
import com.apprenova.renovacheck.ui.screens.welcome.*
import com.apprenova.renovacheck.ui.theme.*
import com.apprenova.renovacheck.viewmodel.RenovaViewModel

private data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector
)

private val BOTTOM_NAV_ITEMS = listOf(
    BottomNavItem(Route.DASHBOARD,  "Home",     Icons.Filled.Home,      Icons.Outlined.Home),
    BottomNavItem(Route.PROJECTS,   "Projects", Icons.Filled.HomeWork,  Icons.Outlined.HomeWork),
    BottomNavItem(Route.CHECKLISTS, "Lists",    Icons.Filled.Checklist, Icons.Outlined.Checklist),
    BottomNavItem(Route.PROFILE,    "Profile",  Icons.Filled.Person,    Icons.Outlined.Person),
    BottomNavItem(Route.SETTINGS,   "Settings", Icons.Filled.Settings,  Icons.Outlined.Settings),
)

@Composable
fun ChecklistDetailScreen(checklist: Checklist, onBack: () -> Unit) {
    Scaffold(
        containerColor = RenovaBackground,
        topBar = { RenovaTopBar(checklist.name, "${checklist.items.size} items", onBack = onBack) }
    ) { pv ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(pv),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(checklist.items) { index, item ->
                Card(shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(28.dp)
                                .background(RenovaPrimary.copy(alpha = 0.1f), RoundedCornerShape(7.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${index + 1}", style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold, color = RenovaPrimary)
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(item.title, style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.CheckCircleOutline, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun RenovaNavHost(viewModel: RenovaViewModel) {
    val navController = rememberNavController()
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route

    val toast by viewModel.toastMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(toast) {
        toast?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearToast()
        }
    }

    val showBottomNav = currentRoute in BOTTOM_NAV_ITEMS.map { it.route }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(snackbarData = data, containerColor = RenovaPrimary,
                    contentColor = Color.White, shape = RoundedCornerShape(12.dp))
            }
        },
        bottomBar = {
            if (showBottomNav) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
                    BOTTOM_NAV_ITEMS.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (!selected) navController.navigate(item.route) {
                                    popUpTo(Route.DASHBOARD) { saveState = true }
                                    launchSingleTop = true; restoreState = true
                                }
                            },
                            icon = { Icon(if (selected) item.selectedIcon else item.unselectedIcon,
                                item.label, modifier = Modifier.size(22.dp)) },
                            label = { Text(item.label, style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = RenovaPrimary, selectedTextColor = RenovaPrimary,
                                indicatorColor = RenovaPrimary.copy(alpha = 0.12f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.SPLASH,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                fadeIn(tween(220)) + slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start, tween(220))
            },
            exitTransition = {
                fadeOut(tween(220)) + slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start, tween(220))
            },
            popEnterTransition = {
                fadeIn(tween(220)) + slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.End, tween(220))
            },
            popExitTransition = {
                fadeOut(tween(220)) + slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End, tween(220))
            }
        ) {
            composable(Route.SPLASH, enterTransition = { fadeIn(tween(300)) }, exitTransition = { fadeOut(tween(400)) }) {
                SplashScreen(onFinished = {
                    val dest = if (viewModel.isOnboardingDone) Route.DASHBOARD else Route.WELCOME
                    navController.navigate(dest) { popUpTo(Route.SPLASH) { inclusive = true } }
                })
            }
            composable(Route.WELCOME) {
                WelcomeScreen(
                    onStart = { navController.navigate(Route.ONBOARDING) },
                    onLogin = {
                        viewModel.completeOnboarding()
                        navController.navigate(Route.DASHBOARD) { popUpTo(Route.WELCOME) { inclusive = true } }
                    }
                )
            }
            composable(Route.ONBOARDING) {
                OnboardingScreen(onFinished = {
                    viewModel.completeOnboarding()
                    navController.navigate(Route.DASHBOARD) { popUpTo(Route.WELCOME) { inclusive = true } }
                })
            }
            composable(Route.DASHBOARD) {
                DashboardScreen(viewModel = viewModel,
                    onNavigateToProjects = { navController.navigate(Route.PROJECTS) },
                    onNavigateToIssues = {},
                    onNavigateToNotifications = { navController.navigate(Route.NOTIFICATIONS) })
            }
            composable(Route.PROJECTS) {
                ProjectsScreen(viewModel = viewModel,
                    onAddProject = { navController.navigate(Route.ADD_PROJECT) },
                    onProjectClick = { id -> navController.navigate(Route.projectDetail(id)) })
            }
            composable(Route.ADD_PROJECT) {
                AddProjectScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Route.PROJECT_DETAIL,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType })
            ) { entry ->
                val pid = entry.arguments?.getString("projectId") ?: return@composable
                ProjectDetailScreen(projectId = pid, viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onRooms = { navController.navigate(Route.rooms(pid)) },
                    onIssues = { navController.navigate(Route.issues(pid)) },
                    onReports = { navController.navigate(Route.reports(pid)) },
                    onTimeline = { navController.navigate(Route.timeline(pid)) },
                    onTasks = { navController.navigate(Route.tasks(pid)) },
                    onContractorNotes = { navController.navigate(Route.contractorNotes(pid)) })
            }
            composable(Route.ROOMS,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType })
            ) { entry ->
                val pid = entry.arguments?.getString("projectId") ?: return@composable
                RoomsScreen(projectId = pid, viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onAddRoom = { navController.navigate(Route.addRoom(pid)) },
                    onRoomClick = { rid -> navController.navigate(Route.roomDetail(rid)) })
            }
            composable(Route.ADD_ROOM,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType })
            ) { entry ->
                val pid = entry.arguments?.getString("projectId") ?: return@composable
                AddRoomScreen(projectId = pid, viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Route.ROOM_DETAIL,
                arguments = listOf(navArgument("roomId") { type = NavType.StringType })
            ) { entry ->
                val rid = entry.arguments?.getString("roomId") ?: return@composable
                val pid = viewModel.rooms.value.firstOrNull { it.id == rid }?.projectId ?: ""
                RoomDetailScreen(roomId = rid, viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onStartInspection = { rId, clId ->
                        val cl = viewModel.getDefaultChecklists().firstOrNull { it.id == clId }
                        if (cl != null) viewModel.repository.saveChecklist(cl)
                        navController.navigate(Route.inspection(rId, clId))
                    },
                    onIssues = { navController.navigate(Route.issues(pid)) },
                    onScore  = { navController.navigate(Route.roomScore(rid)) })
            }
            composable(Route.CHECKLISTS) {
                ChecklistsScreen(viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onChecklistClick = { id -> navController.navigate(Route.checklistDetail(id)) },
                    onCreateCustom = { navController.navigate(Route.CUSTOM_CHECKLIST) })
            }
            composable(Route.CHECKLIST_DETAIL,
                arguments = listOf(navArgument("checklistId") { type = NavType.StringType })
            ) { entry ->
                val id = entry.arguments?.getString("checklistId") ?: return@composable
                val cl = viewModel.checklists.value.firstOrNull { it.id == id }
                    ?: viewModel.getDefaultChecklists().firstOrNull { it.id == id }
                cl?.let { ChecklistDetailScreen(it) { navController.popBackStack() } }
            }
            composable(Route.CUSTOM_CHECKLIST) {
                CustomChecklistScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Route.INSPECTION,
                arguments = listOf(
                    navArgument("roomId") { type = NavType.StringType },
                    navArgument("checklistId") { type = NavType.StringType }
                )
            ) { entry ->
                val rid = entry.arguments?.getString("roomId") ?: return@composable
                val clId = entry.arguments?.getString("checklistId") ?: return@composable
                val pid = viewModel.rooms.value.firstOrNull { it.id == rid }?.projectId ?: ""
                InspectionScreen(roomId = rid, checklistId = clId, viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onComplete = { inspId -> navController.navigate(Route.inspectionResult(inspId)) },
                    onAddIssue = { pId -> navController.navigate(Route.addIssue(pId)) })
            }
            composable(Route.INSPECTION_RESULT,
                arguments = listOf(navArgument("inspectionId") { type = NavType.StringType })
            ) { entry ->
                val id = entry.arguments?.getString("inspectionId") ?: return@composable
                InspectionResultScreen(inspectionId = id, viewModel = viewModel,
                    onBack = { navController.navigate(Route.PROJECTS) { popUpTo(Route.DASHBOARD) } },
                    onAddIssue = { pId -> navController.navigate(Route.addIssue(pId)) })
            }
            composable(Route.ISSUES,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType })
            ) { entry ->
                val pid = entry.arguments?.getString("projectId") ?: return@composable
                IssuesScreen(projectId = pid, viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onAddIssue = { navController.navigate(Route.addIssue(pid)) },
                    onIssueClick = { id -> navController.navigate(Route.issueDetail(id)) })
            }
            composable(Route.ADD_ISSUE,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType })
            ) { entry ->
                val pid = entry.arguments?.getString("projectId") ?: return@composable
                AddIssueScreen(projectId = pid, viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Route.ISSUE_DETAIL,
                arguments = listOf(navArgument("issueId") { type = NavType.StringType })
            ) { entry ->
                val id = entry.arguments?.getString("issueId") ?: return@composable
                IssueDetailScreen(issueId = id, viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Route.PHOTOS,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType })
            ) { entry ->
                val pid = entry.arguments?.getString("projectId") ?: return@composable
                PhotosScreen(projectId = pid, viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onBeforeAfter = { navController.navigate(Route.beforeAfter(pid)) })
            }
            composable(Route.BEFORE_AFTER,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType })
            ) { entry ->
                val pid = entry.arguments?.getString("projectId") ?: return@composable
                BeforeAfterScreen(projectId = pid, viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Route.ROOM_SCORE,
                arguments = listOf(navArgument("roomId") { type = NavType.StringType })
            ) { entry ->
                val rid = entry.arguments?.getString("roomId") ?: return@composable
                RoomScoreScreen(roomId = rid, viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Route.REPORTS,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType })
            ) { entry ->
                val pid = entry.arguments?.getString("projectId") ?: return@composable
                ReportsScreen(projectId = pid, viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Route.CONTRACTOR_NOTES,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType })
            ) { entry ->
                val pid = entry.arguments?.getString("projectId") ?: return@composable
                ContractorNotesScreen(projectId = pid, viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Route.TIMELINE,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType })
            ) { entry ->
                val pid = entry.arguments?.getString("projectId") ?: return@composable
                TimelineScreen(projectId = pid, viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Route.TASKS,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType })
            ) { entry ->
                val pid = entry.arguments?.getString("projectId") ?: return@composable
                TasksScreen(projectId = pid, viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Route.NOTIFICATIONS) {
                NotificationsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Route.ACTIVITY) {
                ActivityScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Route.PROFILE) {
                ProfileScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Route.SETTINGS) {
                SettingsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
        }
    }
}
