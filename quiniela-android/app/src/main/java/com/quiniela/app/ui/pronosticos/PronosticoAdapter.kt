package com.quiniela.app.ui.pronosticos

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.quiniela.app.databinding.ItemGrupoHeaderBinding
import com.quiniela.app.databinding.ItemPronosticoBinding
import com.quiniela.app.model.PronosticoDTO

sealed class MisPronosticosItem {
    data class Header(val grupo: String) : MisPronosticosItem()
    data class PronosticoItem(val pronostico: PronosticoDTO) : MisPronosticosItem()
}

class PronosticoAdapter : ListAdapter<MisPronosticosItem, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_PARTIDO = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is MisPronosticosItem.Header -> TYPE_HEADER
            is MisPronosticosItem.PronosticoItem -> TYPE_PARTIDO
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val binding = ItemGrupoHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                HeaderViewHolder(binding)
            }
            else -> {
                val binding = ItemPronosticoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is MisPronosticosItem.Header -> (holder as HeaderViewHolder).bind(item.grupo)
            is MisPronosticosItem.PronosticoItem -> (holder as ViewHolder).bind(item.pronostico)
        }
    }

    inner class HeaderViewHolder(private val binding: ItemGrupoHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(grupo: String) {
            binding.tvHeader.text = grupo
        }
    }

    inner class ViewHolder(private val binding: ItemPronosticoBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(item: PronosticoDTO) {
            binding.tvEquipos.text = "${item.partido.equipoLocal} vs ${item.partido.equipoVisitante}"
            binding.tvFecha.text = item.partido.fechaHora
            binding.tvMiPronostico.text = "Pronóstico: ${item.golesLocalPredicho} - ${item.golesVisitantePredicho}"
            binding.tvPuntos.text = "Puntos: ${item.puntosObtenidos}"
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<MisPronosticosItem>() {
        override fun areItemsTheSame(oldItem: MisPronosticosItem, newItem: MisPronosticosItem): Boolean {
            return when {
                oldItem is MisPronosticosItem.Header && newItem is MisPronosticosItem.Header -> oldItem.grupo == newItem.grupo
                oldItem is MisPronosticosItem.PronosticoItem && newItem is MisPronosticosItem.PronosticoItem -> oldItem.pronostico.id == newItem.pronostico.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: MisPronosticosItem, newItem: MisPronosticosItem): Boolean {
            return oldItem == newItem
        }
    }
}