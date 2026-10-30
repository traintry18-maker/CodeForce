package com.example.ui.vscode

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CodeFileEntity
import com.example.data.model.DeploymentRecordEntity
import com.example.data.model.GitCommitEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.SupportedLanguage
import com.example.git.GitFileChange
import com.example.git.GitStatusType
import com.example.ui.theme.*

@Composable
fun VsCodePrimarySidebar(
    activity: VsCodeActivity,
    project: ProjectEntity?,
    files: List<CodeFileEntity>,
    activeFileId: Long?,
    openTabIds: List<Long>,
    commits: List<GitCommitEntity>,
    deployments: List<DeploymentRecordEntity>,
    isDeploying: Boolean,
    breakpoints: Set<Int>,
    extensions: List<VsCodeExtension>,
    searchQuery: String,
    searchResults: List<VsCodeSearchResult>,
    onSelectFile: (CodeFileEntity) -> Unit,
    onCloseTab: (Long) -> Unit,
    onNewFile: (String) -> Unit,
    onDeleteFile: (Long) -> Unit,
    onRenameFile: (Long, String) -> Unit = { _, _ -> },
    onSearchChange: (String) -> Unit,
    onCommitChanges: (String) -> Unit,
    onTriggerDeploy: (String) -> Unit,
    onToggleExtension: (String) -> Unit,
    onToggleBreakpoint: (Int) -> Unit,
    onCloseSidebar: () -> Unit,
    gitStatusMap: Map<Long, GitStatusType> = emptyMap(),
    gitCurrentBranch: String = "main",
    gitBranches: List<String> = listOf("main"),
    gitStagedChanges: List<GitFileChange> = emptyList(),
    gitUnstagedChanges: List<GitFileChange> = emptyList(),
    onStageFile: (Long) -> Unit = {},
    onUnstageFile: (Long) -> Unit = {},
    onStageAll: () -> Unit = {},
    onUnstageAll: () -> Unit = {},
    onDiscardChanges: (Long) -> Unit = {},
    onSelectBranch: (String) -> Unit = {},
    onCreateBranch: (String) -> Unit = {},
    onViewDiff: (GitFileChange) -> Unit = {},
    porcelainStatus: String = "",
    modifier: Modifier = Modifier
) {
    if (activity == VsCodeActivity.EXPLORER) {
        VsCodeFileExplorerSidebar(
            project = project,
            files = files,
            activeFileId = activeFileId,
            openTabIds = openTabIds,
            onSelectFile = onSelectFile,
            onCloseTab = onCloseTab,
            onNewFile = onNewFile,
            onDeleteFile = onDeleteFile,
            onRenameFile = onRenameFile,
            onCloseSidebar = onCloseSidebar,
            gitStatusMap = gitStatusMap,
            modifier = modifier
        )
        return
    }

    var showNewFileDialog by remember { mutableStateOf(false) }
    var commitMessage by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(260.dp)
            .background(VsCodeSidebarBg)
    ) {
        // Sidebar Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(35.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = activity.title.uppercase(),
                color = VsCodeSidebarHeaderText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (activity == VsCodeActivity.EXPLORER) {
                    IconButton(
                        onClick = { showNewFileDialog = true },
                        modifier = Modifier.size(22.dp).testTag("sidebar_new_file")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "New File", tint = VsCodeSidebarHeaderText, modifier = Modifier.size(16.dp))
                    }
                }

                IconButton(
                    onClick = onCloseSidebar,
                    modifier = Modifier.size(22.dp).testTag("sidebar_close")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close Sidebar", tint = VsCodeSidebarHeaderText, modifier = Modifier.size(14.dp))
                }
            }
        }

        HorizontalDivider(color = Color(0xFF1E1E1E), thickness = 1.dp)

        // Sidebar Content Body
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (activity) {
                VsCodeActivity.EXPLORER -> {
                    ExplorerSidebarView(
                        project = project,
                        files = files,
                        activeFileId = activeFileId,
                        openTabIds = openTabIds,
                        onSelectFile = onSelectFile,
                        onCloseTab = onCloseTab,
                        onDeleteFile = onDeleteFile
                    )
                }
                VsCodeActivity.SEARCH -> {
                    SearchSidebarView(
                        query = searchQuery,
                        results = searchResults,
                        onQueryChange = onSearchChange,
                        onSelectResult = { res ->
                            val f = files.firstOrNull { it.id == res.fileId }
                            if (f != null) onSelectFile(f)
                        }
                    )
                }
                VsCodeActivity.SOURCE_CONTROL -> {
                    VsCodeSourceControlView(
                        project = project,
                        currentBranch = gitCurrentBranch,
                        branches = gitBranches,
                        stagedChanges = gitStagedChanges,
                        unstagedChanges = gitUnstagedChanges,
                        commits = commits,
                        commitMessage = commitMessage,
                        onCommitMessageChange = { commitMessage = it },
                        onCommit = {
                            if (commitMessage.isNotBlank()) {
                                onCommitChanges(commitMessage)
                                commitMessage = ""
                            }
                        },
                        onStageFile = onStageFile,
                        onUnstageFile = onUnstageFile,
                        onStageAll = onStageAll,
                        onUnstageAll = onUnstageAll,
                        onDiscardChanges = onDiscardChanges,
                        onSelectBranch = onSelectBranch,
                        onCreateBranch = onCreateBranch,
                        onViewDiff = onViewDiff,
                        porcelainStatus = porcelainStatus
                    )
                }
                VsCodeActivity.RUN_AND_DEBUG -> {
                    RunAndDebugSidebarView(
                        breakpoints = breakpoints,
                        activeFile = files.firstOrNull { it.id == activeFileId },
                        onToggleBreakpoint = onToggleBreakpoint
                    )
                }
                VsCodeActivity.CLOUD_DEPLOY -> {
                    DeploySidebarView(
                        project = project,
                        deployments = deployments,
                        isDeploying = isDeploying,
                        onTriggerDeploy = onTriggerDeploy,
                        onOpenFullDashboard = onCloseSidebar
                    )
                }
                VsCodeActivity.EXTENSIONS -> {
                    ExtensionsSidebarView(
                        extensions = extensions,
                        onToggleExtension = onToggleExtension
                    )
                }
                VsCodeActivity.TESTING -> {
                    TestingSidebarView()
                }
                VsCodeActivity.PYTHON -> {
                    PythonSidebarView()
                }
                VsCodeActivity.SETTINGS -> {
                    SettingsQuickNavView()
                }
            }
        }
    }

    if (showNewFileDialog) {
        var newFileName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewFileDialog = false },
            title = { Text("New File", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newFileName,
                    onValueChange = { newFileName = it },
                    placeholder = { Text("e.g. index.js, Component.jsx, script.py") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("new_file_input_modal")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFileName.isNotBlank()) {
                            onNewFile(newFileName)
                            showNewFileDialog = false
                        }
                    }
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showNewFileDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ExplorerSidebarView(
    project: ProjectEntity?,
    files: List<CodeFileEntity>,
    activeFileId: Long?,
    openTabIds: List<Long>,
    onSelectFile: (CodeFileEntity) -> Unit,
    onCloseTab: (Long) -> Unit,
    onDeleteFile: (Long) -> Unit
) {
    val openFiles = files.filter { openTabIds.contains(it.id) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // OPEN EDITORS section
        item {
            SectionHeader(title = "OPEN EDITORS (${openFiles.size})")
        }
        items(openFiles) { f ->
            val isActive = f.id == activeFileId
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isActive) VsCodeSidebarItemActive else Color.Transparent)
                    .clickable { onSelectFile(f) }
                    .padding(horizontal = 14.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(f.language.badgeColorHex)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = f.name,
                        color = if (isActive) Color.White else Color(0xFFCCCCCC),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = VsCodeSidebarHeaderText,
                    modifier = Modifier.size(14.dp).clickable { onCloseTab(f.id) }
                )
            }
        }

        // WORKSPACE FOLDER section
        item {
            Spacer(modifier = Modifier.height(10.dp))
            SectionHeader(title = "${project?.name?.uppercase() ?: "WORKSPACE"}")
        }
        items(files) { f ->
            val isActive = f.id == activeFileId
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isActive) VsCodeSidebarItemActive else Color.Transparent)
                    .clickable { onSelectFile(f) }
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(f.language.badgeColorHex)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = f.name,
                        color = if (isActive) Color.White else Color(0xFFCCCCCC),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    if (f.isEntry) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "[main]",
                            color = VsCodeSuccess,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                if (files.size > 1) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = VsCodeSidebarHeaderText.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp).clickable { onDeleteFile(f.id) }
                    )
                }
            }
        }

        // OUTLINE section
        item {
            Spacer(modifier = Modifier.height(14.dp))
            SectionHeader(title = "OUTLINE")
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                Text("• imports", color = Color(0xFF858585), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Text("• functions & hooks", color = Color(0xFF858585), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Text("• export default component", color = Color(0xFF858585), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
private fun SearchSidebarView(
    query: String,
    results: List<VsCodeSearchResult>,
    onQueryChange: (String) -> Unit,
    onSelectResult: (VsCodeSearchResult) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search across all files...", color = Color(0xFF6E7681), fontSize = 11.sp) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = VsCodeInputBg,
                unfocusedContainerColor = VsCodeInputBg,
                focusedBorderColor = VsCodeInputFocusBorder,
                unfocusedBorderColor = VsCodeInputBorder,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth().height(44.dp).testTag("global_search_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = if (query.isBlank()) "Type to search workspace" else "${results.size} results in workspace",
            color = Color(0xFF858585),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(results) { res ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectResult(res) }
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "${res.fileName} : ${res.lineNumber}",
                        color = VsCodeInfo,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = res.lineText,
                        color = Color(0xFFCCCCCC),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun SourceControlSidebarView(
    project: ProjectEntity?,
    commits: List<GitCommitEntity>,
    commitMessage: String,
    onCommitMessageChange: (String) -> Unit,
    onCommit: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Branch: ${project?.gitBranch ?: "main"}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color(0xFFCCCCCC), modifier = Modifier.size(16.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = commitMessage,
            onValueChange = onCommitMessageChange,
            placeholder = { Text("Message (Ctrl+Enter to commit)", color = Color(0xFF6E7681), fontSize = 11.sp) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = VsCodeInputBg,
                unfocusedContainerColor = VsCodeInputBg,
                focusedBorderColor = VsCodeInputFocusBorder,
                unfocusedBorderColor = VsCodeInputBorder,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth().height(60.dp).testTag("git_sidebar_commit_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onCommit,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007ACC)),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth().height(32.dp).testTag("git_sidebar_commit_btn")
        ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Commit & Sync", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(14.dp))
        SectionHeader(title = "COMMIT TIMELINE (${commits.size})")

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(commits) { c ->
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(c.message, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Text("${c.commitHash} • ${c.branch}", color = VsCodeInfo, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
private fun RunAndDebugSidebarView(
    breakpoints: Set<Int>,
    activeFile: CodeFileEntity?,
    onToggleBreakpoint: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
        Button(
            onClick = { /* debug */ },
            colors = ButtonDefaults.buttonColors(containerColor = VsCodeSuccess),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth().height(34.dp).testTag("start_debugging_button")
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Start Debugging (F5)", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(14.dp))
        SectionHeader(title = "BREAKPOINTS (${breakpoints.size})")

        if (breakpoints.isEmpty()) {
            Text(
                text = "Tap gutter next to line numbers in the editor to toggle breakpoints.",
                color = Color(0xFF858585),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(breakpoints.sorted()) { line ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleBreakpoint(line) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(VsCodeBreakpoint))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${activeFile?.name ?: "file"} : line $line",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeploySidebarView(
    project: ProjectEntity?,
    deployments: List<DeploymentRecordEntity>,
    isDeploying: Boolean,
    onTriggerDeploy: (String) -> Unit,
    onOpenFullDashboard: () -> Unit = {}
) {
    val providers = listOf("Vercel", "Netlify", "Cloudflare", "Render", "AWS")
    val clipboardManager = LocalClipboardManager.current
    Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
        // Full Dashboard Trigger Button
        Button(
            onClick = onOpenFullDashboard,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
            shape = RoundedCornerShape(6.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .testTag("sidebar_open_full_deploy_dashboard")
        ) {
            Icon(Icons.Default.CloudQueue, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Open Full Dashboard", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Production Status Header
        Surface(
            color = Color(0xFF252526),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isDeploying) Color(0xFFEAB308) else Color(0xFF10B981))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isDeploying) "BUILDING" else "PROD LIVE",
                            color = if (isDeploying) Color(0xFFEAB308) else Color(0xFF10B981),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Text(
                        text = project?.deploymentProvider ?: "Vercel",
                        color = Color(0xFF93C5FD),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                val liveUrl = project?.cloudDeploymentUrl?.ifBlank { "https://${(project.name).lowercase().replace(" ", "-")}.codeforge.cloud" }
                    ?: "https://app.codeforge.cloud"
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = liveUrl,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { clipboardManager.setText(AnnotatedString(liveUrl)) },
                        modifier = Modifier.size(20.dp).testTag("sidebar_copy_deploy_url")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy URL", tint = Color(0xFF94A3B8), modifier = Modifier.size(12.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        SectionHeader(title = "QUICK DEPLOY TARGETS")
        Spacer(modifier = Modifier.height(4.dp))

        providers.forEach { p ->
            Button(
                onClick = { onTriggerDeploy(p) },
                enabled = !isDeploying,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2D2E)),
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.fillMaxWidth().height(32.dp).padding(vertical = 2.dp).testTag("quick_deploy_$p")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(p, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium)
                    }
                    Text("Push", fontSize = 10.sp, color = Color(0xFF94A3B8), fontFamily = FontFamily.Monospace)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        SectionHeader(title = "RECENT RELEASES (${deployments.size})")

        if (deployments.isEmpty()) {
            Text(
                text = "No releases yet. Click a provider above to push code.",
                color = Color(0xFF858585),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(deployments.take(15)) { dep ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF10B981)))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(dep.provider, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("(${dep.buildDurationMs}ms)", color = Color(0xFF858585), fontSize = 10.sp)
                        }
                        Text(
                            text = dep.deployUrl,
                            color = Color(0xFF38BDF8),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )
                        HorizontalDivider(color = Color(0xFF282828), modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ExtensionsSidebarView(
    extensions: List<VsCodeExtension>,
    onToggleExtension: (String) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(10.dp)) {
        item {
            SectionHeader(title = "INSTALLED EXTENSIONS (${extensions.size})")
        }
        items(extensions) { ext ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(ext.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("${ext.publisher} • ${ext.downloads}", color = Color(0xFF858585), fontSize = 10.sp)
                    }
                    Switch(
                        checked = ext.isEnabled,
                        onCheckedChange = { onToggleExtension(ext.id) },
                        modifier = Modifier.height(24.dp)
                    )
                }
                Text(ext.description, color = Color(0xFFCCCCCC), fontSize = 10.sp, maxLines = 2)
                HorizontalDivider(color = Color(0xFF333333), modifier = Modifier.padding(top = 6.dp))
            }
        }
    }
}

@Composable
private fun SettingsQuickNavView() {
    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Text("SETTINGS CATEGORIES", color = VsCodeSidebarHeaderText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        listOf(
            "Commonly Used",
            "Text Editor",
            "Workbench",
            "Window",
            "Features",
            "Application",
            "Security"
        ).forEach { cat ->
            Text(
                text = "• $cat",
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun TestingSidebarView() {
    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        SectionHeader(title = "TEST EXPLORER")
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = VsCodeSuccess, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Unit Tests (All Passed)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text("• ExampleUnitTest: addition_isCorrect", color = Color(0xFFCCCCCC), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        Text("• RobolectricTest: app_name check", color = Color(0xFFCCCCCC), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun PythonSidebarView() {
    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        SectionHeader(title = "PYTHON ENVIRONMENT")
        Spacer(modifier = Modifier.height(8.dp))
        Text("Active Interpreter:", color = VsCodeSidebarHeaderText, fontSize = 11.sp)
        Text("Python 3.12.2 64-bit ('codeforge-env')", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))
        Text("Linters & Formatters:", color = VsCodeSidebarHeaderText, fontSize = 11.sp)
        Text("• Black Formatter: Enabled", color = VsCodeSuccess, fontSize = 11.sp)
        Text("• Flake8 Linter: Enabled", color = VsCodeSuccess, fontSize = 11.sp)
        Text("• Pyright Type Checking: Active", color = VsCodeInfo, fontSize = 11.sp)
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = "▼ $title",
        color = VsCodeSidebarHeaderText,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
    )
}
