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
