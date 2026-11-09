package com.example.lemacdatalab.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import com.example.lemacdatalab.screens.MisDatosScreen
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.lemacdatalab.data.SesionUsuario
import com.example.lemacdatalab.model.RolUsuario
import com.example.lemacdatalab.screens.AdminScreen
import com.example.lemacdatalab.screens.DbtScreen
import com.example.lemacdatalab.screens.EditorScreen
import com.example.lemacdatalab.screens.HistorialScreen
import com.example.lemacdatalab.screens.LoginScreen
import com.example.lemacdatalab.screens.MenuScreen
import com.example.lemacdatalab.screens.MoodScreen
import com.example.lemacdatalab.screens.ProximamenteScreen

private fun NavHostController.volverSeguro() {
    val actual = currentBackStackEntry ?: return
    if (actual.lifecycle.currentState == Lifecycle.State.RESUMED &&
        previousBackStackEntry != null
    ) {
        popBackStack()
    }
}

// Definición de las rutas y conexión de pantallas
@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onIngresoExitoso = { rol ->
                    val destino = when (rol) {
                        RolUsuario.ADMIN -> "admin"
                        RolUsuario.EDITOR -> "editor"
                        RolUsuario.USUARIO -> "menu"
                    }
                    navController.navigate(destino) {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("admin") {
            AdminScreen(
                onCerrarSesion = {
                    SesionUsuario.cerrar()
                    navController.navigate("login") {
                        popUpTo("admin") { inclusive = true }
                    }
                }
            )
        }

        composable("menu") {
            MenuScreen(
                alNavegarNeurodesarrollo = { navController.navigate("neurodesarrollo") },
                alNavegarDBT = { navController.navigate("dbt") },
                alNavegarAnimo = { navController.navigate("animo") },
                alNavegarAdicciones = { navController.navigate("adicciones") },
                alNavegarHistorial = { navController.navigate("historial") },
                alNavegarMisDatos = { navController.navigate("misdatos") }
            )
        }

        composable(
            route = "animo?idRegistro={idRegistro}",
            arguments = listOf(navArgument("idRegistro") {
                type = NavType.IntType
                defaultValue = -1
            })
        ) { entrada ->
            val id = entrada.arguments?.getInt("idRegistro") ?: -1
            MoodScreen(
                onBack = { navController.volverSeguro() },
                idRegistro = id.takeIf { it != -1 }
            )
        }

        composable(
            route = "dbt?idRegistro={idRegistro}",
            arguments = listOf(navArgument("idRegistro") {
                type = NavType.IntType
                defaultValue = -1
            })
        ) { entrada ->
            val id = entrada.arguments?.getInt("idRegistro") ?: -1
            DbtScreen(
                onBack = { navController.volverSeguro() },
                idRegistro = id.takeIf { it != -1 }
            )
        }

        composable("historial") {
            HistorialScreen(
                onBack = { navController.volverSeguro() },
                onEditar = { registro ->
                    when (registro.tipoArea) {
                        "animo" -> navController.navigate("animo?idRegistro=${registro.idRegistro}")
                        "dbt" -> navController.navigate("dbt?idRegistro=${registro.idRegistro}")
                    }
                }
            )
        }

        composable("misdatos") {
            MisDatosScreen(
                onBack = { navController.volverSeguro() }
            )
        }

        // Panel de Editor de Contenidos
        composable("editor") {
            EditorScreen(
                onCerrarSesion = {
                    SesionUsuario.cerrar()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Módulos pendientes: Pantalla reutilizable «Próximamente» con botón volver
        composable("neurodesarrollo") {
            ProximamenteScreen(
                titulo = "Neurodesarrollo",
                subtitulo = "Rutinas y autorregulación",
                icono = Icons.Default.Face,
                onBack = { navController.volverSeguro() }
            )
        }

        composable("adicciones") {
            ProximamenteScreen(
                titulo = "Adicciones",
                subtitulo = "Impulsos y metas",
                icono = Icons.Default.LocalFireDepartment,
                onBack = { navController.volverSeguro() }
            )
        }
    }
}
