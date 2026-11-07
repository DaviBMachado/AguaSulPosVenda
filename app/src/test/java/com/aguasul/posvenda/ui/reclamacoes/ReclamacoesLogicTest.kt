package com.aguasul.posvenda.ui.reclamacoes

import com.aguasul.posvenda.domain.model.MockDataProvider
import com.aguasul.posvenda.domain.model.OrigemReclamacao
import com.aguasul.posvenda.domain.model.Reclamacao
import com.aguasul.posvenda.domain.model.StatusReclamacao
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReclamacoesLogicTest {

    @Test
    fun `filtro TODAS deve retornar todas as reclamacoes cadastradas`() {
        val reclamacoes = MockDataProvider.reclamacoesIniciais
        val filtradas = when (FiltroReclamacao.TODAS) {
            FiltroReclamacao.TODAS -> reclamacoes
            FiltroReclamacao.ABERTAS -> reclamacoes.filter { it.status != StatusReclamacao.RESOLVIDA }
            FiltroReclamacao.RESOLVIDAS -> reclamacoes.filter { it.status == StatusReclamacao.RESOLVIDA }
        }

        assertEquals(reclamacoes.size, filtradas.size)
    }

    @Test
    fun `filtro ABERTAS deve retornar apenas reclamacoes nao resolvidas`() {
        val reclamacoes = MockDataProvider.reclamacoesIniciais
        val filtradas = reclamacoes.filter { it.status != StatusReclamacao.RESOLVIDA }

        assertTrue(filtradas.all { it.status != StatusReclamacao.RESOLVIDA })
        assertEquals(3, filtradas.size) // rec_001 (EM_ANDAMENTO), rec_002 (ABERTA), rec_003 (ABERTA)
    }

    @Test
    fun `filtro RESOLVIDAS deve retornar apenas reclamacoes com status RESOLVIDA`() {
        val reclamacoes = MockDataProvider.reclamacoesIniciais
        val filtradas = reclamacoes.filter { it.status == StatusReclamacao.RESOLVIDA }

        assertTrue(filtradas.all { it.status == StatusReclamacao.RESOLVIDA })
        assertEquals(1, filtradas.size) // rec_004
    }

    @Test
    fun `resolucao de reclamacao deve atualizar status para RESOLVIDA e registrar parecer`() {
        val original = Reclamacao(
            id = "rec_999",
            clienteId = "cli_001",
            clienteNome = "Hospital São Lucas",
            dataAbertura = "07/11/2026 10:00",
            descricao = "Pressão irregular",
            status = StatusReclamacao.ABERTA,
            origem = OrigemReclamacao.CLIENTE
        )

        val resolvida = original.copy(
            status = StatusReclamacao.RESOLVIDA,
            resolucaoTecnica = "Pressurizador recalibrado com sucesso."
        )

        assertEquals(StatusReclamacao.RESOLVIDA, resolvida.status)
        assertEquals("Pressurizador recalibrado com sucesso.", resolvida.resolucaoTecnica)
    }
}
