package com.example.lemacdatalab.model

data class MoodDetalle(
    val idRegistro: Int,
    val nivelIntensidad: Int, // 1..10
    val contexto: String
)