package com.example.lemacdatalab.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lemacdatalab.data.AppDatabase
import com.example.lemacdatalab.data.SesionUsuario
import com.example.lemacdatalab.data.remote.RetrofitClient
import com.example.lemacdatalab.data.repository.RegistroRepository
import com.example.lemacdatalab.data.repository.ResultadoSync
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

sealed interface EstadoSyncUi {
    data object Sincronizando : EstadoSyncUi
    data class Sincronizado(val enviados: Int, val recibidos: Int, val pendientes: Int) : EstadoSyncUi
    data class SinConexion(val pendientes: Int, val mensaje: String) : EstadoSyncUi
}

class HistorialViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RegistroRepository = RoomRegistroRepository(
        dao = AppDatabase.getDatabase(application).registroDetalleDao(),
        apiService = RetrofitClient.obtenerApiService(application)
    )

    private val idUsuario: Int
        get() = SesionUsuario.idUsuario ?: UsuarioRepository.USUARIO_PRUEBA.idUsuario

    private val _registrosDb = repository.observarHistorial(idUsuario)

    private val _filtroActual = MutableStateFlow(FiltroTipoRegistro.TODOS)
    val filtroActual: StateFlow<FiltroTipoRegistro> = _filtroActual.asStateFlow()

    val pendientesSync: StateFlow<Int> = repository.observarCantidadPendientes(idUsuario)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    private val _estadoSyncUi = MutableStateFlow<EstadoSyncUi>(
        EstadoSyncUi.Sincronizado(enviados = 0, recibidos = 0, pendientes = 0)
    )
    val estadoSyncUi: StateFlow<EstadoSyncUi> = _estadoSyncUi.asStateFlow()

    init {
        // Sincroniza automáticamente al iniciar sesión y abrir la vista principal
        sincronizar()
    }

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

    val conteos: StateFlow<Map<FiltroTipoRegistro, Int>> = _registrosDb.map { lista ->
        mapOf(
            FiltroTipoRegistro.TODOS to lista.size,
            FiltroTipoRegistro.MOOD to lista.count { it.tipoArea == "animo" },
            FiltroTipoRegistro.DBT to lista.count { it.tipoArea == "dbt" },
            FiltroTipoRegistro.NEURO to lista.count { it.tipoArea == "neurodesarrollo" },
            FiltroTipoRegistro.ADICCIONES to lista.count { it.tipoArea == "adicciones" }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    private val _diasCarta = MutableStateFlow(DIAS_CARTA_ANIMO)
    val diasCarta: StateFlow<Int> = _diasCarta.asStateFlow()

    val cartaAnimo: StateFlow<List<PuntoCarta>> = combine(
        _registrosDb,
        _diasCarta
    ) { lista, dias ->
        calcularCartaAnimo(lista, dias = dias)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

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

    fun sincronizar() {
        val usuario = idUsuario
        viewModelScope.launch {
            _estadoSyncUi.value = EstadoSyncUi.Sincronizando
            val desdeUtc = SesionUsuario.ultimaSyncUtc(getApplication(), usuario)
            when (val resultado = repository.sincronizar(usuario, desdeUtc)) {
                is ResultadoSync.Exito -> {
                    SesionUsuario.guardarUltimaSyncUtc(
                        getApplication(),
                        usuario,
                        resultado.resumen.nuevaMarcaUtc
                    )
                    _estadoSyncUi.value = EstadoSyncUi.Sincronizado(
                        enviados = resultado.resumen.enviados,
                        recibidos = resultado.resumen.recibidos,
                        pendientes = resultado.resumen.pendientesRestantes
                    )
                }
                is ResultadoSync.SinConexion -> {
                    _estadoSyncUi.value = EstadoSyncUi.SinConexion(
                        pendientes = resultado.pendientes,
                        mensaje = resultado.mensaje
                    )
                }
            }
        }
    }

    fun eliminar(idRegistro: Int) {
        viewModelScope.launch {
            repository.eliminar(idUsuario, idRegistro)
        }
    }
}
