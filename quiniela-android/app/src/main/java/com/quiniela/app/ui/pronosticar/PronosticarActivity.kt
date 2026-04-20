package com.quiniela.app.ui.pronosticar

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.quiniela.app.databinding.ActivityPronosticarBinding
import com.quiniela.app.model.*
import com.quiniela.app.repository.*
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
                        Toast.makeText(this@PronosticarActivity, "No tienes quinielas. Crea o únete a una primero.", Toast.LENGTH_LONG).show()
                        finish()
                        return@launch
                    }
                    setupQuinielaSpinner()
                    loadPartidos()
                }
                is Result.Error -> {
                    Toast.makeText(this@PronosticarActivity, result.message, Toast.LENGTH_SHORT).show()
                    finish()
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
        }
    }

    private fun loadPartidos() {
        lifecycleScope.launch {
            when (val result = partidoRepository.getPartidos()) {
                is Result.Success -> {
                    val partidosPendientes = result.data.filter { it.estado == "PENDIENTE" }
                    adapter.submitList(partidosPendientes)
                }
                is Result.Error -> {
                    Toast.makeText(this@PronosticarActivity, result.message, Toast.LENGTH_SHORT).show()
                }
            }
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun guardarPronosticos() {
        val quinielaId = selectedQuinielaId ?: run {
            Toast.makeText(this, "Selecciona una quiniela", Toast.LENGTH_SHORT).show()
            return
        }

        val pronosticosItems = adapter.getPronosticos().map { pronostico ->
            PronosticoItemRequest(
                idPartido = pronostico.partido.id,
                golesLocalPredicho = pronostico.golesLocalPredicho,
                golesVisitantePredicho = pronostico.golesVisitantePredicho
            )
        }

        if (pronosticosItems.isEmpty()) {
            Toast.makeText(this, "No hay pronósticos para guardar", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnGuardar.isEnabled = false

        lifecycleScope.launch {
            when (val result = pronosticoRepository.crearPronosticosBatch(quinielaId, pronosticosItems)) {
                is Result.Success -> {
                    Toast.makeText(this@PronosticarActivity, "Pronósticos guardados: ${result.data.pronosticosGuardados}", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is Result.Error -> {
                    Toast.makeText(this@PronosticarActivity, result.message, Toast.LENGTH_LONG).show()
                }
            }
            binding.progressBar.visibility = View.GONE
            binding.btnGuardar.isEnabled = true
        }
    }
}