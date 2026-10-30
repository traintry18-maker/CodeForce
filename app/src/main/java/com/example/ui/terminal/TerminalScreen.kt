package com.example.ui.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProjectEntity
import com.example.execution.TerminalLine
import com.example.execution.TerminalLineType
import kotlinx.coroutines.launch

@Composable
fun TerminalScreen(
    lines: List<TerminalLine>,
    input: String,
    project: ProjectEntity?,
    onInputChange: (String) -> Unit,
    onSubmitCommand: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val quickCommands = listOf(
        "run",
        "build",
        "deploy vercel",
        "git status",
        "git push",
        "ls -l",
        "env",
        "help",
        "clear"
    )

    LaunchedEffect(lines.size) {
        if (lines.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(lines.size - 1)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
    ) {
        // Terminal Header
        Surface(
            color = Color(0xFF161B22),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFF238636), shape = RoundedCornerShape(5.dp)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "codeforge@cloud:~/workspace (${project?.gitBranch ?: "main"})",
                        color = Color(0xFF58A6FF),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Linux 6.5.0-x86_64",
                    color = Color(0xFF8B949E),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Terminal Output Lines
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            items(lines) { line ->
                val textColor = when (line.type) {
                    TerminalLineType.INPUT -> Color(0xFF58A6FF)
                    TerminalLineType.OUTPUT -> Color(0xFFC9D1D9)
                    TerminalLineType.SUCCESS -> Color(0xFF3FB950)
                    TerminalLineType.ERROR -> Color(0xFFF85149)
                    TerminalLineType.INFO -> Color(0xFF79C0FF)
                    TerminalLineType.SYSTEM -> Color(0xFF8B949E)
                }

                Text(
                    text = line.text,
                    color = textColor,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
        }

        // Quick Command Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF161B22))
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            quickCommands.forEach { cmd ->
                Surface(
                    color = Color(0xFF21262D),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clickable {
                            onInputChange(cmd)
                            onSubmitCommand()
                        }
                        .testTag("quick_cmd_$cmd")
                ) {
                    Text(
                        text = cmd,
                        color = Color(0xFF58A6FF),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Terminal Prompt Input Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF161B22))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$ ",
                color = Color(0xFF3FB950),
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp
            )

            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                placeholder = { Text("Enter shell command...", color = Color(0xFF484F58), fontSize = 12.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSubmitCommand() }),
                textStyle = LocalTextStyle.current.copy(
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF58A6FF),
                    unfocusedBorderColor = Color(0xFF30363D),
                    focusedContainerColor = Color(0xFF0D1117),
                    unfocusedContainerColor = Color(0xFF0D1117)
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("terminal_input_field")
            )

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = onSubmitCommand,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF238636), RoundedCornerShape(8.dp))
                    .testTag("terminal_send_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Run Command",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
