package com.quiniela.app.repository

import com.quiniela.app.api.ApiService
import com.quiniela.app.api.RetrofitClient
import com.quiniela.app.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuinielaRepository(private val apiService: ApiService = RetrofitClient.apiService) {

    suspend fun getQuinielaDetalle(id: Long): Result<QuinielaDetalleDTO> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getQuinielaDetalle(id)
                if (response.isSuccessful) {
                    response.body()?.let { Result.Success(it) } ?: Result.Error("Respuesta vacía")
                } else {
                    Result.Error("Error: ${response.code()}")
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "Error desconocido")
            }
        }
    }

    suspend fun getLeaderboard(id: Long): Result<List<LeaderboardEntryDTO>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getLeaderboard(id)
                if (response.isSuccessful) {
                    response.body()?.let { Result.Success(it) } ?: Result.Error("Respuesta vacía")
                } else {
                    Result.Error("Error: ${response.code()}")
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "Error desconocido")
            }
        }
    }

    suspend fun crearQuiniela(nombre: String, codigo: String): Result<QuinielaDTO> {
        return withContext(Dispatchers.IO) {
            try {
                val request = CrearQuinielaRequest(nombre, codigo)
                val response = apiService.crearQuiniela(request)
                if (response.isSuccessful) {
                    response.body()?.let { Result.Success(it) } ?: Result.Error("Respuesta vacía")
                } else {
                    Result.Error("Error: ${response.code()}")
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "Error desconocido")
            }
        }
    }

    suspend fun unirseQuiniela(codigo: String): Result<QuinielaDTO> {
        return withContext(Dispatchers.IO) {
            try {
                val request = UnirseQuinielaRequest(codigo)
                val response = apiService.unirseQuiniela(request)
                if (response.isSuccessful) {
                    response.body()?.let { Result.Success(it) } ?: Result.Error("Respuesta vacía")
                } else {
                    Result.Error("Error: ${response.code()}")
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "Error desconocido")
            }
        }
    }
}