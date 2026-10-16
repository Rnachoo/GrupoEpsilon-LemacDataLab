package com.example.lemacdatalab.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
    @Entity(
    tableName = "mood_detalle",
    foreignKeys = [
        ForeignKey(
            entity = RegistroMaestro::class,
            parentColumns = ["idRegistro"],
            childColumns = ["idRegistro"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MoodDetalle(
    @PrimaryKey
    val idRegistro: Int,
    val nivelIntensidad: Int, // 1..10
    val contexto: String
)