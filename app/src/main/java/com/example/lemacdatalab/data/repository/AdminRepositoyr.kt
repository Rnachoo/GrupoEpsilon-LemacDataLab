package com.example.lemacdatalab.data.repository

import com.example.lemacdatalab.data.dao.AdminDao
import com.example.lemacdatalab.model.ResumenCuenta
import kotlinx.coroutines.flow.Flow

class AdminRepository(private val dao: AdminDao) {

    val totalRegistros: Flow<Int> = dao.contarRegistros()

    fun totalPorTipo(tipoArea: String): Flow<Int> = dao.contarPorTipo(tipoArea)

    val cuentas: Flow<List<ResumenCuenta>> = dao.resumenPorCuenta()
}