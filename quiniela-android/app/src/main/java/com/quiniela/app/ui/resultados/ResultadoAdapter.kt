package com.quiniela.app.ui.resultados

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.quiniela.app.databinding.ItemResultadoBinding
import com.quiniela.app.model.PartidoDTO

class ResultadoAdapter(private val partidos: List<PartidoDTO>) : RecyclerView.Adapter<ResultadoAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemResultadoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(partidos[position])
    }

    override fun getItemCount() = partidos.size

    class ViewHolder(private val binding: ItemResultadoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(partido: PartidoDTO) {
            binding.tvEquipos.text = "${partido.equipoLocal} vs ${partido.equipoVisitante}"
            binding.tvFecha.text = partido.fechaHora
            
            val localGoals = partido.golesLocalReal ?: 0
            val visitorGoals = partido.golesVisitanteReal ?: 0
            binding.tvResultado.text = "$localGoals - $visitorGoals"
            binding.tvEstado.text = partido.estado
        }
    }
}