package com.quiniela.app.ui.pronosticos

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.quiniela.app.databinding.ActivityPronosticosBinding
import com.quiniela.app.model.PronosticoDTO
import com.quiniela.app.repository.PronosticoRepository
import com.quiniela.app.repository.Result
import com.quiniela.app.util.UiUtils
import kotlinx.coroutines.launch

class PronosticosActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPronosticosBinding
    private val repository = PronosticoRepository()
    private lateinit var adapter: PronosticoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPronosticosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        loadPronosticos()
    }

    private fun setupRecyclerView() {
        adapter = PronosticoAdapter()
        binding.rvPronosticos.layoutManager = LinearLayoutManager(this)
        binding.rvPronosticos.adapter = adapter
    }

    private fun loadPronosticos() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            when (val result = repository.getMisPronosticos()) {
                is Result.Success -> {
                    val pronosticos = result.data.pronosticos
                    if (pronosticos.isEmpty()) {
                        binding.layoutEmpty.visibility = View.VISIBLE
                        binding.rvPronosticos.visibility = View.GONE
                    } else {
                        val items = crearListaAgrupada(pronosticos)
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

    private fun crearListaAgrupada(pronosticos: List<PronosticoDTO>): List<MisPronosticosItem> {
        val sorted = pronosticos.sortedBy { it.partido.fechaHora }
        val result = mutableListOf<MisPronosticosItem>()
        val porDia = sorted.groupBy { it.partido.fechaHora.substring(0, 10) }
        val diasOrdenados = porDia.keys.sorted()
        for (dia in diasOrdenados) {
            result.add(MisPronosticosItem.Header(dayLabel(dia)))
            for (pronostico in porDia[dia]!!) {
                result.add(MisPronosticosItem.PronosticoItem(pronostico))
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
}