package com.example.lemacdatalab.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lemacdatalab.model.ContenidoEducativo
import com.example.lemacdatalab.ui.theme.LemacNoteBg
import com.example.lemacdatalab.ui.theme.SuperficieBlanca
import com.example.lemacdatalab.ui.theme.TextoPrincipal
import com.example.lemacdatalab.ui.theme.TextoSecundario
import com.example.lemacdatalab.ui.theme.VerdePrincipal

private val Terracota = Color(0xFFD97757)

@Composable
fun LecturaModalDialog(
    contenido: ContenidoEducativo,
    onCerrar: () -> Unit,
    onCompartir: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCerrar,
        containerColor = SuperficieBlanca,
        title = {
            Column {
                Text(
                    text = contenido.ambito.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Terracota
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = contenido.titulo,
                    fontWeight = FontWeight.Bold,
                    color = TextoPrincipal
                )
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = contenido.extracto,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = TextoPrincipal
                )
                Spacer(Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(LemacNoteBg)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Fuente",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoSecundario
                    )
                    Text(
                        text = contenido.fuente,
                        fontSize = 13.sp,
                        fontStyle = FontStyle.Italic,
                        color = TextoPrincipal
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onCompartir) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = null,
                    tint = VerdePrincipal,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("Compartir", fontWeight = FontWeight.Bold, color = VerdePrincipal)
            }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) {
                Text("Cerrar", color = TextoSecundario)
            }
        }
    )
}
