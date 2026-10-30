package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class SupportedLanguage(
    val displayName: String,
    val extension: String,
    val defaultFileName: String,
    val badgeColorHex: Long
) {
    HTML("HTML", "html", "index.html", 0xFFE44D26),
    CSS("CSS", "css", "style.css", 0xFF264DE4),
    JAVASCRIPT("JavaScript", "js", "index.js", 0xFFF7DF1E),
    TYPESCRIPT("TypeScript", "ts", "app.ts", 0xFF3178C6),
    PHP("PHP", "php", "index.php", 0xFF777BB4),
    REACT("React (JS)", "jsx", "App.jsx", 0xFF61DAFB),
    PYTHON("Python", "py", "main.py", 0xFF3776AB),
    SQL("SQL", "sql", "schema.sql", 0xFFCC292B),
    C("C", "c", "main.c", 0xFF555555),
    CPP("C++", "cpp", "main.cpp", 0xFF00599C),
    CSHARP("C#", "cs", "Program.cs", 0xFF239120),
    JAVA("Java", "java", "Main.java", 0xFFB07219),
    RUBY("Ruby", "rb", "app.rb", 0xFFCC342D),
    LUA("Lua", "lua", "script.lua", 0xFF000080),
    KOTLIN("Kotlin", "kt", "Main.kt", 0xFF7F52FF),
    DART("Dart", "dart", "main.dart", 0xFF0175C2),
    REACT_NATIVE("React Native", "jsx", "App.native.jsx", 0xFF61DAFB),
    SWIFT("Swift", "swift", "ContentView.swift", 0xFFF05138),
    TERMINAL("Terminal/Shell", "sh", "deploy.sh", 0xFF4EAA25);

    companion object {
        fun fromExtension(ext: String): SupportedLanguage {
            return entries.firstOrNull { it.extension.equals(ext, ignoreCase = true) } ?: JAVASCRIPT
        }
        fun fromFileName(name: String): SupportedLanguage {
            val ext = name.substringAfterLast('.', "")
            return fromExtension(ext)
        }
    }
}

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val primaryLanguage: SupportedLanguage,
    val gitBranch: String = "main",
    val remoteUrl: String = "",
    val cloudDeploymentUrl: String = "",
    val deploymentProvider: String = "Vercel",
    val deploymentStatus: String = "Idle",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "code_files",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("projectId")]
)
data class CodeFileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val name: String,
    val path: String,
    val language: SupportedLanguage,
    val content: String,
    val isEntry: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "git_commits",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("projectId")]
)
data class GitCommitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val commitHash: String,
    val message: String,
    val author: String = "Developer <dev@codeforge.cloud>",
    val branch: String = "main",
    val filesChangedCount: Int = 1,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "deployment_records",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("projectId")]
)
data class DeploymentRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val provider: String, // Vercel, Netlify, Cloudflare Workers, AWS Lambda, Render, GitHub Pages
    val status: String,   // Queued, Building, Deployed, Failed
    val deployUrl: String,
    val environment: String = "Production",
    val buildDurationMs: Long = 0,
    val logs: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "sync_devices")
data class SyncDeviceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deviceName: String,
    val deviceType: String, // Android Phone, Tablet, Laptop, Cloud Container
    val isCurrentDevice: Boolean = false,
    val syncStatus: String = "Synced",
    val lastActive: Long = System.currentTimeMillis()
)
