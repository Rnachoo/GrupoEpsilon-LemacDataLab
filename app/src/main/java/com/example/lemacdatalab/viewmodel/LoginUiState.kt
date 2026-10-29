package com.example.lemacdatalab.viewmodel

import com.example.lemacdatalab.model.RolUsuario

data class LoginUiState(
    val idSintetico: String = "",
    val deslindeAceptado: Boolean = false,
    val idError: String? = null,
    val ingresado: Boolean = false,
    val rol: RolUsuario = RolUsuario.USUARIO
)