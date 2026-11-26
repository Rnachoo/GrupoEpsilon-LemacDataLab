package com.example.lemacdatalab.data.remote

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ResolutorConflictosTest {

    @get:Rule
    val carpetaTemp = TemporaryFolder()

    private fun registroDbt(
        idRegistro: Int = 1,
        notas: String? = "Nota",
        estado: String = "confirmado",
        modificadoEnUtc: Long = 1_000L,
        intensidad: Int = 2,
        habilidad: String = "Mindfulness"
    ) = RegistroDto(
        idRegistro = idRegistro,
        idUsuario = 1,
        tipoArea = "dbt",
        fechaHoraCreacion = "2026-10-30 10:00:00",
        fechaDia = "2026-10-30",
        notasOpcionales = notas,
        estadoRegistro = estado,
        modificadoEnUtc = modificadoEnUtc,
        habilidad = habilidad,
        intensidadEmocional = intensidad
    )

    @Test
    fun ganaLoMasReciente_cuandoRemotoTieneModificadoEnUtcMayor() {
        val local = registroDbt(idRegistro = 10, notas = "Local antiguo", modificadoEnUtc = 1_000L, intensidad = 1)
        val remoto = registroDbt(idRegistro = 99, notas = "Remoto nuevo", modificadoEnUtc = 2_000L, intensidad = 3)

        val resultado = resolverConflicto(local = local, remoto = remoto)

        assertTrue(resultado.ganoRemoto)
        assertEquals(10, resultado.ganador.idRegistro)
        assertEquals("Remoto nuevo", resultado.ganador.notasOpcionales)
        assertEquals(3, resultado.ganador.intensidadEmocional)
        assertEquals("editado", resultado.ganador.estadoRegistro)
        assertEquals("Local antiguo", resultado.versionDescartada.notasAnteriores)
    }

    @Test
    fun ganaLoMasReciente_cuandoLocalTieneModificadoEnUtcMayor() {
        val local = registroDbt(idRegistro = 10, notas = "Local nuevo", modificadoEnUtc = 3_000L, intensidad = 4)
        val remoto = registroDbt(idRegistro = 99, notas = "Remoto antiguo", modificadoEnUtc = 2_000L, intensidad = 1)

        val resultado = resolverConflicto(local = local, remoto = remoto)

        assertFalse(resultado.ganoRemoto)
        assertEquals(10, resultado.ganador.idRegistro)
        assertEquals("Local nuevo", resultado.ganador.notasOpcionales)
        assertEquals(4, resultado.ganador.intensidadEmocional)
        assertEquals("Remoto antiguo", resultado.versionDescartada.notasAnteriores)
    }

    @Test
    fun empateDeModificadoEnUtc_desempataDeFormaDeterministaYSinImportarOrden() {
        val a = registroDbt(idRegistro = 1, notas = "Alpha", modificadoEnUtc = 5_000L, intensidad = 1)
        val b = registroDbt(idRegistro = 2, notas = "Beta", modificadoEnUtc = 5_000L, intensidad = 3)

        val ab = resolverConflicto(local = a, remoto = b)
        val ba = resolverConflicto(local = b, remoto = a)

        assertEquals(ab.ganador.notasOpcionales, ba.ganador.notasOpcionales)
        assertEquals(ab.ganador.intensidadEmocional, ba.ganador.intensidadEmocional)
        assertEquals("Beta", ab.ganador.notasOpcionales)
    }

    @Test
    fun borradoMasRecienteQueEdicion_ganaBorradoYConservaEdicionEnVersion() {
        val edicionLocal = registroDbt(notas = "Editado antes", estado = "editado", modificadoEnUtc = 2_000L, intensidad = 2)
        val borradoRemoto = registroDbt(notas = "Editado antes", estado = "eliminado", modificadoEnUtc = 4_000L, intensidad = 2)

        val resultado = resolverConflicto(local = edicionLocal, remoto = borradoRemoto)

        assertTrue(resultado.ganoRemoto)
        assertEquals("eliminado", resultado.ganador.estadoRegistro)
        assertEquals("editado", resultado.versionDescartada.estadoAnterior)
        assertEquals("Editado antes", resultado.versionDescartada.notasAnteriores)
    }

    @Test
    fun edicionMasRecienteQueBorrado_ganaEdicionYConservaBorradoEnVersion() {
        val borradoLocal = registroDbt(notas = "Borrado antes", estado = "eliminado", modificadoEnUtc = 2_000L, intensidad = 2)
        val edicionRemota = registroDbt(notas = "Editado después", estado = "editado", modificadoEnUtc = 4_000L, intensidad = 4)

        val resultado = resolverConflicto(local = borradoLocal, remoto = edicionRemota)

        assertTrue(resultado.ganoRemoto)
        assertEquals("editado", resultado.ganador.estadoRegistro)
        assertEquals("Editado después", resultado.ganador.notasOpcionales)
        assertEquals("eliminado", resultado.versionDescartada.estadoAnterior)
    }

    @Test
    fun dosDispositivosSimuladosSinConexion_editanMismoRegistro_quedaMasRecienteYOtraEnHistorial() = runBlocking {
        val archivoServidor = carpetaTemp.newFile("servidor_dos_dispositivos.json")
        archivoServidor.delete()
        val almacen = AlmacenBackendSimulado(archivoPersistencia = archivoServidor)
        val api = RetrofitClient.crearApiService(BackendSimuladoInterceptor(almacen = almacen))

        // Ambos dispositivos partieron del mismo registro inicial (t = 1000)
        val base = RegistroDto(
            idUsuario = 1,
            tipoArea = "animo",
            fechaHoraCreacion = "2026-10-31 09:00:00",
            fechaDia = "2026-10-31",
            notasOpcionales = "Base inicial",
            estadoRegistro = "confirmado",
            modificadoEnUtc = 1_000L,
            nivelIntensidad = 1,
            contexto = "Ámbito Laboral",
            depresion = 1,
            elevacion = 0,
            horasSueno = 7,
            altoRiesgo = false,
            sintomasPsicoticos = false
        )
        val creado = api.crearRegistro(base)

        // Sin conexión: Dispositivo 2 edita a las t = 3500, Dispositivo 1 edita a las t = 2000
        val edicionDispositivo2 = creado.copy(
            notasOpcionales = "Edición Dispositivo 2 (más reciente)",
            modificadoEnUtc = 3_500L,
            depresion = 3,
            elevacion = 1,
            nivelIntensidad = 3
        )
        val edicionDispositivo1 = creado.copy(
            notasOpcionales = "Edición Dispositivo 1 (más antigua)",
            modificadoEnUtc = 2_000L,
            depresion = 2,
            elevacion = 0,
            nivelIntensidad = 2
        )

        // Vuelve la conexión: primero sube Dispositivo 2 y luego sube Dispositivo 1
        val resp2 = api.crearRegistro(edicionDispositivo2)
        val resp1 = api.crearRegistro(edicionDispositivo1)

        assertEquals("Edición Dispositivo 2 (más reciente)", resp2.notasOpcionales)
        assertEquals("Edición Dispositivo 2 (más reciente)", resp1.notasOpcionales)
        assertEquals(3_500L, resp1.modificadoEnUtc)

        // Cuando Dispositivo 1 descarga cambios desde t = 1000 y aplica resolverConflicto localmente
        val cambios = api.obtenerCambios(desdeUtc = 1_000L, idUsuario = 1)
        val remoto = cambios.first { it.fechaDia == "2026-10-31" && it.tipoArea == "animo" }
        val resolucionLocalDisp1 = resolverConflicto(local = edicionDispositivo1, remoto = remoto)

        assertTrue(resolucionLocalDisp1.ganoRemoto)
        assertEquals("Edición Dispositivo 2 (más reciente)", resolucionLocalDisp1.ganador.notasOpcionales)
        assertEquals("Edición Dispositivo 1 (más antigua)", resolucionLocalDisp1.versionDescartada.notasAnteriores)

        // En el historial de versiones descartadas del servidor queda la edición de Dispositivo 1
        val versionesServidor = almacen.obtenerVersionesDescartadas(creado.idRegistro)
        assertTrue(versionesServidor.any { it.notasAnteriores == "Edición Dispositivo 1 (más antigua)" })
    }
}
