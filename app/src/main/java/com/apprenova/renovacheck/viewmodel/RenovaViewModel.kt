package com.apprenova.renovacheck.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.apprenova.renovacheck.data.model.*
import com.apprenova.renovacheck.data.repository.RenovaRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RenovaViewModel(application: Application) : AndroidViewModel(application) {

    val repository = RenovaRepository(application)

    // ── Exposed Flows ─────────────────────────────────────────────────────────
    val projects = repository.projects
    val issues   = repository.issues
    val rooms    = repository.rooms
    val inspections = repository.inspections
    val tasks    = repository.tasks
    val photos   = repository.photos
    val checklists = repository.checklists
    val activity = repository.activity

    // ── UI State ──────────────────────────────────────────────────────────────
    private val _selectedProjectId = MutableStateFlow<String?>(null)
    val selectedProjectId = _selectedProjectId.asStateFlow()

    private val _selectedRoomId = MutableStateFlow<String?>(null)
    val selectedRoomId = _selectedRoomId.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage = _toastMessage.asStateFlow()

    // ── Dashboard ─────────────────────────────────────────────────────────────
    val dashboardStats = combine(projects, issues, rooms, inspections) { p, i, r, ins ->
        repository.getDashboardStats()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.getDashboardStats())

    // ── Selection helpers ─────────────────────────────────────────────────────
    fun selectProject(id: String) { _selectedProjectId.value = id }
    fun selectRoom(id: String)    { _selectedRoomId.value = id }
    fun getSelectedProject() = _selectedProjectId.value?.let { repository.getProject(it) }
    fun getSelectedRoom() = _selectedRoomId.value?.let { id -> rooms.value.firstOrNull { it.id == id } }

    // ── Projects ──────────────────────────────────────────────────────────────
    fun createProject(name: String, type: ProjectType, address: String = "", contractor: String = "") {
        val project = Project(name = name, type = type, address = address, contractorName = contractor)
        repository.saveProject(project)
        showToast("Project '$name' created")
    }

    fun updateProject(project: Project) = repository.saveProject(project)
    fun deleteProject(id: String) { repository.deleteProject(id); showToast("Project deleted") }

    // ── Rooms ─────────────────────────────────────────────────────────────────
    fun createRoom(name: String, projectId: String, area: Float = 0f) {
        val room = Room(name = name, projectId = projectId, area = area)
        repository.saveRoom(room)
        showToast("Room '$name' added")
    }

    fun updateRoom(room: Room) = repository.saveRoom(room)
    fun deleteRoom(id: String) { repository.deleteRoom(id) }
    fun getRoomsForProject(projectId: String) = repository.getRoomsForProject(projectId)

    // ── Checklists ────────────────────────────────────────────────────────────
    fun getDefaultChecklists() = repository.getDefaultChecklists()

    fun createCustomChecklist(name: String, items: List<String>, projectId: String = "", roomId: String = "") {
        val checklist = Checklist(
            name = name,
            category = ChecklistCategory.CUSTOM,
            projectId = projectId,
            roomId = roomId,
            items = items.mapIndexed { i, title ->
                ChecklistItem(title = title, checklistId = "", order = i)
            }
        )
        repository.saveChecklist(checklist)
        showToast("Checklist '$name' created")
    }

    fun updateChecklistItem(checklist: Checklist, itemId: String, isChecked: Boolean, isFailed: Boolean, notes: String = "") {
        val updatedItems = checklist.items.map { item ->
            if (item.id == itemId) item.copy(isChecked = isChecked, isFailed = isFailed, notes = notes)
            else item
        }
        repository.saveChecklist(checklist.copy(items = updatedItems))
    }

    // ── Issues ────────────────────────────────────────────────────────────────
    fun createIssue(
        title: String,
        description: String,
        location: String,
        severity: IssueSeverity,
        projectId: String,
        roomId: String = "",
        inspectionId: String = ""
    ) {
        val issue = Issue(
            title = title,
            description = description,
            location = location,
            severity = severity,
            projectId = projectId,
            roomId = roomId,
            inspectionId = inspectionId
        )
        repository.saveIssue(issue)
        showToast("Issue reported")
    }

    fun resolveIssue(id: String) { repository.resolveIssue(id); showToast("Issue resolved ✓") }
    fun deleteIssue(id: String) = repository.deleteIssue(id)
    fun getIssuesForProject(projectId: String) = repository.getIssuesForProject(projectId)
    fun getIssuesForRoom(roomId: String) = repository.getIssuesForRoom(roomId)

    // ── Inspections ───────────────────────────────────────────────────────────
    fun startInspection(projectId: String, roomId: String, checklistId: String): Inspection {
        val inspection = Inspection(
            projectId = projectId,
            roomId = roomId,
            checklistId = checklistId,
            status = InspectionStatus.IN_PROGRESS,
            inspectorName = repository.userProfile.name
        )
        repository.saveInspection(inspection)
        return inspection
    }

    fun completeInspection(inspection: Inspection, passedItems: Int, failedItems: Int) {
        val total = passedItems + failedItems
        val score = if (total > 0) ((passedItems.toFloat() / total) * 100).toInt() else 0
        val passed = score >= 70
        repository.completeInspection(inspection.id, passed, score)
        if (passed) showToast("Inspection Passed ✓ — Score: $score%")
        else showToast("Inspection Failed — Score: $score%")
    }

    // ── Photos ────────────────────────────────────────────────────────────────
    fun addPhoto(uri: String, projectId: String, roomId: String = "", issueId: String = "", isBefore: Boolean = true, caption: String = "") {
        val photo = Photo(uri = uri, projectId = projectId, roomId = roomId, issueId = issueId, isBefore = isBefore, caption = caption)
        repository.savePhoto(photo)
    }

    fun deletePhoto(id: String) = repository.deletePhoto(id)
    fun getPhotosForProject(projectId: String) = repository.getPhotosForProject(projectId)
    fun getPhotosForIssue(issueId: String) = repository.getPhotosForIssue(issueId)

    // ── Tasks ─────────────────────────────────────────────────────────────────
    fun createTask(title: String, description: String, projectId: String, issueId: String = "", priority: IssueSeverity = IssueSeverity.MEDIUM, assignee: String = "") {
        val task = Task(title = title, description = description, projectId = projectId, issueId = issueId, priority = priority, assignee = assignee)
        repository.saveTask(task)
        showToast("Task created")
    }

    fun completeTask(id: String) { repository.completeTask(id); showToast("Task completed ✓") }
    fun getTasksForProject(projectId: String) = repository.getTasksForProject(projectId)

    // ── Contractor Notes ──────────────────────────────────────────────────────
    fun addContractorNote(contractorName: String, note: String, projectId: String, rating: Int = 0) {
        repository.saveContractorNote(ContractorNote(contractorName = contractorName, note = note, projectId = projectId, rating = rating))
        showToast("Note saved")
    }

    fun getNotesForProject(projectId: String) = repository.getNotesForProject(projectId)

    // ── Profile & Settings ────────────────────────────────────────────────────
    val userProfile get() = repository.userProfile
    val appSettings get() = repository.settings
    val isOnboardingDone get() = repository.isOnboardingDone

    fun saveProfile(profile: UserProfile) { repository.saveProfile(profile); showToast("Profile saved") }
    fun saveSettings(settings: AppSettings) { repository.saveSettings(settings) }
    fun completeOnboarding() = repository.completeOnboarding()

    // ── Report generation ─────────────────────────────────────────────────────
    fun generateReportSummary(projectId: String): String {
        val project = repository.getProject(projectId) ?: return "Project not found"
        val rooms = repository.getRoomsForProject(projectId)
        val issues = repository.getIssuesForProject(projectId)
        val inspections = repository.getInspectionsForProject(projectId)
        val openIssues = issues.filter { !it.isResolved }
        val resolvedIssues = issues.filter { it.isResolved }
        val avgScore = if (rooms.isNotEmpty()) rooms.sumOf { it.score.toLong() }.toInt() / rooms.size else 0

        return buildString {
            appendLine("═══════════════════════════════════")
            appendLine("  RENOVA CHECK — INSPECTION REPORT")
            appendLine("═══════════════════════════════════")
            appendLine("Project: ${project.name}")
            appendLine("Type: ${project.type.label}")
            appendLine("Contractor: ${project.contractorName.ifEmpty { "—" }}")
            appendLine("")
            appendLine("SUMMARY")
            appendLine("───────────────────────────────────")
            appendLine("Rooms Total:   ${rooms.size}")
            appendLine("Rooms Passed:  ${rooms.count { it.status == InspectionStatus.PASSED }}")
            appendLine("Avg. Score:    $avgScore%")
            appendLine("Issues Found:  ${openIssues.size}")
            appendLine("Resolved:      ${resolvedIssues.size}")
            appendLine("")
            appendLine("ISSUES BY SEVERITY")
            appendLine("───────────────────────────────────")
            IssueSeverity.entries.forEach { s ->
                val count = openIssues.count { it.severity == s }
                if (count > 0) appendLine("${s.label}: $count")
            }
            appendLine("")
            appendLine("ROOMS")
            appendLine("───────────────────────────────────")
            rooms.forEach { r ->
                appendLine("• ${r.name} — ${r.status.label} (${r.score}%)")
            }
            appendLine("")
            appendLine("Generated: ${java.text.SimpleDateFormat("dd MMM yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}")
        }
    }

    // ── Toast ─────────────────────────────────────────────────────────────────
    fun showToast(message: String) { _toastMessage.value = message }
    fun clearToast() { _toastMessage.value = null }
}
