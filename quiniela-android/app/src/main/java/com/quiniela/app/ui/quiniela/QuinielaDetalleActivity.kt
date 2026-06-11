package com.quiniela.app.ui.quiniela

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.MenuItem
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.quiniela.app.R
import com.quiniela.app.api.TokenManager
import com.quiniela.app.databinding.ActivityQuinielaDetalleBinding
import com.quiniela.app.databinding.DialogQrBinding
import com.quiniela.app.util.QrUtils
import com.quiniela.app.util.ShareUtils
import com.quiniela.app.model.*
import com.quiniela.app.repository.PronosticoRepository
import com.quiniela.app.repository.QuinielaRepository
import com.quiniela.app.repository.Result
import com.quiniela.app.util.CountryFlagResolver
import com.quiniela.app.util.UiUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class QuinielaDetalleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuinielaDetalleBinding
    private val quinielaRepository = QuinielaRepository()
    private val pronosticoRepository = PronosticoRepository()
    private var quinielaId: Long = 0
    private var lastLeaderboardHash: Int = 0
    
    private lateinit var partidosAdapter: PartidosConPronosticoAdapter
    private lateinit var participantesAdapter: ParticipantesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuinielaDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        quinielaId = intent.getLongExtra("quinielaId", 0)
        val nombreQuiniela = intent.getStringExtra("quinielaNombre") ?: "Quiniela"
        
        binding.toolbar.title = nombreQuiniela
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.inflateMenu(R.menu.quiniela_detalle_menu)
        binding.toolbar.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.action_qr -> {
                    val nombre = intent.getStringExtra("quinielaNombre") ?: "Quiniela"
                    val codigo = intent.getStringExtra("quinielaCodigo") ?: ""
                    if (codigo.isNotEmpty()) {
                        showQRDialog(nombre, codigo)
                    }
                    true
                }
                R.id.action_info -> {
                    showPuntosInfoDialog()
                    true
                }
                else -> false
            }
        }

        setupTabs()
        setupRecyclerViews()
        setupButtons()
        loadData()
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        binding.layoutPronosticos.visibility = View.VISIBLE
                        binding.layoutPosiciones.visibility = View.GONE
                    }
                    1 -> {
                        binding.layoutPronosticos.visibility = View.GONE
                        binding.layoutPosiciones.visibility = View.VISIBLE
                    }
                }
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
    }

    private fun setupRecyclerViews() {
        binding.rvPartidos.layoutManager = LinearLayoutManager(this)
        binding.rvParticipantes.layoutManager = LinearLayoutManager(this)
    }

    private fun setupButtons() {
        binding.btnGuardarPronosticos.setOnClickListener { guardarPronosticos() }
    }

    private fun actualizarBotonGuardar() {
        if (::partidosAdapter.isInitialized) {
            val dirtyCount = partidosAdapter.getDirtyPronosticos().size
            if (dirtyCount > 0) {
                binding.btnGuardarPronosticos.text = "Guardar Pronósticos ($dirtyCount)"
                binding.btnGuardarPronosticos.isEnabled = true
            } else {
                binding.btnGuardarPronosticos.text = "Guardar Pronósticos"
                binding.btnGuardarPronosticos.isEnabled = false
            }
        }
    }

    private fun loadData() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            when (val result = quinielaRepository.getQuinielaDetalle(quinielaId)) {
                is Result.Success -> {
                    val detalle = result.data
                    binding.toolbar.title = detalle.nombre
                    
 when (val pronosResult = pronosticoRepository.getMisPronosticosByQuiniela(quinielaId)) {
                        is Result.Success -> {
                            val totalPartidos = detalle.partidos.size
                            val pronosticosMap = pronosResult.data.pronosticos.associateBy { it.partido.id }
                            val partidosConPronostico = detalle.partidos.map { partido ->
                                val pronostico = pronosticosMap[partido.id]
                                PartidoConPronostico(
                                    partido = partido,
                                    golesLocalPredicho = pronostico?.golesLocalPredicho,
                                    golesVisitantePredicho = pronostico?.golesVisitantePredicho
                                )
                            }
                            val completados = partidosConPronostico.count {
                                it.golesLocalPredicho != null && it.golesVisitantePredicho != null
                            }
                            actualizarProgresoPronosticos(completados, totalPartidos)
                            val itemsAgrupados = crearListaAgrupada(partidosConPronostico)
                            partidosAdapter = PartidosConPronosticoAdapter(itemsAgrupados) { actualizarBotonGuardar() }
                            binding.rvPartidos.adapter = partidosAdapter
                            actualizarBotonGuardar()
                        }
                        is Result.Error -> {
                            val partidosConPronostico = detalle.partidos.map { partido ->
                                PartidoConPronostico(partido = partido)
                            }
                            val itemsAgrupados = crearListaAgrupada(partidosConPronostico)
                            partidosAdapter = PartidosConPronosticoAdapter(itemsAgrupados) { actualizarBotonGuardar() }
                            binding.rvPartidos.adapter = partidosAdapter
                            actualizarBotonGuardar()
                        }
                    }
                }
                is Result.Error -> {
                    UiUtils.showErrorSnackbar(binding.root, result.message)
                }
            }
            
            loadLeaderboard()
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun loadLeaderboard() {
        lifecycleScope.launch {
            when (val result = quinielaRepository.getLeaderboard(quinielaId)) {
                is Result.Success -> {
                    val data = result.data
                    val currentHash = data.hashCode()
                    if (currentHash == lastLeaderboardHash) return@launch

                    lastLeaderboardHash = currentHash

                    if (data.isEmpty()) {
                        binding.layoutEmpty.visibility = View.VISIBLE
                        binding.rvParticipantes.visibility = View.GONE
                    } else {
                        binding.layoutEmpty.visibility = View.GONE
                        binding.rvParticipantes.visibility = View.VISIBLE

                        if (::participantesAdapter.isInitialized) {
                            participantesAdapter.updateList(data)
                        } else {
                            participantesAdapter = ParticipantesAdapter(data)
                            binding.rvParticipantes.adapter = participantesAdapter
                        }

                        // Sticky "Tu posición"
                        val currentEmail = TokenManager.getUsuarioEmail()
                        if (currentEmail != null) {
                            val miEntry = data.find { it.usuario.email == currentEmail }
                            if (miEntry != null) {
                                val miIdx = data.indexOf(miEntry)
                                val nextEntry = if (miIdx >= 0 && miIdx < data.size - 1) data[miIdx + 1] else null
                                val diff = nextEntry?.let { miEntry.puntosTotales - it.puntosTotales }

                                binding.tvMiPosicion.text = "Tu posición: #${miEntry.posicion}"
                                binding.tvDiferenciaPosicion.visibility = if (diff != null && diff > 0) View.VISIBLE else View.GONE
                                if (diff != null && diff > 0) {
                                    binding.tvDiferenciaPosicion.text = "$diff pts por delante del siguiente puesto"
                                }
                                binding.layoutMiPosicion.visibility = View.VISIBLE
                            } else {
                                binding.layoutMiPosicion.visibility = View.GONE
                            }
                        } else {
                            binding.layoutMiPosicion.visibility = View.GONE
                        }
                    }
                }
                is Result.Error -> {}
            }
        }
    }

    private fun crearListaAgrupada(partidos: List<PartidoConPronostico>): List<GrupoExpansible> {
        val ordenGrupo = listOf("A", "B", "C", "D", "E", "F", "G", "H")
        
        val grouped = partidos.groupBy { 
            normalizarGrupo(it.partido.grupo) 
        }
        
        val gruposOrdenados = grouped.keys.sortedWith { a, b ->
            val idxA = ordenGrupo.indexOf(a)
            val idxB = ordenGrupo.indexOf(b)
            when {
                idxA != -1 && idxB != -1 -> idxA - idxB
                idxA != -1 -> -1
                idxB != -1 -> 1
                else -> a.compareTo(b)
            }
        }
        
        return gruposOrdenados.map { grupo ->
            GrupoExpansible(
                nombre = grupo,
                partidos = (grouped[grupo]?.sortedBy { it.partido.fechaHora } ?: emptyList()).toMutableList(),
                expandido = false
            )
        }
    }
    
    private fun normalizarGrupo(grupo: String?): String {
        if (grupo == null) return "Sin grupo"
        return grupo.trim().take(1).uppercase()
    }

    private val leaderboardHandler = Handler(Looper.getMainLooper())
    private val leaderboardPolling = object : Runnable {
        override fun run() {
            if (binding.layoutPosiciones.visibility == View.VISIBLE) {
                loadLeaderboard()
            }
            leaderboardHandler.postDelayed(this, 15000)
        }
    }

    override fun onResume() {
        super.onResume()
        leaderboardHandler.post(leaderboardPolling)
    }

    override fun onPause() {
        super.onPause()
        leaderboardHandler.removeCallbacks(leaderboardPolling)
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

    private fun showQRDialog(nombre: String, codigo: String) {
        val dialogBinding = DialogQrBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.tvCodigo.text = codigo

        val bitmap = QrUtils.generateQrBitmap(codigo, 500)
        if (bitmap != null) {
            dialogBinding.ivQR.setImageBitmap(bitmap)
        } else {
            Toast.makeText(this, "Error al generar QR", Toast.LENGTH_SHORT).show()
        }

        dialogBinding.btnCompartir.setOnClickListener {
            ShareUtils.shareQuiniela(this, nombre, codigo)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun guardarPronosticos() {
        val pronosticosModificados = partidosAdapter.getDirtyPronosticos()
        
        if (pronosticosModificados.isEmpty()) {
            UiUtils.showWarningSnackbar(binding.root, "No hay cambios para guardar")
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnGuardarPronosticos.isEnabled = false

        lifecycleScope.launch {
            val items = pronosticosModificados.map { pp ->
                PronosticoItemRequest(
                    idPartido = pp.partido.id,
                    golesLocalPredicho = pp.golesLocalPredicho ?: 0,
                    golesVisitantePredicho = pp.golesVisitantePredicho ?: 0
                )
            }
            
            when (val result = pronosticoRepository.crearPronosticosBatch(quinielaId, items)) {
                is Result.Success -> {
                    partidosAdapter.clearDirtyFlags()
                    UiUtils.showSuccessSnackbar(binding.root, "Pronósticos guardados: ${result.data.pronosticosGuardados}")
                    loadData()
                }
                is Result.Error -> {
                    UiUtils.showErrorSnackbar(binding.root, result.message)
                    binding.progressBar.visibility = View.GONE
                    binding.btnGuardarPronosticos.isEnabled = true
        }

    }
}

private fun partidoYaComenzo(fechaHora: String): Boolean {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        val fecha = sdf.parse(fechaHora)
        fecha != null && fecha.before(Date())
    } catch (e: Exception) {
        false
    }
}
    }

    private fun actualizarProgresoPronosticos(completados: Int, total: Int) {
        binding.progressPronosticos.progress = if (total > 0) (completados * 100 / total) else 0
        binding.tvProgresoContador.text = "$completados/$total"
        binding.tvProgresoCompletados.text = "$completados completados"
        binding.tvProgresoPendientes.text = "${total - completados} pendientes"
        binding.tvProgresoPendientes.visibility = if (total - completados > 0) View.VISIBLE else View.GONE
        binding.layoutProgresoPronosticos.visibility = if (total > 0) View.VISIBLE else View.GONE
    }
}

data class PartidoConPronostico(
    val partido: PartidoDTO,
    var golesLocalPredicho: Int? = null,
    var golesVisitantePredicho: Int? = null,
    var dirty: Boolean = false
)

data class GrupoExpansible(
    val nombre: String,
    val partidos: MutableList<PartidoConPronostico>,
    var expandido: Boolean = false
)

class PartidosConPronosticoAdapter(
    private val grupos: List<GrupoExpansible>,
    private val onDirtyChanged: () -> Unit = {}
) : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_PARTIDO = 1
    }

    override fun getItemViewType(position: Int): Int {
        var headersCount = 0
        for (grupo in grupos) {
            if (position == headersCount) return TYPE_HEADER
            headersCount++
            if (grupo.expandido) {
                if (position < headersCount + grupo.partidos.size) return TYPE_PARTIDO
                headersCount += grupo.partidos.size
            }
        }
        return TYPE_PARTIDO
    }

    override fun getItemCount(): Int {
        var count = grupos.size
        for (grupo in grupos) {
            if (grupo.expandido) count += grupo.partidos.size
        }
        return count
    }

    private fun getItem(position: Int): Any? {
        var headersCount = 0
        for (grupo in grupos) {
            if (position == headersCount) return grupo
            headersCount++
            if (grupo.expandido) {
                val partidoIndex = position - headersCount
                if (partidoIndex < grupo.partidos.size) {
                    return grupo.partidos[partidoIndex]
                }
                headersCount += grupo.partidos.size
            }
        }
        return null
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val binding = com.quiniela.app.databinding.ItemGrupoHeaderBinding.inflate(
                    android.view.LayoutInflater.from(parent.context), parent, false)
                HeaderViewHolder(binding)
            }
            else -> {
                val binding = com.quiniela.app.databinding.ItemQuinielaPartidoBinding.inflate(
                    android.view.LayoutInflater.from(parent.context), parent, false)
                ViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (item) {
            is GrupoExpansible -> (holder as HeaderViewHolder).bind(item)
            is PartidoConPronostico -> (holder as ViewHolder).bind(item)
            null -> {}
        }
    }

    fun getAllPronosticos(): List<PartidoConPronostico> {
        return grupos.flatMap { it.partidos }
    }

    fun getDirtyPronosticos(): List<PartidoConPronostico> {
        return grupos.flatMap { it.partidos }.filter { it.dirty }
    }

    fun clearDirtyFlags() {
        grupos.flatMap { it.partidos }.forEach { it.dirty = false }
    }

    fun hasDirtyPronosticos(): Boolean {
        return grupos.flatMap { it.partidos }.any { it.dirty }
    }

    inner class HeaderViewHolder(private val binding: com.quiniela.app.databinding.ItemGrupoHeaderBinding) : 
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        
        fun bind(grupo: GrupoExpansible) {
            binding.tvHeader.text = "Grupo ${grupo.nombre}"
            binding.ivExpandido.setImageResource(
                if (grupo.expandido) android.R.drawable.arrow_up_float 
                else android.R.drawable.arrow_down_float
            )
            binding.root.setOnClickListener {
                grupo.expandido = !grupo.expandido
                notifyItemRangeChanged(
                    adapterPosition, 
                    if (grupo.expandido) grupo.partidos.size + 1 else 1
                )
            }
            binding.root.animate()
                .alpha(1f)
                .setDuration(200)
                .start()
        }
    }

    inner class ViewHolder(private val binding: com.quiniela.app.databinding.ItemQuinielaPartidoBinding) : 
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        
        private var textWatcher: TextWatcher? = null
        private var partidoActual: PartidoConPronostico? = null
        
        fun bind(item: PartidoConPronostico) {
            partidoActual = item
            val ctx = binding.root.context

            binding.tvLocal.text = item.partido.equipoLocal
            val localFlag = CountryFlagResolver.getFlagDrawable(ctx, item.partido.equipoLocal)
            binding.tvLocal.setCompoundDrawablesRelativeWithIntrinsicBounds(localFlag, null, null, null)
            binding.tvVisitante.text = item.partido.equipoVisitante
            val visitFlag = CountryFlagResolver.getFlagDrawable(ctx, item.partido.equipoVisitante)
            binding.tvVisitante.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, visitFlag, null)
            binding.tvFecha.text = item.partido.fechaHora

            val esEditable = item.partido.estado == PartidoDTO.ESTADO_PENDIENTE && !partidoYaComenzo(item.partido.fechaHora)
            binding.etGolesLocal.isEnabled = esEditable
            binding.etGolesVisitante.isEnabled = esEditable

            binding.etGolesLocal.removeTextChangedListener(textWatcher)
            binding.etGolesVisitante.removeTextChangedListener(textWatcher)

            binding.etGolesLocal.setText(item.golesLocalPredicho?.toString() ?: "")
            binding.etGolesVisitante.setText(item.golesVisitantePredicho?.toString() ?: "")

            textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    partidoActual?.let { partido ->
                        partido.golesLocalPredicho = binding.etGolesLocal.text.toString().toIntOrNull()
                        partido.golesVisitantePredicho = binding.etGolesVisitante.text.toString().toIntOrNull()
                        partido.dirty = true
                        onDirtyChanged()
                    }
                }
            }

            binding.etGolesLocal.addTextChangedListener(textWatcher)
            binding.etGolesVisitante.addTextChangedListener(textWatcher)

            when (item.partido.estado) {
                PartidoDTO.ESTADO_EN_CURSO -> bindEnCurso(item)
                PartidoDTO.ESTADO_FINALIZADO -> bindFinalizado(item)
                else -> bindPendiente(item)
            }
        }

        private fun bindPendiente(item: PartidoConPronostico) {
            binding.layoutStateChip.visibility = View.GONE
            binding.tvResultadoReal.text = "⏳ Por jugar"
            binding.tvResultadoReal.setTextColor(0x99FFFFFF.toInt())
            binding.tvResultadoReal.textSize = 14f
            binding.tvMiPronostico.visibility = View.GONE
        }

        private fun bindEnCurso(item: PartidoConPronostico) {
            val ctx = binding.root.context

            binding.layoutStateChip.visibility = View.VISIBLE
            binding.vLiveDot.visibility = View.VISIBLE
            UiUtils.startLivePulse(binding.vLiveDot)
            binding.tvEstado.text = "EN VIVO"
            binding.tvEstado.setTextColor(androidx.core.content.ContextCompat.getColor(ctx, com.quiniela.app.R.color.error))

            val mins = item.partido.minutosJugados
            if (mins != null) {
                binding.tvMinutos.text = " • $mins'"
                binding.tvMinutos.visibility = View.VISIBLE
                binding.tvMinutos.setTextColor(androidx.core.content.ContextCompat.getColor(ctx, com.quiniela.app.R.color.error))
            } else {
                binding.tvMinutos.visibility = View.GONE
            }

            val local = item.partido.golesLocalReal
            val visitante = item.partido.golesVisitanteReal
            if (local != null && visitante != null) {
                binding.tvResultadoReal.text = "Resultado parcial: $local - $visitante"
            } else {
                binding.tvResultadoReal.text = "EN VIVO"
            }
            binding.tvResultadoReal.setTextColor(0xFFFFFFFF.toInt())
            binding.tvResultadoReal.textSize = 15f
            binding.tvMiPronostico.visibility = View.GONE
        }

        private fun bindFinalizado(item: PartidoConPronostico) {
            val ctx = binding.root.context

            binding.layoutStateChip.visibility = View.VISIBLE
            binding.vLiveDot.visibility = View.GONE
            binding.tvEstado.text = "FINALIZADO"
            binding.tvEstado.setTextColor(androidx.core.content.ContextCompat.getColor(ctx, com.quiniela.app.R.color.text_secondary))
            binding.tvMinutos.visibility = View.GONE

            val local = item.partido.golesLocalReal
            val visitante = item.partido.golesVisitanteReal
            if (local != null && visitante != null) {
                binding.tvResultadoReal.text = "Resultado final: $local - $visitante"
            } else {
                binding.tvResultadoReal.text = "FINALIZADO"
            }
            binding.tvResultadoReal.setTextColor(0xFFFFFFFF.toInt())
            binding.tvResultadoReal.textSize = 15f

            binding.tvMiPronostico.visibility = View.VISIBLE
            binding.tvMiPronostico.text = "Tu pronóstico: ${item.golesLocalPredicho} - ${item.golesVisitantePredicho}"
        }
    }
}

class ParticipantesAdapter(private var participantes: List<LeaderboardEntryDTO>) : 
    androidx.recyclerview.widget.RecyclerView.Adapter<ParticipantesAdapter.ViewHolder>() {

    private val posicionColors = intArrayOf(
        0xFFFFD700.toInt(), // gold
        0xFFC0C0C0.toInt(), // silver
        0xFFCD7F32.toInt()  // bronze
    )

    fun updateList(newList: List<LeaderboardEntryDTO>) {
        if (participantes.hashCode() != newList.hashCode()) {
            participantes = newList
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val binding = com.quiniela.app.databinding.ItemQuinielaParticipanteBinding.inflate(
            android.view.LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val nextPts = if (position < participantes.size - 1) participantes[position + 1].puntosTotales else null
        holder.bind(participantes[position], position + 1, nextPts)
    }

    override fun getItemCount() = participantes.size

    inner class ViewHolder(private val binding: com.quiniela.app.databinding.ItemQuinielaParticipanteBinding) : 
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        fun bind(entry: LeaderboardEntryDTO, posicion: Int, nextPts: Int? = null) {
            val context = binding.root.context

            if (posicion <= 3) {
                binding.cardRoot.strokeColor = posicionColors[posicion - 1]
                binding.cardRoot.strokeWidth = (2 * context.resources.displayMetrics.density).toInt()
                binding.tvPosicion.text = when (posicion) {
                    1 -> "\uD83E\uDD47"
                    2 -> "\uD83E\uDD48"
                    3 -> "\uD83E\uDD49"
                    else -> posicion.toString()
                }
            } else {
                binding.cardRoot.strokeColor = androidx.core.content.ContextCompat.getColor(context, com.quiniela.app.R.color.border_soft)
                binding.cardRoot.strokeWidth = (1 * context.resources.displayMetrics.density).toInt()
                binding.tvPosicion.text = "#$posicion"
            }

            binding.tvNombre.text = entry.usuario.nombre

            val ptsText = "${entry.puntosTotales} pts"
            binding.tvPuntos.text = ptsText
            binding.tvPuntos.setTextSize(if (posicion == 1) 18f else 16f)

            binding.tvAciertos.text = "${entry.aciertos} aciertos"

            val diff = nextPts?.let { entry.puntosTotales - it }
            if (diff != null && diff > 0) {
                binding.tvDiferencia.text = "+$diff pts al siguiente"
                binding.tvDiferencia.visibility = View.VISIBLE
            } else {
                binding.tvDiferencia.visibility = View.GONE
            }
        }
    }
}