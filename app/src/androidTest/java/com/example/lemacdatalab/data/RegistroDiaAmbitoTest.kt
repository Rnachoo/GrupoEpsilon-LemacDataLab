package com.example.lemacdatalab.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.lemacdatalab.data.repository.RoomRegistroRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegistroDiaAmbitoTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: RoomRegistroRepository

    @Before
    fun crearDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repository = RoomRegistroRepository(db.registroDetalleDao())
    }

    @After
    fun cerrarDb() {
        db.close()
    }

    @Test
    fun segundoGuardadoDelMismoDia_actualizaYDejaLaVersionAnterior() = runBlocking {
        repository.guardarMood(1, 3, "Trabajo", null)
        repository.guardarMood(1, 5, "Casa", "nota")

        val historial = repository.observarHistorial(1).first()
        assertEquals(1, historial.size)
        assertEquals(5, historial[0].nivelIntensidad)
        assertEquals(1, db.registroDetalleDao().contarVersiones(historial[0].idRegistro))
    }

    @Test
    fun registroEliminado_seReutilizaAlGuardarDeNuevo() = runBlocking {
        repository.guardarMood(1, 3, "Trabajo", null)
        val idOriginal = repository.observarHistorial(1).first()[0].idRegistro
        repository.eliminar(1, idOriginal)

        repository.guardarMood(1, 2, "Casa", null)

        val historial = repository.observarHistorial(1).first()
        assertEquals(1, historial.size)
        assertEquals(idOriginal, historial[0].idRegistro)
    }
}