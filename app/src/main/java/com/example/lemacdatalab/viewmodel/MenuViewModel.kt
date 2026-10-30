package com.example.lemacdatalab.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lemacdatalab.data.AppDatabase
import com.example.lemacdatalab.data.SesionUsuario
import com.example.lemacdatalab.data.repository.RegistroRepository
import com.example.lemacdatalab.data.repository.RoomRegistroRepository
import com.example.lemacdatalab.data.repository.UsuarioRepository
import com.example.lemacdatalab.model.calcularRacha
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MenuViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RegistroRepository = RoomRegistroRepository(
        AppDatabase.getDatabase(application).registroDetalleDao()
    )

    private val idUsuario: Int
        get() = SesionUsuario.idUsuario ?: UsuarioRepository.USUARIO_PRUEBA.idUsuario

    private val _onboardingVisto = MutableStateFlow(SesionUsuario.onboardingVisto(application))

    // el historial llega ordenado del más reciente al más antiguo
    val uiState: StateFlow<MenuUiState> = combine(
        repository.observarHistorial(idUsuario),
        _onboardingVisto
    ) { registros, visto ->
        val hoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        MenuUiState(
            racha = calcularRacha(registros),
            ultimosEventos = registros.take(3),
            hayRegistroHoy = registros.any { it.fechaHoraCreacion.startsWith(hoy) },
            onboardingVisto = visto
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        MenuUiState(onboardingVisto = _onboardingVisto.value)
    )

    fun marcarOnboardingVisto() {
        SesionUsuario.marcarOnboardingVisto(getApplication())
        _onboardingVisto.value = true
    }
}
