package com.example.lemacdatalab.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EscalaIntensidadTest {

    @Test
    fun escala_0y4_sonValidos() {
        assertTrue(esEscalaValida(0))
        assertTrue(esEscalaValida(4))
    }

    @Test
    fun escala_menos1y5_sonInvalidos() {
        assertFalse(esEscalaValida(-1))
        assertFalse(esEscalaValida(5))
    }

    @Test
    fun etiqueta_correspondeAlNumero_yNullFueraDeEscala() {
        assertEquals("Sin síntomas", etiquetaEscala(0))
        assertEquals("Severa", etiquetaEscala(4))
        assertNull(etiquetaEscala(5))
    }

    @Test
    fun horasSueno_sinDato_esValido() {
        assertTrue(esHorasSuenoValido(null))
    }

    @Test
    fun horasSueno_bordes0y24_sonValidos_25yNegativoNo() {
        assertTrue(esHorasSuenoValido(0))
        assertTrue(esHorasSuenoValido(24))
        assertFalse(esHorasSuenoValido(25))
        assertFalse(esHorasSuenoValido(-1))
    }

    @Test
    fun resumenAnimo_tomaElMayorDeLosDosValores() {
        assertEquals(3, nivelResumenAnimo(depresion = 3, elevacion = 1))
        assertEquals(4, nivelResumenAnimo(depresion = 0, elevacion = 4))
    }
}