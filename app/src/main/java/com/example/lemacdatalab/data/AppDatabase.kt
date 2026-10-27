package com.example.lemacdatalab.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.lemacdatalab.data.dao.RegistroDetalleDao
import com.example.lemacdatalab.data.dao.RegistroMaestroDao
import com.example.lemacdatalab.data.dao.UsuarioDao
import com.example.lemacdatalab.model.DbtDetalle
import com.example.lemacdatalab.model.MoodDetalle
import com.example.lemacdatalab.model.RegistroMaestro
import com.example.lemacdatalab.model.Usuario

@Database(
    entities = [Usuario::class, RegistroMaestro::class, MoodDetalle::class, DbtDetalle::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun registroMaestroDao(): RegistroMaestroDao
    abstract fun registroDetalleDao(): RegistroDetalleDao

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lemac_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build().also { instancia = it }
            }
    }
}