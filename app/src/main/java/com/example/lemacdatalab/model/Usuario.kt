package com.example.lemacdatalab.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**Por ahora solo funciona como usuario de prueba**/
@Entity(
    tableName = "usuario",
    indices = [Index(value = ["identificadorSintetico"], unique = true)]
)
data class Usuario(
    @PrimaryKey
    val idUsuario: Int,
    val identificadorSintetico: String,
    val fechaCreacion: String
)
