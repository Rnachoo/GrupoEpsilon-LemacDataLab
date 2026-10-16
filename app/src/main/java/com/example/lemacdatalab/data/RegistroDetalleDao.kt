package com.example.lemacdatalab.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.lemacdatalab.model.DbtDetalle
import com.example.lemacdatalab.model.MoodDetalle
import com.example.lemacdatalab.model.RegistroHistorial
import com.example.lemacdatalab.model.RegistroMaestro
import kotlinx.coroutines.flow.Flow

@Dao
abstract class RegistroDetalleDao {

    @Insert
    abstract suspend fun insertarMaestro(registro: RegistroMaestro): Long

    @Insert
    abstract suspend fun insertarMood(detalle: MoodDetalle)

    @Insert
    abstract suspend fun insertarDbt(detalle: DbtDetalle)

    // Maestro y detalle se guardan juntos: si uno falla, no se guarda ninguno
    @Transaction
    open suspend fun guardarMood(registro: RegistroMaestro, nivel: Int, contexto: String) {
        val id = insertarMaestro(registro).toInt()
        insertarMood(MoodDetalle(idRegistro = id, nivelIntensidad = nivel, contexto = contexto))
    }

    @Transaction
    open suspend fun guardarDbt(registro: RegistroMaestro, habilidad: String, nivelImpulso: Int) {
        val id = insertarMaestro(registro).toInt()
        insertarDbt(DbtDetalle(idRegistro = id, habilidad = habilidad, nivelImpulso = nivelImpulso))
    }

    @Query(
        """
        SELECT r.idRegistro AS idRegistro,
               r.tipoArea AS tipoArea,
               r.fechaHoraCreacion AS fechaHoraCreacion,
               r.notasOpcionales AS notasOpcionales,
               m.nivelIntensidad AS nivelIntensidad,
               m.contexto AS contexto,
               d.habilidad AS habilidad,
               d.nivelImpulso AS nivelImpulso
        FROM registro_maestro r
        LEFT JOIN mood_detalle m ON m.idRegistro = r.idRegistro
        LEFT JOIN dbt_detalle d ON d.idRegistro = r.idRegistro
        WHERE r.idUsuario = :idUsuario
        ORDER BY r.fechaHoraCreacion DESC, r.idRegistro DESC
        """
    )
    abstract fun observarHistorial(idUsuario: Int): Flow<List<RegistroHistorial>>
}