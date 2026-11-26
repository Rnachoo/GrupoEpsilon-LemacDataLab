package com.example.lemacdatalab.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExportarCsvTest {

    @Test
    fun exportaLosCuatroAmbitosConEscala0a4YEtiqueta() {
        val registros = listOf(
            RegistroHistorial(
                idRegistro = 1,
                tipoArea = "animo",
                fechaHoraCreacion = "2026-10-30 09:00:00",
                notasOpcionales = "Buen descanso",
                nivelIntensidad = 2,
                contexto = "Ámbito Laboral",
                habilidad = null,
                nivelImpulso = null,
                estadoRegistro = "confirmado",
                depresion = 1,
                elevacion = 2,
                horasSueno = 7,
                altoRiesgo = false,
                sintomasPsicoticos = false
            ),
            RegistroHistorial(
                idRegistro = 2,
                tipoArea = "dbt",
                fechaHoraCreacion = "2026-10-30 14:00:00",
                notasOpcionales = null,
                nivelIntensidad = null,
                contexto = null,
                habilidad = "Mindfulness",
                nivelImpulso = 4,
                estadoRegistro = "editado"
            ),
            RegistroHistorial(
                idRegistro = 3,
                tipoArea = "neurodesarrollo",
                fechaHoraCreacion = "2026-10-30 18:00:00",
                notasOpcionales = "Bloques cortos",
                nivelIntensidad = null,
                contexto = null,
                habilidad = null,
                nivelImpulso = null,
                tipoEntrada = "Tarea",
                intensidadNeuro = 0,
                estrategia = "Lista de pasos",
                resultadoPercibido = "Me ayudó"
            ),
            RegistroHistorial(
                idRegistro = 4,
                tipoArea = "adicciones",
                fechaHoraCreacion = "2026-10-30 21:00:00",
                notasOpcionales = "Caminata",
                nivelIntensidad = null,
                contexto = null,
                habilidad = null,
                nivelImpulso = null,
                craving = 3,
                desencadenante = "Estrés o sobrecarga",
                accionProtectora = "Salir a caminar o ejercicio",
                estadoObjetivo = "Cumplido"
            )
        )

        val lineas = registrosACsv(registros).trim().lines()

        assertEquals(5, lineas.size)
        assertTrue(lineas[0].startsWith("fecha,tipo,estado_registro,nivel_escala_0_4,etiqueta_escala,"))
        assertTrue(lineas[1].contains("\"animo\",\"confirmado\",\"2\",\"Moderada\",\"1\",\"2\",\"7\",\"false\",\"false\",\"Ámbito Laboral\""))
        assertTrue(lineas[2].contains("\"dbt\",\"editado\",\"4\",\"Severa\""))
        assertTrue(lineas[2].contains("\"Mindfulness\",\"4\""))
        assertTrue(lineas[3].contains("\"neurodesarrollo\",\"confirmado\",\"0\",\"Sin síntomas\""))
        assertTrue(lineas[3].contains("\"Tarea\",\"0\",\"Lista de pasos\",\"Me ayudó\""))
        assertTrue(lineas[4].contains("\"adicciones\",\"confirmado\",\"3\",\"Marcada\""))
        assertTrue(lineas[4].contains("\"3\",\"Estrés o sobrecarga\",\"Salir a caminar o ejercicio\",\"Cumplido\",\"Caminata\""))
    }

    @Test
    fun escapaComillasDoblesYComasEnNotasYCampos() {
        val registro = RegistroHistorial(
            idRegistro = 10,
            tipoArea = "dbt",
            fechaHoraCreacion = "2026-10-30 12:00:00",
            notasOpcionales = "Nota con coma, y \"comillas\" internas",
            nivelIntensidad = null,
            contexto = null,
            habilidad = "Otro: \"respirar, pausar\"",
            nivelImpulso = 1
        )

        val lineas = registrosACsv(listOf(registro)).trim().lines()

        assertEquals(2, lineas.size)
        assertTrue(lineas[1].contains("\"Otro: \"\"respirar, pausar\"\"\""))
        assertTrue(lineas[1].contains("\"Nota con coma, y \"\"comillas\"\" internas\""))
    }

    @Test
    fun listaVacia_exportaSoloEncabezado() {
        val lineas = registrosACsv(emptyList()).trim().lines()
        assertEquals(1, lineas.size)
        assertTrue(lineas[0].contains("craving,desencadenante,accion_protectora,estado_objetivo,notas"))
    }
}
