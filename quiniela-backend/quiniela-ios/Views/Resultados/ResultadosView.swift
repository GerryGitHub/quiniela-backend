import SwiftUI

struct ResultadosView: View {
    @StateObject private var viewModel = ResultadosViewModel()
    
    var body: some View {
        NavigationView {
            List {
                if viewModel.partidos.isEmpty {
                    VStack {
                        Spacer()
                        Text("No hay resultados disponibles")
                            .foregroundColor(.gray)
                        Spacer()
                    }
                } else {
                    ForEach(viewModel.partidos) { partido in
                        ResultadoRow(partido: partido)
                    }
                }
            }
            .navigationTitle("Resultados")
            .onAppear {
                viewModel.loadResultados()
            }
            .overlay {
                if viewModel.isLoading {
                    ProgressView()
                }
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
            if let fecha = partido.fecha {
                Text(fecha)
                    .font(.caption)
                    .foregroundColor(.gray)
            }
        }
        .padding(.vertical, 4)
    }
}