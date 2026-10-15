package com.example.lemacdatalab.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "registro_maestro")
data class RegistroMaestro(
    @PrimaryKey(autoGenerate = true)
    val idRegistro: Int,
    val idUsuario: Int,
    val tipoArea: String, // "neurodesarrollo" | "dbt" | "animo" | "adicciones"
    val fechaHoraCreacion: String,
    val notasOpcionales: String? = null,
    val estadoSync: String = "pendiente",
    val estadoRegistro: String = "confirmado"
)
