import Foundation
import Combine

class DashboardViewModel: ObservableObject {
    @Published var quinielas: [QuinielaResumen] = []
    @Published var partidosEnVivo: [PartidoDTO] = []
    @Published var isLoading: Bool = false
    @Published var errorMessage: String?
    
    private let authManager = AuthManager()
    
    func loadData() {
        Task {
            await MainActor.run { isLoading = true }
            
            do {
                guard let token = authManager.token else { return }
                
                let user: User = try await APIService.shared.request(
                    endpoint: "/auth/perfil",
                    method: .GET,
                    token: token
                )
                
                let partidosEnVivo: [PartidoDTO] = try await APIService.shared.request(
                    endpoint: "/resultados/en-vivo",
                    method: .GET,
                    token: token
                )
                
                await MainActor.run {
                    self.quinielas = user.quinielas ?? []
                    self.partidosEnVivo = partidosEnVivo.filter { $0.estado == "EN_CURSO" }
                    self.isLoading = false
                }
            } catch {
                await MainActor.run {
                    self.errorMessage = error.localizedDescription
                    self.isLoading = false
                }
            }
        }
    }
}