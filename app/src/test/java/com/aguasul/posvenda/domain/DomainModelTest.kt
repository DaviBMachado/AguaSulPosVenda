package com.Aquasul.posvenda.domain

import com.Aquasul.posvenda.domain.model.Cliente
import com.Aquasul.posvenda.domain.model.MockDataProvider
import com.Aquasul.posvenda.domain.model.Visita
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
            reclamacoes = listOf("Pressão da Aqua instável")
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
}
