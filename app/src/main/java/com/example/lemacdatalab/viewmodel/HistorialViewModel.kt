package com.example.lemacdatalab.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lemacdatalab.data.AppDatabase
import com.example.lemacdatalab.data.RegistroRepository
import com.example.lemacdatalab.data.RoomRegistroRepository
import com.example.lemacdatalab.data.SesionUsuario
import com.example.lemacdatalab.data.UsuarioRepository
import com.example.lemacdatalab.model.RegistroHistorial
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistorialViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RegistroRepository = RoomRegistroRepository(
        AppDatabase.getDatabase(application).registroDetalleDao()
    )

    private val idUsuario: Int
        get() = SesionUsuario.idUsuario ?: UsuarioRepository.USUARIO_PRUEBA.idUsuario

    // null = cargando; lista vacía = el usuario aún no tiene registros
    val registros: StateFlow<List<RegistroHistorial>?> = repository
        .observarHistorial(idUsuario)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // La lista se actualiza sola: Room vuelve a emitir cuando cambia la tabla
    fun eliminar(idRegistro: Int) {
        viewModelScope.launch {
            repository.eliminar(idUsuario, idRegistro)
        }
    }
}