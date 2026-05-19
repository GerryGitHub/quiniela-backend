import SwiftUI

// MARK: - QuinielasViewModel

class QuinielasViewModel: ObservableObject {
    @Published var quinielas: [QuinielaResumen] = []
    @Published var isLoading: Bool = false
    @Published var errorMessage: String?
    
    func loadQuinielas() {
        Task {
            await MainActor.run { isLoading = true }
            
            do {
                let token = AuthManager().token ?? ""
                let user: User = try await APIService.shared.request(
                    endpoint: "/auth/perfil",
                    method: .GET,
                    token: token
                )
                
                await MainActor.run {
                    self.quinielas = user.quinielas ?? []
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

class CreateQuinielaViewModel: ObservableObject {
    @Published var isLoading = false
    @Published var isSuccess = false
    @Published var errorMessage: String?
    
    func createQuiniela(nombre: String) async {
        await MainActor.run { isLoading = true }
        
        do {
            struct CreateQuinielaRequest: Codable {
                let nombre: String
            }
            let body = try JSONEncoder().encode(CreateQuinielaRequest(nombre: nombre))
            
            let _: Quiniela = try await APIService.shared.request(
                endpoint: "/quinielas",
                method: .POST,
                body: body,
                token: AuthManager().token
            )
            
            await MainActor.run {
                self.isSuccess = true
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

class JoinQuinielaViewModel: ObservableObject {
    @Published var isLoading = false
    @Published var isSuccess = false
    @Published var errorMessage: String?
    
    func joinQuiniela(codigo: String) async {
        await MainActor.run { isLoading = true }
        
        do {
            struct JoinRequest: Codable {
                let codigoInvitacion: String
            }
            let body = try JSONEncoder().encode(JoinRequest(codigoInvitacion: codigo))
            
            let _: Quiniela = try await APIService.shared.request(
                endpoint: "/quinielas/unirse",
                method: .POST,
                body: body,
                token: AuthManager().token
            )
            
            await MainActor.run {
                self.isSuccess = true
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

class QuinielaDetalleViewModel: ObservableObject {
    @Published var partidos: [PartidoDTO] = []
    @Published var participantes: [Participante] = []
    @Published var isLoading: Bool = false
    @Published var errorMessage: String?
    
    func loadQuinielaDetail(id: Long) {
        Task {
            await MainActor.run { isLoading = true }
            
            do {
                let token = AuthManager().token ?? ""
                let detalle: Quiniela = try await APIService.shared.request(
                    endpoint: "/quinielas/\(id)",
                    method: .GET,
                    token: token
                )
                
                await MainActor.run {
                    self.partidos = detalle.partidos ?? []
                    self.participantes = detalle.participantes ?? []
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

// MARK: - GroupsViewModel

class GroupsViewModel: ObservableObject {
    @Published var grupos: [GrupoFIFA] = []
    @Published var isLoading: Bool = false
    @Published var errorMessage: String?
    
    func loadGroups() {
        Task {
            await MainActor.run { isLoading = true }
            
            do {
                let token = AuthManager().token ?? ""
                let gruposDTO: [GrupoDTO] = try await APIService.shared.request(
                    endpoint: "/grupos",
                    method: .GET,
                    token: token
                )
                
                let gruposFIFA = gruposDTO.map { dto in
                    GrupoFIFA(
                        nombre: dto.nombre,
                        equipos: dto.equipos.map { eq in
                            Equipo(nombre: eq.nombre, posicion: eq.posicion)
                        }
                    )
                }
                
                await MainActor.run {
                    self.grupos = gruposFIFA
                    self.isLoading = false
                }
            } catch {
                let sampleData = self.getSampleGroups()
                await MainActor.run {
                    self.grupos = sampleData
                    self.isLoading = false
                }
            }
        }
    }
    
    private func getSampleGroups() -> [GrupoFIFA] {
        return [
            GrupoFIFA(nombre: "A", equipos: [
                Equipo(nombre: "México", posicion: 1),
                Equipo(nombre: "Sudáfica", posicion: 2),
                Equipo(nombre: "República de Corea", posicion: 3),
                Equipo(nombre: "República Checa", posicion: 4)
            ]),
            GrupoFIFA(nombre: "B", equipos: [
                Equipo(nombre: "Canadá", posicion: 1),
                Equipo(nombre: "Marruecos", posicion: 2),
                Equipo(nombre: "Croacia", posicion: 3),
                Equipo(nombre: "Bélgica", posicion: 4)
            ]),
            GrupoFIFA(nombre: "C", equipos: [
                Equipo(nombre: "Argentina", posicion: 1),
                Equipo(nombre: "Polonia", posicion: 2),
                Equipo(nombre: "Arabia Saudí", posicion: 3),
                Equipo(nombre: "México", posicion: 4)
            ]),
            GrupoFIFA(nombre: "D", equipos: [
                Equipo(nombre: "Francia", posicion: 1),
                Equipo(nombre: "Dinamarca", posicion: 2),
                Equipo(nombre: "Túnez", posicion: 3),
                Equipo(nombre: "Australia", posicion: 4)
            ]),
            GrupoFIFA(nombre: "E", equipos: [
                Equipo(nombre: "España", posicion: 1),
                Equipo(nombre: "Alemania", posicion: 2),
                Equipo(nombre: "Japón", posicion: 3),
                Equipo(nombre: "Costa Rica", posicion: 4)
            ]),
            GrupoFIFA(nombre: "F", equipos: [
                Equipo(nombre: "Brasil", posicion: 1),
                Equipo(nombre: "Suiza", posicion: 2),
                Equipo(nombre: "Camerún", posicion: 3),
                Equipo(nombre: "Serbia", posicion: 4)
            ]),
            GrupoFIFA(nombre: "G", equipos: [
                Equipo(nombre: "Portugal", posicion: 1),
                Equipo(nombre: "Uruguay", posicion: 2),
                Equipo(nombre: "Corea del Sur", posicion: 3),
                Equipo(nombre: "Ghana", posicion: 4)
            ]),
            GrupoFIFA(nombre: "H", equipos: [
                Equipo(nombre: "Inglaterra", posicion: 1),
                Equipo(nombre: "Estados Unidos", posicion: 2),
                Equipo(nombre: "Irán", posicion: 3),
                Equipo(nombre: "Gales", posicion: 4)
            ])
        ]
    }
}

// MARK: - ResultadosViewModel

class ResultadosViewModel: ObservableObject {
    @Published var partidos: [PartidoDTO] = []
    @Published var isLoading: Bool = false
    @Published var errorMessage: String?
    
    func loadResultados() {
        Task {
            await MainActor.run { isLoading = true }
            
            do {
                let token = AuthManager().token ?? ""
                let resultados: [PartidoDTO] = try await APIService.shared.request(
                    endpoint: "/resultados/partidos",
                    method: .GET,
                    token: token
                )
                
                await MainActor.run {
                    self.partidos = resultados.filter { $0.golesLocalReal != nil }
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