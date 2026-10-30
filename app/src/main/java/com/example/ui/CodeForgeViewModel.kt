package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiAssistantService
import com.example.ai.AiResponseResult
import com.example.ai.AiTaskType
import com.example.data.local.AppDatabase
import com.example.data.model.CodeFileEntity
import com.example.data.model.DeploymentRecordEntity
import com.example.data.model.GitCommitEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.SupportedLanguage
import com.example.data.model.SyncDeviceEntity
import com.example.data.repository.CodeRepository
import com.example.execution.CodeRunner
import com.example.execution.ExecutionResult
import com.example.execution.TerminalLine
import com.example.execution.TerminalLineType
import com.example.git.*
import com.example.syntax.EditorThemeMode
import com.example.ui.deploy.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppNavTab(val label: String) {
    EDITOR("Editor"),
    PREVIEW("Web Preview"),
    TERMINAL("Terminal"),
    DEPLOY("Cloud Deploy"),
    GIT("Version Control"),
    SYNC("Cross-Device Sync")
}

class CodeForgeViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = CodeRepository(database)

    val projects: StateFlow<List<ProjectEntity>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val devices: StateFlow<List<SyncDeviceEntity>> = repository.allDevices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedProjectId = MutableStateFlow<Long?>(null)
    val selectedProjectId = _selectedProjectId.asStateFlow()

    private val _selectedProject = MutableStateFlow<ProjectEntity?>(null)
    val selectedProject = _selectedProject.asStateFlow()

    private val _projectFiles = MutableStateFlow<List<CodeFileEntity>>(emptyList())
    val projectFiles = _projectFiles.asStateFlow()

    private val _activeFileId = MutableStateFlow<Long?>(null)
    val activeFileId = _activeFileId.asStateFlow()

    private val _activeFile = MutableStateFlow<CodeFileEntity?>(null)
    val activeFile = _activeFile.asStateFlow()

    private val _openTabIds = MutableStateFlow<List<Long>>(emptyList())
    val openTabIds = _openTabIds.asStateFlow()

    // Version Control Module (Git CLI wrapper & status tracker)
    val vcsManager = VersionControlManager()
    val gitStatusMap: StateFlow<Map<Long, GitStatusType>> = vcsManager.fileStatusMap
    val gitFileChanges: StateFlow<List<GitFileChange>> = vcsManager.fileChanges
    val gitStagedChanges: StateFlow<List<GitFileChange>> = vcsManager.stagedChanges
    val gitUnstagedChanges: StateFlow<List<GitFileChange>> = vcsManager.unstagedChanges
    val gitCurrentBranch: StateFlow<String> = vcsManager.currentBranch
    val gitBranches: StateFlow<List<String>> = vcsManager.branches
    val gitPendingChangesCount: StateFlow<Int> = vcsManager.totalChangesCount

    private val _diffFileChange = MutableStateFlow<GitFileChange?>(null)
    val diffFileChange = _diffFileChange.asStateFlow()

    private val _currentDiffLines = MutableStateFlow<List<DiffLine>>(emptyList())
    val currentDiffLines = _currentDiffLines.asStateFlow()

    private val _currentNavTab = MutableStateFlow(AppNavTab.EDITOR)
    val currentNavTab = _currentNavTab.asStateFlow()

    // Editor configurations
    private val _editorTheme = MutableStateFlow(EditorThemeMode.VS_CODE_DARK)
    val editorTheme = _editorTheme.asStateFlow()

    private val _fontSize = MutableStateFlow(14f)
    val fontSize = _fontSize.asStateFlow()

    private val _wordWrap = MutableStateFlow(false)
    val wordWrap = _wordWrap.asStateFlow()

    private val _showLineNumbers = MutableStateFlow(true)
    val showLineNumbers = _showLineNumbers.asStateFlow()

    // Editor content & Undo/Redo
    private val _currentCode = MutableStateFlow("")
    val currentCode = _currentCode.asStateFlow()

    private val undoStack = mutableListOf<String>()
    private val redoStack = mutableListOf<String>()

    // Execution & Terminal
    private val _isExecuting = MutableStateFlow(false)
    val isExecuting = _isExecuting.asStateFlow()

    private val _executionResult = MutableStateFlow<ExecutionResult?>(null)
    val executionResult = _executionResult.asStateFlow()

    private val _terminalLines = MutableStateFlow<List<TerminalLine>>(
        listOf(
            TerminalLine("CodeForge Cloud Shell v3.4 (x86_64-cloud-linux)", TerminalLineType.SYSTEM),
            TerminalLine("Connected to active dev container. Type 'help' for commands.", TerminalLineType.INFO)
        )
    )
    val terminalLines = _terminalLines.asStateFlow()

    private val _terminalInput = MutableStateFlow("")
    val terminalInput = _terminalInput.asStateFlow()

    // Cloud Deployments
    private val _deployments = MutableStateFlow<List<DeploymentRecordEntity>>(emptyList())
    val deployments = _deployments.asStateFlow()

    private val _isDeploying = MutableStateFlow(false)
    val isDeploying = _isDeploying.asStateFlow()

    private val _deployProgressMessage = MutableStateFlow("")
    val deployProgressMessage = _deployProgressMessage.asStateFlow()

    private val _selectedDeployProvider = MutableStateFlow("Vercel")
    val selectedDeployProvider = _selectedDeployProvider.asStateFlow()

    private val _providerConfigs = MutableStateFlow<Map<String, ProviderConfig>>(
        mapOf(
            "Vercel" to ProviderConfig(
                providerId = "Vercel",
                apiToken = "vc_tok_9b2e48fa112d8a",
                projectName = "codeforge-app",
                targetBranch = "main",
                customDomain = "",
                buildCommand = "npm run build",
                outputDir = "dist",
                environment = "Production",
                envVars = listOf(
                    "NODE_ENV" to "production",
                    "VITE_API_URL" to "https://api.codeforge.cloud",
                    "VITE_APP_TITLE" to "CodeForge Production"
                ),
                isConnected = true
            ),
            "Netlify" to ProviderConfig(
                providerId = "Netlify",
                apiToken = "nfp_7a810cd4e9123b",
                projectName = "codeforge-web",
                targetBranch = "main",
                customDomain = "",
                buildCommand = "npm run build",
                outputDir = "build",
                environment = "Production",
                envVars = listOf(
                    "NODE_ENV" to "production",
                    "SITE_NAME" to "codeforge-edge"
                ),
                isConnected = true
            ),
            "Cloudflare" to ProviderConfig(
                providerId = "Cloudflare",
                apiToken = "cf_sec_89c3140ab212ef",
                projectName = "codeforge-workers",
                targetBranch = "main",
                customDomain = "",
                buildCommand = "npx wrangler pages deploy",
                outputDir = "public",
                environment = "Production",
                envVars = listOf(
                    "ENVIRONMENT" to "production",
                    "CF_ACCOUNT_ID" to "871b9c02"
                ),
                isConnected = true
            ),
            "Render" to ProviderConfig(
                providerId = "Render",
                apiToken = "rnd_98ab716f2120e",
                projectName = "codeforge-srv",
                targetBranch = "main",
                customDomain = "",
                buildCommand = "docker build -t app .",
                outputDir = ".",
                environment = "Production",
                envVars = listOf(
                    "PORT" to "8080",
                    "NODE_ENV" to "production"
                ),
                isConnected = true
            ),
            "AWS" to ProviderConfig(
                providerId = "AWS",
                apiToken = "AKIA_718293B8902CF",
                projectName = "codeforge-amplify",
                targetBranch = "main",
                customDomain = "",
                buildCommand = "npm run build",
                outputDir = "out",
                environment = "Production",
                envVars = listOf(
                    "AWS_REGION" to "us-east-1",
                    "STAGE" to "prod"
                ),
                isConnected = true
            ),
            "GitHubPages" to ProviderConfig(
                providerId = "GitHubPages",
                apiToken = "ghp_92019481230182",
                projectName = "codeforge-docs",
                targetBranch = "gh-pages",
                customDomain = "",
                buildCommand = "npm run build",
                outputDir = "docs",
                environment = "Production",
                envVars = emptyList(),
                isConnected = true
            ),
            "Firebase" to ProviderConfig(
                providerId = "Firebase",
                apiToken = "1//04ab9123081ce",
                projectName = "codeforge-firebase",
                targetBranch = "main",
                customDomain = "",
                buildCommand = "firebase deploy --only hosting",
                outputDir = "public",
                environment = "Production",
                envVars = listOf(
                    "GCP_PROJECT" to "codeforge-cloud-prod"
                ),
                isConnected = true
            )
        )
    )
    val providerConfigs = _providerConfigs.asStateFlow()

    private val _pipelineStages = MutableStateFlow<List<PipelineStageInfo>>(emptyList())
    val pipelineStages = _pipelineStages.asStateFlow()

    private val _deployLiveLogs = MutableStateFlow<List<String>>(emptyList())
    val deployLiveLogs = _deployLiveLogs.asStateFlow()

    private var activeDeployJob: Job? = null

    // Git
    private val _gitCommits = MutableStateFlow<List<GitCommitEntity>>(emptyList())
    val gitCommits = _gitCommits.asStateFlow()

    // AI Assistant
    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading = _isAiLoading.asStateFlow()

    private val _aiResult = MutableStateFlow<AiResponseResult?>(null)
    val aiResult = _aiResult.asStateFlow()

    private val _showAiDialog = MutableStateFlow(false)
    val showAiDialog = _showAiDialog.asStateFlow()

    // Search and Replace
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _replaceQuery = MutableStateFlow("")
    val replaceQuery = _replaceQuery.asStateFlow()

    private val _isSearchVisible = MutableStateFlow(false)
    val isSearchVisible = _isSearchVisible.asStateFlow()

    // Sync notification banner
    private val _syncStatus = MutableStateFlow("All devices in sync")
    val syncStatus = _syncStatus.asStateFlow()

    // --- VS Code Layout & Functionality State ---
    private val _activeActivity = MutableStateFlow(com.example.ui.vscode.VsCodeActivity.EXPLORER)
    val activeActivity = _activeActivity.asStateFlow()

    private val _isSidebarOpen = MutableStateFlow(true)
    val isSidebarOpen = _isSidebarOpen.asStateFlow()

    private val _activePanelTab = MutableStateFlow(com.example.ui.vscode.VsCodeBottomTab.TERMINAL)
    val activePanelTab = _activePanelTab.asStateFlow()

    private val _isPanelOpen = MutableStateFlow(false)
    val isPanelOpen = _isPanelOpen.asStateFlow()

    private val _isPanelMaximized = MutableStateFlow(false)
    val isPanelMaximized = _isPanelMaximized.asStateFlow()

    private val _isCommandPaletteOpen = MutableStateFlow(false)
    val isCommandPaletteOpen = _isCommandPaletteOpen.asStateFlow()

    private val _commandPaletteQuery = MutableStateFlow("")
    val commandPaletteQuery = _commandPaletteQuery.asStateFlow()

    private val _breakpoints = MutableStateFlow<Set<Int>>(emptySet())
    val breakpoints = _breakpoints.asStateFlow()

    private val _crossFileSearchQuery = MutableStateFlow("")
    val crossFileSearchQuery = _crossFileSearchQuery.asStateFlow()

    private val _crossFileSearchResults = MutableStateFlow<List<com.example.ui.vscode.VsCodeSearchResult>>(emptyList())
    val crossFileSearchResults = _crossFileSearchResults.asStateFlow()

    private val _extensions = MutableStateFlow(
        listOf(
            com.example.ui.vscode.VsCodeExtension(
                id = "ms-python.python",
                name = "Python",
                publisher = "Microsoft",
                version = "v2024.12.0",
                description = "IntelliSense, syntax highlighting, formatting, and debugging for Python.",
                rating = 4.8f,
                downloads = "120M+",
                isEnabled = true
            ),
            com.example.ui.vscode.VsCodeExtension(
                id = "esbenp.prettier-vscode",
                name = "Prettier - Code Formatter",
                publisher = "Prettier",
                version = "v10.4.0",
                description = "Code formatter using prettier for JS, TS, HTML, CSS, React.",
                rating = 4.7f,
                downloads = "45M+",
                isEnabled = true
            ),
            com.example.ui.vscode.VsCodeExtension(
                id = "dbaeumer.vscode-eslint",
                name = "ESLint",
                publisher = "Microsoft",
                version = "v3.0.10",
                description = "Integrates ESLint JavaScript into VS Code.",
                rating = 4.6f,
                downloads = "34M+",
                isEnabled = true
            ),
            com.example.ui.vscode.VsCodeExtension(
                id = "google.gemini-copilot",
                name = "GitHub Copilot (Gemini AI)",
                publisher = "GitHub & Google",
                version = "v2.5.1",
                description = "AI code generation, completions, debugging, and chat assistant.",
                rating = 4.9f,
                downloads = "18M+",
                isEnabled = true
            ),
            com.example.ui.vscode.VsCodeExtension(
                id = "bradlc.vscode-tailwindcss",
                name = "Tailwind CSS IntelliSense",
                publisher = "Tailwind Labs",
                version = "v0.12.0",
                description = "Intelligent Tailwind CSS tooling for VS Code.",
                rating = 4.9f,
                downloads = "12M+",
                isEnabled = true
            ),
            com.example.ui.vscode.VsCodeExtension(
                id = "ms-azuretools.vscode-docker",
                name = "Docker",
                publisher = "Microsoft",
                version = "v1.29.0",
                description = "Makes it easy to create, manage, and debug containerized applications.",
                rating = 4.6f,
                downloads = "28M+",
                isEnabled = true
            ),
            com.example.ui.vscode.VsCodeExtension(
                id = "eamodio.gitlens",
                name = "GitLens — Git supercharged",
                publisher = "GitKraken",
                version = "v15.0.0",
                description = "Supercharge Git within VS Code — Visualize code authorship at a glance.",
                rating = 4.8f,
                downloads = "31M+",
                isEnabled = true
            )
        )
    )
    val extensions = _extensions.asStateFlow()

    private val _editorSettings = MutableStateFlow(com.example.ui.vscode.EditorSettings())
    val editorSettings = _editorSettings.asStateFlow()

    private val _cursorLine = MutableStateFlow(1)
    val cursorLine = _cursorLine.asStateFlow()

    private val _cursorCol = MutableStateFlow(1)
    val cursorCol = _cursorCol.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeDefaultDataIfEmpty()
            // Observe projects and select first
            projects.collect { list ->
                if (list.isNotEmpty() && _selectedProjectId.value == null) {
                    selectProject(list.first().id)
                }
            }
        }
    }

    fun selectProject(projectId: Long) {
        _selectedProjectId.value = projectId
        viewModelScope.launch {
            val list = projects.value
            val proj = list.firstOrNull { it.id == projectId }
            _selectedProject.value = proj

            // Collect files
            repository.getFilesForProject(projectId).collect { files ->
                _projectFiles.value = files
                vcsManager.initializeForProject(proj, files)
                if (files.isNotEmpty()) {
                    if (_activeFileId.value == null || files.none { it.id == _activeFileId.value }) {
                        val entry = files.firstOrNull { it.isEntry } ?: files.first()
                        openFile(entry)
                    } else {
                        val current = files.firstOrNull { it.id == _activeFileId.value }
                        if (current != null && current.content != _currentCode.value) {
                            _activeFile.value = current
                            _currentCode.value = current.content
                        }
                    }
                }
            }
        }

        // Collect Git Commits
        viewModelScope.launch {
            repository.getCommitsForProject(projectId).collect {
                _gitCommits.value = it
            }
        }

        // Collect Deployments
        viewModelScope.launch {
            repository.getDeploymentsForProject(projectId).collect {
                _deployments.value = it
            }
        }
    }

    private val _fileHistory = mutableListOf<Long>()
    private var _historyIndex = -1

    private val _isQuickOpenOpen = MutableStateFlow(false)
    val isQuickOpenOpen = _isQuickOpenOpen.asStateFlow()

    fun openQuickOpen() {
        _isQuickOpenOpen.value = true
    }

    fun closeQuickOpen() {
        _isQuickOpenOpen.value = false
    }

    fun openFile(file: CodeFileEntity) {
        _activeFileId.value = file.id
        _activeFile.value = file
        _currentCode.value = file.content
        undoStack.clear()
        redoStack.clear()

        val tabs = _openTabIds.value.toMutableList()
        if (!tabs.contains(file.id)) {
            tabs.add(file.id)
            _openTabIds.value = tabs
        }

        if (_historyIndex == -1 || (_historyIndex < _fileHistory.size && _fileHistory[_historyIndex] != file.id)) {
            while (_fileHistory.size > _historyIndex + 1) {
                _fileHistory.removeAt(_fileHistory.size - 1)
            }
            _fileHistory.add(file.id)
            _historyIndex = _fileHistory.size - 1
        }
    }

    fun goBackFile() {
        if (_historyIndex > 0) {
            _historyIndex--
            val id = _fileHistory[_historyIndex]
            val f = _projectFiles.value.firstOrNull { it.id == id }
            if (f != null) {
                _activeFileId.value = f.id
                _activeFile.value = f
                _currentCode.value = f.content
            }
        }
    }

    fun goForwardFile() {
        if (_historyIndex < _fileHistory.size - 1) {
            _historyIndex++
            val id = _fileHistory[_historyIndex]
            val f = _projectFiles.value.firstOrNull { it.id == id }
            if (f != null) {
                _activeFileId.value = f.id
                _activeFile.value = f
                _currentCode.value = f.content
            }
        }
    }

    fun saveActiveFile() {
        val file = _activeFile.value ?: return
        viewModelScope.launch {
            repository.updateFileContent(file.id, _currentCode.value)
            _syncStatus.value = "✓ File ${file.name} saved"
        }
    }

    fun saveAllFiles() {
        saveActiveFile()
        _syncStatus.value = "✓ All workspace files saved"
    }

    fun stopDebugging() {
        _isExecuting.value = false
        val currentLines = _terminalLines.value.toMutableList()
        currentLines.add(TerminalLine("[Debugger] Stopped execution session.", TerminalLineType.INFO))
        _terminalLines.value = currentLines
    }

    fun closeTab(fileId: Long) {
        val tabs = _openTabIds.value.toMutableList()
        tabs.remove(fileId)
        _openTabIds.value = tabs

        if (_activeFileId.value == fileId) {
            val nextId = tabs.lastOrNull()
            if (nextId != null) {
                val nextFile = _projectFiles.value.firstOrNull { it.id == nextId }
                if (nextFile != null) openFile(nextFile)
            } else {
                _activeFileId.value = null
                _activeFile.value = null
                _currentCode.value = ""
            }
        }
    }

    fun onCodeChanged(newCode: String) {
        if (newCode != _currentCode.value) {
            if (_currentCode.value.isNotEmpty()) {
                undoStack.add(_currentCode.value)
                if (undoStack.size > 50) undoStack.removeAt(0)
            }
            redoStack.clear()
            _currentCode.value = newCode

            // Auto-save debounced & notify VCS real-time
            val fileId = _activeFileId.value
            if (fileId != null) {
                val updatedFiles = _projectFiles.value.map {
                    if (it.id == fileId) it.copy(content = newCode) else it
                }
                vcsManager.onFilesUpdated(updatedFiles)
                viewModelScope.launch {
                    repository.updateFileContent(fileId, newCode)
                }
            }
        }
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val prev = undoStack.removeAt(undoStack.lastIndex)
            redoStack.add(_currentCode.value)
            _currentCode.value = prev
            val fileId = _activeFileId.value
            if (fileId != null) {
                viewModelScope.launch { repository.updateFileContent(fileId, prev) }
            }
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.removeAt(redoStack.lastIndex)
            undoStack.add(_currentCode.value)
            _currentCode.value = next
            val fileId = _activeFileId.value
            if (fileId != null) {
                viewModelScope.launch { repository.updateFileContent(fileId, next) }
            }
        }
    }

    fun insertSymbol(symbol: String) {
        val current = _currentCode.value
        onCodeChanged(current + symbol)
    }

    fun setNavTab(tab: AppNavTab) {
        _currentNavTab.value = tab
    }

    fun setEditorTheme(theme: EditorThemeMode) {
        _editorTheme.value = theme
    }

    fun setFontSize(size: Float) {
        _fontSize.value = size.coerceIn(10f, 28f)
    }

    fun toggleWordWrap() {
        _wordWrap.value = !_wordWrap.value
    }

    fun toggleLineNumbers() {
        _showLineNumbers.value = !_showLineNumbers.value
    }

    fun toggleSearch() {
        _isSearchVisible.value = !_isSearchVisible.value
    }

    fun setSearchQuery(q: String) {
        _searchQuery.value = q
    }

    fun setReplaceQuery(q: String) {
        _replaceQuery.value = q
    }

    fun replaceAll() {
        if (_searchQuery.value.isNotEmpty()) {
            val replaced = _currentCode.value.replace(_searchQuery.value, _replaceQuery.value)
            onCodeChanged(replaced)
        }
    }

    // New Project & File management
    fun createProject(name: String, description: String, language: SupportedLanguage) {
        viewModelScope.launch {
            val newId = repository.createProject(name, description, language)
            selectProject(newId)
        }
    }

    fun createNewFile(name: String) {
        val projId = _selectedProjectId.value ?: return
        viewModelScope.launch {
            val fileId = repository.addFile(projId, name)
            delay(100)
            val file = _projectFiles.value.firstOrNull { it.id == fileId }
            if (file != null) openFile(file)
        }
    }

    fun deleteFile(fileId: Long) {
        viewModelScope.launch {
            closeTab(fileId)
            repository.deleteFile(fileId)
        }
    }

    fun renameFile(fileId: Long, newName: String) {
        viewModelScope.launch {
            repository.renameFile(fileId, newName)
            if (_activeFileId.value == fileId) {
                val updated = _activeFile.value?.copy(
                    name = newName,
                    language = SupportedLanguage.fromFileName(newName)
                )
                if (updated != null) {
                    _activeFile.value = updated
                }
            }
        }
    }

    fun deleteProject(projectId: Long) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
            if (_selectedProjectId.value == projectId) {
                _selectedProjectId.value = null
                _selectedProject.value = null
                _projectFiles.value = emptyList()
                _activeFileId.value = null
                _activeFile.value = null
                _currentCode.value = ""
                _openTabIds.value = emptyList()
                val remaining = projects.value.filter { it.id != projectId }
                if (remaining.isNotEmpty()) {
                    selectProject(remaining.first().id)
                }
            }
        }
    }

    // Run Code
    fun runActiveFile() {
        val file = _activeFile.value ?: return
        viewModelScope.launch {
            _isExecuting.value = true
            val result = CodeRunner.executeFile(file)
            _executionResult.value = result
            _isExecuting.value = false

            // Also write to terminal
            val lines = _terminalLines.value.toMutableList()
            lines.add(TerminalLine("$ run ${file.name}", TerminalLineType.INPUT))
            result.output.lines().forEach {
                lines.add(TerminalLine(it, TerminalLineType.OUTPUT))
            }
            _terminalLines.value = lines
        }
    }

    // Terminal
    fun setTerminalInput(input: String) {
        _terminalInput.value = input
    }

    fun submitTerminalCommand() {
        val cmd = _terminalInput.value
        _terminalInput.value = ""
        val trimmed = cmd.trim()
        if (trimmed.lowercase() == "clear") {
            _terminalLines.value = listOf(
                TerminalLine("CodeForge Cloud Shell v3.4", TerminalLineType.SYSTEM)
            )
            return
        }

        val parts = trimmed.split("\\s+".toRegex()).filter { it.isNotBlank() }
        val root = parts.firstOrNull()?.lowercase() ?: ""

        if (root == "git") {
            val list = _terminalLines.value.toMutableList()
            list.add(TerminalLine("$ $cmd", TerminalLineType.INPUT))
            val subCmd = parts.getOrNull(1)?.lowercase() ?: ""
            when (subCmd) {
                "status" -> {
                    val isShort = parts.contains("-s") || parts.contains("--porcelain") || parts.contains("-sb")
                    list.add(TerminalLine("On branch ${vcsManager.currentBranch.value}", TerminalLineType.INFO))
                    val porcelain = vcsManager.getPorcelainStatusString()
                    if (isShort) {
                        if (porcelain.isBlank()) {
                            list.add(TerminalLine("## ${vcsManager.currentBranch.value}", TerminalLineType.SUCCESS))
                        } else {
                            porcelain.lines().filter { it.isNotBlank() }.forEach { line ->
                                val type = when {
                                    line.startsWith("??") -> TerminalLineType.ERROR
                                    line.startsWith("A") || line.startsWith("M ") -> TerminalLineType.SUCCESS
                                    else -> TerminalLineType.OUTPUT
                                }
                                list.add(TerminalLine(line, type))
                            }
                        }
                    } else {
                        if (porcelain.isBlank()) {
                            list.add(TerminalLine("nothing to commit, working tree clean", TerminalLineType.SUCCESS))
                        } else {
                            val staged = vcsManager.stagedChanges.value
                            val unstaged = vcsManager.unstagedChanges.value
                            if (staged.isNotEmpty()) {
                                list.add(TerminalLine("Changes to be committed:", TerminalLineType.INFO))
                                list.add(TerminalLine("  (use \"git restore --staged <file>...\" to unstage)", TerminalLineType.OUTPUT))
                                staged.forEach {
                                    list.add(TerminalLine("\t${it.status.label.lowercase()}:   ${it.filePath}", TerminalLineType.SUCCESS))
                                }
                            }
                            if (unstaged.isNotEmpty()) {
                                list.add(TerminalLine("Changes not staged for commit:", TerminalLineType.INFO))
                                list.add(TerminalLine("  (use \"git add <file>...\" to update what will be committed)", TerminalLineType.OUTPUT))
                                unstaged.forEach {
                                    val prefix = if (it.status == GitStatusType.UNTRACKED) "untracked" else it.status.label.lowercase()
                                    list.add(TerminalLine("\t$prefix:   ${it.filePath}", TerminalLineType.ERROR))
                                }
                            }
                        }
                    }
                }
                "branch" -> {
                    val branchArg = parts.getOrNull(2)
                    if (branchArg != null) {
                        createGitBranch(branchArg)
                        list.add(TerminalLine("Created branch '$branchArg'", TerminalLineType.SUCCESS))
                    } else {
                        vcsManager.branches.value.forEach { b ->
                            val isCurr = b == vcsManager.currentBranch.value
                            val prefix = if (isCurr) "* " else "  "
                            list.add(TerminalLine("$prefix$b", if (isCurr) TerminalLineType.SUCCESS else TerminalLineType.OUTPUT))
                        }
                    }
                }
                "checkout" -> {
                    val isNew = parts.getOrNull(2) == "-b"
                    val bName = if (isNew) parts.getOrNull(3) else parts.getOrNull(2)
                    if (bName != null) {
                        if (isNew) createGitBranch(bName) else selectGitBranch(bName)
                        list.add(TerminalLine("Switched to ${if (isNew) "a new " else ""}branch '$bName'", TerminalLineType.SUCCESS))
                    } else {
                        list.add(TerminalLine("Usage: git checkout [-b] <branch>", TerminalLineType.ERROR))
                    }
                }
                "add" -> {
                    val target = parts.getOrNull(2)
                    if (target == "." || target == "-A" || target == "--all") {
                        stageAllFiles()
                        list.add(TerminalLine("Staged all changed files (${vcsManager.stagedChanges.value.size} files)", TerminalLineType.SUCCESS))
                    } else if (target != null) {
                        val file = _projectFiles.value.firstOrNull { it.name.equals(target, ignoreCase = true) || it.path.equals(target, ignoreCase = true) }
                        if (file != null) {
                            stageFile(file.id)
                            list.add(TerminalLine("Staged '${file.name}'", TerminalLineType.SUCCESS))
                        } else {
                            list.add(TerminalLine("fatal: pathspec '$target' did not match any files", TerminalLineType.ERROR))
                        }
                    } else {
                        list.add(TerminalLine("Usage: git add <file> | git add -A", TerminalLineType.ERROR))
                    }
                }
                "reset" -> {
                    unstageAllFiles()
                    list.add(TerminalLine("Unstaged all files", TerminalLineType.SUCCESS))
                }
                "commit" -> {
                    val mIndex = parts.indexOf("-m")
                    val message = if (mIndex != -1 && mIndex + 1 < parts.size) {
                        trimmed.substringAfter("-m").trim().trim('"', '\'')
                    } else {
                        "Update files"
                    }
                    commitChanges(message)
                    list.add(TerminalLine("[${vcsManager.currentBranch.value}] $message", TerminalLineType.SUCCESS))
                }
                "diff" -> {
                    val changes = vcsManager.fileChanges.value
                    if (changes.isEmpty()) {
                        list.add(TerminalLine("No changes to diff", TerminalLineType.INFO))
                    } else {
                        changes.forEach { c ->
                            val diffLines = vcsManager.getDiffForFile(c.fileId, _projectFiles.value)
                            diffLines.forEach { dl ->
                                val lineType = when (dl.type) {
                                    DiffLineType.ADDED -> TerminalLineType.SUCCESS
                                    DiffLineType.DELETED -> TerminalLineType.ERROR
                                    DiffLineType.HEADER -> TerminalLineType.INFO
                                    DiffLineType.UNCHANGED -> TerminalLineType.OUTPUT
                                }
                                list.add(TerminalLine(dl.text, lineType))
                            }
                        }
                    }
                }
                "log" -> {
                    val commits = _gitCommits.value
                    if (commits.isEmpty()) {
                        list.add(TerminalLine("No commits yet on branch ${vcsManager.currentBranch.value}", TerminalLineType.INFO))
                    } else {
                        commits.take(10).forEach { c ->
                            list.add(TerminalLine("commit ${c.commitHash} (${c.branch})", TerminalLineType.INFO))
                            list.add(TerminalLine("Author: Developer <dev@codeforge.cloud>", TerminalLineType.OUTPUT))
                            list.add(TerminalLine("    ${c.message}", TerminalLineType.OUTPUT))
                        }
                    }
                }
                "push" -> {
                    list.add(TerminalLine("Pushing to origin/${vcsManager.currentBranch.value}...", TerminalLineType.INFO))
                    list.add(TerminalLine("✓ Everything up-to-date", TerminalLineType.SUCCESS))
                }
                else -> {
                    val newLines = CodeRunner.handleCommand(cmd, _selectedProject.value, _projectFiles.value)
                    list.addAll(newLines)
                }
            }
            _terminalLines.value = list
            return
        }

        val newLines = CodeRunner.handleCommand(cmd, _selectedProject.value, _projectFiles.value)
        val list = _terminalLines.value.toMutableList()
        list.addAll(newLines)
        _terminalLines.value = list
    }

    // Cloud Deployment & Provider Management
    fun selectDeployProvider(providerId: String) {
        _selectedDeployProvider.value = providerId
    }

    fun updateProviderConfig(updatedConfig: ProviderConfig) {
        val current = _providerConfigs.value.toMutableMap()
        current[updatedConfig.providerId] = updatedConfig
        _providerConfigs.value = current
    }

    fun addEnvVar(providerId: String, key: String, value: String) {
        if (key.isBlank()) return
        val current = _providerConfigs.value.toMutableMap()
        val conf = current[providerId] ?: ProviderConfig(providerId = providerId)
        val filtered = conf.envVars.filter { it.first != key } + Pair(key.trim(), value.trim())
        current[providerId] = conf.copy(envVars = filtered)
        _providerConfigs.value = current
    }

    fun removeEnvVar(providerId: String, key: String) {
        val current = _providerConfigs.value.toMutableMap()
        val conf = current[providerId] ?: return
        val filtered = conf.envVars.filter { it.first != key }
        current[providerId] = conf.copy(envVars = filtered)
        _providerConfigs.value = current
    }

    fun testProviderConnection(providerId: String) {
        viewModelScope.launch {
            val current = _providerConfigs.value.toMutableMap()
            val conf = current[providerId] ?: ProviderConfig(providerId = providerId)
            delay(400)
            current[providerId] = conf.copy(
                isConnected = true,
                lastTestedMs = System.currentTimeMillis()
            )
            _providerConfigs.value = current
        }
    }

    fun cancelDeployment() {
        activeDeployJob?.cancel()
        _isDeploying.value = false
        _deployProgressMessage.value = "Deployment cancelled by user."
        val logs = _deployLiveLogs.value.toMutableList()
        logs.add("[ERROR] Deployment interrupted: Aborted by operator.")
        _deployLiveLogs.value = logs
    }

    fun rollbackDeployment(record: DeploymentRecordEntity) {
        val proj = _selectedProject.value ?: return
        viewModelScope.launch {
            _isDeploying.value = true
            _deployProgressMessage.value = "Initiating instant rollback to release #${record.id}..."
            delay(500)
            _deployProgressMessage.value = "Reverting CDN edge routing to deployment snapshot..."
            delay(600)

            repository.recordDeployment(
                projectId = proj.id,
                provider = record.provider,
                status = "Deployed",
                deployUrl = record.deployUrl,
                logs = "✓ Rollback executed successfully\n✓ Target release #${record.id} restored\n✓ Edge routing repointed to ${record.deployUrl}\n✓ All health endpoints verified."
            )

            _isDeploying.value = false
            _deployProgressMessage.value = "Rollback complete: ${record.deployUrl} restored."
        }
    }

    fun triggerDeployment(provider: String, customDomain: String = "", branch: String = "") {
        val proj = _selectedProject.value ?: return
        val config = _providerConfigs.value[provider] ?: ProviderConfig(providerId = provider)
        val targetBranch = if (branch.isNotBlank()) branch else (if (config.targetBranch.isNotBlank()) config.targetBranch else proj.gitBranch)

        val initialStages = listOf(
            PipelineStageInfo(1, "Pre-flight & Workspace Verification", "Scanning git branch '$targetBranch' and project tree", DeploymentStageStatus.IN_PROGRESS),
            PipelineStageInfo(2, "Dependency Resolution & Build", "Executing '${config.buildCommand}' with tree-shaking", DeploymentStageStatus.PENDING),
            PipelineStageInfo(3, "Cloud Provider Authentication", "Verifying token & establishing TLS pipe to $provider", DeploymentStageStatus.PENDING),
            PipelineStageInfo(4, "Global Edge CDN Asset Distribution", "Uploading blobs to 32 distributed Edge PoPs", DeploymentStageStatus.PENDING),
            PipelineStageInfo(5, "Health Check & DNS Verification", "Validating SSL certificate and HTTP 200 responses", DeploymentStageStatus.PENDING)
        )
        _pipelineStages.value = initialStages

        val logs = mutableListOf(
            "[INIT] Starting production deployment pipeline to $provider...",
            "[INFO] Project: ${proj.name} | Branch: $targetBranch | Environment: ${config.environment}",
            "[INFO] Build Command: '${config.buildCommand}' | Output Directory: '${config.outputDir}'",
            "[STAGE 1/5] Running pre-flight checks..."
        )
        _deployLiveLogs.value = logs.toList()

        activeDeployJob?.cancel()
        activeDeployJob = viewModelScope.launch {
            _isDeploying.value = true
            _deployProgressMessage.value = "Stage 1/5: Pre-flight checks on branch '$targetBranch'..."

            delay(500)
            logs.add("✓ Git state clean. HEAD commit: ${vcsManager.currentBranch.value} (${proj.primaryLanguage.displayName})")
            logs.add("✓ Packaged ${_projectFiles.value.size} project source files into tarball bundle.")
            logs.add("[STAGE 2/5] Compiling and packaging bundle...")
            _deployLiveLogs.value = logs.toList()
            _pipelineStages.value = _pipelineStages.value.mapIndexed { idx, s ->
                when (idx) {
                    0 -> s.copy(status = DeploymentStageStatus.COMPLETED)
                    1 -> s.copy(status = DeploymentStageStatus.IN_PROGRESS)
                    else -> s
                }
            }
            _deployProgressMessage.value = "Stage 2/5: Building bundle with ${config.buildCommand}..."

            delay(700)
            logs.add("✓ Resolved environment variables: ${config.envVars.size} keys injected securely.")
            logs.add("✓ Bundle optimization: Generated ${config.outputDir}/index.html (${(_projectFiles.value.sumOf { it.content.length } / 1024) + 12} KB)")
            logs.add("✓ Source maps & asset hashes created successfully.")
            logs.add("[STAGE 3/5] Authenticating with $provider Cloud API...")
            _deployLiveLogs.value = logs.toList()
            _pipelineStages.value = _pipelineStages.value.mapIndexed { idx, s ->
                when (idx) {
                    1 -> s.copy(status = DeploymentStageStatus.COMPLETED)
                    2 -> s.copy(status = DeploymentStageStatus.IN_PROGRESS)
                    else -> s
                }
            }
            _deployProgressMessage.value = "Stage 3/5: Authenticating with $provider Cloud API..."

            delay(600)
            logs.add("✓ Authenticated with $provider API Token (Valid until 2027).")
            logs.add("✓ Created remote deployment identifier: dep_${System.currentTimeMillis().toString(36)}.")
            logs.add("[STAGE 4/5] Distributing assets to Global Edge CDN...")
            _deployLiveLogs.value = logs.toList()
            _pipelineStages.value = _pipelineStages.value.mapIndexed { idx, s ->
                when (idx) {
                    2 -> s.copy(status = DeploymentStageStatus.COMPLETED)
                    3 -> s.copy(status = DeploymentStageStatus.IN_PROGRESS)
                    else -> s
                }
            }
            _deployProgressMessage.value = "Stage 4/5: Replicating to 32 Global Edge CDN points..."

            delay(800)
            logs.add("✓ Replicated to Edge nodes: SFO, IAD, FRA, NRT, LHR, SIN, SYD.")
            logs.add("✓ Global Anycast routing updated in 38ms.")
            logs.add("[STAGE 5/5] Performing SSL & health check probe...")
            _deployLiveLogs.value = logs.toList()
            _pipelineStages.value = _pipelineStages.value.mapIndexed { idx, s ->
                when (idx) {
                    3 -> s.copy(status = DeploymentStageStatus.COMPLETED)
                    4 -> s.copy(status = DeploymentStageStatus.IN_PROGRESS)
                    else -> s
                }
            }
            _deployProgressMessage.value = "Stage 5/5: SSL Handshake & Health Probe verification..."

            delay(600)
            val effectiveDomain = if (customDomain.isNotBlank()) {
                if (customDomain.startsWith("http://") || customDomain.startsWith("https://")) customDomain
                else "https://$customDomain"
            } else if (config.customDomain.isNotBlank()) {
                if (config.customDomain.startsWith("http://") || config.customDomain.startsWith("https://")) config.customDomain
                else "https://${config.customDomain}"
            } else {
                val cleanName = proj.name.lowercase().replace("[^a-z0-9]".toRegex(), "-").trim('-')
                val suffix = AVAILABLE_CLOUD_PROVIDERS.firstOrNull { it.id.equals(provider, ignoreCase = true) }?.defaultDomainSuffix ?: "dev"
                "https://$cleanName-${(100..999).random()}.$suffix"
            }

            logs.add("✓ TLS 1.3 Certificate issued & verified.")
            logs.add("✓ GET $effectiveDomain/health -> HTTP 200 OK (22ms)")
            logs.add("✓ Release LIVE at: $effectiveDomain")
            _deployLiveLogs.value = logs.toList()
            _pipelineStages.value = _pipelineStages.value.map { it.copy(status = DeploymentStageStatus.COMPLETED) }

            // Write to local terminal too
            val termList = _terminalLines.value.toMutableList()
            termList.add(TerminalLine("$ deploy --provider $provider --branch $targetBranch", TerminalLineType.INPUT))
            termList.add(TerminalLine("✓ Deployed successfully to $provider: $effectiveDomain", TerminalLineType.SUCCESS))
            _terminalLines.value = termList

            repository.recordDeployment(
                projectId = proj.id,
                provider = provider,
                status = "Deployed",
                deployUrl = effectiveDomain,
                logs = logs.joinToString("\n")
            )

            _isDeploying.value = false
            _deployProgressMessage.value = "Production release complete: $effectiveDomain"
        }
    }

    // Git & Version Control Actions
    fun commitChanges(message: String) {
        val projId = _selectedProjectId.value ?: return
        val commitInfo = vcsManager.commit(message, _projectFiles.value)
        viewModelScope.launch {
            repository.createGitCommit(projId, message, commitInfo.branch)
            _syncStatus.value = "✓ Committed [${commitInfo.commitHash}] $message"
        }
    }

    fun stageFile(fileId: Long) {
        vcsManager.stageFile(fileId, _projectFiles.value)
    }

    fun unstageFile(fileId: Long) {
        vcsManager.unstageFile(fileId, _projectFiles.value)
    }

    fun stageAllFiles() {
        vcsManager.stageAll(_projectFiles.value)
    }

    fun unstageAllFiles() {
        vcsManager.unstageAll(_projectFiles.value)
    }

    fun discardFileChanges(fileId: Long) {
        val original = vcsManager.discardChanges(fileId, _projectFiles.value)
        if (original != null) {
            viewModelScope.launch {
                repository.updateFileContent(fileId, original)
                if (_activeFileId.value == fileId) {
                    _currentCode.value = original
                }
            }
        }
    }

    fun selectGitBranch(branchName: String) {
        vcsManager.switchBranch(branchName)
        val proj = _selectedProject.value ?: return
        viewModelScope.launch {
            repository.updateProjectBranch(proj.id, branchName)
            _selectedProject.value = proj.copy(gitBranch = branchName)
        }
    }

    fun createGitBranch(branchName: String) {
        vcsManager.createBranch(branchName)
        selectGitBranch(branchName)
    }

    fun viewFileDiff(change: GitFileChange) {
        _diffFileChange.value = change
        _currentDiffLines.value = vcsManager.getDiffForFile(change.fileId, _projectFiles.value)
    }

    fun dismissFileDiff() {
        _diffFileChange.value = null
        _currentDiffLines.value = emptyList()
    }

    fun getPorcelainStatus(): String {
        return vcsManager.getPorcelainStatusString()
    }

    // AI Assistant
    fun openAiDialog() {
        _showAiDialog.value = true
    }

    fun closeAiDialog() {
        _showAiDialog.value = false
    }

    fun requestAiAction(
        task: AiTaskType,
        targetLanguage: SupportedLanguage? = null,
        customInstruction: String = ""
    ) {
        val file = _activeFile.value ?: return
        viewModelScope.launch {
            _isAiLoading.value = true
            val result = AiAssistantService.executeTask(
                task = task,
                language = file.language,
                code = _currentCode.value,
                targetLanguage = targetLanguage,
                customInstruction = customInstruction
            )
            _aiResult.value = result
            _isAiLoading.value = false
        }
    }

    fun applyAiCode(newCode: String) {
        onCodeChanged(newCode)
        closeAiDialog()
    }

    // Cross-Device Sync
    fun triggerCloudSync() {
        viewModelScope.launch {
            _syncStatus.value = "Syncing with cloud edge nodes..."
            delay(800)
            _syncStatus.value = "✓ Synced to 4 connected devices just now"
        }
    }

    // --- VS Code Operations ---
    fun selectActivity(activity: com.example.ui.vscode.VsCodeActivity) {
        if (_activeActivity.value == activity && _isSidebarOpen.value) {
            _isSidebarOpen.value = false
        } else {
            _activeActivity.value = activity
            _isSidebarOpen.value = true
        }
    }

    fun toggleSidebar() {
        _isSidebarOpen.value = !_isSidebarOpen.value
    }

    fun closeSidebar() {
        _isSidebarOpen.value = false
    }

    fun selectPanelTab(tab: com.example.ui.vscode.VsCodeBottomTab) {
        _activePanelTab.value = tab
        _isPanelOpen.value = true
    }

    fun togglePanel() {
        _isPanelOpen.value = !_isPanelOpen.value
    }

    fun closePanel() {
        _isPanelOpen.value = false
        _isPanelMaximized.value = false
    }

    fun togglePanelMaximized() {
        _isPanelMaximized.value = !_isPanelMaximized.value
    }

    fun toggleBreakpoint(line: Int) {
        val current = _breakpoints.value.toMutableSet()
        if (current.contains(line)) {
            current.remove(line)
        } else {
            current.add(line)
        }
        _breakpoints.value = current
    }

    fun openCommandPalette() {
        _commandPaletteQuery.value = ""
        _isCommandPaletteOpen.value = true
    }

    fun closeCommandPalette() {
        _isCommandPaletteOpen.value = false
    }

    fun setCommandPaletteQuery(q: String) {
        _commandPaletteQuery.value = q
    }

    fun performCrossFileSearch(query: String) {
        _crossFileSearchQuery.value = query
        if (query.isBlank()) {
            _crossFileSearchResults.value = emptyList()
            return
        }
        val results = mutableListOf<com.example.ui.vscode.VsCodeSearchResult>()
        for (file in _projectFiles.value) {
            val lines = file.content.lines()
            for ((index, line) in lines.withIndex()) {
                val matchIdx = line.indexOf(query, ignoreCase = true)
                if (matchIdx != -1) {
                    results.add(
                        com.example.ui.vscode.VsCodeSearchResult(
                            fileId = file.id,
                            fileName = file.name,
                            lineNumber = index + 1,
                            lineText = line.trim(),
                            matchStart = matchIdx,
                            matchEnd = matchIdx + query.length
                        )
                    )
                }
            }
        }
        _crossFileSearchResults.value = results
    }

    fun toggleExtension(id: String) {
        _extensions.value = _extensions.value.map { ext ->
            if (ext.id == id) ext.copy(isEnabled = !ext.isEnabled) else ext
        }
    }

    fun updateEditorSettings(newSettings: com.example.ui.vscode.EditorSettings) {
        _editorSettings.value = newSettings
        _fontSize.value = newSettings.fontSize
        _wordWrap.value = newSettings.wordWrap
        _showLineNumbers.value = newSettings.lineNumbers != "off"
    }

    fun updateCursorPosition(line: Int, col: Int) {
        _cursorLine.value = line
        _cursorCol.value = col
    }

    // --- Right Copilot Agent Chat Panel & Sign In States (matching user picture) ---
    private val _isRightChatSidebarOpen = MutableStateFlow(true)
    val isRightChatSidebarOpen = _isRightChatSidebarOpen.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<com.example.ui.vscode.AgentChatMessage>>(emptyList())
    val chatMessages = _chatMessages.asStateFlow()

    private val _chatInputText = MutableStateFlow("")
    val chatInputText = _chatInputText.asStateFlow()

    private val _selectedModel = MutableStateFlow("Gemini 2.5 Flash")
    val selectedModel = _selectedModel.asStateFlow()

    private val _isAgentMode = MutableStateFlow(true)
    val isAgentMode = _isAgentMode.asStateFlow()

    private val _isChatGenerating = MutableStateFlow(false)
    val isChatGenerating = _isChatGenerating.asStateFlow()

    private val _isSignedIn = MutableStateFlow(false)
    val isSignedIn = _isSignedIn.asStateFlow()

    private val _showSignInDialog = MutableStateFlow(false)
    val showSignInDialog = _showSignInDialog.asStateFlow()

    fun toggleRightChatSidebar() {
        _isRightChatSidebarOpen.value = !_isRightChatSidebarOpen.value
    }

    fun openRightChatSidebar() {
        _isRightChatSidebarOpen.value = true
    }

    fun closeRightChatSidebar() {
        _isRightChatSidebarOpen.value = false
    }

    fun updateChatInput(text: String) {
        _chatInputText.value = text
    }

    fun selectModel(model: String) {
        _selectedModel.value = model
    }

    fun toggleAgentMode() {
        _isAgentMode.value = !_isAgentMode.value
    }

    fun clearChat() {
        _chatMessages.value = emptyList()
    }

    fun openSignInDialog() {
        _showSignInDialog.value = true
    }

    fun closeSignInDialog() {
        _showSignInDialog.value = false
    }

    fun signInUser(email: String) {
        _isSignedIn.value = true
        _showSignInDialog.value = false
    }

    fun sendChatMessage(prompt: String) {
        if (prompt.isBlank()) return
        val userMsg = com.example.ui.vscode.AgentChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            isUser = true,
            message = prompt,
            timestamp = "Just now"
        )
        val currentList = _chatMessages.value.toMutableList()
        currentList.add(userMsg)
        _chatMessages.value = currentList
        _chatInputText.value = ""

        viewModelScope.launch {
            _isChatGenerating.value = true
            val file = _activeFile.value
            val response = com.example.ai.AiAssistantService.executeTask(
                task = com.example.ai.AiTaskType.SUGGEST_CODE,
                language = file?.language ?: com.example.data.model.SupportedLanguage.JAVASCRIPT,
                code = _currentCode.value,
                customInstruction = prompt
            )
            val assistantMsg = com.example.ui.vscode.AgentChatMessage(
                id = "resp_${System.currentTimeMillis()}",
                isUser = false,
                message = response.explanation.ifBlank { "I've analyzed your project and generated the solution." },
                codeBlock = if (!response.suggestedCode.isNullOrBlank()) response.suggestedCode else null,
                timestamp = "Just now"
            )
            val updated = _chatMessages.value.toMutableList()
            updated.add(assistantMsg)
            _chatMessages.value = updated
            _isChatGenerating.value = false
        }
    }
}
