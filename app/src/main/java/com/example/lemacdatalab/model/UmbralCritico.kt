package com.example.lemacdatalab.model

const val MAX_OTRO = 40


const val CRAVING_CRITICO = 4
const val INTENSIDAD_EMOCIONAL_CRITICA = 4
const val HORAS_SUENO_CRITICAS = 3

fun esCravingCritico(craving: Int): Boolean = craving == CRAVING_CRITICO

fun esIntensidadEmocionalCritica(intensidad: Int): Boolean =
    intensidad == INTENSIDAD_EMOCIONAL_CRITICA

fun esAnimoCritico(altoRiesgo: Boolean, sintomasPsicoticos: Boolean, horasSueno: Int?): Boolean =
    altoRiesgo || sintomasPsicoticos || (horasSueno != null && horasSueno < HORAS_SUENO_CRITICAS)

data class CriteriosCriticos(
    val craving: Int? = null,
    val intensidadEmocional: Int? = null,
    val altoRiesgo: Boolean = false,
    val sintomasPsicoticos: Boolean = false,
    val horasSueno: Int? = null
)

fun hayValorCritico(criterios: CriteriosCriticos): Boolean =
    (criterios.craving?.let { esCravingCritico(it) } ?: false) ||
            (criterios.intensidadEmocional?.let { esIntensidadEmocionalCritica(it) } ?: false) ||
            esAnimoCritico(criterios.altoRiesgo, criterios.sintomasPsicoticos, criterios.horasSueno)

// ---------- TEMPORAL ----------
const val UMBRAL_VALOR_CRITICO = 9
fun esValorCritico(nivel: Int): Boolean = nivel >= UMBRAL_VALOR_CRITICO