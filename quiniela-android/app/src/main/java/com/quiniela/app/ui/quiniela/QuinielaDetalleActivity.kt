package com.quiniela.app.ui.quiniela

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.quiniela.app.databinding.ActivityQuinielaDetalleBinding
import com.quiniela.app.model.*
import com.quiniela.app.repository.PronosticoRepository
import com.quiniela.app.repository.QuinielaRepository
import com.quiniela.app.repository.Result
import kotlinx.coroutines.launch

class QuinielaDetalleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuinielaDetalleBinding
    private val quinielaRepository = QuinielaRepository()
    private val pronosticoRepository = PronosticoRepository()
    private var quinielaId: Long = 0
    
    private lateinit var partidosAdapter: PartidosConPronosticoAdapter
    private lateinit var participantesAdapter: ParticipantesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuinielaDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        quinielaId = intent.getLongExtra("quinielaId", 0)
        val nombreQuiniela = intent.getStringExtra("quinielaNombre") ?: "Quiniela"
        
        binding.toolbar.title = nombreQuiniela
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupTabs()
        setupRecyclerViews()
        setupButtons()
        loadData()
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        binding.layoutPronosticos.visibility = View.VISIBLE
                        binding.rvParticipantes.visibility = View.GONE
                    }
                    1 -> {
                        binding.layoutPronosticos.visibility = View.GONE
                        binding.rvParticipantes.visibility = View.VISIBLE
                    }
                }
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
    }

    private fun setupRecyclerViews() {
        binding.rvPartidos.layoutManager = LinearLayoutManager(this)
        binding.rvParticipantes.layoutManager = LinearLayoutManager(this)
    }

    private fun setupButtons() {
        binding.btnGuardarPronosticos.setOnClickListener { guardarPronosticos() }
    }

    private fun loadData() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            when (val result = quinielaRepository.getQuinielaDetalle(quinielaId)) {
                is Result.Success -> {
                    val detalle = result.data
                    binding.toolbar.title = detalle.nombre
                    
when (val pronosResult = pronosticoRepository.getMisPronosticosByQuiniela(quinielaId)) {
                        is Result.Success -> {
                            val pronosticosMap = pronosResult.data.pronosticos.associateBy { it.partido.id }
                            val partidosConPronostico = detalle.partidos.map { partido ->
                                val pronostico = pronosticosMap[partido.id]
                                PartidoConPronostico(
                                    partido = partido,
                                    golesLocalPredicho = pronostico?.golesLocalPredicho ?: 0,
                                    golesVisitantePredicho = pronostico?.golesVisitantePredicho ?: 0
                                )
                            }
                            val itemsAgrupados = crearListaAgrupada(partidosConPronostico)
                            partidosAdapter = PartidosConPronosticoAdapter(itemsAgrupados)
                            binding.rvPartidos.adapter = partidosAdapter
                        }
                        is Result.Error -> {
                            val partidosConPronostico = detalle.partidos.map { partido ->
                                PartidoConPronostico(partido = partido, golesLocalPredicho = 0, golesVisitantePredicho = 0)
                            }
                            val itemsAgrupados = crearListaAgrupada(partidosConPronostico)
                            partidosAdapter = PartidosConPronosticoAdapter(itemsAgrupados)
                            binding.rvPartidos.adapter = partidosAdapter
                        }
                    }
                }
                is Result.Error -> {
                    Toast.makeText(this@QuinielaDetalleActivity, result.message, Toast.LENGTH_LONG).show()
                }
            }
            
            when (val result = quinielaRepository.getLeaderboard(quinielaId)) {
                is Result.Success -> {
                    participantesAdapter = ParticipantesAdapter(result.data)
                    binding.rvParticipantes.adapter = participantesAdapter
                }
                is Result.Error -> {}
            }
            
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun crearListaAgrupada(partidos: List<PartidoConPronostico>): List<GrupoExpansible> {
        val ordenGrupo = listOf("A", "B", "C", "D", "E", "F", "G", "H")
        
        val grouped = partidos.groupBy { 
            normalizarGrupo(it.partido.grupo) 
        }
        
        val gruposOrdenados = grouped.keys.sortedWith { a, b ->
            val idxA = ordenGrupo.indexOf(a)
            val idxB = ordenGrupo.indexOf(b)
            when {
                idxA != -1 && idxB != -1 -> idxA - idxB
                idxA != -1 -> -1
                idxB != -1 -> 1
                else -> a.compareTo(b)
            }
        }
        
        return gruposOrdenados.map { grupo ->
            GrupoExpansible(
                nombre = grupo,
                partidos = (grouped[grupo]?.sortedBy { it.partido.fechaHora } ?: emptyList()).toMutableList(),
                expandido = false
            )
        }
    }
    
    private fun normalizarGrupo(grupo: String?): String {
        if (grupo == null) return "Sin grupo"
        return grupo.trim().take(1).uppercase()
    }

    private fun guardarPronosticos() {
        val pronosticos = partidosAdapter.getAllPronosticos()
        
        if (pronosticos.isEmpty()) {
            Toast.makeText(this, "No hay pronósticos para guardar", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnGuardarPronosticos.isEnabled = false

        lifecycleScope.launch {
            val items = pronosticos.map { pp ->
                PronosticoItemRequest(
                    idPartido = pp.partido.id,
                    golesLocalPredicho = pp.golesLocalPredicho,
                    golesVisitantePredicho = pp.golesVisitantePredicho
                )
            }
            
            when (val result = pronosticoRepository.crearPronosticosBatch(quinielaId, items)) {
                is Result.Success -> {
                    Toast.makeText(this@QuinielaDetalleActivity, "Pronósticos guardados: ${result.data.pronosticosGuardados}", Toast.LENGTH_SHORT).show()
                }
                is Result.Error -> {
                    Toast.makeText(this@QuinielaDetalleActivity, result.message, Toast.LENGTH_LONG).show()
                }
            }
            binding.progressBar.visibility = View.GONE
            binding.btnGuardarPronosticos.isEnabled = true
        }
    }
}

data class PartidoConPronostico(
    val partido: PartidoDTO,
    var golesLocalPredicho: Int = 0,
    var golesVisitantePredicho: Int = 0
)

data class GrupoExpansible(
    val nombre: String,
    val partidos: MutableList<PartidoConPronostico>,
    var expandido: Boolean = false
)

class PartidosConPronosticoAdapter(
    private val grupos: List<GrupoExpansible>
) : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_PARTIDO = 1
    }

    override fun getItemViewType(position: Int): Int {
        var headersCount = 0
        for (grupo in grupos) {
            if (position == headersCount) return TYPE_HEADER
            headersCount++
            if (grupo.expandido) {
                if (position < headersCount + grupo.partidos.size) return TYPE_PARTIDO
                headersCount += grupo.partidos.size
            }
        }
        return TYPE_PARTIDO
    }

    override fun getItemCount(): Int {
        var count = grupos.size
        for (grupo in grupos) {
            if (grupo.expandido) count += grupo.partidos.size
        }
        return count
    }

    private fun getItem(position: Int): Any? {
        var headersCount = 0
        for (grupo in grupos) {
            if (position == headersCount) return grupo
            headersCount++
            if (grupo.expandido) {
                val partidoIndex = position - headersCount
                if (partidoIndex < grupo.partidos.size) {
                    return grupo.partidos[partidoIndex]
                }
                headersCount += grupo.partidos.size
            }
        }
        return null
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val binding = com.quiniela.app.databinding.ItemGrupoHeaderBinding.inflate(
                    android.view.LayoutInflater.from(parent.context), parent, false)
                HeaderViewHolder(binding)
            }
            else -> {
                val binding = com.quiniela.app.databinding.ItemQuinielaPartidoBinding.inflate(
                    android.view.LayoutInflater.from(parent.context), parent, false)
                ViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (item) {
            is GrupoExpansible -> (holder as HeaderViewHolder).bind(item)
            is PartidoConPronostico -> (holder as ViewHolder).bind(item)
            null -> {}
        }
    }

    fun getAllPronosticos(): List<PartidoConPronostico> {
        return grupos.flatMap { it.partidos }
    }

    inner class HeaderViewHolder(private val binding: com.quiniela.app.databinding.ItemGrupoHeaderBinding) : 
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        
        fun bind(grupo: GrupoExpansible) {
            binding.tvHeader.text = "Grupo ${grupo.nombre} ${if (grupo.expandido) "▼" else "▶"}"
            binding.root.setOnClickListener {
                grupo.expandido = !grupo.expandido
                notifyDataSetChanged()
            }
        }
    }

    inner class ViewHolder(private val binding: com.quiniela.app.databinding.ItemQuinielaPartidoBinding) : 
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        
        private var textWatcher: TextWatcher? = null
        private var partidoActual: PartidoConPronostico? = null
        
        fun bind(item: PartidoConPronostico) {
            partidoActual = item
            
            binding.tvLocal.text = "${obtenerBandera(item.partido.equipoLocal)} ${item.partido.equipoLocal}"
            binding.tvVisitante.text = "${item.partido.equipoVisitante} ${obtenerBandera(item.partido.equipoVisitante)}"
            binding.tvFecha.text = item.partido.fechaHora
            
            if (item.partido.golesLocalReal != null && item.partido.golesVisitanteReal != null) {
                binding.tvResultadoReal.text = "Resultado: ${item.partido.golesLocalReal} - ${item.partido.golesVisitanteReal}"
            } else {
                binding.tvResultadoReal.text = "Resultado: Por jugar"
            }
            
            binding.tvMiPronostico.text = "Tu pronóstico: ${item.golesLocalPredicho} - ${item.golesVisitantePredicho}"
            
            val esEditable = item.partido.estado == "PENDIENTE"
            binding.etGolesLocal.isEnabled = esEditable
            binding.etGolesVisitante.isEnabled = esEditable
            
            binding.etGolesLocal.removeTextChangedListener(textWatcher)
            binding.etGolesVisitante.removeTextChangedListener(textWatcher)
            
            binding.etGolesLocal.setText(if (item.golesLocalPredicho > 0) item.golesLocalPredicho.toString() else "")
            binding.etGolesVisitante.setText(if (item.golesVisitantePredicho > 0) item.golesVisitantePredicho.toString() else "")
            
            textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    partidoActual?.let { partido ->
                        val local = binding.etGolesLocal.text.toString().toIntOrNull() ?: 0
                        val visitante = binding.etGolesVisitante.text.toString().toIntOrNull() ?: 0
                        partido.golesLocalPredicho = local
                        partido.golesVisitantePredicho = visitante
                        binding.tvMiPronostico.text = "Tu pronóstico: $local - $visitante"
                    }
                }
            }
            
            binding.etGolesLocal.addTextChangedListener(textWatcher)
            binding.etGolesVisitante.addTextChangedListener(textWatcher)
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
}

class ParticipantesAdapter(private val participantes: List<LeaderboardEntryDTO>) : 
    androidx.recyclerview.widget.RecyclerView.Adapter<ParticipantesAdapter.ViewHolder>() {
    
    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val binding = com.quiniela.app.databinding.ItemQuinielaParticipanteBinding.inflate(
            android.view.LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(participantes[position], position + 1)
    }

    override fun getItemCount() = participantes.size

    class ViewHolder(private val binding: com.quiniela.app.databinding.ItemQuinielaParticipanteBinding) : 
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        fun bind(entry: LeaderboardEntryDTO, posicion: Int) {
            binding.tvPosicion.text = posicion.toString()
            binding.tvNombre.text = entry.usuario.nombre
            binding.tvPuntos.text = "${entry.puntosTotales} pts"
        }
    }
}