package com.example.lemacdatalab.model

fun registrosACsv(registros: List<RegistroHistorial>): String = buildString {
    appendLine(
        "fecha,tipo,estado_registro,nivel_escala_0_4,etiqueta_escala," +
                "depresion,elevacion,horas_sueno,alto_riesgo,sintomas_psicoticos,contexto," +
                "habilidad,intensidad_emocional," +
                "tipo_entrada,intensidad_neuro,estrategia,resultado_percibido," +
                "craving,desencadenante,accion_protectora,estado_objetivo,notas"
    )
    registros.forEach { r ->
        val nivelEscala = valorEscalaRegistro(r)
        val etiqueta = nivelEscala?.let { etiquetaEscala(it) }.orEmpty()
        appendLine(
            listOf(
                r.fechaHoraCreacion,
                r.tipoArea,
                r.estadoRegistro,
                nivelEscala?.toString().orEmpty(),
                etiqueta,
                r.depresion?.toString().orEmpty(),
                r.elevacion?.toString().orEmpty(),
                r.horasSueno?.toString().orEmpty(),
                r.altoRiesgo?.toString().orEmpty(),
                r.sintomasPsicoticos?.toString().orEmpty(),
                r.contexto.orEmpty(),
                r.habilidad.orEmpty(),
                r.nivelImpulso?.toString().orEmpty(),
                r.tipoEntrada.orEmpty(),
                r.intensidadNeuro?.toString().orEmpty(),
                r.estrategia.orEmpty(),
                r.resultadoPercibido.orEmpty(),
                r.craving?.toString().orEmpty(),
                r.desencadenante.orEmpty(),
                r.accionProtectora.orEmpty(),
                r.estadoObjetivo.orEmpty(),
                r.notasOpcionales.orEmpty()
            ).joinToString(",") { escaparCampoCsv(it) }
        )
    }
}

internal fun valorEscalaRegistro(r: RegistroHistorial): Int? = when (r.tipoArea) {
    "animo" -> r.nivelIntensidad
        ?: if (r.depresion != null || r.elevacion != null) {
            nivelResumenAnimo(r.depresion ?: 0, r.elevacion ?: 0)
        } else {
            null
        }
    "dbt" -> r.nivelImpulso
    "neurodesarrollo" -> r.intensidadNeuro
    "adicciones" -> r.craving
    else -> r.nivelIntensidad
}

internal fun escaparCampoCsv(valor: String): String =
    "\"${valor.replace("\"", "\"\"")}\""
