package com.Aquasul.posvenda.ui.clientes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.Aquasul.posvenda.databinding.ItemClienteBinding
import com.Aquasul.posvenda.domain.model.Cliente

class ClienteAdapter(
    private val onClienteClick: (Cliente) -> Unit
) : ListAdapter<Cliente, ClienteAdapter.ClienteViewHolder>(ClienteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteViewHolder {
        val binding = ItemClienteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ClienteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ClienteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ClienteViewHolder(private val binding: ItemClienteBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(cliente: Cliente) {
            binding.tvClienteNome.text = cliente.nome
            binding.tvClienteContato.text = cliente.contato
            binding.tvResumoAndamento.text = cliente.resumoAndamento()

            if (cliente.possuiReclamacoes()) {
                binding.layoutAlertaReclamacao.visibility = View.VISIBLE
            } else {
                binding.layoutAlertaReclamacao.visibility = View.GONE
            }

            binding.cardCliente.setOnClickListener {
                onClienteClick(cliente)
            }
        }
    }

    class ClienteDiffCallback : DiffUtil.ItemCallback<Cliente>() {
        override fun areItemsTheSame(oldItem: Cliente, newItem: Cliente): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Cliente, newItem: Cliente): Boolean = oldItem == newItem
    }
}
