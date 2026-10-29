package com.example.lemacdatalab.viewmodel

import com.example.lemacdatalab.model.ResumenCuenta

data class AdminUiState(
    val totalRegistros: Int = 0,
    val totalAnimo: Int = 0,
    val totalDbt: Int = 0,
    val cuentas: List<ResumenCuenta> = emptyList()
) {
    val cuentasActivas: Int get() = cuentas.size
    val porcentajeAnimo: Int get() = if (totalRegistros == 0) 0 else totalAnimo * 100 / totalRegistros
    val porcentajeDbt: Int get() = if (totalRegistros == 0) 0 else totalDbt * 100 / totalRegistros
}