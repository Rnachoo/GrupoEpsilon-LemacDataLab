package com.example.lemacdatalab.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class RachaTest {

    private val hoy = Calendar.getInstance().apply { set(2026, Calendar.OCTOBER, 30, 12, 0) }

    private fun registro(fechaHora: String) = RegistroHistorial(
        idRegistro = 0,
        tipoArea = "animo",
        fechaHoraCreacion = fechaHora,
        notasOpcionales = null,
        nivelIntensidad = 5,
        contexto = null,
        habilidad = null,
        nivelImpulso = null
    )

    @Test
    fun sinRegistros_rachaCero() {
        assertEquals(0, calcularRacha(emptyList(), hoy))
    }

    @Test
    fun diasSeguidosHastaHoy_cuentaCadaDiaUnaVez() {
        val registros = listOf(
            registro("2026-10-30 09:00:00"),
            registro("2026-10-30 18:30:00"),
            registro("2026-10-29 10:00:00"),
            registro("2026-10-28 22:15:00")
        )
        assertEquals(3, calcularRacha(registros, hoy))
    }

    @Test
    fun sinRegistroHoy_laRachaSigueDesdeAyer() {
        val registros = listOf(
            registro("2026-10-29 10:00:00"),
            registro("2026-10-28 10:00:00")
        )
        assertEquals(2, calcularRacha(registros, hoy))
    }

    @Test
    fun unDiaSinRegistro_cortaLaRacha() {
        val registros = listOf(
            registro("2026-10-30 10:00:00"),
            registro("2026-10-28 10:00:00")
        )
        assertEquals(1, calcularRacha(registros, hoy))
    }
}
