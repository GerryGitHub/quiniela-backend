import SwiftUI

struct GroupsView: View {
    @StateObject private var viewModel = GroupsViewModel()
    
    var body: some View {
        NavigationView {
            List {
                ForEach(viewModel.grupos, id: \.nombre) { grupo in
                    Section(header: Text("Grupo \(grupo.nombre)")) {
                        ForEach(grupo.equipos.sorted { $0.posicion < $1.posicion }) { equipo in
                            HStack {
                                Text("\(equipo.posicion)")
                                    .font(.headline)
                                    .foregroundColor(Color(hex: "1E88E5"))
                                    .frame(width: 30)
                                Text(equipo.nombre)
                                    .font(.subheadline)
                            }
                        }
                    }
                }
            }
            .navigationTitle("Grupos FIFA")
            .onAppear {
                viewModel.loadGroups()
            }
            .overlay {
                if viewModel.isLoading {
                    ProgressView()
                }
            }
        }
    }
}