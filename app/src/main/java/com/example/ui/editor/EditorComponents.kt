package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
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
import com.example.data.model.CodeFileEntity
import com.example.syntax.EditorColors
import com.example.syntax.EditorThemeMode

@Composable
fun CodeTabBar(
    openFiles: List<CodeFileEntity>,
    activeFileId: Long?,
    onSelectFile: (CodeFileEntity) -> Unit,
    onCloseFile: (Long) -> Unit,
    onNewFileClick: () -> Unit,
    onSplitEditor: () -> Unit = {},
    colors: EditorColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(35.dp)
            .background(Color(0xFF252526)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            openFiles.forEach { file ->
                val isActive = file.id == activeFileId
                val tabBg = if (isActive) Color(0xFF1E1E1E) else Color(0xFF2D2D2D)
                val textColor = if (isActive) Color.White else Color(0xFF969696)

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .background(tabBg)
                        .clickable { onSelectFile(file) }
                        .padding(horizontal = 12.dp)
                        .testTag("tab_${file.name}"),
                    contentAlignment = Alignment.Center
                ) {
                    // Top active line indicator (classic VS Code blue)
                    if (isActive) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(Color(0xFF007ACC))
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(RoundedCornerShape(3.5.dp))
                                .background(Color(file.language.badgeColorHex))
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(
                            text = file.name,
                            color = textColor,
                            fontSize = 12.sp,
                            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Tab",
                            tint = textColor.copy(alpha = 0.7f),
                            modifier = Modifier
                                .size(13.dp)
                                .clickable { onCloseFile(file.id) }
                        )
                    }

                    // Right tab divider line
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(Color(0xFF1E1E1E))
                    )
                }
            }

            IconButton(
                onClick = onNewFileClick,
                modifier = Modifier
                    .size(28.dp)
                    .padding(start = 4.dp)
                    .testTag("add_file_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add File",
                    tint = Color(0xFF858585),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Tab Bar Right Actions (Split Editor, More)
        Row(
            modifier = Modifier
                .padding(end = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onSplitEditor,
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ViewColumn,
                    contentDescription = "Split Editor Right",
                    tint = Color(0xFF858585),
                    modifier = Modifier.size(16.dp)
                )
            }
            IconButton(
                onClick = {},
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More Actions",
                    tint = Color(0xFF858585),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun VsCodeBreadcrumbsBar(
    projectName: String,
    fileName: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(22.dp)
            .background(Color(0xFF1E1E1E))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$projectName",
            color = Color(0xFFA6A6A6),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = " > ",
            color = Color(0xFF6E7681),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = "src",
            color = Color(0xFFA6A6A6),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = " > ",
            color = Color(0xFF6E7681),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = fileName,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun QuickSymbolBar(
    onInsertSymbol: (String) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    colors: EditorColors
) {
    val symbols = listOf(
        "TAB" to "  ",
        "{ }" to "{}",
        "( )" to "()",
        "[ ]" to "[]",
        "< >" to "<>",
        "\" \"" to "\"\"",
        "' '" to "''",
        "=" to " = ",
        "=>" to " => ",
        "->" to " -> ",
        "::" to "::",
        ";" to ";",
        ":" to ":",
        "$" to "$",
        "#" to "#",
        "//" to "// ",
        "+" to "+",
        "-" to "-",
        "*" to "*",
        "/" to "/",
        "!" to "!",
        "?" to "?"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.gutterBackground)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onUndo,
            modifier = Modifier.size(36.dp).testTag("undo_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Undo,
                contentDescription = "Undo",
                tint = colors.text,
                modifier = Modifier.size(18.dp)
            )
        }

        IconButton(
            onClick = onRedo,
            modifier = Modifier.size(36.dp).testTag("redo_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Redo,
                contentDescription = "Redo",
                tint = colors.text,
                modifier = Modifier.size(18.dp)
            )
        }

        Box(
            modifier = Modifier
                .height(20.dp)
                .width(1.dp)
                .background(colors.gutterText.copy(alpha = 0.3f))
                .padding(horizontal = 2.dp)
        )

        symbols.forEach { (label, toInsert) ->
            Surface(
                color = colors.currentLineBackground,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .clickable { onInsertSymbol(toInsert) }
                    .testTag("symbol_$label")
            ) {
                Text(
                    text = label,
                    color = colors.text,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun EditorSearchBar(
    searchQuery: String,
    replaceQuery: String,
    onSearchChange: (String) -> Unit,
    onReplaceChange: (String) -> Unit,
    onReplaceAll: () -> Unit,
    onClose: () -> Unit,
    colors: EditorColors
) {
    Surface(
        color = colors.gutterBackground,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Find in file...", color = colors.gutterText, fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.text,
                        unfocusedTextColor = colors.text,
                        focusedBorderColor = colors.keyword,
                        unfocusedBorderColor = colors.gutterText.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("search_input")
                )
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(onClick = onClose, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Search",
                        tint = colors.text
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = replaceQuery,
                    onValueChange = onReplaceChange,
                    placeholder = { Text("Replace with...", color = colors.gutterText, fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.text,
                        unfocusedTextColor = colors.text,
                        focusedBorderColor = colors.keyword,
                        unfocusedBorderColor = colors.gutterText.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("replace_input")
                )
                Spacer(modifier = Modifier.width(6.dp))
                Button(
                    onClick = onReplaceAll,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.keyword),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(44.dp).testTag("replace_all_button")
                ) {
                    Text("Replace All", fontSize = 11.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun EditorSettingsDialog(
    currentTheme: EditorThemeMode,
    currentFontSize: Float,
    isWordWrap: Boolean,
    showLineNumbers: Boolean,
    onThemeSelect: (EditorThemeMode) -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onToggleWordWrap: () -> Unit,
    onToggleLineNumbers: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editor Preferences", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Theme Palette", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))
                EditorThemeMode.entries.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSelect(mode) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentTheme == mode,
                            onClick = { onThemeSelect(mode) }
                        )
                        Text(mode.displayName, fontSize = 14.sp, modifier = Modifier.padding(start = 6.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Font Size: ${currentFontSize.toInt()}sp", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Slider(
                    value = currentFontSize,
                    onValueChange = onFontSizeChange,
                    valueRange = 10f..24f,
                    steps = 7
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Word Wrap", fontSize = 14.sp)
                    Switch(checked = isWordWrap, onCheckedChange = { onToggleWordWrap() })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Show Line Numbers", fontSize = 14.sp)
                    Switch(checked = showLineNumbers, onCheckedChange = { onToggleLineNumbers() })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        }
    )
}
