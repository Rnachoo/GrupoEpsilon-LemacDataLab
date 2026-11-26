package com.example.lemacdatalab.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class CartaAnimoTest {

    private val hoy: Calendar = Calendar.getInstance().apply {
        set(2026, Calendar.OCTOBER, 30, 12, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }

    private fun registroBase(
        id: Int,
        tipoArea: String,
        fechaHora: String,
        nivelIntensidad: Int? = null,
        depresion: Int? = null,
        elevacion: Int? = null,
        nivelImpulso: Int? = null,
        intensidadNeuro: Int? = null,
        craving: Int? = null
    ) = RegistroHistorial(
        idRegistro = id,
        tipoArea = tipoArea,
        fechaHoraCreacion = fechaHora,
        notasOpcionales = null,
        nivelIntensidad = nivelIntensidad,
        contexto = null,
        habilidad = null,
        nivelImpulso = nivelImpulso,
        depresion = depresion,
        elevacion = elevacion,
        intensidadNeuro = intensidadNeuro,
        craving = craving
    )

    @Test
    fun diaSinRegistro_noGeneraPuntoNiCero_ySeMarcaComoSinRegistro() {
        val registros = listOf(
            registroBase(id = 1, tipoArea = "dbt", fechaHora = "2026-10-25 10:00:00", nivelImpulso = 3),
            registroBase(id = 2, tipoArea = "dbt", fechaHora = "2026-10-30 18:00:00", nivelImpulso = 1)
        )

        val serie = calcularSerieAmbito(registros, tipoArea = "dbt", dias = DIAS_CARTA_CORTO, hoy = hoy)

        assertEquals(2, serie.puntos.size)
        assertEquals(2, serie.diasConRegistro)
        assertEquals(5, serie.diasSinRegistro)
        assertEquals(listOf("2026-10-25", "2026-10-30"), serie.puntos.map { it.fecha })
        assertFalse(serie.puntos.any { it.promedio == 0f })

        val diaVacio = serie.calendario.first { it.fecha == "2026-10-27" }
        assertNull(diaVacio.punto)
        assertFalse(diaVacio.tieneRegistro)
        assertEquals("Sin registro", diaVacio.etiquetaEstado)
        assertEquals("Sin registro", etiquetaDiaEvolucion(diaVacio.punto))
        assertNotEquals("Sin síntomas", diaVacio.etiquetaEstado)
    }

    @Test
    fun diaConValorCeroRegistrado_siGeneraPuntoSinSintomas_distintoDeHueco() {
        val registros = listOf(
            registroBase(id = 1, tipoArea = "adicciones", fechaHora = "2026-10-30 12:00:00", craving = 0)
        )

        val serie = calcularSerieAmbito(registros, tipoArea = "adicciones", dias = DIAS_CARTA_CORTO, hoy = hoy)

        assertEquals(1, serie.puntos.size)
        val diaRegistrado = serie.calendario.first { it.fecha == "2026-10-30" }
        assertEquals(0f, diaRegistrado.punto?.promedio)
        assertTrue(diaRegistrado.tieneRegistro)
        assertEquals("Sin síntomas", diaRegistrado.etiquetaEstado)

        val diaHueco = serie.calendario.first { it.fecha == "2026-10-29" }
        assertNull(diaHueco.punto)
        assertEquals("Sin registro", diaHueco.etiquetaEstado)
    }

    @Test
    fun animoBipolar_separaElevacionPositivaYDepresionNegativaEntreMenos4YMas4() {
        val registros = listOf(
            registroBase(
                id = 1,
                tipoArea = "animo",
                fechaHora = "2026-10-30 09:00:00",
                nivelIntensidad = 3,
                depresion = 3,
                elevacion = 2
            )
        )

        val serie = calcularSerieAmbito(registros, tipoArea = "animo", dias = DIAS_CARTA_CORTO, hoy = hoy)

        assertEquals(1, serie.puntos.size)
        val punto = serie.puntos.first()
        assertEquals(2f, punto.elevacion, 0.001f)
        assertEquals(-3f, punto.depresionConSigno, 0.001f)
        assertEquals(3, punto.depresionMagnitud)
        assertEquals(2, punto.elevacionMagnitud)
    }

    @Test
    fun seriesPorAmbito_calculaLos4AmbitosEn7Y30Dias() {
        val registros = listOf(
            registroBase(id = 1, tipoArea = "animo", fechaHora = "2026-10-30 09:00:00", depresion = 1, elevacion = 4),
            registroBase(id = 2, tipoArea = "dbt", fechaHora = "2026-10-29 15:00:00", nivelImpulso = 2),
            registroBase(id = 3, tipoArea = "neurodesarrollo", fechaHora = "2026-10-28 11:00:00", intensidadNeuro = 3),
            registroBase(id = 4, tipoArea = "adicciones", fechaHora = "2026-10-10 20:00:00", craving = 4)
        )

        val series7 = calcularSeriesPorAmbito(registros, dias = DIAS_CARTA_CORTO, hoy = hoy)
        assertEquals(4, series7.size)
        assertEquals(1, series7["animo"]?.puntos?.size)
        assertEquals(4f, series7["animo"]?.puntos?.first()?.elevacion ?: -1f, 0.001f)
        assertEquals(1, series7["dbt"]?.puntos?.size)
        assertEquals(1, series7["neurodesarrollo"]?.puntos?.size)
        assertEquals(0, series7["adicciones"]?.puntos?.size) // 10 de oct queda fuera de últimos 7 días

        val series30 = calcularSeriesPorAmbito(registros, dias = DIAS_CARTA_ANIMO, hoy = hoy)
        assertEquals(1, series30["adicciones"]?.puntos?.size)
        assertEquals(4f, series30["adicciones"]?.puntos?.first()?.promedio ?: -1f, 0.001f)
    }
}
