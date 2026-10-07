package com.example.data.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream

class AudioPlayerManager(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    var isPlaying: Boolean = false
        private set

    fun playAudioBytes(bytes: ByteArray, onCompletion: () -> Unit = {}) {
        stop()
        try {
            val tempFile = File.createTempFile("vietsub_audio_", ".wav", context.cacheDir)
            FileOutputStream(tempFile).use { it.write(bytes) }

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(context, Uri.fromFile(tempFile))
                prepare()
                start()
                this@AudioPlayerManager.isPlaying = true
                setOnCompletionListener {
                    this@AudioPlayerManager.isPlaying = false
                    onCompletion()
                }
            }
        } catch (e: Exception) {
            Log.e("AudioPlayerManager", "Failed to play audio bytes", e)
            isPlaying = false
        }
    }

    fun stop() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // Ignore
        } finally {
            mediaPlayer = null
            isPlaying = false
        }
    }

    fun release() {
        stop()
    }
}
