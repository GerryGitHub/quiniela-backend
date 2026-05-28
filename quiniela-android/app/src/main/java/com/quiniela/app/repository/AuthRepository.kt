package com.quiniela.app.repository

import com.google.gson.Gson
import com.quiniela.app.api.ApiService
import com.quiniela.app.api.RetrofitClient
import com.quiniela.app.api.TokenManager
import com.quiniela.app.model.AuthResponse
import com.quiniela.app.model.ForgotPasswordRequest
import com.quiniela.app.model.LoginRequest
import com.quiniela.app.model.RefreshTokenRequest
import com.quiniela.app.model.RefreshTokenResponse
import com.quiniela.app.model.RegisterRequest
import com.quiniela.app.model.ResendVerificationRequest
import com.quiniela.app.model.ResetPasswordRequest
import com.quiniela.app.model.VerifyRegistrationOtpRequest
import com.quiniela.app.model.UsuarioPerfilDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}

class AuthRepository(private val apiService: ApiService = RetrofitClient.apiService) {

    private fun parseError(response: retrofit2.Response<*>): String {
        val code = response.code()
        return when (code) {
            401 -> "Usuario o contraseña incorrectos."
            403 -> "Debes validar tu correo antes de iniciar sesión."
            in 500..599 -> "El servidor presentó un problema. Intenta más tarde."
            else -> {
                try {
                    val errorBody = response.errorBody()?.string()
                    if (errorBody != null) {
                        val json = Gson().fromJson(errorBody, Map::class.java)
                        val message = (json as? Map<*, *>)?.get("error")?.toString()
                        if (message != null) improveMessage(message) else "Ocurrió un error inesperado."
                    } else "Ocurrió un error inesperado."
                } catch (e: Exception) {
                    "Ocurrió un error inesperado."
                }
            }
        }
    }

    private fun improveMessage(message: String): String {
        return when {
            message.contains("Usuario no encontrado") -> "Usuario o contraseña incorrectos."
            message.contains("Bad credentials") || message.contains("Unauthorized") -> "Usuario o contraseña incorrectos."
            message.contains("verificar tu correo") -> "Debes validar tu correo antes de iniciar sesión."
            message.contains("ya está registrado") -> "Ya existe una cuenta con ese correo."
            message.contains("Email inválido") -> "Ingresa un correo electrónico válido."
            message.contains("La contraseña debe tener") -> "La contraseña debe tener al menos 6 caracteres."
            message.contains("nombre es requerido") || message.contains("email es requerido") || message.contains("contraseña es requerida") -> "Completa todos los campos obligatorios."
            message.contains("Código inválido") || message.contains("Token inválido") -> "El código ingresado no es válido."
            message.contains("código ya fue utilizado") || message.contains("Token ya fue utilizado") -> "El código ya fue usado. Solicita uno nuevo."
            message.contains("código ha expirado") || message.contains("Token ha expirado") -> "El código expiró. Solicita uno nuevo."
            else -> "Ocurrió un error inesperado."
        }
    }

    suspend fun register(nombre: String, email: String, password: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.register(RegisterRequest(nombre, email, password))
                if (response.isSuccessful) {
                    response.body()?.let {
                        Result.Success(it.message)
                    }                     ?: Result.Error("Error del servidor. Intenta nuevamente.")
                } else {
                    Result.Error(parseError(response))
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "Error de conexión. Intenta nuevamente.")
            }
        }
    }

    private fun parseLoginError(response: retrofit2.Response<*>): String {
        val bodyMessage = try {
            val errorBody = response.errorBody()?.string()
            if (errorBody != null) {
                val json = Gson().fromJson(errorBody, Map::class.java)
                (json as? Map<*, *>)?.get("error")?.toString()
            } else null
        } catch (e: Exception) { null }
        return when {
            response.code() == 403 && bodyMessage?.contains("verificar") == true ->
                "Debes validar tu correo antes de iniciar sesión."
            else -> "Usuario o contraseña incorrectos."
        }
    }

    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(LoginRequest(email, password))
                if (response.isSuccessful) {
                    response.body()?.let {
                        TokenManager.setToken(it.accessToken)
                        it.refreshToken?.let { rt -> TokenManager.setRefreshToken(rt) }
                        TokenManager.setUsuario(it.usuario.email, it.usuario.nombre)
                        Result.Success(it)
                    } ?: Result.Error("Error del servidor. Intenta nuevamente.")
                } else {
                    Result.Error(parseLoginError(response))
                }
            } catch (e: java.io.IOException) {
                Result.Error("No pudimos conectar con el servidor. Intenta nuevamente.")
            } catch (e: Exception) {
                Result.Error("Ocurrió un error inesperado.")
            }
        }
    }

    suspend fun getPerfil(): Result<UsuarioPerfilDTO> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getPerfil()
                if (response.isSuccessful) {
                    response.body()?.let {
                        Result.Success(it)
                    } ?: Result.Error("Error del servidor. Intenta nuevamente.")
                } else {
                    Result.Error(parseError(response))
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "Error de conexión. Intenta nuevamente.")
            }
        }
    }

    fun logout() {
        TokenManager.clearAll()
    }

    fun isLoggedIn(): Boolean = TokenManager.getToken() != null

    suspend fun refreshToken(): Result<RefreshTokenResponse> {
        val currentRefreshToken = TokenManager.getRefreshToken() ?: return Result.Error("No hay sesión activa")
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.refresh(RefreshTokenRequest(currentRefreshToken))
                if (response.isSuccessful) {
                    response.body()?.let {
                        TokenManager.setToken(it.accessToken)
                        it.refreshToken?.let { rt -> TokenManager.setRefreshToken(rt) }
                        Result.Success(it)
                    } ?: Result.Error("Error del servidor. Intenta nuevamente.")
                } else {
                    logout()
                    Result.Error("La sesión expiró. Inicia sesión nuevamente.")
                }
            } catch (e: java.io.IOException) {
                Result.Error("No pudimos conectar con el servidor. Intenta nuevamente.")
            } catch (e: Exception) {
                Result.Error("Ocurrió un error inesperado.")
            }
        }
    }

    suspend fun forgotPassword(email: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.forgotPassword(ForgotPasswordRequest(email))
                if (response.isSuccessful) {
                    response.body()?.let {
                        Result.Success(it.message)
                    } ?: Result.Error("Error del servidor. Intenta nuevamente.")
                } else {
                    Result.Error(parseError(response))
                }
            } catch (e: java.io.IOException) {
                Result.Error("No pudimos conectar con el servidor. Intenta nuevamente.")
            } catch (e: Exception) {
                Result.Error("Ocurrió un error inesperado.")
            }
        }
    }

    suspend fun verifyRegistrationOtp(email: String, code: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.verifyRegistrationOtp(VerifyRegistrationOtpRequest(email, code))
                if (response.isSuccessful) {
                    response.body()?.let {
                        Result.Success(it.message)
                    } ?: Result.Error("Error del servidor. Intenta nuevamente.")
                } else {
                    Result.Error(parseError(response))
                }
            } catch (e: java.io.IOException) {
                Result.Error("No pudimos conectar con el servidor. Intenta nuevamente.")
            } catch (e: Exception) {
                Result.Error("Ocurrió un error inesperado.")
            }
        }
    }

    suspend fun resendVerification(email: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.resendVerification(ResendVerificationRequest(email))
                if (response.isSuccessful) {
                    response.body()?.let {
                        Result.Success(it.message)
                    } ?: Result.Error("Error del servidor. Intenta nuevamente.")
                } else {
                    Result.Error(parseError(response))
                }
            } catch (e: java.io.IOException) {
                Result.Error("No pudimos conectar con el servidor. Intenta nuevamente.")
            } catch (e: Exception) {
                Result.Error("Ocurrió un error inesperado.")
            }
        }
    }

    suspend fun resetPassword(email: String, code: String, newPassword: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.resetPassword(ResetPasswordRequest(email, code, newPassword))
                if (response.isSuccessful) {
                    response.body()?.let {
                        Result.Success(it.message)
                    } ?: Result.Error("Error del servidor. Intenta nuevamente.")
                } else {
                    Result.Error(parseError(response))
                }
            } catch (e: java.io.IOException) {
                Result.Error("No pudimos conectar con el servidor. Intenta nuevamente.")
            } catch (e: Exception) {
                Result.Error("Ocurrió un error inesperado.")
            }
        }
    }
}