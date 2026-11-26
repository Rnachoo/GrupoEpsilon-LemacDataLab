package com.example.lemacdatalab.model

const val ESCALA_MIN = 0
const val ESCALA_MAX = 4

val ETIQUETAS_ESCALA = listOf("Sin síntomas", "Leve", "Moderada", "Marcada", "Severa")

fun esEscalaValida(valor: Int): Boolean = valor in ESCALA_MIN..ESCALA_MAX

fun etiquetaEscala(valor: Int): String? =
    if (esEscalaValida(valor)) ETIQUETAS_ESCALA[valor] else null


const val HORAS_SUENO_MAX = 24

fun esHorasSuenoValido(horas: Int?): Boolean =
    horas == null || (horas >= 0 && horas < HORAS_SUENO_MAX)

fun nivelResumenAnimo(depresion: Int, elevacion: Int): Int = maxOf(depresion, elevacion)

data class DatosAnimo(
    val depresion: Int,
    val elevacion: Int,
    val horasSueno: Int?,
    val altoRiesgo: Boolean,
    val sintomasPsicoticos: Boolean,
    val contexto: String
)