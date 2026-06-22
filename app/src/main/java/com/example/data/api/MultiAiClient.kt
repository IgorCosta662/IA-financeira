package com.example.data.api

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class AiProvider(val displayName: String, val defaultModel: String, val defaultUrl: String) {
    GOOGLE_DEFAULT("Google (Padrão do App)", "gemini-3.5-flash", "https://generativelanguage.googleapis.com/"),
    GOOGLE_CUSTOM("Google Gemini (Chave Própria)", "gemini-3.5-flash", "https://generativelanguage.googleapis.com/"),
    OPENAI("OpenAI", "gpt-4o-mini", "https://api.openai.com/v1/"),
    ANTHROPIC("Anthropic Claude", "claude-3-5-sonnet-latest", "https://api.anthropic.com/v1/"),
    XAI("xAI Grok", "grok-2-1212", "https://api.x.ai/v1/"),
    CUSTOM("Outros (OpenAI Compatível)", "custom-model", "")
}

enum class ConnectionStatus {
    NOT_TESTED,
    CONNECTED,
    INVALID_KEY,
    NO_CONNECTION
}

object MultiAiClient {
    private const val TAG = "MultiAiClient"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val mediaTypeJson = "application/json; charset=utf-8".toMediaType()

    /**
     * Estimates tokens using standard character-based heuristic.
     * Character length / 4 is a standard dynamic approximation for tokens.
     */
    fun estimateTokens(text: String): Int {
        if (text.isEmpty()) return 0
        return (text.length / 4).coerceAtLeast(1)
    }

    suspend fun testConnection(
        provider: AiProvider,
        apiKey: String,
        model: String,
        customUrl: String = ""
    ): ConnectionStatus = withContext(Dispatchers.IO) {
        if (provider == AiProvider.GOOGLE_DEFAULT) {
            return@withContext ConnectionStatus.CONNECTED
        }
        if (apiKey.isEmpty()) {
            return@withContext ConnectionStatus.NO_CONNECTION
        }

        val testPrompt = "Ping"
        try {
            val responseText = executeRequest(
                provider = provider,
                apiKey = apiKey,
                model = model,
                prompt = testPrompt,
                systemInstruction = "Responda apenas 'Pong' ou 'Ok'",
                customUrl = customUrl,
                maxTokensLimit = 5
            )

            if (responseText.contains("Erro") && (responseText.contains("401") || responseText.contains("403") || responseText.contains("unauthorized") || responseText.contains("invalid_api_key") || responseText.contains("Chave inválida"))) {
                ConnectionStatus.INVALID_KEY
            } else if (responseText.startsWith("Erro ao conectar") || responseText.contains("UnknownHostException") || responseText.contains("timeout")) {
                ConnectionStatus.NO_CONNECTION
            } else {
                ConnectionStatus.CONNECTED
            }
        } catch (e: Exception) {
            Log.e(TAG, "Test Connection failed for $provider: ${e.message}")
            if (e is UnknownHostException || e is IOException) {
                ConnectionStatus.NO_CONNECTION
            } else {
                ConnectionStatus.INVALID_KEY
            }
        }
    }

    suspend fun generateContent(
        provider: AiProvider,
        apiKey: String,
        model: String,
        prompt: String,
        systemInstruction: String,
        customUrl: String = "",
        onTokensEstimated: (input: Int, output: Int) -> Unit = { _, _ -> }
    ): String = withContext(Dispatchers.IO) {
        val inputTokens = estimateTokens(systemInstruction) + estimateTokens(prompt)
        
        val actualApiKey = if (provider == AiProvider.GOOGLE_DEFAULT) {
            com.example.BuildConfig.GEMINI_API_KEY
        } else {
            apiKey
        }

        if (actualApiKey.isEmpty() || actualApiKey == "MY_GEMINI_API_KEY") {
            if (provider == AiProvider.GOOGLE_DEFAULT) {
                return@withContext "Para conversar com o Assistente de IA, configure a sua chave GEMINI_API_KEY no painel de Secrets do AI Studio ou insira sua própria chave nas Configurações de IA."
            }
            return@withContext "Por favor, insira uma Chave de API válida para o provedor ${provider.displayName} nas Configurações de IA."
        }

        try {
            val responseText = executeRequest(
                provider = provider,
                apiKey = actualApiKey,
                model = model,
                prompt = prompt,
                systemInstruction = systemInstruction,
                customUrl = customUrl
            )

            val outputTokens = estimateTokens(responseText)
            onTokensEstimated(inputTokens, outputTokens)
            responseText
        } catch (e: Exception) {
            Log.e(TAG, "Generation failed", e)
            val niceError = when (e) {
                is UnknownHostException -> "Sem conexão com a Internet. Verifique sua rede."
                is IOException -> "Erro de rede ao comunicar com o servidor da AI: ${e.localizedMessage}"
                else -> "Erro: ${e.localizedMessage}"
            }
            niceError
        }
    }

    private fun executeRequest(
        provider: AiProvider,
        apiKey: String,
        model: String,
        prompt: String,
        systemInstruction: String,
        customUrl: String,
        maxTokensLimit: Int? = null
    ): String {
        val requestBuilder = Request.Builder()
        var url = ""
        var jsonBodyStr = ""

        when (provider) {
            AiProvider.GOOGLE_DEFAULT, AiProvider.GOOGLE_CUSTOM -> {
                val resolvedModel = model.ifEmpty { "gemini-3.5-flash" }
                url = "${AiProvider.GOOGLE_DEFAULT.defaultUrl}v1beta/models/$resolvedModel:generateContent?key=$apiKey"
                
                val reqJson = JSONObject().apply {
                    val contentsArr = JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply { put("text", prompt) })
                            })
                        })
                    }
                    put("contents", contentsArr)
                    
                    if (systemInstruction.isNotEmpty()) {
                        put("systemInstruction", JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply { put("text", systemInstruction) })
                            })
                        })
                    }

                    if (maxTokensLimit != null) {
                        put("generationConfig", JSONObject().apply {
                            put("maxOutputTokens", maxTokensLimit)
                        })
                    }
                }
                jsonBodyStr = reqJson.toString()
                requestBuilder.post(jsonBodyStr.toRequestBody(mediaTypeJson))
            }

            AiProvider.OPENAI, AiProvider.XAI, AiProvider.CUSTOM -> {
                val baseUrl = when (provider) {
                    AiProvider.OPENAI -> AiProvider.OPENAI.defaultUrl
                    AiProvider.XAI -> AiProvider.XAI.defaultUrl
                    else -> if (customUrl.endsWith("/")) customUrl else "$customUrl/"
                }

                url = "${baseUrl}chat/completions"
                val resolvedModel = model.ifEmpty {
                    if (provider == AiProvider.OPENAI) "gpt-4o-mini" else "grok-2-1212"
                }

                val reqJson = JSONObject().apply {
                    put("model", resolvedModel)
                    val messagesArr = JSONArray().apply {
                        if (systemInstruction.isNotEmpty()) {
                            put(JSONObject().apply {
                                put("role", "system")
                                put("content", systemInstruction)
                            })
                        }
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", prompt)
                        })
                    }
                    put("messages", messagesArr)
                    if (maxTokensLimit != null) {
                        put("max_tokens", maxTokensLimit)
                    }
                }
                jsonBodyStr = reqJson.toString()

                requestBuilder.url(url)
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("Content-Type", "application/json")
                    .post(jsonBodyStr.toRequestBody(mediaTypeJson))
            }

            AiProvider.ANTHROPIC -> {
                url = "${AiProvider.ANTHROPIC.defaultUrl}v1/messages"
                val resolvedModel = model.ifEmpty { "claude-3-5-sonnet-latest" }

                val reqJson = JSONObject().apply {
                    put("model", resolvedModel)
                    put("max_tokens", maxTokensLimit ?: 2048)
                    if (systemInstruction.isNotEmpty()) {
                        put("system", systemInstruction)
                    }
                    val messagesArr = JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", prompt)
                        })
                    }
                    put("messages", messagesArr)
                }
                jsonBodyStr = reqJson.toString()

                requestBuilder.url(url)
                    .addHeader("x-api-key", apiKey)
                    .addHeader("anthropic-version", "2023-06-01")
                    .addHeader("content-type", "application/json")
                    .post(jsonBodyStr.toRequestBody(mediaTypeJson))
            }
        }

        if (url.isEmpty()) {
            return "Erro: Provedor de IA inválido ou não configurado."
        }

        requestBuilder.url(url)
        val request = requestBuilder.build()

        client.newCall(request).execute().use { response ->
            val code = response.code
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "Request failed code $code body: $body")
                return "Erro (Código HTTP $code): " + when (code) {
                    401, 403 -> "Chave inválida ou não autorizada pelo provedor de IA."
                    404 -> "Modelo não encontrado ou URL de endpoint incorreta."
                    429 -> "Limite de requisições excedido. Verifique seus limites de faturamento no provedor."
                    else -> {
                        try {
                            val errJson = JSONObject(body)
                            errJson.optJSONObject("error")?.optString("message") ?: body
                        } catch (e: Exception) {
                            body.take(200)
                        }
                    }
                }
            }

            // Parse success response
            return try {
                when (provider) {
                    AiProvider.GOOGLE_DEFAULT, AiProvider.GOOGLE_CUSTOM -> {
                        val root = JSONObject(body)
                        root.getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text")
                    }

                    AiProvider.OPENAI, AiProvider.XAI, AiProvider.CUSTOM -> {
                        val root = JSONObject(body)
                        root.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content")
                    }

                    AiProvider.ANTHROPIC -> {
                        val root = JSONObject(body)
                        root.getJSONArray("content")
                            .getJSONObject(0)
                            .getString("text")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "JSON parsing error for response: $body", e)
                "Erro ao processar as informações retornadas pela IA."
            }
        }
    }
}
