package com.example.git

import java.io.File
import kotlin.math.max

data class GitCliResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String
)

/**
 * Git CLI wrapper that executes or simulates Git commands and provides
 * porcelain output parsing, diff generation, and repository tracking.
 */
object GitCliWrapper {

    /**
     * Parses standard `git status --porcelain` (or `-s`) output into a map of
     * relative file paths to their [GitStatusType] and staging state.
     */
    fun parsePorcelainStatus(porcelainOutput: String): List<ParsedPorcelainEntry> {
        val entries = mutableListOf<ParsedPorcelainEntry>()
        val lines = porcelainOutput.lines().filter { it.isNotBlank() }

        for (line in lines) {
            if (line.length < 3) continue
            val indexChar = line[0]
            val workTreeChar = line[1]
            val path = line.substring(3).trim()

            val (status, isStaged) = when {
                indexChar == '?' && workTreeChar == '?' -> GitStatusType.UNTRACKED to false
                indexChar == 'A' -> GitStatusType.ADDED to true
                indexChar == 'M' && workTreeChar == ' ' -> GitStatusType.MODIFIED to true
                workTreeChar == 'M' -> GitStatusType.MODIFIED to false
                indexChar == 'D' || workTreeChar == 'D' -> GitStatusType.DELETED to (indexChar == 'D')
                indexChar == 'R' || workTreeChar == 'R' -> GitStatusType.RENAMED to (indexChar == 'R')
                else -> GitStatusType.MODIFIED to false
            }

            entries.add(
                ParsedPorcelainEntry(
                    path = path,
                    status = status,
                    isStaged = isStaged,
                    indexChar = indexChar,
                    workTreeChar = workTreeChar
                )
            )
        }

        return entries
    }

    data class ParsedPorcelainEntry(
        val path: String,
        val status: GitStatusType,
        val isStaged: Boolean,
        val indexChar: Char,
        val workTreeChar: Char
    )

    /**
     * Formats a list of [GitFileChange]s into standard `git status --porcelain` format.
     */
    fun formatPorcelainStatus(changes: List<GitFileChange>): String {
        val builder = StringBuilder()
        for (change in changes) {
            val code = when (change.status) {
                GitStatusType.UNTRACKED -> "??"
                GitStatusType.ADDED -> if (change.isStaged) "A " else " A"
                GitStatusType.MODIFIED -> if (change.isStaged) "M " else " M"
                GitStatusType.DELETED -> if (change.isStaged) "D " else " D"
                GitStatusType.RENAMED -> if (change.isStaged) "R " else " R"
                GitStatusType.CONFLICT -> "UU"
                GitStatusType.UNMODIFIED -> continue
            }
            builder.append("$code ${change.filePath}\n")
        }
        return builder.toString()
    }

    /**
     * Generates a unified diff comparing [oldContent] and [newContent],
     * similar to `git diff <file>`.
     */
    fun generateUnifiedDiff(
        fileName: String,
        oldContent: String,
        newContent: String
    ): List<DiffLine> {
        val oldLines = oldContent.lines()
        val newLines = newContent.lines()
        val diffLines = mutableListOf<DiffLine>()

        diffLines.add(
            DiffLine(
                type = DiffLineType.HEADER,
                text = "--- a/$fileName"
            )
        )
        diffLines.add(
            DiffLine(
                type = DiffLineType.HEADER,
                text = "+++ b/$fileName"
            )
        )

        // Simple Myers-style LCS line diff
        val lcs = computeLCS(oldLines, newLines)
        var i = 0
        var j = 0
        var oldLineNum = 1
        var newLineNum = 1

        while (i < oldLines.size || j < newLines.size) {
            if (i < oldLines.size && j < newLines.size && oldLines[i] == newLines[j] && (lcs.contains(i to j) || oldLines[i] == newLines[j])) {
                diffLines.add(
                    DiffLine(
                        type = DiffLineType.UNCHANGED,
                        text = "  ${oldLines[i]}",
                        oldLineNumber = oldLineNum++,
                        newLineNumber = newLineNum++
                    )
                )
                i++
                j++
            } else {
                // Deletions first
                if (i < oldLines.size && (j >= newLines.size || !newLines.subList(j, newLines.size).contains(oldLines[i]))) {
                    diffLines.add(
                        DiffLine(
                            type = DiffLineType.DELETED,
                            text = "- ${oldLines[i]}",
                            oldLineNumber = oldLineNum++
                        )
                    )
                    i++
                } else if (j < newLines.size) {
                    diffLines.add(
                        DiffLine(
                            type = DiffLineType.ADDED,
                            text = "+ ${newLines[j]}",
                            newLineNumber = newLineNum++
                        )
                    )
                    j++
                } else if (i < oldLines.size) {
                    diffLines.add(
                        DiffLine(
                            type = DiffLineType.DELETED,
                            text = "- ${oldLines[i]}",
                            oldLineNumber = oldLineNum++
                        )
                    )
                    i++
                }
            }
        }

        return diffLines
    }

    /**
     * Calculates line additions and deletions count between two strings.
     */
    fun countDiffMetrics(oldContent: String, newContent: String): Pair<Int, Int> {
        val oldLines = oldContent.lines()
        val newLines = newContent.lines()
        val oldSet = oldLines.toSet()
        val newSet = newLines.toSet()

        val additions = newLines.count { !oldSet.contains(it) }
        val deletions = oldLines.count { !newSet.contains(it) }
        return additions to deletions
    }

    private fun computeLCS(a: List<String>, b: List<String>): Set<Pair<Int, Int>> {
        val dp = Array(a.size + 1) { IntArray(b.size + 1) }
        for (i in 1..a.size) {
            for (j in 1..b.size) {
                if (a[i - 1] == b[j - 1]) {
                    dp[i][j] = dp[i - 1][j - 1] + 1
                } else {
                    dp[i][j] = max(dp[i - 1][j], dp[i][j - 1])
                }
            }
        }

        val result = mutableSetOf<Pair<Int, Int>>()
        var i = a.size
        var j = b.size
        while (i > 0 && j > 0) {
            if (a[i - 1] == b[j - 1]) {
                result.add((i - 1) to (j - 1))
                i--
                j--
            } else if (dp[i - 1][j] > dp[i][j - 1]) {
                i--
            } else {
                j--
            }
        }
        return result
    }

    /**
     * Attempts to execute a real Git CLI command on the device via ProcessBuilder,
     * returning [GitCliResult]. Gracefully falls back if Git is not installed in the OS.
     */
    fun executeCliCommand(workingDir: File?, vararg args: String): GitCliResult {
        return try {
            val commandList = mutableListOf("git")
            commandList.addAll(args)
            val process = ProcessBuilder(commandList)
                .directory(workingDir)
                .redirectErrorStream(false)
                .start()

            val stdout = process.inputStream.bufferedReader().use { it.readText() }
            val stderr = process.errorStream.bufferedReader().use { it.readText() }
            val exitCode = process.waitFor()

            GitCliResult(exitCode, stdout, stderr)
        } catch (e: Exception) {
            GitCliResult(
                exitCode = -1,
                stdout = "",
                stderr = e.message ?: "Git CLI process execution failed"
            )
        }
    }
}
