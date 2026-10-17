package com.Aquasul.posvenda.domain.model

/**
 * Representa um cliente da carteira de pós-venda da Aqua Sul.
 */
data class Cliente(
    val id: String,
    val nome: String,
    val contato: String,
    val andamentoUso: String,
    val usabilidade: String,
    val reclamacoes: List<String> = emptyList()
) {
    /**
     * Retorna o resumo formatado do andamento de uso e usabilidade para visualização rápida.
     */
    fun resumoAndamento(): String {
        return "Uso: $andamentoUso | Usabilidade: $usabilidade"
    }

    /**
     * Indica se o cliente possui reclamações ou alertas registrados.
     */
    fun possuiReclamacoes(): Boolean {
        return reclamacoes.isNotEmpty()
    }
}
