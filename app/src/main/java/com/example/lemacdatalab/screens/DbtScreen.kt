package com.example.lemacdatalab.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lemacdatalab.components.ContextoChip
import com.example.lemacdatalab.components.IntensidadCard
import com.example.lemacdatalab.native.vibrarAlGuardar
import com.example.lemacdatalab.model.HabilidadDbt
import com.example.lemacdatalab.viewmodel.DbtViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DbtScreen(
    onBack: () -> Unit,
    viewModel: DbtViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val contextoLocal = LocalContext.current // Necesario para hacer vibrar el teléfono

    // Efecto 1: exito al guardar
    LaunchedEffect(state.guardado) {
        if (state.guardado) {
            // Vibracion suave de confirmacion
            vibrarAlGuardar(contextoLocal, esError = false)
            scope.launch {
                snackbarHostState.showSnackbar("Registro DBT guardado correctamente")
            }
            viewModel.onGuardadoConsumido()
            onBack()
        }
    }

    // Efecto 2: error de validacion
    LaunchedEffect(state.habilidadError, state.notasError) {
        if (state.habilidadError != null || state.notasError != null) {
            // Vibracion doble de alerta
            vibrarAlGuardar(contextoLocal, esError = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diario DBT") },
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Button(
                onClick = { viewModel.guardar() },
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(16.dp)
            ) {
                Text("Guardar Registro DBT")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Nivel de Impulso
            Text("Nivel de Impulso", style = MaterialTheme.typography.titleLarge)
            Text(
                text = "Indica la intensidad del impulso antes de usar la habilidad.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            IntensidadCard(
                nivel = state.nivelImpulso,
                onNivelChange = viewModel::onNivelChange
            )

            // Seleccion de Habilidad
            Text("Habilidad Utilizada", style = MaterialTheme.typography.titleLarge)
            HabilidadDbt.entries.chunked(2).forEach { fila ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    fila.forEach { habilidadEnum ->
                        ContextoChip(
                            texto = habilidadEnum.etiqueta,
                            seleccionado = state.habilidad == habilidadEnum,
                            onClick = { viewModel.onHabilidadChange(habilidadEnum) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            // Mensaje de error para la habilidad obligatoria
            state.habilidadError?.let { errorTexto ->
                Text(
                    text = errorTexto,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Notas adicionales
            OutlinedTextField(
                value = state.notas,
                onValueChange = viewModel::onNotasChange,
                label = { Text("Notas de justificación") },
                placeholder = {
                    Text("Describe brevemente cómo aplicaste la habilidad...")
                },
                isError = state.notasError != null,
                supportingText = {
                    Text(state.notasError ?: "${state.notas.length}/${DbtViewModel.MAX_NOTAS}")
                },
                minLines = 4,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}