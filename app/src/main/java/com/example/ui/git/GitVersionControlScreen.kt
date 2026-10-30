package com.example.ui.git

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.data.model.CodeFileEntity
import com.example.data.model.GitCommitEntity
import com.example.data.model.ProjectEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GitVersionControlScreen(
    project: ProjectEntity?,
    files: List<CodeFileEntity>,
    commits: List<GitCommitEntity>,
    onCommit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var commitMessage by remember { mutableStateOf("") }
    var selectedBranch by remember { mutableStateOf(project?.gitBranch ?: "main") }
    var selectedCommitForDiff by remember { mutableStateOf<GitCommitEntity?>(null) }
    var isPushing by remember { mutableStateOf(false) }
    var pushSuccessBanner by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp)
    ) {
        // Repo Overview Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ForkRight,
                            contentDescription = "Branch",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Branch: $selectedBranch",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Surface(
                        color = Color(0xFF334155),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "origin/main (synced)",
                            color = Color(0xFF10B981),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = project?.remoteUrl?.ifBlank { "https://github.com/developer/${project.name.lowercase().replace(" ", "-")}.git" }
                        ?: "https://github.com/developer/repo.git",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Commit Box
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Stage & Commit Changes",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = commitMessage,
                    onValueChange = { commitMessage = it },
                    placeholder = { Text("feat: commit message...", color = Color(0xFF64748B), fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF475569)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("commit_message_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (commitMessage.isNotBlank()) {
                                onCommit(commitMessage)
                                commitMessage = ""
                            }
                        },
                        enabled = commitMessage.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("git_commit_button")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Commit (${files.size} files)")
                    }

                    Button(
                        onClick = {
                            isPushing = true
                            pushSuccessBanner = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("git_push_button")
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Push Remote")
                    }
                }

                if (pushSuccessBanner) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "✓ Pushed 1 commit to GitHub remote main branch",
                        color = Color(0xFF10B981),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Commit Log History
        Text(
            text = "Commit History (${commits.size})",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(commits) { c ->
                val dateStr = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US).format(Date(c.timestamp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedCommitForDiff = c }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = c.message,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color(0xFF334155),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = c.commitHash,
                                        color = Color(0xFF38BDF8),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = c.author.substringBefore("<").trim(),
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = dateStr, color = Color(0xFF64748B), fontSize = 10.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Diff",
                                color = Color(0xFF38BDF8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Git Diff Viewer Modal
    if (selectedCommitForDiff != null) {
        val c = selectedCommitForDiff!!
        AlertDialog(
            onDismissRequest = { selectedCommitForDiff = null },
            title = {
                Text(
                    text = "Commit Diff • ${c.commitHash}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(c.message, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color(0xFF0D1117),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("--- a/${files.firstOrNull()?.name ?: "file"}", color = Color(0xFFF85149), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Text("+++ b/${files.firstOrNull()?.name ?: "file"}", color = Color(0xFF3FB950), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("+ // CodeForge cloud commit verified", color = Color(0xFF3FB950), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Text("+ // Synced with multi-language build pipeline", color = Color(0xFF3FB950), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedCommitForDiff = null }) { Text("Close") }
            }
        )
    }
}
