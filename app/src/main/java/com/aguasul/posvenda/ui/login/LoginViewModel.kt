package com.aguasul.posvenda.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aguasul.posvenda.PosVendaApplication
import com.aguasul.posvenda.data.repository.AppRepository
import com.aguasul.posvenda.domain.model.MockDataProvider
import com.aguasul.posvenda.domain.model.SessaoUsuario
import com.aguasul.posvenda.domain.model.TipoPerfil
import com.aguasul.posvenda.domain.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val sessao: SessaoUsuario) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: AppRepository = (application as PosVendaApplication).repository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    val clientesDemo: List<Usuario> = MockDataProvider.usuariosIniciais.filter { it.perfil == TipoPerfil.CLIENTE }

    init {
        verificarSessaoAtiva()
    }

    fun verificarSessaoAtiva() {
        viewModelScope.launch {
            try {
                val sessao = repository.obterSessao().first()
                if (sessao != null) {
                    _uiState.value = LoginUiState.Success(sessao)
                }
            } catch (_: Exception) {
                // Se falhar ao ler sessão, permanece na tela de login
            }
        }
    }

    fun fazerLogin(emailInput: String, senhaInput: String) {
        val email = emailInput.trim()
        val senha = senhaInput.trim()

        if (email.isBlank() || senha.isBlank()) {
            _uiState.value = LoginUiState.Error("Preencha o e-mail e a senha.")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val usuario = MockDataProvider.usuariosIniciais.find {
                it.email.equals(email, ignoreCase = true) && it.senha == senha
            }

            if (usuario != null) {
                repository.salvarSessao(
                    tipoPerfil = usuario.perfil,
                    clienteId = usuario.clienteId,
                    nome = usuario.nomeExibicao
                )
                val sessao = SessaoUsuario(
                    perfil = usuario.perfil,
                    clienteId = usuario.clienteId,
                    nomeExibicao = usuario.nomeExibicao
                )
                _uiState.value = LoginUiState.Success(sessao)
            } else {
                _uiState.value = LoginUiState.Error("Credenciais inválidas. Verifique e-mail e senha.")
            }
        }
    }

    fun fazerLoginTecnicoDemo() {
        val tecnico = MockDataProvider.usuariosIniciais.first { it.perfil == TipoPerfil.TECNICO }
        fazerLogin(tecnico.email, tecnico.senha)
    }

    fun fazerLoginClienteDemo(clienteId: String) {
        val cliente = MockDataProvider.usuariosIniciais.find { it.clienteId == clienteId } ?: return
        fazerLogin(cliente.email, cliente.senha)
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}
