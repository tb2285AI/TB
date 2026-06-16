package com.example.data.repository

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.example.BuildConfig
import com.example.data.database.AppDatabase
import com.example.data.database.BriefingDao
import com.example.data.database.IntelBriefingEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class IntelRepository(context: Context) {

    private val db: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "world_monitor_db"
    ).fallbackToDestructiveMigration().build()

    private val briefingDao: BriefingDao = db.briefingDao()

    val allSavedBriefings: Flow<List<IntelBriefingEntity>> = briefingDao.getAllBriefings()

    suspend fun saveBriefing(briefing: IntelBriefingEntity): Long = withContext(Dispatchers.IO) {
        briefingDao.insertBriefing(briefing)
    }

    suspend fun updateBriefing(briefing: IntelBriefingEntity) = withContext(Dispatchers.IO) {
        briefingDao.updateBriefing(briefing)
    }

    suspend fun deleteBriefing(id: Int) = withContext(Dispatchers.IO) {
        briefingDao.deleteBriefingById(id)
    }

    suspend fun getBriefingById(id: Int): IntelBriefingEntity? = withContext(Dispatchers.IO) {
        briefingDao.getBriefingById(id)
    }

    companion object {
        private const val MODEL = "gemini-3.5-flash"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

        private val client = OkHttpClient.Builder()
            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

    /**
     * Executes direct API query against Gemini 3.5 Flash using local API Secrets.
     */
    suspend fun generateGeminiIntel(
        region: String,
        scenario: String,
        customPrompt: String = ""
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "ERROR_KEY: Gemini API Key is unconfigured. To secure live global monitoring reports, please insert your Gemini API Key in the AI Studio Secrets panel."
        }

        val prompt = if (customPrompt.isNotEmpty()) {
            customPrompt
        } else {
            "Generate an elite military-grade tactical Situation Report 'SITREP' for: Region: $region. Threat Scenario: $scenario.\n\n" +
            "Please deliver a beautifully structured briefing in standard plain text. Break down into sections:\n" +
            "1. TACTICAL OVERVIEW (A sharp executive summary and threat rating of either Low, Guard, Elevated, or Critical)\n" +
            "2. KEY DEVELOPMENTS (At least 3 analytical bullet points with high informational density)\n" +
            "3. REGIONAL & GLOBAL FALLOUT (How this impacts trade routes, cyber defense, or environmental stability)\n" +
            "4. RESPONSE OPTIONS (Strategic advisory recommendations for global defense command)\n\n" +
            "Include simulated dates, data sensors, and realistic naming conventions. Keep the tone clinical, objective, and analytical."
        }

        try {
            // Construct parts request
            val partTextObj = JSONObject().put("text", prompt)
            val partsArray = JSONArray().put(partTextObj)
            val contentObj = JSONObject().put("parts", partsArray)
            val contentsArray = JSONArray().put(contentObj)

            // System instructions
            val systemPartText = JSONObject().put("text", "You are an elite tactical intelligence system. Your text response should be highly structured with clinical headings, realistic names, technical jargon, and precise data indices.")
            val systemParts = JSONArray().put(systemPartText)
            val systemInstruction = JSONObject().put("parts", systemParts)

            val genConfig = JSONObject().put("temperature", 0.72)

            val rootJson = JSONObject().apply {
                put("contents", contentsArray)
                put("systemInstruction", systemInstruction)
                put("generationConfig", genConfig)
            }

            val requestBody = rootJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val requestUrl = "$BASE_URL?key=$apiKey"

            val request = Request.Builder()
                .url(requestUrl)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    Log.e("IntelRepository", "Gemini API Error Code: ${response.code}, Body: $body")
                    return@withContext "ERROR: Telemetry network returned code ${response.code}. Please ensure your API key is correctly enabled in AI Studio."
                }

                val resBodyStr = response.body?.string() ?: return@withContext "ERROR: Telemetry feed is completely blank."
                val responseJson = JSONObject(resBodyStr)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val contentObjRes = candidate.optJSONObject("content")
                    if (contentObjRes != null) {
                        val partsRes = contentObjRes.optJSONArray("parts")
                        if (partsRes != null && partsRes.length() > 0) {
                            return@withContext partsRes.getJSONObject(0).optString("text", "No readable metadata found.")
                        }
                    }
                }
                "ERROR: Failed to trace intelligence stream content segments."
            }
        } catch (e: Exception) {
            Log.e("IntelRepository", "Gemini API exception", e)
            "ERROR: Connection to deep monitoring matrix failed: ${e.localizedMessage}"
        }
    }

    /**
     * Conduct an AI analysis of a specific news event
     */
    suspend fun analyzeNewsItem(title: String, summary: String, category: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "ERROR_KEY: Gemini API Key is missing. Connect your API key in AI Studio Secrets to unlock the AI Strategic Analyst."
        }

        val prompt = "Perform a quick tactical threat analysis of the following global news update:\n" +
                "Title: $title\n" +
                "Category: $category\n" +
                "Summary: $summary\n\n" +
                "Please respond with:\n" +
                "- THREAT LEVEL ASSESSMENT (Low, Warning, or Critical with brief reasoning)\n" +
                "- INTEL SYNTAX SUMMARY (Who is behind it, why it matters)\n" +
                "- RISK MULTIPLIERS (Interconnected impacts to cyber networks, fuel costs, supply lines, or space systems)\n" +
                "- RECOMMENDED WATCH LIST (Countries, regions, or assets that global monitors should observe next)"

        try {
            val partTextObj = JSONObject().put("text", prompt)
            val partsArray = JSONArray().put(partTextObj)
            val contentObj = JSONObject().put("parts", partsArray)
            val contentsArray = JSONArray().put(contentObj)

            val systemPartText = JSONObject().put("text", "You are an elite, military-grade geopolitical advisor module. Respond in a strict, analytical, and professional intelligence protocol tone. Keep headings bold.")
            val systemParts = JSONArray().put(systemPartText)
            val systemInstruction = JSONObject().put("parts", systemParts)

            val genConfig = JSONObject().put("temperature", 0.6)

            val rootJson = JSONObject().apply {
                put("contents", contentsArray)
                put("systemInstruction", systemInstruction)
                put("generationConfig", genConfig)
            }

            val requestBody = rootJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val requestUrl = "$BASE_URL?key=$apiKey"

            val request = Request.Builder()
                .url(requestUrl)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext "ERROR: Telemetry network returned code ${response.code}."
                }
                val resBodyStr = response.body?.string() ?: "ERROR: Feed empty."
                val responseJson = JSONObject(resBodyStr)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val text = candidates.getJSONObject(0)
                        .optJSONObject("content")
                        ?.optJSONArray("parts")
                        ?.getJSONObject(0)
                        ?.optString("text")
                    if (text != null) return@withContext text
                }
                "ERROR: Failed parsing AI Intel briefing."
            }
        } catch (e: Exception) {
            "An error occurred during AI analysis: ${e.localizedMessage}"
        }
    }
}
