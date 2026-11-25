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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lemacdatalab.model.ESCALA_MAX
import com.example.lemacdatalab.model.ESCALA_MIN
import com.example.lemacdatalab.model.ETIQUETAS_ESCALA
import com.example.lemacdatalab.model.etiquetaEscala
import com.example.lemacdatalab.native.vibrarTic
import com.example.lemacdatalab.ui.theme.WarmHighAccent
import com.example.lemacdatalab.ui.theme.WarmHighBg
import com.example.lemacdatalab.ui.theme.WarmLowAccent
import com.example.lemacdatalab.ui.theme.WarmLowBg
import com.example.lemacdatalab.ui.theme.WarmMedAccent
import com.example.lemacdatalab.ui.theme.WarmMedBg
import kotlin.math.roundToInt

// Selector reutilizable de la escala común 0 a 4: muestra número y etiqueta.
// Sirve para Ánimo (depresión y elevación), DBT, Neurodesarrollo y Adicciones.
@Composable
fun SelectorEscala(
    valor: Int,
    onValorChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    titulo: String? = null
) {
    val nivel = valor.coerceIn(ESCALA_MIN, ESCALA_MAX)
    val etiqueta = etiquetaEscala(nivel).orEmpty()
    val context = LocalContext.current

    // 0 y 1: salvia · 2: arena · 3 y 4: terracota
    val targetBgColor = when (nivel) {
        0, 1 -> WarmLowBg
        2 -> WarmMedBg
        else -> WarmHighBg
    }
    val targetAccentColor = when (nivel) {
        0, 1 -> WarmLowAccent
        2 -> WarmMedAccent
        else -> WarmHighAccent
    }

    val animatedBgColor by animateColorAsState(
        targetValue = targetBgColor,
        animationSpec = tween(durationMillis = 350),
        label = "FondoEscala"
    )
    val animatedAccentColor by animateColorAsState(
        targetValue = targetAccentColor,
        animationSpec = tween(durationMillis = 350),
        label = "AcentoEscala"
    )

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
            if (titulo != null) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = animatedAccentColor
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = nivel.toString(),
                fontSize = 52.sp,
                fontWeight = FontWeight.Bold,
                color = animatedAccentColor
            )

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                shape = CircleShape,
                color = animatedAccentColor.copy(alpha = 0.14f)
            ) {
                Text(
                    text = etiqueta,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = animatedAccentColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Slider(
                value = nivel.toFloat(),
                onValueChange = { nuevo ->
                    val nuevoNivel = nuevo.roundToInt().coerceIn(ESCALA_MIN, ESCALA_MAX)
                    if (nuevoNivel != nivel) {
                        vibrarTic(context)
                        onValorChange(nuevoNivel)
                    }
                },
                valueRange = ESCALA_MIN.toFloat()..ESCALA_MAX.toFloat(),
                steps = ESCALA_MAX - ESCALA_MIN - 1,
                modifier = Modifier.semantics {
                    contentDescription = "${titulo ?: "Escala"}: $nivel, $etiqueta"
                },
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
                    text = "${ETIQUETAS_ESCALA.first().uppercase()} ($ESCALA_MIN)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = animatedAccentColor.copy(alpha = 0.85f)
                )
                Text(
                    text = "${ETIQUETAS_ESCALA.last().uppercase()} ($ESCALA_MAX)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = animatedAccentColor.copy(alpha = 0.85f)
                )
            }
        }
    }
}