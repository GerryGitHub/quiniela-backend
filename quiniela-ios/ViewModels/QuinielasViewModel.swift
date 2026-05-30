import SwiftUI

// MARK: - QuinielasViewModel

class QuinielasViewModel: ObservableObject {
    @Published var quinielas: [QuinielaResumenDTO] = []
    @Published var isLoading = false
    @Published var errorMessage: String?

    func loadQuinielas(token: String?) {
        Task {
            await MainActor.run { isLoading = true }
            do {
                guard let token = token else { return }
                let user: UsuarioPerfilDTO = try await APIService.shared.request(
                    endpoint: "auth/me", token: token)
                await MainActor.run {
                    self.quinielas = user.quinielas
                    self.isLoading = false
                }
            } catch {
                await MainActor.run {
                    self.errorMessage = "Error al cargar quinielas"
                    self.isLoading = false
                }
            }
        }
    }
}

// MARK: - CreateQuinielaViewModel

class CreateQuinielaViewModel: ObservableObject {
    @Published var isLoading = false
    @Published var isSuccess = false
    @Published var errorMessage: String?

    func createQuiniela(nombre: String, token: String?) {
        Task {
            await MainActor.run { isLoading = true }
            do {
                let body = try JSONEncoder().encode(CrearQuinielaRequest(nombre: nombre, codigoInvitacion: ""))
                let _: QuinielaDTO = try await APIService.shared.request(
                    endpoint: "quinielas", method: .POST, body: body, token: token)
                await MainActor.run { isSuccess = true; isLoading = false }
            } catch let err as APIError {
                if case .httpError(_, let msg) = err {
                    await MainActor.run { errorMessage = msg; isLoading = false }
                } else {
                    await MainActor.run { errorMessage = "Error al crear"; isLoading = false }
                }
            } catch {
                await MainActor.run { errorMessage = "Error al crear"; isLoading = false }
            }
        }
    }
}

// MARK: - JoinQuinielaViewModel

class JoinQuinielaViewModel: ObservableObject {
    @Published var isLoading = false
    @Published var isSuccess = false
    @Published var errorMessage: String?

    func joinQuiniela(codigo: String, token: String?) {
        Task {
            await MainActor.run { isLoading = true }
            do {
                let body = try JSONEncoder().encode(UnirseQuinielaRequest(codigoInvitacion: codigo))
                let _: QuinielaDTO = try await APIService.shared.request(
                    endpoint: "quinielas/join", method: .POST, body: body, token: token)
                await MainActor.run { isSuccess = true; isLoading = false }
            } catch let err as APIError {
                if case .httpError(_, let msg) = err {
                    await MainActor.run { errorMessage = msg; isLoading = false }
                } else {
                    await MainActor.run { errorMessage = "Error al unirse"; isLoading = false }
                }
            } catch {
                await MainActor.run { errorMessage = "Error al unirse"; isLoading = false }
            }
        }
    }
}

// MARK: - QuinielaDetalleViewModel

@MainActor
class QuinielaDetalleViewModel: ObservableObject {
    @Published var partidos: [PartidoDTO] = []
    @Published var participantes: [LeaderboardEntryDTO] = []
    @Published var isLoading = false
    @Published var errorMessage: String?
    @Published var saving = false
    @Published var savedMessage: String?

    var misPronosticos: [Int: (local: Int, visitante: Int)] = [:]
    var dirtyIds: Set<Int> = []

    func loadDetail(id: Int, token: String?) {
        isLoading = true
        Task {
            do {
                guard let token = token else { return }
                async let detalleFetch: QuinielaDetalleDTO = APIService.shared.request(
                    endpoint: "quinielas/\(id)", token: token)
                async let pronosFetch: MisPronosticosDTO = APIService.shared.request(
                    endpoint: "pronosticos/quiniela/\(id)", token: token)

                let (detalle, pronos) = try await (detalleFetch, pronosFetch)

                self.partidos = detalle.partidos ?? []

                for p in pronos.pronosticos {
                    misPronosticos[p.partido.id] = (p.golesLocalPredicho, p.golesVisitantePredicho)
                }
                isLoading = false
            } catch {
                errorMessage = "Error al cargar detalle"
                isLoading = false
            }
        }
    }

    func loadLeaderboard(id: Int, token: String?) {
        Task {
            do {
                guard let token = token else { return }
                let data: [LeaderboardEntryDTO] = try await APIService.shared.request(
                    endpoint: "quinielas/\(id)/leaderboard", token: token)
                self.participantes = data
            } catch {}
        }
    }

    func savePronosticos(quinielaId: Int, token: String?) {
        guard !dirtyIds.isEmpty else { return }
        saving = true
        let items = dirtyIds.compactMap { partidoId -> PronosticoItemRequest? in
            guard let p = misPronosticos[partidoId] else { return nil }
            return PronosticoItemRequest(idPartido: partidoId, golesLocalPredicho: p.local, golesVisitantePredicho: p.visitante)
        }
        guard !items.isEmpty else { saving = false; return }

        Task {
            do {
                let body = try JSONEncoder().encode(CrearPronosticosBatchRequest(idQuiniela: quinielaId, pronosticos: items))
                let response: CrearPronosticosBatchResponse = try await APIService.shared.request(
                    endpoint: "pronosticos/batch", method: .POST, body: body, token: token)
                dirtyIds.removeAll()
                savedMessage = "Pronósticos guardados: \(response.pronosticosGuardados)"
                saving = false
            } catch {
                errorMessage = "Error al guardar"
                saving = false
            }
        }
    }
}

// MARK: - GroupsViewModel

class GroupsViewModel: ObservableObject {
    @Published var grupos: [GrupoFIFA] = []
    @Published var isLoading = false
    @Published var errorMessage: String?

    func loadGroups(token: String?) {
        Task {
            await MainActor.run { isLoading = true }
            do {
                let gruposDTO: TablaGruposDTO = try await APIService.shared.request(
                    endpoint: "api/grupos", token: token)
                let mapped = gruposDTO.grupos.map { dto in
                    GrupoFIFA(nombre: dto.nombre, equipos: dto.equipos.map { eq in
                        Equipo(nombre: eq.nombre, posicion: eq.posicion)
                    })
                }
                await MainActor.run { self.grupos = mapped; self.isLoading = false }
            } catch {
                await MainActor.run {
                    self.grupos = self.sampleGroups()
                    self.isLoading = false
                }
            }
        }
    }

    private func sampleGroups() -> [GrupoFIFA] {
        [
            GrupoFIFA(nombre: "A", equipos: [Equipo(nombre: "México", posicion: 1), Equipo(nombre: "Sudáfrica", posicion: 2), Equipo(nombre: "República de Corea", posicion: 3), Equipo(nombre: "República Checa", posicion: 4)]),
            GrupoFIFA(nombre: "B", equipos: [Equipo(nombre: "Canadá", posicion: 1), Equipo(nombre: "Marruecos", posicion: 2), Equipo(nombre: "Croacia", posicion: 3), Equipo(nombre: "Bélgica", posicion: 4)]),
            GrupoFIFA(nombre: "C", equipos: [Equipo(nombre: "Argentina", posicion: 1), Equipo(nombre: "Polonia", posicion: 2), Equipo(nombre: "Arabia Saudí", posicion: 3), Equipo(nombre: "México", posicion: 4)]),
            GrupoFIFA(nombre: "D", equipos: [Equipo(nombre: "Francia", posicion: 1), Equipo(nombre: "Dinamarca", posicion: 2), Equipo(nombre: "Túnez", posicion: 3), Equipo(nombre: "Australia", posicion: 4)]),
            GrupoFIFA(nombre: "E", equipos: [Equipo(nombre: "España", posicion: 1), Equipo(nombre: "Alemania", posicion: 2), Equipo(nombre: "Japón", posicion: 3), Equipo(nombre: "Costa Rica", posicion: 4)]),
            GrupoFIFA(nombre: "F", equipos: [Equipo(nombre: "Brasil", posicion: 1), Equipo(nombre: "Suiza", posicion: 2), Equipo(nombre: "Camerún", posicion: 3), Equipo(nombre: "Serbia", posicion: 4)]),
            GrupoFIFA(nombre: "G", equipos: [Equipo(nombre: "Portugal", posicion: 1), Equipo(nombre: "Uruguay", posicion: 2), Equipo(nombre: "Corea del Sur", posicion: 3), Equipo(nombre: "Ghana", posicion: 4)]),
            GrupoFIFA(nombre: "H", equipos: [Equipo(nombre: "Inglaterra", posicion: 1), Equipo(nombre: "Estados Unidos", posicion: 2), Equipo(nombre: "Irán", posicion: 3), Equipo(nombre: "Gales", posicion: 4)])
        ]
    }
}

// MARK: - ResultadosViewModel

class ResultadosViewModel: ObservableObject {
    @Published var partidos: [PartidoDTO] = []
    @Published var isLoading = false
    @Published var errorMessage: String?

    func loadResultados(token: String?) {
        Task {
            await MainActor.run { isLoading = true }
            do {
                guard let token = token else { return }
                let resultados: [PartidoDTO] = try await APIService.shared.request(
                    endpoint: "api/resultados/partidos", token: token)
                await MainActor.run {
                    self.partidos = resultados.filter { $0.golesLocalReal != nil }
                    self.isLoading = false
                }
            } catch {
                await MainActor.run { self.errorMessage = "Error al cargar resultados"; self.isLoading = false }
            }
        }
    }
}
