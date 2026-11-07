package com.aguasul.posvenda.ui.relatorios

import com.aguasul.posvenda.data.repository.AppRepository
import com.aguasul.posvenda.domain.model.MockDataProvider
import com.aguasul.posvenda.domain.model.StatusReclamacao
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RelatoriosLogicTest {

    @Test
    fun `calculo de metricas deve computar percentuais de usabilidade corretamente`() {
        val metricas = AppRepository.calcularMetricas(
            clientes = MockDataProvider.clientesIniciais,
            visitas = MockDataProvider.visitasIniciais,
            reclamacoes = MockDataProvider.reclamacoesIniciais
        )

        assertEquals(6, metricas.totalClientes)
        assertEquals(4, metricas.usabilidadeAlta)
        assertEquals(1, metricas.usabilidadeMedia)
        assertEquals(1, metricas.usabilidadeBaixa)

        assertEquals(66, metricas.percentualAlta) // (4*100)/6 = 66
        assertEquals(16, metricas.percentualMedia) // (1*100)/6 = 16
        assertEquals(16, metricas.percentualBaixa) // (1*100)/6 = 16
    }

    @Test
    fun `calculo de metricas deve computar taxa de resolucao de queixas com precisao`() {
        val metricas = AppRepository.calcularMetricas(
            clientes = MockDataProvider.clientesIniciais,
            visitas = MockDataProvider.visitasIniciais,
            reclamacoes = MockDataProvider.reclamacoesIniciais
        )

        assertEquals(4, metricas.totalReclamacoes)
        assertEquals(1, metricas.reclamacoesResolvidas)
        assertEquals(3, metricas.reclamacoesPendentes)
        assertEquals(25, metricas.taxaResolucaoPercentual) // (1*100)/4 = 25%
    }

    @Test
    fun `calculo de clientes criticos deve listar apenas clientes com reclamacoes abertas ordenados por quantidade`() {
        val metricas = AppRepository.calcularMetricas(
            clientes = MockDataProvider.clientesIniciais,
            visitas = MockDataProvider.visitasIniciais,
            reclamacoes = MockDataProvider.reclamacoesIniciais
        )

        val criticos = metricas.clientesCriticos
        assertEquals(2, criticos.size)

        // cli_002 tem 2 reclamacoes abertas (rec_001 e rec_002)
        assertEquals("cli_002", criticos[0].clienteId)
        assertEquals(2, criticos[0].totalReclamacoesAbertas)

        // cli_004 tem 1 reclamacao aberta (rec_003)
        assertEquals("cli_004", criticos[1].clienteId)
        assertEquals(1, criticos[1].totalReclamacoesAbertas)
    }
}
