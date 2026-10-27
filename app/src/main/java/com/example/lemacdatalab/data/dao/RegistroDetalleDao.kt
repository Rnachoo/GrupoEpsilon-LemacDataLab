package com.example.lemacdatalab.data.dao

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

    // ---------- CREATE ----------

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

    // ---------- READ ----------

    // Historial del usuario (todos los módulos), del más reciente al más antiguo
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

    // Un solo registro (para cargarlo en el formulario al editar)
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
        WHERE r.idRegistro = :idRegistro AND r.idUsuario = :idUsuario
        """
    )
    abstract suspend fun obtenerRegistro(idUsuario: Int, idRegistro: Int): RegistroHistorial?

    // ---------- UPDATE ----------

    @Query("UPDATE registro_maestro SET notasOpcionales = :notas WHERE idRegistro = :idRegistro")
    abstract suspend fun actualizarNotas(idRegistro: Int, notas: String?)

    @Query(
        "UPDATE mood_detalle SET nivelIntensidad = :nivel, contexto = :contexto " +
                "WHERE idRegistro = :idRegistro"
    )
    abstract suspend fun actualizarDetalleMood(idRegistro: Int, nivel: Int, contexto: String)

    @Query(
        "UPDATE dbt_detalle SET habilidad = :habilidad, nivelImpulso = :nivelImpulso " +
                "WHERE idRegistro = :idRegistro"
    )
    abstract suspend fun actualizarDetalleDbt(idRegistro: Int, habilidad: String, nivelImpulso: Int)

    @Transaction
    open suspend fun actualizarMood(idRegistro: Int, nivel: Int, contexto: String, notas: String?) {
        actualizarNotas(idRegistro, notas)
        actualizarDetalleMood(idRegistro, nivel, contexto)
    }

    @Transaction
    open suspend fun actualizarDbt(idRegistro: Int, habilidad: String, nivelImpulso: Int, notas: String?) {
        actualizarNotas(idRegistro, notas)
        actualizarDetalleDbt(idRegistro, habilidad, nivelImpulso)
    }

    // ---------- DELETE ----------

    @Query("DELETE FROM registro_maestro WHERE idRegistro = :idRegistro AND idUsuario = :idUsuario")
    abstract suspend fun eliminarRegistro(idUsuario: Int, idRegistro: Int)
}