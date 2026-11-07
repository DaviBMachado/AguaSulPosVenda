package com.aguasul.posvenda.domain.model

/**
 * Cliente com chamados pendentes para destaque no painel de atenção crítica.
 */
data class ClienteCritico(
    val clienteId: String,
    val clienteNome: String,
    val totalReclamacoesAbertas: Int
)

/**
 * Agrega as métricas calculadas em memória para o painel de relatórios avançados.
 */
data class MetricasRelatorio(
    val totalClientes: Int = 0,
    val usabilidadeAlta: Int = 0,
    val usabilidadeMedia: Int = 0,
    val usabilidadeBaixa: Int = 0,
    val percentualAlta: Int = 0,
    val percentualMedia: Int = 0,
    val percentualBaixa: Int = 0,

    val totalReclamacoes: Int = 0,
    val reclamacoesResolvidas: Int = 0,
    val reclamacoesPendentes: Int = 0,
    val taxaResolucaoPercentual: Int = 0,

    val totalVisitas: Int = 0,
    val visitasConcluidas: Int = 0,
    val visitasPreventivas: Int = 0,
    val visitasCorretivas: Int = 0,
    val visitasOutras: Int = 0,

    val clientesCriticos: List<ClienteCritico> = emptyList()
)
