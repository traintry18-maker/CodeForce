package com.example.ui.vscode

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun VsCodeActivityBar(
    activeActivity: VsCodeActivity,
    isSidebarOpen: Boolean,
    pendingChangesCount: Int,
    onSelectActivity: (VsCodeActivity) -> Unit,
    onOpenSettingsMenu: () -> Unit,
    onOpenCommandPalette: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Explorer, Search, Source Control, Run & Debug, Cloud Deploy, Extensions, Testing, Python
    val items = listOf(
        Triple(VsCodeActivity.EXPLORER, Icons.Default.FolderOpen, "Explorer"),
        Triple(VsCodeActivity.SEARCH, Icons.Default.Search, "Search"),
        Triple(VsCodeActivity.SOURCE_CONTROL, Icons.Default.ForkRight, "Source Control"),
        Triple(VsCodeActivity.RUN_AND_DEBUG, Icons.Default.BugReport, "Run and Debug"),
        Triple(VsCodeActivity.CLOUD_DEPLOY, Icons.Default.CloudUpload, "Cloud Deploy"),
        Triple(VsCodeActivity.EXTENSIONS, Icons.Default.Extension, "Extensions"),
        Triple(VsCodeActivity.TESTING, Icons.Default.Science, "Testing"),
        Triple(VsCodeActivity.PYTHON, Icons.Default.Terminal, "Python")
    )

    Surface(
        color = Color(0xFF181818),
        modifier = modifier
            .width(46.dp)
            .fillMaxHeight()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Primary Activity Items
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                items.forEach { (activity, icon, label) ->
                    val isSelected = activeActivity == activity && isSidebarOpen
                    val iconTint = if (isSelected) Color.White else Color(0xFF858585)

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .then(
                                if (isSelected) {
                                    Modifier.border(1.dp, Color(0xFF0078D4), RoundedCornerShape(6.dp))
                                } else {
                                    Modifier
                                }
                            )
                            .clickable { onSelectActivity(activity) }
                            .testTag("activity_${activity.name}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = iconTint,
                            modifier = Modifier.size(20.dp)
                        )

                        // Badge counter (e.g. for Git changes)
                        if (activity == VsCodeActivity.SOURCE_CONTROL && pendingChangesCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = 4.dp, bottom = 4.dp)
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0078D4)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$pendingChangesCount",
                                    color = Color.White,
                                    fontSize = 7.sp
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Profile Avatar & Settings Gear (matching user's picture)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                // Profile Avatar Icon
                IconButton(
                    onClick = onOpenSettingsMenu,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("activity_accounts")
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Accounts",
                        tint = Color(0xFF858585),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Settings Gear Icon (⚙️)
                IconButton(
                    onClick = { onSelectActivity(VsCodeActivity.SETTINGS) },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("activity_settings")
                ) {
                    val isSelected = activeActivity == VsCodeActivity.SETTINGS && isSidebarOpen
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = if (isSelected) Color.White else Color(0xFF858585),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
