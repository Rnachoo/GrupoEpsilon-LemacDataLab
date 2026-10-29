package com.example.lemacdatalab.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lemacdatalab.data.AppDatabase
import com.example.lemacdatalab.data.repository.AdminRepository
import com.example.lemacdatalab.model.RolUsuario
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class AdminViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AdminRepository(
        AppDatabase.getDatabase(application).adminDao()
    )

    // Se recalcula solo cada vez que cambia la base de datos
    val uiState: StateFlow<AdminUiState> = combine(
        repository.totalRegistros,
        repository.totalPorTipo("animo"),
        repository.totalPorTipo("dbt"),
        repository.cuentas
    ) { total, animo, dbt, cuentas ->
        AdminUiState(
            totalRegistros = total,
            totalAnimo = animo,
            totalDbt = dbt,
            // Solo cuentas de personas usuarias; admin y editor no se listan
            cuentas = cuentas.filter { RolUsuario.detectarRol(it.alias) == RolUsuario.USUARIO }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AdminUiState()
    )
}