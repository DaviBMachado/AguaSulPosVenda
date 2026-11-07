package com.aguasul.posvenda.ui.clientes

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.aguasul.posvenda.PosVendaApplication
import com.aguasul.posvenda.R
import com.aguasul.posvenda.databinding.ActivityClientesBinding
import com.aguasul.posvenda.domain.model.Cliente
import com.aguasul.posvenda.ui.agenda.AgendaActivity
import com.aguasul.posvenda.ui.login.LoginActivity
import com.aguasul.posvenda.ui.perfil.PerfilClienteActivity
import com.aguasul.posvenda.ui.reclamacoes.ReclamacoesActivity
import com.aguasul.posvenda.ui.relatorios.RelatoriosActivity
import kotlinx.coroutines.launch

class ClientesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClientesBinding
    private val viewModel: ClientesViewModel by viewModels()
    private lateinit var adapter: ClienteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarClientes)

        setupRecyclerView()
        setupBusca()
        setupBottomNavigation()
        observeUiState()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_app_bar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.action_logout) {
            lifecycleScope.launch {
                (application as PosVendaApplication).repository.fazerLogout()
                val intent = Intent(this@ClientesActivity, LoginActivity::class.java).apply {
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

    override fun onResume() {
        super.onResume()
        binding.bottomNavigation.selectedItemId = R.id.nav_clientes
        viewModel.recarregar()
    }

    private fun setupRecyclerView() {
        adapter = ClienteAdapter { cliente ->
            abrirPerfilCliente(cliente)
        }
        binding.rvClientes.layoutManager = LinearLayoutManager(this)
        binding.rvClientes.adapter = adapter
    }

    private fun setupBusca() {
        binding.etBuscaCliente.doOnTextChanged { text, _, _, _ ->
            viewModel.filtrar(text?.toString() ?: "")
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_clientes
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_clientes -> true
                R.id.nav_agenda -> {
                    val intent = Intent(this, AgendaActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    }
                    startActivity(intent)
                    transicaoSuave()
                    true
                }
                R.id.nav_reclamacoes -> {
                    val intent = Intent(this, ReclamacoesActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    }
                    startActivity(intent)
                    transicaoSuave()
                    true
                }
                R.id.nav_relatorios -> {
                    val intent = Intent(this, RelatoriosActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    }
                    startActivity(intent)
                    transicaoSuave()
                    true
                }
                else -> false
            }
        }
    }

    private fun transicaoSuave() {
        if (android.os.Build.VERSION.SDK_INT >= 34) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 0, 0)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is ClientesUiState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.layoutEmpty.visibility = View.GONE
                        }
                        is ClientesUiState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            binding.layoutEmpty.visibility = View.GONE
                            binding.rvClientes.visibility = View.VISIBLE
                            adapter.submitList(state.clientes)
                        }
                        is ClientesUiState.Empty -> {
                            binding.progressBar.visibility = View.GONE
                            binding.rvClientes.visibility = View.GONE
                            binding.layoutEmpty.visibility = View.VISIBLE
                        }
                        is ClientesUiState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.layoutEmpty.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    private fun abrirPerfilCliente(cliente: Cliente) {
        val intent = Intent(this, PerfilClienteActivity::class.java).apply {
            putExtra(EXTRA_CLIENTE_ID, cliente.id)
        }
        startActivity(intent)
    }

    companion object {
        const val EXTRA_CLIENTE_ID = "extra_cliente_id"
    }
}
