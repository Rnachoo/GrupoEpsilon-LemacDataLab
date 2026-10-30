package com.example.lemacdatalab.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
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

/**
 * Volver "seguro": evita el bug de la pantalla en blanco al spamear el botón volver.
 *
 * - previousBackStackEntry != null -> nunca saca la última pantalla de la pila
 *   (si la pila queda vacía, el NavHost no dibuja nada).
 * - RESUMED -> ignora toques mientras la pantalla ya se está cerrando (animación de salida).
 */
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
                alNavegarHistorial = { navController.navigate("historial") }
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

        // Panel de Editor de Contenidos
        // (se eliminó el placeholder duplicado "Editor (en construcción)")
        composable("editor") {
            EditorScreen(
                onCerrarSesion = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Rutas temporales restantes
        composable("neurodesarrollo") {
            Text("Pantalla de Neurodesarrollo (En construcción)")
        }

        composable("adicciones") {
            Text("Pantalla de Adicciones (En construcción)")
        }
    }
}