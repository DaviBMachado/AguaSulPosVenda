package com.aguasul.posvenda.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.aguasul.posvenda.databinding.ActivityLoginBinding
import com.aguasul.posvenda.domain.model.SessaoUsuario
import com.aguasul.posvenda.domain.model.TipoPerfil
import com.aguasul.posvenda.ui.agenda.AgendaActivity
import com.aguasul.posvenda.ui.cliente.AreaClienteActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        setupDemoShortcuts()
        observeUiState()
    }

    private fun setupListeners() {
        binding.etEmail.doOnTextChanged { _, _, _, _ ->
            binding.tilEmail.error = null
            binding.tvErroLogin.visibility = View.GONE
        }

        binding.etSenha.doOnTextChanged { _, _, _, _ ->
            binding.tilSenha.error = null
            binding.tvErroLogin.visibility = View.GONE
        }

        binding.btnEntrar.setOnClickListener {
            val email = binding.etEmail.text?.toString().orEmpty()
            val senha = binding.etSenha.text?.toString().orEmpty()
            viewModel.fazerLogin(email, senha)
        }
    }

    private fun setupDemoShortcuts() {
        // Botão de atalho técnico
        binding.btnDemoTecnico.setOnClickListener {
            viewModel.fazerLoginTecnicoDemo()
        }

        // Dropdown de clientes simulados
        val nomesClientes = viewModel.clientesDemo.map { it.nomeExibicao }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, nomesClientes)
        binding.actvDemoCliente.setAdapter(adapter)

        binding.actvDemoCliente.setOnItemClickListener { _, _, position, _ ->
            val clienteEscolhido = viewModel.clientesDemo.getOrNull(position)
            clienteEscolhido?.clienteId?.let { cid ->
                viewModel.fazerLoginClienteDemo(cid)
            }
        }
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is LoginUiState.Idle -> {
                            binding.progressBar.visibility = View.GONE
                            binding.btnEntrar.isEnabled = true
                            binding.tvErroLogin.visibility = View.GONE
                        }
                        is LoginUiState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.btnEntrar.isEnabled = false
                            binding.tvErroLogin.visibility = View.GONE
                        }
                        is LoginUiState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            navegarParaPerfil(state.sessao)
                        }
                        is LoginUiState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.btnEntrar.isEnabled = true
                            binding.tvErroLogin.text = state.message
                            binding.tvErroLogin.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    private fun navegarParaPerfil(sessao: SessaoUsuario) {
        val intent = when (sessao.perfil) {
            TipoPerfil.TECNICO -> Intent(this, AgendaActivity::class.java)
            TipoPerfil.CLIENTE -> Intent(this, AreaClienteActivity::class.java).apply {
                putExtra(AreaClienteActivity.EXTRA_CLIENTE_ID, sessao.clienteId)
            }
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
