package com.quiniela.app.ui.quiniela

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.quiniela.app.databinding.ActivityCrearQuinielaBinding
import com.quiniela.app.model.QuinielaDTO
import com.quiniela.app.repository.QuinielaRepository
import com.quiniela.app.repository.Result
import com.quiniela.app.util.QrUtils
import com.quiniela.app.util.ShareUtils
import kotlinx.coroutines.launch

class CrearQuinielaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCrearQuinielaBinding
    private val quinielaRepository = QuinielaRepository()
    private var modo: String = "crear"
    private var quinielaCreada: QuinielaDTO? = null

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
            binding.btnEscanearQR.setOnClickListener { escanearQR() }
        }
    }

    private val scanLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            unirseQuiniela(result.contents)
        }
    }

    private fun escanearQR() {
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        options.setPrompt("Escanea el código QR de la quiniela")
        options.setCameraId(0)
        options.setBeepEnabled(true)
        options.setOrientationLocked(true)
        scanLauncher.launch(options)
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
                    quinielaCreada = result.data
                    showQRCode(result.data)
                }
                is Result.Error -> {
                    Toast.makeText(this@CrearQuinielaActivity, result.message, Toast.LENGTH_LONG).show()
                }
            }
            binding.progressBar.visibility = View.GONE
            binding.btnCrear.isEnabled = true
        }
    }

    private fun showQRCode(quiniela: QuinielaDTO) {
        binding.cardCrear.visibility = View.GONE
        binding.cardQR.visibility = View.VISIBLE
        
        binding.tvCodigo.text = quiniela.codigoInvitacion
        
        val bitmap = QrUtils.generateQrBitmap(quiniela.codigoInvitacion, 400)
        if (bitmap != null) {
            binding.ivQR.setImageBitmap(bitmap)
        } else {
            Toast.makeText(this, "Error al generar QR", Toast.LENGTH_SHORT).show()
        }
        
        binding.btnCompartir.setOnClickListener {
            ShareUtils.shareQuiniela(this, quiniela.nombre, quiniela.codigoInvitacion)
        }
    }

    private fun unirseQuiniela(codigo: String) {
        if (codigo.isEmpty()) {
            Toast.makeText(this, "Código no válido", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE

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
        }
    }
}
