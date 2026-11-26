package com.example.lemacdatalab.data.repository

import com.example.lemacdatalab.data.dao.RegistroDetalleDao
import com.example.lemacdatalab.data.remote.ApiService
import com.example.lemacdatalab.model.DatosAnimo
import com.example.lemacdatalab.model.DatosNeuro
import com.example.lemacdatalab.model.RegistroHistorial
import com.example.lemacdatalab.model.RegistroMaestro
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ResumenSincronizacion(
    val enviados: Int = 0,
    val recibidos: Int = 0,
    val conflictosResueltos: Int = 0,
    val pendientesRestantes: Int = 0,
    val nuevaMarcaUtc: Long = 0L
)

sealed interface ResultadoSync {
    data class Exito(val resumen: ResumenSincronizacion) : ResultadoSync
    data class SinConexion(val pendientes: Int, val mensaje: String) : ResultadoSync
}

interface RegistroRepository {
    // Create
    suspend fun guardarMood(idUsuario: Int, datos: DatosAnimo, notas: String?)
    suspend fun guardarDbt(idUsuario: Int, habilidad: String, intensidadEmocional: Int, notas: String?)
    suspend fun guardarNeuro(idUsuario: Int, datos: DatosNeuro, notas: String?)

    // Read
    fun observarHistorial(idUsuario: Int): Flow<List<RegistroHistorial>>
    suspend fun obtenerRegistro(idUsuario: Int, idRegistro: Int): RegistroHistorial?

    // Update
    suspend fun actualizarMood(idRegistro: Int, datos: DatosAnimo, notas: String?)
    suspend fun actualizarDbt(idRegistro: Int, habilidad: String, intensidadEmocional: Int, notas: String?)
    suspend fun actualizarNeuro(idRegistro: Int, datos: DatosNeuro, notas: String?)

    suspend fun eliminar(idUsuario: Int, idRegistro: Int)

    suspend fun eliminarTodos(idUsuario: Int)

    // Sincronización
    fun observarCantidadPendientes(idUsuario: Int): Flow<Int>
    suspend fun sincronizarPendientes(idUsuario: Int): Int
    suspend fun sincronizar(idUsuario: Int, desdeUtc: Long = 0L): ResultadoSync
}

class RoomRegistroRepository(
    private val dao: RegistroDetalleDao,
    private val apiService: ApiService? = null
) : RegistroRepository {

    override suspend fun guardarMood(idUsuario: Int, datos: DatosAnimo, notas: String?) {
        dao.guardarMood(nuevoRegistro(idUsuario, "animo", notas), datos)
        if (apiService != null) sincronizarPendientes(idUsuario)
    }

    override suspend fun guardarDbt(idUsuario: Int, habilidad: String, intensidadEmocional: Int, notas: String?) {
        dao.guardarDbt(nuevoRegistro(idUsuario, "dbt", notas), habilidad, intensidadEmocional)
        if (apiService != null) sincronizarPendientes(idUsuario)
    }

    override suspend fun guardarNeuro(idUsuario: Int, datos: DatosNeuro, notas: String?) {
        dao.guardarNeuro(nuevoRegistro(idUsuario, "neurodesarrollo", notas), datos)
        if (apiService != null) sincronizarPendientes(idUsuario)
    }

    override fun observarHistorial(idUsuario: Int): Flow<List<RegistroHistorial>> =
        dao.observarHistorial(idUsuario)

    override suspend fun obtenerRegistro(idUsuario: Int, idRegistro: Int): RegistroHistorial? =
        dao.obtenerRegistro(idUsuario, idRegistro)

    override suspend fun actualizarMood(idRegistro: Int, datos: DatosAnimo, notas: String?) {
        dao.actualizarMood(idRegistro, datos, notas)
        if (apiService != null) {
            dao.obtenerMaestro(idRegistro)?.let { sincronizarPendientes(it.idUsuario) }
        }
    }

    override suspend fun actualizarDbt(idRegistro: Int, habilidad: String, intensidadEmocional: Int, notas: String?) {
        dao.actualizarDbt(idRegistro, habilidad, intensidadEmocional, notas)
        if (apiService != null) {
            dao.obtenerMaestro(idRegistro)?.let { sincronizarPendientes(it.idUsuario) }
        }
    }

    override suspend fun actualizarNeuro(idRegistro: Int, datos: DatosNeuro, notas: String?) {
        dao.actualizarNeuro(idRegistro, datos, notas)
        if (apiService != null) {
            dao.obtenerMaestro(idRegistro)?.let { sincronizarPendientes(it.idUsuario) }
        }
    }

    override suspend fun eliminar(idUsuario: Int, idRegistro: Int) {
        dao.eliminarRegistro(idUsuario, idRegistro)
        if (apiService != null) sincronizarPendientes(idUsuario)
    }

    override suspend fun eliminarTodos(idUsuario: Int) {
        dao.eliminarTodos(idUsuario)
        if (apiService != null) sincronizarPendientes(idUsuario)
    }

    override fun observarCantidadPendientes(idUsuario: Int): Flow<Int> =
        dao.observarCantidadPendientes(idUsuario)

    override suspend fun sincronizarPendientes(idUsuario: Int): Int {
        val api = apiService ?: return 0
        val pendientes = dao.obtenerPendientes(idUsuario)
        var enviados = 0

        for (maestro in pendientes) {
            val dto = dao.construirDto(maestro)
            try {
                val respuesta = api.crearRegistro(dto)
                val respuestaDifiere = respuesta.modificadoEnUtc != dto.modificadoEnUtc ||
                        respuesta.estadoRegistro != dto.estadoRegistro ||
                        respuesta.notasOpcionales != dto.notasOpcionales ||
                        respuesta.describirDetalle() != dto.describirDetalle()

                if (respuestaDifiere) {
                    dao.aplicarCambioRemoto(respuesta)
                } else {
                    dao.actualizarEstadoSync(maestro.idRegistro, "sincronizado")
                }
                enviados++
            } catch (_: Exception) {
                // Si no hay conexión o falla la API, el registro permanece en 'pendiente'
            }
        }
        return enviados
    }

    override suspend fun sincronizar(idUsuario: Int, desdeUtc: Long): ResultadoSync {
        val api = apiService
            ?: return ResultadoSync.SinConexion(
                pendientes = dao.obtenerPendientes(idUsuario).size,
                mensaje = "API no configurada"
            )

        return try {
            var enviados = 0
            val pendientes = dao.obtenerPendientes(idUsuario)
            for (maestro in pendientes) {
                val dto = dao.construirDto(maestro)
                val respuesta = api.crearRegistro(dto)
                val respuestaDifiere = respuesta.modificadoEnUtc != dto.modificadoEnUtc ||
                        respuesta.estadoRegistro != dto.estadoRegistro ||
                        respuesta.notasOpcionales != dto.notasOpcionales ||
                        respuesta.describirDetalle() != dto.describirDetalle()
                if (respuestaDifiere) {
                    dao.aplicarCambioRemoto(respuesta)
                } else {
                    dao.actualizarEstadoSync(maestro.idRegistro, "sincronizado")
                }
                enviados++
            }

            val cambios = api.obtenerCambios(desdeUtc = desdeUtc, idUsuario = idUsuario)
            var conflictosResueltos = 0
            for (remoto in cambios) {
                if (dao.aplicarCambioRemoto(remoto) != null) {
                    conflictosResueltos++
                }
            }

            val maxUtc = (cambios.map { it.modificadoEnUtc } + desdeUtc).maxOrNull() ?: desdeUtc
            val restantes = dao.obtenerPendientes(idUsuario).size
            ResultadoSync.Exito(
                ResumenSincronizacion(
                    enviados = enviados,
                    recibidos = cambios.size,
                    conflictosResueltos = conflictosResueltos,
                    pendientesRestantes = restantes,
                    nuevaMarcaUtc = maxUtc
                )
            )
        } catch (e: Exception) {
            ResultadoSync.SinConexion(
                pendientes = dao.obtenerPendientes(idUsuario).size,
                mensaje = e.message ?: "Sin conexión"
            )
        }
    }

    private fun nuevoRegistro(idUsuario: Int, tipoArea: String, notas: String?): RegistroMaestro {
        val ahora = Date()
        return RegistroMaestro(
            idRegistro = 0,
            idUsuario = idUsuario,
            tipoArea = tipoArea,
            fechaHoraCreacion = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(ahora),
            notasOpcionales = notas,
            fechaDia = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(ahora),
            modificadoEnUtc = ahora.time
        )
    }
}
