package com.example.lemacdatalab.data.remote

import com.example.lemacdatalab.model.RegistroVersion

data class ResultadoConflicto(
    val ganador: RegistroDto,
    val descartado: RegistroDto,
    val ganoRemoto: Boolean,
    val versionDescartada: RegistroVersion
)

/**
 * Función pura compartida entre el backend simulado y la sincronización de la app.
 * Regla: «última modificación gana» según modificadoEnUtc.
 * En caso de empate exacto de modificadoEnUtc, desempata de forma determinista
 * comparando la firma canónica del registro para que el resultado sea siempre el mismo.
 */
fun resolverConflicto(local: RegistroDto, remoto: RegistroDto): ResultadoConflicto {
    val ganoRemoto = when {
        remoto.modificadoEnUtc > local.modificadoEnUtc -> true
        local.modificadoEnUtc > remoto.modificadoEnUtc -> false
        else -> firmaDeterminista(remoto) >= firmaDeterminista(local)
    }

    val ganadorBase = if (ganoRemoto) remoto else local
    val descartado = if (ganoRemoto) local else remoto
    val idFinal = if (local.idRegistro != 0) local.idRegistro else remoto.idRegistro

    // Si el ganador no está eliminado y está reemplazando otra versión previa, queda marcado como editado
    val estadoFinal = when {
        ganadorBase.estadoRegistro == "eliminado" -> "eliminado"
        descartado.estadoRegistro == "eliminado" -> "confirmado"
        else -> "editado"
    }

    val ganador = ganadorBase.copy(
        idRegistro = idFinal,
        estadoRegistro = estadoFinal
    )

    val versionDescartada = descartado.aRegistroVersion(
        idRegistroLocal = idFinal,
        fechaVersionUtc = ganador.modificadoEnUtc
    )

    return ResultadoConflicto(
        ganador = ganador,
        descartado = descartado,
        ganoRemoto = ganoRemoto,
        versionDescartada = versionDescartada
    )
}

private fun firmaDeterminista(registro: RegistroDto): String =
    "${registro.estadoRegistro}|${registro.notasOpcionales.orEmpty()}|${registro.describirDetalle()}|${registro.idRegistro}"
