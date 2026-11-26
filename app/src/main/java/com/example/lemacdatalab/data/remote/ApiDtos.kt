package com.example.lemacdatalab.data.remote

import com.example.lemacdatalab.model.AdiccionDetalle
import com.example.lemacdatalab.model.ContextoMood
import com.example.lemacdatalab.model.DbtDetalle
import com.example.lemacdatalab.model.EstrategiaNeuro
import com.example.lemacdatalab.model.HabilidadDbt
import com.example.lemacdatalab.model.MoodDetalle
import com.example.lemacdatalab.model.NeuroDetalle
import com.example.lemacdatalab.model.RegistroHistorial
import com.example.lemacdatalab.model.RegistroMaestro
import com.example.lemacdatalab.model.RegistroVersion
import com.example.lemacdatalab.model.ResultadoNeuro
import com.example.lemacdatalab.model.TipoEntradaNeuro
import com.example.lemacdatalab.model.Usuario
import com.example.lemacdatalab.model.esEscalaValida
import com.example.lemacdatalab.model.nivelResumenAnimo

data class LoginRequestDto(
    val identificadorSintetico: String
)

data class LoginResponseDto(
    val token: String,
    val idUsuario: Int,
    val identificadorSintetico: String,
    val rol: String,
    val fechaCreacion: String,
    val objetivoAdicciones: String? = null
) {
    fun aUsuario(): Usuario = Usuario(
        idUsuario = idUsuario,
        identificadorSintetico = identificadorSintetico,
        fechaCreacion = fechaCreacion,
        objetivoPrueba = objetivoAdicciones
    )
}

data class ConsentimientoDto(
    val identificadorSintetico: String,
    val version: String = "1.0",
    val aceptado: Boolean = false,
    val fechaAceptacionUtc: Long? = null,
    val texto: String = "LemacDataLab utiliza identificadores sintéticos y datos simulados. No constituye diagnóstico clínico ni servicio de urgencias."
)

data class AceptacionConsentimientoRequestDto(
    val identificadorSintetico: String,
    val version: String = "1.0",
    val fechaAceptacionUtc: Long
)

data class CatalogosDto(
    val contextosAnimo: List<String> = ContextoMood.entries.map { it.etiqueta },
    val habilidadesDbt: List<String> = HabilidadDbt.entries.map { it.etiqueta },
    val tiposEntradaNeuro: List<String> = TipoEntradaNeuro.entries.map { it.etiqueta },
    val estrategiasNeuro: List<String> = EstrategiaNeuro.entries.map { it.etiqueta },
    val resultadosNeuro: List<String> = ResultadoNeuro.entries.map { it.etiqueta },
    val desencadenantesAdiccion: List<String> = listOf(
        "Estrés o sobrecarga",
        "Contexto social",
        "Aislamiento",
        "Insomnio o cansancio",
        "Conflicto interpersonal",
        "Ninguno",
        "Otro (especificar)"
    ),
    val accionesProtectorasAdiccion: List<String> = listOf(
        "Contactar red de apoyo",
        "Salir a caminar o ejercicio",
        "Técnica de respiración",
        "Cambiar de ambiente",
        "Posponer 15 minutos",
        "Ninguna",
        "Otro (especificar)"
    ),
    val estadosObjetivoAdiccion: List<String> = listOf(
        "Cumplido",
        "Parcialmente cumplido",
        "No cumplido"
    )
)

data class PerfilDto(
    val idUsuario: Int,
    val identificadorSintetico: String,
    val rol: String,
    val fechaCreacion: String,
    val objetivoAdicciones: String? = null
) {
    fun aUsuario(): Usuario = Usuario(
        idUsuario = idUsuario,
        identificadorSintetico = identificadorSintetico,
        fechaCreacion = fechaCreacion,
        objetivoPrueba = objetivoAdicciones
    )
}

data class RegistroDto(
    val idRegistro: Int = 0,
    val idUsuario: Int,
    val tipoArea: String, // "animo" | "dbt" | "neurodesarrollo" | "adicciones"
    val fechaHoraCreacion: String,
    val fechaDia: String = fechaHoraCreacion.take(10),
    val notasOpcionales: String? = null,
    val estadoRegistro: String = "confirmado", // "confirmado" | "editado" | "eliminado"
    val modificadoEnUtc: Long,
    // Ánimo (0..4)
    val nivelIntensidad: Int? = null,
    val contexto: String? = null,
    val depresion: Int? = null,
    val elevacion: Int? = null,
    val horasSueno: Int? = null,
    val altoRiesgo: Boolean? = null,
    val sintomasPsicoticos: Boolean? = null,
    // DBT (0..4)
    val habilidad: String? = null,
    val intensidadEmocional: Int? = null,
    // Neurodesarrollo (0..4)
    val tipoEntrada: String? = null,
    val intensidadNeuro: Int? = null,
    val estrategia: String? = null,
    val resultadoPercibido: String? = null,
    // Adicciones (0..4)
    val craving: Int? = null,
    val desencadenante: String? = null,
    val accionProtectora: String? = null,
    val estadoObjetivo: String? = null
) {
    fun claveNatural(): Triple<Int, String, String> = Triple(idUsuario, fechaDia, tipoArea)

    fun tieneEscalaValida(): Boolean = when (tipoArea) {
        "animo" -> {
            val dep = depresion ?: 0
            val ele = elevacion ?: 0
            val niv = nivelIntensidad ?: nivelResumenAnimo(dep, ele)
            esEscalaValida(dep) && esEscalaValida(ele) && esEscalaValida(niv)
        }
        "dbt" -> intensidadEmocional != null && esEscalaValida(intensidadEmocional)
        "neurodesarrollo" -> intensidadNeuro != null && esEscalaValida(intensidadNeuro)
        "adicciones" -> craving != null && esEscalaValida(craving)
        else -> false
    }

    fun aRegistroMaestro(estadoSync: String = "sincronizado"): RegistroMaestro = RegistroMaestro(
        idRegistro = idRegistro,
        idUsuario = idUsuario,
        tipoArea = tipoArea,
        fechaHoraCreacion = fechaHoraCreacion,
        notasOpcionales = notasOpcionales,
        estadoSync = estadoSync,
        estadoRegistro = estadoRegistro,
        fechaDia = fechaDia,
        modificadoEnUtc = modificadoEnUtc
    )

    fun aMoodDetalle(idRegistroLocal: Int = idRegistro): MoodDetalle? {
        if (tipoArea != "animo") return null
        val dep = depresion ?: 0
        val ele = elevacion ?: 0
        return MoodDetalle(
            idRegistro = idRegistroLocal,
            nivelIntensidad = nivelIntensidad ?: nivelResumenAnimo(dep, ele),
            contexto = contexto.orEmpty(),
            depresion = dep,
            elevacion = ele,
            horasSueno = horasSueno,
            altoRiesgo = altoRiesgo ?: false,
            sintomasPsicoticos = sintomasPsicoticos ?: false
        )
    }

    fun aDbtDetalle(idRegistroLocal: Int = idRegistro): DbtDetalle? {
        if (tipoArea != "dbt") return null
        val intensidad = intensidadEmocional ?: 0
        return DbtDetalle(
            idRegistro = idRegistroLocal,
            habilidad = habilidad.orEmpty(),
            nivelImpulso = intensidad,
            intensidadEmocional = intensidad
        )
    }

    fun aNeuroDetalle(idRegistroLocal: Int = idRegistro): NeuroDetalle? {
        if (tipoArea != "neurodesarrollo") return null
        return NeuroDetalle(
            idRegistro = idRegistroLocal,
            tipoEntrada = tipoEntrada.orEmpty(),
            intensidad = intensidadNeuro ?: 0,
            estrategia = estrategia.orEmpty(),
            resultadoPercibido = resultadoPercibido.orEmpty()
        )
    }

    fun aAdiccionDetalle(idRegistroLocal: Int = idRegistro): AdiccionDetalle? {
        if (tipoArea != "adicciones") return null
        return AdiccionDetalle(
            idRegistro = idRegistroLocal,
            craving = craving ?: 0,
            desencadenante = desencadenante.orEmpty(),
            accionProtectora = accionProtectora.orEmpty(),
            estadoObjetivo = estadoObjetivo.orEmpty()
        )
    }

    fun aRegistroHistorial(): RegistroHistorial {
        val dep = depresion
        val ele = elevacion
        val resumenAnimo = if (tipoArea == "animo") {
            nivelIntensidad ?: if (dep != null || ele != null) nivelResumenAnimo(dep ?: 0, ele ?: 0) else null
        } else {
            nivelIntensidad
        }
        return RegistroHistorial(
            idRegistro = idRegistro,
            tipoArea = tipoArea,
            fechaHoraCreacion = fechaHoraCreacion,
            notasOpcionales = notasOpcionales,
            nivelIntensidad = resumenAnimo,
            contexto = contexto,
            habilidad = habilidad,
            nivelImpulso = intensidadEmocional,
            estadoRegistro = estadoRegistro,
            depresion = dep,
            elevacion = ele,
            horasSueno = horasSueno,
            altoRiesgo = altoRiesgo,
            sintomasPsicoticos = sintomasPsicoticos,
            tipoEntrada = tipoEntrada,
            intensidadNeuro = intensidadNeuro,
            estrategia = estrategia,
            resultadoPercibido = resultadoPercibido,
            craving = craving,
            desencadenante = desencadenante,
            accionProtectora = accionProtectora,
            estadoObjetivo = estadoObjetivo
        )
    }

    fun describirDetalle(): String = when (tipoArea) {
        "animo" -> {
            val dep = depresion ?: 0
            val ele = elevacion ?: 0
            val niv = nivelIntensidad ?: nivelResumenAnimo(dep, ele)
            "nivelIntensidad=$niv;contexto=${contexto.orEmpty()};depresion=$dep;" +
                    "elevacion=$ele;horasSueno=$horasSueno;altoRiesgo=${altoRiesgo ?: false};" +
                    "sintomasPsicoticos=${sintomasPsicoticos ?: false}"
        }
        "dbt" -> "habilidad=${habilidad.orEmpty()};intensidadEmocional=${intensidadEmocional ?: 0}"
        "neurodesarrollo" ->
            "tipoEntrada=${tipoEntrada.orEmpty()};intensidad=${intensidadNeuro ?: 0};" +
                    "estrategia=${estrategia.orEmpty()};resultadoPercibido=${resultadoPercibido.orEmpty()}"
        "adicciones" ->
            "craving=${craving ?: 0};desencadenante=${desencadenante.orEmpty()};" +
                    "accionProtectora=${accionProtectora.orEmpty()};estadoObjetivo=${estadoObjetivo.orEmpty()}"
        else -> ""
    }

    fun aRegistroVersion(
        idRegistroLocal: Int = idRegistro,
        fechaVersionUtc: Long = modificadoEnUtc
    ): RegistroVersion = RegistroVersion(
        idRegistro = idRegistroLocal,
        fechaVersionUtc = fechaVersionUtc,
        estadoAnterior = estadoRegistro,
        notasAnteriores = notasOpcionales,
        detalleAnterior = describirDetalle()
    )
}

data class DatasetSinteticoDto(
    val usuarios: List<PerfilDto> = emptyList(),
    val consentimientos: List<ConsentimientoDto> = emptyList(),
    val catalogos: CatalogosDto = CatalogosDto(),
    val registros: List<RegistroDto> = emptyList()
)
