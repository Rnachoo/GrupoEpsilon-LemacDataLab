package com.example.lemacdatalab.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.lemacdatalab.model.RegistroMaestro
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: UsuarioRepository

    @Before
    fun crearDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repository = UsuarioRepository(db.usuarioDao())
    }

    @After
    fun cerrarDb() {
        db.close()
    }

    @Test
    fun usuarioPrueba_seEncuentraPorIdentificador() = runBlocking {
        repository.sembrarUsuarioPrueba()
        repository.sembrarUsuarioPrueba() // no debe duplicarse

        assertNotNull(repository.buscarPorIdentificador("AX-7B99"))
        assertNull(repository.buscarPorIdentificador("ZZ-0000"))
    }

    @Test
    fun registroMaestro_seGuardaYSeRecupera() = runBlocking {
        repository.sembrarUsuarioPrueba()
        val id = db.registroMaestroDao().insertar(
            RegistroMaestro(
                idRegistro = 0,
                idUsuario = 1,
                tipoArea = "animo",
                fechaHoraCreacion = "2026-10-12 10:00:00"
            )
        )

        val registros = db.registroMaestroDao().obtenerPorUsuario(1).first()
        assertEquals(1, registros.size)
        assertEquals(id.toInt(), registros[0].idRegistro)
        assertEquals("animo", registros[0].tipoArea)
    }
}
