package com.example.data.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.FileInputStream

class AudioRecorderManager(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var currentFile: File? = null
    var isRecording: Boolean = false
        private set

    fun startRecording(): Boolean {
        return try {
            val file = File.createTempFile("vietsub_rec_", ".m4a", context.cacheDir)
            currentFile = file

            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            isRecording = true
            true
        } catch (e: Exception) {
            Log.e("AudioRecorderManager", "Error starting recording", e)
            isRecording = false
            false
        }
    }

    fun getMaxAmplitude(): Int {
        return try {
            if (isRecording) recorder?.maxAmplitude ?: 0 else 0
        } catch (e: Exception) {
            0
        }
    }

    fun stopRecording(): ByteArray? {
        if (!isRecording) return null
        return try {
            recorder?.stop()
            recorder?.release()
            recorder = null
            isRecording = false

            currentFile?.let { file ->
                if (file.exists() && file.length() > 0) {
                    val bytes = ByteArray(file.length().toInt())
                    FileInputStream(file).use { it.read(bytes) }
                    bytes
                } else null
            }
        } catch (e: Exception) {
            Log.e("AudioRecorderManager", "Error stopping recording", e)
            recorder = null
            isRecording = false
            null
        }
    }

    fun cancel() {
        try {
            if (isRecording) {
                recorder?.stop()
            }
        } catch (e: Exception) {
            // Ignore
        } finally {
            recorder?.release()
            recorder = null
            isRecording = false
            currentFile?.delete()
            currentFile = null
        }
    }
}
