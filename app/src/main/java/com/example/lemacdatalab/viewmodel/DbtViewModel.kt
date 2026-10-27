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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DbtViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(DbtUiState())
    val uiState: StateFlow<DbtUiState> = _uiState.asStateFlow()

    // Persistencia local (Room) a traves del repositorio
    private val repository: RegistroRepository = RoomRegistroRepository(
        AppDatabase.getDatabase(application).registroDetalleDao()
    )

    // Evita guardar dos veces si el usuario toca el boton repetidamente
    private var guardando = false

    private val idUsuario: Int
        get() = SesionUsuario.idUsuario ?: UsuarioRepository.USUARIO_PRUEBA.idUsuario

    // Modo edicion: carga el registro existente en el formulario
    fun cargarParaEditar(idRegistro: Int) {
        // Si ya esta cargado (ej. rotacion de pantalla) no pisa lo que el usuario fue cambiando
        if (_uiState.value.idRegistro == idRegistro) return
        viewModelScope.launch {
            val registro = repository.obtenerRegistro(idUsuario, idRegistro) ?: return@launch
            _uiState.update {
                it.copy(
                    idRegistro = registro.idRegistro,
                    nivelImpulso = registro.nivelImpulso ?: it.nivelImpulso,
                    habilidad = HabilidadDbt.entries.firstOrNull { h -> h.etiqueta == registro.habilidad },
                    notas = registro.notasOpcionales.orEmpty()
                )
            }
        }
    }

    fun onNivelChange(valor: Int) {
        _uiState.update { it.copy(nivelImpulso = valor) }
    }

    fun onHabilidadChange(habilidad: HabilidadDbt) {
        _uiState.update { it.copy(habilidad = habilidad, habilidadError = null) }
    }

    fun onNotasChange(texto: String) {
        val error = if (texto.length > MAX_NOTAS) "Máximo $MAX_NOTAS caracteres" else null
        _uiState.update { it.copy(notas = texto, notasError = error) }
    }

    fun guardar() {
        if (guardando) return
        val state = _uiState.value
        val habilidadSeleccionada = state.habilidad

        // Validacion de campo obligatorio
        var errorHabilidad: String? = null
        if (habilidadSeleccionada == null) {
            errorHabilidad = "Seleccionar una habilidad es obligatorio"
        }

        // Validacion de regla de negocio
        var errorNotas: String? = null
        if (state.notas.length > MAX_NOTAS) {
            errorNotas = "Las notas exceden el límite de $MAX_NOTAS"
        } else if (state.nivelImpulso > 7 && state.notas.isBlank()) {
            errorNotas = "Si el impulso es mayor a 7, debes justificar en las notas"
        }

        // Si hay errores, se actualiza el estado para mostrarlo
        if (errorHabilidad != null || errorNotas != null) {
            _uiState.update {
                it.copy(habilidadError = errorHabilidad, notasError = errorNotas)
            }
            return
        }

        // Si pasa las validaciones, guardamos en Room de forma asincrona
        val habilidad = habilidadSeleccionada ?: return
        guardando = true
        viewModelScope.launch {
            try {
                val idEdicion = state.idRegistro
                if (idEdicion != null) {
                    repository.actualizarDbt(
                        idRegistro = idEdicion,
                        habilidad = habilidad.etiqueta,
                        nivelImpulso = state.nivelImpulso,
                        notas = state.notas.ifBlank { null }
                    )
                } else {
                    repository.guardarDbt(
                        idUsuario = idUsuario,
                        habilidad = habilidad.etiqueta,
                        nivelImpulso = state.nivelImpulso,
                        notas = state.notas.ifBlank { null }
                    )
                }
                _uiState.update { it.copy(guardado = true) }
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