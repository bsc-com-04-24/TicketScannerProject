package com.example.ticket_scanner.util

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import com.example.ticket_scanner.model.HistoryStorageManager

object ScanFeedback {
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        } catch (_: Exception) {
            toneGenerator = null
        }
    }

    fun playSuccess(context: Context) {
        if (!HistoryStorageManager.loadSoundVibrationEnabled(context)) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
        } catch (_: Exception) { }
        vibrate(context, 100)
    }

    fun playError(context: Context) {
        if (!HistoryStorageManager.loadSoundVibrationEnabled(context)) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_SUP_ERROR, 300)
        } catch (_: Exception) { }
        vibrate(context, 300)
    }

    @SuppressLint("MissingPermission")
    private fun vibrate(context: Context, durationMs: Long) {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(durationMs)
                }
            }
        } catch (_: Exception) { }
    }
}
