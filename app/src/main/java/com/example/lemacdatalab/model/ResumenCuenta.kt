package com.example.lemacdatalab.model

// Resultado de una consulta agregada: no contiene notas ni escalas de ningún usuario
data class ResumenCuenta(
    val alias: String,
    val totalEventos: Int,
    val ultimaActividad: String?
)