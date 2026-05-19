import SwiftUI

struct QuinielaDetalleView: View {
    let quiniela: QuinielaResumen
    @StateObject private var viewModel = QuinielaDetalleViewModel()
    
    var body: some View {
        List {
            Section(header: Text("Partidos")) {
                if viewModel.partidos.isEmpty {
                    Text("No hay partidos disponibles")
                        .foregroundColor(.gray)
                } else {
                    ForEach(viewModel.partidos) { partido in
                        PartidoCard(partido: partido)
                    }
                }
            }
            
            Section(header: Text("Participantes")) {
                if viewModel.participantes.isEmpty {
                    Text("No hay participantes")
                        .foregroundColor(.gray)
                } else {
                    ForEach(viewModel.participantes) { participante in
                        ParticipanteRow(participante: participante)
                    }
                }
            }
        }
        .navigationTitle(quiniela.nombre)
        .onAppear {
            viewModel.loadQuinielaDetail(id: quiniela.id)
        }
    }
}

struct PartidoCard: View {
    let partido: PartidoDTO
    
    var body: some View {
        HStack {
            VStack(alignment: .leading) {
                Text(partido.equipoLocal)
                    .font(.subheadline)
                Text(partido.equipoVisitante)
                    .font(.subheadline)
            }
            Spacer()
            if let local = partido.golesLocalReal, let visitante = partido.golesVisitanteReal {
                Text("\(local) - \(visitante)")
                    .font(.headline)
            } else {
                Text("Por jugar")
                    .font(.caption)
                    .foregroundColor(.gray)
            }
        }
    }
}

struct ParticipanteRow: View {
    let participante: Participante
    
    var body: some View {
        HStack {
            VStack(alignment: .leading) {
                Text(participante.nombre)
                    .font(.subheadline)
                Text("Puntos: \(participante.puntos)")
                    .font(.caption)
                    .foregroundColor(.gray)
            }
            Spacer()
            Text("#\(participante.posicion)")
                .font(.headline)
                .foregroundColor(Color(hex: "1E88E5"))
        }
    }
}