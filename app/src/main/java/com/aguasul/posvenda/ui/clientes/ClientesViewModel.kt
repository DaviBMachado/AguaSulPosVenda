package com.Aquasul.posvenda.ui.clientes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.Aquasul.posvenda.PosVendaApplication
import com.Aquasul.posvenda.domain.model.Cliente
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

sealed class ClientesUiState {
    object Loading : ClientesUiState()
    data class Success(val clientes: List<Cliente>) : ClientesUiState()
    object Empty : ClientesUiState()
    data class Error(val message: String) : ClientesUiState()
}

class ClientesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as PosVendaApplication).repository

    private val _clientesBase = MutableStateFlow<List<Cliente>>(emptyList())
    private val _filtroQuery = MutableStateFlow("")

    private val _uiState = MutableStateFlow<ClientesUiState>(ClientesUiState.Loading)
    val uiState: StateFlow<ClientesUiState> = _uiState.asStateFlow()

    init {
        observarClientes()
    }

    private fun observarClientes() {
        viewModelScope.launch {
            _uiState.value = ClientesUiState.Loading
            try {
                combine(_clientesBase, _filtroQuery) { lista, query ->
                    if (query.isBlank()) {
                        lista
                    } else {
                        lista.filter { cliente ->
                            cliente.nome.contains(query, ignoreCase = true) ||
                            cliente.contato.contains(query, ignoreCase = true)
                        }
                    }
                }.collect { listaFiltrada ->
                    if (listaFiltrada.isEmpty()) {
                        _uiState.value = ClientesUiState.Empty
                    } else {
                        _uiState.value = ClientesUiState.Success(listaFiltrada)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = ClientesUiState.Error(e.message ?: "Erro ao carregar clientes")
            }
        }

        viewModelScope.launch {
            repository.getClientes().collect { clientes ->
                _clientesBase.value = clientes
            }
        }
    }

    fun filtrar(query: String) {
        _filtroQuery.value = query.trim()
    }

    fun recarregar() {
        viewModelScope.launch {
            repository.getClientes().collect { clientes ->
                _clientesBase.value = clientes
            }
        }
    }
}
