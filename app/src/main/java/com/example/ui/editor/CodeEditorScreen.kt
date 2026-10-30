package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CodeFileEntity
import com.example.data.model.SupportedLanguage
import com.example.execution.ExecutionResult
import com.example.syntax.CodeHighlighter
import com.example.syntax.EditorColors
import com.example.syntax.EditorThemeMode
import com.example.syntax.SyntaxThemes
import com.example.ui.theme.VsCodeBreakpoint

class SyntaxVisualTransformation(
    private val language: SupportedLanguage,
    private val colors: EditorColors
) : VisualTransformation {
    override fun filter(text: androidx.compose.ui.text.AnnotatedString): TransformedText {
        val highlighted = CodeHighlighter.highlight(text.text, language, colors)
        return TransformedText(highlighted, OffsetMapping.Identity)
    }
}

@Composable
fun CodeEditorScreen(
    activeFile: CodeFileEntity?,
    openFiles: List<CodeFileEntity>,
    currentCode: String,
    themeMode: EditorThemeMode,
    fontSize: Float,
    isWordWrap: Boolean,
    showLineNumbers: Boolean,
    isExecuting: Boolean,
    executionResult: ExecutionResult?,
    isSearchVisible: Boolean,
    searchQuery: String,
    replaceQuery: String,
    projectName: String = "Workspace",
    breadcrumbsVisible: Boolean = true,
    minimapVisible: Boolean = true,
    breakpoints: Set<Int> = emptySet(),
    onToggleBreakpoint: (Int) -> Unit = {},
    onCodeChange: (String) -> Unit,
    onSelectFile: (CodeFileEntity) -> Unit,
    onCloseFile: (Long) -> Unit,
    onNewFileClick: () -> Unit,
    onRunClick: () -> Unit,
    onAiClick: () -> Unit,
    onToggleSearch: () -> Unit,
    onSearchChange: (String) -> Unit,
    onReplaceChange: (String) -> Unit,
    onReplaceAll: () -> Unit,
    onInsertSymbol: (String) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismissExecutionResult: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = remember(themeMode) { SyntaxThemes.getTheme(themeMode) }
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    val language = activeFile?.language ?: SupportedLanguage.JAVASCRIPT
    val visualTransformation = remember(language, colors) {
        SyntaxVisualTransformation(language, colors)
    }

    val lineCount = remember(currentCode) {
        currentCode.count { it == '\n' } + 1
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // VS Code Tabs Bar
        if (openFiles.isNotEmpty()) {
            CodeTabBar(
                openFiles = openFiles,
                activeFileId = activeFile?.id,
                onSelectFile = onSelectFile,
                onCloseFile = onCloseFile,
                onNewFileClick = onNewFileClick,
                colors = colors
            )
        }

        // VS Code Breadcrumbs Bar
        if (breadcrumbsVisible && activeFile != null) {
            VsCodeBreadcrumbsBar(
                projectName = projectName,
                fileName = activeFile.name
            )
        }

        // Search bar if toggled
        if (isSearchVisible) {
            EditorSearchBar(
                searchQuery = searchQuery,
                replaceQuery = replaceQuery,
                onSearchChange = onSearchChange,
                onReplaceChange = onReplaceChange,
                onReplaceAll = onReplaceAll,
                onClose = onToggleSearch,
                colors = colors
            )
        }

        // Editor Canvas (Line numbers + Breakpoint margin + Code + Minimap)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (activeFile == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "CodeForge — Visual Studio Code for Cloud",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Select a file from the Explorer or create a new file to start coding.",
                            color = colors.gutterText,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Editor & Gutter scrollable area
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(verticalScrollState)
                    ) {
                        // Line numbers gutter with Breakpoints Margin
                        if (showLineNumbers) {
                            Column(
                                modifier = Modifier
                                    .background(colors.gutterBackground)
                                    .padding(start = 4.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                for (line in 1..lineCount) {
                                    val hasBreakpoint = breakpoints.contains(line)
                                    Row(
                                        modifier = Modifier
                                            .height((fontSize * 1.4f).dp)
                                            .clickable { onToggleBreakpoint(line) },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Breakpoint Dot
                                        if (hasBreakpoint) {
                                            Box(
                                                modifier = Modifier
                                                    .size(7.dp)
                                                    .clip(CircleShape)
                                                    .background(VsCodeBreakpoint)
                                                    .testTag("breakpoint_$line")
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.width(7.dp))
                                        }

                                        Spacer(modifier = Modifier.width(5.dp))

                                        Text(
                                            text = "$line",
                                            color = if (hasBreakpoint) Color.White else colors.gutterText,
                                            fontSize = fontSize.sp,
                                            fontFamily = FontFamily.Monospace,
                                            lineHeight = (fontSize * 1.4f).sp
                                        )
                                    }
                                }
                            }

                            // Thin Gutter Separator
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .fillMaxHeight()
                                    .background(Color(0xFF2B2B2B))
                            )
                        }

                        // Code input field
                        val editorModifier = if (isWordWrap) {
                            Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        } else {
                            Modifier
                                .horizontalScroll(horizontalScrollState)
                                .defaultMinSize(minWidth = 700.dp)
                                .padding(8.dp)
                        }

                        BasicTextField(
                            value = currentCode,
                            onValueChange = onCodeChange,
                            modifier = editorModifier.testTag("code_input_field"),
                            textStyle = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = fontSize.sp,
                                lineHeight = (fontSize * 1.4f).sp,
                                color = colors.text
                            ),
                            cursorBrush = SolidColor(Color(0xFF569CD6)),
                            visualTransformation = visualTransformation
                        )
                    }

                    // VS Code Minimap Strip
                    if (minimapVisible) {
                        Box(
                            modifier = Modifier
                                .width(34.dp)
                                .fillMaxHeight()
                                .background(Color(0xFF1E1E1E).copy(alpha = 0.8f))
                                .padding(vertical = 8.dp, horizontal = 2.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                for (i in 0 until (lineCount.coerceAtMost(30))) {
                                    val barWidth = (10..24).random().dp
                                    Box(
                                        modifier = Modifier
                                            .height(2.dp)
                                            .width(barWidth)
                                            .background(
                                                if (i % 3 == 0) Color(0xFF569CD6).copy(alpha = 0.5f)
                                                else if (i % 2 == 0) Color(0xFFCE9178).copy(alpha = 0.5f)
                                                else Color(0xFF6E7681).copy(alpha = 0.3f)
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Symbol Bar (above mobile keyboard)
        QuickSymbolBar(
            onInsertSymbol = onInsertSymbol,
            onUndo = onUndo,
            onRedo = onRedo,
            colors = colors
        )
    }
}
