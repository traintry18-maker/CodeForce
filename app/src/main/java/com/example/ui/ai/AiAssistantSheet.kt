package com.example.ui.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.window.Dialog
import com.example.ai.AiResponseResult
import com.example.ai.AiTaskType
import com.example.data.model.SupportedLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantDialog(
    currentLanguage: SupportedLanguage,
    isLoading: Boolean,
    result: AiResponseResult?,
    onRequestAction: (task: AiTaskType, targetLanguage: SupportedLanguage?, customInstruction: String) -> Unit,
    onApplyCode: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTask by remember { mutableStateOf(AiTaskType.DEBUG_FIX) }
    var targetLanguage by remember { mutableStateOf(SupportedLanguage.TYPESCRIPT) }
    var customInstruction by remember { mutableStateOf("") }
    var showLangPicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E293B),
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 620.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF60A5FA),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gemini AI Code Assistant",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Task Chips Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AiTaskType.entries.forEach { task ->
                        FilterChip(
                            selected = selectedTask == task,
                            onClick = { selectedTask = task },
                            label = { Text(task.title, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0284C7),
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("ai_task_${task.name}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Target Language selector if translation task
                if (selectedTask == AiTaskType.CONVERT_LANGUAGE) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLangPicker = true }
                            .background(Color(0xFF334155), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Target: ${targetLanguage.displayName}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Instruction Input
                OutlinedTextField(
                    value = customInstruction,
                    onValueChange = { customInstruction = it },
                    placeholder = {
                        Text(
                            text = when (selectedTask) {
                                AiTaskType.DEBUG_FIX -> "Describe specific bug or error to fix..."
                                AiTaskType.REFACTOR -> "e.g. improve memory, convert to async..."
                                AiTaskType.GENERATE_TESTS -> "e.g. edge cases, mock dependencies..."
                                AiTaskType.CONVERT_LANGUAGE -> "Target language details..."
                                AiTaskType.SUGGEST_CODE -> "What feature to implement next..."
                            },
                            color = Color(0xFF64748B),
                            fontSize = 12.sp
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF475569)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("ai_custom_instruction_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Execute Button
                Button(
                    onClick = {
                        onRequestAction(selectedTask, targetLanguage, customInstruction)
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(42.dp).testTag("ai_run_action_button")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gemini is analyzing code...", color = Color.White, fontSize = 12.sp)
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Execute ${selectedTask.title}", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Results Container
                if (result != null) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Explanation
                        Surface(
                            color = Color(0xFF0F172A),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "AI Technical Analysis",
                                        color = Color(0xFF60A5FA),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    if (!result.isLiveAi) {
                                        Text(
                                            text = "[Smart Offline Mode]",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = result.explanation,
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        // Code Result if present
                        if (result.suggestedCode != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = Color(0xFF0D1117),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Suggested Code Update",
                                            color = Color(0xFF10B981),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Button(
                                            onClick = { onApplyCode(result.suggestedCode) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp).testTag("apply_ai_code_button")
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Apply to File", fontSize = 11.sp, color = Color.White)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = result.suggestedCode,
                                        color = Color(0xFFE2E8F0),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                } else if (!isLoading) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Select a task above to analyze and improve your code with Gemini AI.",
                            color = Color(0xFF64748B),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }

    // Language Dropdown Dialog
    if (showLangPicker) {
        AlertDialog(
            onDismissRequest = { showLangPicker = false },
            title = { Text("Select Target Language", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    SupportedLanguage.entries.forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    targetLanguage = lang
                                    showLangPicker = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(lang.badgeColorHex))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(lang.displayName, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLangPicker = false }) { Text("Cancel") }
            }
        )
    }
}
