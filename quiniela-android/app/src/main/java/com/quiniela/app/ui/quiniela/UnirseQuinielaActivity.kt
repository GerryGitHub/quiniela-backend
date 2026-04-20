package com.quiniela.app.ui.quiniela

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.quiniela.app.databinding.ActivityUnirseQuinielaBinding
import com.quiniela.app.repository.QuinielaRepository
import com.quiniela.app.repository.Result
import kotlinx.coroutines.launch

class UnirseQuinielaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUnirseQuinielaBinding
    private val quinielaRepository = QuinielaRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUnirseQuinielaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.btnUnirse.setOnClickListener { unirseQuiniela() }
    }

    private fun unirseQuiniela() {
        val codigo = binding.etCodigo.text.toString().trim()

        if (codigo.isEmpty()) {
            Toast.makeText(this, "Ingresa el código de invitación", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnUnirse.isEnabled = false

        lifecycleScope.launch {
            when (val result = quinielaRepository.unirseQuiniela(codigo)) {
                is Result.Success -> {
                    Toast.makeText(this@UnirseQuinielaActivity, "Te uniste a: ${result.data.nombre}", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is Result.Error -> {
                    Toast.makeText(this@UnirseQuinielaActivity, result.message, Toast.LENGTH_LONG).show()
                }
            }
            binding.progressBar.visibility = View.GONE
            binding.btnUnirse.isEnabled = true
        }
    }
}
