package com.example.lemacdatalab.data.repository

import com.example.lemacdatalab.data.dao.RegistroDetalleDao
import com.example.lemacdatalab.model.DatosAnimo
import com.example.lemacdatalab.model.DatosNeuro
import com.example.lemacdatalab.model.RegistroHistorial
import com.example.lemacdatalab.model.RegistroMaestro
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

interface RegistroRepository {
    // Create
    suspend fun guardarMood(idUsuario: Int, datos: DatosAnimo, notas: String?)
    suspend fun guardarDbt(idUsuario: Int, habilidad: String, intensidadEmocional: Int, notas: String?)
    suspend fun guardarNeuro(idUsuario: Int, datos: DatosNeuro, notas: String?)

    // Read
    fun observarHistorial(idUsuario: Int): Flow<List<RegistroHistorial>>
    suspend fun obtenerRegistro(idUsuario: Int, idRegistro: Int): RegistroHistorial?

    // Update
    suspend fun actualizarMood(idRegistro: Int, datos: DatosAnimo, notas: String?)
    suspend fun actualizarDbt(idRegistro: Int, habilidad: String, intensidadEmocional: Int, notas: String?)
    suspend fun actualizarNeuro(idRegistro: Int, datos: DatosNeuro, notas: String?)

    suspend fun eliminar(idUsuario: Int, idRegistro: Int)

    suspend fun eliminarTodos(idUsuario: Int)
}

class RoomRegistroRepository(
    private val dao: RegistroDetalleDao
) : RegistroRepository {

    override suspend fun guardarMood(idUsuario: Int, datos: DatosAnimo, notas: String?) {
        dao.guardarMood(nuevoRegistro(idUsuario, "animo", notas), datos)
    }

    override suspend fun guardarDbt(idUsuario: Int, habilidad: String, intensidadEmocional: Int, notas: String?) {
        dao.guardarDbt(nuevoRegistro(idUsuario, "dbt", notas), habilidad, intensidadEmocional)
    }

    override suspend fun guardarNeuro(idUsuario: Int, datos: DatosNeuro, notas: String?) {
        dao.guardarNeuro(nuevoRegistro(idUsuario, "neurodesarrollo", notas), datos)
    }

    override fun observarHistorial(idUsuario: Int): Flow<List<RegistroHistorial>> =
        dao.observarHistorial(idUsuario)

    override suspend fun obtenerRegistro(idUsuario: Int, idRegistro: Int): RegistroHistorial? =
        dao.obtenerRegistro(idUsuario, idRegistro)

    override suspend fun actualizarMood(idRegistro: Int, datos: DatosAnimo, notas: String?) {
        dao.actualizarMood(idRegistro, datos, notas)
    }

    override suspend fun actualizarDbt(idRegistro: Int, habilidad: String, intensidadEmocional: Int, notas: String?) {
        dao.actualizarDbt(idRegistro, habilidad, intensidadEmocional, notas)
    }

    override suspend fun actualizarNeuro(idRegistro: Int, datos: DatosNeuro, notas: String?) {
        dao.actualizarNeuro(idRegistro, datos, notas)
    }

    override suspend fun eliminar(idUsuario: Int, idRegistro: Int) {
        dao.eliminarRegistro(idUsuario, idRegistro)
    }

    override suspend fun eliminarTodos(idUsuario: Int) {
        dao.eliminarTodos(idUsuario)
    }

    private fun nuevoRegistro(idUsuario: Int, tipoArea: String, notas: String?): RegistroMaestro {
        val ahora = Date()
        return RegistroMaestro(
            idRegistro = 0,
            idUsuario = idUsuario,
            tipoArea = tipoArea,
            fechaHoraCreacion = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(ahora),
            notasOpcionales = notas,
            fechaDia = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(ahora),
            modificadoEnUtc = ahora.time
        )
    }
}