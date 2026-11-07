package com.aguasul.posvenda.ui.cliente

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aguasul.posvenda.PosVendaApplication
import com.aguasul.posvenda.data.repository.AppRepository
import com.aguasul.posvenda.domain.model.Atendimento
import com.aguasul.posvenda.domain.model.Cliente
import com.aguasul.posvenda.domain.model.OrigemReclamacao
import com.aguasul.posvenda.domain.model.Reclamacao
import com.aguasul.posvenda.domain.model.StatusReclamacao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class AreaClienteUiState(
    val cliente: Cliente? = null,
    val reclamacoes: List<Reclamacao> = emptyList(),
    val atendimentos: List<Atendimento> = emptyList(),
    val isLoading: Boolean = true
)

class AreaClienteViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: AppRepository = (application as PosVendaApplication).repository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AreaClienteUiState())
    val uiState: StateFlow<AreaClienteUiState> = _uiState.asStateFlow()

    private var currentClienteId: String? = null

    fun carregarDados(clienteId: String? = null) {
        viewModelScope.launch {
            val cid = clienteId ?: repository.obterSessao().first()?.clienteId ?: "cli_001"
            currentClienteId = cid
            combine(
                repository.getClienteById(cid),
                repository.obterReclamacoesPorCliente(cid),
                repository.getAtendimentos()
            ) { cliente, reclamacoes, atendimentos ->
                val atendimentosCliente = atendimentos.filter { it.clienteId == cid }
                AreaClienteUiState(
                    cliente = cliente,
                    reclamacoes = reclamacoes,
                    atendimentos = atendimentosCliente,
                    isLoading = false
                )
            }.collect { estado ->
                _uiState.value = estado
            }
        }
    }

    fun registrarReclamacao(descricao: String) {
        val cid = currentClienteId ?: return
        val clienteNome = _uiState.value.cliente?.nome ?: "Cliente"
        val dataFormatada = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

        val novaReclamacao = Reclamacao(
            id = "rec_${UUID.randomUUID()}",
            clienteId = cid,
            clienteNome = clienteNome,
            dataAbertura = dataFormatada,
            descricao = descricao.trim(),
            status = StatusReclamacao.ABERTA,
            origem = OrigemReclamacao.CLIENTE
        )

        viewModelScope.launch {
            repository.adicionarReclamacao(novaReclamacao)
        }
    }

    fun fazerLogout(onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            repository.fazerLogout()
            onLogoutComplete()
        }
    }
}
