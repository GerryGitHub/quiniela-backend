import SwiftUI

struct CreateQuinielaView: View {
    @EnvironmentObject var authManager: AuthManager
    @Environment(\.dismiss) var dismiss
    @StateObject private var viewModel = CreateQuinielaViewModel()
    @State private var nombre: String = ""

    var body: some View {
        NavigationView {
            Form {
                Section {
                    TextField("Nombre de la Quiniela", text: $nombre)
                }

                Section {
                    Button(action: create) {
                        HStack {
                            Spacer()
                            if viewModel.isLoading {
                                ProgressView()
                            } else {
                                Text("Crear Quiniela")
                                    .fontWeight(.semibold)
                            }
                            Spacer()
                        }
                    }
                    .disabled(nombre.isEmpty || viewModel.isLoading)
                }
            }
            .navigationTitle("Crear Quiniela")
            .navigationBarItems(leading: Button("Cancelar") { dismiss() })
            .alert("Error", isPresented: .constant(viewModel.errorMessage != nil)) {
                Button("OK") { viewModel.errorMessage = nil }
            } message: {
                Text(viewModel.errorMessage ?? "")
            }
            .onChange(of: viewModel.isSuccess) { _, success in
                if success { dismiss() }
            }
        }
    }

    private func create() {
        viewModel.createQuiniela(nombre: nombre, token: authManager.token)
    }
}
