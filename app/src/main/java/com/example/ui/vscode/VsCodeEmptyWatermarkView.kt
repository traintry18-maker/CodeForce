package com.example.ui.vscode

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VsCodeEditorBg

@Composable
fun VsCodeEmptyWatermarkView(
    onOpenChat: () -> Unit,
    onShowAllCommands: () -> Unit,
    onToggleTerminal: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VsCodeEditorBg),
        contentAlignment = Alignment.Center
    ) {
        // Large VS Code Watermark Ribbon in Background
        Canvas(
            modifier = Modifier
                .size(240.dp)
                .align(Alignment.Center)
        ) {
            val w = size.width
            val h = size.height
            val ribbonColor = Color(0xFF282828).copy(alpha = 0.45f)

            // Draw VS Code iconic angled infinity / ribbon loops
            val path1 = Path().apply {
                moveTo(w * 0.75f, h * 0.15f)
                lineTo(w * 0.95f, h * 0.28f)
                lineTo(w * 0.95f, h * 0.72f)
                lineTo(w * 0.75f, h * 0.85f)
                close()
            }
            drawPath(path1, ribbonColor)

            val path2 = Path().apply {
                moveTo(w * 0.75f, h * 0.15f)
                lineTo(w * 0.32f, h * 0.52f)
                lineTo(w * 0.15f, h * 0.38f)
                lineTo(w * 0.08f, h * 0.44f)
                lineTo(w * 0.22f, h * 0.55f)
                lineTo(w * 0.08f, h * 0.66f)
                lineTo(w * 0.15f, h * 0.72f)
                lineTo(w * 0.32f, h * 0.58f)
                lineTo(w * 0.75f, h * 0.85f)
                close()
            }
            drawPath(path2, ribbonColor)
        }

        // Center Keyboard Shortcuts Card (matching picture exactly)
        Column(
            modifier = Modifier.padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(100.dp))

            // Open Chat
            ShortcutRow(
                label = "Open Chat",
                keys = listOf("Ctrl", "Alt", "I"),
                onClick = onOpenChat,
                tag = "watermark_open_chat"
            )

            // Show All Commands
            ShortcutRow(
                label = "Show All Commands",
                keys = listOf("Ctrl", "Shift", "P"),
                onClick = onShowAllCommands,
                tag = "watermark_show_commands"
            )

            // Toggle Terminal
            ShortcutRow(
                label = "Toggle Terminal",
                keys = listOf("Ctrl", "`"),
                onClick = onToggleTerminal,
                tag = "watermark_toggle_terminal"
            )
        }
    }
}

@Composable
private fun ShortcutRow(
    label: String,
    keys: List<String>,
    onClick: () -> Unit,
    tag: String
) {
    Row(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color(0xFFCCCCCC),
            fontSize = 13.sp,
            fontFamily = FontFamily.SansSerif,
            modifier = Modifier.widthIn(min = 180.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            keys.forEachIndexed { index, key ->
                Surface(
                    color = Color(0xFF2D2D2D),
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3C3C3C))
                ) {
                    Text(
                        text = key,
                        color = Color(0xFFCCCCCC),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
                if (index < keys.size - 1) {
                    Text(
                        text = "+",
                        color = Color(0xFF858585),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
            }
        }
    }
}
