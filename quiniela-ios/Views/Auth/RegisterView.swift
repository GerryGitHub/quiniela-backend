import SwiftUI

struct RegisterView: View {
    @EnvironmentObject var authManager: AuthManager
    @State private var nombre: String = ""
    @State private var email: String = ""
    @State private var password: String = ""
    @State private var confirmPassword: String = ""
    @State private var showError: Bool = false
    @State private var navigateToVerify = false
    @State private var registeredEmail = ""
    @State private var registeredPassword = ""

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 20) {
                    Text("Crear Cuenta")
                        .font(.title)
                        .fontWeight(.bold)
                        .foregroundColor(Color(hex: "1E88E5"))
                        .padding(.top, 20)

                    VStack(spacing: 16) {
                        TextField("Nombre completo", text: $nombre)
                            .textFieldStyle(RoundedBorderTextFieldStyle())
                            .textContentType(.name)
                            .autocapitalization(.words)

                        TextField("Correo electrónico", text: $email)
                            .textFieldStyle(RoundedBorderTextFieldStyle())
                            .textContentType(.emailAddress)
                            .autocapitalization(.none)
                            .keyboardType(.emailAddress)

                        SecureField("Contraseña", text: $password)
                            .textFieldStyle(RoundedBorderTextFieldStyle())
                            .textContentType(.newPassword)

                        SecureField("Confirmar contraseña", text: $confirmPassword)
                            .textFieldStyle(RoundedBorderTextFieldStyle())
                            .textContentType(.newPassword)
                    }
                    .padding(.horizontal)

                    Button(action: register) {
                        Text("Registrarse")
                            .font(.headline)
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(isValid ? Color(hex: "1E88E5") : Color.gray)
                            .cornerRadius(12)
                    }
                    .disabled(!isValid)
                    .padding(.horizontal)

                    NavigationLink(
                        destination: VerifyOtpView(email: registeredEmail, password: registeredPassword)
                            .environmentObject(authManager),
                        isActive: $navigateToVerify
                    ) { EmptyView() }
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
            .navigationBarTitleDisplayMode(.inline)
        }
    }

    private var isValid: Bool {
        !nombre.isEmpty && isValidEmail && password.count >= 6 && password == confirmPassword
    }

    private var isValidEmail: Bool {
        let emailRegex = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}"
        return email.range(of: emailRegex, options: .regularExpression) != nil
    }

    private func register() {
        Task {
            let success = await authManager.register(nombre: nombre, email: email, password: password)
            if success {
                registeredEmail = email
                registeredPassword = password
                navigateToVerify = true
            } else {
                showError = true
            }
        }
    }
}
