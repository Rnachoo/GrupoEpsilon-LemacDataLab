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
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Spa
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
import com.example.lemacdatalab.model.HabilidadDbt
import com.example.lemacdatalab.native.vibrarAlGuardar
import com.example.lemacdatalab.ui.theme.LemacBorderSoft
import com.example.lemacdatalab.ui.theme.LemacGreenDark
import com.example.lemacdatalab.ui.theme.LemacNoteBg
import com.example.lemacdatalab.ui.theme.LemacSurfaceWarm
import com.example.lemacdatalab.ui.theme.WarmLowAccent
import com.example.lemacdatalab.viewmodel.DbtViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DbtScreen(
    onBack: () -> Unit,
    idRegistro: Int? = null, // null = registro nuevo; con valor = editar ese registro
    viewModel: DbtViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
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

    // Éxito al guardar: vibración suave, aviso y vuelta atrás
    LaunchedEffect(uiState.guardado) {
        if (uiState.guardado) {
            vibrarAlGuardar(context, esError = false)
            scope.launch {
                snackbarHostState.showSnackbar("Registro DBT guardado correctamente")
            }
            viewModel.onGuardadoConsumido()
            onBack()
        }
    }

    // Error de validación: vibración doble de alerta
    LaunchedEffect(uiState.habilidadError, uiState.notasError) {
        if (uiState.habilidadError != null || uiState.notasError != null) {
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
                        text = if (uiState.esEdicion) "Editar Diario DBT" else "Diario DBT",
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
                text = "Nivel de Impulso",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = LemacGreenDark
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Respira un segundo. ¿Qué tan fuerte sentiste el impulso hoy?",
                style = MaterialTheme.typography.bodyMedium,
                color = LemacGreenDark.copy(alpha = 0.78f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // IntensidadCard trabaja con Float; el ViewModel guarda el nivel como Int
            IntensidadCard(
                valor = uiState.nivelImpulso.toFloat(),
                onValorChange = { viewModel.onNivelChange(it.roundToInt()) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "¿Qué habilidad pusiste en práctica?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LemacGreenDark
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                HabilidadDbt.entries.chunked(2).forEach { fila ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        fila.forEach { habilidad ->
                            ContextoChip(
                                texto = habilidad.etiqueta,
                                seleccionado = uiState.habilidad == habilidad,
                                icono = iconoParaHabilidadDbt(habilidad),
                                onClick = { viewModel.onHabilidadChange(habilidad) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (fila.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Error: la habilidad es obligatoria
            uiState.habilidadError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = uiState.notas,
                onValueChange = viewModel::onNotasChange,
                label = {
                    Text("¿Cómo te ayudó esta habilidad o qué aprendiste del momento?")
                },
                isError = uiState.notasError != null,
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

            // Contador, o el mensaje de error (por ejemplo: impulso > 7 exige justificar)
            Text(
                text = uiState.notasError ?: "${uiState.notas.length}/${DbtViewModel.MAX_NOTAS}",
                style = MaterialTheme.typography.labelSmall,
                color = if (uiState.notasError != null) MaterialTheme.colorScheme.error
                else LemacGreenDark.copy(alpha = 0.65f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, end = 4.dp),
                textAlign = TextAlign.End
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Siempre habilitado: guardar() valida y muestra los errores
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
                    text = if (uiState.esEdicion) "Guardar Cambios" else "Guardar Registro DBT",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun iconoParaHabilidadDbt(habilidad: HabilidadDbt): ImageVector {
    return when (habilidad) {
        HabilidadDbt.ACEPTACION -> Icons.Outlined.Spa
        HabilidadDbt.MINDFULNESS -> Icons.Outlined.SelfImprovement
        HabilidadDbt.TOLERANCIA -> Icons.Outlined.Shield
        HabilidadDbt.REGULACION -> Icons.Outlined.Balance
        HabilidadDbt.EFECTIVIDAD -> Icons.Outlined.Handshake
        HabilidadDbt.NINGUNA -> Icons.Outlined.RemoveCircleOutline
    }
}