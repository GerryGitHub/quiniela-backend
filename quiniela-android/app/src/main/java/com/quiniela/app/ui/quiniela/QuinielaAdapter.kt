package com.quiniela.app.ui.quiniela

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.quiniela.app.R
import com.quiniela.app.databinding.ItemGrupoHeaderBinding
import com.quiniela.app.databinding.ItemQuinielaBinding
import com.quiniela.app.model.QuinielaResumenDTO

sealed class QuinielaListItem {
    data class Header(val label: String) : QuinielaListItem()
    data class Quiniela(val quiniela: QuinielaResumenDTO) : QuinielaListItem()
}

class QuinielaAdapter(
    private val onItemClick: (QuinielaResumenDTO) -> Unit,
    private val onShareClick: (QuinielaResumenDTO) -> Unit
) : ListAdapter<QuinielaListItem, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_QUINIELA = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is QuinielaListItem.Header -> TYPE_HEADER
            is QuinielaListItem.Quiniela -> TYPE_QUINIELA
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val binding = ItemGrupoHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                HeaderViewHolder(binding)
            }
            else -> {
                val binding = ItemQuinielaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is QuinielaListItem.Header -> (holder as HeaderViewHolder).bind(item.label)
            is QuinielaListItem.Quiniela -> (holder as ViewHolder).bind(item.quiniela)
        }
    }

    fun submitSections(activas: List<QuinielaResumenDTO>, finalizadas: List<QuinielaResumenDTO>) {
        val items = mutableListOf<QuinielaListItem>()
        if (activas.isNotEmpty()) {
            items.add(QuinielaListItem.Header("Activas"))
            activas.forEach { items.add(QuinielaListItem.Quiniela(it)) }
        }
        if (finalizadas.isNotEmpty()) {
            items.add(QuinielaListItem.Header("Finalizadas"))
            finalizadas.forEach { items.add(QuinielaListItem.Quiniela(it)) }
        }
        submitList(items)
    }

    inner class HeaderViewHolder(private val binding: ItemGrupoHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(label: String) {
            binding.tvHeader.text = label
        }
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

    class DiffCallback : DiffUtil.ItemCallback<QuinielaListItem>() {
        override fun areItemsTheSame(oldItem: QuinielaListItem, newItem: QuinielaListItem): Boolean {
            return when {
                oldItem is QuinielaListItem.Header && newItem is QuinielaListItem.Header -> oldItem.label == newItem.label
                oldItem is QuinielaListItem.Quiniela && newItem is QuinielaListItem.Quiniela -> oldItem.quiniela.id == newItem.quiniela.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: QuinielaListItem, newItem: QuinielaListItem): Boolean {
            return oldItem == newItem
        }
    }
}
