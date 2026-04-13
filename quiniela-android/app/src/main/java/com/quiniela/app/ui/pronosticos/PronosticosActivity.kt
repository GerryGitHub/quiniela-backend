package com.quiniela.app.ui.pronosticos

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.quiniela.app.databinding.ActivityPronosticosBinding
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
                    adapter.submitList(result.data.pronosticos)
                }
                is Result.Error -> {
                    Toast.makeText(this@PronosticosActivity, result.message, Toast.LENGTH_SHORT).show()
                }
            }
            binding.progressBar.visibility = View.GONE
        }
    }
}