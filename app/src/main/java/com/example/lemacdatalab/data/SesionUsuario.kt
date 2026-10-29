package com.example.lemacdatalab.data

import com.example.lemacdatalab.model.RolUsuario
import com.example.lemacdatalab.model.Usuario

// Usuario autenticado durante la ejecución de la app
object SesionUsuario {
    val rol: RolUsuario?
        get() = usuarioActual?.let { RolUsuario.detectarRol(it.identificadorSintetico) }

    var usuarioActual: Usuario? = null
        private set

    val idUsuario: Int?
        get() = usuarioActual?.idUsuario

    // Acceso directo al ID sintético del usuario logueado (ej: "AX-7B99" o "BZ-4M12")
    val identificadorSintetico: String
        get() = usuarioActual?.identificadorSintetico ?: "SIN-ID"

    fun iniciar(usuario: Usuario) {
        usuarioActual = usuario
    }

    fun cerrar() {
        usuarioActual = null
    }
}