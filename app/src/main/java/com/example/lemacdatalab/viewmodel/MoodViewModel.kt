package com.example.lemacdatalab.viewmodel

import androidx.lifecycle.ViewModel
import com.example.lemacdatalab.model.ContextoMood
import com.example.lemacdatalab.model.MoodDetalle
import com.example.lemacdatalab.model.RegistroMaestro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MoodViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MoodUiState())
    val uiState: StateFlow<MoodUiState> = _uiState.asStateFlow()

    private val registros = mutableListOf<Pair<RegistroMaestro, MoodDetalle>>()
    private var nextId = 1

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

    fun guardar(idUsuario: Int = 1) {
        val s = _uiState.value
        val contexto = s.contexto

        if (contexto == null || s.notas.length > MAX_NOTAS) {
            _uiState.update {
                it.copy(
                    contextoError = if (contexto == null)
                        "Selecciona un contexto detonante" else null,
                    notasError = if (s.notas.length > MAX_NOTAS)
                        "Máximo $MAX_NOTAS caracteres" else null
                )
            }
            return
        }

        val id = nextId++
        val fechaHora = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(Date())

        registros.add(
            RegistroMaestro(
                idRegistro = id,
                idUsuario = idUsuario,
                tipoArea = "animo",
                fechaHoraCreacion = fechaHora,
                notasOpcionales = s.notas.ifBlank { null }
            ) to MoodDetalle(
                idRegistro = id,
                nivelIntensidad = s.nivel,
                contexto = contexto.etiqueta
            )
        )
        _uiState.update { it.copy(guardado = true) }
    }

    fun onGuardadoConsumido() {
        _uiState.value = MoodUiState()
    }

    companion object {
        const val MAX_NOTAS = 300
    }
}