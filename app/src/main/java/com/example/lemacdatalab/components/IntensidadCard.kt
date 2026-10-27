package com.example.lemacdatalab.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lemacdatalab.native.vibrarTic
import com.example.lemacdatalab.ui.theme.WarmHighAccent
import com.example.lemacdatalab.ui.theme.WarmHighBg
import com.example.lemacdatalab.ui.theme.WarmLowAccent
import com.example.lemacdatalab.ui.theme.WarmLowBg
import com.example.lemacdatalab.ui.theme.WarmMedAccent
import com.example.lemacdatalab.ui.theme.WarmMedBg
import kotlin.math.roundToInt

@Composable
fun IntensidadCard(
    valor: Float,
    onValorChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    etiquetaMin: String = "NULO (1)",
    etiquetaMax: String = "ELEVADO (10)"
) {
    val nivel = valor.roundToInt().coerceIn(1, 10)
    val context = LocalContext.current

    // Selección de colores según el rango (1-3 Salvia, 4-7 Arena, 8-10 Terracota)
    val targetBgColor = when (nivel) {
        in 1..3 -> WarmLowBg
        in 4..7 -> WarmMedBg
        else -> WarmHighBg
    }

    val targetAccentColor = when (nivel) {
        in 1..3 -> WarmLowAccent
        in 4..7 -> WarmMedAccent
        else -> WarmHighAccent
    }

    val animatedBgColor by animateColorAsState(
        targetValue = targetBgColor,
        animationSpec = tween(durationMillis = 350),
        label = "FondoIntensidad"
    )

    val animatedAccentColor by animateColorAsState(
        targetValue = targetAccentColor,
        animationSpec = tween(durationMillis = 350),
        label = "AcentoIntensidad"
    )

    // Descriptor emocional dinámico
    val descriptorEmocional = when (nivel) {
        in 1..3 -> "En calma / Muy leve"
        in 4..6 -> "Moderado / Presente"
        in 7..8 -> "Elevado / Intenso"
        else -> "Muy abrumador"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = animatedBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = nivel.toString(),
                fontSize = 52.sp,
                fontWeight = FontWeight.Bold,
                color = animatedAccentColor
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Píldora semántica de validación emocional
            Surface(
                shape = CircleShape,
                color = animatedAccentColor.copy(alpha = 0.14f)
            ) {
                Text(
                    text = descriptorEmocional,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = animatedAccentColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Slider(
                value = valor,
                onValueChange = { nuevo ->
                    // Tic suave solo cuando cambia el número, no en cada píxel del arrastre.
                    // Misma intensidad en todos los niveles para no sentirse alarmante.
                    if (nuevo.roundToInt().coerceIn(1, 10) != nivel) {
                        vibrarTic(context)
                    }
                    onValorChange(nuevo)
                },
                valueRange = 1f..10f,
                steps = 8,
                colors = SliderDefaults.colors(
                    thumbColor = animatedAccentColor,
                    activeTrackColor = animatedAccentColor,
                    inactiveTrackColor = animatedAccentColor.copy(alpha = 0.22f),
                    activeTickColor = animatedBgColor,
                    inactiveTickColor = animatedAccentColor.copy(alpha = 0.45f)
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = etiquetaMin,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = animatedAccentColor.copy(alpha = 0.85f)
                )
                Text(
                    text = etiquetaMax,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = animatedAccentColor.copy(alpha = 0.85f)
                )
            }
        }
    }
}