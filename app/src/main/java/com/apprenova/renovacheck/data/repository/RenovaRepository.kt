package com.apprenova.renovacheck.data.repository

import android.content.Context
import com.apprenova.renovacheck.data.model.*
import com.apprenova.renovacheck.data.preferences.RenovaPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RenovaRepository(context: Context) {

    private val prefs = RenovaPreferences(context)

    // ── Reactive Flows ────────────────────────────────────────────────────────
    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    private val _rooms = MutableStateFlow<List<Room>>(emptyList())
    val rooms: StateFlow<List<Room>> = _rooms.asStateFlow()

    private val _issues = MutableStateFlow<List<Issue>>(emptyList())
    val issues: StateFlow<List<Issue>> = _issues.asStateFlow()

    private val _inspections = MutableStateFlow<List<Inspection>>(emptyList())
    val inspections: StateFlow<List<Inspection>> = _inspections.asStateFlow()

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _photos = MutableStateFlow<List<Photo>>(emptyList())
    val photos: StateFlow<List<Photo>> = _photos.asStateFlow()

    private val _checklists = MutableStateFlow<List<Checklist>>(emptyList())
    val checklists: StateFlow<List<Checklist>> = _checklists.asStateFlow()

    private val _activity = MutableStateFlow<List<ActivityEvent>>(emptyList())
    val activity: StateFlow<List<ActivityEvent>> = _activity.asStateFlow()

    // onboarding / profile
    val isOnboardingDone get() = prefs.isOnboardingDone
    val userProfile get() = prefs.userProfile
    val settings get() = prefs.settings

    init { loadAll() }

    private fun loadAll() {
        _projects.value    = prefs.getProjects()
        _rooms.value       = prefs.getRooms()
        _issues.value      = prefs.getIssues()
        _inspections.value = prefs.getInspections()
        _tasks.value       = prefs.getTasks()
        _photos.value      = prefs.getPhotos()
        _checklists.value  = prefs.getChecklists()
        _activity.value    = prefs.getActivity()
    }

    // ── Projects ──────────────────────────────────────────────────────────────
    fun saveProject(project: Project) {
        prefs.saveProject(project)
        _projects.value = prefs.getProjects()
    }

    fun deleteProject(id: String) {
        prefs.deleteProject(id)
        loadAll()
    }

    fun getProject(id: String) = _projects.value.firstOrNull { it.id == id }

    // ── Rooms ─────────────────────────────────────────────────────────────────
    fun saveRoom(room: Room) {
        prefs.saveRoom(room)
        _rooms.value = prefs.getRooms()
        _projects.value = prefs.getProjects()
    }

    fun deleteRoom(id: String) {
        prefs.deleteRoom(id)
        _rooms.value = prefs.getRooms()
        _projects.value = prefs.getProjects()
    }

    fun getRoomsForProject(projectId: String) = _rooms.value.filter { it.projectId == projectId }

    // ── Checklists ────────────────────────────────────────────────────────────
    fun saveChecklist(checklist: Checklist) {
        prefs.saveChecklist(checklist)
        _checklists.value = prefs.getChecklists()
    }

    fun getDefaultChecklists(): List<Checklist> {
        return ChecklistCategory.entries
            .filter { it != ChecklistCategory.CUSTOM }
            .map { cat ->
                val items = when (cat) {
                    ChecklistCategory.WALLS       -> DefaultChecklists.walls()
                    ChecklistCategory.PAINTING    -> DefaultChecklists.painting()
                    ChecklistCategory.TILES       -> DefaultChecklists.tiles()
                    ChecklistCategory.ELECTRICAL  -> DefaultChecklists.electrical()
                    ChecklistCategory.PLUMBING    -> DefaultChecklists.plumbing()
                    else -> emptyList()
                }
                Checklist(
                    name = cat.label,
                    category = cat,
                    items = items.map { it.copy(id = java.util.UUID.randomUUID().toString()) },
                    isDefault = true
                )
            }
    }

    // ── Issues ────────────────────────────────────────────────────────────────
    fun saveIssue(issue: Issue) {
        prefs.saveIssue(issue)
        _issues.value = prefs.getIssues()
        _projects.value = prefs.getProjects()
    }

    fun deleteIssue(id: String) {
        prefs.deleteIssue(id)
        _issues.value = prefs.getIssues()
        _projects.value = prefs.getProjects()
    }

    fun resolveIssue(id: String) {
        val issue = _issues.value.firstOrNull { it.id == id } ?: return
        saveIssue(issue.copy(isResolved = true, resolvedAt = System.currentTimeMillis(), status = "Resolved"))
    }

    fun getIssuesForProject(projectId: String) = _issues.value.filter { it.projectId == projectId }
    fun getIssuesForRoom(roomId: String) = _issues.value.filter { it.roomId == roomId }

    // ── Inspections ───────────────────────────────────────────────────────────
    fun saveInspection(inspection: Inspection) {
        prefs.saveInspection(inspection)
        _inspections.value = prefs.getInspections()
    }

    fun completeInspection(id: String, passed: Boolean, score: Int) {
        val inspection = _inspections.value.firstOrNull { it.id == id } ?: return
        val status = if (passed) InspectionStatus.PASSED else InspectionStatus.FAILED
        saveInspection(inspection.copy(
            status = status,
            score = score,
            completedAt = System.currentTimeMillis()
        ))
        // Update room score
        val room = _rooms.value.firstOrNull { it.id == inspection.roomId }
        room?.let { saveRoom(it.copy(status = status, score = score)) }
    }

    fun getInspectionsForProject(projectId: String) = _inspections.value.filter { it.projectId == projectId }

    // ── Photos ────────────────────────────────────────────────────────────────
    fun savePhoto(photo: Photo) {
        prefs.savePhoto(photo)
        _photos.value = prefs.getPhotos()
    }

    fun deletePhoto(id: String) {
        prefs.deletePhoto(id)
        _photos.value = prefs.getPhotos()
    }

    fun getPhotosForProject(projectId: String) = _photos.value.filter { it.projectId == projectId }
    fun getPhotosForIssue(issueId: String) = _photos.value.filter { it.issueId == issueId }

    // ── Tasks ─────────────────────────────────────────────────────────────────
    fun saveTask(task: Task) {
        prefs.saveTask(task)
        _tasks.value = prefs.getTasks()
    }

    fun completeTask(id: String) {
        val task = _tasks.value.firstOrNull { it.id == id } ?: return
        saveTask(task.copy(isCompleted = true, completedAt = System.currentTimeMillis()))
    }

    fun getTasksForProject(projectId: String) = _tasks.value.filter { it.projectId == projectId }

    // ── Contractor Notes ──────────────────────────────────────────────────────
    fun saveContractorNote(note: ContractorNote) {
        prefs.saveContractorNote(note)
    }

    fun getNotesForProject(projectId: String) = prefs.getNotesForProject(projectId)

    // ── Profile & Settings ────────────────────────────────────────────────────
    fun saveProfile(profile: UserProfile) { prefs.userProfile = profile }
    fun saveSettings(settings: AppSettings) { prefs.settings = settings }
    fun completeOnboarding() { prefs.isOnboardingDone = true }

    // ── Activity ──────────────────────────────────────────────────────────────
    fun refreshActivity() { _activity.value = prefs.getActivity() }

    // ── Dashboard Stats ───────────────────────────────────────────────────────
    data class DashboardStats(
        val activeProjects: Int,
        val totalIssues: Int,
        val resolvedIssues: Int,
        val roomsChecked: Int,
        val totalRooms: Int,
        val activeInspections: Int,
        val completedInspections: Int,
        val recentActivity: List<ActivityEvent>
    )

    fun getDashboardStats(): DashboardStats {
        val allIssues = _issues.value
        val allRooms = _rooms.value
        val allInspections = _inspections.value
        return DashboardStats(
            activeProjects = _projects.value.count { it.status != InspectionStatus.PASSED },
            totalIssues = allIssues.count { !it.isResolved },
            resolvedIssues = allIssues.count { it.isResolved },
            roomsChecked = allRooms.count { it.status == InspectionStatus.PASSED },
            totalRooms = allRooms.size,
            activeInspections = allInspections.count { it.status == InspectionStatus.IN_PROGRESS },
            completedInspections = allInspections.count { it.status == InspectionStatus.PASSED || it.status == InspectionStatus.FAILED },
            recentActivity = _activity.value.take(10)
        )
    }
}