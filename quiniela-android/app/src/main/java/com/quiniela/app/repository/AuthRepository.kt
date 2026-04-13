package com.quiniela.app.repository

import com.quiniela.app.api.ApiService
import com.quiniela.app.api.RetrofitClient
import com.quiniela.app.api.TokenManager
import com.quiniela.app.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}

class AuthRepository(private val apiService: ApiService = RetrofitClient.apiService) {

    suspend fun register(nombre: String, email: String, password: String): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.register(RegisterRequest(nombre, email, password))
                if (response.isSuccessful) {
                    response.body()?.let {
                        TokenManager.setToken(it.token)
                        Result.Success(it)
                    } ?: Result.Error("Respuesta vacía")
                } else {
                    Result.Error("Error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "Error desconocido")
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
                    } ?: Result.Error("Respuesta vacía")
                } else {
                    Result.Error("Error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "Error desconocido")
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
                    } ?: Result.Error("Respuesta vacía")
                } else {
                    Result.Error("Error: ${response.code()}")
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun logout() {
        TokenManager.clearToken()
    }

    fun isLoggedIn(): Boolean = TokenManager.getToken() != null
}