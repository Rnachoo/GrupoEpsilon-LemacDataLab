package com.example.lemacdatalab.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.lemacdatalab.data.dao.AdminDao
import com.example.lemacdatalab.data.dao.ContenidoDao
import com.example.lemacdatalab.data.dao.RegistroDetalleDao
import com.example.lemacdatalab.data.dao.RegistroMaestroDao
import com.example.lemacdatalab.data.dao.UsuarioDao
import com.example.lemacdatalab.model.AdiccionDetalle
import com.example.lemacdatalab.model.ContenidoEducativo
import com.example.lemacdatalab.model.DbtDetalle
import com.example.lemacdatalab.model.MoodDetalle
import com.example.lemacdatalab.model.NeuroDetalle
import com.example.lemacdatalab.model.RegistroMaestro
import com.example.lemacdatalab.model.RegistroVersion
import com.example.lemacdatalab.model.Usuario


@Database(
    entities = [
        Usuario::class,
        RegistroMaestro::class,
        MoodDetalle::class,
        DbtDetalle::class,
        NeuroDetalle::class,
        AdiccionDetalle::class,
        RegistroVersion::class,
        ContenidoEducativo::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun registroMaestroDao(): RegistroMaestroDao
    abstract fun registroDetalleDao(): RegistroDetalleDao
    abstract fun contenidoDao(): ContenidoDao

    abstract fun adminDao(): AdminDao

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