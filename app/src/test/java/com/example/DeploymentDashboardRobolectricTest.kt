package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.ui.CodeForgeViewModel
import com.example.ui.deploy.AVAILABLE_CLOUD_PROVIDERS
import com.example.ui.deploy.ProviderConfig
import com.example.ui.vscode.VsCodeActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DeploymentDashboardRobolectricTest {

    @Test
    fun `available cloud providers contains all major targets`() {
        val providers = AVAILABLE_CLOUD_PROVIDERS.map { it.id }
        assertTrue(providers.contains("Vercel"))
        assertTrue(providers.contains("Netlify"))
        assertTrue(providers.contains("Cloudflare"))
        assertTrue(providers.contains("Render"))
        assertTrue(providers.contains("AWS"))
    }

    @Test
    fun `viewModel initializes with default deployment provider and config`() = runTest {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = CodeForgeViewModel(app)

        assertEquals("Vercel", viewModel.selectedDeployProvider.value)
        val configs = viewModel.providerConfigs.value
        assertNotNull(configs["Vercel"])
        assertEquals("Vercel", configs["Vercel"]?.providerId)
    }

    @Test
    fun `viewModel can select provider and manage environment variables`() = runTest {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = CodeForgeViewModel(app)

        viewModel.selectDeployProvider("Cloudflare")
        assertEquals("Cloudflare", viewModel.selectedDeployProvider.value)

        viewModel.addEnvVar("Cloudflare", "API_SECRET", "super_secret_token_123")
        val cfConfig = viewModel.providerConfigs.value["Cloudflare"]
        assertNotNull(cfConfig)
        assertEquals("super_secret_token_123", cfConfig?.envVars?.firstOrNull { it.first == "API_SECRET" }?.second)

        viewModel.removeEnvVar("Cloudflare", "API_SECRET")
        val updatedConfig = viewModel.providerConfigs.value["Cloudflare"]
        assertNull(updatedConfig?.envVars?.firstOrNull { it.first == "API_SECRET" })
    }

    @Test
    fun `viewModel updates provider configuration and navigates to cloud deploy`() = runTest {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = CodeForgeViewModel(app)

        viewModel.selectActivity(VsCodeActivity.CLOUD_DEPLOY)
        assertEquals(VsCodeActivity.CLOUD_DEPLOY, viewModel.activeActivity.value)

        val newConfig = ProviderConfig(
            providerId = "Netlify",
            apiToken = "net_test_token",
            projectName = "my-awesome-site",
            buildCommand = "npm run export",
            outputDir = "out"
        )
        viewModel.updateProviderConfig(newConfig)
        val fetched = viewModel.providerConfigs.value["Netlify"]
        assertEquals("net_test_token", fetched?.apiToken)
        assertEquals("npm run export", fetched?.buildCommand)
        assertEquals("out", fetched?.outputDir)
    }
}
