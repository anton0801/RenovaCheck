package com.apprenova.renovacheck.ui.screens.photos

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import com.apprenova.renovacheck.data.model.*
import com.apprenova.renovacheck.ui.components.*
import com.apprenova.renovacheck.ui.theme.*
import com.apprenova.renovacheck.viewmodel.RenovaViewModel
import java.text.SimpleDateFormat
import java.util.*

// ── Photos Gallery ────────────────────────────────────────────────────────────
@Composable
fun PhotosScreen(
    projectId: String,
    viewModel: RenovaViewModel,
    onBack: () -> Unit,
    onBeforeAfter: () -> Unit
) {
    val allPhotos by viewModel.photos.collectAsState()
    val photos = remember(allPhotos) { allPhotos.filter { it.projectId == projectId } }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("All", "Before", "After", "Defects")

    val filtered = when (selectedTab) {
        1 -> photos.filter { it.isBefore }
        2 -> photos.filter { !it.isBefore }
        3 -> photos.filter { it.type == "defect" }
        else -> photos
    }

    Scaffold(
        containerColor = RenovaBackground,
        topBar = {
            RenovaTopBar("Photos", "${photos.size} photos", onBack = onBack, actions = {
                IconButton(onClick = onBeforeAfter) {
                    Icon(Icons.Default.CompareArrows, contentDescription = "Before/After",
                        tint = MaterialTheme.colorScheme.onSurface)
                }
            })
        }
    ) { pv ->
        Column(modifier = Modifier.fillMaxSize().padding(pv)) {
            // Tab row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = RenovaPrimary,
                edgePadding = 16.dp
            ) {
                tabs.forEachIndexed { i, title ->
                    Tab(
                        selected = selectedTab == i,
                        onClick = { selectedTab = i },
                        text = { Text(title, style = MaterialTheme.typography.labelLarge) }
                    )
                }
            }

            if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState("📸", "No Photos Yet",
                        "Photos attached to issues will appear here.")
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filtered, key = { it.id }) { photo ->
                        PhotoThumbnail(photo)
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoThumbnail(photo: Photo) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        if (photo.uri.isNotEmpty()) {
            AsyncImage(
                model = photo.uri,
                contentDescription = photo.caption,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Image, null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp))
            }
        }
        // Badge
        if (photo.type == "defect") {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(16.dp)
                    .background(RenovaRed, CircleShape)
            )
        }
        // Before/After tag
        Surface(
            modifier = Modifier.align(Alignment.BottomStart).padding(4.dp),
            shape = RoundedCornerShape(4.dp),
            color = Color.Black.copy(alpha = 0.55f)
        ) {
            Text(
                if (photo.isBefore) "B" else "A",
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ── Before / After Comparison ─────────────────────────────────────────────────
@Composable
fun BeforeAfterScreen(
    projectId: String,
    viewModel: RenovaViewModel,
    onBack: () -> Unit
) {
    val allPhotos by viewModel.photos.collectAsState()
    val before = remember(allPhotos) { allPhotos.filter { it.projectId == projectId && it.isBefore } }
    val after  = remember(allPhotos) { allPhotos.filter { it.projectId == projectId && !it.isBefore } }

    Scaffold(
        containerColor = RenovaBackground,
        topBar = { RenovaTopBar("Before / After", onBack = onBack) }
    ) { pv ->
        Column(modifier = Modifier.fillMaxSize().padding(pv).padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PhotoColumn("BEFORE", before, RenovaGold, Modifier.weight(1f))
                PhotoColumn("AFTER", after, RenovaSuccess, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PhotoColumn(label: String, photos: List<Photo>, color: Color, modifier: Modifier) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold, color = color)
            Text("${photos.size}", style = MaterialTheme.typography.labelMedium, color = color)
        }
        Spacer(Modifier.height(8.dp))
        if (photos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(160.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("No photos", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(photos.take(6)) { photo ->
                    Box(
                        modifier = Modifier.fillMaxWidth().aspectRatio(1.2f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        if (photo.uri.isNotEmpty()) {
                            AsyncImage(model = photo.uri, contentDescription = null,
                                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        } else {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Image, null,
                                    modifier = Modifier.size(28.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}
