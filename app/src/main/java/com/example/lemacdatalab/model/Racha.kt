package com.example.lemacdatalab.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// cuenta los días seguidos con al menos un registro; si hoy aún no hay, la racha sigue viva desde ayer
fun calcularRacha(
    registros: List<RegistroHistorial>,
    hoy: Calendar = Calendar.getInstance()
): Int {
    val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val diasConRegistro = registros.map { it.fechaHoraCreacion.take(10) }.toSet()
    val dia = hoy.clone() as Calendar

    if (formato.format(dia.time) !in diasConRegistro) {
        dia.add(Calendar.DAY_OF_YEAR, -1)
    }

    var racha = 0
    while (formato.format(dia.time) in diasConRegistro) {
        racha++
        dia.add(Calendar.DAY_OF_YEAR, -1)
    }
    return racha
}
