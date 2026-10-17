package com.Aquasul.posvenda.ui.formulario

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.Aquasul.posvenda.R
import com.Aquasul.posvenda.databinding.ActivityFormularioAtendimentoBinding
import com.Aquasul.posvenda.domain.model.Atendimento
import com.Aquasul.posvenda.domain.model.Visita
import com.Aquasul.posvenda.ui.agenda.AgendaActivity
import com.Aquasul.posvenda.ui.confirmacao.ConfirmacaoActivity
import kotlinx.coroutines.launch

class FormularioAtendimentoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormularioAtendimentoBinding
    private val viewModel: FormularioViewModel by viewModels()

    private var visitaId: String = ""
    private var clienteId: String = ""
    private var clienteNome: String = ""
    private var dataHorario: String = ""
    private var tipoVisitaAgendada: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormularioAtendimentoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recuperarDadosVisita()
        setupToolbar()
        popularCabecalhoContextual()
        preencherValoresPadrao()
        setupListeners()
        observeUiState()
    }

    private fun recuperarDadosVisita() {
        visitaId = intent.getStringExtra(AgendaActivity.EXTRA_VISITA_ID)
            ?: intent.getStringExtra("visitaId") ?: ""
        clienteId = intent.getStringExtra(AgendaActivity.EXTRA_CLIENTE_ID)
            ?: intent.getStringExtra("clienteId") ?: ""
        clienteNome = intent.getStringExtra(AgendaActivity.EXTRA_CLIENTE_NOME)
            ?: intent.getStringExtra("clienteNome") ?: ""
        dataHorario = intent.getStringExtra(AgendaActivity.EXTRA_DATA_HORARIO)
            ?: intent.getStringExtra("dataHorario") ?: ""
        tipoVisitaAgendada = intent.getStringExtra(AgendaActivity.EXTRA_TIPO_VISITA)
            ?: intent.getStringExtra("tipo") ?: ""
    }

    private fun setupToolbar() {
        binding.toolbarFormulario.setNavigationOnClickListener {
            finish()
        }
    }

    private fun popularCabecalhoContextual() {
        binding.tvContextoCliente.text = clienteNome.ifBlank {
            getString(R.string.cliente_nao_informado)
        }
        binding.tvContextoDataHorario.text = dataHorario.ifBlank {
            getString(R.string.data_hora_nao_informada)
        }
        binding.tvContextoTipoAgendado.text = tipoVisitaAgendada.ifBlank {
            getString(R.string.tipo_nao_informado)
        }
    }

    private fun preencherValoresPadrao() {
        // Pré-seleciona o tipo de atendimento de acordo com o agendamento prévio
        when (tipoVisitaAgendada) {
            Visita.TIPO_PREVENTIVA -> binding.rbTipoPreventiva.isChecked = true
            Visita.TIPO_CORRETIVA -> binding.rbTipoCorretiva.isChecked = true
            Visita.TIPO_TROCA_FILTRO -> binding.rbTipoTrocaFiltro.isChecked = true
            Visita.TIPO_TREINAMENTO -> binding.rbTipoTreinamento.isChecked = true
            else -> binding.rbTipoPreventiva.isChecked = true
        }

        // Pré-seleciona "Operando Normal" por padrão operacional
        binding.rbCondicaoNormal.isChecked = true
    }

    private fun setupListeners() {
        // Limpa erro do parecer conforme o usuário digita
        binding.etParecerTecnico.doOnTextChanged { text, _, _, _ ->
            if (!text.isNullOrBlank()) {
                binding.tilParecerTecnico.error = null
            }
        }

        // Limpa mensagem de erro da condição se houver seleção
        binding.rgCondicaoEquipamento.setOnCheckedChangeListener { _, _ ->
            binding.tvErroCondicao.visibility = View.GONE
        }

        binding.btnRegistrarAtendimento.setOnClickListener {
            processarSubmissao()
        }
    }

    private fun processarSubmissao() {
        val tipoAtendimento = when (binding.rgTipoAtendimento.checkedRadioButtonId) {
            R.id.rbTipoPreventiva -> Visita.TIPO_PREVENTIVA
            R.id.rbTipoCorretiva -> Visita.TIPO_CORRETIVA
            R.id.rbTipoTrocaFiltro -> Visita.TIPO_TROCA_FILTRO
            R.id.rbTipoTreinamento -> Visita.TIPO_TREINAMENTO
            else -> ""
        }

        val condicaoEquipamento = when (binding.rgCondicaoEquipamento.checkedRadioButtonId) {
            R.id.rbCondicaoNormal -> Atendimento.CONDICAO_NORMAL
            R.id.rbCondicaoRestricao -> Atendimento.CONDICAO_RESTRICAO
            R.id.rbCondicaoInoperante -> Atendimento.CONDICAO_INOPERANTE
            else -> ""
        }

        val parecerTecnico = binding.etParecerTecnico.text?.toString() ?: ""
        val houveReclamacao = binding.switchReclamacao.isChecked

        viewModel.registrarAtendimento(
            visitaId = visitaId,
            clienteId = clienteId,
            clienteNome = clienteNome,
            tipoAtendimento = tipoAtendimento,
            condicaoEquipamento = condicaoEquipamento,
            parecerTecnico = parecerTecnico,
            houveReclamacao = houveReclamacao
        )
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is FormularioUiState.Idle -> {
                            binding.progressBar.visibility = View.GONE
                            binding.btnRegistrarAtendimento.isEnabled = true
                        }
                        is FormularioUiState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.btnRegistrarAtendimento.isEnabled = false
                        }
                        is FormularioUiState.ValidationError -> {
                            binding.progressBar.visibility = View.GONE
                            binding.btnRegistrarAtendimento.isEnabled = true

                            if (state.parecerError != null) {
                                binding.tilParecerTecnico.error = state.parecerError
                                binding.etParecerTecnico.requestFocus()
                            } else {
                                binding.tilParecerTecnico.error = null
                            }

                            if (state.condicaoError != null) {
                                binding.tvErroCondicao.text = state.condicaoError
                                binding.tvErroCondicao.visibility = View.VISIBLE
                            } else {
                                binding.tvErroCondicao.visibility = View.GONE
                            }

                            if (state.tipoError != null) {
                                Toast.makeText(
                                    this@FormularioAtendimentoActivity,
                                    state.tipoError,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        is FormularioUiState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            navegarParaConfirmacao(state.atendimento)
                        }
                        is FormularioUiState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.btnRegistrarAtendimento.isEnabled = true
                            Toast.makeText(
                                this@FormularioAtendimentoActivity,
                                state.message,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private fun navegarParaConfirmacao(atendimento: Atendimento) {
        val intent = Intent(this, ConfirmacaoActivity::class.java).apply {
            putExtra(ConfirmacaoActivity.EXTRA_CLIENTE_NOME, atendimento.clienteNome)
            putExtra(ConfirmacaoActivity.EXTRA_TIPO_ATENDIMENTO, atendimento.tipoAtendimento)
            putExtra(ConfirmacaoActivity.EXTRA_DATA_HORA, atendimento.dataHora)
            putExtra(ConfirmacaoActivity.EXTRA_CONDICAO, atendimento.condicaoEquipamento)
            putExtra(ConfirmacaoActivity.EXTRA_HOUVE_RECLAMACAO, atendimento.houveReclamacao)
        }
        startActivity(intent)
        finish()
    }
}
