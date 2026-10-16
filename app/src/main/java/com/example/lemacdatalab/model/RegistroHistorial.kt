package com.example.lemacdatalab.model

data class RegistroHistorial(
    val idRegistro: Int,
    val tipoArea: String, // "animo" | "dbt" | ...
    val fechaHoraCreacion: String,
    val notasOpcionales: String?,
    val nivelIntensidad: Int?,
    val contexto: String?,
    val habilidad: String?,
    val nivelImpulso: Int?
)