package com.example.lemacdatalab.data

import com.example.lemacdatalab.model.Usuario

class UsuarioRepository(private val usuarioDao: UsuarioDao) {

    // Inserta el usuario de prueba si aún no existe
    suspend fun sembrarUsuarioPrueba() {
        usuarioDao.insertar(USUARIO_PRUEBA)
    }

    suspend fun buscarPorIdentificador(identificador: String): Usuario? =
        usuarioDao.buscarPorIdentificador(identificador)

    companion object {
        val USUARIO_PRUEBA = Usuario(
            idUsuario = 1,
            identificadorSintetico = "AX-7B99",
            fechaCreacion = "2026-10-12"
        )
    }
}
