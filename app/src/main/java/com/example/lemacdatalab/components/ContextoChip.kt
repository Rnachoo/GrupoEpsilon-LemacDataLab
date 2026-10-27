package com.example.lemacdatalab.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.lemacdatalab.ui.theme.LemacBorderSoft
import com.example.lemacdatalab.ui.theme.LemacGreenDark
import com.example.lemacdatalab.ui.theme.WarmLowAccent
import com.example.lemacdatalab.ui.theme.WarmLowBg

@Composable
fun ContextoChip(
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icono: ImageVector? = null,
    subtitulo: String? = null
) {
    // Micro-interacción táctil: pulso suave a 1.03f cuando está seleccionado
    val scale by animateFloatAsState(
        targetValue = if (seleccionado) 1.03f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "EscalaChip"
    )

    val bgColor by animateColorAsState(
        targetValue = if (seleccionado) WarmLowBg else Color.White,
        animationSpec = tween(durationMillis = 250),
        label = "FondoChip"
    )

    val borderColor by animateColorAsState(
        targetValue = if (seleccionado) WarmLowAccent else LemacBorderSoft,
        animationSpec = tween(durationMillis = 250),
        label = "BordeChip"
    )

    val iconContainerColor by animateColorAsState(
        targetValue = if (seleccionado) WarmLowAccent.copy(alpha = 0.18f) else Color(0xFFF0F3F1),
        animationSpec = tween(durationMillis = 250),
        label = "IconoContainerChip"
    )

    Surface(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .heightIn(min = 64.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = bgColor,
        border = BorderStroke(
            width = if (seleccionado) 1.8.dp else 1.dp,
            color = borderColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            // Contenedor circular con icono contextual
            Box(
                modifier = Modifier.size(36.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = iconContainerColor,
                    modifier = Modifier.size(36.dp)
                ) {}
                Icon(
                    imageVector = icono ?: if (seleccionado) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                    contentDescription = texto,
                    tint = if (seleccionado) WarmLowAccent else LemacGreenDark.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = texto,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Medium,
                    color = LemacGreenDark,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (!subtitulo.isNullOrBlank()) {
                    Text(
                        text = subtitulo,
                        style = MaterialTheme.typography.labelSmall,
                        color = LemacGreenDark.copy(alpha = 0.65f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}