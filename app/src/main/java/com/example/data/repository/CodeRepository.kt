package com.example.data.repository

import com.example.data.ProjectTemplates
import com.example.data.local.AppDatabase
import com.example.data.model.CodeFileEntity
import com.example.data.model.DeploymentRecordEntity
import com.example.data.model.GitCommitEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.SupportedLanguage
import com.example.data.model.SyncDeviceEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.UUID

class CodeRepository(private val database: AppDatabase) {
    private val projectDao = database.projectDao()
    private val codeFileDao = database.codeFileDao()
    private val gitDao = database.gitDao()
    private val deploymentDao = database.deploymentDao()
    private val syncDeviceDao = database.syncDeviceDao()

    val allProjects: Flow<List<ProjectEntity>> = projectDao.getAllProjects()
    val allDevices: Flow<List<SyncDeviceEntity>> = syncDeviceDao.getAllDevices()

    fun getFilesForProject(projectId: Long): Flow<List<CodeFileEntity>> =
        codeFileDao.getFilesForProject(projectId)

    fun getCommitsForProject(projectId: Long): Flow<List<GitCommitEntity>> =
        gitDao.getCommitsForProject(projectId)

    fun getDeploymentsForProject(projectId: Long): Flow<List<DeploymentRecordEntity>> =
        deploymentDao.getDeploymentsForProject(projectId)

    suspend fun initializeDefaultDataIfEmpty() = withContext(Dispatchers.IO) {
        val currentProjects = projectDao.getAllProjects().first()
        if (currentProjects.isEmpty()) {
            // Seed first 3 starter projects
            for (template in ProjectTemplates.templates.take(5)) {
                val projectId = projectDao.insertProject(
                    ProjectEntity(
                        name = template.title,
                        description = template.description,
                        primaryLanguage = template.language,
                        gitBranch = "main",
                        remoteUrl = "https://github.com/codeforge-cloud/${template.title.lowercase().replace(" ", "-")}.git",
                        cloudDeploymentUrl = "https://${template.title.lowercase().replace(" ", "-")}.codeforge.cloud",
                        deploymentProvider = if (template.language == SupportedLanguage.REACT) "Vercel" else "Render",
                        deploymentStatus = "Live"
                    )
                )

                // Add files
                val files = template.files.mapIndexed { index, (fileName, content) ->
                    CodeFileEntity(
                        projectId = projectId,
                        name = fileName,
                        path = "/$fileName",
                        language = SupportedLanguage.fromFileName(fileName),
                        content = content,
                        isEntry = index == 0
                    )
                }
                codeFileDao.insertFiles(files)

                // Initial git commit
                gitDao.insertCommit(
                    GitCommitEntity(
                        projectId = projectId,
                        commitHash = UUID.randomUUID().toString().take(7),
                        message = "Initial commit: scaffold ${template.title}",
                        branch = "main",
                        filesChangedCount = files.size
                    )
                )

                // Initial deployment record
                deploymentDao.insertDeployment(
                    DeploymentRecordEntity(
                        projectId = projectId,
                        provider = if (template.language == SupportedLanguage.REACT) "Vercel" else "Render",
                        status = "Deployed",
                        deployUrl = "https://${template.title.lowercase().replace(" ", "-")}.codeforge.cloud",
                        buildDurationMs = 1840,
                        logs = "Build completed successfully in 1.84s.\nCDN cache primed across 35 edge nodes.\nSSL Certificate generated."
                    )
                )
            }

            // Seed sync devices
            syncDeviceDao.insertDevices(
                listOf(
                    SyncDeviceEntity(
                        deviceName = "Android Mobile (This Device)",
                        deviceType = "Pixel / Android",
                        isCurrentDevice = true,
                        syncStatus = "Synced"
                    ),
                    SyncDeviceEntity(
                        deviceName = "MacBook Pro M3 (Workspace)",
                        deviceType = "Laptop / MacOS",
                        isCurrentDevice = false,
                        syncStatus = "Synced"
                    ),
                    SyncDeviceEntity(
                        deviceName = "iPad Pro 12.9",
                        deviceType = "Tablet / iPadOS",
                        isCurrentDevice = false,
                        syncStatus = "Synced"
                    ),
                    SyncDeviceEntity(
                        deviceName = "CodeForge Cloud Container",
                        deviceType = "Cloud Edge Daemon",
                        isCurrentDevice = false,
                        syncStatus = "Live Streaming"
                    )
                )
            )
        }
    }

    suspend fun createProject(
        name: String,
        description: String,
        language: SupportedLanguage
    ): Long = withContext(Dispatchers.IO) {
        val projectId = projectDao.insertProject(
            ProjectEntity(
                name = name,
                description = description,
                primaryLanguage = language,
                gitBranch = "main",
                remoteUrl = "https://github.com/developer/${name.lowercase().replace(" ", "-")}.git",
                cloudDeploymentUrl = "https://${name.lowercase().replace(" ", "-")}.codeforge.cloud",
                deploymentProvider = "Vercel",
                deploymentStatus = "Idle"
            )
        )

        // Find template or create default entry file
        val matchingTemplate = ProjectTemplates.templates.firstOrNull { it.language == language }
        if (matchingTemplate != null) {
            val files = matchingTemplate.files.mapIndexed { index, (fileName, content) ->
                CodeFileEntity(
                    projectId = projectId,
                    name = fileName,
                    path = "/$fileName",
                    language = SupportedLanguage.fromFileName(fileName),
                    content = content,
                    isEntry = index == 0
                )
            }
            codeFileDao.insertFiles(files)
        } else {
            codeFileDao.insertFile(
                CodeFileEntity(
                    projectId = projectId,
                    name = language.defaultFileName,
                    path = "/${language.defaultFileName}",
                    language = language,
                    content = "// Welcome to $name\n// Language: ${language.displayName}\n\n",
                    isEntry = true
                )
            )
        }

        gitDao.insertCommit(
            GitCommitEntity(
                projectId = projectId,
                commitHash = UUID.randomUUID().toString().take(7),
                message = "Initial repository creation: $name",
                branch = "main",
                filesChangedCount = 1
            )
        )

        projectId
    }

    suspend fun updateFileContent(fileId: Long, newContent: String) = withContext(Dispatchers.IO) {
        val file = codeFileDao.getFileById(fileId) ?: return@withContext
        codeFileDao.updateFile(file.copy(content = newContent, updatedAt = System.currentTimeMillis()))
    }

    suspend fun addFile(projectId: Long, fileName: String): Long = withContext(Dispatchers.IO) {
        val lang = SupportedLanguage.fromFileName(fileName)
        codeFileDao.insertFile(
            CodeFileEntity(
                projectId = projectId,
                name = fileName,
                path = "/$fileName",
                language = lang,
                content = "// New $fileName\n",
                isEntry = false
            )
        )
    }

    suspend fun renameFile(fileId: Long, newName: String) = withContext(Dispatchers.IO) {
        val file = codeFileDao.getFileById(fileId) ?: return@withContext
        val lang = SupportedLanguage.fromFileName(newName)
        codeFileDao.updateFile(
            file.copy(
                name = newName,
                path = if (file.path.startsWith("/")) "/$newName" else newName,
                language = lang,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteFile(fileId: Long) = withContext(Dispatchers.IO) {
        codeFileDao.deleteFileById(fileId)
    }

    suspend fun deleteProject(projectId: Long) = withContext(Dispatchers.IO) {
        projectDao.deleteProjectById(projectId)
    }

    suspend fun createGitCommit(
        projectId: Long,
        message: String,
        branch: String = "main"
    ) = withContext(Dispatchers.IO) {
        val commitHash = UUID.randomUUID().toString().take(7)
        gitDao.insertCommit(
            GitCommitEntity(
                projectId = projectId,
                commitHash = commitHash,
                message = message,
                branch = branch,
                filesChangedCount = 1
            )
        )
    }

    suspend fun updateProjectBranch(projectId: Long, branch: String) = withContext(Dispatchers.IO) {
        val proj = projectDao.getProjectById(projectId) ?: return@withContext
        projectDao.updateProject(proj.copy(gitBranch = branch, updatedAt = System.currentTimeMillis()))
    }

    suspend fun recordDeployment(
        projectId: Long,
        provider: String,
        status: String,
        deployUrl: String,
        logs: String
    ): Long = withContext(Dispatchers.IO) {
        val id = deploymentDao.insertDeployment(
            DeploymentRecordEntity(
                projectId = projectId,
                provider = provider,
                status = status,
                deployUrl = deployUrl,
                buildDurationMs = (800..2500).random().toLong(),
                logs = logs
            )
        )
        // Also update project entity status
        val project = projectDao.getProjectById(projectId)
        if (project != null) {
            projectDao.updateProject(
                project.copy(
                    cloudDeploymentUrl = deployUrl,
                    deploymentProvider = provider,
                    deploymentStatus = status,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
        id
    }
}
