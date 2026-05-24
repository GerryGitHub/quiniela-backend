package com.quiniela.app.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.quiniela.app.databinding.ItemPartidoEnVivoBinding
import com.quiniela.app.model.PartidoDTO

class PartidoEnVivoAdapter(
    private val onClick: (PartidoDTO) -> Unit = {}
) : RecyclerView.Adapter<PartidoEnVivoAdapter.ViewHolder>() {

    private var partidos: List<PartidoDTO> = emptyList()

    fun submitList(newList: List<PartidoDTO>) {
        partidos = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPartidoEnVivoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(partidos[position])
    }

    override fun getItemCount() = partidos.size

    inner class ViewHolder(
        private val binding: ItemPartidoEnVivoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(partido: PartidoDTO) {
            android.util.Log.d("Adapter", "Binding: ${partido.equipoLocal} ${partido.golesLocalReal}-${partido.golesVisitanteReal}")
            binding.tvEquipoLocal.text = partido.equipoLocal
            binding.tvEquipoVisitante.text = partido.equipoVisitante
            binding.tvMarcador.text = "${partido.golesLocalReal ?: 0} - ${partido.golesVisitanteReal ?: 0}"
            binding.root.setOnClickListener { onClick(partido) }
        }
    }
}