package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiApiClient
import com.example.data.audio.AudioPlayerManager
import com.example.data.audio.AudioRecorderManager
import com.example.data.model.MediaType
import com.example.data.model.SubtitleItem
import com.example.data.model.SubtitlePosition
import com.example.data.model.SubtitleStyle
import com.example.data.model.VietsubProject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ScreenDestination {
    TOOLS_HUB,
    SUBTITLE_EDITOR,
    HIGH_THINKING,
    AUDIO_TRANSCRIBE,
    VIDEO_ANALYSIS,
    LOW_LATENCY,
    IMAGE_ANALYSIS,
    TTS_VOICEOVER,
    MUSIC_GENERATOR,
    TELEPROMPTER
}

data class VietsubUiState(
    val currentScreen: ScreenDestination = ScreenDestination.TOOLS_HUB,
    val activeProject: VietsubProject = VietsubProject(),
    val savedProjects: List<VietsubProject> = emptyList(),
    val isLoading: Boolean = false,
    val loadingMessage: String = "",
    val errorMessage: String? = null,
    val isRecordingMic: Boolean = false,
    val micAmplitude: Int = 0,
    val recordedAudioBytes: ByteArray? = null,
    val isPlayingAudio: Boolean = false,
    val lastGeneratedAudioBytes: ByteArray? = null,
    // AI Specific Results
    val highThinkingResult: String = "",
    val highThinkingThoughtTrace: String? = null,
    val lowLatencyResult: String = "",
    val transcribeResult: String = "",
    val videoAnalysisResult: String = "",
    val imageAnalysisResult: String = "",
    val generatedMusicPrompt: String = "",
    val hasMusicAudio: Boolean = false,
    val hasTtsAudio: Boolean = false
)

class VietsubViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(VietsubUiState())
    val uiState: StateFlow<VietsubUiState> = _uiState.asStateFlow()

    private val audioPlayer = AudioPlayerManager(application)
    private val audioRecorder = AudioRecorderManager(application)
    private var recordingJob: Job? = null

    init {
        loadInitialSampleProject()
    }

    private fun loadInitialSampleProject() {
        val initialSubtitles = listOf(
            SubtitleItem(
                startMs = 0L,
                endMs = 3200L,
                vietnameseText = "Xin chào mọi người! Chào mừng đến với vlog hôm nay.",
                originalText = "Hello everyone! Welcome to today's vlog.",
                speaker = "Dẫn chuyện"
            ),
            SubtitleItem(
                startMs = 3500L,
                endMs = 7000L,
                vietnameseText = "Chúng ta đang có mặt tại phố cổ Hà Nội xinh đẹp.",
                originalText = "We are currently in the beautiful Hanoi Old Quarter.",
                speaker = "Dẫn chuyện"
            ),
            SubtitleItem(
                startMs = 7200L,
                endMs = 11500L,
                vietnameseText = "Món phở truyền thống ở đây thơm ngon nức tiếng gần xa.",
                originalText = "The traditional pho here is famously delicious near and far.",
                speaker = "Dẫn chuyện"
            ),
            SubtitleItem(
                startMs = 12000L,
                endMs = 15800L,
                vietnameseText = "Hãy cùng thưởng thức và cảm nhận hương vị đặc biệt này nhé!",
                originalText = "Let's taste and savor this special flavor together!",
                speaker = "Dẫn chuyện"
            )
        )

        val sampleProject = VietsubProject(
            title = "Khám phá Phố Cổ Hà Nội (Vietsub)",
            mediaType = MediaType.VIDEO,
            sampleVideoTitle = "Ẩm thực đường phố Hà Nội",
            subtitles = initialSubtitles,
            style = SubtitleStyle(
                fontSizeSp = 20f,
                textColorHex = "#FFE500", // Bright TikTok Yellow
                bgColorHex = "#80000000",
                position = SubtitlePosition.BOTTOM
            )
        )

        _uiState.update {
            it.copy(
                activeProject = sampleProject,
                savedProjects = listOf(sampleProject)
            )
        }
    }

    fun navigateTo(screen: ScreenDestination) {
        _uiState.update { it.copy(currentScreen = screen, errorMessage = null) }
    }

    fun navigateBack() {
        audioPlayer.stop()
        audioRecorder.cancel()
        recordingJob?.cancel()
        _uiState.update {
            it.copy(
                currentScreen = ScreenDestination.TOOLS_HUB,
                isRecordingMic = false,
                isPlayingAudio = false,
                errorMessage = null
            )
        }
    }

    fun createNewProject(title: String = "Dự án mới " + (System.currentTimeMillis() % 1000)) {
        val newProject = VietsubProject(
            title = title,
            mediaType = MediaType.VIDEO,
            subtitles = listOf(
                SubtitleItem(
                    startMs = 0L,
                    endMs = 3000L,
                    vietnameseText = "Thêm câu phụ đề tiếng Việt đầu tiên của bạn...",
                    speaker = "Người nói 1"
                )
            )
        )
        _uiState.update {
            it.copy(
                activeProject = newProject,
                savedProjects = listOf(newProject) + it.savedProjects,
                currentScreen = ScreenDestination.SUBTITLE_EDITOR
            )
        }
    }

    // Subtitle editing
    fun addSubtitleItem(startMs: Long = 0L, text: String = "Dòng phụ đề mới") {
        val currentSubs = _uiState.value.activeProject.subtitles.toMutableList()
        val calculatedStart = if (currentSubs.isNotEmpty()) currentSubs.last().endMs + 500L else startMs
        val calculatedEnd = calculatedStart + 3000L

        val newItem = SubtitleItem(
            startMs = calculatedStart,
            endMs = calculatedEnd,
            vietnameseText = text
        )
        currentSubs.add(newItem)
        updateProjectSubtitles(currentSubs)
    }

    fun updateSubtitleItem(id: String, newText: String, startMs: Long, endMs: Long) {
        val updated = _uiState.value.activeProject.subtitles.map { item ->
            if (item.id == id) {
                item.copy(
                    vietnameseText = newText,
                    startMs = startMs,
                    endMs = endMs
                )
            } else item
        }
        updateProjectSubtitles(updated)
    }

    fun deleteSubtitleItem(id: String) {
        val updated = _uiState.value.activeProject.subtitles.filterNot { it.id == id }
        updateProjectSubtitles(updated)
    }

    fun updateSubtitleStyle(
        textColorHex: String? = null,
        bgColorHex: String? = null,
        fontSizeSp: Float? = null,
        position: SubtitlePosition? = null
    ) {
        val currentStyle = _uiState.value.activeProject.style
        val newStyle = currentStyle.copy(
            textColorHex = textColorHex ?: currentStyle.textColorHex,
            bgColorHex = bgColorHex ?: currentStyle.bgColorHex,
            fontSizeSp = fontSizeSp ?: currentStyle.fontSizeSp,
            position = position ?: currentStyle.position
        )
        val updatedProject = _uiState.value.activeProject.copy(
            style = newStyle,
            updatedAt = System.currentTimeMillis()
        )
        _uiState.update { it.copy(activeProject = updatedProject) }
    }

    private fun updateProjectSubtitles(subtitles: List<SubtitleItem>) {
        val updatedProject = _uiState.value.activeProject.copy(
            subtitles = subtitles,
            updatedAt = System.currentTimeMillis()
        )
        _uiState.update { it.copy(activeProject = updatedProject) }
    }

    // API calls

    /**
     * High Thinking Mode: gemini-3.1-pro-preview with thinkingLevel HIGH
     */
    fun runHighThinkingTranslation(query: String, contextText: String = "") {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    loadingMessage = "Gemini 3.1 Pro đang suy luận sâu (Thinking: HIGH)...",
                    errorMessage = null
                )
            }
            try {
                val (result, thinkingTrace) = GeminiApiClient.generateWithHighThinking(query, contextText)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        highThinkingResult = result,
                        highThinkingThoughtTrace = thinkingTrace
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Lỗi xử lý: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    /**
     * Low-Latency Subtitle Generation: gemini-3.1-flash-lite
     */
    fun runLowLatencySubtitle(text: String, style: String = "TikTok") {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    loadingMessage = "Gemini 3.1 Flash-Lite đang xử lý siêu tốc...",
                    errorMessage = null
                )
            }
            try {
                val result = GeminiApiClient.generateLowLatencySubtitle(text, style)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        lowLatencyResult = result
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Lỗi xử lý: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    /**
     * Audio Transcription: gemini-3.5-transcribe
     */
    fun runAudioTranscription(audioBytes: ByteArray? = _uiState.value.recordedAudioBytes) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    loadingMessage = "Gemini 3.5 Transcribe đang bóc băng âm thanh...",
                    errorMessage = null
                )
            }
            try {
                val result = GeminiApiClient.transcribeAudio(audioBytes)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        transcribeResult = result
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Lỗi: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    /**
     * Video Content Analysis: gemini-3.1-pro-preview
     */
    fun runVideoAnalysis(title: String, notes: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    loadingMessage = "Gemini 3.1 Pro đang phân tích nội dung video...",
                    errorMessage = null
                )
            }
            try {
                val result = GeminiApiClient.analyzeVideoContent(title, notes)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        videoAnalysisResult = result
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Lỗi: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    /**
     * Image Analysis: gemini-3.1-pro-preview
     */
    fun runImageAnalysis(imageBytes: ByteArray, prompt: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    loadingMessage = "Gemini 3.1 Pro đang đọc ảnh và tạo phụ đề...",
                    errorMessage = null
                )
            }
            try {
                val result = GeminiApiClient.analyzeImageForSubtitles(imageBytes, prompt)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        imageAnalysisResult = result
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Lỗi: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    /**
     * Text to Speech (TTS): gemini-3.8-flash-tts
     */
    fun runTtsSynthesis(text: String, voiceName: String = "Kore") {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    loadingMessage = "Gemini 3.8 Flash-TTS đang tổng hợp giọng đọc...",
                    errorMessage = null
                )
            }
            try {
                val audioBytes = GeminiApiClient.generateSpeech(text, voiceName)
                if (audioBytes != null && audioBytes.isNotEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            lastGeneratedAudioBytes = audioBytes,
                            hasTtsAudio = true
                        )
                    }
                    audioPlayer.playAudioBytes(audioBytes) {
                        _uiState.update { it.copy(isPlayingAudio = false) }
                    }
                    _uiState.update { it.copy(isPlayingAudio = true) }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            hasTtsAudio = false,
                            errorMessage = "TTS đã tạo kịch bản đọc. Hãy thêm GEMINI_API_KEY để nghe âm thanh trực tiếp từ Gemini 3.8 Flash-TTS."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Lỗi TTS: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    /**
     * Music Generation: lyria-3-clip-preview / lyria-3-pro-preview
     */
    fun runMusicGeneration(prompt: String, isShortClip: Boolean = true) {
        viewModelScope.launch {
            val modelName = if (isShortClip) "Lyria 3 Clip (30s)" else "Lyria 3 Pro"
            _uiState.update {
                it.copy(
                    isLoading = true,
                    loadingMessage = "$modelName đang tạo nhạc nền video...",
                    errorMessage = null
                )
            }
            try {
                val musicBytes = GeminiApiClient.generateMusic(prompt, isShortClip)
                if (musicBytes != null && musicBytes.isNotEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            lastGeneratedAudioBytes = musicBytes,
                            hasMusicAudio = true,
                            generatedMusicPrompt = prompt
                        )
                    }
                    audioPlayer.playAudioBytes(musicBytes) {
                        _uiState.update { it.copy(isPlayingAudio = false) }
                    }
                    _uiState.update { it.copy(isPlayingAudio = true) }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            hasMusicAudio = true,
                            generatedMusicPrompt = prompt,
                            errorMessage = "Đã lên cấu hình âm nhạc $modelName. Cung cấp API key để truyền phát trực tiếp từ Lyria."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Lỗi tạo nhạc: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    // Audio Playback
    fun togglePlayGeneratedAudio() {
        val bytes = _uiState.value.lastGeneratedAudioBytes
        if (bytes == null || bytes.isEmpty()) return

        if (_uiState.value.isPlayingAudio) {
            audioPlayer.stop()
            _uiState.update { it.copy(isPlayingAudio = false) }
        } else {
            audioPlayer.playAudioBytes(bytes) {
                _uiState.update { it.copy(isPlayingAudio = false) }
            }
            _uiState.update { it.copy(isPlayingAudio = true) }
        }
    }

    // Audio Recorder
    fun startMicRecording(): Boolean {
        val started = audioRecorder.startRecording()
        if (started) {
            _uiState.update { it.copy(isRecordingMic = true, micAmplitude = 0) }
            recordingJob?.cancel()
            recordingJob = viewModelScope.launch {
                while (_uiState.value.isRecordingMic) {
                    val amp = audioRecorder.getMaxAmplitude()
                    _uiState.update { it.copy(micAmplitude = amp) }
                    delay(100)
                }
            }
        }
        return started
    }

    fun stopMicRecording() {
        val bytes = audioRecorder.stopRecording()
        recordingJob?.cancel()
        _uiState.update {
            it.copy(
                isRecordingMic = false,
                micAmplitude = 0,
                recordedAudioBytes = bytes
            )
        }
        if (bytes != null && bytes.isNotEmpty()) {
            runAudioTranscription(bytes)
        }
    }

    // Convert active project subtitles to full SRT text
    fun exportToSrt(): String {
        val subs = _uiState.value.activeProject.subtitles
        return buildString {
            subs.forEachIndexed { index, item ->
                append(item.toSrtBlock(index + 1))
                append("\n")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
        audioRecorder.cancel()
    }
}
