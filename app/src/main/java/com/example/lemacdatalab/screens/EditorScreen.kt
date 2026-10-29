package com.example.lemacdatalab.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lemacdatalab.model.ContenidoEducativo
import com.example.lemacdatalab.viewmodel.ContenidoViewModel

// colores del módulo Editor
private val VerdePrincipal = Color(0xFF2B4C3B)
private val BordeSuave = Color(0xFFE4E4E0)
private val FondoCard = Color.White
private val DoradoEstrella = Color(0xFFD9822B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    onCerrarSesion: () -> Unit,
    viewModel: ContenidoViewModel = viewModel(factory = ContenidoViewModel.Factory)
) {
    val listaContenidos by viewModel.contenidos.collectAsState()

    var mostrarDialogo by remember { mutableStateOf(false) }
    var contenidoAEditar by remember { mutableStateOf<ContenidoEducativo?>(null) }
    var contenidoAEliminar by remember { mutableStateOf<ContenidoEducativo?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Panel de Contenidos",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Rol: Editor de Contenidos (EDT)",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onCerrarSesion) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VerdePrincipal)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    contenidoAEditar = null
                    mostrarDialogo = true
                },
                containerColor = VerdePrincipal,
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Nueva cápsula")
            }
        }
    ) { paddingValores ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValores)
                .background(Color(0xFFF9F9F9))
        ) {
            if (listaContenidos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Cargando catálogo psicoeducativo...",
                        color = Color.Gray,
                        fontStyle = FontStyle.Italic
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(listaContenidos, key = { it.id }) { item ->
                        TarjetaContenido(
                            contenido = item,
                            onMarcarDia = { viewModel.marcarLecturaDelDia(item) },
                            onEditar = {
                                contenidoAEditar = item
                                mostrarDialogo = true
                            },
                            onEliminar = { contenidoAEliminar = item }
                        )
                    }
                }
            }
        }
    }

    if (mostrarDialogo) {
        DialogoFormularioContenido(
            contenidoInicial = contenidoAEditar,
            onDescartar = { mostrarDialogo = false },
            onGuardar = { titulo, extracto, fuente, ambito ->
                val guardadoExitoso = viewModel.guardar(
                    id = contenidoAEditar?.id,
                    titulo = titulo,
                    extracto = extracto,
                    fuente = fuente,
                    ambito = ambito
                )
                if (guardadoExitoso) {
                    mostrarDialogo = false
                }
                guardadoExitoso
            }
        )
    }

    // confirmación para eliminar
    contenidoAEliminar?.let { contenido ->
        AlertDialog(
            onDismissRequest = { contenidoAEliminar = null },
            title = {
                Text(
                    text = "Confirmar eliminación",
                    fontWeight = FontWeight.Bold,
                    color = VerdePrincipal
                )
            },
            text = {
                Text(
                    text = "¿Estás seguro de que deseas eliminar la cápsula \"${contenido.titulo}\"? Esta acción no se puede deshacer.",
                    color = Color.DarkGray
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.eliminar(contenido)
                        contenidoAEliminar = null
                    }
                ) {
                    Text("Eliminar", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { contenidoAEliminar = null }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
private fun TarjetaContenido(
    contenido: ContenidoEducativo,
    onMarcarDia: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = FondoCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, BordeSuave)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Etiqueta de Ámbito
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFF2F5F3),
                    modifier = Modifier.border(1.dp, BordeSuave, CircleShape)
                ) {
                    Text(
                        text = contenido.ambito,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = VerdePrincipal
                    )
                }

                // Acciones: Estrella (Lectura del día), Editar y Eliminar
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                    // Botón Estrella
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (contenido.esLecturaDelDia) Color(0xFFFDF6EC) else Color(0xFFF2F5F3))
                            .clickable { onMarcarDia() }
                    ) {
                        Icon(
                            imageVector = if (contenido.esLecturaDelDia) Icons.Default.Star else Icons.Outlined.StarOutline,
                            contentDescription = "Lectura del día",
                            tint = if (contenido.esLecturaDelDia) DoradoEstrella else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Botón Editar
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFDF6EC))
                            .clickable { onEditar() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = Color(0xFFD9822B),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Botón Eliminar
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFDECE8))
                            .clickable { onEliminar() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = Color(0xFFC85A32),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = contenido.titulo,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF1A3B2B)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF8FAF9),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "\"${contenido.extracto}\"",
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Italic,
                    lineHeight = 20.sp,
                    color = Color(0xFF333333),
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Fuente: ${contenido.fuente}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun DialogoFormularioContenido(
    contenidoInicial: ContenidoEducativo?,
    onDescartar: () -> Unit,
    onGuardar: (titulo: String, extracto: String, fuente: String, ambito: String) -> Boolean
) {
    val ambitosDisponibles = listOf("DBT", "Estado de Ánimo", "Neurodesarrollo", "Adicciones")

    var titulo by remember { mutableStateOf(contenidoInicial?.titulo ?: "") }
    var extracto by remember { mutableStateOf(contenidoInicial?.extracto ?: "") }
    var fuente by remember { mutableStateOf(contenidoInicial?.fuente ?: "") }
    var ambitoSeleccionado by remember { mutableStateOf(contenidoInicial?.ambito ?: ambitosDisponibles.first()) }
    var mensajeError by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDescartar,
        title = {
            Text(
                text = if (contenidoInicial == null) "Nueva Cápsula" else "Editar Cápsula",
                fontWeight = FontWeight.Bold,
                color = VerdePrincipal
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Ámbito clínico:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.DarkGray
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    items(ambitosDisponibles) { ambito ->
                        FilterChip(
                            selected = (ambito == ambitoSeleccionado),
                            onClick = { ambitoSeleccionado = ambito },
                            label = { Text(text = ambito, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VerdePrincipal,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título de la lectura") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = extracto,
                    onValueChange = { extracto = it },
                    label = { Text("Extracto / Contenido") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = fuente,
                    onValueChange = { fuente = it },
                    label = { Text("Fuente bibliográfica") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (mensajeError.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = mensajeError,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val exito = onGuardar(titulo, extracto, fuente, ambitoSeleccionado)
                    if (!exito) {
                        mensajeError = "Todos los campos son obligatorios"
                    }
                }
            ) {
                Text("Guardar", fontWeight = FontWeight.Bold, color = VerdePrincipal)
            }
        },
        dismissButton = {
            TextButton(onClick = onDescartar) {
                Text("Cancelar", color = Color.Gray)
            }
        }
    )
}