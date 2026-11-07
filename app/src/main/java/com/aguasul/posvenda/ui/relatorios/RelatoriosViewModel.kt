package com.aguasul.posvenda.ui.relatorios

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aguasul.posvenda.PosVendaApplication
import com.aguasul.posvenda.data.repository.AppRepository
import com.aguasul.posvenda.domain.model.MetricasRelatorio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed class RelatoriosUiState {
    object Loading : RelatoriosUiState()
    data class Success(val metricas: MetricasRelatorio) : RelatoriosUiState()
    data class Error(val message: String) : RelatoriosUiState()
}

class RelatoriosViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: AppRepository = (application as PosVendaApplication).repository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<RelatoriosUiState>(RelatoriosUiState.Loading)
    val uiState: StateFlow<RelatoriosUiState> = _uiState.asStateFlow()

    init {
        observarMetricas()
    }

    private fun observarMetricas() {
        viewModelScope.launch {
            repository.obterMetricasUsabilidade()
                .catch { e ->
                    _uiState.value = RelatoriosUiState.Error(e.message ?: "Erro ao carregar relatórios.")
                }
                .collect { metricas ->
                    _uiState.value = RelatoriosUiState.Success(metricas)
                }
        }
    }

    fun fazerLogout(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.fazerLogout()
            onComplete()
        }
    }
}
