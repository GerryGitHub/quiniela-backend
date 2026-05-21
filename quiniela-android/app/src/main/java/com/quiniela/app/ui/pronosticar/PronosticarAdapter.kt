package com.quiniela.app.ui.pronosticar

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.quiniela.app.databinding.ItemGrupoHeaderBinding
import com.quiniela.app.databinding.ItemPronosticarBinding
import com.quiniela.app.model.PartidoDTO

sealed class PronosticoItem {
    data class Header(val grupo: String) : PronosticoItem()
    data class PartidoItem(val partido: PartidoDTO) : PronosticoItem()
}

data class PartidoConPronostico(
    val partido: PartidoDTO,
    var golesLocalPredicho: Int = 0,
    var golesVisitantePredicho: Int = 0
)

class PronosticarAdapter : ListAdapter<PronosticoItem, RecyclerView.ViewHolder>(DiffCallback()) {

    private val pronosticos = mutableMapOf<Long, PartidoConPronostico>()

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_PARTIDO = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is PronosticoItem.Header -> TYPE_HEADER
            is PronosticoItem.PartidoItem -> TYPE_PARTIDO
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val binding = ItemGrupoHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                HeaderViewHolder(binding)
            }
            else -> {
                val binding = ItemPronosticarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is PronosticoItem.Header -> (holder as HeaderViewHolder).bind(item.grupo)
            is PronosticoItem.PartidoItem -> {
                val h = holder as ViewHolder
                val existente = pronosticos[item.partido.id]
                h.bind(item.partido, existente)
            }
        }
    }

    fun getPronosticos(): List<PartidoConPronostico> {
        return pronosticos.values.filter { it.partido.estado == "PENDIENTE" }.toList()
    }

    inner class HeaderViewHolder(private val binding: ItemGrupoHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(grupo: String) {
            binding.tvHeader.text = "Grupo $grupo"
        }
    }

    inner class ViewHolder(private val binding: ItemPronosticarBinding) : RecyclerView.ViewHolder(binding.root) {

        private var partidoId: Long = 0

        private val textWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val partido = (currentList.find { 
                    it is PronosticoItem.PartidoItem && (it as PronosticoItem.PartidoItem).partido.id == partidoId 
                } as? PronosticoItem.PartidoItem)?.partido ?: return
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
            binding.tvFlagLocal.text = obtenerBandera(partido.equipoLocal)
            binding.tvFlagVisitante.text = obtenerBandera(partido.equipoVisitante)
            
            val esEditable = partido.estado == "PENDIENTE"
            binding.etGolesLocal.isEnabled = esEditable
            binding.etGolesVisitante.isEnabled = esEditable
            
            val esFinalizado = partido.estado == "FINALIZADO"
            if (esFinalizado) {
                binding.tvEstado.text = "Finalizado"
                binding.tvEstado.setTextColor(0xFFF44336.toInt())
            } else if (partido.estado == "EN_CURSO") {
                binding.tvEstado.text = "En vivo"
                binding.tvEstado.setTextColor(0xFFFFC107.toInt())
            } else {
                binding.tvEstado.text = ""
            }
            
            val local = existente?.golesLocalPredicho ?: 0
            val visitante = existente?.golesVisitantePredicho ?: 0
            
            binding.etGolesLocal.removeTextChangedListener(textWatcher)
            binding.etGolesVisitante.removeTextChangedListener(textWatcher)
            
            if (esEditable) {
                binding.etGolesLocal.setText(local.toString())
                binding.etGolesVisitante.setText(visitante.toString())
            } else {
                binding.etGolesLocal.setText(if (local > 0) local.toString() else "")
                binding.etGolesVisitante.setText(if (visitante > 0) visitante.toString() else "")
            }
            
            binding.etGolesLocal.addTextChangedListener(textWatcher)
            binding.etGolesVisitante.addTextChangedListener(textWatcher)
            
            pronosticos[partido.id] = PartidoConPronostico(partido, local, visitante)
        }

        private fun obtenerBandera(pais: String): String {
            return when (pais.uppercase()) {
                "MEXICO", "MÉXICO" -> "🇲🇽"
                "ARGENTINA" -> "🇦🇷"
                "BRASIL" -> "🇧🇷"
                "URUGUAY" -> "🇺🇾"
                "COLOMBIA" -> "🇨🇴"
                "PERÚ", "PERU" -> "🇵🇪"
                "CHILE" -> "🇨🇱"
                "VENEZUELA" -> "🇻🇪"
                "ECUADOR" -> "🇪🇨"
                "PARAGUAY" -> "🇵🇾"
                "BOLIVIA" -> "🇧🇴"
                "PANAMÁ" -> "🇵🇦"
                "USA", "ESTADOS UNIDOS" -> "🇺🇸"
                "CANADÁ", "CANADA" -> "🇨🇦"
                else -> "⚽"
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<PronosticoItem>() {
        override fun areItemsTheSame(oldItem: PronosticoItem, newItem: PronosticoItem): Boolean {
            return when {
                oldItem is PronosticoItem.Header && newItem is PronosticoItem.Header -> oldItem.grupo == newItem.grupo
                oldItem is PronosticoItem.PartidoItem && newItem is PronosticoItem.PartidoItem -> oldItem.partido.id == newItem.partido.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: PronosticoItem, newItem: PronosticoItem): Boolean {
            return oldItem == newItem
        }
    }
}