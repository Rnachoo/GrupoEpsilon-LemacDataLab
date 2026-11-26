package com.example.lemacdatalab.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lemacdatalab.data.AppDatabase
import com.example.lemacdatalab.data.SesionUsuario
import com.example.lemacdatalab.data.repository.RegistroRepository
import com.example.lemacdatalab.data.repository.RoomRegistroRepository
import com.example.lemacdatalab.data.repository.UsuarioRepository
import com.example.lemacdatalab.model.DIAS_CARTA_ANIMO
import com.example.lemacdatalab.model.PuntoCarta
import com.example.lemacdatalab.model.RegistroHistorial
import com.example.lemacdatalab.model.SerieEvolucionAmbito
import com.example.lemacdatalab.model.calcularCartaAnimo
import com.example.lemacdatalab.model.calcularSeriesPorAmbito
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Enum para filtro por los 4 ámbitos
enum class FiltroTipoRegistro { TODOS, MOOD, DBT, NEURO, ADICCIONES }

class HistorialViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RegistroRepository = RoomRegistroRepository(
        AppDatabase.getDatabase(application).registroDetalleDao()
    )

    private val idUsuario: Int
        get() = SesionUsuario.idUsuario ?: UsuarioRepository.USUARIO_PRUEBA.idUsuario

    // Flujo original desde Room
    private val _registrosDb = repository.observarHistorial(idUsuario)

    // Estado reactivo para el filtro actual
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
            FiltroTipoRegistro.NEURO -> lista.filter { it.tipoArea == "neurodesarrollo" }
            FiltroTipoRegistro.ADICCIONES -> lista.filter { it.tipoArea == "adicciones" }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // Contadores dinámicos por categoría
    val conteos: StateFlow<Map<FiltroTipoRegistro, Int>> = _registrosDb.map { lista ->
        mapOf(
            FiltroTipoRegistro.TODOS to lista.size,
            FiltroTipoRegistro.MOOD to lista.count { it.tipoArea == "animo" },
            FiltroTipoRegistro.DBT to lista.count { it.tipoArea == "dbt" },
            FiltroTipoRegistro.NEURO to lista.count { it.tipoArea == "neurodesarrollo" },
            FiltroTipoRegistro.ADICCIONES to lista.count { it.tipoArea == "adicciones" }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    // Período seleccionado para los gráficos (7 o 30 días)
    private val _diasCarta = MutableStateFlow(DIAS_CARTA_ANIMO)
    val diasCarta: StateFlow<Int> = _diasCarta.asStateFlow()

    // Carta del ánimo (compatibilidad)
    val cartaAnimo: StateFlow<List<PuntoCarta>> = combine(
        _registrosDb,
        _diasCarta
    ) { lista, dias ->
        calcularCartaAnimo(lista, dias = dias)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Series por cada uno de los 4 ámbitos para 7 o 30 días
    val seriesPorAmbito: StateFlow<Map<String, SerieEvolucionAmbito>> = combine(
        _registrosDb,
        _diasCarta
    ) { lista, dias ->
        calcularSeriesPorAmbito(lista, dias = dias)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    fun setFiltro(filtro: FiltroTipoRegistro) {
        _filtroActual.value = filtro
    }

    fun setDiasCarta(dias: Int) {
        _diasCarta.value = dias
    }

    fun eliminar(idRegistro: Int) {
        viewModelScope.launch {
            repository.eliminar(idUsuario, idRegistro)
        }
    }
}
