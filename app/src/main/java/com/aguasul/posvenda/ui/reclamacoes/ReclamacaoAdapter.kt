package com.aguasul.posvenda.ui.reclamacoes

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aguasul.posvenda.R
import com.aguasul.posvenda.databinding.ItemReclamacaoBinding
import com.aguasul.posvenda.domain.model.OrigemReclamacao
import com.aguasul.posvenda.domain.model.Reclamacao
import com.aguasul.posvenda.domain.model.StatusReclamacao

class ReclamacaoAdapter(
    private val isTecnico: Boolean = true,
    private val onReclamacaoClick: ((Reclamacao) -> Unit)? = null
) : ListAdapter<Reclamacao, ReclamacaoAdapter.ReclamacaoViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReclamacaoViewHolder {
        val binding = ItemReclamacaoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReclamacaoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReclamacaoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ReclamacaoViewHolder(
        private val binding: ItemReclamacaoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Reclamacao) {
            val context = itemView.context

            binding.tvClienteNome.text = item.clienteNome
            binding.tvDataAbertura.text = item.dataAbertura
            binding.tvDescricao.text = item.descricao

            // Origem badge
            binding.tvOrigemBadge.text = when (item.origem) {
                OrigemReclamacao.CLIENTE -> context.getString(R.string.origem_cliente)
                OrigemReclamacao.VISITA_TECNICA -> context.getString(R.string.origem_visita)
            }

            // Status badge styling
            when (item.status) {
                StatusReclamacao.ABERTA -> {
                    binding.tvStatusBadge.text = context.getString(R.string.status_aberta)
                    binding.tvStatusBadge.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.alerta_reclamacao_bg)
                    )
                    binding.tvStatusBadge.setTextColor(
                        ContextCompat.getColor(context, R.color.alerta_reclamacao_text)
                    )
                }
                StatusReclamacao.EM_ANDAMENTO -> {
                    binding.tvStatusBadge.text = context.getString(R.string.status_em_andamento)
                    binding.tvStatusBadge.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.status_em_andamento_bg)
                    )
                    binding.tvStatusBadge.setTextColor(
                        ContextCompat.getColor(context, R.color.status_em_andamento_text)
                    )
                }
                StatusReclamacao.RESOLVIDA -> {
                    binding.tvStatusBadge.text = context.getString(R.string.status_resolvida)
                    binding.tvStatusBadge.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.status_concluida_bg)
                    )
                    binding.tvStatusBadge.setTextColor(
                        ContextCompat.getColor(context, R.color.status_concluida_text)
                    )
                }
            }

            // Parecer de Resolução
            if (!item.resolucaoTecnica.isNullOrBlank()) {
                binding.layoutResolucao.visibility = View.VISIBLE
                binding.tvResolucaoTecnica.text = item.resolucaoTecnica
            } else {
                binding.layoutResolucao.visibility = View.GONE
            }

            // Ação hint para técnico
            val podeResolver = isTecnico && item.status != StatusReclamacao.RESOLVIDA
            binding.tvAcaoHint.visibility = if (podeResolver) View.VISIBLE else View.GONE

            if (podeResolver && onReclamacaoClick != null) {
                itemView.setOnClickListener { onReclamacaoClick.invoke(item) }
            } else {
                itemView.setOnClickListener(null)
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Reclamacao>() {
            override fun areItemsTheSame(oldItem: Reclamacao, newItem: Reclamacao): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Reclamacao, newItem: Reclamacao): Boolean {
                return oldItem == newItem
            }
        }
    }
}
