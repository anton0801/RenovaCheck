package com.apprenova.renovacheck.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.apprenova.renovacheck.data.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RenovaPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "renova_check_prefs"
        private const val KEY_ONBOARDING_DONE = "onboarding_done"
        private const val KEY_USER_PROFILE = "user_profile"
        private const val KEY_SETTINGS = "app_settings"
        private const val KEY_PROJECTS = "projects"
        private const val KEY_ROOMS = "rooms"
        private const val KEY_CHECKLISTS = "checklists"
        private const val KEY_CHECKLIST_ITEMS = "checklist_items"
        private const val KEY_ISSUES = "issues"
        private const val KEY_INSPECTIONS = "inspections"
        private const val KEY_PHOTOS = "photos"
        private const val KEY_TASKS = "tasks"
        private const val KEY_CONTRACTOR_NOTES = "contractor_notes"
        private const val KEY_ACTIVITY = "activity_events"
    }

    // ── Onboarding ──────────────────────────────────────────────────────────
    var isOnboardingDone: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_DONE, false)
        set(value) { prefs.edit().putBoolean(KEY_ONBOARDING_DONE, value).apply() }

    // ── User Profile ────────────────────────────────────────────────────────
    var userProfile: UserProfile
        get() = getObject(KEY_USER_PROFILE) ?: UserProfile()
        set(value) { saveObject(KEY_USER_PROFILE, value) }

    // ── Settings ────────────────────────────────────────────────────────────
    var settings: AppSettings
        get() = getObject(KEY_SETTINGS) ?: AppSettings()
        set(value) { saveObject(KEY_SETTINGS, value) }

    // ── Projects ────────────────────────────────────────────────────────────
    fun getProjects(): List<Project> = getList(KEY_PROJECTS)
    fun saveProjects(list: List<Project>) = saveList(KEY_PROJECTS, list)

    fun saveProject(project: Project) {
        val list = getProjects().toMutableList()
        val idx = list.indexOfFirst { it.id == project.id }
        if (idx >= 0) list[idx] = project else list.add(0, project)
        saveProjects(list)
        logActivity(project.id, "project", "Project '${project.name}' updated")
    }

    fun deleteProject(id: String) {
        saveProjects(getProjects().filter { it.id != id })
        saveRooms(getRooms().filter { it.projectId != id })
        saveIssues(getIssues().filter { it.projectId != id })
        saveInspections(getInspections().filter { it.projectId != id })
        saveTasks(getTasks().filter { it.projectId != id })
    }

    // ── Rooms ───────────────────────────────────────────────────────────────
    fun getRooms(): List<Room> = getList(KEY_ROOMS)
    fun getRoomsForProject(projectId: String) = getRooms().filter { it.projectId == projectId }
    fun saveRooms(list: List<Room>) = saveList(KEY_ROOMS, list)

    fun saveRoom(room: Room) {
        val list = getRooms().toMutableList()
        val idx = list.indexOfFirst { it.id == room.id }
        if (idx >= 0) list[idx] = room else list.add(room)
        saveRooms(list)
        updateProjectStats(room.projectId)
    }

    fun deleteRoom(id: String) {
        val room = getRooms().firstOrNull { it.id == id } ?: return
        saveRooms(getRooms().filter { it.id != id })
        updateProjectStats(room.projectId)
    }

    // ── Checklists ──────────────────────────────────────────────────────────
    fun getChecklists(): List<Checklist> = getList(KEY_CHECKLISTS)
    fun saveChecklists(list: List<Checklist>) = saveList(KEY_CHECKLISTS, list)

    fun saveChecklist(checklist: Checklist) {
        val list = getChecklists().toMutableList()
        val idx = list.indexOfFirst { it.id == checklist.id }
        if (idx >= 0) list[idx] = checklist else list.add(checklist)
        saveChecklists(list)
    }

    // ── Issues ──────────────────────────────────────────────────────────────
    fun getIssues(): List<Issue> = getList(KEY_ISSUES)
    fun getIssuesForProject(projectId: String) = getIssues().filter { it.projectId == projectId }
    fun getIssuesForRoom(roomId: String) = getIssues().filter { it.roomId == roomId }
    fun saveIssues(list: List<Issue>) = saveList(KEY_ISSUES, list)

    fun saveIssue(issue: Issue) {
        val list = getIssues().toMutableList()
        val idx = list.indexOfFirst { it.id == issue.id }
        if (idx >= 0) list[idx] = issue else list.add(0, issue)
        saveIssues(list)
        updateProjectStats(issue.projectId)
        logActivity(issue.projectId, "issue", "Issue '${issue.title}' reported")
    }

    fun deleteIssue(id: String) {
        val issue = getIssues().firstOrNull { it.id == id } ?: return
        saveIssues(getIssues().filter { it.id != id })
        updateProjectStats(issue.projectId)
    }

    // ── Inspections ─────────────────────────────────────────────────────────
    fun getInspections(): List<Inspection> = getList(KEY_INSPECTIONS)
    fun getInspectionsForProject(projectId: String) = getInspections().filter { it.projectId == projectId }
    fun saveInspections(list: List<Inspection>) = saveList(KEY_INSPECTIONS, list)

    fun saveInspection(inspection: Inspection) {
        val list = getInspections().toMutableList()
        val idx = list.indexOfFirst { it.id == inspection.id }
        if (idx >= 0) list[idx] = inspection else list.add(0, inspection)
        saveInspections(list)
        logActivity(inspection.projectId, "inspection", "Inspection ${inspection.status.label}")
    }

    // ── Photos ───────────────────────────────────────────────────────────────
    fun getPhotos(): List<Photo> = getList(KEY_PHOTOS)
    fun getPhotosForProject(projectId: String) = getPhotos().filter { it.projectId == projectId }
    fun getPhotosForIssue(issueId: String) = getPhotos().filter { it.issueId == issueId }
    fun savePhotos(list: List<Photo>) = saveList(KEY_PHOTOS, list)

    fun savePhoto(photo: Photo) {
        val list = getPhotos().toMutableList()
        list.add(0, photo)
        savePhotos(list)
    }

    fun deletePhoto(id: String) = savePhotos(getPhotos().filter { it.id != id })

    // ── Tasks ────────────────────────────────────────────────────────────────
    fun getTasks(): List<Task> = getList(KEY_TASKS)
    fun getTasksForProject(projectId: String) = getTasks().filter { it.projectId == projectId }
    fun saveTasks(list: List<Task>) = saveList(KEY_TASKS, list)

    fun saveTask(task: Task) {
        val list = getTasks().toMutableList()
        val idx = list.indexOfFirst { it.id == task.id }
        if (idx >= 0) list[idx] = task else list.add(0, task)
        saveTasks(list)
    }

    // ── Contractor Notes ─────────────────────────────────────────────────────
    fun getContractorNotes(): List<ContractorNote> = getList(KEY_CONTRACTOR_NOTES)
    fun getNotesForProject(projectId: String) = getContractorNotes().filter { it.projectId == projectId }
    fun saveContractorNotes(list: List<ContractorNote>) = saveList(KEY_CONTRACTOR_NOTES, list)

    fun saveContractorNote(note: ContractorNote) {
        val list = getContractorNotes().toMutableList()
        val idx = list.indexOfFirst { it.id == note.id }
        if (idx >= 0) list[idx] = note else list.add(0, note)
        saveContractorNotes(list)
    }

    // ── Activity ─────────────────────────────────────────────────────────────
    fun getActivity(): List<ActivityEvent> = getList(KEY_ACTIVITY)
    fun getActivityForProject(projectId: String) = getActivity().filter { it.projectId == projectId }

    private fun logActivity(projectId: String, type: String, description: String) {
        val list = getActivity().toMutableList()
        list.add(0, ActivityEvent(projectId = projectId, type = type, description = description, entityType = type))
        if (list.size > 200) list.dropLast(list.size - 200)
        saveList(KEY_ACTIVITY, list)
    }

    // ── Stats helpers ─────────────────────────────────────────────────────────
    private fun updateProjectStats(projectId: String) {
        val projects = getProjects().toMutableList()
        val idx = projects.indexOfFirst { it.id == projectId }
        if (idx < 0) return
        val rooms = getRoomsForProject(projectId)
        val issues = getIssuesForProject(projectId).filter { !it.isResolved }
        val completed = rooms.count { it.status == InspectionStatus.PASSED }
        val avgScore = if (rooms.isNotEmpty()) rooms.sumOf { it.score.toLong() }.toInt() / rooms.size else 0
        projects[idx] = projects[idx].copy(
            totalRooms = rooms.size,
            completedRooms = completed,
            issueCount = issues.size,
            score = avgScore,
            updatedAt = System.currentTimeMillis()
        )
        saveProjects(projects)
    }

    // ── Generic helpers ───────────────────────────────────────────────────────
    private fun <T> saveObject(key: String, obj: T) {
        prefs.edit().putString(key, gson.toJson(obj)).apply()
    }

    private inline fun <reified T> getObject(key: String): T? {
        val json = prefs.getString(key, null) ?: return null
        return try { gson.fromJson(json, T::class.java) } catch (e: Exception) { null }
    }

    private inline fun <reified T> getList(key: String): List<T> {
        val json = prefs.getString(key, null) ?: return emptyList()
        return try {
            val type = TypeToken.getParameterized(List::class.java, T::class.java).type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }

    private fun <T> saveList(key: String, list: List<T>) {
        prefs.edit().putString(key, gson.toJson(list)).apply()
    }

    fun clearAll() = prefs.edit().clear().apply()
}
