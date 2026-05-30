import Foundation
import LocalAuthentication

class AuthManager: ObservableObject {
    @Published var isAuthenticated = false
    @Published var currentUser: User?
    @Published var isLoading = false
    @Published var errorMessage: String?

    private let defaults = UserDefaults.standard
    private let tokenKey = "auth_token"
    private let refreshTokenKey = "refresh_token"
    private let userKey = "user_data"

    var token: String? {
        get { defaults.string(forKey: tokenKey) }
        set {
            if let v = newValue { defaults.set(v, forKey: tokenKey) }
            else { defaults.removeObject(forKey: tokenKey) }
        }
    }

    var refreshToken: String? {
        get { defaults.string(forKey: refreshTokenKey) }
        set {
            if let v = newValue { defaults.set(v, forKey: refreshTokenKey) }
            else { defaults.removeObject(forKey: refreshTokenKey) }
        }
    }

    init() {
        if let _ = token {
            isAuthenticated = true
            loadUser()
        }
    }

    func login(email: String, password: String) async {
        await setLoading(true)
        do {
            let body = try JSONEncoder().encode(LoginRequest(email: email, password: password))
            let response: AuthResponse = try await APIService.shared.request(
                endpoint: "auth/login", method: .POST, body: body)
            await handleAuth(response)
            saveCredentials(email: email, password: password)
        } catch let err as APIError {
            if case .httpError(_, let msg) = err {
                await setError(msg)
            } else {
                await setError("Error al iniciar sesión")
            }
        } catch {
            await setError("Error al iniciar sesión")
        }
    }

    func register(nombre: String, email: String, password: String) async -> Bool {
        await setLoading(true)
        do {
            let body = try JSONEncoder().encode(RegisterRequest(nombre: nombre, email: email, password: password))
            let _: MessageResponse = try await APIService.shared.request(
                endpoint: "auth/register", method: .POST, body: body)
            await MainActor.run { isLoading = false; errorMessage = nil }
            return true
        } catch let err as APIError {
            if case .httpError(_, let msg) = err {
                await setError(msg)
            } else {
                await setError("Error al registrar")
            }
            return false
        } catch {
            await setError("Error al registrar")
            return false
        }
    }

    func verifyRegistrationOtp(email: String, code: String) async -> Bool {
        await setLoading(true)
        do {
            let body = try JSONEncoder().encode(VerifyRegistrationOtpRequest(email: email, code: code))
            let _: MessageResponse = try await APIService.shared.request(
                endpoint: "auth/verify-registration-otp", method: .POST, body: body)
            await MainActor.run { isLoading = false; errorMessage = nil }
            return true
        } catch let err as APIError {
            if case .httpError(_, let msg) = err {
                await setError(msg)
            } else {
                await setError("Error al verificar código")
            }
            return false
        } catch {
            await setError("Error al verificar código")
            return false
        }
    }

    func resendVerification(email: String) async -> Bool {
        await setLoading(true)
        do {
            let body = try JSONEncoder().encode(ResendVerificationRequest(email: email))
            let _: MessageResponse = try await APIService.shared.request(
                endpoint: "auth/resend-verification", method: .POST, body: body)
            await MainActor.run { isLoading = false; errorMessage = nil }
            return true
        } catch {
            await setError("Error al reenviar código")
            return false
        }
    }

    func loginAfterVerification(email: String, password: String) async {
        await login(email: email, password: password)
    }

    func logout() {
        token = nil
        refreshToken = nil
        currentUser = nil
        isAuthenticated = false
        defaults.removeObject(forKey: userKey)
        defaults.removeObject(forKey: "saved_email")
        defaults.removeObject(forKey: "saved_password")
    }

    func authenticateWithBiometrics() async -> Bool {
        let context = LAContext()
        var error: NSError?
        guard context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error) else {
            return false
        }
        do {
            let success = try await context.evaluatePolicy(
                .deviceOwnerAuthenticationWithBiometrics,
                localizedReason: "Iniciar sesión en Quiniela")
            if success, let email = defaults.string(forKey: "saved_email"),
               let password = defaults.string(forKey: "saved_password") {
                await login(email: email, password: password)
            }
            return success
        } catch {
            return false
        }
    }

    func loadPerfil() async {
        do {
            let user: UsuarioPerfilDTO = try await APIService.shared.request(
                endpoint: "auth/me", token: token)
            await MainActor.run {
                self.currentUser = User(id: user.id, nombre: user.nombre, email: user.email, quinielas: user.quinielas)
                saveUser(self.currentUser!)
            }
        } catch {}
    }

    // MARK: - Private

    private func handleAuth(_ response: AuthResponse) async {
        await MainActor.run {
            token = response.accessToken
            refreshToken = response.refreshToken
            currentUser = User(id: response.usuario.id, nombre: response.usuario.nombre, email: response.usuario.email, quinielas: nil)
            isAuthenticated = true
            isLoading = false
            errorMessage = nil
            saveUser(currentUser!)
        }
    }

    private func saveUser(_ user: User) {
        if let data = try? JSONEncoder().encode(user) {
            defaults.set(data, forKey: userKey)
        }
    }

    private func loadUser() {
        if let data = defaults.data(forKey: userKey),
           let user = try? JSONDecoder().decode(User.self, from: data) {
            currentUser = user
        }
    }

    private func saveCredentials(email: String, password: String) {
        defaults.set(email, forKey: "saved_email")
        defaults.set(password, forKey: "saved_password")
    }

    @MainActor
    private func setLoading(_ v: Bool) {
        isLoading = v
        if v { errorMessage = nil }
    }

    @MainActor
    private func setError(_ msg: String) {
        errorMessage = msg
        isLoading = false
    }
}

// MARK: - Local User model for persistence

struct User: Codable {
    let id: Int
    let nombre: String
    let email: String
    let quinielas: [QuinielaResumenDTO]?
}
