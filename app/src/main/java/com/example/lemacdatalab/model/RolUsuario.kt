package com.example.lemacdatalab.model

enum class RolUsuario(val prefijo: String, val titulo: String, val idDemo: String) {
    USUARIO(prefijo = "AX-", titulo = "Persona Usuaria", idDemo = "AX-7B99"),
    ADMIN(prefijo = "ADM-", titulo = "Administración y Operación", idDemo = "ADM-0001"),
    EDITOR(prefijo = "EDT-", titulo = "Editor de Contenidos", idDemo = "EDT-0001");

    companion object {
        fun detectarRol(codigo: String): RolUsuario {
            val limpio = codigo.trim().uppercase()
            return when {
                limpio.startsWith(ADMIN.prefijo) -> ADMIN
                limpio.startsWith(EDITOR.prefijo) -> EDITOR
                else -> USUARIO
            }
        }
    }
}