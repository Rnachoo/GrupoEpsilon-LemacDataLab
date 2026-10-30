package com.example.lemacdatalab.viewmodel

import com.example.lemacdatalab.model.RegistroHistorial

data class MenuUiState(
    val racha: Int = 0,
    val ultimosEventos: List<RegistroHistorial> = emptyList(),
    val hayRegistroHoy: Boolean = false,
    val onboardingVisto: Boolean = false
)
