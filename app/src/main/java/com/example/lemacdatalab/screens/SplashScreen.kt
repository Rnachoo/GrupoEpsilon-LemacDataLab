package com.example.lemacdatalab.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lemacdatalab.R
import com.example.lemacdatalab.ui.theme.SuperficieBlanca
import com.example.lemacdatalab.ui.theme.VerdePrincipal
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinalizar: () -> Unit
) {
    val progreso = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progreso.animateTo(1f, animationSpec = tween(durationMillis = 450))
        delay(500)
        onFinalizar()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VerdePrincipal)
            .semantics {
                contentDescription = "Pantalla de arranque de Lemac DataLab, autorregistro estructurado"
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .alpha(progreso.value)
                .scale(0.92f + 0.08f * progreso.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(SuperficieBlanca.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "Ícono de Lemac DataLab",
                    modifier = Modifier.size(108.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Lemac DataLab",
                fontFamily = FontFamily.Serif,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = SuperficieBlanca,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Autorregistro estructurado\nPrivacidad desde el diseño",
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = SuperficieBlanca.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
        }
    }
}
