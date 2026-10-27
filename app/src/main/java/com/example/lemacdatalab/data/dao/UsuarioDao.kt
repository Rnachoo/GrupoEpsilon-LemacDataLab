package com.example.lemacdatalab.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.lemacdatalab.model.Usuario

@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(usuario: Usuario)

    @Query("SELECT * FROM usuario WHERE identificadorSintetico = :identificador LIMIT 1")
    suspend fun buscarPorIdentificador(identificador: String): Usuario?
}
