package com.quiniela.app.ui.dashboard

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.quiniela.app.util.UiUtils
import com.quiniela.app.util.ShareUtils
import com.quiniela.app.util.QrUtils
import com.quiniela.app.util.CountryFlagResolver
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.quiniela.app.databinding.DialogQrBinding
import com.quiniela.app.model.PartidoDTO
import com.quiniela.app.model.QuinielaResumenDTO
import com.quiniela.app.model.QuinielaDetalleDTO
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private val authRepository = AuthRepository()
    private val quinielaRepository = QuinielaRepository()
    private val partidoRepository = PartidoRepository()
    private lateinit var adapter: QuinielaAdapter
    private lateinit var adapterPartidosEnVivo: PartidoEnVivoAdapter
    private var lastPartidosHash: Int = 0
    private var isFirstLoad = true
    private var countdownTimer: CountDownTimer? = null
    private var proximoPartido: PartidoDTO? = null
    private var proximaQuinielaId: Long = 0
    private var proximaQuinielaNombre: String = ""

    override fun onResume() {
        super.onResume()
        if (com.quiniela.app.api.RetrofitClient.sessionExpired) {
            com.quiniela.app.api.RetrofitClient.sessionExpired = false
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

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
                intent.putExtra("quinielaCodigo", quiniela.codigoInvitacion)
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
                        cargarProximoPartido(quinielas[0].id, quinielas[0].nombre)
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

                        val hasLiveOrUpcoming = liveCount > 0 || upcomingCount > 0
                        binding.layoutLiveSectionHeader.visibility = if (hasLiveOrUpcoming) View.VISIBLE else View.GONE

                        val headerText = when {
                            liveCount > 0 && upcomingCount > 0 -> "🔴 En Vivo · ⏳ Próximos"
                            liveCount > 0 -> "🔴 En Vivo"
                            upcomingCount > 0 -> "⏳ Próximos"
                            else -> ""
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

    private fun cargarProximoPartido(quinielaId: Long, quinielaNombre: String) {
        lifecycleScope.launch {
            when (val result = quinielaRepository.getQuinielaDetalle(quinielaId)) {
                is Result.Success -> {
                    val partidos = result.data.partidos
                        .filter { it.estado == PartidoDTO.ESTADO_PENDIENTE }
                        .sortedBy { it.fechaHora }
                    val match = partidos.firstOrNull()
                    if (match != null) {
                        proximoPartido = match
                        proximaQuinielaId = quinielaId
                        proximaQuinielaNombre = quinielaNombre
                        setupProximoPartidoCard(match, quinielaNombre)
                    } else {
                        binding.cardProximoPartido.visibility = View.GONE
                    }
                }
                is Result.Error -> {
                    binding.cardProximoPartido.visibility = View.GONE
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupProximoPartidoCard(match: PartidoDTO, quinielaNombre: String) {
        binding.cardProximoPartido.visibility = View.VISIBLE
        binding.tvProximoQuiniela.text = quinielaNombre

        val localFlag = CountryFlagResolver.getFlagDrawable(this, match.equipoLocal)
        binding.ivProximoLocal.setImageDrawable(localFlag)
        binding.tvProximoLocal.text = match.equipoLocal

        val visitFlag = CountryFlagResolver.getFlagDrawable(this, match.equipoVisitante)
        binding.ivProximoVisitante.setImageDrawable(visitFlag)
        binding.tvProximoVisitante.text = match.equipoVisitante

        binding.tvProximoFecha.text = try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
            val date = sdf.parse(match.fechaHora)
            val out = java.text.SimpleDateFormat("d 'de' MMMM '—' HH:mm", java.util.Locale("es", "MX"))
            out.format(date!!)
        } catch (e: Exception) {
            match.fechaHora
        }

        iniciarCuentaRegresiva(match.fechaHora)

        binding.btnPronosticarProximo.setOnClickListener {
            countdownTimer?.cancel()
            val intent = Intent(this, QuinielaDetalleActivity::class.java)
            intent.putExtra("quinielaId", proximaQuinielaId)
            intent.putExtra("quinielaNombre", proximaQuinielaNombre)
            intent.putExtra("quinielaCodigo", "")
            startActivity(intent)
        }
    }

    private fun iniciarCuentaRegresiva(fechaHora: String) {
        countdownTimer?.cancel()
        countdownTimer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val diff = try {
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                    sdf.parse(fechaHora)?.time?.minus(System.currentTimeMillis()) ?: 0L
                } catch (e: Exception) { 0L }

                if (diff <= 0) {
                    binding.layoutCuentaRegresiva.visibility = View.GONE
                    return
                }

                binding.layoutCuentaRegresiva.visibility = View.VISIBLE
                val dias = diff / 86400000
                val horas = (diff % 86400000) / 3600000
                val minutos = (diff % 3600000) / 60000
                val segundos = (diff % 60000) / 1000

                binding.tvCountdownDias.text = String.format("%02d", dias)
                binding.tvCountdownHoras.text = String.format("%02d", horas)
                binding.tvCountdownMinutos.text = String.format("%02d", minutos)
                binding.tvCountdownSegundos.text = String.format("%02d", segundos)
            }

            override fun onFinish() {
                binding.layoutCuentaRegresiva.visibility = View.GONE
            }
        }.start()
    }

    private fun showQRDialog(quiniela: QuinielaResumenDTO) {
        val dialogBinding = DialogQrBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.tvCodigo.text = quiniela.codigoInvitacion

        val bitmap = QrUtils.generateQrBitmap(quiniela.codigoInvitacion, 500)
        if (bitmap != null) {
            dialogBinding.ivQR.setImageBitmap(bitmap)
        } else {
            Toast.makeText(this, "Error al generar QR", Toast.LENGTH_SHORT).show()
        }

        dialogBinding.btnCompartir.setOnClickListener {
            ShareUtils.shareQuiniela(this, quiniela.nombre, quiniela.codigoInvitacion)
            dialog.dismiss()
        }

        dialog.show()
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

    private fun showPuntosInfoDialog() {
        val dialogBinding = com.quiniela.app.databinding.DialogPuntosInfoBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .create()
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(0xE80B1220.toInt()))
        dialog.window?.setDimAmount(0.6f)
        dialog.show()
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

        binding.cardPuntosInfo.setOnClickListener { showPuntosInfoDialog() }
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
        countdownTimer?.cancel()
        handler.removeCallbacks(pollingRunnable)
        adapterPartidosEnVivo.cleanup()
    }

    override fun onDestroy() {
        super.onDestroy()
        countdownTimer?.cancel()
    }
}
