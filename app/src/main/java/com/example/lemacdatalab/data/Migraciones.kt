package com.example.lemacdatalab.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


private const val DESCARTADOS =
    "SELECT r.idRegistro FROM registro_maestro r WHERE EXISTS (" +
            "SELECT 1 FROM registro_maestro o " +
            "WHERE o.idUsuario = r.idUsuario AND o.fechaDia = r.fechaDia AND o.tipoArea = r.tipoArea " +
            "AND (o.fechaHoraCreacion > r.fechaHoraCreacion " +
            "OR (o.fechaHoraCreacion = r.fechaHoraCreacion AND o.idRegistro > r.idRegistro)))"


val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE registro_maestro ADD COLUMN fechaDia TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE registro_maestro ADD COLUMN modificadoEnUtc INTEGER NOT NULL DEFAULT 0")
        db.execSQL(
            "UPDATE registro_maestro SET fechaDia = substr(fechaHoraCreacion, 1, 10), " +
                    "modificadoEnUtc = COALESCE(CAST(strftime('%s', fechaHoraCreacion) AS INTEGER) * 1000, 0)"
        )

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS registro_version (" +
                    "idVersion INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "idRegistro INTEGER NOT NULL, fechaVersionUtc INTEGER NOT NULL, " +
                    "estadoAnterior TEXT NOT NULL, notasAnteriores TEXT, detalleAnterior TEXT NOT NULL, " +
                    "FOREIGN KEY(idRegistro) REFERENCES registro_maestro(idRegistro) " +
                    "ON UPDATE NO ACTION ON DELETE CASCADE)"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_registro_version_idRegistro ON registro_version (idRegistro)")

        db.execSQL(
            "INSERT INTO registro_version (idRegistro, fechaVersionUtc, estadoAnterior, notasAnteriores, detalleAnterior) " +
                    "SELECT (SELECT o.idRegistro FROM registro_maestro o " +
                    "WHERE o.idUsuario = r.idUsuario AND o.fechaDia = r.fechaDia AND o.tipoArea = r.tipoArea " +
                    "ORDER BY o.fechaHoraCreacion DESC, o.idRegistro DESC LIMIT 1), " +
                    "r.modificadoEnUtc, r.estadoRegistro, r.notasOpcionales, " +
                    "COALESCE('nivelIntensidad=' || m.nivelIntensidad || ';contexto=' || m.contexto, " +
                    "'habilidad=' || d.habilidad || ';nivelImpulso=' || d.nivelImpulso, '') " +
                    "FROM registro_maestro r " +
                    "LEFT JOIN mood_detalle m ON m.idRegistro = r.idRegistro " +
                    "LEFT JOIN dbt_detalle d ON d.idRegistro = r.idRegistro " +
                    "WHERE r.idRegistro IN ($DESCARTADOS)"
        )
        // Se borran los detalles a mano: no se depende de que las claves foráneas estén activas
        db.execSQL("DELETE FROM mood_detalle WHERE idRegistro IN ($DESCARTADOS)")
        db.execSQL("DELETE FROM dbt_detalle WHERE idRegistro IN ($DESCARTADOS)")
        db.execSQL("DELETE FROM registro_maestro WHERE idRegistro IN ($DESCARTADOS)")

        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS index_registro_maestro_idUsuario_fechaDia_tipoArea " +
                    "ON registro_maestro (idUsuario, fechaDia, tipoArea)"
        )

        db.execSQL("ALTER TABLE mood_detalle ADD COLUMN depresion INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE mood_detalle ADD COLUMN elevacion INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE mood_detalle ADD COLUMN horasSueno INTEGER")
        db.execSQL("ALTER TABLE mood_detalle ADD COLUMN altoRiesgo INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE mood_detalle ADD COLUMN sintomasPsicoticos INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE dbt_detalle ADD COLUMN intensidadEmocional INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE usuario ADD COLUMN objetivoPrueba TEXT")

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS neuro_detalle (" +
                    "idRegistro INTEGER NOT NULL, tipoEntrada TEXT NOT NULL, intensidad INTEGER NOT NULL, " +
                    "estrategia TEXT NOT NULL, resultadoPercibido TEXT NOT NULL, PRIMARY KEY(idRegistro), " +
                    "FOREIGN KEY(idRegistro) REFERENCES registro_maestro(idRegistro) " +
                    "ON UPDATE NO ACTION ON DELETE CASCADE)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS adiccion_detalle (" +
                    "idRegistro INTEGER NOT NULL, craving INTEGER NOT NULL, desencadenante TEXT NOT NULL, " +
                    "accionProtectora TEXT NOT NULL, estadoObjetivo TEXT NOT NULL, PRIMARY KEY(idRegistro), " +
                    "FOREIGN KEY(idRegistro) REFERENCES registro_maestro(idRegistro) " +
                    "ON UPDATE NO ACTION ON DELETE CASCADE)"
        )
    }
}