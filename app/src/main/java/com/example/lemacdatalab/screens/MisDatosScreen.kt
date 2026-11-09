package com.example.lemacdatalab.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lemacdatalab.native.compartirRegistrosCsv
import com.example.lemacdatalab.ui.theme.LemacBorderSoft
import com.example.lemacdatalab.ui.theme.LemacGreenDark
import com.example.lemacdatalab.ui.theme.LemacSurfaceWarm
import com.example.lemacdatalab.ui.theme.RojoError
import com.example.lemacdatalab.ui.theme.SuperficieBlanca
import com.example.lemacdatalab.ui.theme.TextoPrincipal
import com.example.lemacdatalab.ui.theme.TextoSecundario
import com.example.lemacdatalab.ui.theme.VerdePrincipal
import com.example.lemacdatalab.viewmodel.MisDatosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisDatosScreen(
    onBack: () -> Unit,
    viewModel: MisDatosViewModel = viewModel()
) {
    val total by viewModel.totalRegistros.collectAsState()
    val context = LocalContext.current
    var confirmando by remember { mutableStateOf(false) }
    var confirmandoExportacion by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = LemacSurfaceWarm,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mis datos",
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Tienes $total registros guardados en este dispositivo.",
                fontSize = 14.sp,
                color = TextoSecundario
            )

            AccionDatosCard(
                titulo = "Exportar mis datos",
                descripcion = "Comparte tus registros en formato CSV.",
                icono = Icons.Default.Share,
                color = VerdePrincipal,
                habilitado = total > 0,
                alHacerClic = { confirmandoExportacion = true }
            )

            AccionDatosCard(
                titulo = "Eliminar todos mis registros",
                descripcion = "Borra tu historial de este dispositivo.",
                icono = Icons.Default.Delete,
                color = RojoError,
                habilitado = total > 0,
                alHacerClic = { confirmando = true }
            )
        }
    }

    if (confirmandoExportacion) {
        AlertDialog(
            onDismissRequest = { confirmandoExportacion = false },
            containerColor = SuperficieBlanca,
            title = {
                Text(
                    text = "Exportar registros",
                    fontWeight = FontWeight.Bold,
                    color = TextoPrincipal
                )
            },
            text = {
                Text(
                    text = "Este archivo contiene datos sensibles simulados. ¿Deseas continuar con la exportación?",
                    fontSize = 14.sp,
                    color = TextoSecundario
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmandoExportacion = false
                    viewModel.exportar { csv -> compartirRegistrosCsv(context, csv) }
                }) {
                    Text("Confirmar", fontWeight = FontWeight.Bold, color = VerdePrincipal)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmandoExportacion = false }) {
                    Text("Cancelar", color = TextoSecundario)
                }
            }
        )
    }

    if (confirmando) {
        AlertDialog(
            onDismissRequest = { confirmando = false },
            containerColor = SuperficieBlanca,
            title = {
                Text(
                    text = "¿Eliminar todos tus registros?",
                    fontWeight = FontWeight.Bold,
                    color = TextoPrincipal
                )
            },
            text = {
                Text(
                    text = "Se borrarán $total registros. Esta acción no se puede deshacer.",
                    fontSize = 14.sp,
                    color = TextoSecundario
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarTodo()
                    confirmando = false
                }) {
                    Text("Eliminar todo", fontWeight = FontWeight.Bold, color = RojoError)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmando = false }) {
                    Text("Cancelar", color = TextoSecundario)
                }
            }
        )
    }
}

@Composable
private fun AccionDatosCard(
    titulo: String,
    descripcion: String,
    icono: ImageVector,
    color: Color,
    habilitado: Boolean,
    alHacerClic: () -> Unit
) {
    Card(
        onClick = alHacerClic,
        enabled = habilitado,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (habilitado) 1f else 0.5f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = SuperficieBlanca,
            disabledContainerColor = SuperficieBlanca
        ),
        border = BorderStroke(1.dp, LemacBorderSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icono,
                contentDescription = titulo,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = titulo, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
                Text(text = descripcion, fontSize = 14.sp, color = TextoSecundario)
            }
        }
    }
}
