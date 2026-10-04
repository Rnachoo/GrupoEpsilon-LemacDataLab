package com.example.lemacdatalab.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lemacdatalab.ui.theme.*

// pantalla principal del menu (recibe eventos de navegación como parámetros)
@Composable
fun MenuScreen(
    idUsuario: String = "AX-7B99",
    alNavegarNeurodesarrollo: () -> Unit = {},
    alNavegarDBT: () -> Unit = {},
    alNavegarAnimo: () -> Unit = {},
    alNavegarAdicciones: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoClaro)
    ) {
        EncabezadoMenu(idUsuario = idUsuario)

        Spacer(modifier = Modifier.height(24.dp))

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(
                text = "Módulos de Autorregistro",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextoPrincipal
            )
            Text(
                text = "Selecciona el área que deseas registrar hoy.",
                fontSize = 14.sp,
                color = TextoSecundario,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                TarjetaModulo(
                    titulo = "Neurodesarrollo",
                    subtitulo = "Rutinas y autorregulación",
                    icono = Icons.Default.Face,
                    alHacerClic = alNavegarNeurodesarrollo
                )
            }
            item {
                TarjetaModulo(
                    titulo = "Diario DBT",
                    subtitulo = "Habilidades y emociones",
                    icono = Icons.Default.MenuBook,
                    alHacerClic = alNavegarDBT
                )
            }
            item {
                TarjetaModulo(
                    titulo = "Estado de Ánimo",
                    subtitulo = "Escala y contexto",
                    icono = Icons.Default.CloudQueue,
                    alHacerClic = alNavegarAnimo
                )
            }
            item {
                TarjetaModulo(
                    titulo = "Adicciones",
                    subtitulo = "Impulsos y metas",
                    icono = Icons.Default.LocalFireDepartment,
                    alHacerClic = alNavegarAdicciones
                )
            }
        }
    }
}

// dibuja la parte superior curva con los datos del usuario
@Composable
fun EncabezadoMenu(idUsuario: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(VerdePrincipal)
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Usuario",
                        tint = SuperficieBlanca,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SuperficieBlanca.copy(alpha = 0.2f))
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "USUARIO", color = SuperficieBlanca, fontSize = 12.sp)
                        Text(
                            text = "ID: $idUsuario",
                            color = SuperficieBlanca,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notificaciones",
                    tint = SuperficieBlanca,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SuperficieBlanca.copy(alpha = 0.2f))
                        .padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SuperficieBlanca.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = "Racha",
                            tint = SuperficieBlanca
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Cumplimiento actual", color = SuperficieBlanca, fontWeight = FontWeight.Bold)
                            Text(text = "5 días de registro continuo", color = SuperficieBlanca.copy(alpha = 0.8f), fontSize = 12.sp)
                        }
                    }

                    Button(
                        onClick = { /* Pendiente: Acción historial */ },
                        colors = ButtonDefaults.buttonColors(containerColor = SuperficieBlanca.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(text = "Ver historial", color = SuperficieBlanca, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// componente para generar c/u de las 4 opciones de autorregistro
@Composable
fun TarjetaModulo(
    titulo: String,
    subtitulo: String,
    icono: ImageVector,
    alHacerClic: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieBlanca),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { alHacerClic() }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icono,
                    contentDescription = titulo,
                    tint = TextoSecundario,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(FondoClaro)
                        .padding(12.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = titulo, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextoPrincipal)
                    Text(text = subtitulo, fontSize = 14.sp, color = TextoSecundario)
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Ir",
                tint = TextoSecundario
            )
        }
    }
}