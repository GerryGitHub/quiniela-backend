package com.quiniela.app.ui.pronosticar

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.quiniela.app.databinding.ItemPronosticarBinding
import com.quiniela.app.model.PartidoDTO

data class PartidoConPronostico(
    val partido: PartidoDTO,
    var golesLocalPredicho: Int = 0,
    var golesVisitantePredicho: Int = 0
)

class PronosticarAdapter : ListAdapter<PartidoDTO, PronosticarAdapter.ViewHolder>(DiffCallback()) {

    private val pronosticos = mutableMapOf<Long, PartidoConPronostico>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPronosticarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val partido = getItem(position)
        val existente = pronosticos[partido.id]
        holder.bind(partido, existente)
    }

    fun getPronosticos(): List<PartidoConPronostico> {
        return pronosticos.values.toList()
    }

    inner class ViewHolder(private val binding: ItemPronosticarBinding) : RecyclerView.ViewHolder(binding.root) {

        private var partidoId: Long = 0

        private val textWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val partido = currentList.find { it.id == partidoId } ?: return
                val local = binding.etGolesLocal.text.toString().toIntOrNull() ?: 0
                val visitante = binding.etGolesVisitante.text.toString().toIntOrNull() ?: 0
                
                pronosticos[partidoId] = PartidoConPronostico(partido, local, visitante)
            }
        }

        init {
            binding.etGolesLocal.addTextChangedListener(textWatcher)
            binding.etGolesVisitante.addTextChangedListener(textWatcher)
        }

        fun bind(partido: PartidoDTO, existente: PartidoConPronostico?) {
            partidoId = partido.id
            binding.tvFecha.text = partido.fechaHora
            binding.tvLocal.text = partido.equipoLocal
            binding.tvVisitante.text = partido.equipoVisitante
            
            val local = existente?.golesLocalPredicho ?: 0
            val visitante = existente?.golesVisitantePredicho ?: 0
            
            binding.etGolesLocal.removeTextChangedListener(textWatcher)
            binding.etGolesVisitante.removeTextChangedListener(textWatcher)
            
            binding.etGolesLocal.setText(local.toString())
            binding.etGolesVisitante.setText(visitante.toString())
            
            binding.etGolesLocal.addTextChangedListener(textWatcher)
            binding.etGolesVisitante.addTextChangedListener(textWatcher)
            
            pronosticos[partido.id] = PartidoConPronostico(partido, local, visitante)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<PartidoDTO>() {
        override fun areItemsTheSame(oldItem: PartidoDTO, newItem: PartidoDTO) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: PartidoDTO, newItem: PartidoDTO) = oldItem == newItem
    }
}