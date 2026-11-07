package com.aguasul.posvenda.ui.cliente

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.aguasul.posvenda.R
import com.aguasul.posvenda.databinding.ActivityAreaClienteBinding
import com.aguasul.posvenda.ui.login.LoginActivity
import com.aguasul.posvenda.ui.reclamacoes.ReclamacaoAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class AreaClienteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAreaClienteBinding
    private val viewModel: AreaClienteViewModel by viewModels()

    private lateinit var reclamacoesAdapter: ReclamacaoAdapter
    private lateinit var atendimentosAdapter: AtendimentoHistoricoAdapter

    companion object {
        const val EXTRA_CLIENTE_ID = "extra_cliente_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAreaClienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarAreaCliente)

        setupRecyclerViews()
        setupFab()
        observeUiState()

        val clienteId = intent.getStringExtra(EXTRA_CLIENTE_ID)
        viewModel.carregarDados(clienteId)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_app_bar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.action_logout) {
            viewModel.fazerLogout {
                val intent = Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            }
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecyclerViews() {
        reclamacoesAdapter = ReclamacaoAdapter(isTecnico = false)
        binding.rvReclamacoesCliente.layoutManager = LinearLayoutManager(this)
        binding.rvReclamacoesCliente.adapter = reclamacoesAdapter

        atendimentosAdapter = AtendimentoHistoricoAdapter()
        binding.rvAtendimentosCliente.layoutManager = LinearLayoutManager(this)
        binding.rvAtendimentosCliente.adapter = atendimentosAdapter
    }

    private fun setupFab() {
        binding.fabNovaReclamacao.setOnClickListener {
            exibirDialogNovaReclamacao()
        }
    }

    private fun exibirDialogNovaReclamacao() {
        val input = EditText(this).apply {
            hint = getString(R.string.hint_descricao_reclamacao)
            setPadding(48, 32, 48, 32)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.nova_reclamacao_titulo)
            .setView(input)
            .setPositiveButton(R.string.btn_enviar_reclamacao) { _, _ ->
                val desc = input.text.toString().trim()
                if (desc.isNotBlank()) {
                    viewModel.registrarReclamacao(desc)
                    Toast.makeText(this, "Reclamação registrada com sucesso!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "A descrição não pode ser vazia.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.btn_cancelar, null)
            .show()
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    state.cliente?.let { cliente ->
                        binding.toolbarAreaCliente.subtitle = cliente.nome
                        binding.tvNomeClienteArea.text = cliente.nome
                        binding.tvContatoClienteArea.text = cliente.contato
                        binding.tvBadgeUsabilidadeArea.text = cliente.usabilidade
                        binding.tvAndamentoUsoArea.text = "Uso: ${cliente.andamentoUso}"

                        val (bgColor, textColor) = when {
                            cliente.usabilidade.contains("Alta", ignoreCase = true) || cliente.usabilidade.contains("Excelente", ignoreCase = true) ->
                                R.color.status_concluida_bg to R.color.status_concluida_text
                            cliente.usabilidade.contains("Média", ignoreCase = true) || cliente.usabilidade.contains("Satisfatória", ignoreCase = true) ->
                                R.color.status_em_andamento_bg to R.color.status_em_andamento_text
                            else ->
                                R.color.alerta_reclamacao_bg to R.color.alerta_reclamacao_text
                        }
                        binding.tvBadgeUsabilidadeArea.backgroundTintList = ColorStateList.valueOf(
                            ContextCompat.getColor(this@AreaClienteActivity, bgColor)
                        )
                        binding.tvBadgeUsabilidadeArea.setTextColor(
                            ContextCompat.getColor(this@AreaClienteActivity, textColor)
                        )
                    }

                    if (state.reclamacoes.isEmpty()) {
                        binding.tvReclamacoesVaziasArea.visibility = View.VISIBLE
                        binding.rvReclamacoesCliente.visibility = View.GONE
                    } else {
                        binding.tvReclamacoesVaziasArea.visibility = View.GONE
                        binding.rvReclamacoesCliente.visibility = View.VISIBLE
                        reclamacoesAdapter.submitList(state.reclamacoes)
                    }

                    if (state.atendimentos.isEmpty()) {
                        binding.tvAtendimentosVaziosArea.visibility = View.VISIBLE
                        binding.rvAtendimentosCliente.visibility = View.GONE
                    } else {
                        binding.tvAtendimentosVaziosArea.visibility = View.GONE
                        binding.rvAtendimentosCliente.visibility = View.VISIBLE
                        atendimentosAdapter.submitList(state.atendimentos)
                    }
                }
            }
        }
    }
}
