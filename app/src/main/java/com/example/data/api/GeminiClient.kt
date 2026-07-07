package com.example.data.api

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.net.URL

// --- Result wrapper for rich outputs ---
sealed class GeminiCustomResult {
    data class SuccessText(val text: String, val groundingInfo: String? = null) : GeminiCustomResult()
    data class SuccessMedia(val mimeType: String, val base64Data: String, val textDescription: String? = null) : GeminiCustomResult()
    data class Error(val message: String) : GeminiCustomResult()
}

interface GeminiApiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: RequestBody
    ): ResponseBody
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .build()

    val apiService: GeminiApiService = retrofit.create(GeminiApiService::class.java)

    // --- Legacy signature kept for back-compatibility with other files ---
    suspend fun getAiResponse(prompt: String, systemInstruction: String? = null): String {
        val result = getGenerativeResponse(
            model = "gemini-3.5-flash",
            prompt = prompt,
            systemInstruction = systemInstruction
        )
        return when (result) {
            is GeminiCustomResult.SuccessText -> result.text
            is GeminiCustomResult.SuccessMedia -> "[Media Generated: ${result.mimeType}]"
            is GeminiCustomResult.Error -> result.message
        }
    }

    // --- New powerful multi-purpose dynamic entry point ---
    suspend fun getGenerativeResponse(
        model: String,
        prompt: String,
        systemInstruction: String? = null,
        inlineDataList: List<Pair<String, String>>? = null, // mimeType to base64
        aspectRatio: String? = null,
        imageSize: String? = null,
        thinkingLevel: String? = null,
        searchGrounding: Boolean = false,
        mapsGrounding: Boolean = false,
        responseModalities: List<String>? = null,
        voiceName: String? = null
    ): GeminiCustomResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext GeminiCustomResult.Error(
                "Please configure your GEMINI_API_KEY in the Secrets panel in AI Studio to unlock full AI capabilities!"
            )
        }

        try {
            // Build the dynamic JSON payload map
            val requestMap = mutableMapOf<String, Any>()

            // 1. Contents and parts
            val partsList = mutableListOf<Map<String, Any>>()
            
            // Add text prompt part if present
            if (prompt.isNotBlank()) {
                partsList.add(mapOf("text" to prompt))
            }

            // Add any multimodal base64 media inline data
            inlineDataList?.forEach { (mime, base64) ->
                partsList.add(
                    mapOf(
                        "inlineData" to mapOf(
                            "mimeType" to mime,
                            "data" to base64
                        )
                    )
                )
            }

            if (partsList.isEmpty()) {
                partsList.add(mapOf("text" to "Hello"))
            }

            requestMap["contents"] = listOf(mapOf("parts" to partsList))

            // 2. System Instruction
            if (systemInstruction != null && systemInstruction.isNotBlank()) {
                requestMap["systemInstruction"] = mapOf(
                    "parts" to listOf(mapOf("text" to systemInstruction))
                )
            }

            // 3. Generation Config
            val generationConfig = mutableMapOf<String, Any>()
            
            if (thinkingLevel != null) {
                generationConfig["thinkingConfig"] = mapOf("thinkingLevel" to thinkingLevel.lowercase())
                // As per instructions: "Do not set maxOutputTokens" when thinking level is high.
            }
            
            if (aspectRatio != null || imageSize != null) {
                val imgConfig = mutableMapOf<String, String>()
                if (aspectRatio != null) imgConfig["aspectRatio"] = aspectRatio
                if (imageSize != null) imgConfig["imageSize"] = imageSize
                generationConfig["imageConfig"] = imgConfig
            }
            
            if (responseModalities != null) {
                generationConfig["responseModalities"] = responseModalities
            }
            
            if (voiceName != null) {
                generationConfig["speechConfig"] = mapOf(
                    "voiceConfig" to mapOf(
                        "prebuiltVoiceConfig" to mapOf("voiceName" to voiceName)
                    )
                )
            }

            if (generationConfig.isNotEmpty()) {
                requestMap["generationConfig"] = generationConfig
            }

            // 4. Grounding Tools
            val toolsList = mutableListOf<Map<String, Any>>()
            if (searchGrounding) {
                toolsList.add(mapOf("googleSearch" to emptyMap<String, Any>()))
            }
            if (mapsGrounding) {
                toolsList.add(mapOf("googleMaps" to emptyMap<String, Any>()))
            }
            if (toolsList.isNotEmpty()) {
                requestMap["tools"] = toolsList
            }

            // Serialize Request
            val jsonAdapter = moshi.adapter(Map::class.java)
            val jsonString = jsonAdapter.toJson(requestMap)
            val requestBody = jsonString.toRequestBody("application/json".toMediaType())

            // Execute REST API Call
            val responseBody = apiService.generateContent(model, apiKey, requestBody)
            val responseString = responseBody.string()

            // Parse response maps
            val responseMap = jsonAdapter.fromJson(responseString) ?: return@withContext GeminiCustomResult.Error("Failed to parse Gemini response")
            
            val candidates = responseMap["candidates"] as? List<*>
            val firstCandidate = candidates?.firstOrNull() as? Map<*, *>
            val content = firstCandidate?.get("content") as? Map<*, *>
            val parts = content?.get("parts") as? List<*>

            // Grounding metadata check
            val groundingMetadata = firstCandidate?.get("groundingMetadata") as? Map<*, *>
            val webSources = groundingMetadata?.get("groundingChunks") as? List<*>
            var groundingSummary = ""
            if (!webSources.isNullOrEmpty()) {
                groundingSummary = "Sources used:\n" + webSources.mapIndexedNotNull { index, chunk ->
                    val chunkMap = chunk as? Map<*, *>
                    val web = chunkMap?.get("web") as? Map<*, *>
                    val title = web?.get("title") as? String ?: "Source"
                    val url = web?.get("uri") as? String ?: ""
                    "- $title ($url)"
                }.joinToString("\n")
            }

            var textOutput: String? = null
            var mediaMimeType: String? = null
            var mediaBase64: String? = null

            parts?.forEach { part ->
                val partMap = part as? Map<*, *>
                val text = partMap?.get("text") as? String
                if (text != null) {
                    textOutput = (textOutput ?: "") + text
                }
                val inlineData = partMap?.get("inlineData") as? Map<*, *>
                if (inlineData != null) {
                    mediaMimeType = inlineData["mimeType"] as? String
                    mediaBase64 = inlineData["data"] as? String
                }
            }

            when {
                mediaMimeType != null && mediaBase64 != null -> {
                    GeminiCustomResult.SuccessMedia(
                        mimeType = mediaMimeType!!,
                        base64Data = mediaBase64!!,
                        textDescription = textOutput
                    )
                }
                textOutput != null -> {
                    GeminiCustomResult.SuccessText(
                        text = textOutput!!,
                        groundingInfo = if (groundingSummary.isNotBlank()) groundingSummary else null
                    )
                }
                else -> {
                    GeminiCustomResult.Error("Received empty response from Gemini")
                }
            }

        } catch (e: Exception) {
            GeminiCustomResult.Error(
                "Execution failed: ${e.localizedMessage ?: "Unknown Error"}. Please verify your internet connection and API Key."
            )
        }
    }

    // --- Helper to download an image from a URL and convert it to Base64 ---
    suspend fun downloadImageAsBase64(imageUrl: String): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL(imageUrl)
            val connection = url.openConnection()
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            val inputStream = connection.getInputStream()
            val bitmap = BitmapFactory.decodeStream(inputStream) ?: return@withContext null
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val bytes = outputStream.toByteArray()
            android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }
}
