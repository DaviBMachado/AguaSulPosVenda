package com.aguasul.posvenda.ui.perfil

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aguasul.posvenda.PosVendaApplication
import com.aguasul.posvenda.domain.model.Cliente
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PerfilUiState {
    object Loading : PerfilUiState()
    data class Success(val cliente: Cliente) : PerfilUiState()
    object NotFound : PerfilUiState()
    data class Error(val message: String) : PerfilUiState()
}

class PerfilViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as PosVendaApplication).repository

    private val _uiState = MutableStateFlow<PerfilUiState>(PerfilUiState.Loading)
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    fun carregarPerfil(clienteId: String) {
        viewModelScope.launch {
            _uiState.value = PerfilUiState.Loading
            try {
                repository.getClienteById(clienteId).collect { cliente ->
                    if (cliente != null) {
                        _uiState.value = PerfilUiState.Success(cliente)
                    } else {
                        _uiState.value = PerfilUiState.NotFound
                    }
                }
            } catch (e: Exception) {
                _uiState.value = PerfilUiState.Error(e.message ?: "Erro ao carregar cliente")
            }
        }
    }
}
