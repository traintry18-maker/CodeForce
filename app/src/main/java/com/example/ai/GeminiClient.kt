package com.example.ai

import com.example.BuildConfig
import com.example.data.model.SupportedLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

enum class AiTaskType(val title: String, val iconName: String) {
    SUGGEST_CODE("AI Code Completion", "AutoAwesome"),
    DEBUG_FIX("Debug & Fix Issues", "BugReport"),
    REFACTOR("Refactor & Clean", "Build"),
    GENERATE_TESTS("Generate Unit Tests", "FactCheck"),
    CONVERT_LANGUAGE("Translate to Other Language", "Translate")
}

data class AiResponseResult(
    val explanation: String,
    val suggestedCode: String?,
    val isSuccess: Boolean,
    val isLiveAi: Boolean = true
)

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun requestGemini(systemPrompt: String, userPrompt: String): String? = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext null
        }

        try {
            val root = JSONObject()

            // System instruction
            val sysInstruction = JSONObject()
            val sysParts = JSONArray()
            sysParts.put(JSONObject().put("text", systemPrompt))
            sysInstruction.put("parts", sysParts)
            root.put("systemInstruction", sysInstruction)

            // User content
            val contents = JSONArray()
            val userContent = JSONObject()
            val userParts = JSONArray()
            userParts.put(JSONObject().put("text", userPrompt))
            userContent.put("parts", userParts)
            contents.put(userContent)
            root.put("contents", contents)

            // Generation config
            val config = JSONObject()
            config.put("temperature", 0.3)
            config.put("topP", 0.95)
            root.put("generationConfig", config)

            val requestBody = root.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext null
            }

            val bodyString = response.body?.string() ?: return@withContext null
            val json = JSONObject(bodyString)
            val candidates = json.optJSONArray("candidates") ?: return@withContext null
            val firstCand = candidates.optJSONObject(0) ?: return@withContext null
            val contentObj = firstCand.optJSONObject("content") ?: return@withContext null
            val parts = contentObj.optJSONArray("parts") ?: return@withContext null
            val firstPart = parts.optJSONObject(0) ?: return@withContext null
            firstPart.optString("text")
        } catch (e: Exception) {
            null
        }
    }
}
