package com.quiniela.app.ui.dashboard

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.quiniela.app.util.UiUtils
import androidx.recyclerview.widget.LinearLayoutManager
import com.quiniela.app.R
import com.quiniela.app.databinding.ActivityDashboardBinding
import com.quiniela.app.repository.AuthRepository
import com.quiniela.app.repository.QuinielaRepository
import com.quiniela.app.repository.PartidoRepository
import com.quiniela.app.repository.Result
import com.quiniela.app.ui.auth.LoginActivity
import com.quiniela.app.ui.quiniela.CrearQuinielaActivity
import com.quiniela.app.ui.quiniela.QuinielaDetalleActivity
import com.quiniela.app.ui.quiniela.QuinielaAdapter
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.quiniela.app.databinding.DialogQrBinding
import com.quiniela.app.model.PartidoDTO
import com.quiniela.app.model.QuinielaResumenDTO
import kotlinx.coroutines.launch
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private val authRepository = AuthRepository()
    private val quinielaRepository = QuinielaRepository()
    private val partidoRepository = PartidoRepository()
    private lateinit var adapter: QuinielaAdapter
    private lateinit var adapterPartidosEnVivo: PartidoEnVivoAdapter
    private var lastPartidosHash: Int = 0
    private var isFirstLoad = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        adapter = QuinielaAdapter(
            onItemClick = { quiniela ->
                val intent = Intent(this, QuinielaDetalleActivity::class.java)
                intent.putExtra("quinielaId", quiniela.id)
                intent.putExtra("quinielaNombre", quiniela.nombre)
                startActivity(intent)
            },
            onShareClick = { quiniela ->
                showQRDialog(quiniela)
            }
        )
        
        adapterPartidosEnVivo = PartidoEnVivoAdapter()
        
        binding.rvQuinielas.layoutManager = LinearLayoutManager(this)
        binding.rvQuinielas.adapter = adapter
        
        binding.rvPartidosEnVivo.layoutManager = LinearLayoutManager(this)
        binding.rvPartidosEnVivo.adapter = adapterPartidosEnVivo

        setupButtons()
        loadData()
    }

    @SuppressLint("SetTextI18n")
    private fun loadData() {
        if (isFirstLoad) {
            binding.progressBar.visibility = View.VISIBLE
        }
        lifecycleScope.launch {
            when (val result = authRepository.getPerfil()) {
                is Result.Success -> {
                    binding.tvWelcome.text = "Bienvenido, ${result.data.nombre}"
                    binding.tvWelcome.visibility = View.VISIBLE
                    
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
                    UiUtils.showErrorSnackbar(binding.root, result.message)
                }
            }
            binding.progressBar.visibility = View.GONE
            isFirstLoad = false

            loadPartidosEnVivo()
        }
    }
    
    private fun loadPartidosEnVivo() {
        lifecycleScope.launch {
            when (val result = partidoRepository.getPartidosEnVivo()) {
                is Result.Success -> {
                    val partidos = result.data
                    val currentHash = partidos.hashCode()
                    
                    if (currentHash == lastPartidosHash) return@launch

                    val liveCount = partidos.count { it.estado == PartidoDTO.ESTADO_EN_CURSO }
                    val upcomingCount = partidos.count { it.estado == PartidoDTO.ESTADO_POR_COMENZAR }
                    val finishedCount = partidos.count { it.estado == PartidoDTO.ESTADO_FINALIZADO }

                    if (partidos.isNotEmpty()) {
                        binding.cardPartidosEnVivo.visibility = View.VISIBLE
                        adapterPartidosEnVivo.submitList(partidos)

                        val headerText = when {
                            liveCount > 0 && upcomingCount > 0 -> "🔴 En Vivo · ⏳ Próximos"
                            liveCount > 0 -> "🔴 En Vivo"
                            upcomingCount > 0 -> "⏳ Próximos"
                            finishedCount > 0 -> "FINALIZADOS"
                            else -> "Partidos"
                        }
                        binding.tvLiveHeader.text = headerText
                        adapterPartidosEnVivo.notifyDataSetChanged()
                    } else {
                        binding.cardPartidosEnVivo.visibility = View.GONE
                    }

                    lastPartidosHash = currentHash
                }
                is Result.Error -> {
                    // Silently ignore errors for en vivo
                }
            }
        }
    }

    private fun showQRDialog(quiniela: QuinielaResumenDTO) {
        val dialogBinding = DialogQrBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.tvCodigo.text = quiniela.codigoInvitacion

        try {
            val size = 500
            val bitMatrix: BitMatrix = MultiFormatWriter().encode(
                quiniela.codigoInvitacion,
                BarcodeFormat.QR_CODE,
                size,
                size
            )
            val bitmap = createBitmap(size, size)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap[x, y] = if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
                }
            }
            dialogBinding.ivQR.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error QR: ${e.message}", Toast.LENGTH_SHORT).show()
        }

        dialogBinding.btnCompartir.setOnClickListener {
            compartirCodigo(quiniela.codigoInvitacion)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun compartirCodigo(codigo: String) {
        val mensaje = "Únete a mi quiniela! Usa el código: $codigo\n\nO escanea el código QR de la app Quiniela"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, mensaje)
        }
        startActivity(Intent.createChooser(intent, "Compartir con"))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.dashboard_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                authRepository.logout()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
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
    }

    private val handler = Handler(Looper.getMainLooper())
    private val pollingRunnable = object : Runnable {
        override fun run() {
            loadPartidosEnVivo()
            handler.postDelayed(this, 15000)
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
        adapterPartidosEnVivo.cleanup()
    }
}
