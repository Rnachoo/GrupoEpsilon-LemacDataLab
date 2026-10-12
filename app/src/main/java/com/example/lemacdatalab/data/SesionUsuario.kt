package com.example.lemacdatalab.data

import com.example.lemacdatalab.model.Usuario

// Usuario autenticado durante la ejecución de la app
object SesionUsuario {

    var usuarioActual: Usuario? = null
        private set

    val idUsuario: Int?
        get() = usuarioActual?.idUsuario

    fun iniciar(usuario: Usuario) {
        usuarioActual = usuario
    }

    fun cerrar() {
        usuarioActual = null
    }
}
