package com.example.lemacdatalab.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lemacdatalab.components.ContextoChip
import com.example.lemacdatalab.components.IntensidadCard
import com.example.lemacdatalab.model.ContextoMood
import com.example.lemacdatalab.viewmodel.MoodViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodScreen(
    onBack: () -> Unit,
    viewModel: MoodViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Al guardar: limpia el formulario y muestra el mensaje de éxito
    LaunchedEffect(state.guardado) {
        if (state.guardado) {
            viewModel.onGuardadoConsumido()
            scope.launch {
                snackbarHostState.showSnackbar("Evaluación guardada correctamente")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estado de Ánimo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←")
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
                    .padding(16.dp)
            ) {
                Text("Guardar Evaluación")
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
            Text("Escala de Intensidad", style = MaterialTheme.typography.titleLarge)
            IntensidadCard(
                nivel = state.nivel,
                onNivelChange = viewModel::onNivelChange
            )

            Text("Contexto Detonante Principal", style = MaterialTheme.typography.titleLarge)
            ContextoMood.entries.chunked(2).forEach { fila ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    fila.forEach { contexto ->
                        ContextoChip(
                            texto = contexto.etiqueta,
                            seleccionado = state.contexto == contexto,
                            onClick = { viewModel.onContextoChange(contexto) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            state.contextoError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            OutlinedTextField(
                value = state.notas,
                onValueChange = viewModel::onNotasChange,
                label = { Text("Notas (opcional)") },
                placeholder = {
                    Text("Agregue cualquier observación libre sobre su estado de ánimo...")
                },
                isError = state.notasError != null,
                supportingText = {
                    Text(state.notasError ?: "${state.notas.length}/${MoodViewModel.MAX_NOTAS}")
                },
                minLines = 4,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}