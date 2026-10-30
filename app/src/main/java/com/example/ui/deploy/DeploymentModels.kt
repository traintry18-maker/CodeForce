package com.example.ui.deploy

import androidx.compose.ui.graphics.Color

enum class DeploymentStageStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}

data class PipelineStageInfo(
    val id: Int,
    val title: String,
    val detail: String,
    val status: DeploymentStageStatus = DeploymentStageStatus.PENDING
)

data class CloudProviderDef(
    val id: String,
    val name: String,
    val subtitle: String,
    val defaultDomainSuffix: String,
    val defaultBuildCmd: String,
    val defaultOutputDir: String,
    val brandColor: Color,
    val iconName: String,
    val edgeRegionsCount: Int = 32
)

val AVAILABLE_CLOUD_PROVIDERS = listOf(
    CloudProviderDef(
        id = "Vercel",
        name = "Vercel",
        subtitle = "Edge Network & Frontend Cloud",
        defaultDomainSuffix = "vercel.app",
        defaultBuildCmd = "npm run build",
        defaultOutputDir = "dist",
        brandColor = Color(0xFF000000),
        iconName = "vercel",
        edgeRegionsCount = 35
    ),
    CloudProviderDef(
        id = "Netlify",
        name = "Netlify",
        subtitle = "Composable Web Platform & Edge Functions",
        defaultDomainSuffix = "netlify.app",
        defaultBuildCmd = "npm run build",
        defaultOutputDir = "build",
        brandColor = Color(0xFF00AD9F),
        iconName = "netlify",
        edgeRegionsCount = 28
    ),
    CloudProviderDef(
        id = "Cloudflare",
        name = "Cloudflare Pages",
        subtitle = "Hyper-distributed Global Edge Workers",
        defaultDomainSuffix = "pages.dev",
        defaultBuildCmd = "npx wrangler pages deploy",
        defaultOutputDir = "public",
        brandColor = Color(0xFFF38020),
        iconName = "cloudflare",
        edgeRegionsCount = 300
    ),
    CloudProviderDef(
        id = "Render",
        name = "Render",
        subtitle = "Unified Cloud Application Hosting & Docker",
        defaultDomainSuffix = "onrender.com",
        defaultBuildCmd = "npm start",
        defaultOutputDir = "dist",
        brandColor = Color(0xFF46E3B7),
        iconName = "render",
        edgeRegionsCount = 18
    ),
    CloudProviderDef(
        id = "AWS",
        name = "AWS Amplify",
        subtitle = "Amazon Web Services Serverless & CDN",
        defaultDomainSuffix = "amplifyapp.com",
        defaultBuildCmd = "npm run build",
        defaultOutputDir = "out",
        brandColor = Color(0xFFFF9900),
        iconName = "aws",
        edgeRegionsCount = 450
    ),
    CloudProviderDef(
        id = "GitHubPages",
        name = "GitHub Pages",
        subtitle = "Static Git-Powered Site Hosting",
        defaultDomainSuffix = "github.io",
        defaultBuildCmd = "npm run build",
        defaultOutputDir = "docs",
        brandColor = Color(0xFF24292F),
        iconName = "github",
        edgeRegionsCount = 24
    ),
    CloudProviderDef(
        id = "Firebase",
        name = "Firebase Hosting",
        subtitle = "Google Cloud Fast & Secure SSD Edge Hosting",
        defaultDomainSuffix = "web.app",
        defaultBuildCmd = "firebase deploy",
        defaultOutputDir = "public",
        brandColor = Color(0xFFFFCA28),
        iconName = "firebase",
        edgeRegionsCount = 40
    )
)

data class ProviderConfig(
    val providerId: String,
    val apiToken: String = "",
    val projectName: String = "",
    val targetBranch: String = "main",
    val customDomain: String = "",
    val buildCommand: String = "npm run build",
    val outputDir: String = "dist",
    val environment: String = "Production",
    val envVars: List<Pair<String, String>> = listOf(
        "NODE_ENV" to "production",
        "API_BASE_URL" to "https://api.codeforge.cloud"
    ),
    val isConnected: Boolean = true,
    val lastTestedMs: Long = System.currentTimeMillis()
)
