package com.example.lemacdatalab.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Clave natural: un registro por usuario, día y ámbito
@Entity(
    tableName = "registro_maestro",
    indices = [Index(value = ["idUsuario", "fechaDia", "tipoArea"], unique = true)]
)
data class RegistroMaestro(
    @PrimaryKey(autoGenerate = true)
    val idRegistro: Int,
    val idUsuario: Int,
    val tipoArea: String, // "neurodesarrollo" | "dbt" | "animo" | "adicciones"
    val fechaHoraCreacion: String,
    val notasOpcionales: String? = null,
    val estadoSync: String = "pendiente",
    val estadoRegistro: String = "confirmado",
    // Fecha local del dispositivo al crear (yyyy-MM-dd). No se recalcula al editar.
    val fechaDia: String = fechaHoraCreacion.take(10),
    // Epoch en milisegundos UTC de la última modificación (decide los conflictos)
    val modificadoEnUtc: Long = 0L
)