package com.quiniela.app.ui.eliminatorias

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.quiniela.app.databinding.ActivityEliminatoriasBinding
import com.quiniela.app.repository.EliminatoriasRepository
import com.quiniela.app.repository.Result
import com.quiniela.app.util.UiUtils
import kotlinx.coroutines.launch

class EliminatoriasActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEliminatoriasBinding
    private val repository = EliminatoriasRepository()

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
        binding = ActivityEliminatoriasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            WindowInsetsCompat.CONSUMED
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        loadBracket()
    }

    private fun loadBracket() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            when (val result = repository.getPreview()) {
                is Result.Success -> {
                    val preview = result.data
                    if (preview.gruposActivos) {
                        binding.tvInfo.visibility = View.VISIBLE
                    }

                    val items = mutableListOf<BracketItem>()
                    val rondaOrder = listOf("R32", "R16", "QF", "SF", "3RD", "FINAL")
                    for (ronda in rondaOrder) {
                        val matches = preview.rondas[ronda]?.sortedBy { it.orden }
                        if (matches.isNullOrEmpty()) continue
                        items.add(BracketItem(type = 0, roundName = ronda))
                        for (match in matches) {
                            items.add(BracketItem(type = 1, match = match))
                        }
                    }

                    if (items.isEmpty()) {
                        binding.layoutEmpty.visibility = View.VISIBLE
                        binding.rvBracket.visibility = View.GONE
                    } else {
                        binding.rvBracket.layoutManager = LinearLayoutManager(this@EliminatoriasActivity)
                        binding.rvBracket.adapter = BracketAdapter(items, roundLabels)
                    }
                }
                is Result.Error -> {
                    binding.layoutEmpty.visibility = View.VISIBLE
                    UiUtils.showErrorSnackbar(binding.root, result.message)
                }
            }
            binding.progressBar.visibility = View.GONE
        }
    }
}
