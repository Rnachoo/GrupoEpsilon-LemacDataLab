package com.example.lemacdatalab.model

enum class TipoEntradaNeuro(val etiqueta: String) {
    RUTINA("Rutina"),
    TAREA("Tarea"),
    SOBRECARGA("Sobrecarga")
}

enum class EstrategiaNeuro(val etiqueta: String) {
    PAUSA("Pausa breve"),
    LISTA("Lista de pasos"),
    TEMPORIZADOR("Temporizador"),
    RESPIRACION("Respiración o movimiento"),
    AYUDA("Pedir ayuda"),
    NINGUNA("Ninguna"),
    OTRO("Otro (especificar)")
}

enum class ResultadoNeuro(val etiqueta: String) {
    AYUDO("Me ayudó"),
    PARCIAL("Me ayudó en parte"),
    NO_AYUDO("No me ayudó")
}

data class DatosNeuro(
    val tipoEntrada: String,
    val intensidad: Int, // escala 0 a 4
    val estrategia: String,
    val resultadoPercibido: String
)