import SwiftUI

struct QuinielasView: View {
    @EnvironmentObject var authManager: AuthManager
    @StateObject private var viewModel = QuinielasViewModel()

    var body: some View {
        NavigationView {
            Group {
                if viewModel.quinielas.isEmpty && !viewModel.isLoading {
                    VStack(spacing: 16) {
                        Image(systemName: "list.bullet.rectangle")
                            .font(.system(size: 48))
                            .foregroundColor(.gray)
                        Text("No tienes quinielas aún")
                            .font(.headline)
                        Text("Crea una o únete para empezar")
                            .font(.caption)
                            .foregroundColor(.gray)
                    }
                } else {
                    List {
                        ForEach(viewModel.quinielas) { quiniela in
                            NavigationLink(destination: QuinielaDetalleView(
                                quinielaId: quiniela.id,
                                quinielaNombre: quiniela.nombre
                            ).environmentObject(authManager)) {
                                VStack(alignment: .leading, spacing: 4) {
                                    Text(quiniela.nombre)
                                        .font(.headline)
                                    Text("Código: \(quiniela.codigoInvitacion)")
                                        .font(.caption)
                                        .foregroundColor(.gray)
                                }
                            }
                        }
                    }
                }
            }
            .navigationTitle("Quinielas")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Menu {
                        NavigationLink(destination: CreateQuinielaView().environmentObject(authManager)) {
                            Label("Crear Quiniela", systemImage: "plus")
                        }
                        NavigationLink(destination: JoinQuinielaView().environmentObject(authManager)) {
                            Label("Unirse a Quiniela", systemImage: "person.badge.plus")
                        }
                    } label: {
                        Image(systemName: "plus")
                    }
                }
            }
            .onAppear {
                viewModel.loadQuinielas(token: authManager.token)
            }
            .overlay {
                if viewModel.isLoading { ProgressView() }
            }
        }
    }
}
