package com.example.lemacdatalab.native

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Números oficiales de ayuda y emergencia.
 */
object LineasEmergencia {
    const val PREVENCION_SUICIDIO = "*4141"
    const val SALUD_RESPONDE = "600 360 7777"
    const val SAMU = "131"
}

data class LineaAyuda(val titulo: String, val numero: String, val descripcion: String)

val LINEAS_AYUDA = listOf(
    LineaAyuda(
        titulo = "Prevención del Suicidio",
        numero = LineasEmergencia.PREVENCION_SUICIDIO,
        descripcion = "Línea *4141 «No estás solo». Gratuita, confidencial y disponible 24/7."
    ),
    LineaAyuda(
        titulo = "Salud Responde",
        numero = LineasEmergencia.SALUD_RESPONDE,
        descripcion = "Orientación médica y apoyo psicológico 24 horas del Ministerio de Salud."
    ),
    LineaAyuda(
        titulo = "SAMU Urgencias",
        numero = LineasEmergencia.SAMU,
        descripcion = "Atención médica de urgencia en situaciones de riesgo vital."
    )
)

object ContactoApoyoEjemplo {
    const val NOMBRE = "Camila (contacto de ejemplo)"
    const val TELEFONO = "+56 9 1234 5678"
}

/**
 * Segundo recurso nativo: Marcador telefónico mediante Intent.ACTION_DIAL.
 * Abre la app de teléfono del sistema con el número premarcado sin requerir permisos de llamada directa.
 *
 * El asterisco (*) se codifica como %2A (Uri.encode) para compatibilidad
 * con el marcador telefónico de Android (por ejemplo: "*4141" -> "tel:%2A4141").
 */
fun marcarNumeroEmergencia(context: Context, numero: String) {
    val limpio = numero.replace(" ", "").trim()
    val codificado = Uri.encode(limpio)
    val uri = Uri.parse("tel:$codificado")
    val intent = Intent(Intent.ACTION_DIAL, uri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}