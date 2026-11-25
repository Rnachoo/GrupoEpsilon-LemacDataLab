package com.example.lemacdatalab.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "neuro_detalle",
    foreignKeys = [
        ForeignKey(
            entity = RegistroMaestro::class,
            parentColumns = ["idRegistro"],
            childColumns = ["idRegistro"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class NeuroDetalle(
    @PrimaryKey
    val idRegistro: Int,
    val tipoEntrada: String,
    val intensidad: Int,
    val estrategia: String,
    val resultadoPercibido: String
)