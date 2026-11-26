package com.example.lemacdatalab.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.lemacdatalab.model.EstrategiaNeuro
import com.example.lemacdatalab.model.MAX_OTRO
import com.example.lemacdatalab.model.ResultadoNeuro
import com.example.lemacdatalab.model.TipoEntradaNeuro
import com.example.lemacdatalab.ui.theme.LemacBorderSoft
import com.example.lemacdatalab.ui.theme.LemacGreenDark
import com.example.lemacdatalab.ui.theme.LemacNoteBg
import com.example.lemacdatalab.ui.theme.WarmLowAccent
import com.example.lemacdatalab.viewmodel.NeuroUiState
import com.example.lemacdatalab.viewmodel.NeuroViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Sección de Neurodesarrollo sin estado propio: recibe el estado y devuelve los cambios por callbacks.
// La usan NeuroScreen y, más adelante, «Registro de hoy».
@Composable
fun NeuroSeccion(
    state: NeuroUiState,
    onTipoEntradaChange: (TipoEntradaNeuro) -> Unit,
    onIntensidadChange: (Int) -> Unit,
    onEstrategiaChange: (EstrategiaNeuro) -> Unit,
    onEstrategiaOtroChange: (String) -> Unit,
    onResultadoChange: (ResultadoNeuro) -> Unit,
    onNotasChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    val alTerminarTeclado = KeyboardActions(
        onDone = {
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Rutinas y autorregulación",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = LemacGreenDark
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Cuéntanos qué estás registrando y cómo te fue.",
            style = MaterialTheme.typography.bodyMedium,
            color = LemacGreenDark.copy(alpha = 0.88f)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ---------- Tipo de entrada ----------
        TituloGrupo("¿Qué estás registrando?")

        Column(
            modifier = Modifier.semantics {
                contentDescription = "Selector de tipo de entrada: ${state.tipoEntrada?.etiqueta ?: "sin seleccionar"}"
            },
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TipoEntradaNeuro.entries.chunked(2).forEach { fila ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    fila.forEach { tipo ->
                        ContextoChip(
                            texto = tipo.etiqueta,
                            seleccionado = state.tipoEntrada == tipo,
                            icono = iconoParaTipo(tipo),
                            onClick = { onTipoEntradaChange(tipo) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (fila.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        TextoError(state.tipoEntradaError)

        Spacer(modifier = Modifier.height(24.dp))

        SelectorEscala(
            valor = state.intensidad,
            onValorChange = onIntensidadChange,
            titulo = "Intensidad"
        )

        Spacer(modifier = Modifier.height(24.dp))

        TituloGrupo("¿Qué estrategia usaste?")

        Column(
            modifier = Modifier.semantics {
                contentDescription = "Selector de estrategia utilizada: ${state.estrategia?.etiqueta ?: "sin seleccionar"}"
            },
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            EstrategiaNeuro.entries.chunked(2).forEach { fila ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    fila.forEach { estrategia ->
                        ContextoChip(
                            texto = estrategia.etiqueta,
                            seleccionado = state.estrategia == estrategia,
                            icono = iconoParaEstrategia(estrategia),
                            onClick = { onEstrategiaChange(estrategia) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (fila.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        if (state.estrategia == EstrategiaNeuro.OTRO) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = state.estrategiaOtro,
                onValueChange = onEstrategiaOtroChange,
                label = { Text("Especifica la estrategia") },
                singleLine = true,
                isError = state.estrategiaOtroError != null,
                supportingText = {
                    Text(state.estrategiaOtroError ?: "${state.estrategiaOtro.length}/$MAX_OTRO")
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = alTerminarTeclado,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = coloresCampoNeuro()
            )
        }
        TextoError(state.estrategiaError)

        Spacer(modifier = Modifier.height(24.dp))

        TituloGrupo("¿Cómo te fue con la estrategia?")

        Column(
            modifier = Modifier.semantics {
                contentDescription = "Selector de resultado percibido: ${state.resultado?.etiqueta ?: "sin seleccionar"}"
            },
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ResultadoNeuro.entries.forEach { resultado ->
                ContextoChip(
                    texto = resultado.etiqueta,
                    seleccionado = state.resultado == resultado,
                    onClick = { onResultadoChange(resultado) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        TextoError(state.resultadoError)

        Spacer(modifier = Modifier.height(24.dp))

        // ---------- Notas opcionales ----------
        OutlinedTextField(
            value = state.notas,
            onValueChange = onNotasChange,
            label = { Text("Notas (opcional)") },
            isError = state.notasError != null,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done
            ),
            keyboardActions = alTerminarTeclado,
            modifier = Modifier
                .fillMaxWidth()
                .height(135.dp)
                .semantics {
                    contentDescription = "Campo de notas opcionales, ${state.notas.length} de ${NeuroViewModel.MAX_NOTAS} caracteres"
                }
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
            colors = coloresCampoNeuro(),
            maxLines = 5
        )

        // Contador, o el mensaje de error si se pasó del máximo
        Text(
            text = state.notasError ?: "${state.notas.length}/${NeuroViewModel.MAX_NOTAS}",
            style = MaterialTheme.typography.labelSmall,
            color = if (state.notasError != null) MaterialTheme.colorScheme.error
            else LemacGreenDark.copy(alpha = 0.85f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, end = 4.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun TituloGrupo(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = LemacGreenDark
    )
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
private fun TextoError(mensaje: String?) {
    mensaje?.let {
        Text(
            text = it,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun coloresCampoNeuro() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = LemacNoteBg,
    unfocusedContainerColor = LemacNoteBg,
    errorContainerColor = LemacNoteBg,
    focusedBorderColor = WarmLowAccent,
    unfocusedBorderColor = LemacBorderSoft,
    focusedLabelColor = LemacGreenDark,
    unfocusedLabelColor = LemacGreenDark.copy(alpha = 0.85f),
    cursorColor = WarmLowAccent
)

private fun iconoParaTipo(tipo: TipoEntradaNeuro): ImageVector = when (tipo) {
    TipoEntradaNeuro.RUTINA -> Icons.Outlined.Repeat
    TipoEntradaNeuro.TAREA -> Icons.Outlined.TaskAlt
    TipoEntradaNeuro.SOBRECARGA -> Icons.Outlined.Bolt
}

private fun iconoParaEstrategia(estrategia: EstrategiaNeuro): ImageVector = when (estrategia) {
    EstrategiaNeuro.PAUSA -> Icons.Outlined.PauseCircle
    EstrategiaNeuro.LISTA -> Icons.Outlined.Checklist
    EstrategiaNeuro.TEMPORIZADOR -> Icons.Outlined.Timer
    EstrategiaNeuro.RESPIRACION -> Icons.Outlined.Air
    EstrategiaNeuro.AYUDA -> Icons.Outlined.Handshake
    EstrategiaNeuro.NINGUNA -> Icons.Outlined.RemoveCircleOutline
    EstrategiaNeuro.OTRO -> Icons.Outlined.Edit
}