package com.example.lemacdatalab.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

const val DIAS_CARTA_CORTO = 7
const val DIAS_CARTA_ANIMO = 30
val PERIODOS_CARTA_ANIMO = listOf(DIAS_CARTA_CORTO, DIAS_CARTA_ANIMO)
const val TEXTO_SIN_REGISTRO = "Sin registro"
val AMBITOS_EVOLUCION = listOf("animo", "dbt", "neurodesarrollo", "adicciones")

data class PuntoCarta(
    val dia: Int,
    val fecha: String, // yyyy-MM-dd
    val promedio: Float,
    // En Ánimo: elevación hacia arriba (0f..+4f) y depresión hacia abajo (-4f..0f)
    val elevacion: Float = promedio,
    val depresionConSigno: Float = 0f,
    val depresionMagnitud: Int = 0,
    val elevacionMagnitud: Int = promedio.toInt()
)

data class DiaEvolucion(
    val dia: Int,
    val fecha: String, // yyyy-MM-dd
    val punto: PuntoCarta? // null = hueco («Sin registro»), nunca 0 ni «Sin síntomas»
) {
    val tieneRegistro: Boolean get() = punto != null
    val etiquetaEstado: String get() = etiquetaDiaEvolucion(punto)
}

data class SerieEvolucionAmbito(
    val tipoArea: String,
    val dias: Int,
    val puntos: List<PuntoCarta>,
    val calendario: List<DiaEvolucion>
) {
    val diasConRegistro: Int get() = puntos.size
    val diasSinRegistro: Int get() = dias - puntos.size
}

fun etiquetaDiaEvolucion(punto: PuntoCarta?): String {
    if (punto == null) return TEXTO_SIN_REGISTRO
    return etiquetaEscala(punto.promedio.toInt()) ?: "${punto.promedio.toInt()}"
}

fun calcularSerieAmbito(
    registros: List<RegistroHistorial>,
    tipoArea: String,
    dias: Int = DIAS_CARTA_ANIMO,
    hoy: Calendar = Calendar.getInstance()
): SerieEvolucionAmbito {
    val formato = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val inicio = hoy.clone() as Calendar
    inicio.add(Calendar.DAY_OF_YEAR, -(dias - 1))

    val fechas = (0 until dias).map { i ->
        val dia = inicio.clone() as Calendar
        dia.add(Calendar.DAY_OF_YEAR, i)
        i to formato.format(dia.time)
    }

    val porFecha = registros
        .filter { it.tipoArea == tipoArea && it.estadoRegistro != "eliminado" }
        .groupBy { it.fechaHoraCreacion.take(10) }

    val calendario = fechas.map { (indice, fecha) ->
        val delDia = porFecha[fecha].orEmpty()
        val punto = calcularPuntoDelDia(indice, fecha, tipoArea, delDia)
        DiaEvolucion(dia = indice, fecha = fecha, punto = punto)
    }

    return SerieEvolucionAmbito(
        tipoArea = tipoArea,
        dias = dias,
        puntos = calendario.mapNotNull { it.punto },
        calendario = calendario
    )
}

private fun calcularPuntoDelDia(
    dia: Int,
    fecha: String,
    tipoArea: String,
    delDia: List<RegistroHistorial>
): PuntoCarta? {
    if (delDia.isEmpty()) return null

    if (tipoArea == "animo") {
        val pares = delDia.mapNotNull { r ->
            if (r.depresion == null && r.elevacion == null && r.nivelIntensidad == null) {
                null
            } else {
                val dep = r.depresion ?: 0
                val ele = r.elevacion ?: r.nivelIntensidad ?: 0
                dep to ele
            }
        }
        if (pares.isEmpty()) return null
        val promDep = pares.map { it.first }.average().toFloat()
        val promEle = pares.map { it.second }.average().toFloat()
        val resumen = maxOf(promDep, promEle)
        return PuntoCarta(
            dia = dia,
            fecha = fecha,
            promedio = resumen,
            elevacion = promEle,
            depresionConSigno = -promDep,
            depresionMagnitud = promDep.toInt(),
            elevacionMagnitud = promEle.toInt()
        )
    }

    val valores = when (tipoArea) {
        "dbt" -> delDia.mapNotNull { it.nivelImpulso }
        "neurodesarrollo" -> delDia.mapNotNull { it.intensidadNeuro }
        "adicciones" -> delDia.mapNotNull { it.craving }
        else -> emptyList()
    }
    if (valores.isEmpty()) return null

    val promedio = valores.average().toFloat()
    return PuntoCarta(
        dia = dia,
        fecha = fecha,
        promedio = promedio
    )
}

fun calcularSeriesPorAmbito(
    registros: List<RegistroHistorial>,
    dias: Int = DIAS_CARTA_ANIMO,
    hoy: Calendar = Calendar.getInstance()
): Map<String, SerieEvolucionAmbito> =
    AMBITOS_EVOLUCION.associateWith { area ->
        calcularSerieAmbito(registros = registros, tipoArea = area, dias = dias, hoy = hoy)
    }

fun calcularCartaAnimo(
    registros: List<RegistroHistorial>,
    dias: Int = DIAS_CARTA_ANIMO,
    hoy: Calendar = Calendar.getInstance()
): List<PuntoCarta> =
    calcularSerieAmbito(registros = registros, tipoArea = "animo", dias = dias, hoy = hoy).puntos
