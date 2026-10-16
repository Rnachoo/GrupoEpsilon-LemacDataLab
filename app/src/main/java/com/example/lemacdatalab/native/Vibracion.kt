package com.example.lemacdatalab.native

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

// Funcion para utilizar la vibracion del telefono
fun vibrarAlGuardar(context: Context, esError: Boolean = false) {

    val vibrador = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        manager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    if (!vibrador.hasVibrator()) return

    // ejecuta la vibracion
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val efecto = if (esError) {
            VibrationEffect.createWaveform(longArrayOf(0, 50, 100, 50), -1)
        } else {
            VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
        }
        vibrador.vibrate(efecto)
    } else {
        // Compatibilidad para teléefonos antiguos
        @Suppress("DEPRECATION")
        if (esError) {
            vibrador.vibrate(longArrayOf(0, 50, 100, 50), -1)
        } else {
            vibrador.vibrate(100)
        }
    }
}