package com.example.lemacdatalab.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lemacdatalab.model.DIAS_CARTA_ANIMO
import com.example.lemacdatalab.model.PERIODOS_CARTA_ANIMO
import com.example.lemacdatalab.model.PuntoCarta
import com.example.lemacdatalab.ui.theme.LemacBorderSoft
import com.example.lemacdatalab.ui.theme.LemacGreenDark
import com.example.lemacdatalab.ui.theme.TextoSecundario
import com.example.lemacdatalab.ui.theme.WarmHighAccent
import com.example.lemacdatalab.ui.theme.WarmHighBg
import com.example.lemacdatalab.ui.theme.WarmLowAccent
import com.example.lemacdatalab.ui.theme.WarmLowBg
import com.example.lemacdatalab.ui.theme.WarmMedAccent
import com.example.lemacdatalab.ui.theme.WarmMedBg
import java.util.Locale

private const val CORTE_BAJO = 3.5f
private const val CORTE_ALTO = 7.5f

private fun colorPunto(promedio: Float): Color = when {
    promedio < CORTE_BAJO -> WarmLowAccent
    promedio < CORTE_ALTO -> WarmMedAccent
    else -> WarmHighAccent
}

@Composable
fun CartaAnimoCard(
    puntos: List<PuntoCarta>,
    modifier: Modifier = Modifier,
    dias: Int = DIAS_CARTA_ANIMO,
    onCambiarDias: (Int) -> Unit = {}
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, LemacBorderSoft),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Tu carta del ánimo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LemacGreenDark
            )
            Text(
                text = "Promedio diario de intensidad (1 a 10) · últimos $dias días",
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario
            )

            // Selector de período: 7 y 30 días
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PERIODOS_CARTA_ANIMO.forEach { opcionDias ->
                    val seleccionado = dias == opcionDias
                    FilterChip(
                        selected = seleccionado,
                        onClick = { onCambiarDias(opcionDias) },
                        label = { Text("$opcionDias días") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LemacGreenDark,
                            selectedLabelColor = Color.White
                        ),
                        shape = CircleShape
                    )
                }
            }

            if (puntos.size < 2) {
                Text(
                    text = "Registra tu ánimo en al menos 2 días distintos para ver cómo va cambiando.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoSecundario,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                val minimo = puntos.minOf { it.promedio }
                val maximo = puntos.maxOf { it.promedio }
                val descripcion = "Gráfico de línea con el promedio diario de intensidad de ánimo " +
                        "de los últimos $dias días. ${puntos.size} días con registro; " +
                        "mínimo ${formato(minimo)}, máximo ${formato(maximo)}."

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(
                        modifier = Modifier.height(ALTO_GRAFICO),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("10", style = MaterialTheme.typography.labelSmall, color = TextoSecundario)
                        Text("1", style = MaterialTheme.typography.labelSmall, color = TextoSecundario)
                    }
                    GraficoLinea(
                        puntos = puntos,
                        dias = dias,
                        modifier = Modifier
                            .weight(1f)
                            .height(ALTO_GRAFICO)
                            .semantics { contentDescription = descripcion }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Hace $dias días", style = MaterialTheme.typography.labelSmall, color = TextoSecundario)
                    Text("Hoy", style = MaterialTheme.typography.labelSmall, color = TextoSecundario)
                }
            }
        }
    }
}

private val ALTO_GRAFICO = 160.dp

private fun formato(valor: Float): String = String.format(Locale.US, "%.1f", valor)

@Composable
private fun GraficoLinea(
    puntos: List<PuntoCarta>,
    dias: Int,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val margenV = 8.dp.toPx()
        val margenH = 6.dp.toPx()
        val altoUtil = size.height - 2 * margenV
        val anchoUtil = size.width - 2 * margenH

        fun yDe(valor: Float) = margenV + (10f - valor) / 9f * altoUtil
        fun xDe(dia: Int) = margenH + dia.toFloat() / (dias - 1).coerceAtLeast(1) * anchoUtil

        // Bandas de fondo según los rangos de la escala
        val yAlto = yDe(CORTE_ALTO)
        val yBajo = yDe(CORTE_BAJO)
        drawRect(WarmHighBg, topLeft = Offset(0f, 0f), size = Size(size.width, yAlto))
        drawRect(WarmMedBg, topLeft = Offset(0f, yAlto), size = Size(size.width, yBajo - yAlto))
        drawRect(WarmLowBg, topLeft = Offset(0f, yBajo), size = Size(size.width, size.height - yBajo))

        // Línea que une los días con registro
        val trazo = Path()
        puntos.forEachIndexed { i, p ->
            val x = xDe(p.dia)
            val y = yDe(p.promedio)
            if (i == 0) trazo.moveTo(x, y) else trazo.lineTo(x, y)
        }
        drawPath(
            path = trazo,
            color = LemacGreenDark,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Puntos con el color semántico de su rango
        puntos.forEach { p ->
            val centro = Offset(xDe(p.dia), yDe(p.promedio))
            drawCircle(color = Color.White, radius = 6.dp.toPx(), center = centro)
            drawCircle(color = colorPunto(p.promedio), radius = 4.5.dp.toPx(), center = centro)
        }
    }
}
