package com.example.lemacdatalab.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.lemacdatalab.data.AppDatabase
import com.example.lemacdatalab.data.dao.ContenidoDao
import com.example.lemacdatalab.model.ContenidoEducativo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class ContenidoViewModel(private val contenidoDao: ContenidoDao) : ViewModel() {


    val contenidos: StateFlow<List<ContenidoEducativo>> = contenidoDao.obtenerTodos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    init {

        verificarEInyectarSemilla()
    }

    private fun verificarEInyectarSemilla() {
        viewModelScope.launch {
            val listaActual = contenidoDao.obtenerTodos().firstOrNull()


            if (listaActual.isNullOrEmpty()) {
                val semilla1 = ContenidoEducativo(
                    titulo = "Aceptación Radical en crisis",
                    extracto = "La aceptación radical implica dejar de luchar contra la realidad de una situación dolorosa. No significa que la apruebes, sino que reconoces los hechos tal como son para evitar que el dolor se convierta en sufrimiento.",
                    fuente = "Manual de Habilidades DBT, M. Linehan",
                    ambito = "DBT",
                    esLecturaDelDia = true
                )
                val semilla2 = ContenidoEducativo(
                    titulo = "Higiene del sueño y regulación",
                    extracto = "Mantener horarios regulares para dormir y despertar estabiliza el ritmo circadiano. Esta constancia biológica es fundamental para prevenir fluctuaciones severas en el estado de ánimo durante el día.",
                    fuente = "Guía de Autocuidado Clínico",
                    ambito = "Estado de Ánimo"
                )
                val semilla3 = ContenidoEducativo(
                    titulo = "Postergación del impulso",
                    extracto = "Cuando sientas un impulso muy fuerte (craving), comprométete a esperar solo 15 minutos antes de actuar. Durante ese tiempo, distrae tu mente. A menudo, la intensidad bajará como una ola.",
                    fuente = "Terapia Cognitivo Conductual para Adicciones",
                    ambito = "Adicciones"
                )

                contenidoDao.guardarContenido(semilla1)
                contenidoDao.guardarContenido(semilla2)
                contenidoDao.guardarContenido(semilla3)
            }
        }
    }

    // CRUD: Crear o Actualizar
    fun guardar(id: String?, titulo: String, extracto: String, fuente: String, ambito: String): Boolean {

        // Validacion: ningún campo puede quedar en blanco
        if (titulo.isBlank() || extracto.isBlank() || fuente.isBlank() || ambito.isBlank()) {
            return false
        }

        viewModelScope.launch {
            val contenido = ContenidoEducativo(
                id = id ?: UUID.randomUUID().toString(),
                titulo = titulo,
                extracto = extracto,
                fuente = fuente,
                ambito = ambito,

                esLecturaDelDia = false
            )
            contenidoDao.guardarContenido(contenido)
        }
        return true
    }

    // CRUD: Marcar lectura exclusiva del día
    fun marcarLecturaDelDia(contenidoDestacado: ContenidoEducativo) {
        viewModelScope.launch {

            val actual = contenidoDao.obtenerLecturaDelDia().firstOrNull()
            actual?.let {
                contenidoDao.guardarContenido(it.copy(esLecturaDelDia = false))
            }


            contenidoDao.guardarContenido(contenidoDestacado.copy(esLecturaDelDia = true))
        }
    }

    // CRUD: Eliminar
    fun eliminar(contenido: ContenidoEducativo) {
        viewModelScope.launch {
            contenidoDao.eliminarContenido(contenido)
        }
    }


    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as android.app.Application)
                val database = AppDatabase.getDatabase(application)
                ContenidoViewModel(database.contenidoDao())
            }
        }
    }
}