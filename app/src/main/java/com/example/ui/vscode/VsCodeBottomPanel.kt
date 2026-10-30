package com.example.ui.vscode

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CodeFileEntity
import com.example.data.model.ProjectEntity
import com.example.execution.TerminalLine
import com.example.ui.preview.WebPreviewScreen
import com.example.ui.terminal.TerminalScreen
import com.example.ui.theme.*

@Composable
fun VsCodeBottomPanel(
    activeTab: VsCodeBottomTab,
    isMaximized: Boolean,
    project: ProjectEntity?,
    files: List<CodeFileEntity>,
    terminalLines: List<TerminalLine>,
    terminalInput: String,
    onTabSelect: (VsCodeBottomTab) -> Unit,
    onToggleMaximize: () -> Unit,
    onClose: () -> Unit,
    onTerminalInputChange: (String) -> Unit,
    onTerminalSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(VsCodePanelBg)
    ) {
        // Panel Header Tabs Row
        Surface(
            color = VsCodePanelHeaderBg,
            modifier = Modifier
                .fillMaxWidth()
                .height(35.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Panel Tabs
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VsCodeBottomTab.entries.forEach { tab ->
                        val isSelected = activeTab == tab
                        val textColor = if (isSelected) VsCodePanelTabActive else VsCodePanelTabInactive

                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .clickable { onTabSelect(tab) }
                                .padding(horizontal = 10.dp)
                                .testTag("panel_tab_${tab.name}"),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = tab.title,
                                color = textColor,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .height(1.5.dp)
                                        .width(36.dp)
                                        .background(VsCodeTabActiveBorder)
                                )
                            } else {
                                Spacer(modifier = Modifier.height(1.5.dp))
                            }
                        }
                    }
                }

                // Panel Window Controls (Maximize & Close)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleMaximize,
                        modifier = Modifier.size(24.dp).testTag("panel_maximize_button")
                    ) {
                        Icon(
                            imageVector = if (isMaximized) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = "Maximize Panel",
                            tint = VsCodePanelTabInactive,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(24.dp).testTag("panel_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Panel",
                            tint = VsCodePanelTabInactive,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Panel Border Line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFF2B2B2B))
        )

        // Panel Body Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (activeTab) {
                VsCodeBottomTab.TERMINAL -> {
                    TerminalScreen(
                        lines = terminalLines,
                        input = terminalInput,
                        project = project,
                        onInputChange = onTerminalInputChange,
                        onSubmitCommand = onTerminalSubmit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                VsCodeBottomTab.WEB_PREVIEW -> {
                    WebPreviewScreen(
                        files = files,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                VsCodeBottomTab.PROBLEMS -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircleOutline,
                            contentDescription = null,
                            tint = VsCodeSuccess,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No problems have been detected in the workspace.",
                            color = VsCodePanelTabInactive,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                VsCodeBottomTab.OUTPUT -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "[CodeForge Task Runner] Multi-language build daemon active.",
                            color = VsCodeInfo,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "[Info] Compiler target: Release container x86_64-linux.",
                            color = VsCodePanelTabActive,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "[Git] Watching working tree for file changes.",
                            color = VsCodeSuccess,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                VsCodeBottomTab.DEBUG_CONSOLE -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "CodeForge Debugger attached. Ready for evaluation.",
                            color = VsCodeSuccess,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "> All runtime symbols and breakpoints active.",
                            color = VsCodePanelTabInactive,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
