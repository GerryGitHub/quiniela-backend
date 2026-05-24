package com.quiniela.app.ui.grupos

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.quiniela.app.databinding.ActivityGruposBinding
import com.quiniela.app.repository.GrupoRepository
import com.quiniela.app.repository.Result
import kotlinx.coroutines.launch

class GruposActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGruposBinding
    private val repository = GrupoRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGruposBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadGrupos()
    }

    private fun loadGrupos() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            when (val result = repository.getGrupos()) {
                is Result.Success -> {
                    binding.rvGrupos.layoutManager = LinearLayoutManager(this@GruposActivity)
                    binding.rvGrupos.adapter = GrupoAdapter(result.data.grupos)
                }
                is Result.Error -> {
                    Toast.makeText(this@GruposActivity, result.message, Toast.LENGTH_SHORT).show()
                }
            }
            binding.progressBar.visibility = View.GONE
        }
    }
}