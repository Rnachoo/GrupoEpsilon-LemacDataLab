package com.example.lemacdatalab.model

const val ESCALA_MIN = 0
const val ESCALA_MAX = 4

val ETIQUETAS_ESCALA = listOf("Sin síntomas", "Leve", "Moderada", "Marcada", "Severa")

fun esEscalaValida(valor: Int): Boolean = valor in ESCALA_MIN..ESCALA_MAX

// null si el valor está fuera de la escala
fun etiquetaEscala(valor: Int): String? =
    if (esEscalaValida(valor)) ETIQUETAS_ESCALA[valor] else null


const val HORAS_SUENO_MAX = 24

// Horas de sueño: opcional (null) o entre 0 y 24
fun esHorasSuenoValido(horas: Int?): Boolean = horas == null || horas in 0..HORAS_SUENO_MAX

// Resumen 0 a 4 para las vistas que aún leen un solo valor (historial, CSV)
fun nivelResumenAnimo(depresion: Int, elevacion: Int): Int = maxOf(depresion, elevacion)

// Lo que guarda un registro de Ánimo (lo reutilizará «Registro de hoy»)
data class DatosAnimo(
    val depresion: Int,
    val elevacion: Int,
    val horasSueno: Int?,
    val altoRiesgo: Boolean,
    val sintomasPsicoticos: Boolean,
    val contexto: String
)