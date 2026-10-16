package com.example.lemacdatalab.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lemacdatalab.model.RegistroHistorial
import com.example.lemacdatalab.viewmodel.HistorialViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    onBack: () -> Unit,
    viewModel: HistorialViewModel = viewModel()
) {
    val registros by viewModel.registros.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial") },
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
        when (val lista = registros) {
            null -> Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            else -> if (lista.isEmpty()) {
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aún no tienes registros. Guarda una evaluación de ánimo o una entrada del diario DBT y aparecerá aquí.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(lista, key = { it.idRegistro }) { registro ->
                        TarjetaRegistro(registro)
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaRegistro(registro: RegistroHistorial) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = tituloArea(registro.tipoArea),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = formatearFecha(registro.fechaHoraCreacion),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val detalle = detalleRegistro(registro)
            if (detalle != null) {
                Text(text = detalle, style = MaterialTheme.typography.bodyMedium)
            }

            registro.notasOpcionales?.let { notas ->
                Text(
                    text = notas,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun tituloArea(tipoArea: String): String = when (tipoArea) {
    "animo" -> "Estado de Ánimo"
    "dbt" -> "Diario DBT"
    "neurodesarrollo" -> "Neurodesarrollo"
    "adicciones" -> "Adicciones"
    else -> tipoArea
}

private fun detalleRegistro(r: RegistroHistorial): String? = when {
    r.nivelIntensidad != null && r.contexto != null ->
        "Intensidad ${r.nivelIntensidad}/10 · ${r.contexto}"
    r.habilidad != null && r.nivelImpulso != null ->
        "${r.habilidad} · Impulso ${r.nivelImpulso}/10"
    else -> null
}

private fun formatearFecha(fechaHora: String): String {
    val partes = fechaHora.split(" ")
    val fecha = partes.firstOrNull()?.split("-")
    if (partes.size != 2 || fecha == null || fecha.size != 3) return fechaHora
    return "${fecha[2]}/${fecha[1]}/${fecha[0]} ${partes[1].take(5)}"
}