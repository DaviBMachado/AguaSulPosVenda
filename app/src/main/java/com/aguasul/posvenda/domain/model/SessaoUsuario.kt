package com.aguasul.posvenda.domain.model

/**
 * Perfis de usuário com controle de acesso diferenciado.
 */
enum class TipoPerfil(val label: String) {
    TECNICO("Técnico"),
    CLIENTE("Cliente")
}

/**
 * Representa a sessão ativa do usuário autenticado no aplicativo.
 */
data class SessaoUsuario(
    val perfil: TipoPerfil,
    val clienteId: String? = null,
    val nomeExibicao: String
) {
    val isTecnico: Boolean get() = perfil == TipoPerfil.TECNICO
    val isCliente: Boolean get() = perfil == TipoPerfil.CLIENTE
}

/**
 * Modelo de usuário para autenticação local e demonstrações acadêmicas.
 */
data class Usuario(
    val email: String,
    val senha: String,
    val perfil: TipoPerfil,
    val clienteId: String? = null,
    val nomeExibicao: String
)
