package com.aguasul.posvenda.domain.model

/**
 * Status do ciclo de vida de uma reclamação ou queixa técnica.
 */
enum class StatusReclamacao(val label: String) {
    ABERTA("Aberta"),
    EM_ANDAMENTO("Em Andamento"),
    RESOLVIDA("Resolvida")
}

/**
 * Origem do registro da reclamação.
 */
enum class OrigemReclamacao(val label: String) {
    CLIENTE("Cliente"),
    VISITA_TECNICA("Visita Técnica")
}

/**
 * Representa um chamado ou queixa de pós-venda registrado por cliente ou equipe técnica.
 */
data class Reclamacao(
    val id: String,
    val clienteId: String,
    val clienteNome: String,
    val dataAbertura: String,
    val descricao: String,
    val status: StatusReclamacao = StatusReclamacao.ABERTA,
    val resolucaoTecnica: String? = null,
    val origem: OrigemReclamacao = OrigemReclamacao.CLIENTE
) {
    fun isAberta(): Boolean = status == StatusReclamacao.ABERTA
    fun isEmAndamento(): Boolean = status == StatusReclamacao.EM_ANDAMENTO
    fun isResolvida(): Boolean = status == StatusReclamacao.RESOLVIDA
}
