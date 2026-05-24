import SwiftUI

struct JoinQuinielaView: View {
    @Environment(\.dismiss) var dismiss
    @StateObject private var viewModel = JoinQuinielaViewModel()
    @State private var codigo: String = ""
    
    var body: some View {
        NavigationView {
            Form {
                Section {
                    TextField("Código de invitación", text: $codigo)
                        .textInputAutocapitalization(.characters)
                } header: {
                    Text("Código")
                } footer: {
                    Text("Ingresa el código que te compartieron para unirte a una quiniela")
                }
                
                Section {
                    Button(action: joinQuiniela) {
                        HStack {
                            Spacer()
                            if viewModel.isLoading {
                                ProgressView()
                            } else {
                                Text("Unirse")
                                    .fontWeight(.semibold)
                            }
                            Spacer()
                        }
                    }
                    .disabled(codigo.isEmpty || viewModel.isLoading)
                }
            }
            .navigationTitle("Unirse a Quiniela")
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
    
    private func joinQuiniela() {
        Task {
            await viewModel.joinQuiniela(codigo: codigo)
        }
    }
}