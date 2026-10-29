package com.example.lemacdatalab.native

import android.content.Context
import android.content.Intent
import com.example.lemacdatalab.model.ContenidoEducativo

// abre la hoja nativa de Android para enviar la lectura a notas, mensajería o correo
fun compartirLecturaDiaria(context: Context, contenido: ContenidoEducativo) {
    val textoFormateado = buildString {
        appendLine("📖 Lectura del Día (${contenido.ambito}) - Lemac DataLab")
        appendLine()
        appendLine("«${contenido.titulo}»")
        appendLine(contenido.extracto)
        appendLine()
        append("Fuente: ${contenido.fuente}")
    }
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, contenido.titulo)
        putExtra(Intent.EXTRA_TEXT, textoFormateado)
    }
    val shareChooser = Intent.createChooser(sendIntent, "Guardar o compartir lectura diaria")
    context.startActivity(shareChooser)
}
