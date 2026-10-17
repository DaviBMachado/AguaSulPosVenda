package com.Aquasul.posvenda.domain.model

/**
 * Representa o registro do relatório técnico de atendimento preenchido em campo.
 */
data class Atendimento(
    val id: String,
    val visitaId: String,
    val clienteId: String,
    val clienteNome: String,
    val dataHora: String,
    val tipoAtendimento: String,
    val condicaoEquipamento: String,
    val parecerTecnico: String,
    val houveReclamacao: Boolean
) {
    companion object {
        const val CONDICAO_NORMAL = "Operando Normal"
        const val CONDICAO_RESTRICAO = "Operando com Restrição"
        const val CONDICAO_INOPERANTE = "Inoperante"
    }
}
