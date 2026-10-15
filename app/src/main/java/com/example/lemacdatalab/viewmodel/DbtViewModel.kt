package com.example.lemacdatalab.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lemacdatalab.data.AppDatabase
import com.example.lemacdatalab.data.SesionUsuario
import com.example.lemacdatalab.model.HabilidadDbt
import com.example.lemacdatalab.model.RegistroMaestro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DbtViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(DbtUiState())
    val uiState: StateFlow<DbtUiState> = _uiState.asStateFlow()

    // Conexion a la persistencia local (Room)
    private val registroDao = AppDatabase.getDatabase(application).registroMaestroDao()

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

        // Si pasa las validaciones, guardamos en el Room de forma asincrona
        viewModelScope.launch {
            val idUsuarioActual = SesionUsuario.idUsuario ?: 1
            val fechaHora = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

            val registro = RegistroMaestro(
                idRegistro = 0, // 0 para que el Room autogenere el ID
                idUsuario = idUsuarioActual,
                tipoArea = "dbt",
                fechaHoraCreacion = fechaHora,
                notasOpcionales = "Habilidad: ${habilidadSeleccionada!!.etiqueta} | Nivel Impulso: ${state.nivelImpulso} | Notas: ${state.notas}"
            )

            registroDao.insertar(registro)

            _uiState.update { it.copy(guardado = true) }
        }
    }

    fun onGuardadoConsumido() {
        _uiState.value = DbtUiState()
    }

    companion object {
        const val MAX_NOTAS = 200
    }
}