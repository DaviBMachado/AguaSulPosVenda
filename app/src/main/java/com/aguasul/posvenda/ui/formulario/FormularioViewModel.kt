package com.Aquasul.posvenda.ui.formulario

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.Aquasul.posvenda.PosVendaApplication
import com.Aquasul.posvenda.data.repository.AppRepository
import com.Aquasul.posvenda.domain.model.Atendimento
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

sealed class FormularioUiState {
    object Idle : FormularioUiState()
    object Loading : FormularioUiState()
    data class Success(val atendimento: Atendimento) : FormularioUiState()
    data class ValidationError(
        val parecerError: String? = null,
        val condicaoError: String? = null,
        val tipoError: String? = null
    ) : FormularioUiState()
    data class Error(val message: String) : FormularioUiState()
}

class FormularioViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: AppRepository = (application as PosVendaApplication).repository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<FormularioUiState>(FormularioUiState.Idle)
    val uiState: StateFlow<FormularioUiState> = _uiState.asStateFlow()

    /**
     * Valida os campos obrigatórios do formulário técnico de atendimento.
     * Retorna true se todos os campos forem válidos, ou false caso contrário.
     */
    fun validarCampos(
        parecerTecnico: String,
        condicaoEquipamento: String,
        tipoAtendimento: String
    ): Boolean {
        return parecerTecnico.isNotBlank() &&
                condicaoEquipamento.isNotBlank() &&
                tipoAtendimento.isNotBlank()
    }

    /**
     * Valida os dados e registra o atendimento técnico via repositório.
     */
    fun registrarAtendimento(
        visitaId: String,
        clienteId: String,
        clienteNome: String,
        tipoAtendimento: String,
        condicaoEquipamento: String,
        parecerTecnico: String,
        houveReclamacao: Boolean
    ) {
        val parecerTrimmed = parecerTecnico.trim()
        val tipoValido = tipoAtendimento.isNotBlank()
        val condicaoValida = condicaoEquipamento.isNotBlank()
        val parecerValido = parecerTrimmed.isNotBlank()

        if (!parecerValido || !condicaoValida || !tipoValido) {
            _uiState.value = FormularioUiState.ValidationError(
                parecerError = if (!parecerValido) "O parecer técnico é obrigatório." else null,
                condicaoError = if (!condicaoValida) "Selecione a condição do equipamento." else null,
                tipoError = if (!tipoValido) "Selecione o tipo de atendimento." else null
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = FormularioUiState.Loading
            try {
                val dataHoraAtual = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                val novoAtendimento = Atendimento(
                    id = UUID.randomUUID().toString(),
                    visitaId = visitaId,
                    clienteId = clienteId,
                    clienteNome = clienteNome,
                    dataHora = dataHoraAtual,
                    tipoAtendimento = tipoAtendimento,
                    condicaoEquipamento = condicaoEquipamento,
                    parecerTecnico = parecerTrimmed,
                    houveReclamacao = houveReclamacao
                )

                repository.salvarAtendimento(novoAtendimento)
                _uiState.value = FormularioUiState.Success(novoAtendimento)
            } catch (e: Exception) {
                _uiState.value = FormularioUiState.Error(
                    e.message ?: "Erro ao registrar atendimento."
                )
            }
        }
    }

    /**
     * Reseta o estado da UI para o estado ocioso.
     */
    fun resetState() {
        _uiState.value = FormularioUiState.Idle
    }
}
