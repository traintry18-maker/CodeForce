package com.example.ui.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CodeFileEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.SupportedLanguage

@Composable
fun ProjectDrawerContent(
    projects: List<ProjectEntity>,
    selectedProjectId: Long?,
    projectFiles: List<CodeFileEntity>,
    activeFileId: Long?,
    onSelectProject: (Long) -> Unit,
    onSelectFile: (CodeFileEntity) -> Unit,
    onNewProject: (name: String, description: String, language: SupportedLanguage) -> Unit,
    onNewFile: (name: String) -> Unit,
    onDeleteFile: (Long) -> Unit,
    onDeleteProject: (Long) -> Unit,
    onCloseDrawer: () -> Unit
) {
    var showNewProjectDialog by remember { mutableStateOf(false) }
    var showNewFileDialog by remember { mutableStateOf(false) }
    var projectToDelete by remember { mutableStateOf<ProjectEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp)
    ) {
        // App / Workspace Branding
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Color(0xFF0284C7),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("⚡", fontSize = 18.sp)
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "CodeForge Cloud",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Multi-Language Studio",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp
                    )
                }
            }

            IconButton(onClick = onCloseDrawer, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // New Project Button
        Button(
            onClick = { showNewProjectDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(40.dp).testTag("new_project_button")
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Create New Project", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "WORKSPACES (${projects.size})",
            color = Color(0xFF64748B),
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Projects list (horizontal or compact vertical)
        LazyColumn(
            modifier = Modifier.heightIn(max = 160.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(projects) { proj ->
                val isSelected = proj.id == selectedProjectId

                Surface(
                    color = if (isSelected) Color(0xFF1E293B) else Color.Transparent,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectProject(proj.id) }
                        .testTag("project_item_${proj.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(proj.primaryLanguage.badgeColorHex))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = proj.name,
                                color = if (isSelected) Color(0xFF38BDF8) else Color(0xFFCBD5E1),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }

                        if (isSelected) {
                            IconButton(
                                onClick = { projectToDelete = proj },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = Color(0xFF334155), modifier = Modifier.padding(vertical = 12.dp))

        // Current Project File Tree
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "FILES IN WORKSPACE",
                color = Color(0xFF64748B),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )

            IconButton(
                onClick = { showNewFileDialog = true },
                modifier = Modifier.size(26.dp).testTag("drawer_new_file_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = "New File", tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(projectFiles) { file ->
                val isActive = file.id == activeFileId

                Surface(
                    color = if (isActive) Color(0xFF1E293B) else Color.Transparent,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelectFile(file)
                            onCloseDrawer()
                        }
                        .testTag("file_item_${file.name}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(file.language.badgeColorHex))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = file.name,
                                color = if (isActive) Color.White else Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
                            )
                            if (file.isEntry) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "[main]",
                                    color = Color(0xFF10B981),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        if (projectFiles.size > 1) {
                            IconButton(
                                onClick = { onDeleteFile(file.id) },
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Delete File",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // New Project Dialog
    if (showNewProjectDialog) {
        var projName by remember { mutableStateOf("") }
        var projDesc by remember { mutableStateOf("") }
        var selectedLang by remember { mutableStateOf(SupportedLanguage.REACT) }

        AlertDialog(
            onDismissRequest = { showNewProjectDialog = false },
            title = { Text("Create New Project", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                    OutlinedTextField(
                        value = projName,
                        onValueChange = { projName = it },
                        label = { Text("Project Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("new_project_name_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = projDesc,
                        onValueChange = { projDesc = it },
                        label = { Text("Description") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Select Primary Language:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    SupportedLanguage.entries.forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedLang = lang }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedLang == lang,
                                onClick = { selectedLang = lang }
                            )
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(lang.badgeColorHex))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(lang.displayName, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (projName.isNotBlank()) {
                            onNewProject(projName, projDesc, selectedLang)
                            showNewProjectDialog = false
                            onCloseDrawer()
                        }
                    },
                    enabled = projName.isNotBlank()
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewProjectDialog = false }) { Text("Cancel") }
            }
        )
    }

    // New File Dialog
    if (showNewFileDialog) {
        var fileName by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showNewFileDialog = false },
            title = { Text("Create New File", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = fileName,
                        onValueChange = { fileName = it },
                        placeholder = { Text("e.g. utils.js, styles.css, main.py") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("new_file_name_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (fileName.isNotBlank()) {
                            onNewFile(fileName)
                            showNewFileDialog = false
                            onCloseDrawer()
                        }
                    },
                    enabled = fileName.isNotBlank()
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFileDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Delete Project Confirmation Dialog
    if (projectToDelete != null) {
        AlertDialog(
            onDismissRequest = { projectToDelete = null },
            title = { Text("Delete Workspace?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to permanently delete '${projectToDelete?.name}' and all its files?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteProject(projectToDelete!!.id)
                        projectToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { projectToDelete = null }) { Text("Cancel") }
            }
        )
    }
}
