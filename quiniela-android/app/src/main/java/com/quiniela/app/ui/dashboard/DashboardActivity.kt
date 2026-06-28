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
import androidx.activity.enableEdgeToEdge
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
import com.quiniela.app.repository.EliminatoriasRepository
import com.quiniela.app.repository.Result
import com.quiniela.app.ui.auth.LoginActivity
import com.quiniela.app.ui.quiniela.CrearQuinielaActivity
import com.quiniela.app.ui.quiniela.QuinielaDetalleActivity
import com.quiniela.app.ui.quiniela.QuinielaAdapter
import com.quiniela.app.ui.eliminatorias.BracketAdapter
import com.quiniela.app.ui.eliminatorias.BracketItem
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.quiniela.app.databinding.DialogQrBinding
import com.quiniela.app.model.PartidoDTO
import com.quiniela.app.model.QuinielaResumenDTO
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private val authRepository = AuthRepository()
    private val quinielaRepository = QuinielaRepository()
    private val partidoRepository = PartidoRepository()
    private val eliminatoriasRepository = EliminatoriasRepository()
    private lateinit var adapter: QuinielaAdapter
    private var isFirstLoad = true
    private var primeraQuinielaId: Long? = null

    private val roundLabels = mapOf(
        "R32" to "R32 — Dieciseisavos",
        "R16" to "R16 — Octavos",
        "QF" to "QF — Cuartos",
        "SF" to "SF — Semifinales",
        "3RD" to "3.er — Tercer lugar",
        "FINAL" to "Final"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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

        binding.rvQuinielas.layoutManager = LinearLayoutManager(this)
        binding.rvQuinielas.adapter = adapter

        setupBottomNav()
        setupButtons()
        loadData()
    }

    private fun setupBottomNav() {
        selectTab(TAB_INICIO)

        binding.tabInicio.setOnClickListener { selectTab(TAB_INICIO) }
        binding.tabEnVivo.setOnClickListener { selectTab(TAB_ENVIVO) }
        binding.tabEliminatorias.setOnClickListener { selectTab(TAB_ELIMINATORIAS) }
    }

    private fun selectTab(tab: String) {
        animateTabClick(tab)
        when (tab) {
            TAB_INICIO -> {
                binding.scrollInicio.visibility = View.VISIBLE
                binding.scrollEnVivo.visibility = View.GONE
                binding.scrollEliminatorias.visibility = View.GONE
                binding.btnCrearQuiniela.visibility = View.VISIBLE
                binding.btnUnirseQuiniela.visibility = View.VISIBLE
                binding.ivBackground.visibility = View.VISIBLE
            }
            TAB_ENVIVO -> {
                binding.scrollInicio.visibility = View.GONE
                binding.scrollEnVivo.visibility = View.VISIBLE
                binding.scrollEliminatorias.visibility = View.GONE
                binding.btnCrearQuiniela.visibility = View.GONE
                binding.btnUnirseQuiniela.visibility = View.GONE
                binding.ivBackground.visibility = View.VISIBLE
                cargarEnVivo()
            }
            TAB_ELIMINATORIAS -> {
                binding.scrollInicio.visibility = View.GONE
                binding.scrollEnVivo.visibility = View.GONE
                binding.scrollEliminatorias.visibility = View.VISIBLE
                binding.btnCrearQuiniela.visibility = View.GONE
                binding.btnUnirseQuiniela.visibility = View.GONE
                binding.ivBackground.visibility = View.VISIBLE
                cargarEliminatorias()
            }
        }
    }

    private fun animateTabClick(tab: String) {
        val tabs = listOf(
            Triple(binding.tabInicio, binding.glowInicio, binding.lineInicio) to TAB_INICIO,
            Triple(binding.tabEnVivo, binding.glowEnVivo, binding.lineEnVivo) to TAB_ENVIVO,
            Triple(binding.tabEliminatorias, binding.glowEliminatorias, binding.lineEliminatorias) to TAB_ELIMINATORIAS
        )

        for ((views, id) in tabs) {
            val (tabView, glow, line) = views
            val icon = when (id) {
                TAB_INICIO -> binding.ivTabInicio
                TAB_ENVIVO -> binding.ivTabEnVivo
                TAB_ELIMINATORIAS -> binding.ivTabEliminatorias
                else -> null
            }
            val text = when (id) {
                TAB_INICIO -> binding.tvTabInicio
                TAB_ENVIVO -> binding.tvTabEnVivo
                TAB_ELIMINATORIAS -> binding.tvTabEliminatorias
                else -> null
            }

            if (id == tab) {
                // Scale pulse animation
                tabView.animate()
                    .scaleX(0.93f).scaleY(0.93f)
                    .setDuration(100)
                    .withEndAction {
                        tabView.animate()
                            .scaleX(1f).scaleY(1f)
                            .setDuration(150)
                            .start()
                    }
                    .start()

                // Glow fade in
                glow.animate().alpha(0.25f).setDuration(200).start()
                // Line fade in
                line.animate().alpha(1f).setDuration(200).start()

                icon?.setColorFilter(android.graphics.Color.WHITE)
                text?.setTextColor(android.graphics.Color.WHITE)
            } else {
                glow.animate().alpha(0f).setDuration(150).start()
                line.animate().alpha(0f).setDuration(150).start()

                icon?.setColorFilter(android.graphics.Color.parseColor("#8A8AA5"))
                text?.setTextColor(android.graphics.Color.parseColor("#8A8AA5"))
            }
        }
    }

    companion object {
        private const val TAB_INICIO = "inicio"
        private const val TAB_ENVIVO = "envivo"
        private const val TAB_ELIMINATORIAS = "eliminatorias"
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
                    primeraQuinielaId = quinielas.firstOrNull()?.id
                    if (quinielas.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.rvQuinielas.visibility = View.GONE
                    } else {
                        binding.tvEmpty.visibility = View.GONE
                        binding.rvQuinielas.visibility = View.VISIBLE
                        adapter.submitSections(
                            quinielas.filter { it.estado != "FINALIZADA" },
                            quinielas.filter { it.estado == "FINALIZADA" }
                        )
                    }
                }
                is Result.Error -> {
                    UiUtils.showErrorSnackbar(binding.root, result.message)
                }
            }
            binding.progressBar.visibility = View.GONE
            isFirstLoad = false
        }
    }

    private fun cargarEnVivo() {
        lifecycleScope.launch {
            // Próximo partido
            when (val proxResult = partidoRepository.getPartidos()) {
                is Result.Success -> {
                    val ahora = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US).format(java.util.Date())
                    val prox = proxResult.data
                        .filter { it.estado == PartidoDTO.ESTADO_PENDIENTE || it.estado == PartidoDTO.ESTADO_POR_COMENZAR }
                        .filter { it.fechaHora >= ahora }
                        .minByOrNull { it.fechaHora }
                    if (prox != null) {
                        binding.cardProximoPartido.visibility = View.VISIBLE
                        val ctx = this@DashboardActivity
                        binding.ivProximoLocal.setImageDrawable(CountryFlagResolver.getFlagDrawable(ctx, prox.equipoLocal))
                        binding.tvProximoLocal.text = prox.equipoLocal
                        binding.ivProximoVisitante.setImageDrawable(CountryFlagResolver.getFlagDrawable(ctx, prox.equipoVisitante))
                        binding.tvProximoVisitante.text = prox.equipoVisitante
                        iniciarCuentaRegresiva(prox.fechaHora)
                        binding.btnPronosticarProximo.setOnClickListener {
                            val id = primeraQuinielaId ?: return@setOnClickListener
                            val intent = Intent(this, com.quiniela.app.ui.quiniela.QuinielaDetalleActivity::class.java)
                            intent.putExtra("quinielaId", id)
                            startActivity(intent)
                        }
                    } else {
                        binding.cardProximoPartido.visibility = View.GONE
                    }
                }
                is Result.Error -> {
                    binding.cardProximoPartido.visibility = View.GONE
                }
            }

            // En vivo
            when (val result = partidoRepository.getPartidosEnVivo()) {
                is Result.Success -> {
                    val partidos = result.data
                    if (partidos.isNotEmpty()) {
                        val envivoAdapter = PartidoEnVivoAdapter()
                        binding.rvEnVivoFull.layoutManager = LinearLayoutManager(this@DashboardActivity)
                        binding.rvEnVivoFull.adapter = envivoAdapter
                        envivoAdapter.submitList(partidos)
                    } else {
                        binding.rvEnVivoFull.adapter = null
                    }
                }
                is Result.Error -> { }
            }
        }
    }

    private fun cargarEliminatorias() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            when (val result = eliminatoriasRepository.getPreview()) {
                is Result.Success -> {
                    val preview = result.data
                    if (preview.gruposActivos) {
                        binding.tvEliminatoriasInfo.visibility = View.VISIBLE
                        binding.tvEliminatoriasInfo.text = "Fase de grupos en curso — los cruces se definen al finalizar"
                    } else {
                        binding.tvEliminatoriasInfo.visibility = View.GONE
                    }

                    val items = mutableListOf<BracketItem>()
                    val rondaOrder = listOf("R32", "R16")
                    for (ronda in rondaOrder) {
                        val matches = preview.rondas[ronda]?.sortedBy { it.orden }
                        if (matches.isNullOrEmpty()) continue
                        val reales = matches.filter { it.resuelto }
                        if (reales.isEmpty()) continue
                        items.add(BracketItem(type = 0, roundName = ronda))
                        for (match in reales) {
                            items.add(BracketItem(type = 1, match = match))
                        }
                    }

                    if (items.isEmpty()) {
                        binding.layoutEliminatoriasEmpty.visibility = View.VISIBLE
                        binding.rvBracket.visibility = View.GONE
                    } else {
                        binding.layoutEliminatoriasEmpty.visibility = View.GONE
                        binding.rvBracket.visibility = View.VISIBLE
                        binding.rvBracket.layoutManager = LinearLayoutManager(this@DashboardActivity)
                        binding.rvBracket.adapter = BracketAdapter(items, roundLabels)
                    }
                }
                is Result.Error -> {
                    binding.layoutEliminatoriasEmpty.visibility = View.VISIBLE
                    UiUtils.showErrorSnackbar(binding.root, result.message)
                }
            }
            binding.progressBar.visibility = View.GONE
        }
    }

    private var countdownHandler: android.os.Handler = android.os.Handler(android.os.Looper.getMainLooper())
    private var countdownRunnable: Runnable? = null

    private fun iniciarCuentaRegresiva(fechaHora: String) {
        countdownRunnable?.let { countdownHandler.removeCallbacks(it) }
        countdownRunnable = object : Runnable {
            override fun run() {
                try {
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
                    val fecha = sdf.parse(fechaHora)
                    val ahora = java.util.Date()
                    if (fecha != null && fecha.after(ahora)) {
                        val diff = fecha.time - ahora.time
                        val dias = diff / (24 * 60 * 60 * 1000)
                        val horas = (diff % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000)
                        val minutos = (diff % (60 * 60 * 1000)) / (60 * 1000)
                        val segundos = (diff % (60 * 1000)) / 1000
                        binding.tvCountdownDias.text = String.format("%02d", dias)
                        binding.tvCountdownHoras.text = String.format("%02d", horas)
                        binding.tvCountdownMinutos.text = String.format("%02d", minutos)
                        binding.tvCountdownSegundos.text = String.format("%02d", segundos)
                        countdownHandler.postDelayed(this, 1000)
                    } else {
                        binding.tvCountdownDias.text = "00"
                        binding.tvCountdownHoras.text = "00"
                        binding.tvCountdownMinutos.text = "00"
                        binding.tvCountdownSegundos.text = "00"
                    }
                } catch (e: Exception) {
                    countdownHandler.postDelayed(this, 1000)
                }
            }
        }
        countdownHandler.post(countdownRunnable!!)
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

    override fun onResume() {
        super.onResume()
        if (com.quiniela.app.api.RetrofitClient.sessionExpired) {
            com.quiniela.app.api.RetrofitClient.sessionExpired = false
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        loadData()
    }
}
