package com.example.lemacdatalab.model

fun esOtroValido(seleccionoOtro: Boolean, texto: String): Boolean =
    !seleccionoOtro || texto.isNotBlank()

fun sonNotasValidas(notas: String, maximo: Int): Boolean = notas.length <= maximo