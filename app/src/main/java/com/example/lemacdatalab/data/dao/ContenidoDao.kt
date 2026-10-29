package com.example.lemacdatalab.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.lemacdatalab.model.ContenidoEducativo
import kotlinx.coroutines.flow.Flow

@Dao
interface ContenidoDao {
    // obtiene toda la lista ordenada, dejando primero la "Lectura del Día" y luego las más recientes
    @Query("SELECT * FROM contenidos_educativos ORDER BY esLecturaDelDia DESC, fechaCreacion DESC")
    fun obtenerTodos(): Flow<List<ContenidoEducativo>>

    // obtiene solo la capsula marcada para mostrar en la pantalla principal del usuario
    @Query("SELECT * FROM contenidos_educativos WHERE esLecturaDelDia = 1 LIMIT 1")
    fun obtenerLecturaDelDia(): Flow<ContenidoEducativo?>

    // Upsert = Si no existe lo inserta (Insert), si ya existe lo actualiza (Update)
    @Upsert
    suspend fun guardarContenido(contenido: ContenidoEducativo)

    @Delete
    suspend fun eliminarContenido(contenido: ContenidoEducativo)
}