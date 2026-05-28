import SwiftUI

struct GroupsView: View {
    @EnvironmentObject var authManager: AuthManager
    @StateObject private var viewModel = GroupsViewModel()

    var body: some View {
        NavigationView {
            Group {
                if viewModel.grupos.isEmpty && !viewModel.isLoading {
                    VStack(spacing: 16) {
                        Image(systemName: "person.3")
                            .font(.system(size: 48))
                            .foregroundColor(.gray)
                        Text("No hay grupos disponibles")
                            .font(.headline)
                            .foregroundColor(.gray)
                        Text("Los grupos del torneo aparecerán aquí")
                            .font(.caption)
                            .foregroundColor(.gray.opacity(0.7))
                    }
                } else {
                    List {
                        ForEach(viewModel.grupos, id: \.nombre) { grupo in
                            Section(header: Text("Grupo \(grupo.nombre)")) {
                                ForEach(grupo.equipos.sorted { ($0.posicion ?? 99) < ($1.posicion ?? 99) }) { equipo in
                                    HStack {
                                        if let pos = equipo.posicion {
                                            Text("\(pos)")
                                                .font(.headline)
                                                .foregroundColor(Color(hex: "1E88E5"))
                                                .frame(width: 30)
                                        }
                                        Text(equipo.nombre)
                                            .font(.subheadline)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            .navigationTitle("Grupos FIFA")
            .onAppear {
                viewModel.loadGroups(token: authManager.token)
            }
            .overlay {
                if viewModel.isLoading { ProgressView() }
            }
        }
    }
}
