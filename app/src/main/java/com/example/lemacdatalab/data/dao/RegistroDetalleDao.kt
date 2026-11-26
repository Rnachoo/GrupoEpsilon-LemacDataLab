package com.example.lemacdatalab.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.lemacdatalab.model.DatosAnimo
import com.example.lemacdatalab.model.DatosNeuro
import com.example.lemacdatalab.model.DbtDetalle
import com.example.lemacdatalab.model.MoodDetalle
import com.example.lemacdatalab.model.NeuroDetalle
import com.example.lemacdatalab.model.RegistroHistorial
import com.example.lemacdatalab.model.RegistroMaestro
import com.example.lemacdatalab.model.RegistroVersion
import com.example.lemacdatalab.model.nivelResumenAnimo
import kotlinx.coroutines.flow.Flow

@Dao
abstract class RegistroDetalleDao {

    // ---------- INSERT ----------

    @Insert
    abstract suspend fun insertarMaestro(registro: RegistroMaestro): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertarMood(detalle: MoodDetalle)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertarDbt(detalle: DbtDetalle)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertarNeuro(detalle: NeuroDetalle)

    @Insert
    abstract suspend fun insertarVersion(version: RegistroVersion)

    // ---------- LECTURAS AUXILIARES ----------

    @Query(
        "SELECT * FROM registro_maestro " +
                "WHERE idUsuario = :idUsuario AND fechaDia = :fechaDia AND tipoArea = :tipoArea LIMIT 1"
    )
    abstract suspend fun buscarPorClave(idUsuario: Int, fechaDia: String, tipoArea: String): RegistroMaestro?

    @Query("SELECT * FROM registro_maestro WHERE idRegistro = :idRegistro")
    abstract suspend fun obtenerMaestro(idRegistro: Int): RegistroMaestro?

    @Query("SELECT * FROM mood_detalle WHERE idRegistro = :idRegistro")
    abstract suspend fun obtenerMoodDetalle(idRegistro: Int): MoodDetalle?

    @Query("SELECT * FROM dbt_detalle WHERE idRegistro = :idRegistro")
    abstract suspend fun obtenerDbtDetalle(idRegistro: Int): DbtDetalle?

    @Query("SELECT * FROM neuro_detalle WHERE idRegistro = :idRegistro")
    abstract suspend fun obtenerNeuroDetalle(idRegistro: Int): NeuroDetalle?

    @Query("SELECT COUNT(*) FROM registro_version WHERE idRegistro = :idRegistro")
    abstract suspend fun contarVersiones(idRegistro: Int): Int

    // ---------- GUARDAR (un registro por usuario, día y ámbito) ----------

    @Transaction
    open suspend fun guardarMood(registro: RegistroMaestro, datos: DatosAnimo) {
        val id = guardarMaestroDelDia(registro)
        insertarMood(
            MoodDetalle(
                idRegistro = id,
                nivelIntensidad = nivelResumenAnimo(datos.depresion, datos.elevacion),
                contexto = datos.contexto,
                depresion = datos.depresion,
                elevacion = datos.elevacion,
                horasSueno = datos.horasSueno,
                altoRiesgo = datos.altoRiesgo,
                sintomasPsicoticos = datos.sintomasPsicoticos
            )
        )
    }

    @Transaction
    open suspend fun guardarDbt(registro: RegistroMaestro, habilidad: String, intensidadEmocional: Int) {
        val id = guardarMaestroDelDia(registro)
        insertarDbt(
            DbtDetalle(
                idRegistro = id,
                habilidad = habilidad,
                intensidadEmocional = intensidadEmocional
            )
        )
    }

    @Transaction
    open suspend fun guardarNeuro(registro: RegistroMaestro, datos: DatosNeuro) {
        val id = guardarMaestroDelDia(registro)
        insertarNeuro(
            NeuroDetalle(
                idRegistro = id,
                tipoEntrada = datos.tipoEntrada,
                intensidad = datos.intensidad,
                estrategia = datos.estrategia,
                resultadoPercibido = datos.resultadoPercibido
            )
        )
    }

    private suspend fun guardarMaestroDelDia(registro: RegistroMaestro): Int {
        val existente = buscarPorClave(registro.idUsuario, registro.fechaDia, registro.tipoArea)
            ?: return insertarMaestro(registro).toInt()

        guardarVersionAnterior(existente.idRegistro, registro.modificadoEnUtc)
        val estado = if (existente.estadoRegistro == "eliminado") "confirmado" else "editado"
        actualizarMaestro(existente.idRegistro, estado, registro.notasOpcionales, registro.modificadoEnUtc)
        return existente.idRegistro
    }

    @Transaction
    open suspend fun guardarVersionAnterior(idRegistro: Int, ahoraUtc: Long) {
        val maestro = obtenerMaestro(idRegistro) ?: return
        val detalle = obtenerMoodDetalle(idRegistro)?.let { describirMood(it) }
            ?: obtenerDbtDetalle(idRegistro)?.let { describirDbt(it) }
            ?: obtenerNeuroDetalle(idRegistro)?.let { describirNeuro(it) }
            ?: ""
        insertarVersion(
            RegistroVersion(
                idRegistro = idRegistro,
                fechaVersionUtc = ahoraUtc,
                estadoAnterior = maestro.estadoRegistro,
                notasAnteriores = maestro.notasOpcionales,
                detalleAnterior = detalle
            )
        )
    }

    private fun describirMood(m: MoodDetalle) =
        "nivelIntensidad=${m.nivelIntensidad};contexto=${m.contexto};depresion=${m.depresion};" +
                "elevacion=${m.elevacion};horasSueno=${m.horasSueno};altoRiesgo=${m.altoRiesgo};" +
                "sintomasPsicoticos=${m.sintomasPsicoticos}"

    private fun describirDbt(d: DbtDetalle) =
        "habilidad=${d.habilidad};intensidadEmocional=${d.intensidadEmocional}"

    private fun describirNeuro(n: NeuroDetalle) =
        "tipoEntrada=${n.tipoEntrada};intensidad=${n.intensidad};estrategia=${n.estrategia};" +
                "resultadoPercibido=${n.resultadoPercibido}"

    @Query(
        "UPDATE registro_maestro SET estadoRegistro = :estado, notasOpcionales = :notas, " +
                "estadoSync = 'pendiente', modificadoEnUtc = :ahoraUtc WHERE idRegistro = :idRegistro"
    )
    abstract suspend fun actualizarMaestro(idRegistro: Int, estado: String, notas: String?, ahoraUtc: Long)

    // ---------- READ ----------

    @Query(
        """
        SELECT r.idRegistro AS idRegistro,
               r.tipoArea AS tipoArea,
               r.fechaHoraCreacion AS fechaHoraCreacion,
               r.notasOpcionales AS notasOpcionales,
               r.estadoRegistro AS estadoRegistro,
               m.nivelIntensidad AS nivelIntensidad,
               m.contexto AS contexto,
               d.habilidad AS habilidad,
               d.intensidadEmocional AS nivelImpulso,
               m.depresion AS depresion,
               m.elevacion AS elevacion,
               m.horasSueno AS horasSueno,
               m.altoRiesgo AS altoRiesgo,
               m.sintomasPsicoticos AS sintomasPsicoticos,
               n.tipoEntrada AS tipoEntrada,
               n.intensidad AS intensidadNeuro,
               n.estrategia AS estrategia,
               n.resultadoPercibido AS resultadoPercibido
        FROM registro_maestro r
        LEFT JOIN mood_detalle m ON m.idRegistro = r.idRegistro
        LEFT JOIN dbt_detalle d ON d.idRegistro = r.idRegistro
        LEFT JOIN neuro_detalle n ON n.idRegistro = r.idRegistro
        WHERE r.idUsuario = :idUsuario AND r.estadoRegistro != 'eliminado'
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
               r.estadoRegistro AS estadoRegistro,
               m.nivelIntensidad AS nivelIntensidad,
               m.contexto AS contexto,
               d.habilidad AS habilidad,
               d.intensidadEmocional AS nivelImpulso,
               m.depresion AS depresion,
               m.elevacion AS elevacion,
               m.horasSueno AS horasSueno,
               m.altoRiesgo AS altoRiesgo,
               m.sintomasPsicoticos AS sintomasPsicoticos,
               n.tipoEntrada AS tipoEntrada,
               n.intensidad AS intensidadNeuro,
               n.estrategia AS estrategia,
               n.resultadoPercibido AS resultadoPercibido
        FROM registro_maestro r
        LEFT JOIN mood_detalle m ON m.idRegistro = r.idRegistro
        LEFT JOIN dbt_detalle d ON d.idRegistro = r.idRegistro
        LEFT JOIN neuro_detalle n ON n.idRegistro = r.idRegistro
        WHERE r.idRegistro = :idRegistro AND r.idUsuario = :idUsuario AND r.estadoRegistro != 'eliminado'
        """
    )
    abstract suspend fun obtenerRegistro(idUsuario: Int, idRegistro: Int): RegistroHistorial?

    // ---------- UPDATE ----------

    @Query(
        "UPDATE mood_detalle SET nivelIntensidad = :nivel, contexto = :contexto, " +
                "depresion = :depresion, elevacion = :elevacion, horasSueno = :horasSueno, " +
                "altoRiesgo = :altoRiesgo, sintomasPsicoticos = :sintomasPsicoticos " +
                "WHERE idRegistro = :idRegistro"
    )
    abstract suspend fun actualizarDetalleMood(
        idRegistro: Int,
        nivel: Int,
        contexto: String,
        depresion: Int,
        elevacion: Int,
        horasSueno: Int?,
        altoRiesgo: Boolean,
        sintomasPsicoticos: Boolean
    )

    @Query(
        "UPDATE dbt_detalle SET habilidad = :habilidad, intensidadEmocional = :intensidadEmocional " +
                "WHERE idRegistro = :idRegistro"
    )
    abstract suspend fun actualizarDetalleDbt(idRegistro: Int, habilidad: String, intensidadEmocional: Int)

    @Query(
        "UPDATE neuro_detalle SET tipoEntrada = :tipoEntrada, intensidad = :intensidad, " +
                "estrategia = :estrategia, resultadoPercibido = :resultadoPercibido " +
                "WHERE idRegistro = :idRegistro"
    )
    abstract suspend fun actualizarDetalleNeuro(
        idRegistro: Int,
        tipoEntrada: String,
        intensidad: Int,
        estrategia: String,
        resultadoPercibido: String
    )

    @Transaction
    open suspend fun actualizarMood(idRegistro: Int, datos: DatosAnimo, notas: String?) {
        val ahora = System.currentTimeMillis()
        guardarVersionAnterior(idRegistro, ahora)
        actualizarMaestro(idRegistro, "editado", notas, ahora)
        actualizarDetalleMood(
            idRegistro = idRegistro,
            nivel = nivelResumenAnimo(datos.depresion, datos.elevacion),
            contexto = datos.contexto,
            depresion = datos.depresion,
            elevacion = datos.elevacion,
            horasSueno = datos.horasSueno,
            altoRiesgo = datos.altoRiesgo,
            sintomasPsicoticos = datos.sintomasPsicoticos
        )
    }

    @Transaction
    open suspend fun actualizarDbt(idRegistro: Int, habilidad: String, intensidadEmocional: Int, notas: String?) {
        val ahora = System.currentTimeMillis()
        guardarVersionAnterior(idRegistro, ahora)
        actualizarMaestro(idRegistro, "editado", notas, ahora)
        actualizarDetalleDbt(idRegistro, habilidad, intensidadEmocional)
    }

    @Transaction
    open suspend fun actualizarNeuro(idRegistro: Int, datos: DatosNeuro, notas: String?) {
        val ahora = System.currentTimeMillis()
        guardarVersionAnterior(idRegistro, ahora)
        actualizarMaestro(idRegistro, "editado", notas, ahora)
        actualizarDetalleNeuro(
            idRegistro = idRegistro,
            tipoEntrada = datos.tipoEntrada,
            intensidad = datos.intensidad,
            estrategia = datos.estrategia,
            resultadoPercibido = datos.resultadoPercibido
        )
    }

    // ---------- DELETE (borrado lógico) ----------

    @Query(
        "UPDATE registro_maestro SET estadoRegistro = 'eliminado', estadoSync = 'pendiente', " +
                "modificadoEnUtc = :ahoraUtc WHERE idRegistro = :idRegistro AND idUsuario = :idUsuario"
    )
    abstract suspend fun marcarEliminado(idUsuario: Int, idRegistro: Int, ahoraUtc: Long)

    @Query(
        "UPDATE registro_maestro SET estadoRegistro = 'eliminado', estadoSync = 'pendiente', " +
                "modificadoEnUtc = :ahoraUtc WHERE idUsuario = :idUsuario"
    )
    abstract suspend fun marcarTodosEliminados(idUsuario: Int, ahoraUtc: Long)

    @Transaction
    open suspend fun eliminarRegistro(idUsuario: Int, idRegistro: Int) {
        marcarEliminado(idUsuario, idRegistro, System.currentTimeMillis())
    }

    @Transaction
    open suspend fun eliminarTodos(idUsuario: Int) {
        marcarTodosEliminados(idUsuario, System.currentTimeMillis())
    }
}