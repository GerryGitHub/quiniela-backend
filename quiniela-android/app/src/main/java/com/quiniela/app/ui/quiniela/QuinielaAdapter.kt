package com.quiniela.app.ui.quiniela

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.quiniela.app.databinding.ItemQuinielaBinding
import com.quiniela.app.model.QuinielaResumenDTO

class QuinielaAdapter(
    private val onItemClick: (QuinielaResumenDTO) -> Unit
) : ListAdapter<QuinielaResumenDTO, QuinielaAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemQuinielaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemQuinielaBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(quiniela: QuinielaResumenDTO) {
            binding.tvNombre.text = quiniela.nombre
            binding.tvCodigo.text = "Código: ${quiniela.codigoInvitacion}"
            binding.tvPuntos.text = "${quiniela.puntosTotales} pts"
            
            binding.root.setOnClickListener { onItemClick(quiniela) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<QuinielaResumenDTO>() {
        override fun areItemsTheSame(oldItem: QuinielaResumenDTO, newItem: QuinielaResumenDTO) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: QuinielaResumenDTO, newItem: QuinielaResumenDTO) = oldItem == newItem
    }
}