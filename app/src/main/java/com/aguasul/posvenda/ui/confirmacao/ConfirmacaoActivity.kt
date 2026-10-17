package com.Aquasul.posvenda.ui.confirmacao

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.Aquasul.posvenda.R
import com.Aquasul.posvenda.databinding.ActivityConfirmacaoBinding
import com.Aquasul.posvenda.ui.agenda.AgendaActivity

class ConfirmacaoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfirmacaoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfirmacaoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val clienteNome = intent.getStringExtra(EXTRA_CLIENTE_NOME)
            ?: intent.getStringExtra("com.Aquasul.posvenda.CLIENTE_NOME")
            ?: intent.getStringExtra("com.Aquasul.posvenda.extra.CONFIRM_CLIENTE_NOME")
            ?: getString(R.string.cliente_nao_informado)

        val tipoAtendimento = intent.getStringExtra(EXTRA_TIPO_ATENDIMENTO)
            ?: intent.getStringExtra("com.Aquasul.posvenda.TIPO_ATENDIMENTO")
            ?: intent.getStringExtra("com.Aquasul.posvenda.extra.CONFIRM_TIPO")
            ?: getString(R.string.tipo_nao_informado)

        val dataHora = intent.getStringExtra(EXTRA_DATA_HORA)
            ?: intent.getStringExtra("com.Aquasul.posvenda.DATA_HORA")
            ?: intent.getStringExtra("com.Aquasul.posvenda.extra.CONFIRM_DATA_HORA")
            ?: getString(R.string.data_hora_nao_informada)

        val condicao = intent.getStringExtra(EXTRA_CONDICAO)
            ?: intent.getStringExtra(EXTRA_CONDICAO_EQUIPAMENTO)
            ?: intent.getStringExtra("com.Aquasul.posvenda.CONDICAO_EQUIPAMENTO")
            ?: intent.getStringExtra("com.Aquasul.posvenda.extra.CONFIRM_CONDICAO")

        binding.tvResumoCliente.text = getString(R.string.resumo_cliente_label, clienteNome)
        binding.tvResumoTipo.text = getString(R.string.resumo_tipo_label, tipoAtendimento)
        binding.tvResumoDataHora.text = getString(R.string.resumo_data_hora_label, dataHora)

        if (!condicao.isNullOrBlank()) {
            binding.tvResumoCondicao.visibility = View.VISIBLE
            binding.tvResumoCondicao.text = getString(R.string.resumo_condicao_label, condicao)
        } else {
            binding.tvResumoCondicao.visibility = View.GONE
        }

        binding.btnVoltarAgenda.setOnClickListener {
            voltarParaAgenda()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                voltarParaAgenda()
            }
        })
    }

    private fun voltarParaAgenda() {
        val intent = Intent(this, AgendaActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
        finish()
    }

    companion object {
        const val EXTRA_CLIENTE_NOME = "extra_cliente_nome"
        const val EXTRA_TIPO_ATENDIMENTO = "extra_tipo_atendimento"
        const val EXTRA_DATA_HORA = "extra_data_hora"
        const val EXTRA_CONDICAO = "extra_condicao"
        const val EXTRA_CONDICAO_EQUIPAMENTO = "extra_condicao_equipamento"
        const val EXTRA_HOUVE_RECLAMACAO = "extra_houve_reclamacao"
        const val EXTRA_ATENDIMENTO_ID = "extra_atendimento_id"

        fun newIntent(
            context: Context,
            clienteNome: String,
            tipoAtendimento: String,
            dataHora: String,
            condicao: String? = null,
            houveReclamacao: Boolean = false,
            atendimentoId: String? = null
        ): Intent {
            return Intent(context, ConfirmacaoActivity::class.java).apply {
                putExtra(EXTRA_CLIENTE_NOME, clienteNome)
                putExtra(EXTRA_TIPO_ATENDIMENTO, tipoAtendimento)
                putExtra(EXTRA_DATA_HORA, dataHora)
                condicao?.let {
                    putExtra(EXTRA_CONDICAO, it)
                    putExtra(EXTRA_CONDICAO_EQUIPAMENTO, it)
                }
                putExtra(EXTRA_HOUVE_RECLAMACAO, houveReclamacao)
                atendimentoId?.let { putExtra(EXTRA_ATENDIMENTO_ID, it) }
            }
        }
    }
}
