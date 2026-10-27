package com.example.lemacdatalab.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lemacdatalab.data.AppDatabase
import com.example.lemacdatalab.data.RegistroRepository
import com.example.lemacdatalab.data.RoomRegistroRepository
import com.example.lemacdatalab.data.SesionUsuario
import com.example.lemacdatalab.data.UsuarioRepository
import com.example.lemacdatalab.model.RegistroHistorial
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Enum para la barra superior
enum class FiltroTipoRegistro { TODOS, MOOD, DBT }

class HistorialViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RegistroRepository = RoomRegistroRepository(
        AppDatabase.getDatabase(application).registroDetalleDao()
    )

    private val idUsuario: Int
        get() = SesionUsuario.idUsuario ?: UsuarioRepository.USUARIO_PRUEBA.idUsuario

    // Flujo original desde Room
    private val _registrosDb = repository.observarHistorial(idUsuario)

    // 2. Estado reactivo para el filtro actual
    private val _filtroActual = MutableStateFlow(FiltroTipoRegistro.TODOS)
    val filtroActual: StateFlow<FiltroTipoRegistro> = _filtroActual.asStateFlow()

    // Lista filtrada combinada, reacciona tanto a cambios en la BD como a cambios de filtro
    val registros: StateFlow<List<RegistroHistorial>?> = combine(
        _registrosDb,
        _filtroActual
    ) { lista, filtro ->
        when (filtro) {
            FiltroTipoRegistro.TODOS -> lista
            FiltroTipoRegistro.MOOD -> lista.filter { it.tipoArea == "animo" }
            FiltroTipoRegistro.DBT -> lista.filter { it.tipoArea == "dbt" }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // Contadores dinámicos por categoría
    val conteos: StateFlow<Map<FiltroTipoRegistro, Int>> = _registrosDb.map { lista ->
        mapOf(
            FiltroTipoRegistro.TODOS to lista.size,
            FiltroTipoRegistro.MOOD to lista.count { it.tipoArea == "animo" },
            FiltroTipoRegistro.DBT to lista.count { it.tipoArea == "dbt" }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    // Función para cambiar el filtro
    fun setFiltro(filtro: FiltroTipoRegistro) {
        _filtroActual.value = filtro
    }

    fun eliminar(idRegistro: Int) {
        viewModelScope.launch {
            repository.eliminar(idUsuario, idRegistro)
        }
    }
}