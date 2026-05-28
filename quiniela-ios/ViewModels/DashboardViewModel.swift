import Foundation

class DashboardViewModel: ObservableObject {
    @Published var quinielas: [QuinielaResumenDTO] = []
    @Published var partidosEnVivo: [PartidoDTO] = []
    @Published var isLoading = false
    @Published var errorMessage: String?

    func loadData(token: String?) {
        Task {
            await MainActor.run { isLoading = true }
            do {
                guard let token = token else { return }
                let user: UsuarioPerfilDTO = try await APIService.shared.request(
                    endpoint: "auth/me", token: token)
                let enVivo: [PartidoDTO] = try await APIService.shared.request(
                    endpoint: "api/resultados/en-vivo", token: token)

                await MainActor.run {
                    self.quinielas = user.quinielas
                    self.partidosEnVivo = enVivo
                    self.isLoading = false
                }
            } catch {
                await MainActor.run {
                    self.errorMessage = "Error al cargar datos"
                    self.isLoading = false
                }
            }
        }
    }
}
