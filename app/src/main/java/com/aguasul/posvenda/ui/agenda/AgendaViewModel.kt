package com.Aquasul.posvenda.ui.agenda

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.Aquasul.posvenda.PosVendaApplication
import com.Aquasul.posvenda.domain.model.Visita
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AgendaUiState {
    object Loading : AgendaUiState()
    data class Success(val visitas: List<Visita>) : AgendaUiState()
    object Empty : AgendaUiState()
    data class Error(val message: String) : AgendaUiState()
}

class AgendaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as PosVendaApplication).repository

    private val _uiState = MutableStateFlow<AgendaUiState>(AgendaUiState.Loading)
    val uiState: StateFlow<AgendaUiState> = _uiState.asStateFlow()

    init {
        carregarVisitas()
    }

    fun carregarVisitas() {
        viewModelScope.launch {
            _uiState.value = AgendaUiState.Loading
            try {
                repository.getVisitas().collect { lista ->
                    if (lista.isEmpty()) {
                        _uiState.value = AgendaUiState.Empty
                    } else {
                        _uiState.value = AgendaUiState.Success(lista)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = AgendaUiState.Error(e.message ?: "Erro ao carregar visitas")
            }
        }
    }
}
