package com.example.lemacdatalab.model

data class RegistroMaestro(
    val idRegistro: Int,
    val idUsuario: Int,
    val tipoArea: String, // "neurodesarrollo" | "dbt" | "animo" | "adicciones"
    val fechaHoraCreacion: String,
    val notasOpcionales: String? = null,
    val estadoSync: String = "pendiente",
    val estadoRegistro: String = "confirmado"
)