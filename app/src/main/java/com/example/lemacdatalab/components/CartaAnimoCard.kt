package com.example.lemacdatalab.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lemacdatalab.model.DIAS_CARTA_ANIMO
import com.example.lemacdatalab.model.DiaEvolucion
import com.example.lemacdatalab.model.ESCALA_MAX
import com.example.lemacdatalab.model.ESCALA_MIN
import com.example.lemacdatalab.model.PERIODOS_CARTA_ANIMO
import com.example.lemacdatalab.model.PuntoCarta
import com.example.lemacdatalab.model.SerieEvolucionAmbito
import com.example.lemacdatalab.model.TEXTO_SIN_REGISTRO
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

// Escala 0 a 4: 0-1 salvia · 2 arena · 3-4 terracota
private const val CORTE_BAJO = 1.5f
private const val CORTE_ALTO = 2.5f
private val ColorElevacion = WarmMedAccent
private val ColorDepresion = Color(0xFF4A6FA5)
private val ColorFondoDepresion = Color(0xFFEEF3F9)
private val ColorHueco = Color(0xFFB5BEB8)

private fun colorPunto(promedio: Float): Color = when {
    promedio < CORTE_BAJO -> WarmLowAccent
    promedio < CORTE_ALTO -> WarmMedAccent
    else -> WarmHighAccent
}

private fun colorLineaAmbito(tipoArea: String): Color = when (tipoArea) {
    "dbt" -> Color(0xFF5D8470)
    "neurodesarrollo" -> Color(0xFF5E739B)
    "adicciones" -> WarmHighAccent
    else -> LemacGreenDark
}

private fun tituloGraficoAmbito(tipoArea: String): String = when (tipoArea) {
    "animo" -> "Evolución · Estado de Ánimo"
    "dbt" -> "Evolución · Diario DBT"
    "neurodesarrollo" -> "Evolución · Neurodesarrollo"
    "adicciones" -> "Evolución · Adicciones"
    else -> "Evolución"
}

private fun subtituloGraficoAmbito(tipoArea: String, dias: Int): String = when (tipoArea) {
    "animo" -> "Elevación (0 a +4) y Depresión (0 a -4) · últimos $dias días"
    "dbt" -> "Intensidad emocional ($ESCALA_MIN a $ESCALA_MAX) · últimos $dias días"
    "neurodesarrollo" -> "Intensidad ($ESCALA_MIN a $ESCALA_MAX) · últimos $dias días"
    "adicciones" -> "Craving ($ESCALA_MIN a $ESCALA_MAX) · últimos $dias días"
    else -> "Escala $ESCALA_MIN a $ESCALA_MAX · últimos $dias días"
}

@Composable
fun GraficoEvolucionAmbitoCard(
    serie: SerieEvolucionAmbito,
    modifier: Modifier = Modifier,
    mostrarSelectorDias: Boolean = true,
    onCambiarDias: (Int) -> Unit = {}
) {
    val titulo = tituloGraficoAmbito(serie.tipoArea)
    val subtitulo = subtituloGraficoAmbito(serie.tipoArea, serie.dias)

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
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LemacGreenDark
            )
            Text(
                text = subtitulo,
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario
            )
            Text(
                text = "${serie.diasConRegistro} con registro · ${serie.diasSinRegistro} días «$TEXTO_SIN_REGISTRO»",
                style = MaterialTheme.typography.labelSmall,
                color = TextoSecundario
            )

            if (mostrarSelectorDias) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PERIODOS_CARTA_ANIMO.forEach { opcionDias ->
                        val seleccionado = serie.dias == opcionDias
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
            }

            if (serie.puntos.isEmpty()) {
                Text(
                    text = "$TEXTO_SIN_REGISTRO en los últimos ${serie.dias} días. Los días sin datos se muestran como hueco y nunca como 0.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoSecundario,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else if (serie.tipoArea == "animo") {
                val maxElevacion = serie.puntos.maxOf { it.elevacion }
                val minDepresion = serie.puntos.minOf { it.depresionConSigno }
                val descripcion = "Gráfico de Estado de Ánimo de los últimos ${serie.dias} días con dos líneas entre -4 y +4. " +
                        "${serie.diasConRegistro} días con registro y ${serie.diasSinRegistro} días sin registro. " +
                        "Elevación máxima +${formato(maxElevacion)}, depresión mínima ${formato(minDepresion)}."

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(
                        modifier = Modifier.height(ALTO_GRAFICO_BIPOLAR),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("+4", style = MaterialTheme.typography.labelSmall, color = TextoSecundario)
                        Text("0", style = MaterialTheme.typography.labelSmall, color = TextoSecundario)
                        Text("-4", style = MaterialTheme.typography.labelSmall, color = TextoSecundario)
                    }
                    GraficoLineaAnimoBipolar(
                        puntos = serie.puntos,
                        calendario = serie.calendario,
                        dias = serie.dias,
                        modifier = Modifier
                            .weight(1f)
                            .height(ALTO_GRAFICO_BIPOLAR)
                            .semantics { contentDescription = descripcion }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Hace ${serie.dias} días", style = MaterialTheme.typography.labelSmall, color = TextoSecundario)
                    Text("Hoy", style = MaterialTheme.typography.labelSmall, color = TextoSecundario)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LeyendaPunto(color = ColorElevacion, texto = "Elevación (0 a +4)")
                    LeyendaPunto(color = ColorDepresion, texto = "Depresión (0 a -4)")
                    LeyendaHueco(texto = TEXTO_SIN_REGISTRO)
                }
            } else {
                val minimo = serie.puntos.minOf { it.promedio }
                val maximo = serie.puntos.maxOf { it.promedio }
                val descripcion = "$titulo de los últimos ${serie.dias} días en escala $ESCALA_MIN a $ESCALA_MAX. " +
                        "${serie.diasConRegistro} días con registro y ${serie.diasSinRegistro} días sin registro. " +
                        "Mínimo ${formato(minimo)}, máximo ${formato(maximo)}."

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(
                        modifier = Modifier.height(ALTO_GRAFICO),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("$ESCALA_MAX", style = MaterialTheme.typography.labelSmall, color = TextoSecundario)
                        Text("$ESCALA_MIN", style = MaterialTheme.typography.labelSmall, color = TextoSecundario)
                    }
                    GraficoLineaAmbito(
                        puntos = serie.puntos,
                        calendario = serie.calendario,
                        dias = serie.dias,
                        colorLinea = colorLineaAmbito(serie.tipoArea),
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
                    Text("Hace ${serie.dias} días", style = MaterialTheme.typography.labelSmall, color = TextoSecundario)
                    Text("Hoy", style = MaterialTheme.typography.labelSmall, color = TextoSecundario)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LeyendaPunto(color = colorLineaAmbito(serie.tipoArea), texto = "Con registro (0 a 4)")
                    LeyendaHueco(texto = "Hueco: $TEXTO_SIN_REGISTRO")
                }
            }
        }
    }
}

@Composable
fun CartaAnimoCard(
    puntos: List<PuntoCarta>,
    modifier: Modifier = Modifier,
    dias: Int = DIAS_CARTA_ANIMO,
    onCambiarDias: (Int) -> Unit = {}
) {
    val puntosPorDia = puntos.associateBy { it.dia }
    val calendario = (0 until dias).map { dia ->
        val p = puntosPorDia[dia]
        DiaEvolucion(dia = dia, fecha = p?.fecha.orEmpty(), punto = p)
    }
    GraficoEvolucionAmbitoCard(
        serie = SerieEvolucionAmbito(
            tipoArea = "animo",
            dias = dias,
            puntos = puntos,
            calendario = calendario
        ),
        modifier = modifier,
        mostrarSelectorDias = true,
        onCambiarDias = onCambiarDias
    )
}

@Composable
private fun LeyendaPunto(color: Color, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(text = texto, fontSize = 11.sp, color = TextoSecundario)
    }
}

@Composable
private fun LeyendaHueco(texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(ColorHueco.copy(alpha = 0.4f))
        )
        Text(text = texto, fontSize = 11.sp, color = TextoSecundario)
    }
}

private val ALTO_GRAFICO = 150.dp
private val ALTO_GRAFICO_BIPOLAR = 176.dp

private fun formato(valor: Float): String = String.format(Locale.US, "%.1f", valor)

@Composable
private fun GraficoLineaAmbito(
    puntos: List<PuntoCarta>,
    calendario: List<DiaEvolucion>,
    dias: Int,
    colorLinea: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val margenV = 8.dp.toPx()
        val margenH = 6.dp.toPx()
        val altoUtil = size.height - 2 * margenV
        val anchoUtil = size.width - 2 * margenH

        fun yDe(valor: Float) =
            margenV + (ESCALA_MAX - valor) / (ESCALA_MAX - ESCALA_MIN).toFloat() * altoUtil

        fun xDe(dia: Int) = margenH + dia.toFloat() / (dias - 1).coerceAtLeast(1) * anchoUtil

        val yAlto = yDe(CORTE_ALTO)
        val yBajo = yDe(CORTE_BAJO)
        drawRect(WarmHighBg, topLeft = Offset(0f, 0f), size = Size(size.width, yAlto))
        drawRect(WarmMedBg, topLeft = Offset(0f, yAlto), size = Size(size.width, yBajo - yAlto))
        drawRect(WarmLowBg, topLeft = Offset(0f, yBajo), size = Size(size.width, size.height - yBajo))

        // Marcadores huecos para días sin registro (nunca valor 0)
        val yHueco = size.height - margenV / 2f
        calendario.filter { !it.tieneRegistro }.forEach { hueco ->
            drawCircle(
                color = ColorHueco,
                radius = 2.dp.toPx(),
                center = Offset(xDe(hueco.dia), yHueco),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // Conectar días con registro; si hay días sin registro en medio, usar línea punteada
        val trazoPunteado = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
        for (i in 0 until puntos.size - 1) {
            val actual = puntos[i]
            val siguiente = puntos[i + 1]
            val esConsecutivo = siguiente.dia == actual.dia + 1
            drawLine(
                color = colorLinea,
                start = Offset(xDe(actual.dia), yDe(actual.promedio)),
                end = Offset(xDe(siguiente.dia), yDe(siguiente.promedio)),
                strokeWidth = 2.5.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = if (esConsecutivo) null else trazoPunteado
            )
        }

        puntos.forEach { p ->
            val centro = Offset(xDe(p.dia), yDe(p.promedio))
            drawCircle(color = Color.White, radius = 6.dp.toPx(), center = centro)
            drawCircle(color = colorPunto(p.promedio), radius = 4.5.dp.toPx(), center = centro)
        }
    }
}

@Composable
private fun GraficoLineaAnimoBipolar(
    puntos: List<PuntoCarta>,
    calendario: List<DiaEvolucion>,
    dias: Int,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val margenV = 10.dp.toPx()
        val margenH = 6.dp.toPx()
        val altoUtil = size.height - 2 * margenV
        val anchoUtil = size.width - 2 * margenH

        // Escala vertical de -4f (abajo) a +4f (arriba)
        fun yDe(valor: Float): Float =
            margenV + (4f - valor) / 8f * altoUtil

        fun xDe(dia: Int) = margenH + dia.toFloat() / (dias - 1).coerceAtLeast(1) * anchoUtil

        val yCero = yDe(0f)
        drawRect(WarmMedBg.copy(alpha = 0.7f), topLeft = Offset(0f, 0f), size = Size(size.width, yCero))
        drawRect(ColorFondoDepresion, topLeft = Offset(0f, yCero), size = Size(size.width, size.height - yCero))

        // Línea horizontal de referencia en 0
        drawLine(
            color = LemacBorderSoft,
            start = Offset(0f, yCero),
            end = Offset(size.width, yCero),
            strokeWidth = 1.2.dp.toPx()
        )

        // Marcadores huecos para días sin registro en el borde inferior (no en valor 0 del gráfico)
        val yHueco = size.height - margenV / 2f
        calendario.filter { !it.tieneRegistro }.forEach { hueco ->
            drawCircle(
                color = ColorHueco,
                radius = 2.dp.toPx(),
                center = Offset(xDe(hueco.dia), yHueco),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        val trazoPunteado = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
        for (i in 0 until puntos.size - 1) {
            val actual = puntos[i]
            val siguiente = puntos[i + 1]
            val efecto = if (siguiente.dia == actual.dia + 1) null else trazoPunteado

            // Línea 1: Elevación (0 a +4)
            drawLine(
                color = ColorElevacion,
                start = Offset(xDe(actual.dia), yDe(actual.elevacion)),
                end = Offset(xDe(siguiente.dia), yDe(siguiente.elevacion)),
                strokeWidth = 2.5.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = efecto
            )
            // Línea 2: Depresión (0 a -4)
            drawLine(
                color = ColorDepresion,
                start = Offset(xDe(actual.dia), yDe(actual.depresionConSigno)),
                end = Offset(xDe(siguiente.dia), yDe(siguiente.depresionConSigno)),
                strokeWidth = 2.5.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = efecto
            )
        }

        puntos.forEach { p ->
            val x = xDe(p.dia)
            val centroElevacion = Offset(x, yDe(p.elevacion))
            drawCircle(color = Color.White, radius = 5.5.dp.toPx(), center = centroElevacion)
            drawCircle(color = ColorElevacion, radius = 4.dp.toPx(), center = centroElevacion)

            val centroDepresion = Offset(x, yDe(p.depresionConSigno))
            drawCircle(color = Color.White, radius = 5.5.dp.toPx(), center = centroDepresion)
            drawCircle(color = ColorDepresion, radius = 4.dp.toPx(), center = centroDepresion)
        }
    }
}
