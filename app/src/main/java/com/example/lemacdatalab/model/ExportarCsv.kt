package com.example.lemacdatalab.model

fun registrosACsv(registros: List<RegistroHistorial>): String = buildString {
    appendLine("fecha,tipo,nivel_intensidad,contexto,habilidad,nivel_impulso,notas")
    registros.forEach { r ->
        appendLine(
            listOf(
                r.fechaHoraCreacion,
                r.tipoArea,
                r.nivelIntensidad?.toString().orEmpty(),
                r.contexto.orEmpty(),
                r.habilidad.orEmpty(),
                r.nivelImpulso?.toString().orEmpty(),
                r.notasOpcionales.orEmpty()
            ).joinToString(",") { "\"${it.replace("\"", "\"\"")}\"" }
        )
    }
}