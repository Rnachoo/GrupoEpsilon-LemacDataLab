package com.example.lemacdatalab.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "dbt_detalle",
    foreignKeys = [
        ForeignKey(
            entity = RegistroMaestro::class,
            parentColumns = ["idRegistro"],
            childColumns = ["idRegistro"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DbtDetalle(
    @PrimaryKey
    val idRegistro: Int,
    val habilidad: String,
    val nivelImpulso: Int // 1..10
)