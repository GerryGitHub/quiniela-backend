import Foundation

struct APIConfig {
    private static let baseURLKey = "api_base_url"

    static var baseURL: String {
        get {
            if let custom = UserDefaults.standard.string(forKey: baseURLKey) {
                return custom
            }
            return "https://api.gjapps.com/"
        }
        set {
            UserDefaults.standard.set(newValue, forKey: baseURLKey)
        }
    }
}

enum HTTPMethod: String {
    case GET, POST, PUT, PATCH, DELETE
}

class APIService {
    static let shared = APIService()
    private let session = URLSession.shared
    private let decoder: JSONDecoder = {
        let d = JSONDecoder()
        return d
    }()

    private init() {}

    func request<T: Decodable>(
        endpoint: String,
        method: HTTPMethod = .GET,
        body: Data? = nil,
        token: String? = nil
    ) async throws -> T {
        guard let url = URL(string: "\(APIConfig.baseURL)\(endpoint)") else {
            throw APIError.invalidURL
        }

        var request = URLRequest(url: url)
        request.httpMethod = method.rawValue
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")

        if let token = token {
            request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }

        if let body = body {
            request.httpBody = body
        }

        let (data, response) = try await session.data(for: request)

        guard let httpResponse = response as? HTTPURLResponse else {
            throw APIError.invalidResponse
        }

        if !(200...299).contains(httpResponse.statusCode) {
            let message = parseErrorMessage(data: data, statusCode: httpResponse.statusCode)
            throw APIError.httpError(statusCode: httpResponse.statusCode, message: message)
        }

        return try decoder.decode(T.self, from: data)
    }

    private func parseErrorMessage(data: Data, statusCode: Int) -> String {
        if let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
           let error = json["error"] as? String {
            return improveMessage(error)
        }
        return defaultMessage(for: statusCode)
    }

    private func improveMessage(_ message: String) -> String {
        if message.contains("verificar tu correo") {
            return "Debes validar tu correo antes de iniciar sesión."
        }
        if message.contains("ya está registrado") {
            return "Ya existe una cuenta con ese correo."
        }
        if message.contains("Email inválido") {
            return "Ingresa un correo electrónico válido."
        }
        if message.contains("La contraseña debe tener") {
            return "La contraseña debe tener al menos 6 caracteres."
        }
        if message.contains("nombre es requerido") || message.contains("email es requerido") || message.contains("contraseña es requerida") {
            return "Completa todos los campos obligatorios."
        }
        if message.contains("Código inválido") || message.contains("Token inválido") {
            return "El código ingresado no es válido."
        }
        if message.contains("código ya fue utilizado") || message.contains("Token ya fue utilizado") {
            return "El código ya fue usado. Solicita uno nuevo."
        }
        if message.contains("código ha expirado") || message.contains("Token ha expirado") {
            return "El código expiró. Solicita uno nuevo."
        }
        if message.contains("Usuario no encontrado") || message.contains("Bad credentials") || message.contains("Unauthorized") {
            return "Usuario o contraseña incorrectos."
        }
        return message
    }

    private func defaultMessage(for code: Int) -> String {
        switch code {
        case 400: return "Solicitud inválida"
        case 401: return "Usuario o contraseña incorrectos."
        case 403: return "No autorizado"
        case 404: return "Recurso no encontrado"
        case 409: return "El recurso ya existe"
        case 500...599: return "El servidor presentó un problema. Intenta más tarde."
        default: return "Ocurrió un error inesperado."
        }
    }
}

enum APIError: Error {
    case invalidURL
    case invalidResponse
    case httpError(statusCode: Int, message: String)
}
