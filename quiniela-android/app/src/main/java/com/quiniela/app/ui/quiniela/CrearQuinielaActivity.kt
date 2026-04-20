package com.quiniela.app.ui.quiniela

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.quiniela.app.databinding.ActivityCrearQuinielaBinding
import com.quiniela.app.repository.QuinielaRepository
import com.quiniela.app.repository.Result
import kotlinx.coroutines.launch

class CrearQuinielaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCrearQuinielaBinding
    private val quinielaRepository = QuinielaRepository()
    private var modo: String = "crear"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearQuinielaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        modo = intent.getStringExtra("modo") ?: "crear"

        binding.toolbar.setNavigationOnClickListener { finish() }
        
        if (modo == "crear") {
            binding.toolbar.title = "Crear Quiniela"
            binding.cardCrear.visibility = View.VISIBLE
            binding.cardUnirse.visibility = View.GONE
            binding.btnCrear.setOnClickListener { crearQuiniela() }
        } else {
            binding.toolbar.title = "Unirse a Quiniela"
            binding.cardCrear.visibility = View.GONE
            binding.cardUnirse.visibility = View.VISIBLE
            binding.btnUnirseConfirmar.setOnClickListener { unirseQuiniela() }
        }
    }

    private fun crearQuiniela() {
        val nombre = binding.etNombre.text.toString().trim()

        if (nombre.isEmpty()) {
            Toast.makeText(this, "Ingresa un nombre", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnCrear.isEnabled = false

        lifecycleScope.launch {
            when (val result = quinielaRepository.crearQuiniela(nombre, "")) {
                is Result.Success -> {
                    Toast.makeText(this@CrearQuinielaActivity, "Quiniela creada: ${result.data.nombre}\nCódigo: ${result.data.codigoInvitacion}", Toast.LENGTH_LONG).show()
                    finish()
                }
                is Result.Error -> {
                    Toast.makeText(this@CrearQuinielaActivity, result.message, Toast.LENGTH_LONG).show()
                }
            }
            binding.progressBar.visibility = View.GONE
            binding.btnCrear.isEnabled = true
        }
    }

    private fun unirseQuiniela() {
        val codigo = binding.etCodigoUnirse.text.toString().trim()

        if (codigo.isEmpty()) {
            Toast.makeText(this, "Ingresa el código de invitación", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnUnirseConfirmar.isEnabled = false

        lifecycleScope.launch {
            when (val result = quinielaRepository.unirseQuiniela(codigo)) {
                is Result.Success -> {
                    Toast.makeText(this@CrearQuinielaActivity, "Te uniste a: ${result.data.nombre}", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is Result.Error -> {
                    Toast.makeText(this@CrearQuinielaActivity, result.message, Toast.LENGTH_LONG).show()
                }
            }
            binding.progressBar.visibility = View.GONE
            binding.btnUnirseConfirmar.isEnabled = true
        }
    }
}
