package com.example.lemacdatalab.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lemacdatalab.components.ContextoChip
import com.example.lemacdatalab.components.IntensidadCard
import com.example.lemacdatalab.model.ContextoMood
import com.example.lemacdatalab.native.vibrarAlGuardar
import com.example.lemacdatalab.ui.theme.LemacBorderSoft
import com.example.lemacdatalab.ui.theme.LemacGreenDark
import com.example.lemacdatalab.ui.theme.LemacNoteBg
import com.example.lemacdatalab.ui.theme.LemacSurfaceWarm
import com.example.lemacdatalab.ui.theme.WarmLowAccent
import com.example.lemacdatalab.viewmodel.MoodViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Modo edición: carga el registro en el formulario
    LaunchedEffect(idRegistro) {
        if (idRegistro != null) viewModel.cargarParaEditar(idRegistro)
    }

    // Al guardar: si se estaba editando vuelve al historial; si era nuevo, limpia el formulario y avisa
    LaunchedEffect(state.guardado) {
        if (state.guardado) {
            val eraEdicion = state.esEdicion
            vibrarAlGuardar(context, esError = false)
            viewModel.onGuardadoConsumido()
            if (eraEdicion) {
                onBack()
            } else {
                scope.launch {
                    snackbarHostState.showSnackbar("Evaluación guardada correctamente")
                }
            }
        }
    }

    // Vibración doble cuando falla la validación
    LaunchedEffect(state.intentosFallidos) {
        if (state.intentosFallidos > 0) {
            vibrarAlGuardar(context, esError = true)
        }
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
            Text(
                text = "Escala de Intensidad",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = LemacGreenDark
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Tómate un momento para identificar con qué fuerza sientes esta emoción.",
                style = MaterialTheme.typography.bodyMedium,
                color = LemacGreenDark.copy(alpha = 0.75f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // IntensidadCard trabaja con Float; el ViewModel guarda el nivel como Int
            IntensidadCard(
                valor = state.nivel.toFloat(),
                onValorChange = { viewModel.onNivelChange(it.roundToInt()) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Microcopy empático
            Text(
                text = "¿Qué área influyó más en cómo te sientes hoy?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LemacGreenDark
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ContextoMood.entries.chunked(2).forEach { fila ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        fila.forEach { contexto ->
                            ContextoChip(
                                texto = contexto.etiqueta,
                                seleccionado = state.contexto == contexto,
                                icono = iconoParaContextoMood(contexto),
                                onClick = { viewModel.onContextoChange(contexto) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (fila.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Error de validación: contexto obligatorio
            state.contextoError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Campo de Notas Acogedor
            OutlinedTextField(
                value = state.notas,
                onValueChange = viewModel::onNotasChange,
                label = {
                    Text("Espacio libre: ¿Quieres desahogarte o anotar algún detalle? (Opcional)")
                },
                isError = state.notasError != null,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(135.dp)
                    .bringIntoViewRequester(bringIntoViewRequester)
                    .onFocusEvent { focus ->
                        if (focus.isFocused) {
                            scope.launch {
                                delay(300) // espera a que el teclado termine de abrirse
                                bringIntoViewRequester.bringIntoView()
                            }
                        }
                    },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = LemacNoteBg,
                    unfocusedContainerColor = LemacNoteBg,
                    errorContainerColor = LemacNoteBg,
                    focusedBorderColor = WarmLowAccent,
                    unfocusedBorderColor = LemacBorderSoft,
                    focusedLabelColor = LemacGreenDark,
                    unfocusedLabelColor = LemacGreenDark.copy(alpha = 0.7f),
                    cursorColor = WarmLowAccent
                ),
                maxLines = 5
            )

            // Contador, o el mensaje de error si se pasó del máximo
            Text(
                text = state.notasError ?: "${state.notas.length}/${MoodViewModel.MAX_NOTAS}",
                style = MaterialTheme.typography.labelSmall,
                color = if (state.notasError != null) MaterialTheme.colorScheme.error
                else LemacGreenDark.copy(alpha = 0.65f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, end = 4.dp),
                textAlign = TextAlign.End
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Siempre habilitado: si falta el contexto, guardar() muestra el error y vibra
            Button(
                onClick = { viewModel.guardar() },
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

private fun iconoParaContextoMood(contexto: ContextoMood): ImageVector {
    return when (contexto) {
        ContextoMood.LABORAL -> Icons.Outlined.WorkOutline
        ContextoMood.RELACIONES -> Icons.Outlined.Groups
        ContextoMood.SALUD -> Icons.Outlined.FavoriteBorder
        ContextoMood.FAMILIAR -> Icons.Outlined.Home
        ContextoMood.SOCIAL -> Icons.Outlined.Forum
        ContextoMood.AISLAMIENTO -> Icons.Outlined.PersonOutline
    }
}