package com.example.lemacdatalab.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lemacdatalab.data.AppDatabase
import com.example.lemacdatalab.data.SesionUsuario
import com.example.lemacdatalab.data.repository.UsuarioRepository
import com.example.lemacdatalab.model.RolUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val repository = UsuarioRepository(
        AppDatabase.getDatabase(application).usuarioDao()
    )

    // Asegura que las cuentas sintéticas existan antes de cualquier ingreso
    private val siembra = viewModelScope.launch {
        repository.sembrarUsuarioPrueba()
    }

    fun onIdChange(texto: String) {
        _uiState.update {
            it.copy(idSintetico = texto.uppercase().take(MAX_ID), idError = null)
        }
    }

    fun onDeslindeChange(aceptado: Boolean) {
        _uiState.update { it.copy(deslindeAceptado = aceptado) }
    }

    fun ingresar() {
        val s = _uiState.value
        if (!s.deslindeAceptado) return

        val id = s.idSintetico.trim()
        val error = when {
            id.isBlank() -> "El ID sintético es obligatorio"
            !FORMATO_ID.matches(id) -> "Formato inválido. Ejemplo: AX-7B99"
            else -> null
        }
        if (error != null) {
            _uiState.update { it.copy(idError = error) }
            return
        }

        viewModelScope.launch {
            siembra.join()
            // El ID debe existir exactamente; el rol se deduce de su prefijo
            val usuario = repository.buscarPorIdentificador(id)
            if (usuario == null) {
                _uiState.update { it.copy(idError = "ID no registrado en el sistema") }
                return@launch
            }
            SesionUsuario.iniciar(usuario)
            val rol = RolUsuario.detectarRol(usuario.identificadorSintetico)
            _uiState.update { it.copy(ingresado = true, rol = rol) }
        }
    }

    fun onIngresoConsumido() {
        _uiState.value = LoginUiState()
    }

    companion object {
        const val MAX_ID = 8 // "ADM-0001" tiene 8 caracteres
        private val FORMATO_ID = Regex("^[A-Z]{2,3}-[A-Z0-9]{4}$")
    }
}