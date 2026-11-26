package com.example.lemacdatalab.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lemacdatalab.data.AppDatabase
import com.example.lemacdatalab.data.SesionUsuario
import com.example.lemacdatalab.data.repository.RegistroRepository
import com.example.lemacdatalab.data.repository.RoomRegistroRepository
import com.example.lemacdatalab.data.repository.UsuarioRepository
import com.example.lemacdatalab.model.ContextoMood
import com.example.lemacdatalab.model.CriteriosCriticos
import com.example.lemacdatalab.model.DatosAnimo
import com.example.lemacdatalab.model.ESCALA_MAX
import com.example.lemacdatalab.model.ESCALA_MIN
import com.example.lemacdatalab.model.HORAS_SUENO_MAX
import com.example.lemacdatalab.model.MAX_OTRO
import com.example.lemacdatalab.model.calcularRacha
import com.example.lemacdatalab.model.hayValorCritico
import com.example.lemacdatalab.native.NotificacionHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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

    private val idUsuario: Int
        get() = SesionUsuario.idUsuario ?: UsuarioRepository.USUARIO_PRUEBA.idUsuario

    // Modo edición: carga el registro existente en el formulario
    fun cargarParaEditar(idRegistro: Int) {
        // Si ya está cargado (ej. rotación de pantalla) no pisa lo que el usuario fue cambiando
        if (_uiState.value.idRegistro == idRegistro) return
        viewModelScope.launch {
            val registro = repository.obtenerRegistro(idUsuario, idRegistro) ?: return@launch
            _uiState.update {
                val guardado = registro.contexto.orEmpty()
                val enCatalogo = ContextoMood.entries.firstOrNull { c ->
                    c != ContextoMood.OTRO && c.etiqueta == guardado
                }
                it.copy(
                    idRegistro = registro.idRegistro,
                    depresion = registro.depresion ?: 0,
                    elevacion = registro.elevacion ?: 0,
                    horasSueno = registro.horasSueno?.toString().orEmpty(),
                    altoRiesgo = registro.altoRiesgo ?: false,
                    sintomasPsicoticos = registro.sintomasPsicoticos ?: false,
                    contexto = enCatalogo ?: if (guardado.isNotBlank()) ContextoMood.OTRO else null,
                    contextoOtro = if (enCatalogo == null) guardado else "",
                    notas = registro.notasOpcionales.orEmpty()
                )
            }
        }
    }

    fun onDepresionChange(valor: Int) {
        _uiState.update { it.copy(depresion = valor.coerceIn(ESCALA_MIN, ESCALA_MAX)) }
    }

    fun onElevacionChange(valor: Int) {
        _uiState.update { it.copy(elevacion = valor.coerceIn(ESCALA_MIN, ESCALA_MAX)) }
    }

    fun onHorasSuenoChange(texto: String) {
        // Solo dígitos, máximo 2 (0 a 24)
        _uiState.update { it.copy(horasSueno = texto.filter(Char::isDigit).take(2), horasSuenoError = null) }
    }

    fun onAltoRiesgoChange(valor: Boolean) {
        _uiState.update { it.copy(altoRiesgo = valor) }
    }

    fun onSintomasPsicoticosChange(valor: Boolean) {
        _uiState.update { it.copy(sintomasPsicoticos = valor) }
    }

    fun onContextoChange(contexto: ContextoMood) {
        _uiState.update { it.copy(contexto = contexto, contextoError = null, contextoOtroError = null) }
    }

    fun onContextoOtroChange(texto: String) {
        _uiState.update {
            it.copy(
                contextoOtro = texto.take(MAX_OTRO),
                contextoOtroError = null
            )
        }
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

        val otroVacio = contexto == ContextoMood.OTRO && s.contextoOtro.isBlank()
        val horasError = if (!s.horasSuenoValidas) "Ingresa un número entre 0 y $HORAS_SUENO_MAX" else null

        if (contexto == null || otroVacio || s.notas.length > MAX_NOTAS || horasError != null) {
            _uiState.update {
                it.copy(
                    contextoError = if (contexto == null)
                        "Selecciona un contexto detonante" else null,
                    contextoOtroError = if (otroVacio)
                        "Escribe a qué te refieres con «Otro»" else null,
                    notasError = if (s.notas.length > MAX_NOTAS)
                        "Máximo $MAX_NOTAS caracteres" else null,
                    horasSuenoError = horasError,
                    intentosFallidos = it.intentosFallidos + 1
                )
            }
            return
        }

        val horas = s.horasSueno.toIntOrNull()
        val datos = DatosAnimo(
            depresion = s.depresion,
            elevacion = s.elevacion,
            horasSueno = horas,
            altoRiesgo = s.altoRiesgo,
            sintomasPsicoticos = s.sintomasPsicoticos,
            contexto = if (contexto == ContextoMood.OTRO) s.contextoOtro.trim() else contexto.etiqueta
        )
        val esCritico = hayValorCritico(
            CriteriosCriticos(
                altoRiesgo = s.altoRiesgo,
                sintomasPsicoticos = s.sintomasPsicoticos,
                horasSueno = horas
            )
        )

        guardando = true
        viewModelScope.launch {
            try {
                val idEdicion = s.idRegistro
                if (idEdicion != null) {
                    repository.actualizarMood(
                        idRegistro = idEdicion,
                        datos = datos,
                        notas = s.notas.ifBlank { null }
                    )
                } else {
                    repository.guardarMood(
                        idUsuario = idUsuario,
                        datos = datos,
                        notas = s.notas.ifBlank { null }
                    )
                    // aviso nativo con la racha ya actualizada (solo en registros nuevos)
                    val racha = calcularRacha(repository.observarHistorial(idUsuario).first())
                    NotificacionHelper.mostrarAlertaRegistroGuardado(getApplication(), "Estado de Ánimo", racha)
                }
                _uiState.update { it.copy(guardado = true, alertaCritica = esCritico) }
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