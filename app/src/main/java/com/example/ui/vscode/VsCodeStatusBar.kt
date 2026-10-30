package com.example.ui.vscode

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SupportedLanguage

@Composable
fun VsCodeStatusBar(
    branchName: String,
    cursorLine: Int,
    cursorCol: Int,
    language: SupportedLanguage,
    tabSize: Int,
    isPanelOpen: Boolean,
    onTogglePanel: () -> Unit,
    onBranchClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onCopilotClick: () -> Unit,
    onSyncClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF181818),
        modifier = modifier
            .fillMaxWidth()
            .height(22.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Status Items: >< and ⨂ 0 ⚠ 0 (matching user picture)
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Remote >< icon
                Text(
                    text = "><",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .clickable { onSyncClick() }
                        .padding(horizontal = 4.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Errors and Warnings ⨂ 0  ⚠ 0
                Row(
                    modifier = Modifier
                        .clickable { onTogglePanel() }
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⨂ 0  ⚠ 0",
                        color = Color(0xFFCCCCCC),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Git branch
                Text(
                    text = branchName,
                    color = Color(0xFF858585),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.clickable { onBranchClick() }
                )
            }

            // Right Status Items: Ln/Col, Spaces, UTF-8, Copilot, Bell (matching user picture)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Ln $cursorLine, Col $cursorCol",
                    color = Color(0xFF858585),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Text(
                    text = "Spaces: $tabSize",
                    color = Color(0xFF858585),
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Text(
                    text = language.displayName,
                    color = Color(0xFF858585),
                    fontSize = 10.sp,
                    modifier = Modifier
                        .clickable { onLanguageClick() }
                        .padding(horizontal = 4.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Copilot Icon 👥 (matching user picture bottom right)
                Icon(
                    imageVector = Icons.Default.PeopleOutline,
                    contentDescription = "Copilot Agent Status",
                    tint = Color(0xFF858585),
                    modifier = Modifier
                        .size(13.dp)
                        .clickable { onCopilotClick() }
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Notification Bell 🔔 (matching user picture bottom right)
                Icon(
                    imageVector = Icons.Default.NotificationsNone,
                    contentDescription = "Notifications",
                    tint = Color(0xFF858585),
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}
