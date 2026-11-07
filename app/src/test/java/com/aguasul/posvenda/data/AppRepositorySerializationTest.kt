package com.aguasul.posvenda.data

import com.aguasul.posvenda.data.repository.AppRepository
import com.aguasul.posvenda.domain.model.Atendimento
import com.aguasul.posvenda.domain.model.Cliente
import com.aguasul.posvenda.domain.model.MockDataProvider
import com.aguasul.posvenda.domain.model.Reclamacao
import com.aguasul.posvenda.domain.model.StatusReclamacao
import com.aguasul.posvenda.domain.model.Visita
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class AppRepositorySerializationTest {

    private val gson = Gson()

    @Test
    fun `serializacao e desserializacao de clientes devem manter integridade dos dados`() {
        val original = MockDataProvider.clientesIniciais
        val json = gson.toJson(original)

        val type = object : TypeToken<List<Cliente>>() {}.type
        val recuperado: List<Cliente> = gson.fromJson(json, type)

        assertEquals(original.size, recuperado.size)
        assertEquals(original[0].id, recuperado[0].id)
        assertEquals(original[0].nome, recuperado[0].nome)
        assertEquals(original[1].reclamacoes.size, recuperado[1].reclamacoes.size)
    }

    @Test
    fun `serializacao e desserializacao de visitas devem preservar status e atributos`() {
        val original = MockDataProvider.visitasIniciais
        val json = gson.toJson(original)

        val type = object : TypeToken<List<Visita>>() {}.type
        val recuperado: List<Visita> = gson.fromJson(json, type)

        assertEquals(original.size, recuperado.size)
        assertEquals(original[0].clienteNome, recuperado[0].clienteNome)
        assertEquals(Visita.STATUS_PENDENTE, recuperado[0].status)
    }

    @Test
    fun `serializacao de atendimento com reclamacao deve manter campos tecnicos`() {
        val atendimento = Atendimento(
            id = "ate_001",
            visitaId = "vis_001",
            clienteId = "cli_001",
            clienteNome = "Hospital São Lucas",
            dataHora = "10/10/2026 10:00",
            tipoAtendimento = "Preventiva",
            condicaoEquipamento = Atendimento.CONDICAO_NORMAL,
            parecerTecnico = "Filtro operando em perfeitas condições.",
            houveReclamacao = false
        )

        val json = gson.toJson(listOf(atendimento))
        val type = object : TypeToken<List<Atendimento>>() {}.type
        val recuperado: List<Atendimento> = gson.fromJson(json, type)

        assertNotNull(recuperado)
        assertEquals(1, recuperado.size)
        assertEquals("Filtro operando em perfeitas condições.", recuperado[0].parecerTecnico)
    }

    @Test
    fun `serializacao e desserializacao de reclamacoes devem manter status e tipos`() {
        val original = MockDataProvider.reclamacoesIniciais
        val json = gson.toJson(original)

        val type = object : TypeToken<List<Reclamacao>>() {}.type
        val recuperado: List<Reclamacao> = gson.fromJson(json, type)

        assertEquals(original.size, recuperado.size)
        assertEquals("rec_001", recuperado[0].id)
        assertEquals(StatusReclamacao.EM_ANDAMENTO, recuperado[0].status)
        assertEquals("rec_004", recuperado[3].id)
        assertEquals(StatusReclamacao.RESOLVIDA, recuperado[3].status)
    }

    @Test
    fun `calcularMetricas deve retornar contagens e percentuais exatos a partir dos mocks`() {
        val metricas = AppRepository.calcularMetricas(
            clientes = MockDataProvider.clientesIniciais,
            visitas = MockDataProvider.visitasIniciais,
            reclamacoes = MockDataProvider.reclamacoesIniciais
        )

        // Clientes & Usabilidade
        assertEquals(6, metricas.totalClientes)
        assertEquals(4, metricas.usabilidadeAlta)
        assertEquals(1, metricas.usabilidadeMedia)
        assertEquals(1, metricas.usabilidadeBaixa)
        assertEquals(66, metricas.percentualAlta)
        assertEquals(16, metricas.percentualMedia)
        assertEquals(16, metricas.percentualBaixa)

        // Reclamações & Eficácia
        assertEquals(4, metricas.totalReclamacoes)
        assertEquals(1, metricas.reclamacoesResolvidas)
        assertEquals(3, metricas.reclamacoesPendentes)
        assertEquals(25, metricas.taxaResolucaoPercentual)

        // Visitas & Serviços
        assertEquals(4, metricas.totalVisitas)
        assertEquals(0, metricas.visitasConcluidas)
        assertEquals(1, metricas.visitasPreventivas)
        assertEquals(1, metricas.visitasCorretivas)
        assertEquals(2, metricas.visitasOutras)

        // Clientes Críticos
        assertEquals(2, metricas.clientesCriticos.size)
        assertEquals("cli_002", metricas.clientesCriticos[0].clienteId)
        assertEquals(2, metricas.clientesCriticos[0].totalReclamacoesAbertas)
        assertEquals("cli_004", metricas.clientesCriticos[1].clienteId)
        assertEquals(1, metricas.clientesCriticos[1].totalReclamacoesAbertas)
    }
}
