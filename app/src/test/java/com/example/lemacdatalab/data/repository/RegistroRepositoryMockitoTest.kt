package com.example.lemacdatalab.data.repository

import com.example.lemacdatalab.data.dao.RegistroDetalleDao
import com.example.lemacdatalab.data.remote.ApiService
import com.example.lemacdatalab.data.remote.RegistroDto
import com.example.lemacdatalab.model.AdiccionDetalle
import com.example.lemacdatalab.model.DatosNeuro
import com.example.lemacdatalab.model.DbtDetalle
import com.example.lemacdatalab.model.MoodDetalle
import com.example.lemacdatalab.model.NeuroDetalle
import com.example.lemacdatalab.model.RegistroHistorial
import com.example.lemacdatalab.model.RegistroMaestro
import com.example.lemacdatalab.model.RegistroVersion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class RegistroRepositoryMockitoTest {

    @Test
    fun guardarRegistro_cuandoApiRespondeExito_quedaSincronizadoYLlamaApiUnaVez() = runTest {
        val dao = FakeRegistroDetalleDao()
        val apiService: ApiService = mock()
        whenever(apiService.crearRegistro(any())).thenAnswer { invocation ->
            invocation.getArgument<RegistroDto>(0)
        }
        val repository = RoomRegistroRepository(dao = dao, apiService = apiService)

        repository.guardarDbt(
            idUsuario = 1,
            habilidad = "Mindfulness",
            intensidadEmocional = 2,
            notas = "Prueba exitosa"
        )

        val maestro = dao.obtenerMaestro(1)
        assertEquals("sincronizado", maestro?.estadoSync)
        assertTrue(dao.obtenerPendientes(1).isEmpty())
        verify(apiService, times(1)).crearRegistro(any())
    }

    @Test
    fun guardarRegistro_cuandoApiLanzaExcepcion_quedaPendienteYLlamaApiUnaVez() = runTest {
        val dao = FakeRegistroDetalleDao()
        val apiService: ApiService = mock()
        whenever(apiService.crearRegistro(any())).thenAnswer {
            throw RuntimeException("Error simulado de red")
        }
        val repository = RoomRegistroRepository(dao = dao, apiService = apiService)

        repository.guardarDbt(
            idUsuario = 1,
            habilidad = "Aceptación Radical",
            intensidadEmocional = 3,
            notas = "Sin conexión"
        )

        val maestro = dao.obtenerMaestro(1)
        assertEquals("pendiente", maestro?.estadoSync)
        assertEquals(1, dao.obtenerPendientes(1).size)
        verify(apiService, times(1)).crearRegistro(any())
    }

    @Test
    fun sincronizarPendientes_conVariosRegistros_llamaApiExactamenteUnaVezPorRegistro() = runTest {
        val dao = FakeRegistroDetalleDao()
        val apiService: ApiService = mock()
        val repositoryOffline = RoomRegistroRepository(dao = dao, apiService = null)

        repositoryOffline.guardarDbt(
            idUsuario = 1,
            habilidad = "Mindfulness",
            intensidadEmocional = 1,
            notas = "Registro pendiente 1"
        )
        repositoryOffline.guardarNeuro(
            idUsuario = 1,
            datos = DatosNeuro(
                tipoEntrada = "Tarea",
                intensidad = 2,
                estrategia = "Pausa breve",
                resultadoPercibido = "Me ayudó"
            ),
            notas = "Registro pendiente 2"
        )
        assertEquals(2, dao.obtenerPendientes(1).size)

        whenever(apiService.crearRegistro(any())).thenAnswer { invocation ->
            invocation.getArgument<RegistroDto>(0)
        }
        val repositoryOnline = RoomRegistroRepository(dao = dao, apiService = apiService)

        val enviados = repositoryOnline.sincronizarPendientes(idUsuario = 1)

        assertEquals(2, enviados)
        assertTrue(dao.obtenerPendientes(1).isEmpty())
        verify(apiService, times(2)).crearRegistro(any())
    }
}

private class FakeRegistroDetalleDao : RegistroDetalleDao() {
    private var secuenciaId = 0
    private val maestros = MutableStateFlow<List<RegistroMaestro>>(emptyList())
    private val moods = mutableMapOf<Int, MoodDetalle>()
    private val dbts = mutableMapOf<Int, DbtDetalle>()
    private val neuros = mutableMapOf<Int, NeuroDetalle>()
    private val adicciones = mutableMapOf<Int, AdiccionDetalle>()
    private val versiones = mutableListOf<RegistroVersion>()

    override suspend fun insertarMaestro(registro: RegistroMaestro): Long {
        val id = if (registro.idRegistro > 0) registro.idRegistro else ++secuenciaId
        maestros.value = maestros.value.filterNot { it.idRegistro == id } + registro.copy(idRegistro = id)
        return id.toLong()
    }

    override suspend fun insertarMood(detalle: MoodDetalle) {
        moods[detalle.idRegistro] = detalle
    }

    override suspend fun insertarDbt(detalle: DbtDetalle) {
        dbts[detalle.idRegistro] = detalle
    }

    override suspend fun insertarNeuro(detalle: NeuroDetalle) {
        neuros[detalle.idRegistro] = detalle
    }

    override suspend fun insertarAdiccion(detalle: AdiccionDetalle) {
        adicciones[detalle.idRegistro] = detalle
    }

    override suspend fun insertarVersion(version: RegistroVersion) {
        versiones.add(version)
    }

    override suspend fun buscarPorClave(idUsuario: Int, fechaDia: String, tipoArea: String): RegistroMaestro? =
        maestros.value.firstOrNull {
            it.idUsuario == idUsuario && it.fechaDia == fechaDia && it.tipoArea == tipoArea
        }

    override suspend fun obtenerMaestro(idRegistro: Int): RegistroMaestro? =
        maestros.value.firstOrNull { it.idRegistro == idRegistro }

    override suspend fun obtenerMoodDetalle(idRegistro: Int): MoodDetalle? = moods[idRegistro]

    override suspend fun obtenerDbtDetalle(idRegistro: Int): DbtDetalle? = dbts[idRegistro]

    override suspend fun obtenerNeuroDetalle(idRegistro: Int): NeuroDetalle? = neuros[idRegistro]

    override suspend fun obtenerAdiccionDetalle(idRegistro: Int): AdiccionDetalle? = adicciones[idRegistro]

    override suspend fun contarVersiones(idRegistro: Int): Int =
        versiones.count { it.idRegistro == idRegistro }

    override suspend fun obtenerVersiones(idRegistro: Int): List<RegistroVersion> =
        versiones.filter { it.idRegistro == idRegistro }.sortedByDescending { it.fechaVersionUtc }

    override suspend fun actualizarMaestro(idRegistro: Int, estado: String, notas: String?, ahoraUtc: Long) {
        maestros.value = maestros.value.map {
            if (it.idRegistro == idRegistro) {
                it.copy(
                    estadoRegistro = estado,
                    notasOpcionales = notas,
                    estadoSync = "pendiente",
                    modificadoEnUtc = ahoraUtc
                )
            } else {
                it
            }
        }
    }

    override fun observarHistorial(idUsuario: Int): Flow<List<RegistroHistorial>> =
        maestros.map { lista ->
            lista.filter { it.idUsuario == idUsuario && it.estadoRegistro != "eliminado" }
                .map { m ->
                    val mood = moods[m.idRegistro]
                    val dbt = dbts[m.idRegistro]
                    val neuro = neuros[m.idRegistro]
                    val adiccion = adicciones[m.idRegistro]
                    RegistroHistorial(
                        idRegistro = m.idRegistro,
                        tipoArea = m.tipoArea,
                        fechaHoraCreacion = m.fechaHoraCreacion,
                        notasOpcionales = m.notasOpcionales,
                        nivelIntensidad = mood?.nivelIntensidad,
                        contexto = mood?.contexto,
                        habilidad = dbt?.habilidad,
                        nivelImpulso = dbt?.intensidadEmocional,
                        estadoRegistro = m.estadoRegistro,
                        depresion = mood?.depresion,
                        elevacion = mood?.elevacion,
                        horasSueno = mood?.horasSueno,
                        altoRiesgo = mood?.altoRiesgo,
                        sintomasPsicoticos = mood?.sintomasPsicoticos,
                        tipoEntrada = neuro?.tipoEntrada,
                        intensidadNeuro = neuro?.intensidad,
                        estrategia = neuro?.estrategia,
                        resultadoPercibido = neuro?.resultadoPercibido,
                        craving = adiccion?.craving,
                        desencadenante = adiccion?.desencadenante,
                        accionProtectora = adiccion?.accionProtectora,
                        estadoObjetivo = adiccion?.estadoObjetivo
                    )
                }
        }

    override suspend fun obtenerRegistro(idUsuario: Int, idRegistro: Int): RegistroHistorial? = null

    override suspend fun actualizarDetalleMood(
        idRegistro: Int,
        nivel: Int,
        contexto: String,
        depresion: Int,
        elevacion: Int,
        horasSueno: Int?,
        altoRiesgo: Boolean,
        sintomasPsicoticos: Boolean
    ) {
        moods[idRegistro] = MoodDetalle(
            idRegistro = idRegistro,
            nivelIntensidad = nivel,
            contexto = contexto,
            depresion = depresion,
            elevacion = elevacion,
            horasSueno = horasSueno,
            altoRiesgo = altoRiesgo,
            sintomasPsicoticos = sintomasPsicoticos
        )
    }

    override suspend fun actualizarDetalleDbt(idRegistro: Int, habilidad: String, intensidadEmocional: Int) {
        dbts[idRegistro] = DbtDetalle(
            idRegistro = idRegistro,
            habilidad = habilidad,
            intensidadEmocional = intensidadEmocional
        )
    }

    override suspend fun actualizarDetalleNeuro(
        idRegistro: Int,
        tipoEntrada: String,
        intensidad: Int,
        estrategia: String,
        resultadoPercibido: String
    ) {
        neuros[idRegistro] = NeuroDetalle(
            idRegistro = idRegistro,
            tipoEntrada = tipoEntrada,
            intensidad = intensidad,
            estrategia = estrategia,
            resultadoPercibido = resultadoPercibido
        )
    }

    override suspend fun marcarEliminado(idUsuario: Int, idRegistro: Int, ahoraUtc: Long) {
        maestros.value = maestros.value.map {
            if (it.idRegistro == idRegistro && it.idUsuario == idUsuario) {
                it.copy(estadoRegistro = "eliminado", estadoSync = "pendiente", modificadoEnUtc = ahoraUtc)
            } else {
                it
            }
        }
    }

    override suspend fun marcarTodosEliminados(idUsuario: Int, ahoraUtc: Long) {
        maestros.value = maestros.value.map {
            if (it.idUsuario == idUsuario) {
                it.copy(estadoRegistro = "eliminado", estadoSync = "pendiente", modificadoEnUtc = ahoraUtc)
            } else {
                it
            }
        }
    }

    override suspend fun obtenerPendientes(idUsuario: Int): List<RegistroMaestro> =
        maestros.value.filter { it.idUsuario == idUsuario && it.estadoSync == "pendiente" }
            .sortedWith(compareBy<RegistroMaestro> { it.modificadoEnUtc }.thenBy { it.idRegistro })

    override fun observarCantidadPendientes(idUsuario: Int): Flow<Int> =
        maestros.map { lista -> lista.count { it.idUsuario == idUsuario && it.estadoSync == "pendiente" } }

    override suspend fun actualizarEstadoSync(idRegistro: Int, estadoSync: String) {
        maestros.value = maestros.value.map {
            if (it.idRegistro == idRegistro) it.copy(estadoSync = estadoSync) else it
        }
    }

    override suspend fun actualizarMaestroDesdeSync(
        idRegistro: Int,
        estadoRegistro: String,
        notas: String?,
        estadoSync: String,
        modificadoEnUtc: Long
    ) {
        maestros.value = maestros.value.map {
            if (it.idRegistro == idRegistro) {
                it.copy(
                    estadoRegistro = estadoRegistro,
                    notasOpcionales = notas,
                    estadoSync = estadoSync,
                    modificadoEnUtc = modificadoEnUtc
                )
            } else {
                it
            }
        }
    }
}
