package com.example.lemacdatalab.model

data class RegistroHistorial(
    val idRegistro: Int,
    val tipoArea: String,
    val fechaHoraCreacion: String,
    val notasOpcionales: String?,
    val nivelIntensidad: Int?,
    val contexto: String?,
    val habilidad: String?,
    val nivelImpulso: Int?,
    val estadoRegistro: String = "confirmado",
    val depresion: Int? = null,
    val elevacion: Int? = null,
    val horasSueno: Int? = null,
    val altoRiesgo: Boolean? = null,
    val sintomasPsicoticos: Boolean? = null,
    val tipoEntrada: String? = null,
    val intensidadNeuro: Int? = null,
    val estrategia: String? = null,
    val resultadoPercibido: String? = null,
    val craving: Int? = null,
    val desencadenante: String? = null,
    val accionProtectora: String? = null,
    val estadoObjetivo: String? = null
)