package com.apprenova.renovacheck.data.model

import java.util.UUID

// ── Enums ─────────────────────────────────────────────────────────────────────

enum class ProjectType(val label: String, val icon: String) {
    APARTMENT("Apartment", "🏢"),
    HOUSE("House", "🏠"),
    OFFICE("Office", "🏗️"),
    COMMERCIAL("Commercial", "🏬"),
    OTHER("Other", "📦")
}

enum class IssueSeverity(val label: String, val color: Long) {
    CRITICAL("Critical", 0xFFD93025),
    HIGH("High", 0xFFFF6D00),
    MEDIUM("Medium", 0xFFF5A623),
    LOW("Low", 0xFF34A853)
}

enum class InspectionStatus(val label: String) {
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    PASSED("Passed"),
    FAILED("Failed"),
    NEEDS_REWORK("Needs Rework")
}

enum class ChecklistCategory(val label: String, val icon: String) {
    WALLS("Walls", "🧱"),
    PAINTING("Painting", "🖌️"),
    TILES("Tiles", "⬜"),
    ELECTRICAL("Electrical", "⚡"),
    PLUMBING("Plumbing", "🚿"),
    FLOORING("Flooring", "📐"),
    CEILING("Ceiling", "⬆️"),
    WINDOWS("Windows & Doors", "🪟"),
    CUSTOM("Custom", "✏️")
}

// ── Core Models ───────────────────────────────────────────────────────────────

data class Project(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val type: ProjectType = ProjectType.APARTMENT,
    val address: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val status: InspectionStatus = InspectionStatus.PENDING,
    val totalRooms: Int = 0,
    val completedRooms: Int = 0,
    val issueCount: Int = 0,
    val score: Int = 0,
    val contractorName: String = "",
    val notes: String = ""
)

data class Room(
    val id: String = UUID.randomUUID().toString(),
    val projectId: String = "",
    val name: String = "",
    val area: Float = 0f,
    val createdAt: Long = System.currentTimeMillis(),
    val status: InspectionStatus = InspectionStatus.PENDING,
    val score: Int = 0,
    val issueCount: Int = 0,
    val photoCount: Int = 0
)

data class ChecklistItem(
    val id: String = UUID.randomUUID().toString(),
    val checklistId: String = "",
    val title: String = "",
    val description: String = "",
    val isChecked: Boolean = false,
    val isFailed: Boolean = false,
    val notes: String = "",
    val order: Int = 0
)

data class Checklist(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val category: ChecklistCategory = ChecklistCategory.CUSTOM,
    val projectId: String = "",
    val roomId: String = "",
    val items: List<ChecklistItem> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val isDefault: Boolean = false
)

data class Issue(
    val id: String = UUID.randomUUID().toString(),
    val projectId: String = "",
    val roomId: String = "",
    val inspectionId: String = "",
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val severity: IssueSeverity = IssueSeverity.MEDIUM,
    val status: String = "Open",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val photoIds: List<String> = emptyList(),
    val isResolved: Boolean = false,
    val resolvedAt: Long? = null
)

data class Inspection(
    val id: String = UUID.randomUUID().toString(),
    val projectId: String = "",
    val roomId: String = "",
    val checklistId: String = "",
    val status: InspectionStatus = InspectionStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val score: Int = 0,
    val totalItems: Int = 0,
    val passedItems: Int = 0,
    val failedItems: Int = 0,
    val issueIds: List<String> = emptyList(),
    val notes: String = "",
    val inspectorName: String = ""
)

data class Photo(
    val id: String = UUID.randomUUID().toString(),
    val projectId: String = "",
    val roomId: String = "",
    val issueId: String = "",
    val uri: String = "",
    val thumbnailUri: String = "",
    val caption: String = "",
    val isBefore: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val type: String = "defect"
)

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val projectId: String = "",
    val issueId: String = "",
    val title: String = "",
    val description: String = "",
    val dueDate: Long? = null,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val priority: IssueSeverity = IssueSeverity.MEDIUM,
    val assignee: String = ""
)

data class ContractorNote(
    val id: String = UUID.randomUUID().toString(),
    val projectId: String = "",
    val contractorName: String = "",
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val rating: Int = 0
)

data class ActivityEvent(
    val id: String = UUID.randomUUID().toString(),
    val projectId: String = "",
    val type: String = "",
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val entityId: String = "",
    val entityType: String = ""
)

data class UserProfile(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "Inspector",
    val company: String = "",
    val avatarUri: String = ""
)

data class AppSettings(
    val notificationsEnabled: Boolean = true,
    val darkMode: Boolean = false,
    val units: String = "metric",
    val language: String = "en",
    val autoBackup: Boolean = true,
    val reportFormat: String = "PDF"
)

// ── Default Checklists ────────────────────────────────────────────────────────

object DefaultChecklists {
    fun walls() = listOf(
        ChecklistItem(title = "Wall flatness (tolerance ≤ 2mm/2m)", order = 0),
        ChecklistItem(title = "No cracks or splits", order = 1),
        ChecklistItem(title = "No holes or dents", order = 2),
        ChecklistItem(title = "Corner alignment is straight", order = 3),
        ChecklistItem(title = "Surface is smooth before painting", order = 4),
        ChecklistItem(title = "No moisture stains or mold", order = 5),
        ChecklistItem(title = "Proper wall-to-floor joint", order = 6)
    )

    fun painting() = listOf(
        ChecklistItem(title = "Uniform color coverage — no streaks", order = 0),
        ChecklistItem(title = "No visible brush marks", order = 1),
        ChecklistItem(title = "Even sheen across surface", order = 2),
        ChecklistItem(title = "Clean cut lines at edges", order = 3),
        ChecklistItem(title = "No paint drips or runs", order = 4),
        ChecklistItem(title = "Correct number of coats applied", order = 5),
        ChecklistItem(title = "Baseboards and trim protected", order = 6)
    )

    fun tiles() = listOf(
        ChecklistItem(title = "Tiles are level — no lippage", order = 0),
        ChecklistItem(title = "Grout lines are even width", order = 1),
        ChecklistItem(title = "No hollow tiles (tap test)", order = 2),
        ChecklistItem(title = "No cracked or chipped tiles", order = 3),
        ChecklistItem(title = "Grout is fully filled", order = 4),
        ChecklistItem(title = "Pattern alignment is correct", order = 5),
        ChecklistItem(title = "Edge tiles properly cut", order = 6),
        ChecklistItem(title = "Waterproofing joints sealed", order = 7)
    )

    fun electrical() = listOf(
        ChecklistItem(title = "All outlets and switches functional", order = 0),
        ChecklistItem(title = "Outlets are level and properly mounted", order = 1),
        ChecklistItem(title = "Circuit breaker correctly labeled", order = 2),
        ChecklistItem(title = "No exposed wiring", order = 3),
        ChecklistItem(title = "Ground fault protection in wet areas", order = 4),
        ChecklistItem(title = "Lighting fixtures fully installed", order = 5),
        ChecklistItem(title = "No flickering or buzzing", order = 6)
    )

    fun plumbing() = listOf(
        ChecklistItem(title = "No leaks under sinks or at connections", order = 0),
        ChecklistItem(title = "Hot and cold water correctly labeled", order = 1),
        ChecklistItem(title = "Drain flows without blockage", order = 2),
        ChecklistItem(title = "Fixtures properly sealed to wall/floor", order = 3),
        ChecklistItem(title = "Water pressure adequate", order = 4),
        ChecklistItem(title = "No exposed pipes where not intended", order = 5),
        ChecklistItem(title = "Toilet flushes properly", order = 6)
    )
}
