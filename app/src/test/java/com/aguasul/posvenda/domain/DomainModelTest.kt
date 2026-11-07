package com.aguasul.posvenda.domain

import com.aguasul.posvenda.domain.model.Cliente
import com.aguasul.posvenda.domain.model.MockDataProvider
import com.aguasul.posvenda.domain.model.OrigemReclamacao
import com.aguasul.posvenda.domain.model.Reclamacao
import com.aguasul.posvenda.domain.model.SessaoUsuario
import com.aguasul.posvenda.domain.model.StatusReclamacao
import com.aguasul.posvenda.domain.model.TipoPerfil
import com.aguasul.posvenda.domain.model.Visita
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainModelTest {

    @Test
    fun `cliente resumoAndamento deve formatar uso e usabilidade corretamente`() {
        val cliente = Cliente(
            id = "c1",
            nome = "Cliente Teste",
            contato = "123",
            andamentoUso = "Intensivo",
            usabilidade = "Excelente"
        )
        assertEquals("Uso: Intensivo | Usabilidade: Excelente", cliente.resumoAndamento())
    }

    @Test
    fun `cliente possuiReclamacoes deve retornar verdadeiro quando houver itens e falso quando vazia`() {
        val clienteSemReclamacao = Cliente(
            id = "c1",
            nome = "Cliente Feliz",
            contato = "123",
            andamentoUso = "Normal",
            usabilidade = "Boa",
            reclamacoes = emptyList()
        )
        val clienteComReclamacao = Cliente(
            id = "c2",
            nome = "Cliente Alerta",
            contato = "456",
            andamentoUso = "Crítico",
            usabilidade = "Ruim",
            reclamacoes = listOf("Pressão da água instável")
        )

        assertFalse(clienteSemReclamacao.possuiReclamacoes())
        assertTrue(clienteComReclamacao.possuiReclamacoes())
    }

    @Test
    fun `mockDataProvider deve conter clientes e visitas consistentes`() {
        val clientes = MockDataProvider.clientesIniciais
        val visitas = MockDataProvider.visitasIniciais

        assertTrue(clientes.size >= 5)
        assertTrue(visitas.size >= 4)

        // Toda visita inicial deve apontar para um cliente existente
        val clienteIds = clientes.map { it.id }.toSet()
        visitas.forEach { visita ->
            assertTrue("Visita ${visita.id} deve apontar para um cliente existente", clienteIds.contains(visita.clienteId))
            assertEquals(Visita.STATUS_PENDENTE, visita.status)
        }
    }

    @Test
    fun `reclamacao deve verificar estados de ciclo de vida corretamente`() {
        val recAberta = Reclamacao(
            id = "r1",
            clienteId = "c1",
            clienteNome = "Cliente 1",
            dataAbertura = "01/10/2026",
            descricao = "Falha de vazao",
            status = StatusReclamacao.ABERTA,
            origem = OrigemReclamacao.CLIENTE
        )
        val recEmAndamento = recAberta.copy(status = StatusReclamacao.EM_ANDAMENTO)
        val recResolvida = recAberta.copy(status = StatusReclamacao.RESOLVIDA, resolucaoTecnica = "Ajustado")

        assertTrue(recAberta.isAberta())
        assertFalse(recAberta.isEmAndamento())
        assertFalse(recAberta.isResolvida())

        assertTrue(recEmAndamento.isEmAndamento())
        assertTrue(recResolvida.isResolvida())
        assertNotNull(recResolvida.resolucaoTecnica)
    }

    @Test
    fun `sessaoUsuario deve identificar perfil de acesso corretamente`() {
        val sessaoTecnico = SessaoUsuario(
            perfil = TipoPerfil.TECNICO,
            nomeExibicao = "Técnico"
        )
        val sessaoCliente = SessaoUsuario(
            perfil = TipoPerfil.CLIENTE,
            clienteId = "c1",
            nomeExibicao = "Cliente"
        )

        assertTrue(sessaoTecnico.isTecnico)
        assertFalse(sessaoTecnico.isCliente)

        assertTrue(sessaoCliente.isCliente)
        assertFalse(sessaoCliente.isTecnico)
        assertEquals("c1", sessaoCliente.clienteId)
    }

    @Test
    fun `mockDataProvider deve conter usuarios e reclamacoes consistentes`() {
        val usuarios = MockDataProvider.usuariosIniciais
        val reclamacoes = MockDataProvider.reclamacoesIniciais
        val clientes = MockDataProvider.clientesIniciais
        val clienteIds = clientes.map { it.id }.toSet()

        assertTrue(usuarios.any { it.perfil == TipoPerfil.TECNICO && it.email == "tecnico@aquasul.com" })
        assertEquals(4, reclamacoes.size)

        reclamacoes.forEach { rec ->
            assertTrue("Reclamação ${rec.id} deve estar vinculada a um cliente existente", clienteIds.contains(rec.clienteId))
            assertTrue(rec.descricao.isNotBlank())
        }
    }
}
