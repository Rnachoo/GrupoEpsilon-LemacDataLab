package com.example.lemacdatalab.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lemacdatalab.native.ContactoApoyoEjemplo
import com.example.lemacdatalab.native.LINEAS_AYUDA
import com.example.lemacdatalab.native.marcarNumeroEmergencia
import com.example.lemacdatalab.ui.theme.SuperficieBlanca
import com.example.lemacdatalab.ui.theme.TextoPrincipal
import com.example.lemacdatalab.ui.theme.TextoSecundario
import com.example.lemacdatalab.ui.theme.VerdePrincipal

@Composable
fun AlertaValorCriticoDialog(onCerrar: () -> Unit) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onCerrar,
        containerColor = SuperficieBlanca,
        icon = {
            Icon(
                imageVector = Icons.Default.FavoriteBorder,
                contentDescription = null,
                tint = VerdePrincipal,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "Gracias por registrarlo",
                fontWeight = FontWeight.Bold,
                color = TextoPrincipal,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Tu registro quedó guardado. Lo que sientes es importante y no tienes que pasar por esto solo/a. Tomarte un momento para ti ya es un paso.",
                    fontSize = 14.sp,
                    color = TextoPrincipal,
                    lineHeight = 20.sp
                )

                Text(
                    text = "LemacDataLab no es un servicio de urgencias y nadie revisa tus registros en tiempo real. Si necesitas ayuda ahora, usa uno de los contactos de abajo.",
                    fontSize = 13.sp,
                    color = TextoSecundario,
                    lineHeight = 18.sp
                )

                Text(
                    text = "Algo que puedes probar ahora",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextoPrincipal
                )
                Text(
                    text = "• Tolerancia al Malestar: respira lento, inhalando 4 segundos y exhalando 6 u 8, durante unos minutos.\n" +
                            "• Acción protectora: aléjate de cualquier cosa con la que podrías hacerte daño y quédate cerca de otra persona.",
                    fontSize = 13.sp,
                    color = TextoSecundario,
                    lineHeight = 18.sp
                )

                TarjetaLineaEmergencia(
                    titulo = "Tu contacto de apoyo",
                    numero = ContactoApoyoEjemplo.TELEFONO,
                    descripcion = "${ContactoApoyoEjemplo.NOMBRE}. Una persona de confianza a quien escribir o llamar.",
                    alLlamar = { marcarNumeroEmergencia(context, ContactoApoyoEjemplo.TELEFONO) }
                )

                LINEAS_AYUDA.forEach { linea ->
                    TarjetaLineaEmergencia(
                        titulo = linea.titulo,
                        numero = linea.numero,
                        descripcion = linea.descripcion,
                        alLlamar = { marcarNumeroEmergencia(context, linea.numero) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onCerrar) {
                Text(text = "Entendido, continuar", fontWeight = FontWeight.Bold, color = VerdePrincipal)
            }
        }
    )
}