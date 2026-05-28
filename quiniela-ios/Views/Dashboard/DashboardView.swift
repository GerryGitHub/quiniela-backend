import SwiftUI

struct DashboardView: View {
    @EnvironmentObject var authManager: AuthManager
    @StateObject private var viewModel = DashboardViewModel()

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Bienvenido")
                            .font(.title2)
                            .foregroundColor(.gray)
                        Text(authManager.currentUser?.nombre ?? "Usuario")
                            .font(.title)
                            .fontWeight(.bold)
                    }
                    .padding(.horizontal)
                    .padding(.top)

                    if !viewModel.partidosEnVivo.isEmpty {
                        LiveMatchesCard(partidos: viewModel.partidosEnVivo)
                    }

                    VStack(alignment: .leading, spacing: 12) {
                        HStack {
                            Text("Mis Quinielas")
                                .font(.headline)
                            Spacer()
                            NavigationLink(destination: QuinielasView()) {
                                Text("Ver todas")
                                    .font(.subheadline)
                                    .foregroundColor(Color(hex: "1E88E5"))
                            }
                        }

                        if viewModel.quinielas.isEmpty {
                            EmptyQuinielasCard()
                        } else {
                            ForEach(viewModel.quinielas.prefix(3)) { quiniela in
                                NavigationLink(destination: QuinielaDetalleView(quinielaId: quiniela.id, quinielaNombre: quiniela.nombre)) {
                                    QuinielaCard(quiniela: quiniela)
                                }
                            }
                        }
                    }
                    .padding(.horizontal)
                }
                .padding(.bottom, 20)
            }
            .navigationTitle("Quiniela")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Cerrar") { authManager.logout() }
                }
            }
            .onAppear {
                viewModel.loadData(token: authManager.token)
            }
            .overlay {
                if viewModel.isLoading { ProgressView() }
            }
        }
    }
}

// MARK: - Live Matches Card (3-state)

struct LiveMatchesCard: View {
    let partidos: [PartidoDTO]

    private var liveCount: Int { partidos.filter { $0.estado == PartidoDTO.ESTADO_EN_CURSO }.count }
    private var upcomingCount: Int { partidos.filter { $0.estado == PartidoDTO.ESTADO_POR_COMENZAR }.count }
    private var finishedCount: Int { partidos.filter { $0.estado == PartidoDTO.ESTADO_FINALIZADO }.count }

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Circle()
                    .fill(Color.red)
                    .frame(width: 8, height: 8)
                Text(headerText)
                    .font(.headline)
                    .foregroundColor(.red)
            }

            ForEach(partidos) { partido in
                LiveMatchRow(partido: partido)
            }
        }
        .padding()
        .background(Color.white)
        .cornerRadius(12)
        .shadow(color: .black.opacity(0.1), radius: 4)
        .padding(.horizontal)
    }

    private var headerText: String {
        if liveCount > 0 && upcomingCount > 0 { return "🔴 En Vivo · ⏳ Próximos" }
        if liveCount > 0 { return "🔴 En Vivo" }
        if upcomingCount > 0 { return "⏳ Próximos" }
        if finishedCount > 0 { return "FINALIZADOS" }
        return "Partidos"
    }
}

struct LiveMatchRow: View {
    let partido: PartidoDTO

    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 2) {
                Text(partido.equipoLocal)
                    .font(.subheadline)
                    .fontWeight(partido.estado == PartidoDTO.ESTADO_EN_CURSO ? .bold : .regular)
                Text(partido.equipoVisitante)
                    .font(.subheadline)
                    .fontWeight(partido.estado == PartidoDTO.ESTADO_EN_CURSO ? .bold : .regular)
            }

            Spacer()

            VStack(alignment: .trailing, spacing: 2) {
                if partido.estado == PartidoDTO.ESTADO_EN_CURSO {
                    Text("\(partido.golesLocalReal ?? 0) - \(partido.golesVisitanteReal ?? 0)")
                        .font(.headline)
                        .foregroundColor(.red)
                    if let mins = partido.minutosJugados {
                        Text("\(mins)'")
                            .font(.caption2)
                            .foregroundColor(.red)
                    }
                } else if partido.estado == PartidoDTO.ESTADO_FINALIZADO {
                    Text("\(partido.golesLocalReal ?? 0) - \(partido.golesVisitanteReal ?? 0)")
                        .font(.headline)
                        .foregroundColor(.primary)
                    Text("FINALIZADO")
                        .font(.caption2)
                        .foregroundColor(.gray)
                } else {
                    Text("vs")
                        .font(.headline)
                        .foregroundColor(.orange)
                    if let mins = partido.minutosParaInicio, mins > 0 {
                        Text("Comienza en \(mins) min")
                            .font(.caption2)
                            .foregroundColor(.orange)
                    } else {
                        Text("⏳ Por comenzar")
                            .font(.caption2)
                            .foregroundColor(.orange)
                    }
                }
            }
        }
        .padding(.vertical, 4)
    }
}

// MARK: - Quiniela Card

struct QuinielaCard: View {
    let quiniela: QuinielaResumenDTO

    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                Text(quiniela.nombre)
                    .font(.headline)
                Text("Código: \(quiniela.codigoInvitacion)")
                    .font(.caption)
                    .foregroundColor(.gray)
            }
            Spacer()
            Image(systemName: "chevron.right")
                .foregroundColor(.gray)
        }
        .padding()
        .background(Color.white)
        .cornerRadius(12)
        .shadow(color: .black.opacity(0.05), radius: 2)
    }
}

struct EmptyQuinielasCard: View {
    var body: some View {
        VStack(spacing: 8) {
            Image(systemName: "plus.circle")
                .font(.largeTitle)
                .foregroundColor(Color(hex: "1E88E5"))
            Text("No tienes quinielas")
                .font(.headline)
            Text("Crea o únete a una para comenzar")
                .font(.caption)
                .foregroundColor(.gray)
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(Color.white)
        .cornerRadius(12)
    }
}
