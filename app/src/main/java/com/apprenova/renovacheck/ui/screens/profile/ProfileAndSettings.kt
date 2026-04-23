package com.apprenova.renovacheck.ui.screens.profile

import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.core.net.toUri
import com.apprenova.renovacheck.data.model.*
import com.apprenova.renovacheck.ui.components.*
import com.apprenova.renovacheck.ui.theme.*
import com.apprenova.renovacheck.viewmodel.RenovaViewModel

// ── Profile Screen ────────────────────────────────────────────────────────────
@Composable
fun ProfileScreen(
    viewModel: RenovaViewModel,
    onBack: () -> Unit
) {
    var profile by remember { mutableStateOf(viewModel.userProfile) }
    var isEditing by remember { mutableStateOf(false) }
    var name    by remember { mutableStateOf(profile.name) }
    var email   by remember { mutableStateOf(profile.email) }
    var phone   by remember { mutableStateOf(profile.phone) }
    var role    by remember { mutableStateOf(profile.role) }
    var company by remember { mutableStateOf(profile.company) }

    Scaffold(
        containerColor = RenovaBackground,
        topBar = {
            RenovaTopBar("Profile", onBack = onBack, actions = {
                TextButton(onClick = {
                    if (isEditing) {
                        val updated = profile.copy(name = name, email = email,
                            phone = phone, role = role, company = company)
                        viewModel.saveProfile(updated)
                        profile = updated
                    }
                    isEditing = !isEditing
                }) {
                    Text(if (isEditing) "Save" else "Edit",
                        color = RenovaPrimary, fontWeight = FontWeight.SemiBold)
                }
            })
        }
    ) { pv ->
        Column(
            modifier = Modifier.fillMaxSize().padding(pv)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(Brush.linearGradient(listOf(RenovaGradientStart, RenovaGradientEnd)))
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier.size(88.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                                .border(3.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (if (name.isNotEmpty()) name else "?").first().uppercase(),
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            name.ifEmpty { "Inspector" },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            role.ifEmpty { "Inspector" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        if (company.isNotEmpty()) {
                            Text(company, style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                }
            }

            // Editable fields
            Text("PERSONAL INFO", style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold, letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp))

            if (isEditing) {
                RenovaTextField(name, { name = it }, "Full Name",
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = RenovaPrimary) })
                RenovaTextField(email, { email = it }, "Email",
                    leadingIcon = { Icon(Icons.Default.Email, null, tint = RenovaPrimary) })
                RenovaTextField(phone, { phone = it }, "Phone",
                    leadingIcon = { Icon(Icons.Default.Phone, null, tint = RenovaPrimary) })
                RenovaTextField(role, { role = it }, "Role",
                    leadingIcon = { Icon(Icons.Default.Badge, null, tint = RenovaPrimary) })
                RenovaTextField(company, { company = it }, "Company",
                    leadingIcon = { Icon(Icons.Default.Business, null, tint = RenovaPrimary) })
            } else {
                ProfileInfoCard(profile)
            }

            // Stats
            val projects by viewModel.projects.collectAsState()
            val issues by viewModel.issues.collectAsState()

            Text("YOUR STATS", style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold, letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MiniStatCard("${projects.size}", "Projects", RenovaPrimary, Modifier.weight(1f))
                MiniStatCard("${issues.size}", "Issues\nFound", RenovaGold, Modifier.weight(1f))
                MiniStatCard("${issues.count { it.isResolved }}", "Issues\nResolved", RenovaSuccess, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ProfileInfoCard(profile: UserProfile) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ProfileRow(Icons.Default.Person, "Name", profile.name.ifEmpty { "—" })
            HorizontalDivider(color = RenovaDivider, thickness = 0.5.dp)
            ProfileRow(Icons.Default.Email, "Email", profile.email.ifEmpty { "—" })
            HorizontalDivider(color = RenovaDivider, thickness = 0.5.dp)
            ProfileRow(Icons.Default.Phone, "Phone", profile.phone.ifEmpty { "—" })
            HorizontalDivider(color = RenovaDivider, thickness = 0.5.dp)
            ProfileRow(Icons.Default.Badge, "Role", profile.role.ifEmpty { "Inspector" })
            HorizontalDivider(color = RenovaDivider, thickness = 0.5.dp)
            ProfileRow(Icons.Default.Business, "Company", profile.company.ifEmpty { "—" })
        }
    }
}

@Composable
private fun ProfileRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = RenovaPrimary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun MiniStatCard(value: String, label: String, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

// ── Settings Screen ───────────────────────────────────────────────────────────
@Composable
fun SettingsScreen(
    viewModel: RenovaViewModel,
    onBack: () -> Unit
) {
    var settings by remember { mutableStateOf(viewModel.appSettings) }
    val context = LocalContext.current

    fun save(updated: AppSettings) {
        settings = updated
        viewModel.saveSettings(updated)
    }

    Scaffold(
        containerColor = RenovaBackground,
        topBar = { RenovaTopBar("Settings", onBack = onBack) }
    ) { pv ->
        Column(
            modifier = Modifier.fillMaxSize().padding(pv)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SettingsGroupHeader("GENERAL")

            SettingsSwitchRow(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                subtitle = "Receive inspection reminders",
                checked = settings.notificationsEnabled,
                onCheckedChange = { save(settings.copy(notificationsEnabled = it)) }
            )

            SettingsSwitchRow(
                icon = Icons.Default.DarkMode,
                title = "Dark Mode",
                subtitle = "Switch to dark theme",
                checked = settings.darkMode,
                onCheckedChange = { save(settings.copy(darkMode = it)) }
            )

            SettingsSwitchRow(
                icon = Icons.Default.Backup,
                title = "Auto Backup",
                subtitle = "Automatically backup data",
                checked = settings.autoBackup,
                onCheckedChange = { save(settings.copy(autoBackup = it)) }
            )

            Spacer(Modifier.height(4.dp))
            SettingsGroupHeader("PREFERENCES")

            SettingsOptionRow(
                icon = Icons.Default.Straighten,
                title = "Units",
                currentValue = if (settings.units == "metric") "Metric (m²)" else "Imperial (ft²)",
                onClick = {
                    save(settings.copy(units = if (settings.units == "metric") "imperial" else "metric"))
                }
            )

            SettingsOptionRow(
                icon = Icons.Default.PictureAsPdf,
                title = "Report Format",
                currentValue = settings.reportFormat,
                onClick = {
                    save(settings.copy(reportFormat = if (settings.reportFormat == "PDF") "DOCX" else "PDF"))
                }
            )

            Spacer(Modifier.height(4.dp))
            SettingsGroupHeader("APP INFO")

            SettingsInfoRow(Icons.Default.Info, "Version", "1.0.0")
            SettingsInfoRow(Icons.Default.Code, "Build", "Production")
            SettingsInfoRow(Icons.Default.QuestionMark, "Privacy Policy", "") {
                context.startActivity(Intent(Intent.ACTION_VIEW, "https://renovacheckk.com/privacy-policy.html".toUri()))
            }

            Spacer(Modifier.height(16.dp))

            // App version footer
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Renova Check", style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold, color = RenovaPrimary)
                Text("v1.0.0",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SettingsGroupHeader(text: String) {
    Text(text, style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold, letterSpacing = 1.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp))
}

@Composable
private fun SettingsSwitchRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(40.dp).background(RenovaPrimary.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = RenovaPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = checked, onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = RenovaPrimary)
            )
        }
    }
}

@Composable
private fun SettingsOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    currentValue: String,
    onClick: () -> Unit
) {
    Card(shape = RoundedCornerShape(14.dp),
        modifier = Modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).background(RenovaPrimary.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = RenovaPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text(title, style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Text(currentValue, style = MaterialTheme.typography.labelMedium,
                color = RenovaPrimary, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun SettingsInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    onClick: (() -> Unit)? = null
) {
    Card(shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.clickable(enabled = onClick != null, onClick = {
            onClick?.invoke()
        })) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).background(RenovaPrimary.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = RenovaPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Text(value, style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (onClick != null) {
                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp))
            }
        }
    }
}
