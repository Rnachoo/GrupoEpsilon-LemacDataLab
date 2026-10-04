package com.example.lemacdatalab.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = VerdePrincipal,
    secondary = VerdeSecundario,
    background = FondoClaro,
    surface = SuperficieBlanca,
    onPrimary = SuperficieBlanca,
    onBackground = TextoPrincipal,
    onSurface = TextoPrincipal,
    error = RojoError
)

private val DarkColorScheme = darkColorScheme(
    primary = VerdePrincipal,
    secondary = VerdeSecundario,
    background = TextoPrincipal,
    surface = VerdeSecundario,
    onPrimary = SuperficieBlanca,
    onBackground = SuperficieBlanca,
    onSurface = SuperficieBlanca,
    error = RojoError
)

// configuracion del tema de la aplicacion
@Composable
fun LemacDataLabTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}