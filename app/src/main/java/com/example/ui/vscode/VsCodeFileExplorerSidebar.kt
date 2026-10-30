package com.example.ui.vscode

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CodeFileEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.SupportedLanguage
import com.example.git.GitStatusType
import com.example.ui.theme.*

/**
 * Node model for displaying hierarchical directory trees in the file explorer.
 */
sealed class ExplorerTreeNode(
    open val id: String,
    open val name: String,
    open val fullPath: String,
    open val depth: Int
) {
    data class FolderNode(
        override val id: String,
        override val name: String,
        override val fullPath: String,
        override val depth: Int,
        val children: MutableList<ExplorerTreeNode> = mutableListOf()
    ) : ExplorerTreeNode(id, name, fullPath, depth)

    data class FileNode(
        override val id: String,
        override val name: String,
        override val fullPath: String,
        override val depth: Int,
        val file: CodeFileEntity
    ) : ExplorerTreeNode(id, name, fullPath, depth)
}

/**
 * Builds a hierarchical directory tree from a flat list of CodeFileEntities.
 * Handles paths with directories (e.g. "src/components/Header.jsx", "public/index.html", "App.jsx").
 */
fun buildExplorerTree(
    projectName: String,
    files: List<CodeFileEntity>,
    customFolders: Set<String>
): ExplorerTreeNode.FolderNode {
    val root = ExplorerTreeNode.FolderNode(
        id = "root",
        name = projectName.ifBlank { "WORKSPACE" },
        fullPath = "",
        depth = 0
    )

    // First ensure explicitly created folders exist
    for (folderPath in customFolders) {
        val segments = folderPath.trim('/').split("/").filter { it.isNotBlank() }
        var current = root
        var pathAcc = ""
        for (seg in segments) {
            pathAcc = if (pathAcc.isEmpty()) seg else "$pathAcc/$seg"
            var child = current.children
                .filterIsInstance<ExplorerTreeNode.FolderNode>()
                .firstOrNull { it.name == seg }
            if (child == null) {
                child = ExplorerTreeNode.FolderNode(
                    id = "folder_$pathAcc",
                    name = seg,
                    fullPath = pathAcc,
                    depth = current.depth + 1
                )
                current.children.add(child)
            }
            current = child
        }
    }

    // Populate all files into directory hierarchy
    for (file in files) {
        val rawPath = (if (file.path.isNotBlank() && file.path != "/") file.path else file.name)
            .removePrefix("/")
            .replace("\\", "/")
        val segments = rawPath.split("/").filter { it.isNotBlank() }

        if (segments.isEmpty()) continue

        var currentFolder = root
        var currentPath = ""

        // Folder segments
        for (i in 0 until segments.size - 1) {
            val folderName = segments[i]
            currentPath = if (currentPath.isEmpty()) folderName else "$currentPath/$folderName"

            var childFolder = currentFolder.children
                .filterIsInstance<ExplorerTreeNode.FolderNode>()
                .firstOrNull { it.name == folderName }

            if (childFolder == null) {
                childFolder = ExplorerTreeNode.FolderNode(
                    id = "folder_$currentPath",
                    name = folderName,
                    fullPath = currentPath,
                    depth = currentFolder.depth + 1
                )
                currentFolder.children.add(childFolder)
            }
            currentFolder = childFolder
        }

        // File segment (last part)
        val fileName = segments.last()
        val filePath = if (currentPath.isEmpty()) fileName else "$currentPath/$fileName"
        currentFolder.children.add(
            ExplorerTreeNode.FileNode(
                id = "file_${file.id}",
                name = fileName,
                fullPath = filePath,
                depth = currentFolder.depth + 1,
                file = file
            )
        )
    }

    // Sort folders first, then files alphabetically
    fun sortFolder(folder: ExplorerTreeNode.FolderNode) {
        folder.children.sortWith(
            compareBy<ExplorerTreeNode> { it !is ExplorerTreeNode.FolderNode }
                .thenBy { it.name.lowercase() }
        )
        for (child in folder.children) {
            if (child is ExplorerTreeNode.FolderNode) {
                sortFolder(child)
            }
        }
    }
    sortFolder(root)

    return root
}

/**
 * A dedicated VS Code File Explorer Sidebar Component that displays a project's
 * directory structure as an expandable/collapsible tree and allows users to
 * select files for editing, create new files/folders, rename, and delete files.
 */
@Composable
fun VsCodeFileExplorerSidebar(
    project: ProjectEntity?,
    files: List<CodeFileEntity>,
    activeFileId: Long?,
    openTabIds: List<Long>,
    onSelectFile: (CodeFileEntity) -> Unit,
    onCloseTab: (Long) -> Unit,
    onNewFile: (String) -> Unit,
    onDeleteFile: (Long) -> Unit,
    onRenameFile: (Long, String) -> Unit,
    onCloseSidebar: () -> Unit,
    gitStatusMap: Map<Long, GitStatusType> = emptyMap(),
    modifier: Modifier = Modifier
) {
    // Custom folders created by the user in this session
    val customFolders = remember { mutableStateMapOf<String, Boolean>() }

    // Expanded folders set (path -> isExpanded)
    val expandedFolders = remember { mutableStateMapOf<String, Boolean>() }

    // Section collapse states
    var isOpenEditorsExpanded by remember { mutableStateOf(true) }
    var isWorkspaceExpanded by remember { mutableStateOf(true) }
    var isOutlineExpanded by remember { mutableStateOf(false) }
    var isTimelineExpanded by remember { mutableStateOf(false) }

    // Filter query
    var isFilterVisible by remember { mutableStateOf(false) }
    var filterQuery by remember { mutableStateOf("") }

    // Dialogs state
    var showNewFileDialog by remember { mutableStateOf(false) }
    var newFileTargetFolder by remember { mutableStateOf("") }
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var newFolderTargetParent by remember { mutableStateOf("") }

    var fileToRename by remember { mutableStateOf<CodeFileEntity?>(null) }
    var fileToDelete by remember { mutableStateOf<CodeFileEntity?>(null) }

    val openFiles = remember(files, openTabIds) {
        files.filter { openTabIds.contains(it.id) }
    }

    // Filter files if filterQuery is active
    val filteredFiles = remember(files, filterQuery) {
        if (filterQuery.isBlank()) files
        else files.filter { it.name.contains(filterQuery, ignoreCase = true) || it.path.contains(filterQuery, ignoreCase = true) }
    }

    // Build hierarchical tree
    val rootFolder = remember(project?.name, filteredFiles, customFolders.keys.toSet()) {
        buildExplorerTree(
            projectName = project?.name ?: "WORKSPACE",
            files = filteredFiles,
            customFolders = customFolders.keys.toSet()
        )
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(260.dp)
            .background(Color(0xFF181818))
    ) {
        // --- Sidebar Top Header (EXPLORER title + Action Icons) ---
        Surface(
            color = Color(0xFF181818),
            modifier = Modifier
                .fillMaxWidth()
                .height(35.dp)
                .padding(horizontal = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "EXPLORER",
                    color = Color(0xFFBBBBBB),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 0.8.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // New File button
                    IconButton(
                        onClick = {
                            newFileTargetFolder = ""
                            showNewFileDialog = true
                        },
                        modifier = Modifier.size(22.dp).testTag("explorer_new_file_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.NoteAdd,
                            contentDescription = "New File...",
                            tint = Color(0xFF858585),
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // New Folder button
                    IconButton(
                        onClick = {
                            newFolderTargetParent = ""
                            showNewFolderDialog = true
                        },
                        modifier = Modifier.size(22.dp).testTag("explorer_new_folder_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreateNewFolder,
                            contentDescription = "New Folder...",
                            tint = Color(0xFF858585),
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Refresh button
                    IconButton(
                        onClick = { /* trigger refresh */ },
                        modifier = Modifier.size(22.dp).testTag("explorer_refresh_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Explorer",
                            tint = Color(0xFF858585),
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Collapse All Folders button
                    IconButton(
                        onClick = {
                            expandedFolders.clear()
                        },
                        modifier = Modifier.size(22.dp).testTag("explorer_collapse_all_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.UnfoldLess,
                            contentDescription = "Collapse Folders in Explorer",
                            tint = Color(0xFF858585),
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Filter toggle button
                    IconButton(
                        onClick = {
                            isFilterVisible = !isFilterVisible
                            if (!isFilterVisible) filterQuery = ""
                        },
                        modifier = Modifier.size(22.dp).testTag("explorer_filter_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Filter Files",
                            tint = if (isFilterVisible || filterQuery.isNotEmpty()) Color(0xFF38BDF8) else Color(0xFF858585),
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    // Close Sidebar button
                    IconButton(
                        onClick = onCloseSidebar,
                        modifier = Modifier.size(22.dp).testTag("explorer_close_sidebar_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Primary Side Bar",
                            tint = Color(0xFF858585),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = Color(0xFF282828), thickness = 1.dp)

        // Filter text box if visible
        AnimatedVisibility(visible = isFilterVisible) {
            Surface(
                color = Color(0xFF1E1E1E),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF252526), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Color(0xFF858585),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    BasicTextField(
                        value = filterQuery,
                        onValueChange = { filterQuery = it },
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.SansSerif
                        ),
                        cursorBrush = SolidColor(Color(0xFF0078D4)),
                        modifier = Modifier.weight(1f).testTag("explorer_filter_input")
                    )
                    if (filterQuery.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear Filter",
                            tint = Color(0xFF858585),
                            modifier = Modifier
                                .size(13.dp)
                                .clickable { filterQuery = "" }
                        )
                    }
                }
            }
        }

        // --- Explorer Body: Open Editors + Directory Tree + Outline ---
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // 1. OPEN EDITORS Section
            item {
                SectionAccordionHeader(
                    title = "OPEN EDITORS",
                    badge = if (openFiles.isNotEmpty()) "(${openFiles.size})" else null,
                    isExpanded = isOpenEditorsExpanded,
                    onToggle = { isOpenEditorsExpanded = !isOpenEditorsExpanded },
                    trailingContent = {
                        if (openFiles.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.ClearAll,
                                contentDescription = "Close All Editors",
                                tint = Color(0xFF858585),
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable {
                                        openFiles.forEach { onCloseTab(it.id) }
                                    }
                            )
                        }
                    }
                )
            }

            if (isOpenEditorsExpanded) {
                if (openFiles.isEmpty()) {
                    item {
                        Text(
                            text = "No open editors",
                            color = Color(0xFF6E7681),
                            fontSize = 11.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            modifier = Modifier.padding(start = 24.dp, top = 2.dp, bottom = 6.dp)
                        )
                    }
                } else {
                    items(openFiles) { f ->
                        val isActive = f.id == activeFileId
                        val gitStatus = gitStatusMap[f.id] ?: GitStatusType.UNMODIFIED
                        OpenEditorRow(
                            file = f,
                            isActive = isActive,
                            gitStatus = gitStatus,
                            onSelect = { onSelectFile(f) },
                            onClose = { onCloseTab(f.id) }
                        )
                    }
                }
            }

            // 2. PROJECT DIRECTORY STRUCTURE Section
            item {
                Spacer(modifier = Modifier.height(4.dp))
                SectionAccordionHeader(
                    title = project?.name?.uppercase() ?: "PROJECT",
                    badge = null,
                    isExpanded = isWorkspaceExpanded,
                    onToggle = { isWorkspaceExpanded = !isWorkspaceExpanded },
                    trailingContent = {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.NoteAdd,
                                contentDescription = "New File in Workspace",
                                tint = Color(0xFF858585),
                                modifier = Modifier
                                    .size(13.dp)
                                    .clickable {
                                        newFileTargetFolder = ""
                                        showNewFileDialog = true
                                    }
                            )
                            Icon(
                                imageVector = Icons.Default.CreateNewFolder,
                                contentDescription = "New Folder in Workspace",
                                tint = Color(0xFF858585),
                                modifier = Modifier
                                    .size(13.dp)
                                    .clickable {
                                        newFolderTargetParent = ""
                                        showNewFolderDialog = true
                                    }
                            )
                        }
                    }
                )
            }

            if (isWorkspaceExpanded) {
                // Render root children recursively
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        for (child in rootFolder.children) {
                            RenderTreeNode(
                                node = child,
                                activeFileId = activeFileId,
                                expandedFolders = expandedFolders,
                                gitStatusMap = gitStatusMap,
                                onToggleFolder = { path ->
                                    val current = expandedFolders[path] ?: true
                                    expandedFolders[path] = !current
                                },
                                onSelectFile = onSelectFile,
                                onNewFileInFolder = { folderPath ->
                                    newFileTargetFolder = folderPath
                                    showNewFileDialog = true
                                },
                                onNewFolderInFolder = { folderPath ->
                                    newFolderTargetParent = folderPath
                                    showNewFolderDialog = true
                                },
                                onRenameFile = { file -> fileToRename = file },
                                onDeleteFile = { file -> fileToDelete = file }
                            )
                        }
                    }
                }
            }

            // 3. OUTLINE Section
            item {
                Spacer(modifier = Modifier.height(6.dp))
                SectionAccordionHeader(
                    title = "OUTLINE",
                    badge = null,
                    isExpanded = isOutlineExpanded,
                    onToggle = { isOutlineExpanded = !isOutlineExpanded }
                )
            }

            if (isOutlineExpanded) {
                item {
                    val activeFile = files.firstOrNull { it.id == activeFileId }
                    Column(modifier = Modifier.padding(start = 22.dp, top = 4.dp, bottom = 8.dp)) {
                        if (activeFile != null) {
                            Text("📦 imports", color = Color(0xFF858585), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("⚡ component: ${activeFile.name.substringBeforeLast('.')}", color = Color(0xFF9CDCFE), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("ƒ render()", color = Color(0xFFDCDCAA), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("⇥ export default", color = Color(0xFF858585), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        } else {
                            Text("No symbol outlines available", color = Color(0xFF6E7681), fontSize = 11.sp)
                        }
                    }
                }
            }

            // 4. TIMELINE Section
            item {
                Spacer(modifier = Modifier.height(4.dp))
                SectionAccordionHeader(
                    title = "TIMELINE",
                    badge = null,
                    isExpanded = isTimelineExpanded,
                    onToggle = { isTimelineExpanded = !isTimelineExpanded }
                )
            }

            if (isTimelineExpanded) {
                item {
                    Column(modifier = Modifier.padding(start = 22.dp, top = 4.dp, bottom = 12.dp)) {
                        Text("• Local History: File Saved", color = Color(0xFFCCCCCC), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Text("  Just now", color = Color(0xFF6E7681), fontSize = 9.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("• Git: Initial Scaffold", color = Color(0xFFCCCCCC), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Text("  main branch", color = Color(0xFF6E7681), fontSize = 9.sp)
                    }
                }
            }
        }
    }

    // --- DIALOGS ---

    // 1. New File Dialog
    if (showNewFileDialog) {
        var inputFileName by remember { mutableStateOf("") }
        var targetFolderInput by remember { mutableStateOf(newFileTargetFolder) }

        AlertDialog(
            onDismissRequest = { showNewFileDialog = false },
            title = {
                Text(
                    text = if (targetFolderInput.isNotBlank()) "New File in $targetFolderInput" else "New File",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Enter file name with extension (e.g., Header.jsx, style.css, utils/math.js):",
                        color = Color(0xFFCCCCCC),
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = inputFileName,
                        onValueChange = { inputFileName = it },
                        placeholder = { Text("e.g. Component.jsx", color = Color(0xFF6E7681)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF0078D4),
                            unfocusedBorderColor = Color(0xFF3C3C3C),
                            focusedContainerColor = Color(0xFF1E1E1E),
                            unfocusedContainerColor = Color(0xFF1E1E1E)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("new_file_dialog_input")
                    )

                    if (targetFolderInput.isNotBlank()) {
                        Text(
                            text = "Target directory: $targetFolderInput",
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = inputFileName.trim()
                        if (trimmed.isNotBlank()) {
                            val finalPath = if (targetFolderInput.isNotBlank()) {
                                "${targetFolderInput.trim('/')}/$trimmed"
                            } else {
                                trimmed
                            }
                            onNewFile(finalPath)
                            showNewFileDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0078D4))
                ) {
                    Text("Create File")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFileDialog = false }) {
                    Text("Cancel", color = Color(0xFFCCCCCC))
                }
            },
            containerColor = Color(0xFF252526)
        )
    }

    // 2. New Folder Dialog
    if (showNewFolderDialog) {
        var inputFolderName by remember { mutableStateOf("") }
        var parentFolderInput by remember { mutableStateOf(newFolderTargetParent) }

        AlertDialog(
            onDismissRequest = { showNewFolderDialog = false },
            title = {
                Text(
                    text = if (parentFolderInput.isNotBlank()) "New Folder in $parentFolderInput" else "New Folder",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Enter directory name (e.g. components, utils, styles, assets):",
                        color = Color(0xFFCCCCCC),
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = inputFolderName,
                        onValueChange = { inputFolderName = it },
                        placeholder = { Text("e.g. components", color = Color(0xFF6E7681)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF0078D4),
                            unfocusedBorderColor = Color(0xFF3C3C3C),
                            focusedContainerColor = Color(0xFF1E1E1E),
                            unfocusedContainerColor = Color(0xFF1E1E1E)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("new_folder_dialog_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = inputFolderName.trim()
                        if (trimmed.isNotBlank()) {
                            val folderPath = if (parentFolderInput.isNotBlank()) {
                                "${parentFolderInput.trim('/')}/$trimmed"
                            } else {
                                trimmed
                            }
                            customFolders[folderPath] = true
                            expandedFolders[folderPath] = true
                            showNewFolderDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0078D4))
                ) {
                    Text("Create Folder")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFolderDialog = false }) {
                    Text("Cancel", color = Color(0xFFCCCCCC))
                }
            },
            containerColor = Color(0xFF252526)
        )
    }

    // 3. Rename File Dialog
    fileToRename?.let { targetFile ->
        var renameInput by remember { mutableStateOf(targetFile.name) }
        AlertDialog(
            onDismissRequest = { fileToRename = null },
            title = { Text("Rename File", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter new name for '${targetFile.name}':", color = Color(0xFFCCCCCC), fontSize = 12.sp)
                    OutlinedTextField(
                        value = renameInput,
                        onValueChange = { renameInput = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF0078D4),
                            unfocusedBorderColor = Color(0xFF3C3C3C),
                            focusedContainerColor = Color(0xFF1E1E1E),
                            unfocusedContainerColor = Color(0xFF1E1E1E)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("rename_file_dialog_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = renameInput.trim()
                        if (trimmed.isNotBlank() && trimmed != targetFile.name) {
                            onRenameFile(targetFile.id, trimmed)
                        }
                        fileToRename = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0078D4))
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { fileToRename = null }) {
                    Text("Cancel", color = Color(0xFFCCCCCC))
                }
            },
            containerColor = Color(0xFF252526)
        )
    }

    // 4. Delete File Dialog
    fileToDelete?.let { targetFile ->
        AlertDialog(
            onDismissRequest = { fileToDelete = null },
            title = { Text("Delete File", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White) },
            text = {
                Text(
                    text = "Are you sure you want to delete '${targetFile.name}'? This cannot be undone.",
                    color = Color(0xFFCCCCCC),
                    fontSize = 12.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteFile(targetFile.id)
                        fileToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { fileToDelete = null }) {
                    Text("Cancel", color = Color(0xFFCCCCCC))
                }
            },
            containerColor = Color(0xFF252526)
        )
    }
}

/**
 * Accordion header component with expand/collapse chevron for sections.
 */
@Composable
private fun SectionAccordionHeader(
    title: String,
    badge: String?,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Surface(
        color = Color(0xFF212121),
        modifier = Modifier
            .fillMaxWidth()
            .height(26.dp)
            .clickable { onToggle() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color(0xFFBBBBBB),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = title,
                    color = Color(0xFFCCCCCC),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 0.5.sp
                )
                if (badge != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = badge,
                        color = Color(0xFF858585),
                        fontSize = 10.sp
                    )
                }
            }

            trailingContent?.invoke()
        }
    }
}

/**
 * Row inside "OPEN EDITORS" section.
 */
@Composable
private fun OpenEditorRow(
    file: CodeFileEntity,
    isActive: Boolean,
    gitStatus: GitStatusType = GitStatusType.UNMODIFIED,
    onSelect: () -> Unit,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isActive) Color(0xFF04395E) else Color.Transparent)
            .clickable { onSelect() }
            .padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LanguageFileIcon(fileName = file.name, language = file.language)
            Spacer(modifier = Modifier.width(6.dp))
            val textColor = when {
                isActive -> Color.White
                gitStatus == GitStatusType.MODIFIED -> Color(0xFFE2C08D)
                gitStatus == GitStatusType.UNTRACKED || gitStatus == GitStatusType.ADDED -> Color(0xFF73C991)
                gitStatus == GitStatusType.DELETED -> Color(0xFFE06C75)
                else -> Color(0xFFCCCCCC)
            }
            Text(
                text = file.name,
                color = textColor,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (gitStatus != GitStatusType.UNMODIFIED) {
                Text(
                    text = gitStatus.code,
                    color = gitStatus.color,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(end = 6.dp)
                )
            }
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close Tab",
                tint = Color(0xFF858585),
                modifier = Modifier
                    .size(14.dp)
                    .clickable { onClose() }
            )
        }
    }
}

/**
 * Recursive composable to render a tree node (folder or file) with authentic
 * VS Code styling, indentation guides, and action icons.
 */
@Composable
private fun RenderTreeNode(
    node: ExplorerTreeNode,
    activeFileId: Long?,
    expandedFolders: Map<String, Boolean>,
    gitStatusMap: Map<Long, GitStatusType>,
    onToggleFolder: (String) -> Unit,
    onSelectFile: (CodeFileEntity) -> Unit,
    onNewFileInFolder: (String) -> Unit,
    onNewFolderInFolder: (String) -> Unit,
    onRenameFile: (CodeFileEntity) -> Unit,
    onDeleteFile: (CodeFileEntity) -> Unit
) {
    val indentPadding = (node.depth * 14).dp

    when (node) {
        is ExplorerTreeNode.FolderNode -> {
            val isExpanded = expandedFolders[node.fullPath] ?: true
            var isHovered by remember { mutableStateOf(false) }

            Column(modifier = Modifier.fillMaxWidth()) {
                // Folder Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleFolder(node.fullPath) }
                        .padding(start = indentPadding + 6.dp, end = 6.dp, top = 3.dp, bottom = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Chevron Arrow
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = Color(0xFFBBBBBB),
                            modifier = Modifier.size(15.dp)
                        )

                        Spacer(modifier = Modifier.width(3.dp))

                        // Folder Icon
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
                            contentDescription = null,
                            tint = Color(0xFFDDA343), // Amber VS Code folder color
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // Folder Name
                        Text(
                            text = node.name,
                            color = Color(0xFFCCCCCC),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.SansSerif,
                            maxLines = 1
                        )

                        // Git modified descendant dot indicator
                        val hasGitChanges = remember(node, gitStatusMap) {
                            checkFolderHasChanges(node, gitStatusMap)
                        }
                        if (hasGitChanges) {
                            Box(
                                modifier = Modifier
                                    .padding(start = 6.dp)
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFCCA700))
                            )
                        }
                    }

                    // Action buttons on folder: + file, + subfolder
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.NoteAdd,
                            contentDescription = "New File in ${node.name}",
                            tint = Color(0xFF858585),
                            modifier = Modifier
                                .size(13.dp)
                                .clickable { onNewFileInFolder(node.fullPath) }
                        )
                        Icon(
                            imageVector = Icons.Default.CreateNewFolder,
                            contentDescription = "New Subfolder in ${node.name}",
                            tint = Color(0xFF858585),
                            modifier = Modifier
                                .size(13.dp)
                                .clickable { onNewFolderInFolder(node.fullPath) }
                        )
                    }
                }

                // Render Children if expanded
                if (isExpanded) {
                    for (child in node.children) {
                        RenderTreeNode(
                            node = child,
                            activeFileId = activeFileId,
                            expandedFolders = expandedFolders,
                            gitStatusMap = gitStatusMap,
                            onToggleFolder = onToggleFolder,
                            onSelectFile = onSelectFile,
                            onNewFileInFolder = onNewFileInFolder,
                            onNewFolderInFolder = onNewFolderInFolder,
                            onRenameFile = onRenameFile,
                            onDeleteFile = onDeleteFile
                        )
                    }
                }
            }
        }

        is ExplorerTreeNode.FileNode -> {
            val isActive = node.file.id == activeFileId
            val gitStatus = gitStatusMap[node.file.id] ?: GitStatusType.UNMODIFIED
            val fileNameColor = when {
                isActive -> Color.White
                gitStatus == GitStatusType.MODIFIED -> Color(0xFFE2C08D) // VS Code modified yellow
                gitStatus == GitStatusType.UNTRACKED || gitStatus == GitStatusType.ADDED -> Color(0xFF73C991) // VS Code untracked green
                gitStatus == GitStatusType.DELETED -> Color(0xFFE06C75) // VS Code deleted red
                else -> Color(0xFFCCCCCC)
            }

            // File Row
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isActive) Color(0xFF04395E) else Color.Transparent)
                    .clickable { onSelectFile(node.file) }
                    .testTag("explorer_file_${node.name.replace(".", "_")}")
            ) {
                // Active Left Accent indicator bar
                if (isActive) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .fillMaxHeight()
                            .align(Alignment.CenterStart)
                            .background(Color(0xFF0078D4))
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = indentPadding + 22.dp, end = 6.dp, top = 3.dp, bottom = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // File Type Icon
                        LanguageFileIcon(fileName = node.name, language = node.file.language)

                        Spacer(modifier = Modifier.width(6.dp))

                        // File Name
                        Text(
                            text = node.name,
                            color = fileNameColor,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1
                        )

                        // Entry badge
                        if (node.file.isEntry) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "[main]",
                                color = VsCodeSuccess,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Status Badge and Action Icons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Git Status Badge ('M', 'U', 'A', 'D')
                        if (gitStatus != GitStatusType.UNMODIFIED) {
                            Text(
                                text = gitStatus.code,
                                color = gitStatus.color,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .testTag("git_badge_${node.name}")
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Rename ${node.name}",
                            tint = Color(0xFF6E7681),
                            modifier = Modifier
                                .size(12.dp)
                                .clickable { onRenameFile(node.file) }
                        )
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete ${node.name}",
                            tint = Color(0xFF6E7681),
                            modifier = Modifier
                                .size(13.dp)
                                .clickable { onDeleteFile(node.file) }
                        )
                    }
                }
            }
        }
    }
}

private fun checkFolderHasChanges(
    folder: ExplorerTreeNode.FolderNode,
    gitStatusMap: Map<Long, GitStatusType>
): Boolean {
    for (child in folder.children) {
        when (child) {
            is ExplorerTreeNode.FileNode -> {
                val st = gitStatusMap[child.file.id]
                if (st != null && st != GitStatusType.UNMODIFIED) return true
            }
            is ExplorerTreeNode.FolderNode -> {
                if (checkFolderHasChanges(child, gitStatusMap)) return true
            }
        }
    }
    return false
}

/**
 * File icon representation based on file extension and language mode.
 */
@Composable
fun LanguageFileIcon(
    fileName: String,
    language: SupportedLanguage,
    modifier: Modifier = Modifier
) {
    val ext = fileName.substringAfterLast('.', "").lowercase()

    when (ext) {
        "jsx", "tsx" -> {
            Text(
                text = "⚛",
                color = Color(0xFF61DAFB), // React cyan
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = modifier
            )
        }
        "js" -> {
            Text(
                text = "JS",
                color = Color(0xFFF7DF1E), // JS yellow
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                modifier = modifier
            )
        }
        "ts" -> {
            Text(
                text = "TS",
                color = Color(0xFF3178C6), // TS blue
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                modifier = modifier
            )
        }
        "html" -> {
            Text(
                text = "<>",
                color = Color(0xFFE44D26), // HTML orange
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = modifier
            )
        }
        "css", "scss" -> {
            Text(
                text = "#",
                color = Color(0xFF264DE4), // CSS blue
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = modifier
            )
        }
        "py" -> {
            Text(
                text = "🐍",
                fontSize = 10.sp,
                modifier = modifier
            )
        }
        "json" -> {
            Text(
                text = "{}",
                color = Color(0xFFFBC02D), // JSON amber
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = modifier
            )
        }
        "md" -> {
            Text(
                text = "M↓",
                color = Color(0xFF9E9E9E),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = modifier
            )
        }
        "sql" -> {
            Icon(
                imageVector = Icons.Default.Storage,
                contentDescription = null,
                tint = Color(0xFFCC292B),
                modifier = modifier.size(13.dp)
            )
        }
        "kt" -> {
            Text(
                text = "Kt",
                color = Color(0xFF7F52FF), // Kotlin purple
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = modifier
            )
        }
        else -> {
            Box(
                modifier = modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(Color(language.badgeColorHex))
            )
        }
    }
}
