package com.example.lemacdatalab.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "contenidos_educativos")
data class ContenidoEducativo(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val titulo: String,
    val extracto: String,
    val fuente: String,
    val ambito: String, // EJ:   "DBT", "Estado de animo", "Neurodesarrollo", "Adicciones"
    val esLecturaDelDia: Boolean = false,
    val fechaCreacion: Long = System.currentTimeMillis()
)