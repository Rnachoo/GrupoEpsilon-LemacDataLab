package com.example.lemacdatalab.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lemacdatalab.data.AppDatabase
import com.example.lemacdatalab.data.repository.RegistroRepository
import com.example.lemacdatalab.data.repository.RoomRegistroRepository
import com.example.lemacdatalab.data.SesionUsuario
import com.example.lemacdatalab.data.repository.UsuarioRepository
import com.example.lemacdatalab.model.HabilidadDbt
import com.example.lemacdatalab.model.MAX_OTRO
import com.example.lemacdatalab.model.calcularRacha
import com.example.lemacdatalab.model.esIntensidadEmocionalCritica
import com.example.lemacdatalab.native.NotificacionHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DbtViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(DbtUiState())
    val uiState: StateFlow<DbtUiState> = _uiState.asStateFlow()

    private val repository: RegistroRepository = RoomRegistroRepository(
        AppDatabase.getDatabase(application).registroDetalleDao()
    )

    private var guardando = false

    private val idUsuario: Int
        get() = SesionUsuario.idUsuario ?: UsuarioRepository.USUARIO_PRUEBA.idUsuario

    fun cargarParaEditar(idRegistro: Int) {
        // Si ya esta cargado (ej. rotacion de pantalla) no pisa lo que el usuario fue cambiando
        if (_uiState.value.idRegistro == idRegistro) return
        viewModelScope.launch {
            val registro = repository.obtenerRegistro(idUsuario, idRegistro) ?: return@launch
            _uiState.update {
                val guardada = registro.habilidad.orEmpty()
                val enCatalogo = HabilidadDbt.entries.firstOrNull { h ->
                    h != HabilidadDbt.OTRO && h.etiqueta == guardada
                }
                it.copy(
                    idRegistro = registro.idRegistro,
                    // En el historial, nivelImpulso trae la intensidad emocional (0 a 4)
                    intensidadEmocional = registro.nivelImpulso ?: it.intensidadEmocional,
                    habilidad = enCatalogo ?: if (guardada.isNotBlank()) HabilidadDbt.OTRO else null,
                    habilidadOtro = if (enCatalogo == null) guardada else "",
                    notas = registro.notasOpcionales.orEmpty()
                )
            }
        }
    }

    fun onIntensidadChange(valor: Int) {
        _uiState.update { it.copy(intensidadEmocional = valor) }
    }

    fun onHabilidadChange(habilidad: HabilidadDbt) {
        _uiState.update { it.copy(habilidad = habilidad, habilidadError = null, habilidadOtroError = null) }
    }

    fun onHabilidadOtroChange(texto: String) {
        _uiState.update {
            it.copy(
                habilidadOtro = texto.take(MAX_OTRO),
                habilidadOtroError = null
            )
        }
    }

    fun onNotasChange(texto: String) {
        val error = if (texto.length > MAX_NOTAS) "Máximo $MAX_NOTAS caracteres" else null
        _uiState.update { it.copy(notas = texto, notasError = error) }
    }

    fun guardar() {
        if (guardando) return
        val state = _uiState.value
        val habilidadSeleccionada = state.habilidad

        val errorHabilidad = if (habilidadSeleccionada == null) {
            "Seleccionar una habilidad es obligatorio"
        } else null

        val errorOtro = if (habilidadSeleccionada == HabilidadDbt.OTRO && state.habilidadOtro.isBlank()) {
            "Escribe cuál habilidad pusiste en práctica"
        } else null

        val errorNotas = if (state.notas.length > MAX_NOTAS) {
            "Las notas exceden el límite de $MAX_NOTAS"
        } else null

        if (errorHabilidad != null || errorOtro != null || errorNotas != null) {
            _uiState.update {
                it.copy(
                    habilidadError = errorHabilidad,
                    habilidadOtroError = errorOtro,
                    notasError = errorNotas
                )
            }
            return
        }

        val habilidad = habilidadSeleccionada ?: return
        val habilidadGuardada = if (habilidad == HabilidadDbt.OTRO) state.habilidadOtro.trim() else habilidad.etiqueta
        guardando = true
        viewModelScope.launch {
            try {
                val idEdicion = state.idRegistro
                if (idEdicion != null) {
                    repository.actualizarDbt(
                        idRegistro = idEdicion,
                        habilidad = habilidadGuardada,
                        intensidadEmocional = state.intensidadEmocional,
                        notas = state.notas.ifBlank { null }
                    )
                } else {
                    repository.guardarDbt(
                        idUsuario = idUsuario,
                        habilidad = habilidadGuardada,
                        intensidadEmocional = state.intensidadEmocional,
                        notas = state.notas.ifBlank { null }
                    )
                    val racha = calcularRacha(repository.observarHistorial(idUsuario).first())
                    NotificacionHelper.mostrarAlertaRegistroGuardado(getApplication(), "Diario DBT", racha)
                }
                _uiState.update {
                    it.copy(
                        guardado = true,
                        alertaCritica = esIntensidadEmocionalCritica(state.intensidadEmocional)
                    )
                }
            } finally {
                guardando = false
            }
        }
    }

    fun onGuardadoConsumido() {
        _uiState.value = DbtUiState()
    }

    companion object {
        const val MAX_NOTAS = 200
    }
}