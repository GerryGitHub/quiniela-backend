package com.quiniela.app.ui.pronosticos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.quiniela.app.databinding.ItemPronosticoBinding
import com.quiniela.app.model.PronosticoDTO

class PronosticoAdapter : ListAdapter<PronosticoDTO, PronosticoAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPronosticoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemPronosticoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PronosticoDTO) {
            binding.tvEquipos.text = "${item.partido.equipoLocal} vs ${item.partido.equipoVisitante}"
            binding.tvFecha.text = item.partido.fechaHora
            binding.tvMiPronostico.text = "Pronóstico: ${item.golesLocalPredicho} - ${item.golesVisitantePredicho}"
            binding.tvPuntos.text = "Puntos: ${item.puntosObtenidos}"
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<PronosticoDTO>() {
        override fun areItemsTheSame(oldItem: PronosticoDTO, newItem: PronosticoDTO) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: PronosticoDTO, newItem: PronosticoDTO) = oldItem == newItem
    }
}