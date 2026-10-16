package com.example.lemacdatalab.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lemacdatalab.data.AppDatabase
import com.example.lemacdatalab.data.RegistroRepository
import com.example.lemacdatalab.data.RoomRegistroRepository
import com.example.lemacdatalab.data.SesionUsuario
import com.example.lemacdatalab.data.UsuarioRepository
import com.example.lemacdatalab.model.ContextoMood
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MoodViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MoodUiState())
    val uiState: StateFlow<MoodUiState> = _uiState.asStateFlow()

    // Persistencia local (Room) a través del repositorio
    private val repository: RegistroRepository = RoomRegistroRepository(
        AppDatabase.getDatabase(application).registroDetalleDao()
    )

    // Evita guardar dos veces si el usuario toca el botón repetidamente
    private var guardando = false

    fun onNivelChange(valor: Int) {
        _uiState.update { it.copy(nivel = valor) }
    }

    fun onContextoChange(contexto: ContextoMood) {
        _uiState.update { it.copy(contexto = contexto, contextoError = null) }
    }

    fun onNotasChange(texto: String) {
        _uiState.update {
            it.copy(
                notas = texto,
                notasError = if (texto.length > MAX_NOTAS)
                    "Máximo $MAX_NOTAS caracteres" else null
            )
        }
    }

    fun guardar() {
        if (guardando) return
        val s = _uiState.value
        val contexto = s.contexto

        if (contexto == null || s.notas.length > MAX_NOTAS) {
            _uiState.update {
                it.copy(
                    contextoError = if (contexto == null)
                        "Selecciona un contexto detonante" else null,
                    notasError = if (s.notas.length > MAX_NOTAS)
                        "Máximo $MAX_NOTAS caracteres" else null,
                    intentosFallidos = it.intentosFallidos + 1
                )
            }
            return
        }

        guardando = true
        viewModelScope.launch {
            try {
                repository.guardarMood(
                    idUsuario = SesionUsuario.idUsuario ?: UsuarioRepository.USUARIO_PRUEBA.idUsuario,
                    nivel = s.nivel,
                    contexto = contexto.etiqueta,
                    notas = s.notas.ifBlank { null }
                )
                _uiState.update { it.copy(guardado = true) }
            } finally {
                guardando = false
            }
        }
    }

    fun onGuardadoConsumido() {
        _uiState.value = MoodUiState()
    }

    companion object {
        const val MAX_NOTAS = 300
    }
}