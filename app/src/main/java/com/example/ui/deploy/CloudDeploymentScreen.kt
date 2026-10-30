package com.example.ui.deploy

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeploymentRecordEntity
import com.example.data.model.ProjectEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudDeploymentScreen(
    project: ProjectEntity?,
    deployments: List<DeploymentRecordEntity>,
    isDeploying: Boolean,
    deployProgressMessage: String,
    liveLogs: List<String>,
    pipelineStages: List<PipelineStageInfo>,
    providerConfigs: Map<String, ProviderConfig>,
    selectedProviderId: String,
    branches: List<String>,
    currentBranch: String,
    onSelectProvider: (String) -> Unit,
    onUpdateProviderConfig: (ProviderConfig) -> Unit,
    onAddEnvVar: (providerId: String, key: String, value: String) -> Unit,
    onRemoveEnvVar: (providerId: String, key: String) -> Unit,
    onTestConnection: (String) -> Unit,
    onTriggerDeploy: (provider: String, customDomain: String, branch: String) -> Unit,
    onCancelDeploy: () -> Unit,
    onRollback: (DeploymentRecordEntity) -> Unit,
    onOpenEditor: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val activeConfig = providerConfigs[selectedProviderId] ?: ProviderConfig(providerId = selectedProviderId)
    val activeProviderDef = AVAILABLE_CLOUD_PROVIDERS.firstOrNull { it.id == selectedProviderId }
        ?: AVAILABLE_CLOUD_PROVIDERS.first()

    var customDomainInput by remember(selectedProviderId) { mutableStateOf(activeConfig.customDomain) }
    var selectedBranchInput by remember(selectedProviderId, currentBranch) { mutableStateOf(currentBranch) }
    var showConfigSection by remember { mutableStateOf(false) }
    var configActiveTab by remember { mutableIntStateOf(0) } // 0 = Settings, 1 = Environment Secrets

    var selectedLogRecord by remember { mutableStateOf<DeploymentRecordEntity?>(null) }
    var confirmRollbackRecord by remember { mutableStateOf<DeploymentRecordEntity?>(null) }
    var historySearchQuery by remember { mutableStateOf("") }
    var showQrDialog by remember { mutableStateOf(false) }

    // Editable config fields
    var editableApiToken by remember(selectedProviderId, activeConfig.apiToken) { mutableStateOf(activeConfig.apiToken) }
    var editableProjectName by remember(selectedProviderId, activeConfig.projectName) { mutableStateOf(activeConfig.projectName) }
    var editableBuildCmd by remember(selectedProviderId, activeConfig.buildCommand) { mutableStateOf(activeConfig.buildCommand) }
    var editableOutputDir by remember(selectedProviderId, activeConfig.outputDir) { mutableStateOf(activeConfig.outputDir) }
    var showApiToken by remember { mutableStateOf(false) }

    // New env var inputs
    var newEnvKey by remember { mutableStateOf("") }
    var newEnvValue by remember { mutableStateOf("") }

    val liveLogsListState = rememberLazyListState()

    // Auto-scroll logs when deploying
    LaunchedEffect(liveLogs.size) {
        if (liveLogs.isNotEmpty()) {
            liveLogsListState.animateScrollToItem(liveLogs.size - 1)
        }
    }

    val liveUrl = project?.cloudDeploymentUrl?.ifBlank {
        "https://${(project.name).lowercase().replace("[^a-z0-9]".toRegex(), "-")}.${activeProviderDef.defaultDomainSuffix}"
    } ?: "https://app.codeforge.cloud"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // TOP APP BAR: Navigation & Workspace Context
        Surface(
            color = Color(0xFF1E293B),
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onOpenEditor,
                        modifier = Modifier.size(32.dp).testTag("deploy_back_to_editor")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Editor",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF0284C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "Cloud Deployment Dashboard",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = project?.name ?: "Workspace",
                                color = Color(0xFF38BDF8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(" • ", color = Color(0xFF64748B), fontSize = 11.sp)
                            Icon(Icons.Default.ForkRight, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = selectedBranchInput,
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Active Provider badge
                Surface(
                    color = activeProviderDef.brandColor.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, activeProviderDef.brandColor.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isDeploying) Color(0xFFFBBF24) else Color(0xFF10B981))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = activeProviderDef.name,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // SCROLLABLE DASHBOARD CONTENT
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. CLOUD PROVIDER SELECTION BAR (Horizontal scroll)
            Column {
                Text(
                    text = "TARGET CLOUD PROVIDER",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AVAILABLE_CLOUD_PROVIDERS.forEach { prov ->
                        val isSelected = selectedProviderId == prov.id
                        Surface(
                            color = if (isSelected) Color(0xFF1E293B) else Color(0xFF141E33),
                            shape = RoundedCornerShape(10.dp),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF38BDF8)) else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                            modifier = Modifier
                                .clickable { onSelectProvider(prov.id) }
                                .testTag("provider_tab_${prov.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(prov.brandColor)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = prov.name,
                                        color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                    Text(
                                        text = "${prov.edgeRegionsCount} Edge PoPs",
                                        color = Color(0xFF64748B),
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. PRODUCTION STATUS HERO CARD
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isDeploying) Color(0xFFEAB308) else Color(0xFF10B981))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isDeploying) "BUILDING & ROUTING EDGE" else "PRODUCTION LIVE",
                                color = if (isDeploying) Color(0xFFEAB308) else Color(0xFF10B981),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Surface(
                            color = Color(0xFF0F172A),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Text(
                                text = "Uptime: 99.99% • 24ms",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = liveUrl,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.testTag("production_live_url_text")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action buttons: Copy, Open Browser, Share
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(liveUrl))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp).testTag("copy_deploy_url_button")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy URL", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Medium)
                        }

                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(liveUrl))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // fallback
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp).testTag("open_live_app_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Visit Site", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showQrDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF475569)),
                            modifier = Modifier.height(36.dp).testTag("show_qr_preview_btn")
                        ) {
                            Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFCBD5E1))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share / QR", fontSize = 12.sp, color = Color(0xFFCBD5E1))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFF334155))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Production Telemetry row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("SSL/TLS", color = Color(0xFF64748B), fontSize = 10.sp)
                            Text("TLS 1.3 Active", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Column {
                            Text("Edge Delivery", color = Color(0xFF64748B), fontSize = 10.sp)
                            Text("${activeProviderDef.edgeRegionsCount} Global Zones", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Column {
                            Text("Branch", color = Color(0xFF64748B), fontSize = 10.sp)
                            Text(project?.gitBranch ?: "main", color = Color(0xFF38BDF8), fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
                        }
                        Column {
                            Text("Environment", color = Color(0xFF64748B), fontSize = 10.sp)
                            Text("Production", color = Color(0xFFA78BFA), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // 3. PUSH & DEPLOY TRIGGER CARD
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "PUSH & DEPLOY CONTROLS",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Branch Selector
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Deploy Branch", color = Color(0xFFCBD5E1), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = Color(0xFF0F172A),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                                modifier = Modifier.fillMaxWidth().height(42.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.ForkRight, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = selectedBranchInput,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    if (branches.size > 1) {
                                        TextButton(
                                            onClick = {
                                                val nextIdx = (branches.indexOf(selectedBranchInput) + 1) % branches.size
                                                selectedBranchInput = branches[nextIdx]
                                            },
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text("Switch", fontSize = 10.sp, color = Color(0xFF38BDF8))
                                        }
                                    }
                                }
                            }
                        }

                        // Custom Domain or Subdomain
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Custom Domain (Optional)", color = Color(0xFFCBD5E1), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = customDomainInput,
                                onValueChange = { customDomainInput = it },
                                placeholder = { Text("app.mycompany.com", color = Color(0xFF64748B), fontSize = 11.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF0F172A),
                                    unfocusedContainerColor = Color(0xFF0F172A),
                                    focusedBorderColor = Color(0xFF38BDF8),
                                    unfocusedBorderColor = Color(0xFF334155),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                                modifier = Modifier.fillMaxWidth().height(42.dp).testTag("custom_domain_input")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Deploy Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                onTriggerDeploy(selectedProviderId, customDomainInput, selectedBranchInput)
                            },
                            enabled = !isDeploying,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("deploy_now_button")
                        ) {
                            if (isDeploying) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Deploying...", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                            } else {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Push & Deploy to ${activeProviderDef.name}", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                            }
                        }

                        if (isDeploying) {
                            OutlinedButton(
                                onClick = onCancelDeploy,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                                modifier = Modifier.height(46.dp).testTag("cancel_deploy_btn")
                            ) {
                                Text("Abort", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // 4. REAL-TIME PIPELINE & LIVE CONSOLE
            AnimatedVisibility(visible = isDeploying || liveLogs.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isDeploying) Color(0xFF0284C7) else Color(0xFF334155)),
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
                                    Icons.Default.AutoMode,
                                    contentDescription = null,
                                    tint = if (isDeploying) Color(0xFF38BDF8) else Color(0xFF10B981),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isDeploying) "LIVE DEPLOYMENT PIPELINE" else "LAST PIPELINE EXECUTION",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            if (liveLogs.isNotEmpty()) {
                                TextButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(liveLogs.joinToString("\n")))
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy Logs", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Multi-Stage Visual Stepper
                        if (pipelineStages.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                pipelineStages.forEach { stage ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        when (stage.status) {
                                            DeploymentStageStatus.COMPLETED -> {
                                                Icon(
                                                    Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = Color(0xFF10B981),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            DeploymentStageStatus.IN_PROGRESS -> {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(16.dp),
                                                    color = Color(0xFF38BDF8),
                                                    strokeWidth = 2.dp
                                                )
                                            }
                                            DeploymentStageStatus.FAILED -> {
                                                Icon(
                                                    Icons.Default.Cancel,
                                                    contentDescription = null,
                                                    tint = Color(0xFFEF4444),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            DeploymentStageStatus.PENDING -> {
                                                Box(
                                                    modifier = Modifier
                                                        .size(16.dp)
                                                        .clip(CircleShape)
                                                        .border(1.dp, Color(0xFF64748B), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "${stage.id}",
                                                        color = Color(0xFF64748B),
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = stage.title,
                                                color = when (stage.status) {
                                                    DeploymentStageStatus.COMPLETED -> Color(0xFFE2E8F0)
                                                    DeploymentStageStatus.IN_PROGRESS -> Color(0xFF38BDF8)
                                                    DeploymentStageStatus.FAILED -> Color(0xFFEF4444)
                                                    DeploymentStageStatus.PENDING -> Color(0xFF64748B)
                                                },
                                                fontSize = 11.sp,
                                                fontWeight = if (stage.status == DeploymentStageStatus.IN_PROGRESS) FontWeight.Bold else FontWeight.Medium
                                            )
                                            Text(
                                                text = stage.detail,
                                                color = Color(0xFF64748B),
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Terminal Live Stream Log Container
                        Surface(
                            color = Color(0xFF070B13),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp, max = 220.dp)
                        ) {
                            LazyColumn(
                                state = liveLogsListState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(10.dp)
                                    .testTag("deployment_terminal_logs")
                            ) {
                                items(liveLogs) { logLine ->
                                    val textColor = when {
                                        logLine.startsWith("✓") -> Color(0xFF3FB950)
                                        logLine.startsWith("[ERROR]") -> Color(0xFFF85149)
                                        logLine.startsWith("[STAGE") -> Color(0xFFE3B341)
                                        logLine.startsWith("[INIT]") || logLine.startsWith("[INFO]") -> Color(0xFF58A6FF)
                                        else -> Color(0xFFC9D1D9)
                                    }
                                    Text(
                                        text = logLine,
                                        color = textColor,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. PROVIDER CONFIGURATION & ENVIRONMENT SECRETS ACCORDION
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showConfigSection = !showConfigSection }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Provider Settings & Secrets (${activeProviderDef.name})",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Icon(
                            imageVector = if (showConfigSection) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8)
                        )
                    }

                    if (showConfigSection) {
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Color(0xFF334155))
                        Spacer(modifier = Modifier.height(10.dp))

                        // Tab Switcher: Settings vs Env Secrets
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                                .padding(3.dp)
                        ) {
                            Surface(
                                color = if (configActiveTab == 0) Color(0xFF0284C7) else Color.Transparent,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { configActiveTab = 0 }
                            ) {
                                Text(
                                    text = "Build & Deploy Config",
                                    color = if (configActiveTab == 0) Color.White else Color(0xFF94A3B8),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }

                            Surface(
                                color = if (configActiveTab == 1) Color(0xFF0284C7) else Color.Transparent,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { configActiveTab = 1 }
                            ) {
                                Text(
                                    text = "Environment Variables (${activeConfig.envVars.size})",
                                    color = if (configActiveTab == 1) Color.White else Color(0xFF94A3B8),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (configActiveTab == 0) {
                            // Sub-tab 0: Cloud Provider Settings
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                // API Access Token / Secret
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("API Access Token", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                                        TextButton(
                                            onClick = { onTestConnection(selectedProviderId) },
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Test Connection", color = Color(0xFF10B981), fontSize = 10.sp)
                                        }
                                    }

                                    OutlinedTextField(
                                        value = editableApiToken,
                                        onValueChange = { editableApiToken = it },
                                        placeholder = { Text("vc_tok_... or api token", color = Color(0xFF64748B), fontSize = 11.sp) },
                                        visualTransformation = if (showApiToken) VisualTransformation.None else PasswordVisualTransformation(),
                                        trailingIcon = {
                                            IconButton(onClick = { showApiToken = !showApiToken }) {
                                                Icon(
                                                    imageVector = if (showApiToken) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                    contentDescription = "Toggle Token Visibility",
                                                    tint = Color(0xFF94A3B8),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color(0xFF0F172A),
                                            unfocusedContainerColor = Color(0xFF0F172A),
                                            focusedBorderColor = Color(0xFF38BDF8),
                                            unfocusedBorderColor = Color(0xFF334155),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("api_token_input")
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Project Slug / App ID", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        OutlinedTextField(
                                            value = editableProjectName,
                                            onValueChange = { editableProjectName = it },
                                            placeholder = { Text("codeforge-app", color = Color(0xFF64748B), fontSize = 11.sp) },
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = Color(0xFF0F172A),
                                                unfocusedContainerColor = Color(0xFF0F172A),
                                                focusedBorderColor = Color(0xFF38BDF8),
                                                unfocusedBorderColor = Color(0xFF334155),
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            ),
                                            modifier = Modifier.fillMaxWidth().height(48.dp)
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Publish Directory", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        OutlinedTextField(
                                            value = editableOutputDir,
                                            onValueChange = { editableOutputDir = it },
                                            placeholder = { Text("dist", color = Color(0xFF64748B), fontSize = 11.sp) },
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = Color(0xFF0F172A),
                                                unfocusedContainerColor = Color(0xFF0F172A),
                                                focusedBorderColor = Color(0xFF38BDF8),
                                                unfocusedBorderColor = Color(0xFF334155),
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            ),
                                            modifier = Modifier.fillMaxWidth().height(48.dp)
                                        )
                                    }
                                }

                                Column {
                                    Text("Build Command", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = editableBuildCmd,
                                        onValueChange = { editableBuildCmd = it },
                                        placeholder = { Text("npm run build", color = Color(0xFF64748B), fontSize = 11.sp) },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color(0xFF0F172A),
                                            unfocusedContainerColor = Color(0xFF0F172A),
                                            focusedBorderColor = Color(0xFF38BDF8),
                                            unfocusedBorderColor = Color(0xFF334155),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                                        modifier = Modifier.fillMaxWidth().height(48.dp)
                                    )
                                }

                                Button(
                                    onClick = {
                                        val updated = activeConfig.copy(
                                            apiToken = editableApiToken,
                                            projectName = editableProjectName,
                                            buildCommand = editableBuildCmd,
                                            outputDir = editableOutputDir
                                        )
                                        onUpdateProviderConfig(updated)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().height(38.dp).testTag("save_config_btn")
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Save Provider Configuration", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            // Sub-tab 1: Environment Variables Manager
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "Encrypted environment variables injected into the build environment at deploy time.",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )

                                // Add new var row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = newEnvKey,
                                        onValueChange = { newEnvKey = it },
                                        placeholder = { Text("KEY", color = Color(0xFF64748B), fontSize = 11.sp) },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color(0xFF0F172A),
                                            unfocusedContainerColor = Color(0xFF0F172A),
                                            focusedBorderColor = Color(0xFF38BDF8),
                                            unfocusedBorderColor = Color(0xFF334155),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                                        modifier = Modifier.weight(1f).height(44.dp).testTag("env_key_input")
                                    )

                                    OutlinedTextField(
                                        value = newEnvValue,
                                        onValueChange = { newEnvValue = it },
                                        placeholder = { Text("VALUE", color = Color(0xFF64748B), fontSize = 11.sp) },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color(0xFF0F172A),
                                            unfocusedContainerColor = Color(0xFF0F172A),
                                            focusedBorderColor = Color(0xFF38BDF8),
                                            unfocusedBorderColor = Color(0xFF334155),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                                        modifier = Modifier.weight(1f).height(44.dp).testTag("env_value_input")
                                    )

                                    Button(
                                        onClick = {
                                            if (newEnvKey.isNotBlank()) {
                                                onAddEnvVar(selectedProviderId, newEnvKey, newEnvValue)
                                                newEnvKey = ""
                                                newEnvValue = ""
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.height(44.dp).testTag("add_env_var_btn")
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // List existing env vars
                                if (activeConfig.envVars.isEmpty()) {
                                    Text(
                                        text = "No environment variables configured.",
                                        color = Color(0xFF64748B),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                } else {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        activeConfig.envVars.forEach { (key, value) ->
                                            Surface(
                                                color = Color(0xFF0F172A),
                                                shape = RoundedCornerShape(6.dp),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFEAB308), modifier = Modifier.size(12.dp))
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = key,
                                                            color = Color(0xFF38BDF8),
                                                            fontSize = 11.sp,
                                                            fontFamily = FontFamily.Monospace,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Text(" = ", color = Color(0xFF64748B), fontSize = 11.sp)
                                                        Text(
                                                            text = if (value.length > 15) "${value.take(4)}••••${value.takeLast(4)}" else value,
                                                            color = Color(0xFFCBD5E1),
                                                            fontSize = 11.sp,
                                                            fontFamily = FontFamily.Monospace
                                                        )
                                                    }

                                                    IconButton(
                                                        onClick = { onRemoveEnvVar(selectedProviderId, key) },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Default.Delete,
                                                            contentDescription = "Delete Variable",
                                                            tint = Color(0xFFEF4444),
                                                            modifier = Modifier.size(14.dp)
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
                }
            }

            // 6. DEPLOYMENT HISTORY & ROLLBACK RELEASES
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "DEPLOYMENT HISTORY",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = Color(0xFF334155),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "${deployments.size}",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (deployments.isEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CloudQueue, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No deployments recorded yet.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            Text("Push your project code above to deploy to $selectedProviderId.", color = Color(0xFF64748B), fontSize = 11.sp)
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        deployments.forEach { dep ->
                            val dateStr = SimpleDateFormat("MMM dd, yyyy • HH:mm:ss", Locale.US).format(Date(dep.timestamp))
                            val isCurrentLive = dep.deployUrl == project?.cloudDeploymentUrl

                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                shape = RoundedCornerShape(10.dp),
                                border = if (isCurrentLive) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981)) else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isCurrentLive) Color(0xFF10B981) else Color(0xFF64748B))
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = dep.provider,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            if (isCurrentLive) {
                                                Surface(
                                                    color = Color(0xFF10B981).copy(alpha = 0.2f),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = "ACTIVE",
                                                        color = Color(0xFF10B981),
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Text(
                                            text = "${dep.buildDurationMs}ms build",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = dep.deployUrl,
                                        color = Color(0xFF38BDF8),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = dateStr, color = Color(0xFF64748B), fontSize = 10.sp)

                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            TextButton(
                                                onClick = { selectedLogRecord = dep },
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                modifier = Modifier.height(28.dp).testTag("view_logs_btn_${dep.id}")
                                            ) {
                                                Icon(Icons.Default.Terminal, contentDescription = null, tint = Color(0xFF93C5FD), modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Logs", color = Color(0xFF93C5FD), fontSize = 11.sp)
                                            }

                                            if (!isCurrentLive) {
                                                Button(
                                                    onClick = { confirmRollbackRecord = dep },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                    modifier = Modifier.height(28.dp).testTag("rollback_btn_${dep.id}")
                                                ) {
                                                    Icon(Icons.Default.Restore, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Rollback", fontSize = 11.sp, color = Color.White)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // RELEASE BUILD LOGS DIALOG
    selectedLogRecord?.let { log ->
        AlertDialog(
            onDismissRequest = { selectedLogRecord = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Terminal, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Deployment Release #${log.id} (${log.provider})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Live Endpoint: ${log.deployUrl}",
                        color = Color(0xFF38BDF8),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color(0xFF070B13),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp)
                    ) {
                        LazyColumn(modifier = Modifier.padding(10.dp)) {
                            items(log.logs.lines()) { line ->
                                Text(
                                    text = line,
                                    color = if (line.startsWith("✓")) Color(0xFF3FB950) else Color(0xFFC9D1D9),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(log.logs))
                    }
                ) {
                    Text("Copy Logs")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedLogRecord = null }) {
                    Text("Close")
                }
            }
        )
    }

    // ROLLBACK CONFIRMATION DIALOG
    confirmRollbackRecord?.let { record ->
        AlertDialog(
            onDismissRequest = { confirmRollbackRecord = null },
            icon = {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEAB308))
            },
            title = { Text("Confirm Rollback") },
            text = {
                Text(
                    text = "Are you sure you want to revert production edge CDN routing to release #${record.id} deployed on ${record.provider}?\n\nTarget URL: ${record.deployUrl}"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRollback(record)
                        confirmRollbackRecord = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Rollback Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmRollbackRecord = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // SHARE & QR CODE DIALOG
    if (showQrDialog) {
        AlertDialog(
            onDismissRequest = { showQrDialog = false },
            title = { Text("Share & In-App Preview", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.QrCode2,
                        contentDescription = "QR Code",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(140.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = liveUrl,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Scan with your phone or share the production link with your team.",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(liveUrl))
                        showQrDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Text("Copy Link")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQrDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
