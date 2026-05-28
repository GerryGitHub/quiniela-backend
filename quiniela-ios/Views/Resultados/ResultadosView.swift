import SwiftUI

struct ResultadosView: View {
    @EnvironmentObject var authManager: AuthManager
    @StateObject private var viewModel = ResultadosViewModel()

    var body: some View {
        NavigationView {
            Group {
                if viewModel.partidos.isEmpty && !viewModel.isLoading {
                    VStack(spacing: 16) {
                        Image(systemName: "sportscourt")
                            .font(.system(size: 48))
                            .foregroundColor(.gray)
                        Text("No hay resultados aún")
                            .font(.headline)
                            .foregroundColor(.gray)
                        Text("Los resultados aparecerán cuando los partidos finalicen")
                            .font(.caption)
                            .foregroundColor(.gray.opacity(0.7))
                            .multilineTextAlignment(.center)
                    }
                    .padding(.horizontal, 40)
                } else {
                    List {
                        ForEach(viewModel.partidos) { partido in
                            ResultadoRow(partido: partido)
                        }
                    }
                }
            }
            .navigationTitle("Resultados")
            .onAppear {
                viewModel.loadResultados(token: authManager.token)
            }
            .overlay {
                if viewModel.isLoading { ProgressView() }
            }
        }
    }
}

struct ResultadoRow: View {
    let partido: PartidoDTO

    var body: some View {
        VStack(spacing: 8) {
            HStack {
                Text(partido.equipoLocal)
                    .font(.subheadline)
                Spacer()
                Text("\(partido.golesLocalReal ?? 0)")
                    .font(.headline)
                    .fontWeight(.bold)
            }
            HStack {
                Text(partido.equipoVisitante)
                    .font(.subheadline)
                Spacer()
                Text("\(partido.golesVisitanteReal ?? 0)")
                    .font(.headline)
                    .fontWeight(.bold)
            }
        }
        .padding(.vertical, 4)
    }
}
