package com.quiniela.app.ui.dashboard

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.quiniela.app.R
import com.quiniela.app.databinding.ItemPartidoEnVivoBinding
import com.quiniela.app.model.PartidoDTO
import com.quiniela.app.util.UiUtils

class PartidoEnVivoAdapter(
    private val onClick: (PartidoDTO) -> Unit = {}
) : RecyclerView.Adapter<PartidoEnVivoAdapter.ViewHolder>() {

    private var partidos: List<PartidoDTO> = emptyList()
    private val countdownHandler = Handler(Looper.getMainLooper())
    private val countdownRunnables = mutableMapOf<Int, Runnable>()

    fun submitList(newList: List<PartidoDTO>) {
        partidos = newList.toList()
        notifyDataSetChanged()
        updateCountdowns()
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

    private fun updateCountdowns() {
        countdownRunnables.values.forEach { countdownHandler.removeCallbacks(it) }
        countdownRunnables.clear()

        partidos.forEachIndexed { index, partido ->
            if (partido.estado == PartidoDTO.ESTADO_POR_COMENZAR && partido.minutosParaInicio != null && partido.minutosParaInicio > 0) {
                val runnable = object : Runnable {
                    override fun run() {
                        notifyItemChanged(index)
                        countdownHandler.postDelayed(this, 30000)
                    }
                }
                countdownRunnables[index] = runnable
                countdownHandler.postDelayed(runnable, 30000)
            }
        }
    }

    fun cleanup() {
        countdownRunnables.values.forEach { countdownHandler.removeCallbacks(it) }
        countdownRunnables.clear()
    }

    inner class ViewHolder(
        private val binding: ItemPartidoEnVivoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(partido: PartidoDTO) {
            binding.tvEquipoLocal.text = partido.equipoLocal
            binding.tvEquipoVisitante.text = partido.equipoVisitante
            binding.root.setOnClickListener { onClick(partido) }

            when (partido.estado) {
                PartidoDTO.ESTADO_POR_COMENZAR -> bindPorComenzar(partido)
                PartidoDTO.ESTADO_EN_CURSO -> bindEnCurso(partido)
                PartidoDTO.ESTADO_FINALIZADO -> bindFinalizado(partido)
                else -> bindPorComenzar(partido)
            }
        }

        private fun bindPorComenzar(partido: PartidoDTO) {
            binding.layoutStatus.visibility = View.VISIBLE
            binding.vLiveDot.visibility = View.GONE
            binding.tvEstado.text = "⏳ Por comenzar"
            binding.tvEstado.setTextColor(ContextCompat.getColor(binding.root.context, R.color.warning))

            val scoreboardBg = ContextCompat.getDrawable(binding.root.context, R.drawable.chip_warning_background)
            binding.tvMarcador.background = scoreboardBg
            binding.tvMarcador.text = if (partido.minutosParaInicio != null && partido.minutosParaInicio > 0) {
                if (partido.minutosParaInicio >= 60) {
                    "vs"
                } else {
                    "vs"
                }
            } else {
                "vs"
            }
            binding.tvMarcador.setTextColor(ContextCompat.getColor(binding.root.context, R.color.white))

            binding.tvCountdown.visibility = View.VISIBLE
            binding.tvCountdown.text = if (partido.minutosParaInicio != null && partido.minutosParaInicio > 0) {
                "Comienza en ${partido.minutosParaInicio} min"
            } else {
                "A punto de comenzar"
            }
        }

        private fun bindEnCurso(partido: PartidoDTO) {
            binding.layoutStatus.visibility = View.VISIBLE
            binding.vLiveDot.visibility = View.VISIBLE
            UiUtils.startLivePulse(binding.vLiveDot)
            binding.tvEstado.text = "EN VIVO"
            binding.tvEstado.setTextColor(ContextCompat.getColor(binding.root.context, R.color.error))

            val liveBg = ContextCompat.getDrawable(binding.root.context, R.drawable.chip_error_background)
            binding.tvMarcador.background = liveBg
            binding.tvMarcador.text = "${partido.golesLocalReal ?: 0} - ${partido.golesVisitanteReal ?: 0}"
            binding.tvMarcador.setTextColor(ContextCompat.getColor(binding.root.context, R.color.white))

            binding.tvCountdown.visibility = View.GONE
        }

        private fun bindFinalizado(partido: PartidoDTO) {
            binding.layoutStatus.visibility = View.VISIBLE
            binding.vLiveDot.visibility = View.GONE
            binding.tvEstado.text = "FINALIZADO"
            binding.tvEstado.setTextColor(ContextCompat.getColor(binding.root.context, R.color.white))

            val finishedBg = ContextCompat.getDrawable(binding.root.context, R.drawable.chip_default_background)
            binding.tvMarcador.background = finishedBg
            binding.tvMarcador.text = "${partido.golesLocalReal ?: 0} - ${partido.golesVisitanteReal ?: 0}"
            binding.tvMarcador.setTextColor(ContextCompat.getColor(binding.root.context, R.color.white))

            binding.tvCountdown.visibility = View.GONE
        }
    }
}
