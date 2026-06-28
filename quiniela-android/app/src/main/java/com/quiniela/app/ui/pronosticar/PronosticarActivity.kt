package com.quiniela.app.ui.pronosticar

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.quiniela.app.databinding.ActivityPronosticarBinding
import com.quiniela.app.model.PartidoDTO
import com.quiniela.app.model.PronosticoItemRequest
import com.quiniela.app.model.QuinielaResumenDTO
import com.quiniela.app.repository.AuthRepository
import com.quiniela.app.repository.PartidoRepository
import com.quiniela.app.repository.PronosticoRepository
import com.quiniela.app.repository.Result
import com.quiniela.app.util.UiUtils
import kotlinx.coroutines.launch

class PronosticarActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPronosticarBinding
    private val partidoRepository = PartidoRepository()
    private val pronosticoRepository = PronosticoRepository()
    private val authRepository = AuthRepository()
    private lateinit var adapter: PronosticarAdapter
    private var quinielas: List<QuinielaResumenDTO> = emptyList()
    private var selectedQuinielaId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPronosticarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = PronosticarAdapter()
        binding.rvPartidos.layoutManager = LinearLayoutManager(this)
        binding.rvPartidos.adapter = adapter

        binding.btnGuardar.setOnClickListener { guardarPronosticos() }

        loadQuinielas()
    }

    private fun loadQuinielas() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            when (val result = authRepository.getPerfil()) {
                is Result.Success -> {
                    quinielas = result.data.quinielas
                    if (quinielas.isEmpty()) {
                        binding.progressBar.visibility = View.GONE
                        binding.layoutEmpty.visibility = View.VISIBLE
                        binding.rvPartidos.visibility = View.GONE
                        binding.btnGuardar.visibility = View.GONE
                        return@launch
                    }
                    setupQuinielaSpinner()
                    loadPartidos()
                }
                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    UiUtils.showErrorSnackbar(binding.root, result.message)
                }
            }
        }
    }

    private fun setupQuinielaSpinner() {
        val nombres = quinielas.map { it.nombre }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, nombres)
        binding.spQuiniela.setAdapter(adapter)
        
        binding.spQuiniela.setOnItemClickListener { _, _, position, _ ->
            selectedQuinielaId = quinielas[position].id
            selectedQuinielaId?.let { cargarPronosticos(it) }
        }
    }

    private fun loadPartidos() {
        lifecycleScope.launch {
            when (val result = partidoRepository.getPartidos()) {
                is Result.Success -> {
                    val pendientes = result.data.filter { it.estado == "PENDIENTE" }
                    if (pendientes.isEmpty()) {
                        binding.layoutEmpty.visibility = View.VISIBLE
                        binding.tvEmptyTitle.text = "No hay partidos disponibles"
                        binding.tvEmptySubtitle.text = "Los partidos se publicarán cuando el torneo comience."
                        binding.rvPartidos.visibility = View.GONE
                    } else {
                        val items = crearListaAgrupada(pendientes)
                        adapter.submitList(items)
                    }
                }
                is Result.Error -> {
                    UiUtils.showErrorSnackbar(binding.root, result.message)
                }
            }
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun crearListaAgrupada(partidos: List<PartidoDTO>): List<PronosticoItem> {
        val sorted = partidos.sortedBy { it.fechaHora }
        val result = mutableListOf<PronosticoItem>()
        val porDia = sorted.groupBy { it.fechaHora.substring(0, 10) }
        val diasOrdenados = porDia.keys.sorted()
        for (dia in diasOrdenados) {
            result.add(PronosticoItem.Header(dayLabel(dia)))
            for (partido in porDia[dia]!!) {
                result.add(PronosticoItem.PartidoItem(partido))
            }
        }
        return result
    }

    private fun dayLabel(isoDate: String): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val date = sdf.parse(isoDate) ?: return isoDate
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val tomorrow = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(
            java.util.Date(System.currentTimeMillis() + 86400000L)
        )
        return when (isoDate) {
            today -> "Hoy"
            tomorrow -> "Mañana"
            else -> {
                val fmt = java.text.SimpleDateFormat("EEEE d 'de' MMMM", java.util.Locale("es"))
                fmt.format(date)
            }
        }
    }

    private fun cargarPronosticos(quinielaId: Long) {
        lifecycleScope.launch {
            when (val result = pronosticoRepository.getMisPronosticosByQuiniela(quinielaId)) {
                is Result.Success -> {
                    val data = result.data.pronosticos.associate { 
                        it.partido.id to Pair(it.golesLocalPredicho, it.golesVisitantePredicho)
                    }
                    adapter.cargarPronosticos(data)
                }
                is Result.Error -> {
                    UiUtils.showErrorSnackbar(binding.root, "Error al cargar pronósticos")
                }
            }
        }
    }

    private fun guardarPronosticos() {
        val quinielaId = selectedQuinielaId ?: run {
            UiUtils.showWarningSnackbar(binding.root, "Selecciona una quiniela")
            return
        }

        val pronosticosItems = adapter.getPronosticos()
            .filter { it.partido.estado == "PENDIENTE" && !partidoYaComenzo(it.partido.fechaHora) }
            .map { pronostico ->
            PronosticoItemRequest(
                idPartido = pronostico.partido.id,
                golesLocalPredicho = pronostico.golesLocalPredicho,
                golesVisitantePredicho = pronostico.golesVisitantePredicho
            )
        }

        if (pronosticosItems.isEmpty()) {
            UiUtils.showWarningSnackbar(binding.root, "No hay pronósticos para guardar")
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnGuardar.isEnabled = false

        lifecycleScope.launch {
            when (val result = pronosticoRepository.crearPronosticosBatch(quinielaId, pronosticosItems)) {
                is Result.Success -> {
                    UiUtils.showSuccessSnackbar(binding.root, "Pronósticos guardados: ${result.data.pronosticosGuardados}")
                    finish()
                }
                is Result.Error -> {
                    UiUtils.showErrorSnackbar(binding.root, result.message)
                }
            }
            binding.progressBar.visibility = View.GONE
            binding.btnGuardar.isEnabled = true
        }
    }
}