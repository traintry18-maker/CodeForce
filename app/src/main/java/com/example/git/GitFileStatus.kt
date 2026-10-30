package com.example.git

import androidx.compose.ui.graphics.Color

/**
 * Standard Git status indicators matching Git CLI porcelain output and VS Code.
 */
enum class GitStatusType(
    val code: String,
    val label: String,
    val color: Color,
    val description: String
) {
    MODIFIED(
        code = "M",
        label = "Modified",
        color = Color(0xFFCCA700), // VS Code amber/yellow for modified files
        description = "Tracked file has uncommitted changes"
    ),
    UNTRACKED(
        code = "U",
        label = "Untracked",
        color = Color(0xFF73C991), // VS Code green for untracked/new files
        description = "New file not yet tracked by Git"
    ),
    ADDED(
        code = "A",
        label = "Index Added",
        color = Color(0xFF89D185), // VS Code green for staged new files
        description = "File added to staging index"
    ),
    DELETED(
        code = "D",
        label = "Deleted",
        color = Color(0xFFE06C75), // VS Code red for deleted files
        description = "File deleted from working tree"
    ),
    RENAMED(
        code = "R",
        label = "Renamed",
        color = Color(0xFF569CD6), // VS Code blue for renamed files
        description = "File path or name changed"
    ),
    CONFLICT(
        code = "C",
        label = "Conflict",
        color = Color(0xFFF14C4C),
        description = "Merge conflict detected"
    ),
    UNMODIFIED(
        code = "",
        label = "Clean",
        color = Color(0xFF858585),
        description = "File matches HEAD commit"
    )
}

/**
 * Detailed representation of a file change tracked by Version Control.
 */
data class GitFileChange(
    val fileId: Long,
    val filePath: String,
    val fileName: String,
    val status: GitStatusType,
    val isStaged: Boolean = false,
    val additions: Int = 0,
    val deletions: Int = 0,
    val originalContent: String = "",
    val currentContent: String = ""
)

enum class DiffLineType {
    ADDED,
    DELETED,
    UNCHANGED,
    HEADER
}

data class DiffLine(
    val type: DiffLineType,
    val text: String,
    val oldLineNumber: Int? = null,
    val newLineNumber: Int? = null
)

data class GitCommitInfo(
    val commitHash: String,
    val message: String,
    val author: String,
    val branch: String,
    val filesChangedCount: Int,
    val timestamp: Long
)
