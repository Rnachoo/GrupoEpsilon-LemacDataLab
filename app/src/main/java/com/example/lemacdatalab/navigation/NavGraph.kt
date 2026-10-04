package com.example.lemacdatalab.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.lemacdatalab.screens.MenuScreen
import com.example.lemacdatalab.screens.MoodScreen

// definicion de las rutas y conexion de pantallas
@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "menu"
    ) {
        composable("login") {
            Text("Pantalla de Login (Asignada a Persona 1)")
        }

        composable("menu") {
            // rutas de direccion de cada botón
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

        // rutas temporales
        composable("neurodesarrollo") {
            Text("Pantalla de Neurodesarrollo (En construcción)")
        }

        composable("dbt") {
            Text("Pantalla de Diario DBT (En construcción)")
        }

        composable("adicciones") {
            Text("Pantalla de Adicciones (En construcción)")
        }
    }
}