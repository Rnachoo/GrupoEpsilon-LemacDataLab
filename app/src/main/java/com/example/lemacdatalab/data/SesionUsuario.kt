package com.example.lemacdatalab.data

import android.content.Context
import androidx.core.content.edit
import com.example.lemacdatalab.model.RolUsuario
import com.example.lemacdatalab.model.Usuario

// Usuario autenticado durante la ejecución de la app
object SesionUsuario {
    val rol: RolUsuario?
        get() = usuarioActual?.let { RolUsuario.detectarRol(it.identificadorSintetico) }

    var usuarioActual: Usuario? = null
        private set

    val idUsuario: Int?
        get() = usuarioActual?.idUsuario

    // Acceso directo al ID sintético del usuario logueado (ej: "AX-7B99" o "BZ-4M12")
    val identificadorSintetico: String
        get() = usuarioActual?.identificadorSintetico ?: "SIN-ID"

    fun iniciar(usuario: Usuario) {
        usuarioActual = usuario
    }

    fun cerrar() {
        usuarioActual = null
    }

    // la guía de bienvenida se recuerda en el dispositivo aunque se cierre la app
    fun onboardingVisto(context: Context): Boolean =
        preferencias(context).getBoolean(ONBOARDING_VISTO, false)

    fun marcarOnboardingVisto(context: Context) {
        preferencias(context).edit { putBoolean(ONBOARDING_VISTO, true) }
    }

    private fun preferencias(context: Context) =
        context.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)

    private const val PREFERENCIAS = "lemac_preferencias"
    private const val ONBOARDING_VISTO = "onboarding_visto"
}