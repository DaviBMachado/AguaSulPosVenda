package com.aguasul.posvenda.ui.cliente

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aguasul.posvenda.databinding.ItemAtendimentoHistoricoBinding
import com.aguasul.posvenda.domain.model.Atendimento

class AtendimentoHistoricoAdapter : ListAdapter<Atendimento, AtendimentoHistoricoAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAtendimentoHistoricoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemAtendimentoHistoricoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Atendimento) {
            binding.tvTipoAtendimento.text = item.tipoAtendimento
            binding.tvDataAtendimento.text = item.dataHora
            binding.tvCondicaoAtendimento.text = "Condição: ${item.condicaoEquipamento}"
            binding.tvParecerAtendimento.text = item.parecerTecnico
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Atendimento>() {
            override fun areItemsTheSame(oldItem: Atendimento, newItem: Atendimento) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Atendimento, newItem: Atendimento) = oldItem == newItem
        }
    }
}
