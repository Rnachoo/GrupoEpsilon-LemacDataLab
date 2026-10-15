package com.example.lemacdatalab.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.lemacdatalab.screens.DbtScreen
import com.example.lemacdatalab.screens.LoginScreen
import com.example.lemacdatalab.screens.MenuScreen
import com.example.lemacdatalab.screens.MoodScreen

// definición de las rutas y conexión de pantallas
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
            // rutas de dirección de cada botón
            MenuScreen(
                alNavegarNeurodesarrollo = { navController.navigate("neurodesarrollo") },
                alNavegarDBT = { navController.navigate("dbt") },
                alNavegarAnimo = { navController.navigate("animo") },
                alNavegarAdicciones = { navController.navigate("adicciones") }
            )
        }

        composable("animo") {
            MoodScreen(onBack = { navController.popBackStack() })
        }

        composable("dbt") {
            DbtScreen(onBack = { navController.popBackStack() })
        }

        // rutas temporales restantes
        composable("neurodesarrollo") {
            Text("Pantalla de Neurodesarrollo (En construcción)")
        }

        composable("adicciones") {
            Text("Pantalla de Adicciones (En construcción)")
        }
    }
}