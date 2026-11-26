package com.example.lemacdatalab.viewmodel

import com.example.lemacdatalab.model.EstrategiaNeuro
import com.example.lemacdatalab.model.ResultadoNeuro
import com.example.lemacdatalab.model.TipoEntradaNeuro
import com.example.lemacdatalab.model.esOtroValido
import com.example.lemacdatalab.model.sonNotasValidas

data class NeuroUiState(
    val tipoEntrada: TipoEntradaNeuro? = null,
    val intensidad: Int = 0, // escala 0 a 4
    val estrategia: EstrategiaNeuro? = null,
    val estrategiaOtro: String = "",
    val resultado: ResultadoNeuro? = null,
    val notas: String = "",
    val tipoEntradaError: String? = null,
    val estrategiaError: String? = null,
    val estrategiaOtroError: String? = null,
    val resultadoError: String? = null,
    val notasError: String? = null,
    val guardado: Boolean = false,
    val idRegistro: Int? = null
) {
    val esEdicion: Boolean get() = idRegistro != null

    val esValido: Boolean
        get() = tipoEntrada != null &&
                estrategia != null &&
                resultado != null &&
                esOtroValido(estrategia == EstrategiaNeuro.OTRO, estrategiaOtro) &&
                sonNotasValidas(notas, NeuroViewModel.MAX_NOTAS)
}