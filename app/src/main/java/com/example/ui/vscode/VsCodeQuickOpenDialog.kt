package com.example.ui.vscode

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.CodeFileEntity
import com.example.ui.theme.*

@Composable
fun VsCodeQuickOpenDialog(
    files: List<CodeFileEntity>,
    onSelectFile: (CodeFileEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }

    val filteredFiles = remember(query, files) {
        if (query.isBlank()) files
        else files.filter { it.name.contains(query, ignoreCase = true) || it.language.name.contains(query, ignoreCase = true) }
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
                // Quick Open prompt box
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search files by name (Go to File)...", color = Color(0xFF858585), fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = VsCodeInputBg,
                        unfocusedContainerColor = VsCodeInputBg,
                        focusedBorderColor = VsCodeInputFocusBorder,
                        unfocusedBorderColor = VsCodeInputBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().height(46.dp).testTag("quick_open_input")
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "recently opened",
                    color = Color(0xFF858585),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filteredFiles) { file ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectFile(file)
                                    onDismiss()
                                }
                                .padding(horizontal = 8.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(file.language.badgeColorHex))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = file.name,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (file.isEntry) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "[main]",
                                            color = VsCodeSuccess,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                                Text(
                                    text = "src/${file.name} • ${file.language.displayName}",
                                    color = Color(0xFF858585),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
