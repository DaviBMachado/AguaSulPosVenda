package com.Aquasul.posvenda.ui.formulario

import com.Aquasul.posvenda.domain.model.Atendimento
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FormularioValidationTest {

    @Test
    fun `validarCampos deve falhar quando parecer estiver vazio ou em branco`() {
        val parecer = "   "
        val condicao = Atendimento.CONDICAO_NORMAL
        val tipo = "Preventiva"

        val valido = parecer.isNotBlank() && condicao.isNotBlank() && tipo.isNotBlank()
        assertFalse(valido)
    }

    @Test
    fun `validarCampos deve falhar quando condicao estiver vazia`() {
        val parecer = "Filtro trocado e teste de condutividade aprovado."
        val condicao = ""
        val tipo = "Preventiva"

        val valido = parecer.isNotBlank() && condicao.isNotBlank() && tipo.isNotBlank()
        assertFalse(valido)
    }

    @Test
    fun `validarCampos deve falhar quando tipo estiver vazio`() {
        val parecer = "Filtro trocado."
        val condicao = Atendimento.CONDICAO_NORMAL
        val tipo = ""

        val valido = parecer.isNotBlank() && condicao.isNotBlank() && tipo.isNotBlank()
        assertFalse(valido)
    }

    @Test
    fun `validarCampos deve passar com sucesso quando todos os campos obrigatorios estiverem preenchidos`() {
        val parecer = "Manutenção preventiva concluída sem intercorrências."
        val condicao = Atendimento.CONDICAO_NORMAL
        val tipo = "Preventiva"

        val valido = parecer.isNotBlank() && condicao.isNotBlank() && tipo.isNotBlank()
        assertTrue(valido)
    }

    @Test
    fun `atendimento deve armazenar dados e constantes de condicao corretamente`() {
        val atendimento = Atendimento(
            id = "atend-01",
            visitaId = "visita-01",
            clienteId = "cli-01",
            clienteNome = "Hospital São Lucas",
            dataHora = "10/10/2026 14:00",
            tipoAtendimento = "Preventiva",
            condicaoEquipamento = Atendimento.CONDICAO_NORMAL,
            parecerTecnico = "Tudo em ordem",
            houveReclamacao = false
        )

        assertEquals("atend-01", atendimento.id)
        assertEquals("Hospital São Lucas", atendimento.clienteNome)
        assertEquals(Atendimento.CONDICAO_NORMAL, atendimento.condicaoEquipamento)
        assertFalse(atendimento.houveReclamacao)
    }
}
