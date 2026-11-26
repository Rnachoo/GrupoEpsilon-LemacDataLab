package com.example.lemacdatalab.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidacionTextoTest {

    // ---------- «Otro (especificar)» ----------

    @Test
    fun otroSeleccionadoYVacio_esInvalido() {
        assertFalse(esOtroValido(seleccionoOtro = true, texto = ""))
    }

    @Test
    fun otroSeleccionadoSoloConEspacios_esInvalido() {
        assertFalse(esOtroValido(seleccionoOtro = true, texto = "   "))
    }

    @Test
    fun otroSeleccionadoConTexto_esValido() {
        assertTrue(esOtroValido(seleccionoOtro = true, texto = "Escuchar música"))
    }

    @Test
    fun otroNoSeleccionado_esValidoAunqueEsteVacio() {
        assertTrue(esOtroValido(seleccionoOtro = false, texto = ""))
    }

    // ---------- Largo de las notas ----------

    @Test
    fun notasVacias_sonValidas() {
        assertTrue(sonNotasValidas("", maximo = 200))
    }

    @Test
    fun notasJustoEnElLimite_sonValidas() {
        assertTrue(sonNotasValidas("a".repeat(200), maximo = 200))
    }

    @Test
    fun notasUnCaracterSobreElLimite_sonInvalidas() {
        assertFalse(sonNotasValidas("a".repeat(201), maximo = 200))
    }
}