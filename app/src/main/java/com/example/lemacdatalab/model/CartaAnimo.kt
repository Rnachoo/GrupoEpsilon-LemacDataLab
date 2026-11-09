package com.example.lemacdatalab.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

const val DIAS_CARTA_CORTO = 7
const val DIAS_CARTA_ANIMO = 30
val PERIODOS_CARTA_ANIMO = listOf(DIAS_CARTA_CORTO, DIAS_CARTA_ANIMO)

data class PuntoCarta(
    val dia: Int,
    val fecha: String, // yyyy-MM-dd
    val promedio: Float
)

fun calcularCartaAnimo(
    registros: List<RegistroHistorial>,
    dias: Int = DIAS_CARTA_ANIMO,
    hoy: Calendar = Calendar.getInstance()
): List<PuntoCarta> {
    val formato = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val inicio = hoy.clone() as Calendar
    inicio.add(Calendar.DAY_OF_YEAR, -(dias - 1))

    val indicePorFecha = (0 until dias).associate { i ->
        val dia = inicio.clone() as Calendar
        dia.add(Calendar.DAY_OF_YEAR, i)
        formato.format(dia.time) to i
    }

    return registros
        .filter { it.tipoArea == "animo" && it.nivelIntensidad != null }
        .groupBy { it.fechaHoraCreacion.take(10) }
        .mapNotNull { (fecha, delDia) ->
            val indice = indicePorFecha[fecha] ?: return@mapNotNull null
            PuntoCarta(
                dia = indice,
                fecha = fecha,
                promedio = delDia.mapNotNull { it.nivelIntensidad }.average().toFloat()
            )
        }
        .sortedBy { it.dia }
}
