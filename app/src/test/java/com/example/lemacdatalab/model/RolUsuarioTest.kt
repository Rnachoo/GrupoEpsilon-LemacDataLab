package com.example.lemacdatalab.model

import org.junit.Assert.assertEquals
import org.junit.Test

class RolUsuarioTest {

    @Test
    fun prefijoAdm_detectaRolAdmin() {
        assertEquals(RolUsuario.ADMIN, RolUsuario.detectarRol("ADM-0001"))
    }

    @Test
    fun prefijoEdt_detectaRolEditor() {
        assertEquals(RolUsuario.EDITOR, RolUsuario.detectarRol("EDT-0001"))
    }

    @Test
    fun prefijoUsuarioConEspaciosOMinusculas_detectaRolCorrecto() {
        assertEquals(RolUsuario.ADMIN, RolUsuario.detectarRol(" adm-0001 "))
        assertEquals(RolUsuario.EDITOR, RolUsuario.detectarRol("edt-9999"))
        assertEquals(RolUsuario.USUARIO, RolUsuario.detectarRol("ax-7b99"))
        assertEquals(RolUsuario.USUARIO, RolUsuario.detectarRol("BZ-4M12"))
    }
}
