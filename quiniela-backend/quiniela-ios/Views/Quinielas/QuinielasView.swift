import SwiftUI

struct QuinielasView: View {
    @StateObject private var viewModel = QuinielasViewModel()
    
    var body: some View {
        NavigationView {
            List {
                Section(header: Text("Mis Quinielas")) {
                    ForEach(viewModel.quinielas) { quiniela in
                        NavigationLink(destination: QuinielaDetalleView(quiniela: quiniela)) {
                            VStack(alignment: .leading) {
                                Text(quiniela.nombre)
                                    .font(.headline)
                                Text("Código: \(quiniela.codigoInvitacion ?? "N/A")")
                                    .font(.caption)
                                    .foregroundColor(.gray)
                            }
                        }
                    }
                }
            }
            .navigationTitle("Quinielas")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Menu {
                        NavigationLink(destination: CreateQuinielaView()) {
                            Label("Crear Quiniela", systemImage: "plus")
                        }
                        NavigationLink(destination: JoinQuinielaView()) {
                            Label("Unirse a Quiniela", systemImage: "person.badge.plus")
                        }
                    } label: {
                        Image(systemName: "plus")
                    }
                }
            }
            .onAppear {
                viewModel.loadQuinielas()
            }
        }
    }
}