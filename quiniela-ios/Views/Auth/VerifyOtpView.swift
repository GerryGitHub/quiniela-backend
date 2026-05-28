import SwiftUI

struct VerifyOtpView: View {
    let email: String
    let password: String
    @EnvironmentObject var authManager: AuthManager
    @Environment(\.dismiss) var dismiss
    @State private var code: String = ""
    @State private var showError = false
    @State private var isResending = false
    @State private var showSuccess = false
    @State private var successMessage = ""

    var body: some View {
        VStack(spacing: 24) {
            Spacer()

            Image(systemName: "envelope.badge.shield.half.filled")
                .font(.system(size: 60))
                .foregroundColor(Color(hex: "1E88E5"))

            Text("Verifica tu correo")
                .font(.title2)
                .fontWeight(.bold)

            Text("Hemos enviado un código de 6 dígitos a\n\(email)")
                .font(.subheadline)
                .foregroundColor(.gray)
                .multilineTextAlignment(.center)

            TextField("Código de verificación", text: $code)
                .textFieldStyle(RoundedBorderTextFieldStyle())
                .keyboardType(.numberPad)
                .multilineTextAlignment(.center)
                .font(.title2)
                .onChange(of: code) { _, newValue in
                    if newValue.count > 6 { code = String(newValue.prefix(6)) }
                }
                .padding(.horizontal, 40)

            Button(action: verifyCode) {
                Text("Verificar")
                    .font(.headline)
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(code.count == 6 ? Color(hex: "1E88E5") : Color.gray)
                    .cornerRadius(12)
            }
            .disabled(code.count != 6)
            .padding(.horizontal, 40)

            Button(action: resendCode) {
                if isResending {
                    ProgressView()
                } else {
                    Text("Reenviar código")
                        .font(.subheadline)
                        .foregroundColor(Color(hex: "1E88E5"))
                }
            }
            .disabled(isResending)

            Spacer()
        }
        .padding()
        .alert("Error", isPresented: $showError) {
            Button("OK", role: .cancel) {}
        } message: {
            Text(authManager.errorMessage ?? "Error desconocido")
        }
        .alert("Verificado", isPresented: $showSuccess) {
            Button("Iniciar sesión") {
                Task { await authManager.loginAfterVerification(email: email, password: password) }
            }
        } message: {
            Text(successMessage)
        }
        .overlay {
            if authManager.isLoading {
                ProgressView().scaleEffect(1.5)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .background(Color.black.opacity(0.3))
            }
        }
    }

    private func verifyCode() {
        Task {
            let success = await authManager.verifyRegistrationOtp(email: email, code: code)
            if success {
                successMessage = "Correo verificado exitosamente. Ahora puedes iniciar sesión."
                showSuccess = true
            } else {
                showError = true
            }
        }
    }

    private func resendCode() {
        isResending = true
        Task {
            _ = await authManager.resendVerification(email: email)
            isResending = false
        }
    }
}
