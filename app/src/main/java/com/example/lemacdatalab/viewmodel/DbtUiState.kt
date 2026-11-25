package com.example.lemacdatalab.viewmodel

import com.example.lemacdatalab.model.HabilidadDbt

data class DbtUiState(
    val intensidadEmocional: Int = 2,
    val habilidad: HabilidadDbt? = null,
    val habilidadOtro: String = "",
    val notas: String = "",
    val habilidadError: String? = null,
    val habilidadOtroError: String? = null,
    val notasError: String? = null,
    val guardado: Boolean = false,
    val alertaCritica: Boolean = false,
    val idRegistro: Int? = null
) {
    val esEdicion: Boolean get() = idRegistro != null
}