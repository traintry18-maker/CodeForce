package com.example.ui.vscode

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

@Composable
fun VsCodeCommandPalette(
    query: String,
    onQueryChange: (String) -> Unit,
    commands: List<CommandPaletteItem>,
    onDismiss: () -> Unit
) {
    val filtered = remember(query, commands) {
        if (query.isBlank()) commands
        else commands.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.category.contains(query, ignoreCase = true)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = Color(0xFF252526),
            shape = RoundedCornerShape(8.dp),
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 420.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                // Command input box
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = { Text("Type a command or search...", color = Color(0xFF858585), fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = VsCodeInputBg,
                        unfocusedContainerColor = VsCodeInputBg,
                        focusedBorderColor = VsCodeInputFocusBorder,
                        unfocusedBorderColor = VsCodeInputBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().height(46.dp).testTag("command_palette_input")
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filtered) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    item.action()
                                    onDismiss()
                                }
                                .padding(horizontal = 8.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = item.title,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = item.category,
                                    color = Color(0xFF858585),
                                    fontSize = 10.sp
                                )
                            }

                            if (item.shortcut.isNotBlank()) {
                                Surface(
                                    color = Color(0xFF333333),
                                    shape = RoundedCornerShape(3.dp)
                                ) {
                                    Text(
                                        text = item.shortcut,
                                        color = Color(0xFFCCCCCC),
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
