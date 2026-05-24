package com.quiniela.app.ui.resultados

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.quiniela.app.databinding.ActivityResultadosBinding
import com.quiniela.app.repository.PartidoRepository
import com.quiniela.app.repository.Result
import kotlinx.coroutines.launch

class ResultadosActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultadosBinding
    private val repository = PartidoRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultadosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadResultados()
    }

    private fun loadResultados() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            when (val result = repository.getResultados()) {
                is Result.Success -> {
                    binding.rvResultados.layoutManager = LinearLayoutManager(this@ResultadosActivity)
                    binding.rvResultados.adapter = ResultadoAdapter(result.data)
                }
                is Result.Error -> {
                    Toast.makeText(this@ResultadosActivity, result.message, Toast.LENGTH_SHORT).show()
                }
            }
            binding.progressBar.visibility = View.GONE
        }
    }
}