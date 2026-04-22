package com.quiniela.app.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.quiniela.app.databinding.ActivityDashboardBinding
import com.quiniela.app.repository.AuthRepository
import com.quiniela.app.repository.QuinielaRepository
import com.quiniela.app.repository.PartidoRepository
import com.quiniela.app.repository.Result
import com.quiniela.app.ui.auth.LoginActivity
import com.quiniela.app.ui.quiniela.CrearQuinielaActivity
import com.quiniela.app.ui.quiniela.QuinielaDetalleActivity
import com.quiniela.app.ui.quiniela.QuinielaAdapter
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private val authRepository = AuthRepository()
    private val quinielaRepository = QuinielaRepository()
    private val partidoRepository = PartidoRepository()
    private lateinit var adapter: QuinielaAdapter
    private lateinit var adapterPartidosEnVivo: PartidoEnVivoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = QuinielaAdapter { quiniela ->
            val intent = Intent(this, QuinielaDetalleActivity::class.java)
            intent.putExtra("quinielaId", quiniela.id)
            intent.putExtra("quinielaNombre", quiniela.nombre)
            startActivity(intent)
        }
        
        adapterPartidosEnVivo = PartidoEnVivoAdapter()
        
        binding.rvQuinielas.layoutManager = LinearLayoutManager(this)
        binding.rvQuinielas.adapter = adapter
        
        binding.rvPartidosEnVivo.layoutManager = LinearLayoutManager(this)
        binding.rvPartidosEnVivo.adapter = adapterPartidosEnVivo

        setupButtons()
        loadData()
    }

    private fun loadData() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            when (val result = authRepository.getPerfil()) {
                is Result.Success -> {
                    binding.tvWelcome.text = "Bienvenido, ${result.data.nombre}"
                    
                    val quinielas = result.data.quinielas
                    if (quinielas.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.rvQuinielas.visibility = View.GONE
                    } else {
                        binding.tvEmpty.visibility = View.GONE
                        binding.rvQuinielas.visibility = View.VISIBLE
                        adapter.submitList(quinielas)
                    }
                }
                is Result.Error -> {
                    Toast.makeText(this@DashboardActivity, result.message, Toast.LENGTH_SHORT).show()
                }
            }
            binding.progressBar.visibility = View.GONE
            
            loadPartidosEnVivo()
        }
    }
    
    private fun loadPartidosEnVivo() {
        lifecycleScope.launch {
            when (val result = partidoRepository.getPartidosEnVivo()) {
                is Result.Success -> {
                    val partidos = result.data.filter { it.estado == "EN_CURSO" }
                    if (partidos.isNotEmpty()) {
                        binding.cardPartidosEnVivo.visibility = View.VISIBLE
                        adapterPartidosEnVivo.submitList(partidos)
                    } else {
                        binding.cardPartidosEnVivo.visibility = View.GONE
                    }
                }
                is Result.Error -> {
                    // Silently ignore errors for en vivo
                }
            }
        }
    }

    private fun setupButtons() {
        binding.btnCrearQuiniela.setOnClickListener {
            val intent = Intent(this, CrearQuinielaActivity::class.java)
            intent.putExtra("modo", "crear")
            startActivity(intent)
        }
        
        binding.btnUnirseQuiniela.setOnClickListener {
            val intent = Intent(this, CrearQuinielaActivity::class.java)
            intent.putExtra("modo", "unirse")
            startActivity(intent)
        }
        
        binding.btnLogout.setOnClickListener {
            authRepository.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private val pollingRunnable = object : Runnable {
        override fun run() {
            loadPartidosEnVivo()
            handler.postDelayed(this, 30000)
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
        handler.post(pollingRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(pollingRunnable)
    }
}