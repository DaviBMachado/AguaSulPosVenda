package com.aguasul.posvenda.ui.reclamacoes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aguasul.posvenda.PosVendaApplication
import com.aguasul.posvenda.data.repository.AppRepository
import com.aguasul.posvenda.domain.model.Reclamacao
import com.aguasul.posvenda.domain.model.StatusReclamacao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

enum class FiltroReclamacao {
    TODAS,
    ABERTAS,
    RESOLVIDAS
}

sealed class ReclamacoesUiState {
    object Loading : ReclamacoesUiState()
    data class Success(
        val reclamacoes: List<Reclamacao>,
        val filtroAtual: FiltroReclamacao
    ) : ReclamacoesUiState()
    object Empty : ReclamacoesUiState()
    data class Error(val message: String) : ReclamacoesUiState()
}

class ReclamacoesViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: AppRepository = (application as PosVendaApplication).repository
) : AndroidViewModel(application) {

    private val _filtro = MutableStateFlow(FiltroReclamacao.TODAS)
    private val _uiState = MutableStateFlow<ReclamacoesUiState>(ReclamacoesUiState.Loading)
    val uiState: StateFlow<ReclamacoesUiState> = _uiState.asStateFlow()

    init {
        observarReclamacoes()
    }

    private fun observarReclamacoes() {
        viewModelScope.launch {
            combine(repository.obterReclamacoes(), _filtro) { lista, filtro ->
                val filtradas = when (filtro) {
                    FiltroReclamacao.TODAS -> lista
                    FiltroReclamacao.ABERTAS -> lista.filter { it.status != StatusReclamacao.RESOLVIDA }
                    FiltroReclamacao.RESOLVIDAS -> lista.filter { it.status == StatusReclamacao.RESOLVIDA }
                }
                if (filtradas.isEmpty()) {
                    ReclamacoesUiState.Empty
                } else {
                    ReclamacoesUiState.Success(filtradas, filtro)
                }
            }.collect { estado ->
                _uiState.value = estado
            }
        }
    }

    fun aplicarFiltro(novoFiltro: FiltroReclamacao) {
        _filtro.value = novoFiltro
    }

    fun resolverReclamacao(reclamacaoId: String, parecerResolucao: String) {
        viewModelScope.launch {
            repository.atualizarStatusReclamacao(
                id = reclamacaoId,
                novoStatus = StatusReclamacao.RESOLVIDA,
                resolucao = parecerResolucao.trim()
            )
        }
    }

    fun fazerLogout(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.fazerLogout()
            onComplete()
        }
    }
}
