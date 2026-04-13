package com.quiniela.app.ui.grupos

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.quiniela.app.databinding.ItemGrupoBinding
import com.quiniela.app.model.GrupoDTO
import com.quiniela.app.model.SeleccionDTO

class GrupoAdapter(private val grupos: List<GrupoDTO>) : RecyclerView.Adapter<GrupoAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGrupoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(grupos[position])
    }

    override fun getItemCount() = grupos.size

    class ViewHolder(private val binding: ItemGrupoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(grupo: GrupoDTO) {
            binding.tvGrupoNombre.text = "Grupo ${grupo.nombre}"
            binding.tvGrupoPais.text = "País: ${grupo.pais}"
            
            binding.llSelecciones.removeAllViews()
            grupo.selecciones.forEach { seleccion ->
                val textView = TextView(binding.root.context).apply {
                    text = "${seleccion.nombre} - Pts: ${seleccion.puntos}, PJ: ${seleccion.partidosJugados}, GF: ${seleccion.golesAFavor}, GC: ${seleccion.golesEnContra}"
                    setPadding(8, 4, 8, 4)
                }
                binding.llSelecciones.addView(textView)
            }
        }
    }
}