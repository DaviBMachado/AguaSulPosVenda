package com.aguasul.posvenda.ui.agenda

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.aguasul.posvenda.R
import com.aguasul.posvenda.databinding.ActivityAgendaBinding
import com.aguasul.posvenda.domain.model.Visita
import com.aguasul.posvenda.ui.clientes.ClientesActivity
import com.aguasul.posvenda.ui.formulario.FormularioAtendimentoActivity
import kotlinx.coroutines.launch

class AgendaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAgendaBinding
    private val viewModel: AgendaViewModel by viewModels()
    private lateinit var adapter: VisitaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgendaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupBottomNavigation()
        observeUiState()
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNavigation.selectedItemId = R.id.nav_agenda
        viewModel.carregarVisitas()
    }

    private fun setupRecyclerView() {
        adapter = VisitaAdapter { visita ->
            abrirFormularioAtendimento(visita)
        }
        binding.rvVisitas.layoutManager = LinearLayoutManager(this)
        binding.rvVisitas.adapter = adapter
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_agenda
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_agenda -> true
                R.id.nav_clientes -> {
                    val intent = Intent(this, ClientesActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    }
                    startActivity(intent)
                    if (android.os.Build.VERSION.SDK_INT >= 34) {
                        overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 0, 0)
                    } else {
                        @Suppress("DEPRECATION")
                        overridePendingTransition(0, 0)
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is AgendaUiState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.layoutEmpty.visibility = View.GONE
                        }
                        is AgendaUiState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            binding.layoutEmpty.visibility = View.GONE
                            binding.rvVisitas.visibility = View.VISIBLE
                            adapter.submitList(state.visitas)
                        }
                        is AgendaUiState.Empty -> {
                            binding.progressBar.visibility = View.GONE
                            binding.rvVisitas.visibility = View.GONE
                            binding.layoutEmpty.visibility = View.VISIBLE
                        }
                        is AgendaUiState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.layoutEmpty.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    private fun abrirFormularioAtendimento(visita: Visita) {
        val intent = Intent(this, FormularioAtendimentoActivity::class.java).apply {
            putExtra(EXTRA_VISITA_ID, visita.id)
            putExtra(EXTRA_CLIENTE_ID, visita.clienteId)
            putExtra(EXTRA_CLIENTE_NOME, visita.clienteNome)
            putExtra(EXTRA_DATA_HORARIO, visita.dataHorario)
            putExtra(EXTRA_TIPO_VISITA, visita.tipo)
        }
        startActivity(intent)
    }

    companion object {
        const val EXTRA_VISITA_ID = "extra_visita_id"
        const val EXTRA_CLIENTE_ID = "extra_cliente_id"
        const val EXTRA_CLIENTE_NOME = "extra_cliente_nome"
        const val EXTRA_DATA_HORARIO = "extra_data_horario"
        const val EXTRA_TIPO_VISITA = "extra_tipo_visita"
    }
}
