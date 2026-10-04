package com.example.lemacdatalab.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable


// definicion de las rutas de navegacion
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
            Text("Pantalla de Menú (Tu sección, la reemplazaremos pronto)")
        }
        composable("animo") {
            Text("Pantalla de Ánimo (Asignada a Persona 3)")
        }
    }
}