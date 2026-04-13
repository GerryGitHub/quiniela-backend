package com.quiniela.app.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.quiniela.app.databinding.ActivityDashboardBinding
import com.quiniela.app.repository.AuthRepository
import com.quiniela.app.repository.Result
import com.quiniela.app.ui.auth.LoginActivity
import com.quiniela.app.ui.grupos.GruposActivity
import com.quiniela.app.ui.pronosticos.PronosticosActivity
import com.quiniela.app.ui.resultados.ResultadosActivity
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadPerfil()
        setupButtons()
    }

    private fun loadPerfil() {
        lifecycleScope.launch {
            when (val result = authRepository.getPerfil()) {
                is Result.Success -> {
                    binding.tvWelcome.text = "Bienvenido, ${result.data.nombre}"
                    binding.tvPuntos.text = "Puntos: ${result.data.puntosTotalesGlobales}"
                }
                is Result.Error -> {
                    Toast.makeText(this@DashboardActivity, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupButtons() {
        binding.btnMisPronosticos.setOnClickListener {
            startActivity(Intent(this, PronosticosActivity::class.java))
        }
        binding.btnGrupos.setOnClickListener {
            startActivity(Intent(this, GruposActivity::class.java))
        }
        binding.btnResultados.setOnClickListener {
            startActivity(Intent(this, ResultadosActivity::class.java))
        }
        binding.btnLogout.setOnClickListener {
            authRepository.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}