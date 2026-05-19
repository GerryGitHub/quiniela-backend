import Foundation
import LocalAuthentication
import SwiftUI

class AuthManager: ObservableObject {
    @Published var isAuthenticated: Bool = false
    @Published var currentUser: User?
    @Published var isLoading: Bool = false
    @Published var errorMessage: String?
    
    private let userDefaults = UserDefaults.standard
    private let tokenKey = "auth_token"
    private let userKey = "user_data"
    
    var token: String? {
        get { userDefaults.string(forKey: tokenKey) }
        set {
            if let value = newValue {
                userDefaults.set(value, forKey: tokenKey)
            } else {
                userDefaults.removeObject(forKey: tokenKey)
            }
        }
    }
    
    init() {
        checkAuthentication()
    }
    
    private func checkAuthentication() {
        if let _ = token {
            isAuthenticated = true
            loadUser()
        }
    }
    
    func login(email: String, password: String) async {
        await MainActor.run { isLoading = true }
        
        do {
            let loginData = try JSONEncoder().encode(LoginRequest(email: email, password: password))
            let response: AuthResponse = try await APIService.shared.request(
                endpoint: "/auth/login",
                method: .POST,
                body: loginData
            )
            
            await MainActor.run {
                self.token = response.token
                self.currentUser = response.user
                self.isAuthenticated = true
                self.isLoading = false
                self.saveUser(response.user)
                self.saveCredentials(email: email, password: password)
            }
        } catch {
            await MainActor.run {
                self.errorMessage = "Error al iniciar sesión: \(error.localizedDescription)"
                self.isLoading = false
            }
        }
    }
    
    func register(nombre: String, email: String, password: String) async {
        await MainActor.run { isLoading = true }
        
        do {
            let registerData = try JSONEncoder().encode(RegisterRequest(nombre: nombre, email: email, password: password))
            let response: AuthResponse = try await APIService.shared.request(
                endpoint: "/auth/register",
                method: .POST,
                body: registerData
            )
            
            await MainActor.run {
                self.token = response.token
                self.currentUser = response.user
                self.isAuthenticated = true
                self.isLoading = false
                self.saveUser(response.user)
            }
        } catch {
            await MainActor.run {
                self.errorMessage = "Error al registrar: \(error.localizedDescription)"
                self.isLoading = false
            }
        }
    }
    
    func logout() {
        token = nil
        currentUser = nil
        isAuthenticated = false
        userDefaults.removeObject(forKey: userKey)
        userDefaults.removeObject(forKey: "saved_email")
        userDefaults.removeObject(forKey: "saved_password")
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
                localizedReason: "Iniciar sesión en Quiniela"
            )
            
            if success {
                await MainActor.run {
                    if let email = userDefaults.string(forKey: "saved_email"),
                       let password = userDefaults.string(forKey: "saved_password") {
                        Task {
                            await self.login(email: email, password: password)
                        }
                    }
                }
            }
            return success
        } catch {
            return false
        }
    }
    
    private func saveUser(_ user: User) {
        if let data = try? JSONEncoder().encode(user) {
            userDefaults.set(data, forKey: userKey)
        }
    }
    
    private func loadUser() {
        if let data = userDefaults.data(forKey: userKey),
           let user = try? JSONDecoder().decode(User.self, from: data) {
            currentUser = user
        }
    }
    
    private func saveCredentials(email: String, password: String) {
        userDefaults.set(email, forKey: "saved_email")
        userDefaults.set(password, forKey: "saved_password")
    }
}