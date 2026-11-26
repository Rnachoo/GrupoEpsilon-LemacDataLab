package com.example.lemacdatalab.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.lemacdatalab.data.repository.RoomRegistroRepository
import com.example.lemacdatalab.model.DatosNeuro
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GuardarNeuroTest {

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

    private fun datos(intensidad: Int, estrategia: String) = DatosNeuro(
        tipoEntrada = "Rutina",
        intensidad = intensidad,
        estrategia = estrategia,
        resultadoPercibido = "Me ayudó"
    )

    @Test
    fun guardarNeuro_apareceEnElHistorialConSusDatos() = runBlocking {
        repository.guardarNeuro(1, datos(3, "Pausa breve"), "nota")

        val historial = repository.observarHistorial(1).first()
        assertEquals(1, historial.size)
        assertEquals("neurodesarrollo", historial[0].tipoArea)
        assertEquals("Rutina", historial[0].tipoEntrada)
        assertEquals(3, historial[0].intensidadNeuro)
        assertEquals("Pausa breve", historial[0].estrategia)
        assertEquals("Me ayudó", historial[0].resultadoPercibido)
    }

    @Test
    fun segundoGuardadoDelMismoDia_actualizaYDejaLaVersionAnterior() = runBlocking {
        repository.guardarNeuro(1, datos(2, "Pausa breve"), null)
        repository.guardarNeuro(1, datos(4, "Lista de pasos"), null)

        val historial = repository.observarHistorial(1).first()
        assertEquals(1, historial.size)
        assertEquals(4, historial[0].intensidadNeuro)
        assertEquals(1, db.registroDetalleDao().contarVersiones(historial[0].idRegistro))
    }

    @Test
    fun actualizarNeuro_cambiaLosDatosYDejaLaVersionAnterior() = runBlocking {
        repository.guardarNeuro(1, datos(2, "Pausa breve"), null)
        val id = repository.observarHistorial(1).first()[0].idRegistro

        repository.actualizarNeuro(id, datos(1, "Temporizador"), "ajustado")

        val registro = repository.obtenerRegistro(1, id)
        assertEquals(1, registro?.intensidadNeuro)
        assertEquals("Temporizador", registro?.estrategia)
        assertEquals(1, db.registroDetalleDao().contarVersiones(id))
    }
}