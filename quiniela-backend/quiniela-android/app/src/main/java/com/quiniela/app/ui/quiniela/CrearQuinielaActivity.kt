package com.quiniela.app.ui.quiniela

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.quiniela.app.databinding.ActivityCrearQuinielaBinding
import com.quiniela.app.repository.QuinielaRepository
import com.quiniela.app.repository.Result
import kotlinx.coroutines.launch

class CrearQuinielaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCrearQuinielaBinding
    private val quinielaRepository = QuinielaRepository()
    private var modo: String = "crear"
    private var codigoCreado: String? = null

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
                    codigoCreado = result.data.codigoInvitacion
                    showQRCode(result.data.codigoInvitacion)
                }
                is Result.Error -> {
                    Toast.makeText(this@CrearQuinielaActivity, result.message, Toast.LENGTH_LONG).show()
                }
            }
            binding.progressBar.visibility = View.GONE
            binding.btnCrear.isEnabled = true
        }
    }

    private fun showQRCode(codigo: String) {
        binding.cardCrear.visibility = View.GONE
        binding.cardQR.visibility = View.VISIBLE
        
        binding.tvCodigo.text = codigo
        
        try {
            val size = 400
            val bitMatrix: BitMatrix = MultiFormatWriter().encode(
                codigo,
                BarcodeFormat.QR_CODE,
                size,
                size
            )
            val bitmap = android.graphics.Bitmap.createBitmap(
                size, size, android.graphics.Bitmap.Config.ARGB_8888
            )
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
                }
            }
            binding.ivQR.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error QR: ${e.message}", Toast.LENGTH_SHORT).show()
        }
        
        binding.btnCompartir.setOnClickListener { compartirCodigo(codigo) }
    }

    private fun compartirCodigo(codigo: String) {
        val mensaje = "Únete a mi quiniela! Usa el código: $codigo\n\nO escanea el código QR de la app Quiniela"
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, mensaje)
        }
        
        startActivity(Intent.createChooser(intent, "Compartir con"))
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
