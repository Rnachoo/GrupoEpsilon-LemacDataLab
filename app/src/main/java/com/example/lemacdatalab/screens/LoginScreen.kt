package com.example.lemacdatalab.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lemacdatalab.components.DeslindeCard
import com.example.lemacdatalab.components.IdSinteticoCard
import com.example.lemacdatalab.ui.theme.FondoClaro
import com.example.lemacdatalab.ui.theme.SuperficieBlanca
import com.example.lemacdatalab.ui.theme.TextoPrincipal
import com.example.lemacdatalab.ui.theme.TextoSecundario
import com.example.lemacdatalab.ui.theme.VerdePrincipal
import com.example.lemacdatalab.viewmodel.LoginViewModel

private val BotonDeshabilitado = Color(0xFFE4E4E0)

@Composable
fun LoginScreen(
    onIngresoExitoso: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    // Al autenticar: limpia el formulario y avanza al menú
    LaunchedEffect(state.ingresado) {
        if (state.ingresado) {
            viewModel.onIngresoConsumido()
            onIngresoExitoso()
        }
    }

    Scaffold(containerColor = FondoClaro) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(40.dp))

            Icon(
                imageVector = Icons.Outlined.Shield,
                contentDescription = null,
                tint = SuperficieBlanca,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(VerdePrincipal)
                    .padding(12.dp)
            )

            Text(
                text = "Portal de\nAutorregistro",
                fontFamily = FontFamily.Serif,
                fontSize = 30.sp,
                lineHeight = 36.sp,
                color = TextoPrincipal,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Plataforma de auto-registro estructurado.\nPrivacidad desde el diseño.",
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = TextoSecundario,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            IdSinteticoCard(
                valor = state.idSintetico,
                error = state.idError,
                onValorChange = viewModel::onIdChange,
                onConfirmar = viewModel::ingresar
            )

            DeslindeCard(
                aceptado = state.deslindeAceptado,
                onAceptadoChange = viewModel::onDeslindeChange
            )

            // Botón justo bajo el deslinde para que quede al alcance en cualquier pantalla
            Button(
                onClick = { viewModel.ingresar() },
                enabled = state.deslindeAceptado,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerdePrincipal,
                    contentColor = SuperficieBlanca,
                    disabledContainerColor = BotonDeshabilitado,
                    disabledContentColor = SuperficieBlanca
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Ingresar al Portal", fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}
