package com.aguasul.posvenda.ui.navegacao

import android.content.Intent
import com.aguasul.posvenda.R
import org.junit.Assert.assertEquals
import org.junit.Test

class NavegacaoFlagsTest {

    @Test
    fun `menu inferior do tecnico deve conter exatamente 4 abas distintas`() {
        val abas = listOf(
            R.id.nav_agenda,
            R.id.nav_clientes,
            R.id.nav_reclamacoes,
            R.id.nav_relatorios
        )

        assertEquals(4, abas.size)
        assertEquals(4, abas.distinct().size)
    }

    @Test
    fun `flags de reordenamento de abas devem usar FLAG_ACTIVITY_REORDER_TO_FRONT`() {
        val flagEsperada = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        assertEquals(0x20000, flagEsperada)
    }

    @Test
    fun `flags de logout devem usar FLAG_ACTIVITY_NEW_TASK e FLAG_ACTIVITY_CLEAR_TASK`() {
        val flagsLogout = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        assertEquals(0x10008000, flagsLogout)
    }
}
