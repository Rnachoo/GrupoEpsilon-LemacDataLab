package com.example.lemacdatalab.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lemacdatalab.components.AlertaValorCriticoDialog
import com.example.lemacdatalab.components.AnimoSeccion
import com.example.lemacdatalab.model.ContextoMood
import com.example.lemacdatalab.native.vibrarAlGuardar
import com.example.lemacdatalab.ui.theme.LemacGreenDark
import com.example.lemacdatalab.ui.theme.LemacSurfaceWarm
import com.example.lemacdatalab.viewmodel.MoodViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodScreen(
    onBack: () -> Unit,
    idRegistro: Int? = null, // null = registro nuevo; con valor = editar ese registro
    viewModel: MoodViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(idRegistro) {
        if (idRegistro != null) viewModel.cargarParaEditar(idRegistro)
    }

    // Al guardar: limpia el estado, avisa y redirige siempre hacia atrás
    LaunchedEffect(state.guardado) {
        if (state.guardado) {
            vibrarAlGuardar(context, esError = false)
            if (!state.alertaCritica) {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        if (state.esEdicion) "Cambios guardados correctamente" else "Evaluación guardada correctamente"
                    )
                }
                viewModel.onGuardadoConsumido()
                onBack()
            }
        }
    }

    if (state.guardado && state.alertaCritica) {
        AlertaValorCriticoDialog(
            onCerrar = {
                viewModel.onGuardadoConsumido()
                onBack()
            }
        )
    }

    Scaffold(
        containerColor = LemacSurfaceWarm,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.esEdicion) "Editar Estado de Ánimo" else "Estado de Ánimo",
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
                .imePadding() // sube el contenido junto con el teclado
                .background(LemacSurfaceWarm)
                // Tocar fuera del campo cierra el teclado
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    })
                }
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            AnimoSeccion(
                state = state,
                onDepresionChange = viewModel::onDepresionChange,
                onElevacionChange = viewModel::onElevacionChange,
                onHorasSuenoChange = viewModel::onHorasSuenoChange,
                onAltoRiesgoChange = viewModel::onAltoRiesgoChange,
                onSintomasPsicoticosChange = viewModel::onSintomasPsicoticosChange,
                onContextoChange = viewModel::onContextoChange,
                onContextoOtroChange = viewModel::onContextoOtroChange,
                onNotasChange = viewModel::onNotasChange
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val hayError = state.contexto == null ||
                            (state.contexto == ContextoMood.OTRO && state.contextoOtro.isBlank()) ||
                            state.notasError != null ||
                            !state.horasSuenoValidas
                    if (hayError) {
                        vibrarAlGuardar(context, esError = true)
                    }

                    viewModel.guardar()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = LemacGreenDark,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (state.esEdicion) "Guardar Cambios" else "Guardar Evaluación",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}