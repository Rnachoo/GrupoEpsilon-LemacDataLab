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
// Pulso muy corto y suave para el slider (un "tic" por cada valor)
fun vibrarTic(context: Context) {
    val vibrador = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        manager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    if (!vibrador.hasVibrator()) return

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // 20 ms con amplitud media-baja: se siente como un clic, no como un zumbido
        vibrador.vibrate(VibrationEffect.createOneShot(20, 80))
    } else {
        @Suppress("DEPRECATION")
        vibrador.vibrate(20)
    }
}