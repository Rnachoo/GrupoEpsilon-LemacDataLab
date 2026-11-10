package com.example.lemacdatalab.data

import android.content.Context
import android.os.SystemClock
import androidx.core.content.edit
import com.example.lemacdatalab.model.RolUsuario
import com.example.lemacdatalab.model.Usuario
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    private var ultimaActividad = 0L

    fun iniciar(usuario: Usuario) {
        usuarioActual = usuario
        ultimaActividad = SystemClock.elapsedRealtime()
    }

    fun cerrar() {
        usuarioActual = null
    }

    // cada toque reinicia el reloj, salvo que la sesión ya haya expirado
    fun registrarActividad() {
        if (!sesionExpirada()) ultimaActividad = SystemClock.elapsedRealtime()
    }

    fun sesionExpirada(): Boolean =
        usuarioActual != null &&
                SystemClock.elapsedRealtime() - ultimaActividad > TIEMPO_INACTIVIDAD_MS

    // la guía de bienvenida se recuerda en el dispositivo aunque se cierre la app
    fun onboardingVisto(context: Context): Boolean =
        preferencias(context).getBoolean(ONBOARDING_VISTO, false)

    fun marcarOnboardingVisto(context: Context) {
        preferencias(context).edit { putBoolean(ONBOARDING_VISTO, true) }
    }

    // guarda la versión del deslinde aceptada y la fecha, por usuario
    fun registrarConsentimiento(context: Context) {
        val id = identificadorSintetico
        preferencias(context).edit {
            putString("consentimiento_version_$id", VERSION_DESLINDE)
            putLong("consentimiento_fecha_$id", System.currentTimeMillis())
        }
    }

    fun consentimiento(context: Context): String? {
        val id = identificadorSintetico
        val version = preferencias(context).getString("consentimiento_version_$id", null) ?: return null
        val fecha = preferencias(context).getLong("consentimiento_fecha_$id", 0L)
        val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return "Versión $version, aceptada el ${formato.format(Date(fecha))}"
    }

    private fun preferencias(context: Context) =
        context.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)

    private const val PREFERENCIAS = "lemac_preferencias"
    private const val ONBOARDING_VISTO = "onboarding_visto"
    private const val TIEMPO_INACTIVIDAD_MS = 5 * 60 * 1000L

    private const val VERSION_DESLINDE = "1.0"
}