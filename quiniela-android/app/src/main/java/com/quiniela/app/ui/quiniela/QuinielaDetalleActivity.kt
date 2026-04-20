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
                            }.toMutableList()
                            partidosAdapter = PartidosConPronosticoAdapter(partidosConPronostico)
                            binding.rvPartidos.adapter = partidosAdapter
                        }
                        is Result.Error -> {
                            val partidosConPronostico = detalle.partidos.map { partido ->
                                PartidoConPronostico(partido = partido, golesLocalPredicho = 0, golesVisitantePredicho = 0)
                            }.toMutableList()
                            partidosAdapter = PartidosConPronosticoAdapter(partidosConPronostico)
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

class PartidosConPronosticoAdapter(
    private val partidos: MutableList<PartidoConPronostico>
) : androidx.recyclerview.widget.RecyclerView.Adapter<PartidosConPronosticoAdapter.ViewHolder>() {
    
    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val binding = com.quiniela.app.databinding.ItemQuinielaPartidoBinding.inflate(
            android.view.LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(partidos[position])
    }

    override fun getItemCount() = partidos.size

    fun getAllPronosticos(): List<PartidoConPronostico> = partidos

    inner class ViewHolder(private val binding: com.quiniela.app.databinding.ItemQuinielaPartidoBinding) : 
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        
        private var textWatcher: TextWatcher? = null
        
        fun bind(item: PartidoConPronostico) {
            val pos = adapterPosition
            
            binding.tvLocal.text = item.partido.equipoLocal
            binding.tvVisitante.text = item.partido.equipoVisitante
            binding.tvFecha.text = item.partido.fechaHora
            
            if (item.partido.golesLocalReal != null && item.partido.golesVisitanteReal != null) {
                binding.tvResultadoReal.text = "Resultado: ${item.partido.golesLocalReal} - ${item.partido.golesVisitanteReal}"
            } else {
                binding.tvResultadoReal.text = "Resultado: Por jugar"
            }
            
            binding.tvMiPronostico.text = "Tu pronóstico: ${item.golesLocalPredicho} - ${item.golesVisitantePredicho}"
            
            binding.etGolesLocal.removeTextChangedListener(textWatcher)
            binding.etGolesVisitante.removeTextChangedListener(textWatcher)
            
            binding.etGolesLocal.setText(item.golesLocalPredicho.toString())
            binding.etGolesVisitante.setText(item.golesVisitantePredicho.toString())
            
            textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val position = adapterPosition
                    if (position != androidx.recyclerview.widget.RecyclerView.NO_POSITION && position < partidos.size) {
                        val local = binding.etGolesLocal.text.toString().toIntOrNull() ?: 0
                        val visitante = binding.etGolesVisitante.text.toString().toIntOrNull() ?: 0
                        partidos[position].golesLocalPredicho = local
                        partidos[position].golesVisitantePredicho = visitante
                        binding.tvMiPronostico.text = "Tu pronóstico: $local - $visitante"
                    }
                }
            }
            
            binding.etGolesLocal.addTextChangedListener(textWatcher)
            binding.etGolesVisitante.addTextChangedListener(textWatcher)
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