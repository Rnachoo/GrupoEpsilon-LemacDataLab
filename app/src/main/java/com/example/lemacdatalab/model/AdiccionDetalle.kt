package com.example.lemacdatalab.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "adiccion_detalle",
    foreignKeys = [
        ForeignKey(
            entity = RegistroMaestro::class,
            parentColumns = ["idRegistro"],
            childColumns = ["idRegistro"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AdiccionDetalle(
    @PrimaryKey
    val idRegistro: Int,
    val craving: Int,             // 0..4
    val desencadenante: String,
    val accionProtectora: String,
    val estadoObjetivo: String    // cumplido | parcialmente cumplido | no cumplido
)