package com.example.lemacdatalab.model

const val ESCALA_MIN = 0
const val ESCALA_MAX = 4

val ETIQUETAS_ESCALA = listOf("Sin síntomas", "Leve", "Moderada", "Marcada", "Severa")

fun esEscalaValida(valor: Int): Boolean = valor in ESCALA_MIN..ESCALA_MAX

// null si el valor está fuera de la escala
fun etiquetaEscala(valor: Int): String? =
    if (esEscalaValida(valor)) ETIQUETAS_ESCALA[valor] else null