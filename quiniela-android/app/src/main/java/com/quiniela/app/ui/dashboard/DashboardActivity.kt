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
import com.quiniela.app.repository.Result
import com.quiniela.app.ui.auth.LoginActivity
import com.quiniela.app.ui.quiniela.CrearQuinielaActivity
import com.quiniela.app.ui.quiniela.QuinielaDetalleActivity
import com.quiniela.app.ui.eliminatorias.EliminatoriasActivity
import com.quiniela.app.ui.quiniela.QuinielaAdapter
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
    private lateinit var adapter: QuinielaAdapter
    private var isFirstLoad = true

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
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    binding.scrollInicio.visibility = View.VISIBLE
                    binding.scrollEnVivo.visibility = View.GONE
                    binding.ivBackground.visibility = View.VISIBLE
                    true
                }
                R.id.nav_envivo -> {
                    binding.scrollInicio.visibility = View.GONE
                    binding.scrollEnVivo.visibility = View.VISIBLE
                    binding.ivBackground.visibility = View.VISIBLE
                    cargarEnVivo()
                    true
                }
                R.id.nav_eliminatorias -> {
                    startActivity(Intent(this, EliminatoriasActivity::class.java))
                    true
                }
                else -> false
            }
        }
        binding.bottomNav.selectedItemId = R.id.nav_inicio
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
        }
    }

    private fun cargarEnVivo() {
        lifecycleScope.launch {
            when (val result = partidoRepository.getPartidosEnVivo()) {
                is Result.Success -> {
                    val partidos = result.data
                    if (partidos.isNotEmpty()) {
                        val envivoAdapter = PartidoEnVivoAdapter()
                        binding.rvEnVivoFull.layoutManager = LinearLayoutManager(this@DashboardActivity)
                        binding.rvEnVivoFull.adapter = envivoAdapter
                        envivoAdapter.submitList(partidos)
                    }
                }
                is Result.Error -> { }
            }
        }
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
