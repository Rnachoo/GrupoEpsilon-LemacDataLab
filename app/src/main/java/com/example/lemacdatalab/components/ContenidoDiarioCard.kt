package com.example.lemacdatalab.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lemacdatalab.model.ContenidoEducativo
import com.example.lemacdatalab.ui.theme.TextoPrincipal
import com.example.lemacdatalab.ui.theme.TextoSecundario
import com.example.lemacdatalab.ui.theme.WarmMedBg

private val Terracota = Color(0xFFD97757)

@Composable
fun ContenidoDiarioCard(
    contenido: ContenidoEducativo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Terracota),
        colors = CardDefaults.cardColors(containerColor = WarmMedBg)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.AutoStories,
                contentDescription = null,
                tint = Terracota,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "LECTURA DEL DÍA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Terracota,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Terracota.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = contenido.titulo,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoPrincipal
                )
                Text(
                    text = contenido.ambito,
                    fontSize = 13.sp,
                    color = TextoSecundario
                )
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Leer",
                tint = TextoSecundario
            )
        }
    }
}
