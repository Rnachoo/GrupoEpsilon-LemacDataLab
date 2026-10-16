package com.example.lemacdatalab

import android.graphics.Color as ColorAndroid
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.lemacdatalab.navigation.NavGraph
import com.example.lemacdatalab.ui.theme.LemacDataLabTheme
import com.example.lemacdatalab.ui.theme.VerdePrincipal

class MainActivity : ComponentActivity() {

    // punto de entrada de la aplicacion
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            // Íconos claros arriba (sobre la banda verde) y oscuros abajo (sobre fondo claro),
            // sin importar si el teléfono está en modo oscuro
            statusBarStyle = SystemBarStyle.dark(ColorAndroid.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(ColorAndroid.TRANSPARENT, ColorAndroid.TRANSPARENT)
        )
        setContent {
            LemacDataLabTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Banda verde detrás de la barra de estado del teléfono
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .windowInsetsTopHeight(WindowInsets.statusBars)
                                .background(VerdePrincipal)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .consumeWindowInsets(WindowInsets.statusBars)
                        ) {
                            val navController = rememberNavController()
                            NavGraph(navController = navController)
                        }
                    }
                }
            }
        }
    }
}