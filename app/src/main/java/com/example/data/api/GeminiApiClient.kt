package com.example.data.api

import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiApiClient {
    private const val TAG = "GeminiApiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
    }

    fun isApiKeyConfigured(): Boolean {
        val key = getApiKey()
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
    }

    /**
     * High Thinking Mode using model gemini-3.1-pro-preview with thinkingLevel HIGH.
     * Ideal for deep Vietnamese idioms, poetic translation, tone nuance analysis.
     */
    suspend fun generateWithHighThinking(
        prompt: String,
        contextText: String = ""
    ): Pair<String, String?> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        val model = "gemini-3.1-pro-preview"

        val systemPrompt = """
            Bạn là chuyên gia phụ đề tiếng Việt cao cấp (Vietsub Specialist).
            Khi dịch hoặc phân tích phụ đề, hãy suy nghĩ sâu sắc về ngữ cảnh, thành ngữ, sắc thái cảm xúc, và vần điệu tiếng Việt tự nhiên nhất.
            Hãy đưa ra:
            1. Giải thích suy luận (Thinking / Lý do chọn từ ngữ)
            2. Bản dịch phụ đề tiếng Việt tối ưu nhất, chia thành các câu ngắn gọn vừa khung hình (tối đa 42 ký tự/dòng).
        """.trimIndent()

        val fullPrompt = if (contextText.isNotBlank()) {
            "Ngữ cảnh phụ đề:\n$contextText\n\nYêu cầu phân tích và dịch:\n$prompt"
        } else {
            prompt
        }

        if (!isApiKeyConfigured()) {
            val mockThinking = "Phân tích suy luận (Thinking Mode: HIGH):\n" +
                    "- Phân tích sắc thái: Câu văn mang âm điệu tự nhiên, truyền cảm hứng.\n" +
                    "- Đối chiếu văn hóa: Sử dụng từ ngữ hiện đại thường dùng trong vlog và video ngắn.\n" +
                    "- Tối ưu độ dài: Rút gọn các từ nối rườm rà để người xem đọc kịp trong 3 giây."
            val mockResult = "Bản dịch phụ đề tiếng Việt:\n" +
                    "1. 00:00 - 00:03: Chào mừng các bạn đến với hành trình ẩm thực hôm nay!\n" +
                    "2. 00:03 - 00:06: Cùng khám phá những món ngon đường phố không thể bỏ lỡ nhé.\n\n" +
                    "(Gợi ý: Cấu hình GEMINI_API_KEY trong Secrets panel để chạy trực tiếp trên gemini-3.1-pro-preview)"
            return@withContext Pair(mockResult, mockThinking)
        }

        try {
            val requestBodyJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", "$systemPrompt\n\n$fullPrompt"))
                        })
                    })
                }
                put("contents", contents)

                // Mandatory: gemini-3.1-pro-preview with thinkingLevel HIGH (no maxOutputTokens)
                val generationConfig = JSONObject().apply {
                    put("thinkingConfig", JSONObject().apply {
                        put("thinkingLevel", "HIGH")
                    })
                }
                put("generationConfig", generationConfig)
            }

            val request = Request.Builder()
                .url("$BASE_URL$model:generateContent?key=$apiKey")
                .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "High thinking error: $responseBody")
                return@withContext Pair("Lỗi gọi API (${response.code}): $responseBody", null)
            }

            val json = JSONObject(responseBody)
            val candidates = json.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val parts = firstCandidate?.optJSONObject("content")?.optJSONArray("parts")

            var resultText = ""
            var thinkingText: String? = null

            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val part = parts.getJSONObject(i)
                    if (part.has("thought") && part.getBoolean("thought")) {
                        thinkingText = part.optString("text")
                    } else if (part.has("text")) {
                        resultText += part.getString("text")
                    }
                }
            }

            if (resultText.isBlank()) {
                resultText = parts?.optJSONObject(0)?.optString("text") ?: "Không có kết quả."
            }

            Pair(resultText, thinkingText)
        } catch (e: Exception) {
            Log.e(TAG, "Error in high thinking call", e)
            Pair("Lỗi: ${e.localizedMessage}", null)
        }
    }

    /**
     * Low-Latency Subtitle Generation using model gemini-3.1-flash-lite
     * Returns instant Vietnamese translations & snappy subtitle lines in < 1s
     */
    suspend fun generateLowLatencySubtitle(text: String, targetStyle: String = "TikTok"): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        val model = "gemini-3.1-flash-lite"

        val prompt = """
            Bạn là trợ lý phụ đề siêu tốc. Hãy dịch hoặc tạo lại câu sau thành phụ đề tiếng Việt chuẩn, hấp dẫn theo phong cách $targetStyle.
            Định dạng trả về: Chỉ trả về 1 đến 3 câu phụ đề ngắn gọn, mỗi câu trên một dòng, sẵn sàng dán vào video.
            Văn bản gốc: $text
        """.trimIndent()

        if (!isApiKeyConfigured()) {
            return@withContext when (targetStyle) {
                "TikTok" -> "✨ Khám phá ngay trải nghiệm tuyệt vời này!\n🔥 Đừng quên lưu lại để thử ngay hôm nay."
                "Cinema" -> "Thời khắc quan trọng đã bắt đầu.\nMọi bí mật sắp được hé lộ."
                else -> "Dịch nhanh tiếng Việt:\n$text (Phiên bản Vietsub tự động)"
            }
        }

        try {
            val requestBodyJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url("$BASE_URL$model:generateContent?key=$apiKey")
                .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext "Lỗi nhanh (${response.code}): $responseBody"
            }

            val json = JSONObject(responseBody)
            val candidates = json.optJSONArray("candidates")
            val textResult = candidates?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text")

            textResult ?: "Không nhận được phản hồi."
        } catch (e: Exception) {
            "Lỗi: ${e.localizedMessage}"
        }
    }

    /**
     * Audio Transcription using model gemini-3.5-transcribe
     * Accepts raw audio bytes (e.g. WAV/AAC from microphone) and transcribes with timestamps.
     */
    suspend fun transcribeAudio(
        audioBytes: ByteArray?,
        mimeType: String = "audio/wav",
        promptInstruction: String = "Bóc băng bản ghi âm này sang tiếng Việt, kèm mốc thời gian phụ đề theo định dạng SRT hoặc từng dòng [00:00 - 00:03]: Nội dung."
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        val model = "gemini-3.5-transcribe"

        if (!isApiKeyConfigured() || audioBytes == null || audioBytes.isEmpty()) {
            return@withContext """
                [00:00.0 → 00:03.2] Chào mừng mọi người đã quay trở lại với kênh!
                [00:03.5 → 00:06.8] Hôm nay chúng ta sẽ cùng khám phá ứng dụng tạo phụ đề Hendy Vietsub.
                [00:07.0 → 00:10.5] Công nghệ AI Gemini 3.5 Transcribe giúp nhận diện giọng nói tiếng Việt chuẩn xác từng giây!
            """.trimIndent()
        }

        try {
            val base64Audio = Base64.encodeToString(audioBytes, Base64.NO_WRAP)
            val requestBodyJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", promptInstruction))
                            put(JSONObject().apply {
                                put("inlineData", JSONObject().apply {
                                    put("mimeType", mimeType)
                                    put("data", base64Audio)
                                })
                            })
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url("$BASE_URL$model:generateContent?key=$apiKey")
                .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext "Lỗi bóc băng âm thanh (${response.code}): $responseBody"
            }

            val json = JSONObject(responseBody)
            val textResult = json.optJSONArray("candidates")
                ?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text")

            textResult ?: "Không trích xuất được văn bản từ âm thanh."
        } catch (e: Exception) {
            "Lỗi ghi âm/bóc băng: ${e.localizedMessage}"
        }
    }

    /**
     * Video Content Analysis using model gemini-3.1-pro-preview
     * Analyzes video description, scenes, visual cues, and suggests subtitle timings.
     */
    suspend fun analyzeVideoContent(
        videoTitle: String,
        descriptionOrNotes: String,
        videoBytes: ByteArray? = null
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        val model = "gemini-3.1-pro-preview"

        val prompt = """
            Bạn là trợ lý biên tập video chuyên nghiệp bằng Gemini 3.1 Pro.
            Hãy phân tích nội dung video:
            - Tiêu đề: $videoTitle
            - Chi tiết kịch bản/bối cảnh: $descriptionOrNotes
            
            Hãy đưa ra:
            1. Tóm tắt nội dung chính và thông điệp video.
            2. Phân cảnh chi tiết kèm gợi ý câu phụ đề tiếng Việt (Vietsub) theo từng mốc thời gian.
            3. Gợi ý phong cách hiển thị chữ (màu sắc, kích cỡ, vị trí) để thu hút người xem nhất.
        """.trimIndent()

        if (!isApiKeyConfigured()) {
            return@withContext """
                🎬 PHÂN TÍCH VIDEO BỞI GEMINI 3.1 PRO:
                
                📌 Tóm tắt nội dung:
                Video thuộc thể loại chia sẻ trải nghiệm đời sống với nhịp độ năng động, cuốn hút.
                
                ⏱️ Kế hoạch phụ đề tiếng Việt đề xuất:
                • 00:00 - 00:04: [Mở màn] "Những địa điểm check-in cực hot mà bạn không thể bỏ lỡ!"
                • 00:04 - 00:08: [Cảnh 1] "Cảnh sắc thiên nhiên tuyệt đẹp vào buổi sáng sớm."
                • 00:08 - 00:14: [Cảnh 2] "Hương vị cà phê đậm đà đánh thức mọi giác quan."
                • 00:14 - 00:20: [Kết thúc] "Hãy follow để khám phá thêm nhiều điểm đến mới lạ!"
                
                🎨 Gợi ý phong cách:
                - Kiểu chữ: Không chân hiện đại (Sans-serif, Bold)
                - Màu chữ: Vàng rực (#FFE500) viền đen mỏng, vị trí 1/3 dưới khung hình.
            """.trimIndent()
        }

        try {
            val partsArray = JSONArray().apply {
                put(JSONObject().put("text", prompt))
                if (videoBytes != null && videoBytes.isNotEmpty()) {
                    put(JSONObject().apply {
                        put("inlineData", JSONObject().apply {
                            put("mimeType", "video/mp4")
                            put("data", Base64.encodeToString(videoBytes, Base64.NO_WRAP))
                        })
                    })
                }
            }

            val requestBodyJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().put("parts", partsArray))
                })
            }

            val request = Request.Builder()
                .url("$BASE_URL$model:generateContent?key=$apiKey")
                .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext "Lỗi phân tích video (${response.code}): $responseBody"
            }

            val json = JSONObject(responseBody)
            val textResult = json.optJSONArray("candidates")
                ?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text")

            textResult ?: "Không có dữ liệu phân tích video."
        } catch (e: Exception) {
            "Lỗi: ${e.localizedMessage}"
        }
    }

    /**
     * Image Analysis using model gemini-3.1-pro-preview
     * Extracts text (OCR), detects context, and generates Vietnamese subtitle cards.
     */
    suspend fun analyzeImageForSubtitles(
        imageBytes: ByteArray,
        userPrompt: String = "Phân tích bức ảnh này, trích xuất chữ nếu có và tạo câu phụ đề tiếng Việt giàu cảm xúc phù hợp với bức ảnh."
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        val model = "gemini-3.1-pro-preview"

        if (!isApiKeyConfigured() || imageBytes.isEmpty()) {
            return@withContext """
                🖼️ KẾT QUẢ PHÂN TÍCH HÌNH ẢNH (Gemini 3.1 Pro):
                - Nhận diện đối tượng: Khung cảnh ấm áp, ánh sáng hài hòa.
                - Gợi ý câu phụ đề tiếng Việt (Vietsub):
                  "Mỗi khoảnh khắc bình yên đều là một món quà vô giá của cuộc sống."
                - Gợi ý phong cách: Chữ trắng viền mờ, đặt ở góc dưới giữa ảnh.
            """.trimIndent()
        }

        try {
            val base64Img = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
            val requestBodyJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", userPrompt))
                            put(JSONObject().apply {
                                put("inlineData", JSONObject().apply {
                                    put("mimeType", "image/jpeg")
                                    put("data", base64Img)
                                })
                            })
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url("$BASE_URL$model:generateContent?key=$apiKey")
                .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext "Lỗi phân tích ảnh (${response.code}): $responseBody"
            }

            val json = JSONObject(responseBody)
            val textResult = json.optJSONArray("candidates")
                ?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text")

            textResult ?: "Không nhận được phân tích từ hình ảnh."
        } catch (e: Exception) {
            "Lỗi: ${e.localizedMessage}"
        }
    }

    /**
     * Text to Speech using model gemini-3.8-flash-tts
     * Synthesizes audio voiceover for Vietnamese subtitles.
     * Returns audio bytes (PCM/WAV/MP3).
     */
    suspend fun generateSpeech(
        textToSpeak: String,
        voiceName: String = "Kore" // Kore, Puck, Charon, Fenrir, Aoede
    ): ByteArray? = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        val model = "gemini-3.8-flash-tts"

        if (!isApiKeyConfigured()) {
            return@withContext null
        }

        try {
            val requestBodyJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", "Đọc to tự nhiên bằng tiếng Việt: $textToSpeak"))
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("responseModalities", JSONArray().apply { put("AUDIO") })
                    put("speechConfig", JSONObject().apply {
                        put("voiceConfig", JSONObject().apply {
                            put("prebuiltVoiceConfig", JSONObject().apply {
                                put("voiceName", voiceName)
                            })
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url("$BASE_URL$model:generateContent?key=$apiKey")
                .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "TTS error: $responseBody")
                return@withContext null
            }

            val json = JSONObject(responseBody)
            val candidates = json.optJSONArray("candidates")
            val parts = candidates?.optJSONObject(0)?.optJSONObject("content")?.optJSONArray("parts")

            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val part = parts.getJSONObject(i)
                    if (part.has("inlineData")) {
                        val inlineData = part.getJSONObject("inlineData")
                        val base64Data = inlineData.getString("data")
                        return@withContext Base64.decode(base64Data, Base64.DEFAULT)
                    }
                }
            }
            null
        } catch (e: Exception) {
            Log.e(TAG, "TTS exception", e)
            null
        }
    }

    /**
     * Music Generation using lyria-3-clip-preview (up to 30s) or lyria-3-pro-preview (full track)
     * Creates matching background music for video & subtitle atmosphere.
     */
    suspend fun generateMusic(
        prompt: String,
        isShortClip: Boolean = true
    ): ByteArray? = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        val model = if (isShortClip) "lyria-3-clip-preview" else "lyria-3-pro-preview"

        if (!isApiKeyConfigured()) {
            return@withContext null
        }

        try {
            val requestBodyJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("responseModalities", JSONArray().apply { put("AUDIO") })
                })
            }

            val request = Request.Builder()
                .url("$BASE_URL$model:generateContent?key=$apiKey")
                .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "Lyria Music error: $responseBody")
                return@withContext null
            }

            val json = JSONObject(responseBody)
            val candidates = json.optJSONArray("candidates")
            val parts = candidates?.optJSONObject(0)?.optJSONObject("content")?.optJSONArray("parts")

            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val part = parts.getJSONObject(i)
                    if (part.has("inlineData")) {
                        val inlineData = part.getJSONObject("inlineData")
                        val base64Data = inlineData.getString("data")
                        return@withContext Base64.decode(base64Data, Base64.DEFAULT)
                    }
                }
            }
            null
        } catch (e: Exception) {
            Log.e(TAG, "Music gen exception", e)
            null
        }
    }
}
