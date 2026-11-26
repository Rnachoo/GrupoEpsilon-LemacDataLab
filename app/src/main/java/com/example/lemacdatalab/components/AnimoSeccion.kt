package com.example.lemacdatalab.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.lemacdatalab.model.ContextoMood
import com.example.lemacdatalab.model.HORAS_SUENO_MAX
import com.example.lemacdatalab.model.MAX_OTRO
import com.example.lemacdatalab.ui.theme.LemacBorderSoft
import com.example.lemacdatalab.ui.theme.LemacGreenDark
import com.example.lemacdatalab.ui.theme.LemacNoteBg
import com.example.lemacdatalab.ui.theme.WarmLowAccent
import com.example.lemacdatalab.viewmodel.MoodUiState
import com.example.lemacdatalab.viewmodel.MoodViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Sección de Ánimo sin estado propio: recibe el estado y devuelve los cambios por callbacks.
// La usan MoodScreen y, más adelante, «Registro de hoy».
@Composable
fun AnimoSeccion(
    state: MoodUiState,
    onDepresionChange: (Int) -> Unit,
    onElevacionChange: (Int) -> Unit,
    onHorasSuenoChange: (String) -> Unit,
    onAltoRiesgoChange: (Boolean) -> Unit,
    onSintomasPsicoticosChange: (Boolean) -> Unit,
    onContextoChange: (ContextoMood) -> Unit,
    onContextoOtroChange: (String) -> Unit,
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
            text = "¿Cómo te has sentido?",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = LemacGreenDark
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Tómate un momento para identificar qué tan presentes están estas dos experiencias.",
            style = MaterialTheme.typography.bodyMedium,
            color = LemacGreenDark.copy(alpha = 0.88f)
        )

        Spacer(modifier = Modifier.height(14.dp))

        SelectorEscala(
            valor = state.depresion,
            onValorChange = onDepresionChange,
            titulo = "Depresión (ánimo bajo)"
        )

        Spacer(modifier = Modifier.height(12.dp))

        SelectorEscala(
            valor = state.elevacion,
            onValorChange = onElevacionChange,
            titulo = "Elevación (ánimo alto)"
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = state.horasSueno,
            onValueChange = onHorasSuenoChange,
            label = { Text("Horas de sueño (opcional)") },
            singleLine = true,
            isError = state.horasSuenoError != null,
            supportingText = {
                Text(state.horasSuenoError ?: "Entre 0 y $HORAS_SUENO_MAX")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = alTerminarTeclado,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = coloresCampo()
        )

        Spacer(modifier = Modifier.height(12.dp))

        FilaInterruptor(
            titulo = "Alto riesgo",
            descripcion = "Actívalo si sientes que estás en riesgo ahora.",
            activo = state.altoRiesgo,
            onCambio = onAltoRiesgoChange
        )

        Spacer(modifier = Modifier.height(8.dp))

        FilaInterruptor(
            titulo = "Síntomas psicóticos",
            descripcion = "Por ejemplo, oír o ver cosas que otras personas no perciben.",
            activo = state.sintomasPsicoticos,
            onCambio = onSintomasPsicoticosChange
        )

        Spacer(modifier = Modifier.height(24.dp))

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
                            onClick = { onContextoChange(contexto) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (fila.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        if (state.contexto == ContextoMood.OTRO) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = state.contextoOtro,
                onValueChange = onContextoOtroChange,
                label = { Text("Especifica el contexto") },
                singleLine = true,
                isError = state.contextoOtroError != null,
                supportingText = {
                    Text(state.contextoOtroError ?: "${state.contextoOtro.length}/$MAX_OTRO")
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = alTerminarTeclado,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = coloresCampo()
            )
        }

        state.contextoError?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = state.notas,
            onValueChange = onNotasChange,
            label = {
                Text("Espacio libre: ¿Quieres desahogarte o anotar algún detalle? (Opcional)")
            },
            isError = state.notasError != null,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done
            ),
            keyboardActions = alTerminarTeclado,
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
            colors = coloresCampo(),
            maxLines = 5
        )

        Text(
            text = state.notasError ?: "${state.notas.length}/${MoodViewModel.MAX_NOTAS}",
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
private fun FilaInterruptor(
    titulo: String,
    descripcion: String,
    activo: Boolean,
    onCambio: (Boolean) -> Unit
) {
    val estadoTexto = if (activo) "Activado" else "Desactivado"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LemacNoteBg)
            .semantics(mergeDescendants = true) {
                contentDescription = "$titulo: $estadoTexto. $descripcion"
            }
            .toggleable(value = activo, role = Role.Switch, onValueChange = onCambio)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = LemacGreenDark
            )
            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = LemacGreenDark.copy(alpha = 0.85f)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = activo,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(checkedTrackColor = LemacGreenDark)
        )
    }
}

@Composable
private fun coloresCampo() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = LemacNoteBg,
    unfocusedContainerColor = LemacNoteBg,
    errorContainerColor = LemacNoteBg,
    focusedBorderColor = WarmLowAccent,
    unfocusedBorderColor = LemacBorderSoft,
    focusedLabelColor = LemacGreenDark,
    unfocusedLabelColor = LemacGreenDark.copy(alpha = 0.85f),
    cursorColor = WarmLowAccent
)

private fun iconoParaContextoMood(contexto: ContextoMood): ImageVector {
    return when (contexto) {
        ContextoMood.LABORAL -> Icons.Outlined.WorkOutline
        ContextoMood.RELACIONES -> Icons.Outlined.Groups
        ContextoMood.SALUD -> Icons.Outlined.FavoriteBorder
        ContextoMood.FAMILIAR -> Icons.Outlined.Home
        ContextoMood.SOCIAL -> Icons.Outlined.Forum
        ContextoMood.AISLAMIENTO -> Icons.Outlined.PersonOutline
        ContextoMood.OTRO -> Icons.Outlined.Edit
    }
}