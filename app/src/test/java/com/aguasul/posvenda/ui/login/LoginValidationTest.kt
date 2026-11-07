package com.aguasul.posvenda.ui.login

import com.aguasul.posvenda.domain.model.MockDataProvider
import com.aguasul.posvenda.domain.model.TipoPerfil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginValidationTest {

    @Test
    fun `credenciais de tecnico mock devem ser validas e mapear para perfil TECNICO`() {
        val email = "tecnico@aquasul.com"
        val senha = "1234"

        val usuario = MockDataProvider.usuariosIniciais.find {
            it.email.equals(email, ignoreCase = true) && it.senha == senha
        }

        assertNotNull(usuario)
        assertEquals(TipoPerfil.TECNICO, usuario?.perfil)
        assertNull(usuario?.clienteId)
    }

    @Test
    fun `credenciais de cliente mock devem ser validas e mapear para perfil CLIENTE com clienteId vinculado`() {
        val email = "hospital@saolucas.com"
        val senha = "1234"

        val usuario = MockDataProvider.usuariosIniciais.find {
            it.email.equals(email, ignoreCase = true) && it.senha == senha
        }

        assertNotNull(usuario)
        assertEquals(TipoPerfil.CLIENTE, usuario?.perfil)
        assertEquals("cli_001", usuario?.clienteId)
    }

    @Test
    fun `credenciais incorretas devem retornar nulo`() {
        val email = "inexistente@aquasul.com"
        val senha = "errada"

        val usuario = MockDataProvider.usuariosIniciais.find {
            it.email.equals(email, ignoreCase = true) && it.senha == senha
        }

        assertNull(usuario)
    }

    @Test
    fun `lista de usuarios de demonstracao de clientes deve conter todos os clientes mock`() {
        val clientesDemo = MockDataProvider.usuariosIniciais.filter { it.perfil == TipoPerfil.CLIENTE }
        assertTrue(clientesDemo.size >= 6)
    }
}
