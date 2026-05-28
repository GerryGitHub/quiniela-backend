import Foundation

// MARK: - Auth

struct LoginRequest: Codable {
    let email: String
    let password: String
}

struct RegisterRequest: Codable {
    let nombre: String
    let email: String
    let password: String
}

struct VerifyRegistrationOtpRequest: Codable {
    let email: String
    let code: String
}

struct ResendVerificationRequest: Codable {
    let email: String
}

struct ForgotPasswordRequest: Codable {
    let email: String
}

struct ResetPasswordRequest: Codable {
    let email: String
    let code: String
    let newPassword: String
}

struct MessageResponse: Codable {
    let message: String
}

struct AuthResponse: Codable {
    let accessToken: String
    let refreshToken: String?
    let tipo: String?
    let usuario: UsuarioDTO
}

struct RefreshTokenRequest: Codable {
    let refreshToken: String
}

struct RefreshTokenResponse: Codable {
    let accessToken: String
    let refreshToken: String?
}

// MARK: - User

struct UsuarioDTO: Codable, Identifiable {
    let id: Int
    let nombre: String
    let email: String
    let rol: String?
    let puntosTotales: Int?
}

struct UsuarioPerfilDTO: Codable, Identifiable {
    let id: Int
    let nombre: String
    let email: String
    let rol: String?
    let puntosTotalesGlobales: Int
    let quinielas: [QuinielaResumenDTO]
}

// MARK: - Quinielas

struct QuinielaResumenDTO: Codable, Identifiable {
    let id: Int
    let nombre: String
    let codigoInvitacion: String
    let puntosTotales: Int?
}

struct QuinielaDTO: Codable, Identifiable {
    let id: Int
    let nombre: String
    let codigoInvitacion: String
    let administrador: UsuarioDTO
    let participantes: [UsuarioDTO]?
    let esPublica: Bool?
}

struct QuinielaDetalleDTO: Codable, Identifiable {
    let id: Int
    let nombre: String
    let codigoInvitacion: String
    let administrador: UsuarioDTO
    let participantes: [UsuarioDTO]?
    let partidos: [PartidoDTO]?
}

struct CrearQuinielaRequest: Codable {
    let nombre: String
    let codigoInvitacion: String
}

struct UnirseQuinielaRequest: Codable {
    let codigoInvitacion: String
}

// MARK: - Leaderboard

struct LeaderboardEntryDTO: Codable, Identifiable {
    let posicion: Int
    let usuario: UsuarioDTO
    let puntosTotales: Int
    let aciertos: Int

    var id: Int { posicion }
}

// MARK: - Partidos

struct PartidoDTO: Codable, Identifiable {
    let id: Int
    let equipoLocal: String
    let equipoVisitante: String
    let fechaHora: String
    let grupo: String?
    let grupoId: Int?
    let equipoLocalId: Int?
    let equipoVisitanteId: Int?
    let golesLocalReal: Int?
    let golesVisitanteReal: Int?
    let estado: String
    let minutosParaInicio: Int?
    let minutosJugados: Int?

    static let ESTADO_PENDIENTE = "PENDIENTE"
    static let ESTADO_POR_COMENZAR = "POR_COMENZAR"
    static let ESTADO_EN_CURSO = "EN_CURSO"
    static let ESTADO_FINALIZADO = "FINALIZADO"
}

struct ActualizarPartidoRequest: Codable {
    let golesLocalReal: Int?
    let golesVisitanteReal: Int?
    let estado: String
}

// MARK: - Pronosticos

struct PronosticoDTO: Codable, Identifiable {
    let id: Int
    let usuario: UsuarioDTO
    let partido: PartidoDTO
    let golesLocalPredicho: Int
    let golesVisitantePredicho: Int
    let puntosObtenidos: Int
}

struct PronosticoItemRequest: Codable {
    let idPartido: Int
    let golesLocalPredicho: Int
    let golesVisitantePredicho: Int
}

struct CrearPronosticosBatchRequest: Codable {
    let idQuiniela: Int
    let pronosticos: [PronosticoItemRequest]
}

struct CrearPronosticosBatchResponse: Codable {
    let pronosticosGuardados: Int
    let pronosticos: [PronosticoDTO]?
}

struct MisPronosticosDTO: Codable {
    let pronosticos: [PronosticoDTO]
}

// MARK: - Grupos

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

struct TablaGruposDTO: Codable {
    let grupos: [GrupoDTO]
}
