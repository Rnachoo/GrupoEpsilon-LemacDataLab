package com.example.lemacdatalab.data

import com.example.lemacdatalab.model.RegistroHistorial
import com.example.lemacdatalab.model.RegistroMaestro
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Los ViewModels dependen de esta interfaz, no de Room directamente
interface RegistroRepository {
    // Create
    suspend fun guardarMood(idUsuario: Int, nivel: Int, contexto: String, notas: String?)
    suspend fun guardarDbt(idUsuario: Int, habilidad: String, nivelImpulso: Int, notas: String?)

    // Read
    fun observarHistorial(idUsuario: Int): Flow<List<RegistroHistorial>>
    suspend fun obtenerRegistro(idUsuario: Int, idRegistro: Int): RegistroHistorial?

    // Update
    suspend fun actualizarMood(idRegistro: Int, nivel: Int, contexto: String, notas: String?)
    suspend fun actualizarDbt(idRegistro: Int, habilidad: String, nivelImpulso: Int, notas: String?)

    // Delete
    suspend fun eliminar(idUsuario: Int, idRegistro: Int)
}

class RoomRegistroRepository(
    private val dao: RegistroDetalleDao
) : RegistroRepository {

    override suspend fun guardarMood(idUsuario: Int, nivel: Int, contexto: String, notas: String?) {
        dao.guardarMood(nuevoRegistro(idUsuario, "animo", notas), nivel, contexto)
    }

    override suspend fun guardarDbt(idUsuario: Int, habilidad: String, nivelImpulso: Int, notas: String?) {
        dao.guardarDbt(nuevoRegistro(idUsuario, "dbt", notas), habilidad, nivelImpulso)
    }

    override fun observarHistorial(idUsuario: Int): Flow<List<RegistroHistorial>> =
        dao.observarHistorial(idUsuario)

    override suspend fun obtenerRegistro(idUsuario: Int, idRegistro: Int): RegistroHistorial? =
        dao.obtenerRegistro(idUsuario, idRegistro)

    override suspend fun actualizarMood(idRegistro: Int, nivel: Int, contexto: String, notas: String?) {
        dao.actualizarMood(idRegistro, nivel, contexto, notas)
    }

    override suspend fun actualizarDbt(idRegistro: Int, habilidad: String, nivelImpulso: Int, notas: String?) {
        dao.actualizarDbt(idRegistro, habilidad, nivelImpulso, notas)
    }

    override suspend fun eliminar(idUsuario: Int, idRegistro: Int) {
        dao.eliminarRegistro(idUsuario, idRegistro)
    }

    private fun nuevoRegistro(idUsuario: Int, tipoArea: String, notas: String?) = RegistroMaestro(
        idRegistro = 0, // 0 para que Room genere el id
        idUsuario = idUsuario,
        tipoArea = tipoArea,
        fechaHoraCreacion = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
        notasOpcionales = notas
    )
}