package com.example.lemacdatalab.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lemacdatalab.components.ContenidoDiarioCard
import com.example.lemacdatalab.components.LecturaModalDialog
import com.example.lemacdatalab.data.SesionUsuario
import com.example.lemacdatalab.model.RegistroHistorial
import com.example.lemacdatalab.native.NotificacionHelper
import com.example.lemacdatalab.native.compartirLecturaDiaria
import com.example.lemacdatalab.ui.theme.*
import com.example.lemacdatalab.viewmodel.ContenidoViewModel
import com.example.lemacdatalab.viewmodel.MenuViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val AmbarCalido = Color(0xFFFBBF24)

// pantalla principal del menu (recibe eventos de navegación como parámetros)
@Composable
fun MenuScreen(
    // Ahora lee dinámicamente el ID del usuario que inició sesión en SesionUsuario
    idUsuario: String = SesionUsuario.usuarioActual?.identificadorSintetico ?: "AX-7B99",
    alNavegarNeurodesarrollo: () -> Unit = {},
    alNavegarDBT: () -> Unit = {},
    alNavegarAnimo: () -> Unit = {},
    alNavegarAdicciones: () -> Unit = {},
    alNavegarHistorial: () -> Unit = {},
    contenidoViewModel: ContenidoViewModel = viewModel(factory = ContenidoViewModel.Factory),
    menuViewModel: MenuViewModel = viewModel()
) {
    val contenidos by contenidoViewModel.contenidos.collectAsState()
    val state by menuViewModel.uiState.collectAsState()
    // la consulta deja primero la lectura marcada; si no hay ninguna, queda la más reciente
    val lecturaDiaria = contenidos.firstOrNull()
    var lecturaAbierta by remember { mutableStateOf(false) }
    var guiaAbierta by remember { mutableStateOf(false) }
    var bandejaAbierta by remember { mutableStateOf(false) }
    var permisoSolicitado by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    // si el permiso se rechaza, los avisos igual quedan en la bandeja de la campana
    val permisoNotificaciones = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    // se pide después de cerrar la guía de bienvenida, una vez por visita al menú
    LaunchedEffect(state.onboardingVisto, guiaAbierta) {
        if (state.onboardingVisto && !guiaAbierta && !permisoSolicitado &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            permisoSolicitado = true
            permisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoClaro)
    ) {
        EncabezadoMenu(
            idUsuario = idUsuario,
            racha = state.racha,
            animarAyuda = !state.onboardingVisto,
            hayRegistroHoy = state.hayRegistroHoy,
            alAbrirAyuda = {
                guiaAbierta = true
                menuViewModel.marcarOnboardingVisto()
            },
            alAbrirBandeja = { bandejaAbierta = true },
            alVerHistorial = alNavegarHistorial
        )

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
            if (lecturaDiaria != null) {
                item {
                    ContenidoDiarioCard(
                        contenido = lecturaDiaria,
                        onClick = { lecturaAbierta = true },
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }

    if (lecturaAbierta && lecturaDiaria != null) {
        LecturaModalDialog(
            contenido = lecturaDiaria,
            onCerrar = { lecturaAbierta = false },
            onCompartir = { compartirLecturaDiaria(context, lecturaDiaria) }
        )
    }

    if (guiaAbierta) {
        GuiaBienvenidaDialog(onCerrar = { guiaAbierta = false })
    }

    if (bandejaAbierta) {
        BandejaAlertasDialog(
            eventos = state.ultimosEventos,
            racha = state.racha,
            notificacionesActivas = NotificacionHelper.notificacionesActivas(context),
            onCerrar = { bandejaAbierta = false }
        )
    }
}

// dibuja la parte superior curva con los datos del usuario
@Composable
fun EncabezadoMenu(
    idUsuario: String,
    racha: Int,
    animarAyuda: Boolean,
    hayRegistroHoy: Boolean,
    alAbrirAyuda: () -> Unit,
    alAbrirBandeja: () -> Unit,
    alVerHistorial: () -> Unit = {}
) {
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

                Row(verticalAlignment = Alignment.CenterVertically) {
                    BotonAyuda(animado = animarAyuda, alHacerClic = alAbrirAyuda)
                    Spacer(modifier = Modifier.width(8.dp))
                    BadgedBox(
                        badge = {
                            if (hayRegistroHoy) Badge(containerColor = AmbarCalido)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notificaciones",
                            tint = SuperficieBlanca,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SuperficieBlanca.copy(alpha = 0.2f))
                                .clickable { alAbrirBandeja() }
                                .padding(8.dp)
                        )
                    }
                }
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
                            Text(
                                text = if (racha == 1) "1 día de registro continuo" else "$racha días de registro continuo",
                                color = SuperficieBlanca.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Button(
                        onClick = alVerHistorial,
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

// botón de ayuda: salta y brilla hasta que el usuario abre la guía por primera vez
@Composable
private fun BotonAyuda(animado: Boolean, alHacerClic: () -> Unit) {
    var desplazamiento = 0f
    var escalaHalo = 1f
    var opacidadHalo = 0f
    if (animado) {
        val transicion = rememberInfiniteTransition(label = "botonAyuda")
        desplazamiento = transicion.animateFloat(
            initialValue = 0f,
            targetValue = -5f,
            animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
            label = "saltito"
        ).value
        escalaHalo = transicion.animateFloat(
            initialValue = 1f,
            targetValue = 1.35f,
            animationSpec = infiniteRepeatable(tween(900)),
            label = "escalaHalo"
        ).value
        opacidadHalo = transicion.animateFloat(
            initialValue = 0.6f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(tween(900)),
            label = "opacidadHalo"
        ).value
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.offset(y = desplazamiento.dp)
    ) {
        if (animado) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .graphicsLayer {
                        scaleX = escalaHalo
                        scaleY = escalaHalo
                        alpha = opacidadHalo
                    }
                    .clip(CircleShape)
                    .background(AmbarCalido)
            )
        }
        Icon(
            imageVector = Icons.Default.QuestionMark,
            contentDescription = "Guía de uso",
            tint = SuperficieBlanca,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(SuperficieBlanca.copy(alpha = 0.2f))
                .clickable { alHacerClic() }
                .padding(8.dp)
        )
    }
}

@Composable
private fun GuiaBienvenidaDialog(onCerrar: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCerrar,
        containerColor = SuperficieBlanca,
        icon = { Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = AmbarCalido) },
        title = {
            Text(text = "¿Cómo usar LemacDataLab?", fontWeight = FontWeight.Bold, color = TextoPrincipal)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PasoGuia(numero = 1, titulo = "Elige un módulo", detalle = "Registra tu ánimo o habilidad.")
                PasoGuia(numero = 2, titulo = "Evalúa sin juicios", detalle = "Usa la escala del 1 al 10.")
                PasoGuia(numero = 3, titulo = "Mira tu avance", detalle = "Filtra tu historial cuando quieras.")
            }
        },
        confirmButton = {
            TextButton(onClick = onCerrar) {
                Text(text = "¡Entendido, empezar!", fontWeight = FontWeight.Bold, color = VerdePrincipal)
            }
        }
    )
}

@Composable
private fun PasoGuia(numero: Int, titulo: String, detalle: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = "$numero",
            color = SuperficieBlanca,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier
                .clip(CircleShape)
                .background(VerdePrincipal)
                .padding(horizontal = 8.dp, vertical = 2.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = titulo, fontWeight = FontWeight.Bold, color = TextoPrincipal)
            Text(text = detalle, fontSize = 14.sp, color = TextoSecundario)
        }
    }
}

// últimos registros del usuario y un mensaje de ánimo según su racha
@Composable
private fun BandejaAlertasDialog(
    eventos: List<RegistroHistorial>,
    racha: Int,
    notificacionesActivas: Boolean,
    onCerrar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCerrar,
        containerColor = SuperficieBlanca,
        title = {
            Text(text = "Actividad reciente", fontWeight = FontWeight.Bold, color = TextoPrincipal)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (eventos.isEmpty()) {
                    Text(
                        text = "Aún no tienes registros. Cuando completes un formulario aparecerá aquí.",
                        color = TextoSecundario
                    )
                } else {
                    eventos.forEach { evento ->
                        Text(
                            text = "${formatearEvento(evento.fechaHoraCreacion)} — Completaste ${tituloArea(evento.tipoArea)}",
                            fontSize = 14.sp,
                            color = TextoPrincipal
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = WarmHighAccent
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (racha) {
                            0 -> "Cada pequeño registro cuenta. Hoy es un buen día para empezar."
                            1 -> "Racha actual: 1 día continuo. ¡Sigue así!"
                            else -> "Racha actual: $racha días continuos. ¡Sigue así!"
                        },
                        fontSize = 14.sp,
                        color = TextoPrincipal
                    )
                }
                if (!notificacionesActivas) {
                    Text(
                        text = "Las notificaciones del sistema están desactivadas. Tus avisos quedan guardados en esta bandeja.",
                        fontSize = 12.sp,
                        color = TextoSecundario
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onCerrar) {
                Text(text = "Cerrar", color = VerdePrincipal)
            }
        }
    )
}

// "2026-10-30 13:37:00" -> "Hoy 13:37" o "29/10 13:37"
private fun formatearEvento(fechaHora: String): String {
    val hoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val fecha = fechaHora.take(10)
    val hora = fechaHora.drop(11).take(5)
    val dia = if (fecha == hoy) "Hoy" else "${fecha.drop(8)}/${fecha.drop(5).take(2)}"
    return "$dia $hora"
}
