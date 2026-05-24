import Foundation

struct LoginRequest: Codable {
    let email: String
    let password: String
}

struct RegisterRequest: Codable {
    let nombre: String
    let email: String
    let password: String
}

struct AuthResponse: Codable {
    let token: String
    let user: User
}

struct User: Codable, Identifiable {
    let id: Long
    let nombre: String
    let email: String
    let quinielas: [QuinielaResumen]?
}

struct QuinielaResumen: Codable, Identifiable {
    let id: Long
    let nombre: String
    let codigoInvitacion: String?
}

struct Quiniela: Codable, Identifiable {
    let id: Long
    let nombre: String
    let codigoInvitacion: String
    let creadorId: Long
    let participantes: [Participante]?
    let partidos: [PartidoDTO]?
}

struct Participante: Codable, Identifiable {
    let id: Long
    let usuario: User
    let puntosTotales: Int
}

struct PartidoDTO: Codable, Identifiable {
    let id: Long
    let equipoLocal: String
    let equipoVisitante: String
    let fechaHora: String
    let grupo: String?
    let grupoId: Long?
    let equipoLocalId: Long?
    let equipoVisitanteId: Long?
    let golesLocalReal: Int?
    let golesVisitanteReal: Int?
    let estado: String
}

struct PronosticoItemRequest: Codable {
    let idPartido: Long
    let golesLocalPredicho: Int
    let golesVisitantePredicho: Int
}

struct CrearPronosticosRequest: Codable {
    let idQuiniela: Long
    let pronosticos: [PronosticoItemRequest]
}

struct GrupoFIFA: Identifiable {
    let id = UUID()
    let nombre: String
    let equipos: [Equipo]
}

struct Equipo: Identifiable {
    let id = UUID()
    let nombre: String
    let posicion: Int?
}

struct GrupoDTO: Codable {
    let nombre: String
    let equipos: [SeleccionDTO]
}

struct SeleccionDTO: Codable {
    let nombre: String
    let posicion: Int?
}