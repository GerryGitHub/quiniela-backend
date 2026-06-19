package com.quiniela.app.ui.eliminatorias

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.quiniela.app.databinding.ItemBracketMatchBinding
import com.quiniela.app.databinding.ItemBracketRoundBinding
import com.quiniela.app.model.BracketSlotPreviewDTO
import com.quiniela.app.util.CountryFlagResolver

private const val VIEW_TYPE_ROUND = 0
private const val VIEW_TYPE_MATCH = 1

data class BracketItem(val type: Int, val roundName: String? = null, val match: BracketSlotPreviewDTO? = null)

class BracketAdapter(
    private val items: List<BracketItem>,
    private val roundLabels: Map<String, String>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int) = items[position].type

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_ROUND -> {
                val binding = ItemBracketRoundBinding.inflate(inflater, parent, false)
                RoundViewHolder(binding)
            }
            else -> {
                val matchBinding = ItemBracketMatchBinding.inflate(inflater, parent, false)
                MatchViewHolder(matchBinding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is RoundViewHolder -> holder.bind(items[position])
            is MatchViewHolder -> holder.bind(items[position])
        }
    }

    override fun getItemCount() = items.size

    inner class RoundViewHolder(private val binding: ItemBracketRoundBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BracketItem) {
            val roundCode = item.roundName ?: return
            binding.tvRoundTitle.text = roundLabels[roundCode] ?: roundCode

            val matches = mutableListOf<BracketSlotPreviewDTO>()
            var i = bindingAdapterPosition + 1
            while (i < items.size && items[i].type == VIEW_TYPE_MATCH) {
                items[i].match?.let { matches.add(it) }
                i++
            }

            binding.rvMatches.layoutManager = LinearLayoutManager(binding.root.context)
            binding.rvMatches.adapter = MatchAdapter(matches)
        }
    }

    inner class MatchViewHolder(private val binding: ItemBracketMatchBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BracketItem) {
            val match = item.match ?: return
            val ctx = binding.root.context

            binding.tvCodigo.text = match.codigo

            val localName = match.equipoLocal ?: "Por definir"
            val visitName = match.equipoVisitante ?: "Por definir"

            binding.tvLocal.text = localName
            binding.tvVisitante.text = visitName

            val localFlag = CountryFlagResolver.getFlagDrawable(ctx, localName)
            val visitFlag = CountryFlagResolver.getFlagDrawable(ctx, visitName)
            binding.ivLocalFlag.setImageDrawable(localFlag)
            binding.ivVisitanteFlag.setImageDrawable(visitFlag)

            if (match.localSlot?.tipo == "GRUPO_1") {
                binding.layoutLocal.setBackgroundResource(com.quiniela.app.R.drawable.bg_team_winner)
            } else {
                binding.layoutLocal.background = null
            }
            if (match.visitanteSlot?.tipo == "GRUPO_1") {
                binding.layoutVisitante.setBackgroundResource(com.quiniela.app.R.drawable.bg_team_winner)
            } else {
                binding.layoutVisitante.background = null
            }
        }
    }
}

class MatchAdapter(private val matches: List<BracketSlotPreviewDTO>) :
    RecyclerView.Adapter<MatchAdapter.MatchViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val binding = ItemBracketMatchBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MatchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        holder.bind(matches[position])
    }

    override fun getItemCount() = matches.size

    inner class MatchViewHolder(private val binding: ItemBracketMatchBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(match: BracketSlotPreviewDTO) {
            val ctx = binding.root.context
            binding.tvCodigo.text = match.codigo

            val localName = match.equipoLocal ?: "Por definir"
            val visitName = match.equipoVisitante ?: "Por definir"

            binding.tvLocal.text = localName
            binding.tvVisitante.text = visitName

            val localFlag = CountryFlagResolver.getFlagDrawable(ctx, localName)
            val visitFlag = CountryFlagResolver.getFlagDrawable(ctx, visitName)
            binding.ivLocalFlag.setImageDrawable(localFlag)
            binding.ivVisitanteFlag.setImageDrawable(visitFlag)

            if (match.localSlot?.tipo == "GRUPO_1") {
                binding.layoutLocal.setBackgroundResource(com.quiniela.app.R.drawable.bg_team_winner)
            } else {
                binding.layoutLocal.background = null
            }
            if (match.visitanteSlot?.tipo == "GRUPO_1") {
                binding.layoutVisitante.setBackgroundResource(com.quiniela.app.R.drawable.bg_team_winner)
            } else {
                binding.layoutVisitante.background = null
            }
        }
    }
}
