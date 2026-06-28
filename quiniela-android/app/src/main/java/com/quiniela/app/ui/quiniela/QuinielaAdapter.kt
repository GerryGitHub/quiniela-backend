package com.quiniela.app.ui.quiniela

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.quiniela.app.R
import com.quiniela.app.databinding.ItemQuinielaBinding
import com.quiniela.app.model.QuinielaResumenDTO

class QuinielaAdapter(
    private val onItemClick: (QuinielaResumenDTO) -> Unit,
    private val onShareClick: (QuinielaResumenDTO) -> Unit
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
            binding.tvPuntos.text = quiniela.puntosTotales.toString()

            val finalizada = quiniela.estado == "FINALIZADA"
            binding.tvEstado.text = quiniela.estado
            binding.tvEstado.visibility = View.VISIBLE
            val bg = binding.tvEstado.background?.mutate() as? GradientDrawable
            if (bg != null) {
                bg.setColor(Color.parseColor(if (finalizada) "#E53935" else "#43A047"))
            }
            
            binding.root.setOnClickListener { onItemClick(quiniela) }
            binding.btnShare.setOnClickListener { onShareClick(quiniela) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<QuinielaResumenDTO>() {
        override fun areItemsTheSame(oldItem: QuinielaResumenDTO, newItem: QuinielaResumenDTO) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: QuinielaResumenDTO, newItem: QuinielaResumenDTO) = oldItem == newItem
    }
}