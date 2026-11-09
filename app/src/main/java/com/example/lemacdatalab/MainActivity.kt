package com.example.lemacdatalab

import android.graphics.Color as ColorAndroid
import android.os.Bundle
import android.view.MotionEvent
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.lemacdatalab.data.SesionUsuario
import com.example.lemacdatalab.navigation.NavGraph
import com.example.lemacdatalab.ui.theme.LemacDataLabTheme
import com.example.lemacdatalab.ui.theme.VerdePrincipal
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    // cambia cada vez que la app vuelve a primer plano para revisar la sesión al instante
    private var vecesAbierta by mutableIntStateOf(0)

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
                                // el contenido queda siempre por encima de los botones del teléfono
                                .navigationBarsPadding()
                        ) {
                            val navController = rememberNavController()

                            // cierre por inactividad: revisa al volver a la app y cada segundo
                            LaunchedEffect(vecesAbierta) {
                                while (true) {
                                    if (SesionUsuario.sesionExpirada()) {
                                        SesionUsuario.cerrar()
                                        navController.navigate("login") {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                    delay(1_000)
                                }
                            }

                            NavGraph(navController = navController)
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        vecesAbierta++
    }

    // cada toque cuenta como actividad del usuario
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        SesionUsuario.registrarActividad()
        return super.dispatchTouchEvent(ev)
    }
}