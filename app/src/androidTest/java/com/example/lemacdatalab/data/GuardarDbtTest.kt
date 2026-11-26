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
class GuardarDbtTest {

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
    fun guardarDbt_guardaLaIntensidadEmocionalEnEscala0a4() = runBlocking {
        repository.guardarDbt(1, "Mindfulness", 3, null)

        val historial = repository.observarHistorial(1).first()
        assertEquals(1, historial.size)
        assertEquals("dbt", historial[0].tipoArea)
        assertEquals("Mindfulness", historial[0].habilidad)
        assertEquals(3, historial[0].nivelImpulso) // trae la intensidad emocional
    }

    @Test
    fun actualizarDbt_cambiaLaIntensidadYDejaLaVersionAnterior() = runBlocking {
        repository.guardarDbt(1, "Mindfulness", 3, null)
        val id = repository.observarHistorial(1).first()[0].idRegistro

        repository.actualizarDbt(id, "Mindfulness", 4, "nota")

        val registro = repository.obtenerRegistro(1, id)
        assertEquals(4, registro?.nivelImpulso)
        assertEquals(1, db.registroDetalleDao().contarVersiones(id))
    }
}