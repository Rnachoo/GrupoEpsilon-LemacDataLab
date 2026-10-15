package com.example.lemacdatalab.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun IntensidadCard(
    nivel: Int,
    onNivelChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = nivel.toString(),
                style = MaterialTheme.typography.displayLarge
            )
            Slider(
                value = nivel.toFloat(),
                onValueChange = { onNivelChange(it.roundToInt()) },
                valueRange = 1f..10f,
                steps = 8
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("NULO (1)", style = MaterialTheme.typography.labelSmall)
                Text("ELEVADO (10)", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}