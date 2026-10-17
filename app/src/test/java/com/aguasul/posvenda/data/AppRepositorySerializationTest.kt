package com.Aquasul.posvenda.data

import com.Aquasul.posvenda.domain.model.Atendimento
import com.Aquasul.posvenda.domain.model.Cliente
import com.Aquasul.posvenda.domain.model.MockDataProvider
import com.Aquasul.posvenda.domain.model.Visita
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
}
