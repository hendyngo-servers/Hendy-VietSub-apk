package com.example.data.model

import java.util.UUID

enum class MediaType {
    VIDEO,
    IMAGE,
    AUDIO,
    TEXT
}

enum class SubtitlePosition {
    TOP,
    CENTER,
    BOTTOM
}

data class SubtitleStyle(
    val fontSizeSp: Float = 18f,
    val textColorHex: String = "#FFFF00", // Default Yellow TikTok style
    val bgColorHex: String = "#B3000000",  // Semi-transparent black
    val hasOutline: Boolean = true,
    val outlineColorHex: String = "#000000",
    val position: SubtitlePosition = SubtitlePosition.BOTTOM,
    val isBold: Boolean = true
)

data class SubtitleItem(
    val id: String = UUID.randomUUID().toString(),
    val startMs: Long = 0L,
    val endMs: Long = 3000L,
    val vietnameseText: String = "",
    val originalText: String = "",
    val speaker: String = "Người nói 1"
) {
    fun formatTimestamp(): String {
        val startSec = startMs / 1000
        val startMilli = (startMs % 1000) / 100
        val endSec = endMs / 1000
        val endMilli = (endMs % 1000) / 100
        return "%02d:%02d.%d → %02d:%02d.%d".format(
            startSec / 60, startSec % 60, startMilli,
            endSec / 60, endSec % 60, endMilli
        )
    }

    fun toSrtBlock(index: Int): String {
        fun formatSrtTime(ms: Long): String {
            val hours = ms / 3600000
            val minutes = (ms % 3600000) / 60000
            val seconds = (ms % 60000) / 1000
            val millis = ms % 1000
            return "%02d:%02d:%02d,%03d".format(hours, minutes, seconds, millis)
        }
        return "$index\n${formatSrtTime(startMs)} --> ${formatSrtTime(endMs)}\n$vietnameseText\n"
    }
}

data class VietsubProject(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Dự án Vietsub mới",
    val mediaType: MediaType = MediaType.VIDEO,
    val sampleVideoTitle: String = "Vlog khám phá ẩm thực Hà Nội",
    val subtitles: List<SubtitleItem> = emptyList(),
    val style: SubtitleStyle = SubtitleStyle(),
    val voiceoverAudioPath: String? = null,
    val backgroundMusicPath: String? = null,
    val backgroundMusicName: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

data class ToolItem(
    val id: String,
    val title: String,
    val category: String, // "Thao tác nhanh", "Công cụ AI", "Chỉnh sửa ảnh"
    val iconName: String,
    val description: String = "",
    val badge: String? = null
)

object PredefinedTools {
    val QUICK_ACTIONS = listOf(
        ToolItem("beautify", "Làm đẹp", "Thao tác nhanh", "face_retouching_natural", "Bộ lọc làm đẹp chân dung video"),
        ToolItem("auto_sub", "Phụ đề tự động", "Thao tác nhanh", "subtitles", "Nhận dạng giọng nói tạo Vietsub tức thì", "HOT"),
        ToolItem("teleprompter", "Máy nhắc chữ", "Thao tác nhanh", "desktop_windows", "Cuộn kịch bản tự động khi quay video"),
        ToolItem("camera", "Máy ảnh", "Thao tác nhanh", "photo_camera", "Quay video và chụp ảnh hỗ trợ phụ đề trực tiếp"),
        ToolItem("auto_enhance", "Tự động cải thiện", "Thao tác nhanh", "auto_fix_high", "Tối ưu màu sắc, âm thanh tự động"),
        ToolItem("speed", "Điều chỉnh tốc độ", "Thao tác nhanh", "speed", "Tua nhanh/chậm đồng bộ phụ đề"),
        ToolItem("record", "Ghi âm", "Thao tác nhanh", "mic", "Ghi âm giọng đọc chuyển phụ đề Vietsub"),
        ToolItem("collage", "Ghép ảnh", "Thao tác nhanh", "view_quilt", "Ghép nhiều ảnh kèm phụ đề trích dẫn"),
        ToolItem("frame_capture", "Chụp khung hình", "Thao tác nhanh", "crop", "Chụp khoảnh khắc video tạo ảnh trích dẫn"),
        ToolItem("desktop_edit", "Chỉnh sửa máy tính", "Thao tác nhanh", "laptop", "Đồng bộ dự án sang máy tính")
    )

    val AI_TOOLS = listOf(
        ToolItem("auto_cut", "AutoCut", "Công cụ AI", "movie_filter", "Cắt ghép video thông minh theo phụ đề", "AI"),
        ToolItem("ai_avatar", "Ảnh đại diện AI", "Công cụ AI", "account_box", "Tạo avatar người dẫn chuyện AI"),
        ToolItem("video_translator", "Trình dịch video", "Công cụ AI", "translate", "Dịch phụ đề đa ngôn ngữ sang tiếng Việt chuẩn"),
        ToolItem("ai_dialogue", "Cảnh đối thoại AI", "Công cụ AI", "forum", "Tạo kịch bản đối thoại hai nhân vật"),
        ToolItem("ai_image", "Tạo hình ảnh bằng AI", "Công cụ AI", "image", "Vẽ ảnh minh họa cho câu phụ đề"),
        ToolItem("ai_effects", "Hiệu ứng AI", "Công cụ AI", "auto_awesome", "Hiệu ứng chữ phụ đề phát sáng chuyển động"),
        ToolItem("ai_video", "Tạo video bằng AI", "Công cụ AI", "video_call", "Tạo clip video ngắn từ văn bản kịch bản"),
        ToolItem("ai_cutout", "Công cụ cắt bằng AI", "Công cụ AI", "content_cut", "Tách người khỏi phông nền video"),
        ToolItem("ai_trends", "Xu hướng AI", "Công cụ AI", "trending_up", "Mẫu phụ đề TikTok/Reels đang thịnh hành"),
        ToolItem("ai_media_bundle", "Tạo tệp phương tiện", "Công cụ AI", "video_library", "Tạo gói video + âm thanh + phụ đề hoàn chỉnh"),
        // Specific mandatory models tools
        ToolItem("high_thinking", "Phân tích sâu (Thinking)", "Công cụ AI", "psychology", "Dịch thơ, ca dao, thành ngữ bằng Gemini 3.1 Pro High Thinking", "PRO"),
        ToolItem("gemini_transcribe", "Gemini 3.5 Transcribe", "Công cụ AI", "hearing", "Bóc băng âm thanh siêu chuẩn xác", "MỚI"),
        ToolItem("video_understanding", "Phân tích video", "Công cụ AI", "smart_display", "Hiểu nội dung video và tóm tắt cảnh quay", "AI"),
        ToolItem("fast_flash_lite", "Phụ đề siêu tốc", "Công cụ AI", "bolt", "Gemini 3.1 Flash-Lite phản hồi dưới 1 giây", "LITE"),
        ToolItem("gemini_tts", "Tạo giọng nói AI (TTS)", "Công cụ AI", "record_voice_over", "Đọc phụ đề thành giọng nói bằng Gemini 3.8 Flash-TTS", "TTS"),
        ToolItem("lyria_music", "Tạo nhạc nền (Lyria)", "Công cụ AI", "music_note", "Sinh nhạc nền video bằng Lyria 3", "MUSIC")
    )

    val PHOTO_TOOLS = listOf(
        ToolItem("photo_editor", "Trình chỉnh sửa ảnh", "Chỉnh sửa ảnh", "edit", "Chèn phụ đề, chỉnh màu ảnh"),
        ToolItem("bg_remove", "Xóa nền", "Chỉnh sửa ảnh", "person_remove", "Xóa phông ảnh tạo ảnh đại diện"),
        ToolItem("smart_lighting", "Ánh sáng thông minh", "Chỉnh sửa ảnh", "wb_incandescent", "Làm nổi bật vùng chữ phụ đề"),
        ToolItem("ai_expand", "Mở rộng bằng AI", "Chỉnh sửa ảnh", "aspect_ratio", "Mở rộng góc nhìn ảnh theo tỷ lệ 16:9, 9:16")
    )
}
