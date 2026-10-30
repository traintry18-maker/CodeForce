package com.example.ui.vscode

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VsCodeEditorBg
import com.example.ui.theme.VsCodeInfo
import com.example.ui.theme.VsCodeSuccess

@Composable
fun VsCodeCopilotAgentChatPanel(
    messages: List<AgentChatMessage>,
    inputText: String,
    selectedModel: String,
    isAgentMode: Boolean,
    isGenerating: Boolean,
    onInputChange: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    onSelectModel: (String) -> Unit,
    onToggleAgentMode: () -> Unit,
    onApplyCode: (String) -> Unit,
    onCloseChat: () -> Unit,
    onNewChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showModelDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(310.dp)
            .background(Color(0xFF181818))
    ) {
        // Chat Panel Top Header
        Surface(
            color = Color(0xFF181818),
            modifier = Modifier
                .fillMaxWidth()
                .height(35.dp)
                .padding(horizontal = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Active "Chat" Tab Pill
                Surface(
                    color = Color(0xFF252526),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "Chat",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                // Header Action Buttons: +, v, ⚙, ..., ⛶, ✕
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Chat",
                        tint = Color(0xFFCCCCCC),
                        modifier = Modifier.size(15.dp).clickable { onNewChat() }.testTag("chat_new_button")
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Dropdown",
                        tint = Color(0xFF858585),
                        modifier = Modifier.size(15.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Configure",
                        tint = Color(0xFF858585),
                        modifier = Modifier.size(14.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "More",
                        tint = Color(0xFF858585),
                        modifier = Modifier.size(15.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = "Expand",
                        tint = Color(0xFF858585),
                        modifier = Modifier.size(14.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Chat",
                        tint = Color(0xFF858585),
                        modifier = Modifier.size(15.dp).clickable { onCloseChat() }.testTag("chat_close_button")
                    )
                }
            }
        }

        HorizontalDivider(color = Color(0xFF282828), thickness = 1.dp)

        // Chat Body (Empty state matching picture vs Message Stream)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            if (messages.isEmpty()) {
                // Empty state directly matching the user's screenshot
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 100.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Chat Bubble with Sparkle Icon
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF252526)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💬✨", fontSize = 24.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Build with Agent",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "AI responses may be inaccurate",
                        color = Color(0xFF858585),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.SansSerif
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Generate Agent Instructions to onboard AI onto your codebase.",
                        color = Color(0xFF38BDF8),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier
                            .clickable {
                                onSendMessage("Generate complete agent instructions and documentation for this codebase.")
                            }
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages) { msg ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (msg.isUser) Color(0xFF252526) else Color(0xFF1E1E1E))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (msg.isUser) "You" else "Copilot Agent ($selectedModel)",
                                    color = if (msg.isUser) Color(0xFF75BEFF) else Color(0xFF89D185),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = msg.timestamp,
                                    color = Color(0xFF6E7681),
                                    fontSize = 9.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = msg.message,
                                color = Color(0xFFCCCCCC),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )

                            // If there is code block generated
                            msg.codeBlock?.let { code ->
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    color = Color(0xFF121212),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = code,
                                            color = Color(0xFF9CDCFE),
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            maxLines = 8
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Button(
                                            onClick = { onApplyCode(code) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0078D4)),
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.height(26.dp)
                                        ) {
                                            Text("Apply to Workspace", fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Input Box Area (matching user's screenshot exactly)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Surface(
                color = Color(0xFF1E1E1E),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF333333)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    // Text Input field
                    Box(modifier = Modifier.fillMaxWidth().heightIn(min = 36.dp, max = 80.dp)) {
                        if (inputText.isEmpty()) {
                            Text(
                                text = "Describe what to build",
                                color = Color(0xFF6E7681),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.SansSerif
                            )
                        }
                        BasicTextField(
                            value = inputText,
                            onValueChange = onInputChange,
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.SansSerif
                            ),
                            cursorBrush = SolidColor(Color(0xFF0078D4)),
                            modifier = Modifier.fillMaxWidth().testTag("chat_input_field")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Action Row inside input box
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left buttons: +, </> Agent, Models
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // + add context
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add context",
                                tint = Color(0xFF858585),
                                modifier = Modifier.size(16.dp).padding(end = 4.dp)
                            )

                            // </> Agent badge
                            Surface(
                                color = if (isAgentMode) Color(0xFF252526) else Color.Transparent,
                                shape = RoundedCornerShape(4.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF333333)),
                                modifier = Modifier.clickable { onToggleAgentMode() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "</> Agent",
                                        color = if (isAgentMode) Color.White else Color(0xFF858585),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Models selector
                            Box {
                                Text(
                                    text = "Models ▾",
                                    color = Color(0xFFCCCCCC),
                                    fontSize = 11.sp,
                                    modifier = Modifier
                                        .clickable { showModelDropdown = true }
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                        .testTag("chat_models_selector")
                                )

                                DropdownMenu(
                                    expanded = showModelDropdown,
                                    onDismissRequest = { showModelDropdown = false },
                                    modifier = Modifier.background(Color(0xFF252526))
                                ) {
                                    listOf("Gemini 2.5 Flash", "Claude 3.7 Sonnet", "GPT-4o", "Gemini 2.0 Flash").forEach { model ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    if (model == selectedModel) {
                                                        Text("✓ ", color = Color(0xFF38BDF8))
                                                    }
                                                    Text(model, color = Color.White, fontSize = 12.sp)
                                                }
                                            },
                                            onClick = {
                                                onSelectModel(model)
                                                showModelDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Right icons: ⌥, 🎙, ↑ (send)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Config",
                                tint = Color(0xFF858585),
                                modifier = Modifier.size(15.dp).padding(end = 6.dp)
                            )
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice",
                                tint = Color(0xFF858585),
                                modifier = Modifier.size(15.dp).padding(end = 6.dp)
                            )

                            // Up arrow Send button
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (inputText.isNotBlank()) Color(0xFF0078D4) else Color(0xFF2D2D2D))
                                    .clickable(enabled = inputText.isNotBlank()) {
                                        onSendMessage(inputText)
                                    }
                                    .testTag("chat_send_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = "Send",
                                    tint = if (inputText.isNotBlank()) Color.White else Color(0xFF6E7681),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Footer below input: 🖥 Local   🛡 Default permissions (matching screenshot)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Computer,
                        contentDescription = "Local",
                        tint = Color(0xFF858585),
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Local",
                        color = Color(0xFF858585),
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Permissions",
                        tint = Color(0xFF858585),
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Default permissions",
                        color = Color(0xFF858585),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
