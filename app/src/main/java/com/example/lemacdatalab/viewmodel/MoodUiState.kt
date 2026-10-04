package com.example.lemacdatalab.viewmodel

import com.example.lemacdatalab.model.ContextoMood

data class MoodUiState(
    val nivel: Int = 5,
    val contexto: ContextoMood? = null,
    val notas: String = "",
    val contextoError: String? = null,
    val notasError: String? = null,
    val guardado: Boolean = false
)