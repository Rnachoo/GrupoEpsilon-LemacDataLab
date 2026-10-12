package com.example.lemacdatalab.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.lemacdatalab.model.RegistroMaestro
import com.example.lemacdatalab.model.Usuario

@Database(
    entities = [Usuario::class, RegistroMaestro::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun registroMaestroDao(): RegistroMaestroDao

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        // Una sola instancia de la base de datos para toda la app
        fun getDatabase(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lemac_database"
                ).build().also { instancia = it }
            }
    }
}
