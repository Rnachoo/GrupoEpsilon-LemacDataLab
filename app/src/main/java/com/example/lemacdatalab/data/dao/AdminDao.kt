package com.example.lemacdatalab.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.lemacdatalab.model.ResumenCuenta
import kotlinx.coroutines.flow.Flow

// Solo métricas agregadas (COUNT / MAX). Nunca se leen notas ni escalas.
@Dao
interface AdminDao {

    @Query("SELECT COUNT(*) FROM registro_maestro")
    fun contarRegistros(): Flow<Int>

    @Query("SELECT COUNT(*) FROM registro_maestro WHERE tipoArea = :tipoArea")
    fun contarPorTipo(tipoArea: String): Flow<Int>

    @Query(
        """
        SELECT u.identificadorSintetico AS alias,
               COUNT(r.idRegistro) AS totalEventos,
               MAX(r.fechaHoraCreacion) AS ultimaActividad
        FROM usuario u
        LEFT JOIN registro_maestro r ON r.idUsuario = u.idUsuario
        GROUP BY u.idUsuario
        ORDER BY u.identificadorSintetico
        """
    )
    fun resumenPorCuenta(): Flow<List<ResumenCuenta>>
}