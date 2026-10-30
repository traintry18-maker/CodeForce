package com.example.execution

import com.example.data.model.CodeFileEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.SupportedLanguage
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ExecutionResult(
    val output: String,
    val error: String? = null,
    val exitCode: Int = 0,
    val durationMs: Long = 0,
    val memoryUsageKb: Int = 0
)

data class TerminalLine(
    val text: String,
    val type: TerminalLineType = TerminalLineType.OUTPUT
)

enum class TerminalLineType {
    INPUT,
    OUTPUT,
    SUCCESS,
    ERROR,
    INFO,
    SYSTEM
}

object CodeRunner {

    suspend fun executeFile(file: CodeFileEntity): ExecutionResult {
        val startTime = System.currentTimeMillis()
        delay((60..180).random().toLong()) // simulate compilation / execution time
        val duration = System.currentTimeMillis() - startTime

        return when (file.language) {
            SupportedLanguage.JAVASCRIPT, SupportedLanguage.TYPESCRIPT -> {
                val simulatedOutput = buildString {
                    appendLine("[Runtime: Node.js v20.11.0 / V8 Engine]")
                    appendLine("Executing ${file.name}...")
                    val lines = file.content.lines()
                    var printed = false
                    for (line in lines) {
                        if (line.contains("console.log(")) {
                            val msg = line.substringAfter("console.log(").substringBeforeLast(")")
                                .trim().trim('\'', '"')
                            appendLine(msg)
                            printed = true
                        }
                    }
                    if (!printed) {
                        appendLine("Program executed with return value: undefined")
                    }
                    appendLine("Process exited with code 0 (success)")
                }
                ExecutionResult(
                    output = simulatedOutput,
                    exitCode = 0,
                    durationMs = duration,
                    memoryUsageKb = 32400
                )
            }
            SupportedLanguage.PYTHON -> {
                val simulatedOutput = buildString {
                    appendLine("[Runtime: Python 3.12.2 CPython]")
                    appendLine("Running ${file.name}...")
                    val lines = file.content.lines()
                    var printed = false
                    for (line in lines) {
                        if (line.trim().startsWith("print(")) {
                            val msg = line.substringAfter("print(").substringBeforeLast(")")
                                .trim().trim('\'', '"')
                            appendLine(msg)
                            printed = true
                        }
                    }
                    if (!printed) {
                        appendLine("=== SUMMARY METRICS ===")
                        appendLine("   dataset: CloudLatencyMonitor")
                        appendLine("     count: 9")
                        appendLine("       sum: 161.0")
                        appendLine("      mean: 17.89")
                        appendLine("   std_dev: 10.35")
                        appendLine("       min: 9.8")
                        appendLine("       max: 45.1")
                    }
                    appendLine("Execution complete: 0 warnings, 0 errors.")
                }
                ExecutionResult(
                    output = simulatedOutput,
                    exitCode = 0,
                    durationMs = duration,
                    memoryUsageKb = 18600
                )
            }
            SupportedLanguage.SQL -> {
                val simulatedOutput = buildString {
                    appendLine("[Database: PostgreSQL 16.2 Cloud Instance]")
                    appendLine("Connected to codeforge_cloud_db on port 5432 (SSL active)")
                    appendLine("---------------------------------------------------------")
                    appendLine(" language   | total_projects | deployed_count | deploy_rate_pct ")
                    appendLine("------------+----------------+----------------+-----------------")
                    appendLine(" React      |             24 |             19 |            79.2 ")
                    appendLine(" Python     |             18 |             14 |            77.8 ")
                    appendLine(" Kotlin     |             15 |             12 |            80.0 ")
                    appendLine(" TypeScript |             14 |             11 |            78.6 ")
                    appendLine(" Swift      |              9 |              6 |            66.7 ")
                    appendLine("(5 rows affected in 12.4ms)")
                }
                ExecutionResult(
                    output = simulatedOutput,
                    exitCode = 0,
                    durationMs = duration,
                    memoryUsageKb = 8400
                )
            }
            SupportedLanguage.CPP, SupportedLanguage.C -> {
                val simulatedOutput = buildString {
                    appendLine("[Compiler: Clang 18.1.0 / -O3 Optimization]")
                    appendLine("Compiling ${file.name}...")
                    appendLine("Compilation finished: 0 errors.")
                    appendLine("Running ./a.out:")
                    appendLine("----------------------------------------")
                    appendLine("🚀 CodeForge Compute Engine")
                    appendLine("Computed Matrix Sum: 4.85149e+07")
                    appendLine("Time elapsed: 3.42 ms")
                    appendLine("Process completed with exit code 0.")
                }
                ExecutionResult(
                    output = simulatedOutput,
                    exitCode = 0,
                    durationMs = duration,
                    memoryUsageKb = 4200
                )
            }
            SupportedLanguage.KOTLIN -> {
                val simulatedOutput = buildString {
                    appendLine("[Compiler: Kotlin 2.1.0 / JVM 21 Target]")
                    appendLine("🚀 Starting CodeForge Deployment Pipeline...")
                    appendLine("[STATUS] Idle: Preparing container image...")
                    appendLine("[BUILD] Progress: 20% complete")
                    appendLine("[BUILD] Progress: 60% complete")
                    appendLine("[BUILD] Progress: 100% complete")
                    appendLine("✅ [SUCCESS] Live at: https://omni-builder-api.codeforge.cloud (latency: 42ms)")
                }
                ExecutionResult(
                    output = simulatedOutput,
                    exitCode = 0,
                    durationMs = duration,
                    memoryUsageKb = 45200
                )
            }
            SupportedLanguage.REACT, SupportedLanguage.REACT_NATIVE, SupportedLanguage.HTML, SupportedLanguage.CSS -> {
                ExecutionResult(
                    output = "[Vite / Webpack Bundle Server]\nBundle ready in ${duration}ms.\nListening on http://localhost:3000\nRendering visual UI components in Preview tab.",
                    exitCode = 0,
                    durationMs = duration,
                    memoryUsageKb = 26000
                )
            }
            else -> {
                val simulatedOutput = buildString {
                    appendLine("[Runtime: ${file.language.displayName} Native Sandbox]")
                    appendLine("Executing ${file.name}...")
                    appendLine("Done. Return status code 0 (Success).")
                }
                ExecutionResult(
                    output = simulatedOutput,
                    exitCode = 0,
                    durationMs = duration,
                    memoryUsageKb = 12000
                )
            }
        }
    }

    /**
     * Builds a self-contained HTML page that can be loaded directly inside an Android WebView
     * for real-time live preview of HTML, CSS, JavaScript, and React JSX!
     */
    fun buildWebPreviewHtml(files: List<CodeFileEntity>): String {
        val htmlFile = files.firstOrNull { it.language == SupportedLanguage.HTML }
        val cssFile = files.firstOrNull { it.language == SupportedLanguage.CSS }
        val jsFile = files.firstOrNull { it.language == SupportedLanguage.JAVASCRIPT }
        val reactFile = files.firstOrNull { it.language == SupportedLanguage.REACT || it.language == SupportedLanguage.REACT_NATIVE }

        val customCss = cssFile?.content ?: ""
        val customJs = jsFile?.content ?: ""

        if (reactFile != null) {
            val reactCode = reactFile.content
                .replace("import React.*?;".toRegex(), "")
                .replace("export default ", "")
                .replace("export ", "")

            return """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <script src="https://unpkg.com/react@18/umd/react.production.min.js"></script>
                  <script src="https://unpkg.com/react-dom@18/umd/react-dom.production.min.js"></script>
                  <script src="https://unpkg.com/@babel/standalone/babel.min.js"></script>
                  <style>
                    body { margin: 0; padding: 0; background: #0D1117; color: #E6EDF3; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; }
                    $customCss
                  </style>
                </head>
                <body>
                  <div id="root"></div>
                  <script type="text/babel">
                    const { useState, useEffect, useRef } = React;
                    $reactCode
                    
                    const rootElem = document.getElementById('root');
                    if (typeof App !== 'undefined') {
                      ReactDOM.createRoot(rootElem).render(<App />);
                    } else {
                      rootElem.innerHTML = '<h3 style="color:#58A6FF;padding:20px;">CodeForge React Preview Ready</h3>';
                    }
                  </script>
                </body>
                </html>
            """.trimIndent()
        }

        if (htmlFile != null) {
            var rawHtml = htmlFile.content
            if (!rawHtml.contains("<style>") && customCss.isNotBlank()) {
                rawHtml = rawHtml.replace("</head>", "<style>$customCss</style></head>")
            }
            if (!rawHtml.contains("<script>") && customJs.isNotBlank()) {
                rawHtml = rawHtml.replace("</body>", "<script>$customJs</script></body>")
            }
            return rawHtml
        }

        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <style>
                body { margin: 0; padding: 24px; background: #0D1117; color: #E6EDF3; font-family: system-ui, sans-serif; text-align: center; }
                .card { background: #161B22; border: 1px solid #30363D; padding: 24px; border-radius: 12px; margin-top: 40px; }
                h2 { color: #58A6FF; margin-top: 0; }
                p { color: #8B949E; }
              </style>
            </head>
            <body>
              <div class="card">
                <h2>⚡ CodeForge Web Preview</h2>
                <p>Add an index.html, App.jsx, or style.css to see live interactive renders.</p>
              </div>
            </body>
            </html>
        """.trimIndent()
    }

    /**
     * Executes interactive terminal command
     */
    fun handleCommand(
        command: String,
        project: ProjectEntity?,
        files: List<CodeFileEntity>
    ): List<TerminalLine> {
        val trimmed = command.trim()
        if (trimmed.isEmpty()) return emptyList()

        val parts = trimmed.split("\\s+".toRegex())
        val cmd = parts[0].lowercase()

        val lines = mutableListOf<TerminalLine>()
        lines.add(TerminalLine("$ $trimmed", TerminalLineType.INPUT))

        when (cmd) {
            "help" -> {
                lines.add(TerminalLine("CodeForge Cloud Shell v3.4 (x86_64-cloud-linux)", TerminalLineType.INFO))
                lines.add(TerminalLine("Available commands:", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("  run [file]            Execute the current or specified file", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("  build                 Run compilation & lint bundle check", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("  deploy [provider]     Deploy to Vercel, Netlify, Render, Cloudflare", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("  git status            View modified files and current branch", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("  git commit -m <msg>   Create a git commit", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("  git push              Push local commits to cloud remote", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("  git log               Show recent commit history", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("  ls [-l]               List workspace files", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("  cat <file>            Print file content to terminal", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("  curl <url>            Simulate network HTTP request", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("  env                   List active cloud environment variables", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("  clear                 Clear terminal scrollback", TerminalLineType.OUTPUT))
            }
            "ls" -> {
                lines.add(TerminalLine("Workspace: /home/codeforge/project/${project?.name ?: "app"}", TerminalLineType.SYSTEM))
                files.forEach { f ->
                    val size = f.content.length
                    val date = SimpleDateFormat("MMM dd HH:mm", Locale.US).format(Date(f.updatedAt))
                    lines.add(TerminalLine("-rw-r--r-- 1 dev dev ${size.toString().padStart(6)} $date ${f.name}", TerminalLineType.OUTPUT))
                }
            }
            "cat" -> {
                val filename = parts.getOrNull(1)
                if (filename == null) {
                    lines.add(TerminalLine("Usage: cat <filename>", TerminalLineType.ERROR))
                } else {
                    val file = files.firstOrNull { it.name.equals(filename, ignoreCase = true) }
                    if (file != null) {
                        file.content.lines().forEach {
                            lines.add(TerminalLine(it, TerminalLineType.OUTPUT))
                        }
                    } else {
                        lines.add(TerminalLine("cat: $filename: No such file or directory", TerminalLineType.ERROR))
                    }
                }
            }
            "clear" -> {
                // Handled in UI
            }
            "git" -> {
                val subCmd = parts.getOrNull(1)?.lowercase()
                when (subCmd) {
                    "status" -> {
                        lines.add(TerminalLine("On branch ${project?.gitBranch ?: "main"}", TerminalLineType.INFO))
                        lines.add(TerminalLine("Your branch is up to date with 'origin/${project?.gitBranch ?: "main"}'", TerminalLineType.OUTPUT))
                        lines.add(TerminalLine("nothing to commit, working tree clean", TerminalLineType.SUCCESS))
                    }
                    "log" -> {
                        lines.add(TerminalLine("commit 7f3a9b1 (HEAD -> main, origin/main)", TerminalLineType.INFO))
                        lines.add(TerminalLine("Author: Developer <dev@codeforge.cloud>", TerminalLineType.OUTPUT))
                        lines.add(TerminalLine("Date:   ${SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.US).format(Date())}", TerminalLineType.OUTPUT))
                        lines.add(TerminalLine("    feat: cloud sync and multi-language engine initialized", TerminalLineType.OUTPUT))
                    }
                    "push" -> {
                        lines.add(TerminalLine("Enumerating objects: 12, done.", TerminalLineType.OUTPUT))
                        lines.add(TerminalLine("Writing objects: 100% (12/12), 4.21 KiB | 4.21 MiB/s, done.", TerminalLineType.OUTPUT))
                        lines.add(TerminalLine("Total 12 (delta 3), reused 0 (delta 0)", TerminalLineType.OUTPUT))
                        lines.add(TerminalLine("To ${project?.remoteUrl ?: "https://github.com/codeforge/repo.git"}", TerminalLineType.INFO))
                        lines.add(TerminalLine("   7f3a9b1..e24b89c  main -> main", TerminalLineType.SUCCESS))
                    }
                    else -> {
                        lines.add(TerminalLine("git: '$subCmd' is not a recognized git command. Try 'git status', 'git push', 'git log'.", TerminalLineType.ERROR))
                    }
                }
            }
            "build" -> {
                lines.add(TerminalLine("📦 Building project assets for '${project?.name ?: "app"}'...", TerminalLineType.INFO))
                lines.add(TerminalLine("✓ Parsed ${files.size} source files across ${files.map { it.language.displayName }.distinct().joinToString()}", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("✓ Zero compilation errors. Bundle size: 48.2 KB (gzipped: 14.1 KB)", TerminalLineType.SUCCESS))
                lines.add(TerminalLine("Ready for production cloud deployment.", TerminalLineType.SUCCESS))
            }
            "deploy" -> {
                val provider = parts.getOrNull(1)?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() } ?: "Vercel"
                lines.add(TerminalLine("🚀 Triggering cloud deployment on $provider...", TerminalLineType.INFO))
                lines.add(TerminalLine("-> Uploading artifacts to edge CDN...", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("-> Provisioning serverless container...", TerminalLineType.OUTPUT))
                val targetUrl = "https://${project?.name?.lowercase()?.replace(" ", "-") ?: "app"}.codeforge.cloud"
                lines.add(TerminalLine("✅ Successfully deployed! Live at $targetUrl", TerminalLineType.SUCCESS))
            }
            "env" -> {
                lines.add(TerminalLine("NODE_ENV=production", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("CODEFORGE_REGION=us-east-1", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("GIT_BRANCH=${project?.gitBranch ?: "main"}", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("CLOUD_SYNC=ACTIVE", TerminalLineType.SUCCESS))
                lines.add(TerminalLine("GEMINI_API_ENABLED=true", TerminalLineType.INFO))
            }
            "echo" -> {
                val msg = parts.drop(1).joinToString(" ")
                lines.add(TerminalLine(msg, TerminalLineType.OUTPUT))
            }
            else -> {
                lines.add(TerminalLine("bash: $cmd: command not found. Type 'help' for available commands.", TerminalLineType.ERROR))
            }
        }

        return lines
    }
}
