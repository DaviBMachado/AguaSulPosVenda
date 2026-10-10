package com.aguasul.posvenda.ui.agenda

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aguasul.posvenda.R
import com.aguasul.posvenda.databinding.ItemVisitaBinding
import com.aguasul.posvenda.domain.model.Visita

class VisitaAdapter(
    private val onVisitaClick: (Visita) -> Unit
) : ListAdapter<Visita, VisitaAdapter.VisitaViewHolder>(VisitaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitaViewHolder {
        val binding = ItemVisitaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VisitaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VisitaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VisitaViewHolder(private val binding: ItemVisitaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(visita: Visita) {
            binding.tvClienteNome.text = visita.clienteNome
            binding.tvTipoVisita.text = "Visita ${visita.tipo}"
            binding.tvDataHorario.text = visita.dataHorario

            if (visita.isConcluida()) {
                binding.tvStatusBadge.text = itemView.context.getString(R.string.status_concluida)
                binding.tvStatusBadge.setTextColor(ContextCompat.getColor(itemView.context, R.color.status_concluida_text))
                binding.tvStatusBadge.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.status_concluida_bg))
                binding.tvAcaoHint.visibility = View.GONE
            } else {
                binding.tvStatusBadge.text = itemView.context.getString(R.string.status_pendente)
                binding.tvStatusBadge.setTextColor(ContextCompat.getColor(itemView.context, R.color.status_pendente_text))
                binding.tvStatusBadge.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.status_pendente_bg))
                binding.tvAcaoHint.visibility = View.VISIBLE
            }

            binding.cardVisita.setOnClickListener {
                onVisitaClick(visita)
            }
        }
    }

    class VisitaDiffCallback : DiffUtil.ItemCallback<Visita>() {
        override fun areItemsTheSame(oldItem: Visita, newItem: Visita): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Visita, newItem: Visita): Boolean = oldItem == newItem
    }
}
