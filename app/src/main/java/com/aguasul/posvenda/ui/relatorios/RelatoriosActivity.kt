package com.aguasul.posvenda.ui.relatorios

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.aguasul.posvenda.R
import com.aguasul.posvenda.databinding.ActivityRelatoriosBinding
import com.aguasul.posvenda.domain.model.ClienteCritico
import com.aguasul.posvenda.domain.model.MetricasRelatorio
import com.aguasul.posvenda.ui.agenda.AgendaActivity
import com.aguasul.posvenda.ui.clientes.ClientesActivity
import com.aguasul.posvenda.ui.login.LoginActivity
import com.aguasul.posvenda.ui.reclamacoes.ReclamacoesActivity
import kotlinx.coroutines.launch

class RelatoriosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRelatoriosBinding
    private val viewModel: RelatoriosViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRelatoriosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarRelatorios)

        setupBottomNavigation()
        observeUiState()
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNavigation.selectedItemId = R.id.nav_relatorios
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

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_relatorios
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_relatorios -> true
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
                R.id.nav_reclamacoes -> {
                    val intent = Intent(this, ReclamacoesActivity::class.java).apply {
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
                        is RelatoriosUiState.Loading -> {
                            binding.progressBarRelatorios.visibility = View.VISIBLE
                        }
                        is RelatoriosUiState.Success -> {
                            binding.progressBarRelatorios.visibility = View.GONE
                            atualizarCards(state.metricas)
                        }
                        is RelatoriosUiState.Error -> {
                            binding.progressBarRelatorios.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun atualizarCards(metricas: MetricasRelatorio) {
        // Card 1: Usabilidade
        binding.tvUsabilidadeAlta.text = "${metricas.usabilidadeAlta} clientes (${metricas.percentualAlta}%)"
        binding.progressAlta.setProgressCompat(metricas.percentualAlta, true)

        binding.tvUsabilidadeMedia.text = "${metricas.usabilidadeMedia} clientes (${metricas.percentualMedia}%)"
        binding.progressMedia.setProgressCompat(metricas.percentualMedia, true)

        binding.tvUsabilidadeBaixa.text = "${metricas.usabilidadeBaixa} clientes (${metricas.percentualBaixa}%)"
        binding.progressBaixa.setProgressCompat(metricas.percentualBaixa, true)

        // Card 2: Eficácia do Pós-Venda
        binding.tvTaxaResolucao.text = "${metricas.taxaResolucaoPercentual}%"
        binding.progressTaxaResolucao.setProgressCompat(metricas.taxaResolucaoPercentual, true)
        binding.tvReclamacoesPendentes.text = "Pendentes: ${metricas.reclamacoesPendentes}"
        binding.tvReclamacoesResolvidas.text = "Resolvidas: ${metricas.reclamacoesResolvidas}"

        // Card 3: Serviços Executados
        binding.tvTotalVisitas.text = getString(R.string.total_visitas_label, metricas.totalVisitas)
        binding.tvVisitasConcluidas.text = getString(R.string.visitas_concluidas_label, metricas.visitasConcluidas)
        binding.tvVisitasPreventivas.text = "Preventivas: ${metricas.visitasPreventivas}"
        binding.tvVisitasCorretivas.text = "Corretivas: ${metricas.visitasCorretivas}"
        binding.tvVisitasOutras.text = "Outras: ${metricas.visitasOutras}"

        // Card 4: Clientes Críticos
        renderizarClientesCriticos(metricas.clientesCriticos)
    }

    private fun renderizarClientesCriticos(clientesCriticos: List<ClienteCritico>) {
        binding.containerClientesCriticos.removeAllViews()

        if (clientesCriticos.isEmpty()) {
            binding.tvNenhumCritico.visibility = View.VISIBLE
            return
        }

        binding.tvNenhumCritico.visibility = View.GONE

        clientesCriticos.forEach { critico ->
            val itemLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 8, 0, 8)
                }
                gravity = Gravity.CENTER_VERTICAL
            }

            val tvNome = TextView(this).apply {
                text = critico.clienteNome
                textSize = 14f
                setTextColor(ContextCompat.getColor(this@RelatoriosActivity, R.color.text_primary))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val tvBadge = TextView(this).apply {
                val plural = if (critico.totalReclamacoesAbertas > 1) "queixas abertas" else "queixa aberta"
                text = "${critico.totalReclamacoesAbertas} $plural"
                textSize = 11f
                setTextColor(ContextCompat.getColor(this@RelatoriosActivity, R.color.alerta_reclamacao_text))
                setBackgroundResource(R.drawable.bg_status_badge)
                backgroundTintList = ContextCompat.getColorStateList(this@RelatoriosActivity, R.color.alerta_reclamacao_bg)
                setPadding(20, 8, 20, 8)
            }

            itemLayout.addView(tvNome)
            itemLayout.addView(tvBadge)
            binding.containerClientesCriticos.addView(itemLayout)
        }
    }
}
