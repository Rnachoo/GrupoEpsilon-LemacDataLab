package com.example.lemacdatalab.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lemacdatalab.components.GraficoEvolucionAmbitoCard
import com.example.lemacdatalab.model.AMBITOS_EVOLUCION
import com.example.lemacdatalab.model.ESCALA_MAX
import com.example.lemacdatalab.model.PERIODOS_CARTA_ANIMO
import com.example.lemacdatalab.model.RegistroHistorial
import com.example.lemacdatalab.model.SerieEvolucionAmbito
import com.example.lemacdatalab.viewmodel.FiltroTipoRegistro
import com.example.lemacdatalab.viewmodel.HistorialViewModel

// Paleta cromática específica para el Historial
private val ColorDbt = Color(0xFF8EAF9D) // Verde Salvia
private val ColorMood = Color(0xFFD9822B) // Terracota Cálido
private val ColorNeuro = Color(0xFF7B8FB5) // Azul grisáceo
private val ColorAdicciones = Color(0xFFC85A32) // Terracota intenso
private val ColorFondoNotas = Color(0xFFF8FAF9) // Fondo de lectura
private val VerdePrincipal = Color(0xFF2B4C3B) // Para el chip seleccionado
private val BordeSuave = Color(0xFFE4E4E0)

// Ámbitos que se pueden editar desde el historial / evolución
private val AREAS_EDITABLES = setOf("animo", "dbt", "neurodesarrollo", "adicciones")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    onBack: () -> Unit,
    onEditar: (RegistroHistorial) -> Unit,
    viewModel: HistorialViewModel = viewModel()
) {
    val registros by viewModel.registros.collectAsState()
    val filtroActual by viewModel.filtroActual.collectAsState()
    val conteos by viewModel.conteos.collectAsState()
    val seriesPorAmbito by viewModel.seriesPorAmbito.collectAsState()
    val diasCarta by viewModel.diasCarta.collectAsState()

    var registroAEliminar by remember { mutableStateOf<RegistroHistorial?>(null) }

    registroAEliminar?.let { registro ->
        DialogoConfirmarEliminar(
            registro = registro,
            onConfirmar = {
                viewModel.eliminar(registro.idRegistro)
                registroAEliminar = null
            },
            onCancelar = { registroAEliminar = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial y evolución") },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(40.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            BarraFiltrosAmbito(
                filtroActual = filtroActual,
                conteos = conteos,
                onSeleccionar = { viewModel.setFiltro(it) }
            )

            when (val lista = registros) {
                null -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item(key = "selector_periodo") {
                            SelectorPeriodoEvolucion(
                                diasSeleccionados = diasCarta,
                                onCambiarDias = { viewModel.setDiasCarta(it) }
                            )
                        }

                        val areasVisibles = areasParaFiltro(filtroActual)
                        items(areasVisibles, key = { "grafico_$it" }) { area ->
                            seriesPorAmbito[area]?.let { serie ->
                                GraficoEvolucionAmbitoCard(
                                    serie = serie,
                                    mostrarSelectorDias = false
                                )
                            }
                        }

                        if (lista.isEmpty()) {
                            item(key = "estado_vacio") {
                                EstadoVacioHistorial(
                                    filtroActual = filtroActual,
                                    onLimpiarFiltro = { viewModel.setFiltro(FiltroTipoRegistro.TODOS) }
                                )
                            }
                        } else {
                            items(lista, key = { it.idRegistro }) { registro ->
                                TarjetaRegistro(
                                    registro = registro,
                                    onEditar = { onEditar(registro) },
                                    onEliminar = { registroAEliminar = registro }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun areasParaFiltro(filtro: FiltroTipoRegistro): List<String> = when (filtro) {
    FiltroTipoRegistro.TODOS -> AMBITOS_EVOLUCION
    FiltroTipoRegistro.MOOD -> listOf("animo")
    FiltroTipoRegistro.DBT -> listOf("dbt")
    FiltroTipoRegistro.NEURO -> listOf("neurodesarrollo")
    FiltroTipoRegistro.ADICCIONES -> listOf("adicciones")
}

@Composable
fun SelectorPeriodoEvolucion(
    diasSeleccionados: Int,
    onCambiarDias: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Período:",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = VerdePrincipal
        )
        PERIODOS_CARTA_ANIMO.forEach { opcionDias ->
            val seleccionado = diasSeleccionados == opcionDias
            FilterChip(
                selected = seleccionado,
                onClick = { onCambiarDias(opcionDias) },
                label = { Text("$opcionDias días") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = VerdePrincipal,
                    selectedLabelColor = Color.White
                ),
                shape = CircleShape
            )
        }
    }
}

@Composable
fun BarraFiltrosAmbito(
    filtroActual: FiltroTipoRegistro,
    conteos: Map<FiltroTipoRegistro, Int>,
    onSeleccionar: (FiltroTipoRegistro) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FiltroTipoRegistro.entries.forEach { filtroEnum ->
            val estaActivo = filtroActual == filtroEnum
            val conteo = conteos[filtroEnum] ?: 0
            val titulo = when (filtroEnum) {
                FiltroTipoRegistro.TODOS -> "Todos"
                FiltroTipoRegistro.MOOD -> "Ánimo"
                FiltroTipoRegistro.DBT -> "DBT"
                FiltroTipoRegistro.NEURO -> "Neuro"
                FiltroTipoRegistro.ADICCIONES -> "Adicciones"
            }
            val icono = when (filtroEnum) {
                FiltroTipoRegistro.TODOS -> Icons.AutoMirrored.Filled.List
                FiltroTipoRegistro.MOOD -> Icons.Default.CloudQueue
                FiltroTipoRegistro.DBT -> Icons.AutoMirrored.Filled.MenuBook
                FiltroTipoRegistro.NEURO -> Icons.Default.Face
                FiltroTipoRegistro.ADICCIONES -> Icons.Default.LocalFireDepartment
            }

            FilterChip(
                selected = estaActivo,
                onClick = { onSeleccionar(filtroEnum) },
                label = { Text("$titulo ($conteo)") },
                leadingIcon = {
                    Icon(
                        imageVector = if (estaActivo) Icons.Default.Check else icono,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = VerdePrincipal,
                    selectedLabelColor = Color.White,
                    selectedLeadingIconColor = Color.White
                ),
                shape = CircleShape
            )
        }
    }
}

@Composable
fun EstadoVacioHistorial(
    filtroActual: FiltroTipoRegistro,
    onLimpiarFiltro: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = when (filtroActual) {
                    FiltroTipoRegistro.MOOD -> Icons.Default.CloudQueue
                    FiltroTipoRegistro.NEURO -> Icons.Default.Face
                    FiltroTipoRegistro.ADICCIONES -> Icons.Default.LocalFireDepartment
                    else -> Icons.AutoMirrored.Filled.MenuBook
                },
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Aún no tienes registros en esta categoría. ¡Cada pequeño paso cuenta cuando decidas comenzar!",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (filtroActual != FiltroTipoRegistro.TODOS) {
                OutlinedButton(onClick = onLimpiarFiltro) {
                    Text("Limpiar filtro")
                }
            }
        }
    }
}

@Composable
fun DialogoConfirmarEliminar(
    registro: RegistroHistorial,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Eliminar registro") },
        text = {
            Text(
                "Se eliminará el registro de ${tituloArea(registro.tipoArea)} del " +
                        "${formatearFecha(registro.fechaHoraCreacion)}. " +
                        "Esta acción no se puede deshacer."
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirmar) {
                Text("Eliminar", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun TarjetaRegistro(
    registro: RegistroHistorial,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    val colorFranja = when (registro.tipoArea) {
        "animo" -> ColorMood
        "dbt" -> ColorDbt
        "neurodesarrollo" -> ColorNeuro
        "adicciones" -> ColorAdicciones
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    val iconoArea = when (registro.tipoArea) {
        "animo" -> Icons.Default.CloudQueue
        "neurodesarrollo" -> Icons.Default.Face
        "adicciones" -> Icons.Default.LocalFireDepartment
        else -> Icons.AutoMirrored.Filled.MenuBook
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BordeSuave),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(colorFranja)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(colorFranja.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = iconoArea, contentDescription = null, tint = colorFranja)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = tituloArea(registro.tipoArea),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (registro.estadoRegistro == "editado") {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFFFDF6EC),
                                        border = BorderStroke(1.dp, Color(0xFFE0C49F))
                                    ) {
                                        Text(
                                            text = "Editado",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFFD9822B),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = formatearFecha(registro.fechaHoraCreacion),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        if (registro.tipoArea in AREAS_EDITABLES) {
                            IconButton(
                                onClick = onEditar,
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(0xFFFDF6EC), RoundedCornerShape(8.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Editar registro",
                                    tint = Color(0xFFD9822B),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        IconButton(
                            onClick = onEliminar,
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFFFDECE8), RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar registro",
                                tint = Color(0xFFC85A32),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Píldoras de Metadatos en escala común 0 a 4
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val labelIntensidad = when (registro.tipoArea) {
                        "animo" -> "Resumen: ${registro.nivelIntensidad ?: 0} / $ESCALA_MAX"
                        "neurodesarrollo" -> "Intensidad: ${registro.intensidadNeuro ?: 0} / $ESCALA_MAX"
                        "adicciones" -> "Craving: ${registro.craving ?: 0} / $ESCALA_MAX"
                        else -> "Intensidad emocional: ${registro.nivelImpulso ?: 0} / $ESCALA_MAX"
                    }
                    val labelContexto = when (registro.tipoArea) {
                        "animo" -> registro.contexto
                        "neurodesarrollo" -> registro.estrategia
                        "adicciones" -> registro.accionProtectora ?: registro.desencadenante
                        else -> registro.habilidad
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        border = BorderStroke(1.dp, BordeSuave)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Circle, contentDescription = null, tint = colorFranja, modifier = Modifier.size(8.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = labelIntensidad, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                        }
                    }

                    if (labelContexto != null) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF2F5F3),
                            border = BorderStroke(1.dp, BordeSuave)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = if (registro.tipoArea == "animo") Icons.Outlined.WorkOutline else Icons.Outlined.Spa,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = labelContexto, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                // Detalle específico por ámbito
                when (registro.tipoArea) {
                    "animo" -> {
                        val dep = registro.depresion ?: 0
                        val ele = registro.elevacion ?: 0
                        val sueno = registro.horasSueno?.let { " · Sueño: ${it}h" }.orEmpty()
                        Text(
                            text = "Depresión: -$dep · Elevación: +$ele$sueno",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    "neurodesarrollo" -> {
                        val detalle = listOfNotNull(registro.tipoEntrada, registro.resultadoPercibido)
                            .joinToString(" · ")
                        if (detalle.isNotEmpty()) {
                            Text(
                                text = detalle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    "adicciones" -> {
                        val detalle = listOfNotNull(registro.desencadenante, registro.estadoObjetivo)
                            .joinToString(" · ")
                        if (detalle.isNotEmpty()) {
                            Text(
                                text = detalle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                registro.notasOpcionales?.let { notas ->
                    Surface(
                        color = ColorFondoNotas,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "\"$notas\"",
                            style = MaterialTheme.typography.bodyLarge,
                            fontStyle = FontStyle.Italic,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

fun tituloArea(tipoArea: String): String = when (tipoArea) {
    "animo" -> "Estado de Ánimo"
    "dbt" -> "Diario DBT"
    "neurodesarrollo" -> "Neurodesarrollo"
    "adicciones" -> "Adicciones"
    else -> tipoArea
}

private fun formatearFecha(fechaHora: String): String {
    val partes = fechaHora.split(" ")
    val fecha = partes.firstOrNull()?.split("-")
    if (partes.size != 2 || fecha == null || fecha.size != 3) return fechaHora
    return "${fecha[2]}/${fecha[1]}/${fecha[0]} ${partes[1].take(5)}"
}
