package com.example.lemacdatalab.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lemacdatalab.data.SesionUsuario
import com.example.lemacdatalab.model.ResumenCuenta
import com.example.lemacdatalab.model.RolUsuario
import com.example.lemacdatalab.ui.theme.LemacBorderSoft
import com.example.lemacdatalab.ui.theme.LemacGreenDark
import com.example.lemacdatalab.ui.theme.LemacNoteBg
import com.example.lemacdatalab.ui.theme.LemacSurfaceWarm
import com.example.lemacdatalab.ui.theme.SuperficieBlanca
import com.example.lemacdatalab.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    onCerrarSesion: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val esAdmin = SesionUsuario.rol == RolUsuario.ADMIN

    // Guardia: quien llegue aquí sin ser admin vuelve al login
    LaunchedEffect(esAdmin) {
        if (!esAdmin) onCerrarSesion()
    }

    if (esAdmin) {
        Scaffold(
            containerColor = LemacSurfaceWarm,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Panel de Administración",
                            fontWeight = FontWeight.Bold,
                            color = LemacGreenDark
                        )
                    },
                    actions = {
                        IconButton(onClick = {
                            SesionUsuario.cerrar()
                            onCerrarSesion()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Cerrar sesión",
                                tint = LemacGreenDark
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = LemacSurfaceWarm)
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Métricas operativas",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = LemacGreenDark
                        )
                        Text(
                            text = "Volúmenes de uso agregados de las cuentas sintéticas.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = LemacGreenDark.copy(alpha = 0.75f)
                        )
                    }
                }

                // Grilla 2x2 de KPIs
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        KpiCard(
                            titulo = "Registros totales",
                            valor = state.totalRegistros.toString(),
                            detalle = "Histórico completo",
                            modifier = Modifier.weight(1f)
                        )
                        KpiCard(
                            titulo = "Ánimo vs. DBT",
                            valor = "${state.totalAnimo} / ${state.totalDbt}",
                            detalle = "${state.porcentajeAnimo}% · ${state.porcentajeDbt}%",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        KpiCard(
                            titulo = "IDs sintéticos activos",
                            valor = state.cuentasActivas.toString(),
                            detalle = "Personas usuarias",
                            modifier = Modifier.weight(1f)
                        )
                        KpiCard(
                            titulo = "Almacenamiento local",
                            valor = "Operativo",
                            detalle = "OfflineFirst",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item { TarjetaPrivacidad() }

                item {
                    Text(
                        text = "Cuentas sintéticas (${state.cuentasActivas})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = LemacGreenDark,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                items(state.cuentas, key = { it.alias }) { cuenta ->
                    CuentaItem(cuenta)
                }

                item { Spacer(Modifier.size(8.dp)) }
            }
        }
    }
}

@Composable
private fun KpiCard(
    titulo: String,
    valor: String,
    detalle: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.heightIn(min = 108.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieBlanca),
        border = BorderStroke(1.dp, LemacBorderSoft)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelMedium,
                color = LemacGreenDark.copy(alpha = 0.7f)
            )
            Text(
                text = valor,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = LemacGreenDark
            )
            if (detalle != null) {
                Text(
                    text = detalle,
                    style = MaterialTheme.typography.labelSmall,
                    color = LemacGreenDark.copy(alpha = 0.65f)
                )
            }
        }
    }
}

@Composable
private fun TarjetaPrivacidad() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LemacNoteBg),
        border = BorderStroke(2.dp, LemacGreenDark)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null,
                tint = LemacGreenDark,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Minimización de Datos Activa",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = LemacGreenDark
                )
                Text(
                    text = "El perfil administrativo solo audita métricas operativas y volúmenes " +
                            "de uso. Las notas personales y escalas de autorreporte están restringidas " +
                            "al usuario propietario.",
                    style = MaterialTheme.typography.bodySmall,
                    color = LemacGreenDark.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
private fun CuentaItem(cuenta: ResumenCuenta) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieBlanca),
        border = BorderStroke(1.dp, LemacBorderSoft)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cuenta.alias,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = LemacGreenDark
                )
                Text(
                    text = "Última actividad: ${cuenta.ultimaActividad ?: "sin actividad"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = LemacGreenDark.copy(alpha = 0.65f)
                )
            }
            Text(
                text = "${cuenta.totalEventos} eventos",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = LemacGreenDark
            )
        }
    }
}