package com.quiniela.app.repository

import com.google.gson.Gson
import com.quiniela.app.api.ApiService
import com.quiniela.app.api.RetrofitClient
import com.quiniela.app.api.TokenManager
import com.quiniela.app.model.AuthResponse
import com.quiniela.app.model.LoginRequest
import com.quiniela.app.model.RegisterRequest
import com.quiniela.app.model.UsuarioPerfilDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}

class AuthRepository(private val apiService: ApiService = RetrofitClient.apiService) {

    private fun parseError(response: retrofit2.Response<*>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            val json = Gson().fromJson(errorBody, Map::class.java)
            val message = json["error"]?.toString() ?: getMessageForCode(response.code())
            improveMessage(message)
        } catch (e: Exception) {
            getMessageForCode(response.code())
        }
    }

    private fun improveMessage(message: String): String {
        return when {
            message.contains("Usuario no encontrado") -> "Usuario o contraseña incorrectos."
            message.contains("Bad credentials") -> "Usuario o contraseña incorrectos."
            message.contains("Unauthorized") -> "Usuario o contraseña incorrectos."
            message.contains("verificar tu correo") -> "Debes validar tu correo antes de iniciar sesión."
            message.contains("ya está registrado") -> "Ya existe una cuenta con ese correo."
            message.contains("Email inválido") -> "Ingresa un correo electrónico válido."
            message.contains("La contraseña debe tener") -> "La contraseña debe tener al menos 6 caracteres."
            message.contains("nombre es requerido") || message.contains("email es requerido") || message.contains("contraseña es requerida") -> "Completa todos los campos obligatorios."
            message.contains("Internal Server Error") || message.contains("Error interno") -> "Ocurrió un error inesperado. Intenta más tarde."
            else -> message
        }
    }

    private fun getMessageForCode(code: Int): String = when (code) {
        400 -> "No pudimos procesar tu solicitud. Verifica los datos."
        401 -> "Tu sesión expiró. Inicia sesión nuevamente."
        403 -> "Debes validar tu correo antes de iniciar sesión."
        404 -> "Recurso no encontrado."
        409 -> "El email ya está registrado."
        in 500..599 -> "Error del servidor. Intenta más tarde."
        else -> "Error inesperado ($code). Intenta más tarde."
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

    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(LoginRequest(email, password))
                if (response.isSuccessful) {
                    response.body()?.let {
                        TokenManager.setToken(it.token)
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
        TokenManager.clearToken()
    }

    fun isLoggedIn(): Boolean = TokenManager.getToken() != null
}