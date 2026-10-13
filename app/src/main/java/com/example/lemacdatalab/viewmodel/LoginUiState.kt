package com.example.lemacdatalab.viewmodel

data class LoginUiState(
    val idSintetico: String = "",
    val deslindeAceptado: Boolean = false,
    val idError: String? = null,
    val ingresado: Boolean = false
)
