package com.aguasul.posvenda.ui.reclamacoes

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.aguasul.posvenda.R
import com.aguasul.posvenda.databinding.ActivityReclamacoesBinding
import com.aguasul.posvenda.domain.model.Reclamacao
import com.aguasul.posvenda.ui.agenda.AgendaActivity
import com.aguasul.posvenda.ui.clientes.ClientesActivity
import com.aguasul.posvenda.ui.login.LoginActivity
import com.aguasul.posvenda.ui.relatorios.RelatoriosActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class ReclamacoesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReclamacoesBinding
    private val viewModel: ReclamacoesViewModel by viewModels()
    private lateinit var adapter: ReclamacaoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReclamacoesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarReclamacoes)

        setupRecyclerView()
        setupFiltros()
        setupBottomNavigation()
        observeUiState()
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNavigation.selectedItemId = R.id.nav_reclamacoes
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

    private fun setupRecyclerView() {
        adapter = ReclamacaoAdapter(isTecnico = true) { reclamacao ->
            exibirDialogResolverReclamacao(reclamacao)
        }
        binding.rvReclamacoes.layoutManager = LinearLayoutManager(this)
        binding.rvReclamacoes.adapter = adapter
    }

    private fun setupFiltros() {
        binding.chipGroupFiltros.setOnCheckedStateChangeListener { _, checkedIds ->
            val filtro = when {
                checkedIds.contains(R.id.chipAbertas) -> FiltroReclamacao.ABERTAS
                checkedIds.contains(R.id.chipResolvidas) -> FiltroReclamacao.RESOLVIDAS
                else -> FiltroReclamacao.TODAS
            }
            viewModel.aplicarFiltro(filtro)
        }
    }

    private fun exibirDialogResolverReclamacao(reclamacao: Reclamacao) {
        val input = EditText(this).apply {
            hint = getString(R.string.hint_parecer_resolucao)
            setPadding(48, 32, 48, 32)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_resolver_titulo)
            .setMessage(getString(R.string.dialog_resolver_msg))
            .setView(input)
            .setPositiveButton(R.string.btn_confirmar_resolucao) { _, _ ->
                val parecer = input.text.toString().trim()
                if (parecer.isNotBlank()) {
                    viewModel.resolverReclamacao(reclamacao.id, parecer)
                    Toast.makeText(this, "Reclamação resolvida com sucesso!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Informe o parecer técnico para concluir.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.btn_cancelar, null)
            .show()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_reclamacoes
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_reclamacoes -> true
                R.id.nav_agenda -> {
                    val intent = Intent(this, AgendaActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    }
                    startActivity(intent)
                    transicaoSuave()
                    true
                }
                R.id.nav_clientes -> {
                    val intent = Intent(this, ClientesActivity::class.java).apply {
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
                        is ReclamacoesUiState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.layoutEmpty.visibility = View.GONE
                        }
                        is ReclamacoesUiState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            binding.layoutEmpty.visibility = View.GONE
                            binding.rvReclamacoes.visibility = View.VISIBLE
                            adapter.submitList(state.reclamacoes)
                        }
                        is ReclamacoesUiState.Empty -> {
                            binding.progressBar.visibility = View.GONE
                            binding.rvReclamacoes.visibility = View.GONE
                            binding.layoutEmpty.visibility = View.VISIBLE
                        }
                        is ReclamacoesUiState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.rvReclamacoes.visibility = View.GONE
                            binding.layoutEmpty.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }
}
