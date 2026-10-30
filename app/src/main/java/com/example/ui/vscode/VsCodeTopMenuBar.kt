package com.example.ui.vscode

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ViewSidebar
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun VsCodeTopMenuBar(
    workspaceName: String,
    isLeftSidebarOpen: Boolean,
    isPanelOpen: Boolean,
    isRightChatOpen: Boolean,
    isSignedIn: Boolean,
    onToggleLeftSidebar: () -> Unit,
    onTogglePanel: () -> Unit,
    onToggleRightChat: () -> Unit,
    onOpenCommandPalette: () -> Unit,
    onOpenQuickOpen: () -> Unit,
    onOpenSettings: () -> Unit,
    onNewFile: () -> Unit,
    onSaveFile: () -> Unit,
    onSaveAll: () -> Unit,
    onCloseActiveFile: () -> Unit,
    onRunFile: () -> Unit,
    onStopDebugging: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onCut: () -> Unit,
    onCopy: () -> Unit,
    onPaste: () -> Unit,
    onToggleFind: () -> Unit,
    onToggleReplace: () -> Unit,
    onSelectAll: () -> Unit,
    onGoBack: () -> Unit,
    onGoForward: () -> Unit,
    onOpenBottomTab: (VsCodeBottomTab) -> Unit,
    onOpenDeployDashboard: () -> Unit = {},
    onSignInClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeMenu by remember { mutableStateOf<String?>(null) }

    val menus = remember(isSignedIn) {
        mapOf(
            "File" to listOf(
                MenuItemDef("New Text File", "Ctrl+N") { onNewFile() },
                MenuItemDef("New File...", "Ctrl+Alt+Win+N") { onNewFile() },
                MenuItemDef("New Window", "Ctrl+Shift+N") { /* new window */ },
                MenuItemDef("New Window with Profile", hasSubmenu = true) { /* profile */ },
                MenuItemDef("", isDivider = true),
                MenuItemDef("Open File...", "Ctrl+O") { onOpenQuickOpen() },
                MenuItemDef("Open Folder...", "Ctrl+K Ctrl+O") { onOpenCommandPalette() },
                MenuItemDef("Open Workspace from File...") { onOpenCommandPalette() },
                MenuItemDef("Open Recent", hasSubmenu = true) { onOpenQuickOpen() },
                MenuItemDef("", isDivider = true),
                MenuItemDef("Add Folder to Workspace...") { onOpenCommandPalette() },
                MenuItemDef("Save Workspace As...") { onSaveAll() },
                MenuItemDef("Duplicate Workspace") { /* duplicate */ },
                MenuItemDef("", isDivider = true),
                MenuItemDef("Save", "Ctrl+S") { onSaveFile() },
                MenuItemDef("Save As...", "Ctrl+Shift+S") { onSaveFile() },
                MenuItemDef("Save All", "Ctrl+K S") { onSaveAll() },
                MenuItemDef("", isDivider = true),
                MenuItemDef("Share", hasSubmenu = true) { /* share */ },
                MenuItemDef("", isDivider = true),
                MenuItemDef("Auto Save", isChecked = true) { /* auto save */ },
                MenuItemDef("Preferences", hasSubmenu = true) { onOpenSettings() },
                MenuItemDef("", isDivider = true),
                MenuItemDef("Revert File") { /* revert */ },
                MenuItemDef("Close Editor", "Ctrl+F4") { onCloseActiveFile() },
                MenuItemDef("Close Folder", "Ctrl+K F") { onCloseActiveFile() },
                MenuItemDef("Close Window", "Alt+F4") { /* close window */ },
                MenuItemDef("", isDivider = true),
                MenuItemDef("Exit") { /* exit */ }
            ),
            "Edit" to listOf(
                MenuItemDef("Undo", "Ctrl+Z") { onUndo() },
                MenuItemDef("Redo", "Ctrl+Y") { onRedo() },
                MenuItemDef("", isDivider = true),
                MenuItemDef("Cut", "Ctrl+X") { onCut() },
                MenuItemDef("Copy", "Ctrl+C") { onCopy() },
                MenuItemDef("Paste", "Ctrl+V") { onPaste() },
                MenuItemDef("", isDivider = true),
                MenuItemDef("Find", "Ctrl+F") { onToggleFind() },
                MenuItemDef("Replace", "Ctrl+H") { onToggleReplace() }
            ),
            "Selection" to listOf(
                MenuItemDef("Select All", "Ctrl+A") { onSelectAll() },
                MenuItemDef("Copy Selection", "Ctrl+C") { onCopy() },
                MenuItemDef("Cut Selection", "Ctrl+X") { onCut() }
            ),
            "View" to listOf(
                MenuItemDef("Command Palette...", "Ctrl+Shift+P") { onOpenCommandPalette() },
                MenuItemDef("Quick Open (Go to File)...", "Ctrl+P") { onOpenQuickOpen() },
                MenuItemDef("", isDivider = true),
                MenuItemDef("Primary Side Bar", "Ctrl+B") { onToggleLeftSidebar() },
                MenuItemDef("Secondary Side Bar (Copilot Chat)", "Ctrl+Alt+I") { onToggleRightChat() },
                MenuItemDef("Panel (Terminal)", "Ctrl+J") { onTogglePanel() },
                MenuItemDef("", isDivider = true),
                MenuItemDef("Output", "Ctrl+K Ctrl+H") { onOpenBottomTab(VsCodeBottomTab.OUTPUT) },
                MenuItemDef("Problems", "Ctrl+Shift+M") { onOpenBottomTab(VsCodeBottomTab.PROBLEMS) },
                MenuItemDef("Terminal", "Ctrl+`") { onOpenBottomTab(VsCodeBottomTab.TERMINAL) },
                MenuItemDef("Web Preview", "Ctrl+P") { onOpenBottomTab(VsCodeBottomTab.WEB_PREVIEW) },
                MenuItemDef("", isDivider = true),
                MenuItemDef("Cloud Deployment Dashboard", "Ctrl+Shift+D") { onOpenDeployDashboard() }
            ),
            "Go" to listOf(
                MenuItemDef("Back", "Alt+Left") { onGoBack() },
                MenuItemDef("Forward", "Alt+Right") { onGoForward() },
                MenuItemDef("", isDivider = true),
                MenuItemDef("Go to File...", "Ctrl+P") { onOpenQuickOpen() },
                MenuItemDef("Go to Line/Column...", "Ctrl+G") { onToggleFind() }
            ),
            "Run" to listOf(
                MenuItemDef("Start Debugging", "F5") { onRunFile() },
                MenuItemDef("Run Without Debugging", "Ctrl+F5") { onRunFile() },
                MenuItemDef("Stop Debugging", "Shift+F5") { onStopDebugging() },
                MenuItemDef("", isDivider = true),
                MenuItemDef("Push & Deploy to Cloud...", "Ctrl+Shift+U") { onOpenDeployDashboard() }
            ),
            "..." to listOf(
                MenuItemDef("Build with Agent (Chat)", "Ctrl+Alt+I") { onToggleRightChat() },
                MenuItemDef("Cloud Deploy Dashboard", "Ctrl+Shift+D") { onOpenDeployDashboard() },
                MenuItemDef("Settings", "Ctrl+,") { onOpenSettings() },
                MenuItemDef("Color Theme", "Ctrl+K Ctrl+T") { onOpenSettings() }
            )
        )
    }

    Surface(
        color = Color(0xFF181818),
        modifier = modifier
            .fillMaxWidth()
            .height(35.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Blue VS Code Logo + Menu Items (File, Edit, Selection, View, Go, Run, ...)
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Blue VS Code Ribbon Logo
                VsCodeMiniLogo(modifier = Modifier.size(16.dp).clickable { onOpenCommandPalette() })

                Spacer(modifier = Modifier.width(8.dp))

                // Standard Menu Bar Items
                menus.keys.forEach { menuKey ->
                    Box {
                        Text(
                            text = menuKey,
                            color = if (activeMenu == menuKey) Color.White else Color(0xFFCCCCCC),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.SansSerif,
                            modifier = Modifier
                                .clickable {
                                    activeMenu = if (activeMenu == menuKey) null else menuKey
                                }
                                .padding(horizontal = 5.dp, vertical = 4.dp)
                                .testTag("top_menu_${menuKey.lowercase().replace("...", "more")}")
                        )

                        // Dropdown Menu
                        DropdownMenu(
                            expanded = activeMenu == menuKey,
                            onDismissRequest = { activeMenu = null },
                            modifier = Modifier
                                .background(Color(0xFF252526))
                                .widthIn(min = 230.dp)
                        ) {
                            menus[menuKey]?.forEach { item ->
                                if (item.isDivider) {
                                    HorizontalDivider(
                                        color = Color(0xFF333333),
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                } else {
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    if (item.isChecked == true) {
                                                        Text(
                                                            text = "✓ ",
                                                            fontSize = 12.sp,
                                                            color = Color(0xFF38BDF8),
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                    Text(
                                                        text = item.label,
                                                        fontSize = 12.sp,
                                                        color = Color(0xFFCCCCCC)
                                                    )
                                                }
                                                if (item.shortcut.isNotEmpty()) {
                                                    Text(
                                                        text = item.shortcut,
                                                        fontSize = 10.sp,
                                                        color = Color(0xFF858585),
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                } else if (item.hasSubmenu) {
                                                    Text(
                                                        text = "›",
                                                        fontSize = 14.sp,
                                                        color = Color(0xFF858585),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            activeMenu = null
                                            item.onClick()
                                        },
                                        modifier = Modifier.height(28.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Center: Back/Forward Navigation & Search / Breadcrumb Bar
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back & Forward Navigation buttons
                IconButton(
                    onClick = onGoBack,
                    modifier = Modifier.size(20.dp).testTag("top_nav_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Go Back",
                        tint = Color(0xFF858585),
                        modifier = Modifier.size(14.dp)
                    )
                }

                IconButton(
                    onClick = onGoForward,
                    modifier = Modifier.size(20.dp).testTag("top_nav_forward")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Go Forward",
                        tint = Color(0xFF858585),
                        modifier = Modifier.size(14.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Address & Search Pill: d: [Administrator] (or workspace)
                Surface(
                    color = Color(0xFF2B2B2B),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF383838)),
                    modifier = Modifier
                        .widthIn(min = 180.dp, max = 340.dp)
                        .height(24.dp)
                        .clickable { onOpenQuickOpen() }
                        .testTag("top_search_address_bar")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "d: [$workspaceName]",
                            color = Color(0xFFCCCCCC),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Quick Open",
                            tint = Color(0xFF858585),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            // Right: Copilot Chat, Sign In Button, Layout Toggles & Window Controls
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Copilot Chat Sparkle Icon
                IconButton(
                    onClick = onToggleRightChat,
                    modifier = Modifier.size(24.dp).testTag("top_copilot_chat_icon")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Copilot Chat",
                        tint = Color(0xFF75BEFF),
                        modifier = Modifier.size(15.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Sign In / Account Button
                Button(
                    onClick = onSignInClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSignedIn) Color(0xFF1E1E1E) else Color(0xFF0078D4)
                    ),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    border = if (isSignedIn) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF383838)) else null,
                    modifier = Modifier.height(22.dp).testTag("sign_in_button")
                ) {
                    Text(
                        text = if (isSignedIn) "✓ Signed In" else "Sign In",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Layout control icons: Toggle Primary Side Bar (left)
                IconButton(
                    onClick = onToggleLeftSidebar,
                    modifier = Modifier.size(22.dp).testTag("layout_toggle_primary_sidebar")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ViewSidebar,
                        contentDescription = "Toggle Primary Side Bar",
                        tint = if (isLeftSidebarOpen) Color.White else Color(0xFF858585),
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Toggle Panel (bottom)
                IconButton(
                    onClick = onTogglePanel,
                    modifier = Modifier.size(22.dp).testTag("layout_toggle_panel")
                ) {
                    Icon(
                        imageVector = Icons.Default.VerticalAlignBottom,
                        contentDescription = "Toggle Panel",
                        tint = if (isPanelOpen) Color.White else Color(0xFF858585),
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Toggle Secondary Side Bar (Right Copilot Chat)
                IconButton(
                    onClick = onToggleRightChat,
                    modifier = Modifier.size(22.dp).testTag("layout_toggle_secondary_sidebar")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ViewSidebar,
                        contentDescription = "Toggle Secondary Side Bar",
                        tint = if (isRightChatOpen) Color(0xFF0078D4) else Color(0xFF858585),
                        modifier = Modifier.size(14.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Window control buttons (—, 🗖, ✕)
                IconButton(
                    onClick = onTogglePanel,
                    modifier = Modifier.size(20.dp)
                ) {
                    Text(
                        text = "—",
                        color = Color(0xFF858585),
                        fontSize = 11.sp
                    )
                }
                IconButton(
                    onClick = onToggleLeftSidebar,
                    modifier = Modifier.size(20.dp)
                ) {
                    Text(
                        text = "🗖",
                        color = Color(0xFF858585),
                        fontSize = 10.sp
                    )
                }
                IconButton(
                    onClick = onCloseActiveFile,
                    modifier = Modifier.size(20.dp)
                ) {
                    Text(
                        text = "✕",
                        color = Color(0xFF858585),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun VsCodeMiniLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val blue1 = Color(0xFF007ACC)
        val blue2 = Color(0xFF1F8AD2)

        val path = Path().apply {
            moveTo(w * 0.72f, h * 0.1f)
            lineTo(w * 0.95f, h * 0.25f)
            lineTo(w * 0.95f, h * 0.75f)
            lineTo(w * 0.72f, h * 0.9f)
            close()
        }
        drawPath(path, blue1)

        val path2 = Path().apply {
            moveTo(w * 0.72f, h * 0.1f)
            lineTo(w * 0.28f, h * 0.5f)
            lineTo(w * 0.1f, h * 0.35f)
            lineTo(w * 0.05f, h * 0.42f)
            lineTo(w * 0.2f, h * 0.52f)
            lineTo(w * 0.05f, h * 0.62f)
            lineTo(w * 0.1f, h * 0.7f)
            lineTo(w * 0.28f, h * 0.55f)
            lineTo(w * 0.72f, h * 0.9f)
            close()
        }
        drawPath(path2, blue2)
    }
}
