package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.SupportedLanguage
import com.example.git.*
import com.example.ui.ai.AiAssistantDialog
import com.example.ui.deploy.CloudDeploymentScreen
import com.example.ui.editor.CodeEditorScreen
import com.example.ui.theme.*
import com.example.ui.vscode.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeForgeApp(
    viewModel: CodeForgeViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val selectedProjectId by viewModel.selectedProjectId.collectAsStateWithLifecycle()
    val selectedProject by viewModel.selectedProject.collectAsStateWithLifecycle()
    val projectFiles by viewModel.projectFiles.collectAsStateWithLifecycle()
    val activeFileId by viewModel.activeFileId.collectAsStateWithLifecycle()
    val activeFile by viewModel.activeFile.collectAsStateWithLifecycle()
    val openTabIds by viewModel.openTabIds.collectAsStateWithLifecycle()

    val currentCode by viewModel.currentCode.collectAsStateWithLifecycle()
    val editorTheme by viewModel.editorTheme.collectAsStateWithLifecycle()
    val fontSize by viewModel.fontSize.collectAsStateWithLifecycle()
    val wordWrap by viewModel.wordWrap.collectAsStateWithLifecycle()
    val showLineNumbers by viewModel.showLineNumbers.collectAsStateWithLifecycle()

    val isExecuting by viewModel.isExecuting.collectAsStateWithLifecycle()
    val executionResult by viewModel.executionResult.collectAsStateWithLifecycle()

    val terminalLines by viewModel.terminalLines.collectAsStateWithLifecycle()
    val terminalInput by viewModel.terminalInput.collectAsStateWithLifecycle()

    val deployments by viewModel.deployments.collectAsStateWithLifecycle()
    val isDeploying by viewModel.isDeploying.collectAsStateWithLifecycle()
    val deployProgressMessage by viewModel.deployProgressMessage.collectAsStateWithLifecycle()

    val gitCommits by viewModel.gitCommits.collectAsStateWithLifecycle()

    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    val aiResult by viewModel.aiResult.collectAsStateWithLifecycle()
    val showAiDialog by viewModel.showAiDialog.collectAsStateWithLifecycle()

    val isSearchVisible by viewModel.isSearchVisible.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val replaceQuery by viewModel.replaceQuery.collectAsStateWithLifecycle()

    // VS Code Layout States
    val activeActivity by viewModel.activeActivity.collectAsStateWithLifecycle()
    val isSidebarOpen by viewModel.isSidebarOpen.collectAsStateWithLifecycle()
    val activePanelTab by viewModel.activePanelTab.collectAsStateWithLifecycle()
    val isPanelOpen by viewModel.isPanelOpen.collectAsStateWithLifecycle()
    val isPanelMaximized by viewModel.isPanelMaximized.collectAsStateWithLifecycle()
    val isCommandPaletteOpen by viewModel.isCommandPaletteOpen.collectAsStateWithLifecycle()
    val isQuickOpenOpen by viewModel.isQuickOpenOpen.collectAsStateWithLifecycle()
    val commandPaletteQuery by viewModel.commandPaletteQuery.collectAsStateWithLifecycle()
    val breakpoints by viewModel.breakpoints.collectAsStateWithLifecycle()
    val crossFileSearchQuery by viewModel.crossFileSearchQuery.collectAsStateWithLifecycle()
    val crossFileSearchResults by viewModel.crossFileSearchResults.collectAsStateWithLifecycle()
    val extensions by viewModel.extensions.collectAsStateWithLifecycle()
    val editorSettings by viewModel.editorSettings.collectAsStateWithLifecycle()
    val cursorLine by viewModel.cursorLine.collectAsStateWithLifecycle()
    val cursorCol by viewModel.cursorCol.collectAsStateWithLifecycle()

    // Right Copilot Agent Chat Panel states
    val isRightChatSidebarOpen by viewModel.isRightChatSidebarOpen.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val chatInputText by viewModel.chatInputText.collectAsStateWithLifecycle()
    val selectedModel by viewModel.selectedModel.collectAsStateWithLifecycle()
    val isAgentMode by viewModel.isAgentMode.collectAsStateWithLifecycle()
    val isChatGenerating by viewModel.isChatGenerating.collectAsStateWithLifecycle()
    val isSignedIn by viewModel.isSignedIn.collectAsStateWithLifecycle()
    val showSignInDialog by viewModel.showSignInDialog.collectAsStateWithLifecycle()

    // Git & Version Control states
    val gitStatusMap by viewModel.gitStatusMap.collectAsStateWithLifecycle()
    val gitCurrentBranch by viewModel.gitCurrentBranch.collectAsStateWithLifecycle()
    val gitBranches by viewModel.gitBranches.collectAsStateWithLifecycle()
    val gitStagedChanges by viewModel.gitStagedChanges.collectAsStateWithLifecycle()
    val gitUnstagedChanges by viewModel.gitUnstagedChanges.collectAsStateWithLifecycle()
    val gitPendingChangesCount by viewModel.gitPendingChangesCount.collectAsStateWithLifecycle()
    val diffFileChange by viewModel.diffFileChange.collectAsStateWithLifecycle()
    val currentDiffLines by viewModel.currentDiffLines.collectAsStateWithLifecycle()

    // Cloud Deployment states
    val providerConfigs by viewModel.providerConfigs.collectAsStateWithLifecycle()
    val selectedDeployProvider by viewModel.selectedDeployProvider.collectAsStateWithLifecycle()
    val pipelineStages by viewModel.pipelineStages.collectAsStateWithLifecycle()
    val deployLiveLogs by viewModel.deployLiveLogs.collectAsStateWithLifecycle()

    var showLanguagePicker by remember { mutableStateOf(false) }

    // Handle back button: close diff, palette, quick open, chat, sidebar, cloud deploy, or bottom panel
    BackHandler(enabled = activeActivity == VsCodeActivity.CLOUD_DEPLOY || diffFileChange != null || isCommandPaletteOpen || isQuickOpenOpen || isSidebarOpen || isPanelOpen || isRightChatSidebarOpen) {
        if (activeActivity == VsCodeActivity.CLOUD_DEPLOY) {
            viewModel.selectActivity(VsCodeActivity.EXPLORER)
        } else if (diffFileChange != null) {
            viewModel.dismissFileDiff()
        } else if (isCommandPaletteOpen) {
            viewModel.closeCommandPalette()
        } else if (isQuickOpenOpen) {
            viewModel.closeQuickOpen()
        } else if (isSidebarOpen) {
            viewModel.closeSidebar()
        } else if (isRightChatSidebarOpen) {
            viewModel.closeRightChatSidebar()
        } else if (isPanelOpen) {
            viewModel.closePanel()
        }
    }

    val openFiles = remember(projectFiles, openTabIds) {
        projectFiles.filter { openTabIds.contains(it.id) }
    }

    // Command Palette actions
    val commandPaletteItems = remember(selectedProject, activeFile) {
        listOf(
            CommandPaletteItem("cmd_file_new", "File: New File", "File", "Ctrl+N") {
                viewModel.selectActivity(VsCodeActivity.EXPLORER)
            },
            CommandPaletteItem("cmd_file_quick_open", "File: Go to File...", "File", "Ctrl+P") {
                viewModel.openQuickOpen()
            },
            CommandPaletteItem("cmd_file_save", "File: Save", "File", "Ctrl+S") {
                viewModel.saveActiveFile()
            },
            CommandPaletteItem("cmd_file_save_all", "File: Save All", "File", "Ctrl+K S") {
                viewModel.saveAllFiles()
            },
            CommandPaletteItem("cmd_view_toggle_sidebar", "View: Toggle Primary Side Bar", "View", "Ctrl+B") {
                viewModel.toggleSidebar()
            },
            CommandPaletteItem("cmd_view_toggle_chat", "View: Toggle Copilot Chat", "View", "Ctrl+Alt+I") {
                viewModel.toggleRightChatSidebar()
            },
            CommandPaletteItem("cmd_view_toggle_panel", "View: Toggle Panel (Terminal)", "View", "Ctrl+J") {
                viewModel.togglePanel()
            },
            CommandPaletteItem("cmd_view_terminal", "Terminal: Create New Terminal", "Terminal", "Ctrl+Shift+`") {
                viewModel.selectPanelTab(VsCodeBottomTab.TERMINAL)
            },
            CommandPaletteItem("cmd_view_web_preview", "View: Toggle Web Live Preview", "View", "Ctrl+P") {
                viewModel.selectPanelTab(VsCodeBottomTab.WEB_PREVIEW)
            },
            CommandPaletteItem("cmd_debug_run", "Run: Start Debugging (F5)", "Debug", "F5") {
                viewModel.runActiveFile()
            },
            CommandPaletteItem("cmd_debug_stop", "Run: Stop Debugging", "Debug", "Shift+F5") {
                viewModel.stopDebugging()
            },
            CommandPaletteItem("cmd_git_commit", "Git: Commit Staged Changes", "Git", "Ctrl+Enter") {
                viewModel.selectActivity(VsCodeActivity.SOURCE_CONTROL)
            },
            CommandPaletteItem("cmd_git_push", "Git: Push to Remote", "Git") {
                viewModel.commitChanges("feat: push to origin")
            },
            CommandPaletteItem("cmd_deploy_dashboard", "Deploy: Open Cloud Deployment Dashboard", "Deploy", "Ctrl+Shift+D") {
                viewModel.selectActivity(VsCodeActivity.CLOUD_DEPLOY)
            },
            CommandPaletteItem("cmd_deploy_push_active", "Deploy: Push & Deploy to Cloud Provider", "Deploy") {
                viewModel.triggerDeployment(selectedDeployProvider)
            },
            CommandPaletteItem("cmd_deploy_vercel", "Deploy: Trigger Vercel Production Release", "Deploy") {
                viewModel.triggerDeployment("Vercel")
            },
            CommandPaletteItem("cmd_deploy_cloudflare", "Deploy: Trigger Cloudflare Workers", "Deploy") {
                viewModel.triggerDeployment("Cloudflare")
            },
            CommandPaletteItem("cmd_ai_copilot_debug", "Copilot: Explain & Fix Code Issues", "AI Assistant") {
                viewModel.openRightChatSidebar()
            },
            CommandPaletteItem("cmd_preferences_settings", "Preferences: Open Settings (UI)", "Preferences", "Ctrl+,") {
                viewModel.selectActivity(VsCodeActivity.SETTINGS)
            },
            CommandPaletteItem("cmd_preferences_theme", "Preferences: Color Theme", "Preferences", "Ctrl+K Ctrl+T") {
                viewModel.selectActivity(VsCodeActivity.SETTINGS)
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            // Visual Studio Code Top Menu & Title Bar (matching user picture)
            VsCodeTopMenuBar(
                workspaceName = selectedProject?.name ?: "Administrator",
                isLeftSidebarOpen = isSidebarOpen,
                isPanelOpen = isPanelOpen,
                isRightChatOpen = isRightChatSidebarOpen,
                isSignedIn = isSignedIn,
                onToggleLeftSidebar = { viewModel.toggleSidebar() },
                onTogglePanel = { viewModel.togglePanel() },
                onToggleRightChat = { viewModel.toggleRightChatSidebar() },
                onOpenCommandPalette = { viewModel.openCommandPalette() },
                onOpenQuickOpen = { viewModel.openQuickOpen() },
                onOpenSettings = { viewModel.selectActivity(VsCodeActivity.SETTINGS) },
                onNewFile = { viewModel.selectActivity(VsCodeActivity.EXPLORER) },
                onSaveFile = { viewModel.saveActiveFile() },
                onSaveAll = { viewModel.saveAllFiles() },
                onCloseActiveFile = { activeFileId?.let { viewModel.closeTab(it) } },
                onRunFile = { viewModel.runActiveFile() },
                onStopDebugging = { viewModel.stopDebugging() },
                onUndo = { viewModel.undo() },
                onRedo = { viewModel.redo() },
                onCut = {
                    clipboardManager.setText(AnnotatedString(currentCode))
                    viewModel.onCodeChanged("")
                },
                onCopy = {
                    clipboardManager.setText(AnnotatedString(currentCode))
                },
                onPaste = {
                    clipboardManager.getText()?.text?.let { viewModel.insertSymbol(it) }
                },
                onToggleFind = { viewModel.toggleSearch() },
                onToggleReplace = { viewModel.toggleSearch() },
                onSelectAll = { /* select all feedback */ },
                onGoBack = { viewModel.goBackFile() },
                onGoForward = { viewModel.goForwardFile() },
                onOpenBottomTab = { tab -> viewModel.selectPanelTab(tab) },
                onOpenDeployDashboard = { viewModel.selectActivity(VsCodeActivity.CLOUD_DEPLOY) },
                onSignInClick = { viewModel.openSignInDialog() }
            )
        },
        bottomBar = {
            if (editorSettings.statusBarVisible) {
                VsCodeStatusBar(
                    branchName = gitCurrentBranch,
                    cursorLine = cursorLine,
                    cursorCol = cursorCol,
                    language = activeFile?.language ?: SupportedLanguage.JAVASCRIPT,
                    tabSize = editorSettings.tabSize,
                    isPanelOpen = isPanelOpen,
                    onTogglePanel = { viewModel.togglePanel() },
                    onBranchClick = { viewModel.selectActivity(VsCodeActivity.SOURCE_CONTROL) },
                    onLanguageClick = { showLanguagePicker = true },
                    onCopilotClick = { viewModel.toggleRightChatSidebar() },
                    onSyncClick = { viewModel.triggerCloudSync() }
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Activity Bar (docked left, with Explorer, Search, Source Control, Run, Extensions, Testing, Python)
            VsCodeActivityBar(
                activeActivity = activeActivity,
                isSidebarOpen = isSidebarOpen,
                pendingChangesCount = gitPendingChangesCount,
                onSelectActivity = { act -> viewModel.selectActivity(act) },
                onOpenSettingsMenu = { viewModel.selectActivity(VsCodeActivity.SETTINGS) },
                onOpenCommandPalette = { viewModel.openCommandPalette() }
            )

            // 2. Primary Side Bar (collapsible left panel)
            if (isSidebarOpen) {
                VsCodePrimarySidebar(
                    activity = activeActivity,
                    project = selectedProject,
                    files = projectFiles,
                    activeFileId = activeFileId,
                    openTabIds = openTabIds,
                    commits = gitCommits,
                    deployments = deployments,
                    isDeploying = isDeploying,
                    breakpoints = breakpoints,
                    extensions = extensions,
                    searchQuery = crossFileSearchQuery,
                    searchResults = crossFileSearchResults,
                    onSelectFile = { file -> viewModel.openFile(file) },
                    onCloseTab = { id -> viewModel.closeTab(id) },
                    onNewFile = { name -> viewModel.createNewFile(name) },
                    onDeleteFile = { id -> viewModel.deleteFile(id) },
                    onRenameFile = { id, newName -> viewModel.renameFile(id, newName) },
                    onSearchChange = { q -> viewModel.performCrossFileSearch(q) },
                    onCommitChanges = { msg -> viewModel.commitChanges(msg) },
                    onTriggerDeploy = { provider -> viewModel.triggerDeployment(provider) },
                    onToggleExtension = { id -> viewModel.toggleExtension(id) },
                    onToggleBreakpoint = { line -> viewModel.toggleBreakpoint(line) },
                    onCloseSidebar = { viewModel.closeSidebar() },
                    gitStatusMap = gitStatusMap,
                    gitCurrentBranch = gitCurrentBranch,
                    gitBranches = gitBranches,
                    gitStagedChanges = gitStagedChanges,
                    gitUnstagedChanges = gitUnstagedChanges,
                    onStageFile = { id -> viewModel.stageFile(id) },
                    onUnstageFile = { id -> viewModel.unstageFile(id) },
                    onStageAll = { viewModel.stageAllFiles() },
                    onUnstageAll = { viewModel.unstageAllFiles() },
                    onDiscardChanges = { id -> viewModel.discardFileChanges(id) },
                    onSelectBranch = { branch -> viewModel.selectGitBranch(branch) },
                    onCreateBranch = { branch -> viewModel.createGitBranch(branch) },
                    onViewDiff = { change -> viewModel.viewFileDiff(change) },
                    porcelainStatus = viewModel.getPorcelainStatus()
                )

                // Thin Divider line between sidebar and editor
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(Color(0xFF282828))
                )
            }

            // 3. Center Workspace Area (Watermark vs Code Editor + Bottom Panel)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                // Main Workspace Area (Cloud Deploy Dashboard vs Empty Watermark vs Editor)
                if (!isPanelMaximized) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        if (activeActivity == VsCodeActivity.CLOUD_DEPLOY) {
                            CloudDeploymentScreen(
                                project = selectedProject,
                                deployments = deployments,
                                isDeploying = isDeploying,
                                deployProgressMessage = deployProgressMessage,
                                liveLogs = deployLiveLogs,
                                pipelineStages = pipelineStages,
                                providerConfigs = providerConfigs,
                                selectedProviderId = selectedDeployProvider,
                                branches = gitBranches,
                                currentBranch = gitCurrentBranch,
                                onSelectProvider = { viewModel.selectDeployProvider(it) },
                                onUpdateProviderConfig = { viewModel.updateProviderConfig(it) },
                                onAddEnvVar = { prov, k, v -> viewModel.addEnvVar(prov, k, v) },
                                onRemoveEnvVar = { prov, k -> viewModel.removeEnvVar(prov, k) },
                                onTestConnection = { viewModel.testProviderConnection(it) },
                                onTriggerDeploy = { prov, domain, branch -> viewModel.triggerDeployment(prov, domain, branch) },
                                onCancelDeploy = { viewModel.cancelDeployment() },
                                onRollback = { record -> viewModel.rollbackDeployment(record) },
                                onOpenEditor = { viewModel.selectActivity(VsCodeActivity.EXPLORER) },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else if (openFiles.isEmpty() || activeFile == null) {
                            // Show the watermark logo & keyboard shortcuts matching the user's screenshot
                            VsCodeEmptyWatermarkView(
                                onOpenChat = { viewModel.openRightChatSidebar() },
                                onShowAllCommands = { viewModel.openCommandPalette() },
                                onToggleTerminal = { viewModel.togglePanel() },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            CodeEditorScreen(
                                activeFile = activeFile,
                                openFiles = openFiles,
                                currentCode = currentCode,
                                themeMode = editorTheme,
                                fontSize = editorSettings.fontSize,
                                isWordWrap = editorSettings.wordWrap,
                                showLineNumbers = editorSettings.lineNumbers != "off",
                                isExecuting = isExecuting,
                                executionResult = executionResult,
                                isSearchVisible = isSearchVisible,
                                searchQuery = searchQuery,
                                replaceQuery = replaceQuery,
                                projectName = selectedProject?.name ?: "Administrator",
                                breadcrumbsVisible = editorSettings.breadcrumbsVisible,
                                minimapVisible = editorSettings.minimap,
                                breakpoints = breakpoints,
                                onToggleBreakpoint = { line -> viewModel.toggleBreakpoint(line) },
                                onCodeChange = { viewModel.onCodeChanged(it) },
                                onSelectFile = { viewModel.openFile(it) },
                                onCloseFile = { viewModel.closeTab(it) },
                                onNewFileClick = { viewModel.selectActivity(VsCodeActivity.EXPLORER) },
                                onRunClick = { viewModel.runActiveFile() },
                                onAiClick = { viewModel.openRightChatSidebar() },
                                onToggleSearch = { viewModel.toggleSearch() },
                                onSearchChange = { viewModel.setSearchQuery(it) },
                                onReplaceChange = { viewModel.setReplaceQuery(it) },
                                onReplaceAll = { viewModel.replaceAll() },
                                onInsertSymbol = { viewModel.insertSymbol(it) },
                                onUndo = { viewModel.undo() },
                                onRedo = { viewModel.redo() },
                                onOpenSettings = { viewModel.selectActivity(VsCodeActivity.SETTINGS) },
                                onDismissExecutionResult = { /* dismissed */ },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                // 4. VS Code Bottom Panel (Terminal, Web Preview, Problems, Output, Debug Console)
                if (isPanelOpen) {
                    VsCodeBottomPanel(
                        activeTab = activePanelTab,
                        isMaximized = isPanelMaximized,
                        project = selectedProject,
                        files = projectFiles,
                        terminalLines = terminalLines,
                        terminalInput = terminalInput,
                        onTabSelect = { tab -> viewModel.selectPanelTab(tab) },
                        onToggleMaximize = { viewModel.togglePanelMaximized() },
                        onClose = { viewModel.closePanel() },
                        onTerminalInputChange = { viewModel.setTerminalInput(it) },
                        onTerminalSubmit = { viewModel.submitTerminalCommand() },
                        modifier = if (isPanelMaximized) Modifier.fillMaxSize() else Modifier.height(230.dp)
                    )
                }
            }

            // 5. Right Secondary Side Bar: Copilot Agent Chat Panel (matching picture)
            if (isRightChatSidebarOpen) {
                // Thin Divider line between editor and chat panel
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(Color(0xFF282828))
                )

                VsCodeCopilotAgentChatPanel(
                    messages = chatMessages,
                    inputText = chatInputText,
                    selectedModel = selectedModel,
                    isAgentMode = isAgentMode,
                    isGenerating = isChatGenerating,
                    onInputChange = { viewModel.updateChatInput(it) },
                    onSendMessage = { prompt -> viewModel.sendChatMessage(prompt) },
                    onSelectModel = { model -> viewModel.selectModel(model) },
                    onToggleAgentMode = { viewModel.toggleAgentMode() },
                    onApplyCode = { code ->
                        viewModel.onCodeChanged(code)
                    },
                    onCloseChat = { viewModel.closeRightChatSidebar() },
                    onNewChat = { viewModel.clearChat() }
                )
            }
        }
    }

    // Command Palette Modal (Ctrl+Shift+P)
    if (isCommandPaletteOpen) {
        VsCodeCommandPalette(
            query = commandPaletteQuery,
            onQueryChange = { viewModel.setCommandPaletteQuery(it) },
            commands = commandPaletteItems,
            onDismiss = { viewModel.closeCommandPalette() }
        )
    }

    // Quick Open Dialog (Ctrl+P / Go to File...)
    if (isQuickOpenOpen) {
        VsCodeQuickOpenDialog(
            files = projectFiles,
            onSelectFile = { file -> viewModel.openFile(file) },
            onDismiss = { viewModel.closeQuickOpen() }
        )
    }

    // Full VS Code Settings Dialog (Ctrl+,)
    if (activeActivity == VsCodeActivity.SETTINGS && isSidebarOpen) {
        VsCodeSettingsDialog(
            currentSettings = editorSettings,
            currentTheme = editorTheme,
            onSaveSettings = { updated -> viewModel.updateEditorSettings(updated) },
            onThemeSelect = { theme -> viewModel.setEditorTheme(theme) },
            onDismiss = { viewModel.selectActivity(VsCodeActivity.EXPLORER) }
        )
    }

    // Sign In Modal
    if (showSignInDialog) {
        var emailInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { viewModel.closeSignInDialog() },
            title = { Text("Sign in to Visual Studio Code", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Sign in with your GitHub or Microsoft account to sync your settings and enable GitHub Copilot.",
                        color = Color(0xFFCCCCCC),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Email address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("sign_in_email_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.signInUser(emailInput) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0078D4))
                ) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeSignInDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Gemini AI Assistant Dialog
    if (showAiDialog) {
        AiAssistantDialog(
            currentLanguage = activeFile?.language ?: SupportedLanguage.JAVASCRIPT,
            isLoading = isAiLoading,
            result = aiResult,
            onRequestAction = { task, targetLang, customInst ->
                viewModel.requestAiAction(task, targetLang, customInst)
            },
            onApplyCode = { newCode ->
                viewModel.applyAiCode(newCode)
            },
            onDismiss = { viewModel.closeAiDialog() }
        )
    }

    // Language Mode Picker Dialog
    if (showLanguagePicker) {
        AlertDialog(
            onDismissRequest = { showLanguagePicker = false },
            title = { Text("Select Language Mode", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp)) {
                    SupportedLanguage.entries.forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val current = activeFile
                                    if (current != null) {
                                        viewModel.openFile(current.copy(language = lang))
                                    }
                                    showLanguagePicker = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(8.dp).background(Color(lang.badgeColorHex), RoundedCornerShape(4.dp)))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(lang.displayName, fontSize = 13.sp, color = Color.White)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguagePicker = false }) { Text("Close") }
            }
        )
    }

    // Git Diff Viewer Dialog
    diffFileChange?.let { change ->
        GitDiffViewerDialog(
            fileName = change.fileName,
            diffLines = currentDiffLines,
            onDismiss = { viewModel.dismissFileDiff() }
        )
    }
}
