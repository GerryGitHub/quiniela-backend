package com.quiniela.app.ui.pronosticos

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.quiniela.app.databinding.ActivityPronosticosBinding
import com.quiniela.app.model.PronosticoDTO
import com.quiniela.app.repository.PronosticoRepository
import com.quiniela.app.repository.Result
import kotlinx.coroutines.launch

class PronosticosActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPronosticosBinding
    private val repository = PronosticoRepository()
    private lateinit var adapter: PronosticoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                    val items = crearListaAgrupada(result.data.pronosticos)
                    adapter.submitList(items)
                }
                is Result.Error -> {
                    Toast.makeText(this@PronosticosActivity, result.message, Toast.LENGTH_SHORT).show()
                }
            }
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun crearListaAgrupada(pronosticos: List<PronosticoDTO>): List<MisPronosticosItem> {
        val ordenGrupo = listOf("A", "B", "C", "D", "E", "F", "G", "H")
        
        val grouped = pronosticos.groupBy { it.partido.grupo ?: "Sin grupo" }
        
        val result = mutableListOf<MisPronosticosItem>()
        
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
        
        for (grupo in gruposOrdenados) {
            result.add(MisPronosticosItem.Header(grupo))
            val pronosticosDelGrupo = grouped[grupo]?.sortedBy { it.partido.fechaHora } ?: emptyList()
            for (pronostico in pronosticosDelGrupo) {
                result.add(MisPronosticosItem.PronosticoItem(pronostico))
            }
        }
        
        return result
    }
}