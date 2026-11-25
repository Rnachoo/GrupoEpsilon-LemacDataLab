package com.example.lemacdatalab.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EscalaIntensidadTest {

    @Test
    fun limitesDeLaEscala_sonValidos() {
        assertTrue(esEscalaValida(0))
        assertTrue(esEscalaValida(4))
    }

    @Test
    fun fueraDeLaEscala_esInvalido() {
        assertFalse(esEscalaValida(-1))
        assertFalse(esEscalaValida(5))
    }

    @Test
    fun etiquetas_coincidenConLaContraparte() {
        assertEquals("Sin síntomas", etiquetaEscala(0))
        assertEquals("Moderada", etiquetaEscala(2))
        assertEquals("Severa", etiquetaEscala(4))
    }

    @Test
    fun etiquetaFueraDeEscala_esNula() {
        assertNull(etiquetaEscala(5))
    }
}