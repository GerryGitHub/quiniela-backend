import Foundation

struct APIConfig {
    private static let baseURLKey = "api_base_url"
    
    static var baseURL: String {
        get {
            if let custom = UserDefaults.standard.string(forKey: baseURLKey) {
                return custom
            }
            #if DEBUG
            return "http://10.0.2.2:8080/api"
            #else
            return "http://163.192.151.218:8080/api"
            #endif
        }
        set {
            UserDefaults.standard.set(newValue, forKey: baseURLKey)
        }
    }
    
    static func useLocal() {
        baseURL = "http://10.0.2.2:8080/api"
    }
    
    static func useProduction() {
        baseURL = "http://163.192.151.218:8080/api"
    }
}

enum HTTPMethod: String {
    case GET
    case POST
    case PUT
    case PATCH
    case DELETE
}

class APIService {
    static let shared = APIService()
    private let session = URLSession.shared
    
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
        
        guard (200...299).contains(httpResponse.statusCode) else {
            throw APIError.httpError(statusCode: httpResponse.statusCode)
        }
        
        let decoder = JSONDecoder()
        decoder.dateDecodingStrategy = .iso8601
        
        return try decoder.decode(T.self, from: data)
    }
}

enum APIError: Error {
    case invalidURL
    case invalidResponse
    case httpError(statusCode: Int)
    case decodingError
}