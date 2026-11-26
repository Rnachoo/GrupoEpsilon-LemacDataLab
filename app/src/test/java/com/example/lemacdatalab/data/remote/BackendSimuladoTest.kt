package com.example.lemacdatalab.data.remote

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class BackendSimuladoTest {

    @get:Rule
    val carpetaTemp = TemporaryFolder()

    private fun crearApiConArchivo(archivo: File = carpetaTemp.newFile("estado_test.json")): Pair<ApiService, AlmacenBackendSimulado> {
        archivo.delete() // para que cargue desde el dataset semilla y persista en este archivo
        val assetFile = File("src/main/assets/dataset_sintetico.json").takeIf { it.exists() }
            ?: File("app/src/main/assets/dataset_sintetico.json").takeIf { it.exists() }
        val jsonSemilla = assetFile?.readText(Charsets.UTF_8)
        val almacen = AlmacenBackendSimulado(archivoPersistencia = archivo, jsonSemilla = jsonSemilla)
        val interceptor = BackendSimuladoInterceptor(almacen = almacen)
        return RetrofitClient.crearApiService(interceptor) to almacen
    }

    @Test
    fun loginConUsuarioSinteticoValido_devuelveTokenYPerfil() = runBlocking {
        val (api, _) = crearApiConArchivo()

        val respuesta = api.login(LoginRequestDto("AX-7B99"))

        assertTrue(respuesta.token.startsWith("tok-lemac-AX-7B99-"))
        assertEquals(1, respuesta.idUsuario)
        assertEquals("USUARIO", respuesta.rol)
        assertNotNull(respuesta.objetivoAdicciones)
    }

    @Test
    fun consentimientoCatalogosYPerfil_respondenConDatosDelDatasetSintetico() = runBlocking {
        val (api, _) = crearApiConArchivo()

        val inicial = api.obtenerConsentimiento("BZ-4M12")
        assertFalse(inicial.aceptado)

        val aceptado = api.aceptarConsentimiento(
            AceptacionConsentimientoRequestDto(
                identificadorSintetico = "BZ-4M12",
                version = "1.0",
                fechaAceptacionUtc = 1762000000000L
            )
        )
        assertTrue(aceptado.aceptado)
        assertEquals(1762000000000L, aceptado.fechaAceptacionUtc)

        val catalogos = api.obtenerCatalogos()
        assertTrue(catalogos.estrategiasNeuro.contains("Otro (especificar)"))
        assertTrue(catalogos.desencadenantesAdiccion.contains("Otro (especificar)"))

        val perfil = api.obtenerPerfil(idUsuario = 1)
        assertEquals("AX-7B99", perfil.identificadorSintetico)
        assertNotNull(perfil.objetivoAdicciones)
    }

    @Test
    fun crudRegistros_aplicaUltimaModificacionGana_yConservaVersionDescartada() = runBlocking {
        val archivo = carpetaTemp.newFile("persistencia.json")
        val (api, almacen) = crearApiConArchivo(archivo)

        val inicial = api.crearRegistro(
            RegistroDto(
                idUsuario = 1,
                tipoArea = "adicciones",
                fechaHoraCreacion = "2026-10-30 10:00:00",
                fechaDia = "2026-10-30",
                notasOpcionales = "Primera versión",
                modificadoEnUtc = 2_000L,
                craving = 2,
                desencadenante = "Estrés o sobrecarga",
                accionProtectora = "Técnica de respiración",
                estadoObjetivo = "Cumplido"
            )
        )

        // Edición más reciente (3000 > 2000): gana la nueva y guarda la de 2000 en versionesDescartadas
        val masReciente = api.actualizarRegistro(
            idRegistro = inicial.idRegistro,
            registro = inicial.copy(
                notasOpcionales = "Versión más reciente",
                modificadoEnUtc = 3_000L,
                craving = 3
            )
        )
        assertEquals("Versión más reciente", masReciente.notasOpcionales)
        assertEquals("editado", masReciente.estadoRegistro)
        assertEquals(3, masReciente.craving)

        // Llega una modificación atrasada (1500 < 3000): gana la de 3000 y la de 1500 se conserva como descartada
        val trasIntentoAntiguo = api.crearRegistro(
            inicial.copy(
                notasOpcionales = "Edición antigua sin conexión",
                modificadoEnUtc = 1_500L,
                craving = 1
            )
        )
        assertEquals("Versión más reciente", trasIntentoAntiguo.notasOpcionales)
        assertEquals(3, trasIntentoAntiguo.craving)

        val descartadas = almacen.obtenerVersionesDescartadas(inicial.idRegistro)
        assertEquals(2, descartadas.size)
        assertTrue(descartadas.any { it.notasAnteriores == "Primera versión" })
        assertTrue(descartadas.any { it.notasAnteriores == "Edición antigua sin conexión" })

        // Verificar que cambios?desde= devuelve los registros modificados después de la marca UTC
        val cambios = api.obtenerCambios(desdeUtc = 2_500L, idUsuario = 1)
        assertTrue(cambios.any { it.idRegistro == inicial.idRegistro && it.modificadoEnUtc == 3_000L })

        // Verificar que el almacenamiento propio persiste en disco al recrear el almacén
        val almacenRecargado = AlmacenBackendSimulado(archivoPersistencia = archivo)
        val persistido = almacenRecargado.obtenerEstado().registros.first { it.idRegistro == inicial.idRegistro }
        assertEquals("Versión más reciente", persistido.notasOpcionales)
    }

    @Test
    fun mapeoDtoAModelo_conservaCamposYEscala0a4() {
        val dto = RegistroDto(
            idRegistro = 55,
            idUsuario = 1,
            tipoArea = "animo",
            fechaHoraCreacion = "2026-10-30 08:00:00",
            fechaDia = "2026-10-30",
            notasOpcionales = "Dormí bien",
            estadoRegistro = "confirmado",
            modificadoEnUtc = 5_000L,
            nivelIntensidad = 3,
            contexto = "Salud Física",
            depresion = 0,
            elevacion = 3,
            horasSueno = 8,
            altoRiesgo = false,
            sintomasPsicoticos = false
        )

        assertTrue(dto.tieneEscalaValida())
        val maestro = dto.aRegistroMaestro()
        val detalle = dto.aMoodDetalle()
        val historial = dto.aRegistroHistorial()

        assertEquals(55, maestro.idRegistro)
        assertEquals("2026-10-30", maestro.fechaDia)
        assertEquals(5_000L, maestro.modificadoEnUtc)
        assertNotNull(detalle)
        assertEquals(0, detalle?.depresion)
        assertEquals(3, detalle?.elevacion)
        assertEquals(8, detalle?.horasSueno)
        assertEquals(3, historial.nivelIntensidad)
        assertEquals(0, historial.depresion)
        assertEquals(3, historial.elevacion)
    }
}
