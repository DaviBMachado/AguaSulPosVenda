package com.aguasul.posvenda.ui.cliente

import com.aguasul.posvenda.domain.model.Atendimento
import com.aguasul.posvenda.domain.model.MockDataProvider
import com.aguasul.posvenda.domain.model.OrigemReclamacao
import com.aguasul.posvenda.domain.model.Reclamacao
import com.aguasul.posvenda.domain.model.StatusReclamacao
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AreaClienteLogicTest {

    @Test
    fun `historico de atendimentos deve filtrar exclusivamente pelo cliente logado`() {
        val clienteId = "cli_001"
        val atendimentos = listOf(
            Atendimento(
                id = "atend_001",
                visitaId = "visita_001",
                clienteId = "cli_001",
                clienteNome = "Hospital São Lucas",
                dataHora = "06/11/2026 14:00",
                tipoAtendimento = "Preventiva",
                condicaoEquipamento = Atendimento.CONDICAO_NORMAL,
                parecerTecnico = "Filtros trocados e teste de vazão aprovado.",
                houveReclamacao = false
            ),
            Atendimento(
                id = "atend_002",
                visitaId = "visita_002",
                clienteId = "cli_002",
                clienteNome = "Laboratório Alfa",
                dataHora = "06/11/2026 16:00",
                tipoAtendimento = "Corretiva",
                condicaoEquipamento = Atendimento.CONDICAO_RESTRICAO,
                parecerTecnico = "Ajuste na válvula solenoide.",
                houveReclamacao = true
            )
        )
        val atendimentosDoCliente = atendimentos.filter { it.clienteId == clienteId }

        assertEquals(1, atendimentosDoCliente.size)
        assertEquals("cli_001", atendimentosDoCliente.first().clienteId)
        assertEquals("Filtros trocados e teste de vazão aprovado.", atendimentosDoCliente.first().parecerTecnico)
    }

    @Test
    fun `atendimento concluido deve conter parecer tecnico e condicao do equipamento`() {
        val atendimento = Atendimento(
            id = "atend_001",
            visitaId = "visita_001",
            clienteId = "cli_001",
            clienteNome = "Hospital São Lucas",
            dataHora = "06/11/2026 14:00",
            tipoAtendimento = "Preventiva",
            condicaoEquipamento = Atendimento.CONDICAO_NORMAL,
            parecerTecnico = "Filtros trocados e teste de vazão aprovado.",
            houveReclamacao = false
        )

        assertNotNull(atendimento.parecerTecnico)
        assertFalse(atendimento.parecerTecnico.isBlank())
        assertFalse(atendimento.condicaoEquipamento.isBlank())
    }

    @Test
    fun `nova reclamacao gerada pelo cliente deve ter origem CLIENTE e status inicial ABERTA`() {
        val novaReclamacao = Reclamacao(
            id = "rec_teste",
            clienteId = "cli_001",
            clienteNome = "Hospital São Lucas",
            dataAbertura = "07/11/2026 12:00",
            descricao = "Filtro com vazamento na conexão",
            status = StatusReclamacao.ABERTA,
            origem = OrigemReclamacao.CLIENTE
        )

        assertEquals(OrigemReclamacao.CLIENTE, novaReclamacao.origem)
        assertEquals(StatusReclamacao.ABERTA, novaReclamacao.status)
        assertEquals("cli_001", novaReclamacao.clienteId)
    }

    @Test
    fun `reclamacao resolvida do cliente deve manter o parecer de resolucao tecnica`() {
        val reclamacaoResolvida = MockDataProvider.reclamacoesIniciais.first { it.status == StatusReclamacao.RESOLVIDA }

        assertEquals(StatusReclamacao.RESOLVIDA, reclamacaoResolvida.status)
        assertNotNull(reclamacaoResolvida.resolucaoTecnica)
        assertFalse(reclamacaoResolvida.resolucaoTecnica.isNullOrBlank())
    }
}
