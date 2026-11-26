package com.example.lemacdatalab.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lemacdatalab.data.AppDatabase
import com.example.lemacdatalab.data.SesionUsuario
import com.example.lemacdatalab.data.repository.RegistroRepository
import com.example.lemacdatalab.data.repository.RoomRegistroRepository
import com.example.lemacdatalab.data.repository.UsuarioRepository
import com.example.lemacdatalab.model.DatosNeuro
import com.example.lemacdatalab.model.ESCALA_MAX
import com.example.lemacdatalab.model.ESCALA_MIN
import com.example.lemacdatalab.model.EstrategiaNeuro
import com.example.lemacdatalab.model.MAX_OTRO
import com.example.lemacdatalab.model.ResultadoNeuro
import com.example.lemacdatalab.model.TipoEntradaNeuro
import com.example.lemacdatalab.model.calcularRacha
import com.example.lemacdatalab.model.esOtroValido
import com.example.lemacdatalab.model.sonNotasValidas
import com.example.lemacdatalab.native.NotificacionHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NeuroViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(NeuroUiState())
    val uiState: StateFlow<NeuroUiState> = _uiState.asStateFlow()

    private val repository: RegistroRepository = RoomRegistroRepository(
        AppDatabase.getDatabase(application).registroDetalleDao()
    )

    private var guardando = false

    private val idUsuario: Int
        get() = SesionUsuario.idUsuario ?: UsuarioRepository.USUARIO_PRUEBA.idUsuario

    fun cargarParaEditar(idRegistro: Int) {
        if (_uiState.value.idRegistro == idRegistro) return
        viewModelScope.launch {
            val registro = repository.obtenerRegistro(idUsuario, idRegistro) ?: return@launch
            _uiState.update {
                val guardada = registro.estrategia.orEmpty()
                val enCatalogo = EstrategiaNeuro.entries.firstOrNull { e ->
                    e != EstrategiaNeuro.OTRO && e.etiqueta == guardada
                }
                it.copy(
                    idRegistro = registro.idRegistro,
                    tipoEntrada = TipoEntradaNeuro.entries.firstOrNull { t -> t.etiqueta == registro.tipoEntrada },
                    intensidad = registro.intensidadNeuro ?: it.intensidad,
                    estrategia = enCatalogo ?: if (guardada.isNotBlank()) EstrategiaNeuro.OTRO else null,
                    estrategiaOtro = if (enCatalogo == null) guardada else "",
                    resultado = ResultadoNeuro.entries.firstOrNull { r -> r.etiqueta == registro.resultadoPercibido },
                    notas = registro.notasOpcionales.orEmpty()
                )
            }
        }
    }

    fun onTipoEntradaChange(tipo: TipoEntradaNeuro) {
        _uiState.update { it.copy(tipoEntrada = tipo, tipoEntradaError = null) }
    }

    fun onIntensidadChange(valor: Int) {
        _uiState.update { it.copy(intensidad = valor.coerceIn(ESCALA_MIN, ESCALA_MAX)) }
    }

    fun onEstrategiaChange(estrategia: EstrategiaNeuro) {
        _uiState.update { it.copy(estrategia = estrategia, estrategiaError = null, estrategiaOtroError = null) }
    }

    fun onEstrategiaOtroChange(texto: String) {
        _uiState.update {
            it.copy(
                estrategiaOtro = texto.take(MAX_OTRO),
                estrategiaOtroError = null
            )
        }
    }

    fun onResultadoChange(resultado: ResultadoNeuro) {
        _uiState.update { it.copy(resultado = resultado, resultadoError = null) }
    }

    fun onNotasChange(texto: String) {
        _uiState.update {
            it.copy(
                notas = texto,
                notasError = if (!sonNotasValidas(texto, MAX_NOTAS)) "Máximo $MAX_NOTAS caracteres" else null
            )
        }
    }

    fun guardar() {
        if (guardando) return
        val s = _uiState.value

        if (!s.esValido) {
            val otroInvalido = !esOtroValido(s.estrategia == EstrategiaNeuro.OTRO, s.estrategiaOtro)
            _uiState.update {
                it.copy(
                    tipoEntradaError = if (s.tipoEntrada == null) "Selecciona el tipo de entrada" else null,
                    estrategiaError = if (s.estrategia == null) "Selecciona una estrategia" else null,
                    estrategiaOtroError = if (otroInvalido) "Escribe qué estrategia usaste" else null,
                    resultadoError = if (s.resultado == null) "Selecciona cómo te fue" else null,
                    notasError = if (!sonNotasValidas(s.notas, MAX_NOTAS)) "Máximo $MAX_NOTAS caracteres" else null
                )
            }
            return
        }

        val tipo = s.tipoEntrada ?: return
        val estrategia = s.estrategia ?: return
        val resultado = s.resultado ?: return

        val datos = DatosNeuro(
            tipoEntrada = tipo.etiqueta,
            intensidad = s.intensidad,
            estrategia = if (estrategia == EstrategiaNeuro.OTRO) s.estrategiaOtro.trim() else estrategia.etiqueta,
            resultadoPercibido = resultado.etiqueta
        )

        guardando = true
        viewModelScope.launch {
            try {
                val idEdicion = s.idRegistro
                if (idEdicion != null) {
                    repository.actualizarNeuro(
                        idRegistro = idEdicion,
                        datos = datos,
                        notas = s.notas.ifBlank { null }
                    )
                } else {
                    repository.guardarNeuro(
                        idUsuario = idUsuario,
                        datos = datos,
                        notas = s.notas.ifBlank { null }
                    )
                    val racha = calcularRacha(repository.observarHistorial(idUsuario).first())
                    NotificacionHelper.mostrarAlertaRegistroGuardado(getApplication(), "Neurodesarrollo", racha)
                }
                _uiState.update { it.copy(guardado = true) }
            } finally {
                guardando = false
            }
        }
    }

    fun onGuardadoConsumido() {
        _uiState.value = NeuroUiState()
    }

    companion object {
        const val MAX_NOTAS = 200
    }
}