package com.example.lemacdatalab.model

const val UMBRAL_VALOR_CRITICO = 9

fun esValorCritico(nivel: Int): Boolean = nivel >= UMBRAL_VALOR_CRITICO

const val MAX_OTRO = 40