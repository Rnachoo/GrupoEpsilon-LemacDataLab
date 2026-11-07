package com.example.lemacdatalab.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lemacdatalab.ui.theme.LemacBorderSoft
import com.example.lemacdatalab.ui.theme.LemacGreenDark
import com.example.lemacdatalab.ui.theme.LemacSurfaceWarm
import com.example.lemacdatalab.ui.theme.SuperficieBlanca
import com.example.lemacdatalab.ui.theme.TextoSecundario
import com.example.lemacdatalab.ui.theme.VerdePrincipal
import com.example.lemacdatalab.ui.theme.WarmLowAccent
import com.example.lemacdatalab.ui.theme.WarmLowBg

/**
 * Pantalla reutilizable para módulos en desarrollo.
 * Muestra el nombre del módulo, una descripción clara de su estado "Próximamente"
 * y permite regresar fácilmente a la pantalla anterior.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProximamenteScreen(
    titulo: String,
    subtitulo: String? = null,
    icono: ImageVector = Icons.Outlined.HourglassEmpty,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = LemacSurfaceWarm,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = titulo,
                        fontWeight = FontWeight.Bold,
                        color = LemacGreenDark
                    )
                },
                navigationIcon = {
                    Surface(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(40.dp),
                        shape = CircleShape,
                        color = Color(0xFFE4EAE6)
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = LemacGreenDark
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LemacSurfaceWarm
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SuperficieBlanca),
                border = BorderStroke(1.dp, LemacBorderSoft),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(WarmLowBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icono,
                            contentDescription = titulo,
                            modifier = Modifier.size(40.dp),
                            tint = LemacGreenDark
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = WarmLowBg
                    ) {
                        Text(
                            text = "Próximamente",
                            color = WarmLowAccent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = titulo,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = LemacGreenDark,
                        textAlign = TextAlign.Center
                    )

                    if (!subtitulo.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = subtitulo,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextoSecundario,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Este módulo se encuentra en planificación y estará disponible en una próxima versión de la plataforma.",
                        fontSize = 14.sp,
                        color = TextoSecundario,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = onBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VerdePrincipal
                        )
                    ) {
                        Text(
                            text = "Volver al Menú",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SuperficieBlanca
                        )
                    }
                }
            }
        }
    }
}
