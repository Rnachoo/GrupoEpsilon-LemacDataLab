package com.example.lemacdatalab.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DatosAnimoTest {

    @Test
    fun horasSuenoVacio_esValido() {
        assertTrue(esHorasSuenoValido(null))
    }

    @Test
    fun horasSuenoEnLosLimites_sonValidas() {
        assertTrue(esHorasSuenoValido(0))
        assertTrue(esHorasSuenoValido(24))
    }

    @Test
    fun horasSuenoFueraDeRango_sonInvalidas() {
        assertFalse(esHorasSuenoValido(25))
        assertFalse(esHorasSuenoValido(-1))
    }

    @Test
    fun resumenDeAnimo_esElMayorDeLosDosValores() {
        assertEquals(3, nivelResumenAnimo(depresion = 3, elevacion = 1))
        assertEquals(4, nivelResumenAnimo(depresion = 0, elevacion = 4))
        assertEquals(0, nivelResumenAnimo(depresion = 0, elevacion = 0))
    }
}