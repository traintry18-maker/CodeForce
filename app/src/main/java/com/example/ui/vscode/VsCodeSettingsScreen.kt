package com.example.ui.vscode

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.syntax.EditorThemeMode
import com.example.ui.theme.*

@Composable
fun VsCodeSettingsDialog(
    currentSettings: EditorSettings,
    currentTheme: EditorThemeMode,
    onSaveSettings: (EditorSettings) -> Unit,
    onThemeSelect: (EditorThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Text Editor") }

    var fontFamily by remember { mutableStateOf(currentSettings.fontFamily) }
    var fontSize by remember { mutableStateOf(currentSettings.fontSize) }
    var tabSize by remember { mutableStateOf(currentSettings.tabSize) }
    var wordWrap by remember { mutableStateOf(currentSettings.wordWrap) }
    var lineNumbers by remember { mutableStateOf(currentSettings.lineNumbers) }
    var minimap by remember { mutableStateOf(currentSettings.minimap) }
    var bracketPairs by remember { mutableStateOf(currentSettings.bracketPairColorization) }
    var autoSave by remember { mutableStateOf(currentSettings.autoSave) }
    var cursorBlinking by remember { mutableStateOf(currentSettings.cursorBlinking) }
    var statusBarVisible by remember { mutableStateOf(currentSettings.statusBarVisible) }
    var breadcrumbsVisible by remember { mutableStateOf(currentSettings.breadcrumbsVisible) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = VsCodeEditorBg,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Settings Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(VsCodeTabBg)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Settings — User Preferences",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // Settings Search Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search settings (e.g. font, wrap, tab, theme)...", color = Color(0xFF6E7681), fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = VsCodeInputBg,
                            unfocusedContainerColor = VsCodeInputBg,
                            focusedBorderColor = VsCodeInputFocusBorder,
                            unfocusedBorderColor = VsCodeInputBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().height(46.dp).testTag("settings_search_input")
                    )
                }

                // Settings Navigation Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(VsCodeSidebarBg)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("Text Editor", "Workbench", "AI Copilot", "Git & Deploy").forEach { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            color = if (isSelected) VsCodeEditorBg else Color.Transparent,
                            modifier = Modifier
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) Color.White else Color(0xFF969696),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                // Settings Content
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (selectedCategory) {
                        "Text Editor" -> {
                            item {
                                SettingItem(
                                    title = "Editor: Font Size",
                                    description = "Controls the font size in pixels."
                                ) {
                                    Slider(
                                        value = fontSize,
                                        onValueChange = { fontSize = it },
                                        valueRange = 10f..24f,
                                        steps = 7,
                                        modifier = Modifier.width(200.dp)
                                    )
                                    Text("${fontSize.toInt()} px", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                }
                            }

                            item {
                                SettingItem(
                                    title = "Editor: Tab Size",
                                    description = "The number of spaces a tab is equal to."
                                ) {
                                    Row {
                                        listOf(2, 4).forEach { size ->
                                            FilterChip(
                                                selected = tabSize == size,
                                                onClick = { tabSize = size },
                                                label = { Text("$size spaces", fontSize = 11.sp) },
                                                modifier = Modifier.padding(end = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                SettingItem(
                                    title = "Editor: Word Wrap",
                                    description = "Controls how lines should wrap in the editor."
                                ) {
                                    Switch(checked = wordWrap, onCheckedChange = { wordWrap = it })
                                }
                            }

                            item {
                                SettingItem(
                                    title = "Editor: Line Numbers",
                                    description = "Controls the display of line numbers in the gutter."
                                ) {
                                    Row {
                                        listOf("on", "off", "relative").forEach { opt ->
                                            FilterChip(
                                                selected = lineNumbers == opt,
                                                onClick = { lineNumbers = opt },
                                                label = { Text(opt, fontSize = 11.sp) },
                                                modifier = Modifier.padding(end = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                SettingItem(
                                    title = "Editor: Bracket Pair Colorization",
                                    description = "Controls whether bracket pair colorization is enabled."
                                ) {
                                    Switch(checked = bracketPairs, onCheckedChange = { bracketPairs = it })
                                }
                            }

                            item {
                                SettingItem(
                                    title = "Editor: Minimap",
                                    description = "Controls whether the minimap is shown on the right side."
                                ) {
                                    Switch(checked = minimap, onCheckedChange = { minimap = it })
                                }
                            }
                        }

                        "Workbench" -> {
                            item {
                                SettingItem(
                                    title = "Workbench: Color Theme",
                                    description = "Specifies the color theme used in the workbench."
                                ) {
                                    Column {
                                        EditorThemeMode.entries.forEach { mode ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { onThemeSelect(mode) }
                                                    .padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                RadioButton(
                                                    selected = currentTheme == mode,
                                                    onClick = { onThemeSelect(mode) }
                                                )
                                                Text(mode.displayName, color = Color.White, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                SettingItem(
                                    title = "Workbench: Status Bar Visible",
                                    description = "Controls the display of the status bar at the bottom."
                                ) {
                                    Switch(checked = statusBarVisible, onCheckedChange = { statusBarVisible = it })
                                }
                            }

                            item {
                                SettingItem(
                                    title = "Breadcrumbs: Enabled",
                                    description = "Enable/disable navigation breadcrumbs above the editor."
                                ) {
                                    Switch(checked = breadcrumbsVisible, onCheckedChange = { breadcrumbsVisible = it })
                                }
                            }
                        }

                        "AI Copilot" -> {
                            item {
                                SettingItem(
                                    title = "AI Model: Gemini 3.5 Flash",
                                    description = "High-speed reasoning model for real-time code generation and analysis."
                                ) {
                                    Surface(
                                        color = VsCodeRemoteBadge,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            "ACTIVE & CONNECTED",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            item {
                                SettingItem(
                                    title = "Copilot: Inline Suggestions",
                                    description = "Automatically trigger smart code completions while typing."
                                ) {
                                    Switch(checked = true, onCheckedChange = {})
                                }
                            }
                        }

                        "Git & Deploy" -> {
                            item {
                                SettingItem(
                                    title = "Git: Auto Push",
                                    description = "Automatically push commits to remote repository upon commit."
                                ) {
                                    Switch(checked = true, onCheckedChange = {})
                                }
                            }

                            item {
                                SettingItem(
                                    title = "Cloud: Default Deploy Target",
                                    description = "Preferred edge cloud platform for single-tap deployments."
                                ) {
                                    Text("Vercel Edge Global", color = VsCodeInfo, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Footer Save / Close Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(VsCodeTabBg)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color(0xFFCCCCCC))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val updated = currentSettings.copy(
                                fontFamily = fontFamily,
                                fontSize = fontSize,
                                tabSize = tabSize,
                                wordWrap = wordWrap,
                                lineNumbers = lineNumbers,
                                minimap = minimap,
                                bracketPairColorization = bracketPairs,
                                autoSave = autoSave,
                                cursorBlinking = cursorBlinking,
                                statusBarVisible = statusBarVisible,
                                breadcrumbsVisible = breadcrumbsVisible
                            )
                            onSaveSettings(updated)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VsCodeTabActiveBorder),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Save & Apply")
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingItem(
    title: String,
    description: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(description, color = Color(0xFF969696), fontSize = 11.sp)
        Spacer(modifier = Modifier.height(6.dp))
        content()
        HorizontalDivider(color = Color(0xFF2B2B2B), modifier = Modifier.padding(top = 10.dp))
    }
}
