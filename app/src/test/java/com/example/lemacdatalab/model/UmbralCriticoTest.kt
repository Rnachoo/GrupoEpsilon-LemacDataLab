package com.example.lemacdatalab.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UmbralCriticoTest {

    // ---------- Craving ----------

    @Test
    fun craving3_noAlerta() {
        assertFalse(esCravingCritico(3))
    }

    @Test
    fun craving4_alerta() {
        assertTrue(esCravingCritico(4))
    }

    // ---------- Intensidad emocional (DBT) ----------

    @Test
    fun intensidadEmocional4_alerta() {
        assertTrue(esIntensidadEmocionalCritica(4))
    }

    @Test
    fun intensidadEmocional3_noAlerta() {
        assertFalse(esIntensidadEmocionalCritica(3))
    }

    // ---------- Ánimo ----------

    @Test
    fun animoConSueno3Horas_noAlerta() {
        assertFalse(esAnimoCritico(altoRiesgo = false, sintomasPsicoticos = false, horasSueno = 3))
    }

    @Test
    fun animoConMenosDe3Horas_alerta() {
        assertTrue(esAnimoCritico(altoRiesgo = false, sintomasPsicoticos = false, horasSueno = 2))
    }

    @Test
    fun animoSinDatoDeSueno_noAlerta() {
        assertFalse(esAnimoCritico(altoRiesgo = false, sintomasPsicoticos = false, horasSueno = null))
    }

    @Test
    fun animoConAltoRiesgo_alerta() {
        assertTrue(esAnimoCritico(altoRiesgo = true, sintomasPsicoticos = false, horasSueno = 8))
    }

    @Test
    fun animoConSintomasPsicoticos_alerta() {
        assertTrue(esAnimoCritico(altoRiesgo = false, sintomasPsicoticos = true, horasSueno = 8))
    }

    @Test
    fun animoSinNingunCriterio_noAlerta() {
        assertFalse(esAnimoCritico(altoRiesgo = false, sintomasPsicoticos = false, horasSueno = 7))
    }

    // ---------- Consolidación ----------

    @Test
    fun sinCriterios_noAlerta() {
        assertFalse(hayValorCritico(CriteriosCriticos()))
    }

    @Test
    fun cravingCritico_consolidaComoAlerta() {
        assertTrue(hayValorCritico(CriteriosCriticos(craving = 4)))
    }

    @Test
    fun valoresBajosEnTodosLosAmbitos_noAlertan() {
        val criterios = CriteriosCriticos(
            craving = 3,
            intensidadEmocional = 2,
            horasSueno = 6
        )
        assertFalse(hayValorCritico(criterios))
    }

    @Test
    fun unSoloAmbitoCritico_bastaParaAlertar() {
        val criterios = CriteriosCriticos(
            craving = 1,
            intensidadEmocional = 4,
            horasSueno = 8
        )
        assertTrue(hayValorCritico(criterios))
    }
}