package com.example.lemacdatalab.data

import com.example.lemacdatalab.model.RegistroHistorial
import com.example.lemacdatalab.model.RegistroMaestro
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

interface RegistroRepository {
    suspend fun guardarMood(idUsuario: Int, nivel: Int, contexto: String, notas: String?)
    suspend fun guardarDbt(idUsuario: Int, habilidad: String, nivelImpulso: Int, notas: String?)
    fun observarHistorial(idUsuario: Int): Flow<List<RegistroHistorial>>
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

    private fun nuevoRegistro(idUsuario: Int, tipoArea: String, notas: String?) = RegistroMaestro(
        idRegistro = 0, // 0 para que Room genere el id
        idUsuario = idUsuario,
        tipoArea = tipoArea,
        fechaHoraCreacion = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
        notasOpcionales = notas
    )
}