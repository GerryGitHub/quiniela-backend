package com.quiniela.app.ui.grupos

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.quiniela.app.databinding.ActivityGruposBinding
import com.quiniela.app.repository.GrupoRepository
import com.quiniela.app.repository.Result
import com.quiniela.app.util.UiUtils
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
                    val grupos = result.data.grupos
                    if (grupos.isEmpty()) {
                        binding.layoutEmpty.visibility = View.VISIBLE
                        binding.rvGrupos.visibility = View.GONE
                    } else {
                        binding.rvGrupos.layoutManager = LinearLayoutManager(this@GruposActivity)
                        binding.rvGrupos.adapter = GrupoAdapter(grupos)
                    }
                }
                is Result.Error -> {
                    UiUtils.showErrorSnackbar(binding.root, result.message)
                }
            }
            binding.progressBar.visibility = View.GONE
        }
    }
}