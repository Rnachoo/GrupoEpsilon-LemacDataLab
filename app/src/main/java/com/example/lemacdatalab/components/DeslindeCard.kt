package com.example.lemacdatalab.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lemacdatalab.ui.theme.SuperficieBlanca
import com.example.lemacdatalab.ui.theme.TextoPrincipal
import com.example.lemacdatalab.ui.theme.TextoSecundario
import com.example.lemacdatalab.ui.theme.VerdePrincipal

private val BordeSuave = Color(0xFFE6E6E1)

@Composable
fun DeslindeCard(
    aceptado: Boolean,
    onAceptadoChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = { onAceptadoChange(!aceptado) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BordeSuave),
        colors = CardDefaults.outlinedCardColors(containerColor = SuperficieBlanca)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(
                checked = aceptado,
                onCheckedChange = null,
                colors = CheckboxDefaults.colors(
                    checkedColor = VerdePrincipal,
                    uncheckedColor = TextoSecundario,
                    checkmarkColor = SuperficieBlanca
                ),
                modifier = Modifier.padding(4.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = buildAnnotatedString {
                    append("Confirmo que he leído el ")
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = TextoPrincipal
                        )
                    ) {
                        append("deslinde clínico")
                    }
                    append(". Comprendo que este registro no sustituye el tratamiento médico ni la atención en crisis.")
                },
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = TextoSecundario
            )
        }
    }
}
