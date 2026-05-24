import SwiftUI
import LocalAuthentication

struct LoginView: View {
    @EnvironmentObject var authManager: AuthManager
    @State private var email: String = ""
    @State private var password: String = ""
    @State private var showError: Bool = false
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 20) {
                    Image(systemName: "sportscourt.fill")
                        .font(.system(size: 80))
                        .foregroundColor(Color(hex: "1E88E5"))
                        .padding(.top, 40)
                    
                    Text("Quiniela")
                        .font(.largeTitle)
                        .fontWeight(.bold)
                        .foregroundColor(Color(hex: "1E88E5"))
                    
                    VStack(spacing: 16) {
                        TextField("Correo electrónico", text: $email)
                            .textFieldStyle(RoundedBorderTextFieldStyle())
                            .textContentType(.emailAddress)
                            .autocapitalization(.none)
                            .keyboardType(.emailAddress)
                        
                        SecureField("Contraseña", text: $password)
                            .textFieldStyle(RoundedBorderTextFieldStyle())
                            .textContentType(.password)
                    }
                    .padding(.horizontal)
                    
                    Button(action: login) {
                        Text("Iniciar Sesión")
                            .font(.headline)
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(Color(hex: "1E88E5"))
                            .cornerRadius(12)
                    }
                    .disabled(email.isEmpty || password.isEmpty)
                    .padding(.horizontal)
                    
                    if canUseBiometrics {
                        Button(action: authenticateWithBiometrics) {
                            HStack {
                                Image(systemName: "faceid")
                                Text("Iniciar con Face ID / Huella")
                            }
                            .font(.subheadline)
                            .foregroundColor(Color(hex: "1E88E5"))
                            .padding(.top, 8)
                        }
                    }
                    
                    NavigationLink(destination: RegisterView()) {
                        Text("¿No tienes cuenta? Regístrate")
                            .foregroundColor(Color(hex: "757575"))
                            .padding(.top, 20)
                    }
                }
                .padding(.bottom, 40)
            }
            .alert("Error", isPresented: $showError) {
                Button("OK", role: .cancel) {}
            } message: {
                Text(authManager.errorMessage ?? "Error desconocido")
            }
            .overlay {
                if authManager.isLoading {
                    ProgressView()
                        .scaleEffect(1.5)
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                        .background(Color.black.opacity(0.3))
                }
            }
        }
    }
    
    private var canUseBiometrics: Bool {
        let context = LAContext()
        return context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: nil)
    }
    
    private func login() {
        Task {
            await authManager.login(email: email, password: password)
            if authManager.errorMessage != nil {
                showError = true
            }
        }
    }
    
    private func authenticateWithBiometrics() {
        Task {
            _ = await authManager.authenticateWithBiometrics()
        }
    }
}