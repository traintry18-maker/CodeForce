package com.example.git

import com.example.data.model.CodeFileEntity
import com.example.data.model.ProjectEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Central Version Control Manager that tracks file changes, staging areas,
 * branches, and commits using [GitCliWrapper] concepts.
 */
class VersionControlManager {

    // Baseline snapshots (fileId -> original content at HEAD)
    private val baselineSnapshots = mutableMapOf<Long, String>()
    // Original file names (fileId -> fileName at HEAD)
    private val baselineNames = mutableMapOf<Long, String>()
    // Staged file IDs
    private val stagedFileIds = mutableSetOf<Long>()

    // Current active project ID
    private var activeProjectId: Long? = null

    // Reactive StateFlows
    private val _fileStatusMap = MutableStateFlow<Map<Long, GitStatusType>>(emptyMap())
    val fileStatusMap: StateFlow<Map<Long, GitStatusType>> = _fileStatusMap.asStateFlow()

    private val _fileChanges = MutableStateFlow<List<GitFileChange>>(emptyList())
    val fileChanges: StateFlow<List<GitFileChange>> = _fileChanges.asStateFlow()

    private val _stagedChanges = MutableStateFlow<List<GitFileChange>>(emptyList())
    val stagedChanges: StateFlow<List<GitFileChange>> = _stagedChanges.asStateFlow()

    private val _unstagedChanges = MutableStateFlow<List<GitFileChange>>(emptyList())
    val unstagedChanges: StateFlow<List<GitFileChange>> = _unstagedChanges.asStateFlow()

    private val _currentBranch = MutableStateFlow("main")
    val currentBranch: StateFlow<String> = _currentBranch.asStateFlow()

    private val _branches = MutableStateFlow(listOf("main", "feature/ui-enhancements", "dev"))
    val branches: StateFlow<List<String>> = _branches.asStateFlow()

    private val _totalChangesCount = MutableStateFlow(0)
    val totalChangesCount: StateFlow<Int> = _totalChangesCount.asStateFlow()

    /**
     * Initializes or resets the baseline snapshot for a project.
     */
    fun initializeForProject(project: ProjectEntity?, files: List<CodeFileEntity>) {
        if (project == null) {
            activeProjectId = null
            baselineSnapshots.clear()
            baselineNames.clear()
            stagedFileIds.clear()
            recalculateChanges(emptyList())
            return
        }

        val isNewProject = activeProjectId != project.id
        activeProjectId = project.id
        _currentBranch.value = project.gitBranch.ifBlank { "main" }

        if (isNewProject || baselineSnapshots.isEmpty()) {
            baselineSnapshots.clear()
            baselineNames.clear()
            stagedFileIds.clear()

            // Establish baseline from initial files
            for (file in files) {
                baselineSnapshots[file.id] = file.content
                baselineNames[file.id] = file.name
            }
        }

        recalculateChanges(files)
    }

    /**
     * Recalculates Git status for all files against baseline HEAD snapshot.
     */
    fun onFilesUpdated(files: List<CodeFileEntity>) {
        recalculateChanges(files)
    }

    private fun recalculateChanges(files: List<CodeFileEntity>) {
        val newStatusMap = mutableMapOf<Long, GitStatusType>()
        val allChanges = mutableListOf<GitFileChange>()
        val currentFileIds = files.map { it.id }.toSet()

        // 1. Check existing and new files
        for (file in files) {
            val originalContent = baselineSnapshots[file.id]
            val isStaged = stagedFileIds.contains(file.id)

            if (originalContent == null) {
                // New file not in baseline -> Untracked or Added if staged
                val status = if (isStaged) GitStatusType.ADDED else GitStatusType.UNTRACKED
                val (additions, deletions) = GitCliWrapper.countDiffMetrics("", file.content)
                val change = GitFileChange(
                    fileId = file.id,
                    filePath = file.path.ifBlank { file.name },
                    fileName = file.name,
                    status = status,
                    isStaged = isStaged,
                    additions = additions,
                    deletions = deletions,
                    originalContent = "",
                    currentContent = file.content
                )
                newStatusMap[file.id] = status
                allChanges.add(change)
            } else if (originalContent != file.content) {
                // Content modified compared to baseline
                val status = GitStatusType.MODIFIED
                val (additions, deletions) = GitCliWrapper.countDiffMetrics(originalContent, file.content)
                val change = GitFileChange(
                    fileId = file.id,
                    filePath = file.path.ifBlank { file.name },
                    fileName = file.name,
                    status = status,
                    isStaged = isStaged,
                    additions = additions,
                    deletions = deletions,
                    originalContent = originalContent,
                    currentContent = file.content
                )
                newStatusMap[file.id] = status
                allChanges.add(change)
            } else {
                newStatusMap[file.id] = GitStatusType.UNMODIFIED
            }
        }

        // 2. Check for deleted files (in baseline but missing from current files)
        for ((baseId, baseContent) in baselineSnapshots) {
            if (!currentFileIds.contains(baseId)) {
                val baseName = baselineNames[baseId] ?: "deleted_file"
                val isStaged = stagedFileIds.contains(baseId)
                val change = GitFileChange(
                    fileId = baseId,
                    filePath = baseName,
                    fileName = baseName,
                    status = GitStatusType.DELETED,
                    isStaged = isStaged,
                    additions = 0,
                    deletions = baseContent.lines().size,
                    originalContent = baseContent,
                    currentContent = ""
                )
                newStatusMap[baseId] = GitStatusType.DELETED
                allChanges.add(change)
            }
        }

        _fileStatusMap.value = newStatusMap
        _fileChanges.value = allChanges
        _stagedChanges.value = allChanges.filter { it.isStaged }
        _unstagedChanges.value = allChanges.filter { !it.isStaged }
        _totalChangesCount.value = allChanges.size
    }

    /**
     * Stages a file (equivalent to `git add <file>`).
     */
    fun stageFile(fileId: Long, currentFiles: List<CodeFileEntity>) {
        stagedFileIds.add(fileId)
        recalculateChanges(currentFiles)
    }

    /**
     * Unstages a file (equivalent to `git reset HEAD <file>`).
     */
    fun unstageFile(fileId: Long, currentFiles: List<CodeFileEntity>) {
        stagedFileIds.remove(fileId)
        recalculateChanges(currentFiles)
    }

    /**
     * Stages all changed files (equivalent to `git add -A`).
     */
    fun stageAll(currentFiles: List<CodeFileEntity>) {
        _fileChanges.value.forEach { stagedFileIds.add(it.fileId) }
        recalculateChanges(currentFiles)
    }

    /**
     * Unstages all files (equivalent to `git reset HEAD`).
     */
    fun unstageAll(currentFiles: List<CodeFileEntity>) {
        stagedFileIds.clear()
        recalculateChanges(currentFiles)
    }

    /**
     * Discards changes in a file (equivalent to `git checkout -- <file>`),
     * returning the baseline content to restore.
     */
    fun discardChanges(fileId: Long, currentFiles: List<CodeFileEntity>): String? {
        val original = baselineSnapshots[fileId]
        stagedFileIds.remove(fileId)
        recalculateChanges(currentFiles)
        return original
    }

    /**
     * Commits staged changes or all changes if none are explicitly staged
     * (equivalent to `git commit -m "<message>"`).
     */
    fun commit(
        message: String,
        currentFiles: List<CodeFileEntity>,
        author: String = "Developer <dev@codeforge.cloud>"
    ): GitCommitInfo {
        val commitHash = UUID.randomUUID().toString().replace("-", "").take(7)
        val filesCount = _fileChanges.value.size

        // Update baseline snapshot to current files state
        baselineSnapshots.clear()
        baselineNames.clear()
        for (file in currentFiles) {
            baselineSnapshots[file.id] = file.content
            baselineNames[file.id] = file.name
        }
        stagedFileIds.clear()

        recalculateChanges(currentFiles)

        return GitCommitInfo(
            commitHash = commitHash,
            message = message,
            author = author,
            branch = _currentBranch.value,
            filesChangedCount = filesCount,
            timestamp = System.currentTimeMillis()
        )
    }

    /**
     * Switches the active branch.
     */
    fun switchBranch(branchName: String) {
        _currentBranch.value = branchName
    }

    /**
     * Creates a new Git branch and switches to it.
     */
    fun createBranch(branchName: String) {
        val current = _branches.value.toMutableList()
        if (!current.contains(branchName)) {
            current.add(branchName)
            _branches.value = current
        }
        _currentBranch.value = branchName
    }

    /**
     * Computes the unified diff for a file change.
     */
    fun getDiffForFile(fileId: Long, currentFiles: List<CodeFileEntity>): List<DiffLine> {
        val change = _fileChanges.value.firstOrNull { it.fileId == fileId }
        val currentFile = currentFiles.firstOrNull { it.id == fileId }
        val oldContent = baselineSnapshots[fileId] ?: ""
        val newContent = currentFile?.content ?: change?.currentContent ?: ""
        val fileName = currentFile?.name ?: change?.fileName ?: "file"

        return GitCliWrapper.generateUnifiedDiff(fileName, oldContent, newContent)
    }

    /**
     * Formats the current repository state into standard `git status --porcelain` text.
     */
    fun getPorcelainStatusString(): String {
        return GitCliWrapper.formatPorcelainStatus(_fileChanges.value)
    }
}
