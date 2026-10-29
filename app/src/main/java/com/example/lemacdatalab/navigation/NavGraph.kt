package com.example.lemacdatalab.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.lemacdatalab.screens.DbtScreen
import com.example.lemacdatalab.screens.EditorScreen
import com.example.lemacdatalab.screens.HistorialScreen
import com.example.lemacdatalab.screens.LoginScreen
import com.example.lemacdatalab.screens.MenuScreen
import com.example.lemacdatalab.screens.MoodScreen

// Definición de las rutas y conexión de pantallas
@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onIngresoExitoso = {
                    navController.navigate("menu") {
                        popUpTo("login") { inclusive = true }
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
                onBack = { navController.popBackStack() },
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
                onBack = { navController.popBackStack() },
                idRegistro = id.takeIf { it != -1 }
            )
        }

        composable("historial") {
            HistorialScreen(
                onBack = { navController.popBackStack() },
                onEditar = { registro ->
                    when (registro.tipoArea) {
                        "animo" -> navController.navigate("animo?idRegistro=${registro.idRegistro}")
                        "dbt" -> navController.navigate("dbt?idRegistro=${registro.idRegistro}")
                    }
                }
            )
        }

        // Panel de Editor de Contenidos
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