package com.example.lemacdatalab.data.repository

import com.example.lemacdatalab.data.dao.UsuarioDao
import com.example.lemacdatalab.model.Usuario

class UsuarioRepository(private val usuarioDao: UsuarioDao) {

    // Inserta las cuentas sintéticas si aún no existen
    suspend fun sembrarUsuarioPrueba() {
        USUARIOS_SINTETICOS.forEach { usuarioDao.insertar(it) }
    }

    suspend fun buscarPorIdentificador(identificador: String): Usuario? =
        usuarioDao.buscarPorIdentificador(identificador)

    companion object {
        val USUARIO_PRUEBA = Usuario(
            idUsuario = 1,
            identificadorSintetico = "AX-7B99",
            fechaCreacion = "2026-10-12"
        )

        private val USUARIOS_SINTETICOS = listOf(
            USUARIO_PRUEBA,
            Usuario(idUsuario = 2, identificadorSintetico = "ADM-0001", fechaCreacion = "2026-10-12"),
            Usuario(idUsuario = 3, identificadorSintetico = "EDT-0001", fechaCreacion = "2026-10-12"),
            Usuario(idUsuario = 4, identificadorSintetico = "BZ-4M12", fechaCreacion = "2026-10-12")
        )
    }
}