package com.Aquasul.posvenda.ui.perfil

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.Aquasul.posvenda.R
import com.Aquasul.posvenda.databinding.ActivityPerfilClienteBinding
import com.Aquasul.posvenda.domain.model.Cliente
import com.Aquasul.posvenda.ui.clientes.ClientesActivity
import kotlinx.coroutines.launch

class PerfilClienteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilClienteBinding
    private val viewModel: PerfilViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilClienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()

        val clienteId = intent.getStringExtra(ClientesActivity.EXTRA_CLIENTE_ID) ?: ""
        if (clienteId.isNotBlank()) {
            viewModel.carregarPerfil(clienteId)
        }

        observeUiState()
    }

    private fun setupToolbar() {
        binding.toolbarPerfil.setNavigationOnClickListener {
            finish()
        }
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is PerfilUiState.Loading -> {
                            // UI loading
                        }
                        is PerfilUiState.Success -> {
                            renderizarCliente(state.cliente)
                        }
                        is PerfilUiState.NotFound -> {
                            binding.tvPerfilNome.text = "Cliente não localizado"
                        }
                        is PerfilUiState.Error -> {
                            binding.tvPerfilNome.text = state.message
                        }
                    }
                }
            }
        }
    }

    private fun renderizarCliente(cliente: Cliente) {
        binding.tvPerfilNome.text = cliente.nome
        binding.tvPerfilContato.text = cliente.contato
        binding.tvPerfilAndamento.text = cliente.andamentoUso
        binding.tvPerfilUsabilidade.text = cliente.usabilidade

        if (cliente.possuiReclamacoes()) {
            binding.cardAlertaReclamacao.visibility = View.VISIBLE
            binding.tvSemReclamacoes.visibility = View.GONE
            binding.layoutListaReclamacoes.removeAllViews()

            cliente.reclamacoes.forEach { queixa ->
                val textView = TextView(this).apply {
                    text = "• $queixa"
                    textSize = 14f
                    setTextColor(ContextCompat.getColor(this@PerfilClienteActivity, R.color.text_primary))
                    setPadding(0, 8, 0, 8)
                }
                binding.layoutListaReclamacoes.addView(textView)
            }
        } else {
            binding.cardAlertaReclamacao.visibility = View.GONE
            binding.layoutListaReclamacoes.removeAllViews()
            binding.tvSemReclamacoes.visibility = View.VISIBLE
        }
    }
}
