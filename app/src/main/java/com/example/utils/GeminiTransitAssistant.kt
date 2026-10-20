package com.example.utils

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.firebase.FirebaseManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class WaybillAnalysis(
    val recommendedSeats: Int = 1,
    val suggestedFareNaira: Int = 500,
    val cargoCategory: String = "Standard Goods",
    val handlingAdvice: String = "Secure properly in tricycle rear bay",
    val estimatedWeightKg: Int = 20
)

object GeminiTransitAssistant {
    private const val MODEL_NAME = "gemini-2.5-flash"

    suspend fun analyzeWaybillCargo(
        context: Context,
        cargoDescription: String,
        originMarket: String,
        destinationMarket: String
    ): WaybillAnalysis = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.MAPS_API_KEY.takeIf { it.isNotEmpty() && !it.startsWith("AIzaSy_MOCK") }
        if (apiKey == null) {
            return@withContext fallbackAnalysis(cargoDescription)
        }

        try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent?key=$apiKey"
            val prompt = """
                Analyze the following freight cargo description for commercial tricycle transport between $originMarket and $destinationMarket in Abia State, Nigeria.
                Cargo: "$cargoDescription"
                
                Respond strictly in valid JSON format with keys:
                - "seats": Integer (1 to 3 passenger seats consumed)
                - "fare": Integer (suggested fair market price in Nigerian Naira, typically 500 to 2500)
                - "category": String (e.g. Agricultural produce, Textile, Hardware, Merchant parcels)
                - "advice": String (One short sentence advice for the tricycle rider)
                - "weight": Integer (Estimated weight in KG)
            """.trimIndent()

            val requestBody = JSONObject().apply {
                put("contents", org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", org.json.JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
            }

            val url = URL(endpoint)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.outputStream.use { it.write(requestBody.toString().toByteArray()) }

            if (conn.responseCode == 200) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(responseText)
                val candidates = jsonResponse.optJSONArray("candidates")
                val text = candidates?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text") ?: ""
                parseWaybillJson(text)
            } else {
                fallbackAnalysis(cargoDescription)
            }
        } catch (e: Exception) {
            Log.w("GeminiAssistant", "Gemini API call failed, using heuristic: ${e.message}")
            fallbackAnalysis(cargoDescription)
        }
    }

    suspend fun askCorridorQuestion(context: Context, question: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.MAPS_API_KEY.takeIf { it.isNotEmpty() && !it.startsWith("AIzaSy_MOCK") }
            ?: return@withContext "Bata Junction, Ariaria Market, and Ekeoha Plaza corridors are active with standard regulated ATRWAN tariffs."

        try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent?key=$apiKey"
            val prompt = "Answer the commuter question concisely (under 3 sentences) in natural Nigerian English. Question: $question"

            val requestBody = JSONObject().apply {
                put("contents", org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", org.json.JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
            }

            val url = URL(endpoint)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.outputStream.use { it.write(requestBody.toString().toByteArray()) }

            if (conn.responseCode == 200) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(responseText)
                val text = jsonResponse.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text") ?: ""
                text.ifEmpty { "Corridors are open and active. Book a Keke to travel safely across Aba and Umuahia." }
            } else {
                "Corridors are operating normally across Aba and Umuahia."
            }
        } catch (e: Exception) {
            "Bata Junction, Ariaria International Market, and Ekeoha Plaza corridors are running normally today."
        }
    }

    private fun parseWaybillJson(jsonText: String): WaybillAnalysis {
        return try {
            val cleaned = jsonText.replace("```json", "").replace("```", "").trim()
            val seatsMatch = Regex(""""seats"\s*:\s*(\d+)""").find(cleaned)?.groupValues?.get(1)?.toIntOrNull() ?: 1
            val fareMatch = Regex(""""fare"\s*:\s*(\d+)""").find(cleaned)?.groupValues?.get(1)?.toIntOrNull() ?: 700
            val catMatch = Regex(""""category"\s*:\s*"([^"]+)"""").find(cleaned)?.groupValues?.get(1) ?: "Market Merchandise"
            val adviceMatch = Regex(""""advice"\s*:\s*"([^"]+)"""").find(cleaned)?.groupValues?.get(1) ?: "Secure goods properly"
            val weightMatch = Regex(""""weight"\s*:\s*(\d+)""").find(cleaned)?.groupValues?.get(1)?.toIntOrNull() ?: 25

            WaybillAnalysis(
                recommendedSeats = seatsMatch.coerceIn(1, 3),
                suggestedFareNaira = fareMatch.coerceIn(400, 3500),
                cargoCategory = catMatch,
                handlingAdvice = adviceMatch,
                estimatedWeightKg = weightMatch
            )
        } catch (_: Exception) {
            fallbackAnalysis(jsonText)
        }
    }

    private fun fallbackAnalysis(desc: String): WaybillAnalysis {
        val lower = desc.lowercase()
        val isHeavy = lower.contains("bag") || lower.contains("sack") || lower.contains("basin") || lower.contains("carton")
        return WaybillAnalysis(
            recommendedSeats = if (isHeavy) 2 else 1,
            suggestedFareNaira = if (isHeavy) 800 else 500,
            cargoCategory = if (isHeavy) "Heavy Market Sacks" else "Standard Parcel",
            handlingAdvice = "Position securely in the tricycle rear compartment.",
            estimatedWeightKg = if (isHeavy) 45 else 15
        )
    }
}
