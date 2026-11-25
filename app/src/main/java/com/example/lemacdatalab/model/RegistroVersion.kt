package com.example.lemacdatalab.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Versión anterior de un registro, guardada cada vez que se corrige
@Entity(
    tableName = "registro_version",
    foreignKeys = [
        ForeignKey(
            entity = RegistroMaestro::class,
            parentColumns = ["idRegistro"],
            childColumns = ["idRegistro"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("idRegistro")]
)
data class RegistroVersion(
    @PrimaryKey(autoGenerate = true)
    val idVersion: Int = 0,
    val idRegistro: Int,
    val fechaVersionUtc: Long,
    val estadoAnterior: String,
    val notasAnteriores: String?,
    // Valores anteriores del detalle como texto: "clave=valor;clave=valor"
    val detalleAnterior: String
)