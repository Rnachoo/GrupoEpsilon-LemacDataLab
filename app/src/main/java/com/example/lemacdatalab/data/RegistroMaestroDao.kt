package com.example.lemacdatalab.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.lemacdatalab.model.RegistroMaestro
import kotlinx.coroutines.flow.Flow

@Dao
interface RegistroMaestroDao {

    // Devuelve el idRegistro generado
    @Insert
    suspend fun insertar(registro: RegistroMaestro): Long

    @Query("SELECT * FROM registro_maestro WHERE idUsuario = :idUsuario ORDER BY fechaHoraCreacion DESC")
    fun obtenerPorUsuario(idUsuario: Int): Flow<List<RegistroMaestro>>
}
