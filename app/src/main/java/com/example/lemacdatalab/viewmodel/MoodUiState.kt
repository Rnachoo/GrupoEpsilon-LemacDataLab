package com.example.lemacdatalab.viewmodel

import com.example.lemacdatalab.model.ContextoMood
import com.example.lemacdatalab.model.esHorasSuenoValido

data class MoodUiState(
    val depresion: Int = 0,
    val elevacion: Int = 0,
    val horasSueno: String = "", // vacío = no informado
    val horasSuenoError: String? = null,
    val altoRiesgo: Boolean = false,
    val sintomasPsicoticos: Boolean = false,
    val contexto: ContextoMood? = null,
    val contextoOtro: String = "",
    val notas: String = "",
    val contextoError: String? = null,
    val contextoOtroError: String? = null,
    val notasError: String? = null,
    val intentosFallidos: Int = 0,
    val guardado: Boolean = false,
    val alertaCritica: Boolean = false,
    val idRegistro: Int? = null
) {
    val esEdicion: Boolean get() = idRegistro != null

    val horasSuenoValidas: Boolean
        get() = horasSueno.isBlank() || esHorasSuenoValido(horasSueno.toIntOrNull())
}