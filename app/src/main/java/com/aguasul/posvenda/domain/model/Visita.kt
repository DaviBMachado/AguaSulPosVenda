package com.Aquasul.posvenda.domain.model

/**
 * Representa um agendamento de visita técnica de pós-venda.
 */
data class Visita(
    val id: String,
    val clienteId: String,
    val clienteNome: String,
    val dataHorario: String,
    val tipo: String,
    val status: String = STATUS_PENDENTE
) {
    companion object {
        const val STATUS_PENDENTE = "PENDENTE"
        const val STATUS_CONCLUIDA = "CONCLUIDA"

        const val TIPO_PREVENTIVA = "Preventiva"
        const val TIPO_CORRETIVA = "Corretiva"
        const val TIPO_TROCA_FILTRO = "Troca de Filtro/Refil"
        const val TIPO_TREINAMENTO = "Treinamento"
    }

    fun isPendente(): Boolean = status == STATUS_PENDENTE
    fun isConcluida(): Boolean = status == STATUS_CONCLUIDA
}
