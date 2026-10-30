package com.example.ui.vscode

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Undo
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
import com.example.data.model.GitCommitEntity
import com.example.data.model.ProjectEntity
import com.example.git.DiffLine
import com.example.git.DiffLineType
import com.example.git.GitFileChange
import com.example.git.GitStatusType
import com.example.ui.theme.*

/**
 * Full VS Code Source Control panel displaying Git working tree changes,
 * staged changes, commit input, branch management, and diff review.
 */
@Composable
fun VsCodeSourceControlView(
    project: ProjectEntity?,
    currentBranch: String,
    branches: List<String>,
    stagedChanges: List<GitFileChange>,
    unstagedChanges: List<GitFileChange>,
    commits: List<GitCommitEntity>,
    commitMessage: String,
    onCommitMessageChange: (String) -> Unit,
    onCommit: () -> Unit,
    onStageFile: (Long) -> Unit,
    onUnstageFile: (Long) -> Unit,
    onStageAll: () -> Unit,
    onUnstageAll: () -> Unit,
    onDiscardChanges: (Long) -> Unit,
    onSelectBranch: (String) -> Unit,
    onCreateBranch: (String) -> Unit,
    onViewDiff: (GitFileChange) -> Unit,
    porcelainStatus: String = "",
    modifier: Modifier = Modifier
) {
    var isStagedExpanded by remember { mutableStateOf(true) }
    var isChangesExpanded by remember { mutableStateOf(true) }
    var isBranchesExpanded by remember { mutableStateOf(false) }
    var isTimelineExpanded by remember { mutableStateOf(true) }
    var isCliOutputExpanded by remember { mutableStateOf(false) }

    var showNewBranchDialog by remember { mutableStateOf(false) }
    var newBranchName by remember { mutableStateOf("") }
    var branchMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF181818))
            .padding(8.dp)
    ) {
        // 1. Branch Selector Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Row(
                    modifier = Modifier
                        .clickable { branchMenuExpanded = true }
                        .background(Color(0xFF252526), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ForkRight,
                        contentDescription = "Current Branch",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = currentBranch,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Switch Branch",
                        tint = Color(0xFF858585),
                        modifier = Modifier.size(14.dp)
                    )
                }

                DropdownMenu(
                    expanded = branchMenuExpanded,
                    onDismissRequest = { branchMenuExpanded = false },
                    modifier = Modifier.background(Color(0xFF252526))
                ) {
                    branches.forEach { b ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (b == currentBranch) {
                                        Text("✓ ", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    } else {
                                        Spacer(modifier = Modifier.width(14.dp))
                                    }
                                    Text(b, color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                }
                            },
                            onClick = {
                                onSelectBranch(b)
                                branchMenuExpanded = false
                            }
                        )
                    }
                    HorizontalDivider(color = Color(0xFF333333))
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Create new branch...", color = Color(0xFF38BDF8), fontSize = 11.sp)
                            }
                        },
                        onClick = {
                            branchMenuExpanded = false
                            showNewBranchDialog = true
                        }
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = onStageAll,
                    modifier = Modifier.size(24.dp).testTag("git_stage_all_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Stage All Changes",
                        tint = Color(0xFF858585),
                        modifier = Modifier.size(14.dp)
                    )
                }
                IconButton(
                    onClick = onUnstageAll,
                    modifier = Modifier.size(24.dp).testTag("git_unstage_all_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Unstage All Changes",
                        tint = Color(0xFF858585),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 2. Commit Message Box & Commit Button
        OutlinedTextField(
            value = commitMessage,
            onValueChange = onCommitMessageChange,
            placeholder = {
                Text(
                    text = "Message (Ctrl+Enter to commit)",
                    color = Color(0xFF6E7681),
                    fontSize = 11.sp
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF1E1E1E),
                unfocusedContainerColor = Color(0xFF1E1E1E),
                focusedBorderColor = Color(0xFF0078D4),
                unfocusedBorderColor = Color(0xFF3C3C3C),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .testTag("git_sidebar_commit_input")
        )

        Spacer(modifier = Modifier.height(6.dp))

        Button(
            onClick = onCommit,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007ACC)),
            shape = RoundedCornerShape(3.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .testTag("git_sidebar_commit_btn")
        ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Commit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 3. Changes Accordions
        LazyColumn(modifier = Modifier.weight(1f)) {
            // STAGED CHANGES
            item {
                SourceControlAccordionHeader(
                    title = "STAGED CHANGES",
                    count = stagedChanges.size,
                    isExpanded = isStagedExpanded,
                    onToggle = { isStagedExpanded = !isStagedExpanded },
                    trailingIcon = {
                        if (stagedChanges.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Unstage All",
                                tint = Color(0xFF858585),
                                modifier = Modifier
                                    .size(13.dp)
                                    .clickable { onUnstageAll() }
                            )
                        }
                    }
                )
            }

            if (isStagedExpanded) {
                if (stagedChanges.isEmpty()) {
                    item {
                        Text(
                            text = "No staged changes",
                            color = Color(0xFF6E7681),
                            fontSize = 11.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            modifier = Modifier.padding(start = 22.dp, top = 2.dp, bottom = 6.dp)
                        )
                    }
                } else {
                    items(stagedChanges) { change ->
                        SourceControlFileRow(
                            change = change,
                            onFileClick = { onViewDiff(change) },
                            actionIcon = Icons.Default.Remove,
                            actionTooltip = "Unstage Changes",
                            onActionClick = { onUnstageFile(change.fileId) }
                        )
                    }
                }
            }

            // CHANGES (UNSTAGED)
            item {
                Spacer(modifier = Modifier.height(4.dp))
                SourceControlAccordionHeader(
                    title = "CHANGES",
                    count = unstagedChanges.size,
                    isExpanded = isChangesExpanded,
                    onToggle = { isChangesExpanded = !isChangesExpanded },
                    trailingIcon = {
                        if (unstagedChanges.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Stage All",
                                tint = Color(0xFF858585),
                                modifier = Modifier
                                    .size(13.dp)
                                    .clickable { onStageAll() }
                            )
                        }
                    }
                )
            }

            if (isChangesExpanded) {
                if (unstagedChanges.isEmpty()) {
                    item {
                        Text(
                            text = "Working tree clean",
                            color = Color(0xFF6E7681),
                            fontSize = 11.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            modifier = Modifier.padding(start = 22.dp, top = 2.dp, bottom = 6.dp)
                        )
                    }
                } else {
                    items(unstagedChanges) { change ->
                        SourceControlFileRow(
                            change = change,
                            onFileClick = { onViewDiff(change) },
                            actionIcon = Icons.Default.Add,
                            actionTooltip = "Stage Changes",
                            onActionClick = { onStageFile(change.fileId) },
                            secondaryActionIcon = Icons.AutoMirrored.Filled.Undo,
                            onSecondaryActionClick = { onDiscardChanges(change.fileId) }
                        )
                    }
                }
            }

            // RAW GIT CLI OUTPUT (PORCELAIN)
            item {
                Spacer(modifier = Modifier.height(4.dp))
                SourceControlAccordionHeader(
                    title = "GIT CLI (PORCELAIN)",
                    count = null,
                    isExpanded = isCliOutputExpanded,
                    onToggle = { isCliOutputExpanded = !isCliOutputExpanded }
                )
            }

            if (isCliOutputExpanded) {
                item {
                    Surface(
                        color = Color(0xFF1E1E1E),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = porcelainStatus.ifBlank { "# Working tree clean\ngit status --porcelain (0 changes)" },
                            color = Color(0xFFCCCCCC),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                }
            }

            // COMMIT TIMELINE
            item {
                Spacer(modifier = Modifier.height(6.dp))
                SourceControlAccordionHeader(
                    title = "COMMITS",
                    count = commits.size,
                    isExpanded = isTimelineExpanded,
                    onToggle = { isTimelineExpanded = !isTimelineExpanded }
                )
            }

            if (isTimelineExpanded) {
                items(commits) { c ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 18.dp, top = 4.dp, bottom = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = c.commitHash,
                                color = Color(0xFF38BDF8),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = c.message,
                                color = Color.White,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                        Text(
                            text = "${c.author} • ${c.branch}",
                            color = Color(0xFF858585),
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }

    // Dialog for creating a new branch
    if (showNewBranchDialog) {
        AlertDialog(
            onDismissRequest = { showNewBranchDialog = false },
            title = { Text("Create New Branch", fontWeight = FontWeight.Bold, fontSize = 14.sp) },
            text = {
                OutlinedTextField(
                    value = newBranchName,
                    onValueChange = { newBranchName = it },
                    placeholder = { Text("e.g. feature/auth-flow") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF0078D4),
                        unfocusedBorderColor = Color(0xFF3C3C3C),
                        focusedContainerColor = Color(0xFF1E1E1E),
                        unfocusedContainerColor = Color(0xFF1E1E1E)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("new_branch_dialog_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = newBranchName.trim()
                        if (trimmed.isNotBlank()) {
                            onCreateBranch(trimmed)
                            newBranchName = ""
                            showNewBranchDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0078D4))
                ) {
                    Text("Create Branch")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewBranchDialog = false }) {
                    Text("Cancel", color = Color(0xFFCCCCCC))
                }
            },
            containerColor = Color(0xFF252526)
        )
    }
}

@Composable
private fun SourceControlAccordionHeader(
    title: String,
    count: Int?,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Surface(
        color = Color(0xFF212121),
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .clickable { onToggle() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color(0xFFBBBBBB),
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = title,
                    color = Color(0xFFCCCCCC),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )
                if (count != null && count > 0) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF333333))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "$count",
                            color = Color(0xFFCCCCCC),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            trailingIcon?.invoke()
        }
    }
}

@Composable
private fun SourceControlFileRow(
    change: GitFileChange,
    onFileClick: () -> Unit,
    actionIcon: androidx.compose.ui.graphics.vector.ImageVector,
    actionTooltip: String,
    onActionClick: () -> Unit,
    secondaryActionIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onSecondaryActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onFileClick() }
            .padding(start = 18.dp, end = 6.dp, top = 3.dp, bottom = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = change.fileName,
                color = change.status.color,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = change.filePath.substringBeforeLast('/', ""),
                color = Color(0xFF6E7681),
                fontSize = 10.sp,
                maxLines = 1
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Status Code Badge (M, U, A, D)
            Text(
                text = change.status.code,
                color = change.status.color,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )

            // Secondary Action (e.g. Discard / Revert)
            if (secondaryActionIcon != null && onSecondaryActionClick != null) {
                Icon(
                    imageVector = secondaryActionIcon,
                    contentDescription = "Discard Changes",
                    tint = Color(0xFF858585),
                    modifier = Modifier
                        .size(13.dp)
                        .clickable { onSecondaryActionClick() }
                )
            }

            // Primary Action (e.g. Stage or Unstage)
            Icon(
                imageVector = actionIcon,
                contentDescription = actionTooltip,
                tint = Color(0xFF858585),
                modifier = Modifier
                    .size(13.dp)
                    .clickable { onActionClick() }
            )
        }
    }
}

/**
 * Unified Diff Dialog showing line additions (+) in green and deletions (-) in red.
 */
@Composable
fun GitDiffViewerDialog(
    fileName: String,
    diffLines: List<DiffLine>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Diff: $fileName",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFFCCCCCC))
                }
            }
        },
        text = {
            Surface(
                color = Color(0xFF1E1E1E),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    items(diffLines) { line ->
                        val (bgColor, textColor) = when (line.type) {
                            DiffLineType.ADDED -> Color(0x332EA043) to Color(0xFF7EE787)
                            DiffLineType.DELETED -> Color(0x33F85149) to Color(0xFFFFA198)
                            DiffLineType.HEADER -> Color(0x2238BDF8) to Color(0xFF38BDF8)
                            DiffLineType.UNCHANGED -> Color.Transparent to Color(0xFFC9D1D9)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(bgColor)
                                .padding(vertical = 1.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = (line.oldLineNumber?.toString() ?: "").padStart(3, ' '),
                                color = Color(0xFF6E7681),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.width(26.dp)
                            )
                            Text(
                                text = (line.newLineNumber?.toString() ?: "").padStart(3, ' '),
                                color = Color(0xFF6E7681),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.width(26.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = line.text,
                                color = textColor,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0078D4))
            ) {
                Text("Close")
            }
        },
        containerColor = Color(0xFF252526)
    )
}
