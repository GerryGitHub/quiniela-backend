import SwiftUI

struct QuinielaDetalleView: View {
    let quinielaId: Int
    let quinielaNombre: String
    @EnvironmentObject var authManager: AuthManager
    @StateObject private var viewModel = QuinielaDetalleViewModel()
    @State private var selectedTab = 0
    @State private var showSavedToast = false

    var body: some View {
        VStack(spacing: 0) {
            // Tab bar
            Picker("", selection: $selectedTab) {
                Text("Pronósticos").tag(0)
                Text("Posiciones").tag(1)
            }
            .pickerStyle(.segmented)
            .padding(.horizontal)
            .padding(.vertical, 8)

            if selectedTab == 0 {
                pronosticosTab
            } else {
                posicionesTab
            }
        }
        .navigationTitle(quinielaNombre)
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            viewModel.loadDetail(id: quinielaId, token: authManager.token)
        }
        .overlay {
            if viewModel.isLoading { ProgressView() }
        }
        .onChange(of: viewModel.savedMessage) { _, msg in
            if msg != nil { showSavedToast = true }
        }
        .alert("Guardado", isPresented: $showSavedToast) {
            Button("OK") { viewModel.savedMessage = nil }
        } message: {
            Text(viewModel.savedMessage ?? "")
        }
    }

    // MARK: - Pronósticos Tab

    private var pronosticosTab: some View {
        ScrollView {
            LazyVStack(spacing: 0, pinnedViews: []) {
                let grupos = agruparPartidos(viewModel.partidos)
                ForEach(grupos.indices, id: \.self) { idx in
                    let grupo = grupos[idx]
                    GrupHeaderView(grupo: grupo.grupo)

                    ForEach(grupo.partidos) { partido in
                        PartidoPredictionCardView(
                            partido: partido,
                            localPred: bindingLocal(partido.id),
                            visitPred: bindingVisitante(partido.id),
                            onChanged: { partidoId in
                                viewModel.dirtyIds.insert(partidoId)
                            }
                        )
                    }
                }
            }
            .padding(.horizontal)

            if !viewModel.dirtyIds.isEmpty {
                Button(action: savePronosticos) {
                    HStack {
                        if viewModel.saving {
                            ProgressView().tint(.white)
                        } else {
                            Text("Guardar Pronósticos (\(viewModel.dirtyIds.count))")
                        }
                    }
                    .font(.headline)
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color(hex: "1E88E5"))
                    .cornerRadius(12)
                }
                .disabled(viewModel.saving)
                .padding()
            }
        }
    }

    // MARK: - Posiciones Tab

    private var posicionesTab: some View {
        VStack(spacing: 0) {
            // Sticky "Tu posición"
            if let miPos = miPosicion() {
                HStack {
                    Text("Tu posición: #\(miPos)")
                        .font(.headline)
                        .foregroundColor(Color(hex: "1E88E5"))
                    Spacer()
                }
                .padding(.horizontal)
                .padding(.vertical, 8)
                .background(Color(.systemGray6))
            }

            if viewModel.participantes.isEmpty {
                VStack(spacing: 12) {
                    Image(systemName: "trophy")
                        .font(.system(size: 48))
                        .foregroundColor(.gray)
                    Text("El torneo aún no comienza")
                        .font(.headline)
                        .foregroundColor(.gray)
                    Text("Los resultados aparecerán aquí cuando haya partidos finalizados")
                        .font(.caption)
                        .foregroundColor(.gray.opacity(0.7))
                        .multilineTextAlignment(.center)
                }
                .padding(.top, 60)
                .padding(.horizontal)
                Spacer()
            } else {
                List {
                    ForEach(viewModel.participantes) { entry in
                        LeaderboardRow(entry: entry)
                    }
                }
                .listStyle(.plain)
                .refreshable {
                    viewModel.loadLeaderboard(id: quinielaId, token: authManager.token)
                }
            }
        }
        .onAppear {
            viewModel.loadLeaderboard(id: quinielaId, token: authManager.token)
        }
    }

    // MARK: - Helpers

    private func savePronosticos() {
        viewModel.savePronosticos(quinielaId: quinielaId, token: authManager.token)
    }

    private func bindingLocal(_ partidoId: Int) -> Binding<String> {
        Binding(
            get: { String(viewModel.misPronosticos[partidoId]?.local ?? 0) },
            set: { newVal in
                let val = Int(newVal) ?? 0
                if viewModel.misPronosticos[partidoId] != nil {
                    viewModel.misPronosticos[partidoId]?.local = val
                } else {
                    viewModel.misPronosticos[partidoId] = (val, 0)
                }
            }
        )
    }

    private func bindingVisitante(_ partidoId: Int) -> Binding<String> {
        Binding(
            get: { String(viewModel.misPronosticos[partidoId]?.visitante ?? 0) },
            set: { newVal in
                let val = Int(newVal) ?? 0
                if viewModel.misPronosticos[partidoId] != nil {
                    viewModel.misPronosticos[partidoId]?.visitante = val
                } else {
                    viewModel.misPronosticos[partidoId] = (0, val)
                }
            }
        )
    }

    private func agruparPartidos(_ partidos: [PartidoDTO]) -> [(grupo: String, partidos: [PartidoDTO])] {
        let order = ["A", "B", "C", "D", "E", "F", "G", "H"]
        var groups: [String: [PartidoDTO]] = [:]
        for p in partidos {
            let key = normalizarGrupo(p.grupo)
            groups[key, default: []].append(p)
        }
        return groups.keys.sorted { a, b in
            let ia = order.firstIndex(of: a) ?? 99
            let ib = order.firstIndex(of: b) ?? 99
            return ia < ib
        }.compactMap { key in
            guard let list = groups[key] else { return nil }
            return (key, list.sorted { $0.fechaHora < $1.fechaHora })
        }
    }

    private func normalizarGrupo(_ grupo: String?) -> String {
        guard let g = grupo, !g.isEmpty else { return "Sin grupo" }
        return String(g.trimmingCharacters(in: .whitespaces).prefix(1)).uppercased()
    }

    private func miPosicion() -> Int? {
        guard let email = authManager.currentUser?.email else { return nil }
        return viewModel.participantes.first(where: { $0.usuario.email == email })?.posicion
    }
}

// MARK: - Grupo Header

struct GrupHeaderView: View {
    let grupo: String

    var body: some View {
        HStack {
            Text("Grupo \(grupo)")
                .font(.headline)
                .foregroundColor(.primary)
            Spacer()
        }
        .padding(.vertical, 8)
        .padding(.horizontal, 4)
        .background(Color(.systemGray6))
        .cornerRadius(8)
        .padding(.top, 8)
    }
}

// MARK: - Partido Prediction Card (3 states)

struct PartidoPredictionCardView: View {
    let partido: PartidoDTO
    @Binding var localPred: String
    @Binding var visitPred: String
    let onChanged: (Int) -> Void

    private var esEditable: Bool { partido.estado == PartidoDTO.ESTADO_PENDIENTE }

    var body: some View {
        VStack(spacing: 8) {
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Text(partido.equipoLocal)
                        .font(.subheadline)
                        .fontWeight(esEditable ? .regular : .bold)
                    Text(partido.equipoVisitante)
                        .font(.subheadline)
                        .fontWeight(esEditable ? .regular : .bold)
                }

                Spacer()

                // Score input / display
                HStack(spacing: 8) {
                    if esEditable {
                        TextField("0", text: $localPred)
                            .textFieldStyle(.roundedBorder)
                            .keyboardType(.numberPad)
                            .frame(width: 40)
                            .multilineTextAlignment(.center)
                            .onChange(of: localPred) { _, _ in onChanged(partido.id) }
                        Text("-")
                            .font(.headline)
                        TextField("0", text: $visitPred)
                            .textFieldStyle(.roundedBorder)
                            .keyboardType(.numberPad)
                            .frame(width: 40)
                            .multilineTextAlignment(.center)
                            .onChange(of: visitPred) { _, _ in onChanged(partido.id) }
                    } else {
                        Text(scoreText)
                            .font(.headline)
                            .padding(.horizontal, 12)
                            .padding(.vertical, 6)
                            .background(scoreBgColor.opacity(0.15))
                            .cornerRadius(8)
                    }
                }
            }

            // State indicator
            HStack {
                stateChip
                Spacer()
                if esEditable {
                    Text("⏳ Por jugar")
                        .font(.caption)
                        .foregroundColor(.gray)
                }
            }
        }
        .padding()
        .background(Color.white)
        .cornerRadius(10)
        .shadow(color: .black.opacity(0.05), radius: 2)
        .padding(.vertical, 3)
    }

    @ViewBuilder
    private var stateChip: some View {
        switch partido.estado {
        case PartidoDTO.ESTADO_EN_CURSO:
            HStack(spacing: 4) {
                Circle()
                    .fill(Color.red)
                    .frame(width: 6, height: 6)
                Text("EN VIVO")
                    .font(.caption)
                    .foregroundColor(.red)
                if let mins = partido.minutosJugados {
                    Text(" • \(mins)'")
                        .font(.caption)
                        .foregroundColor(.red)
                }
            }
            .padding(.horizontal, 8)
            .padding(.vertical, 3)
            .background(Color.red.opacity(0.1))
            .cornerRadius(4)

            if let local = partido.golesLocalReal, let visit = partido.golesVisitanteReal {
                Text("Resultado parcial: \(local) - \(visit)")
                    .font(.caption)
                    .foregroundColor(.primary)
            }

        case PartidoDTO.ESTADO_FINALIZADO:
            HStack {
                Text("FINALIZADO")
                    .font(.caption)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 3)
                    .background(Color.gray.opacity(0.2))
                    .cornerRadius(4)
            }

            if let local = partido.golesLocalReal, let visit = partido.golesVisitanteReal {
                Text("Resultado final: \(local) - \(visit)")
                    .font(.caption)
                    .foregroundColor(.primary)
            }

        default:
            EmptyView()
        }
    }

    private var scoreText: String {
        let l = partido.golesLocalReal
        let v = partido.golesVisitanteReal
        if let l = l, let v = v { return "\(l) - \(v)" }
        return "vs"
    }

    private var scoreBgColor: Color {
        switch partido.estado {
        case PartidoDTO.ESTADO_EN_CURSO: return .red
        case PartidoDTO.ESTADO_FINALIZADO: return .gray
        default: return .orange
        }
    }
}

// MARK: - Leaderboard Row

struct LeaderboardRow: View {
    let entry: LeaderboardEntryDTO

    private var posicionColor: Color {
        switch entry.posicion {
        case 1: return Color(hex: "FFD700") // gold
        case 2: return Color(hex: "C0C0C0") // silver
        case 3: return Color(hex: "CD7F32") // bronze
        default: return .clear
        }
    }

    private var posicionEmoji: String? {
        switch entry.posicion {
        case 1: return "🥇"
        case 2: return "🥈"
        case 3: return "🥉"
        default: return nil
        }
    }

    var body: some View {
        HStack {
            if let emoji = posicionEmoji {
                Text(emoji)
                    .font(.title2)
            } else {
                Text("#\(entry.posicion)")
                    .font(.headline)
                    .foregroundColor(Color(hex: "1E88E5"))
                    .frame(width: 36)
            }

            VStack(alignment: .leading, spacing: 2) {
                Text(entry.usuario.nombre)
                    .font(.subheadline)
                    .fontWeight(.medium)
                Text("\(entry.aciertos) aciertos")
                    .font(.caption)
                    .foregroundColor(.gray)
            }

            Spacer()

            Text("\(entry.puntosTotales) pts")
                .font(.headline)
                .fontWeight(entry.posicion == 1 ? .bold : .regular)
                .foregroundColor(entry.posicion == 1 ? Color(hex: "FFD700") : .primary)
        }
        .padding(.vertical, 4)
        .padding(.horizontal, 8)
        .background(
            entry.posicion <= 3
                ? posicionColor.opacity(0.08)
                : Color.clear
        )
        .cornerRadius(8)
        .overlay(
            entry.posicion <= 3
                ? RoundedRectangle(cornerRadius: 8).stroke(posicionColor, lineWidth: 1)
                : nil
        )
    }
}
