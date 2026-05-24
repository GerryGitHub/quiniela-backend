package com.quiniela.app.repository

import com.quiniela.app.api.ApiService
import com.quiniela.app.api.RetrofitClient
import com.quiniela.app.model.CrearQuinielaRequest
import com.quiniela.app.model.LeaderboardEntryDTO
import com.quiniela.app.model.QuinielaDTO
import com.quiniela.app.model.QuinielaDetalleDTO
import com.quiniela.app.model.UnirseQuinielaRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuinielaRepository(private val apiService: ApiService = RetrofitClient.apiService) {

    private fun parseError(response: retrofit2.Response<*>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            val json = com.google.gson.Gson().fromJson(errorBody, Map::class.java)
            json["error"]?.toString() ?: getMessageForCode(response.code())
        } catch (e: Exception) {
            getMessageForCode(response.code())
        }
    }

    private fun getMessageForCode(code: Int): String = when (code) {
        400 -> "Solicitud inválida"
        401 -> "Tu sesión expiró. Por favor inicia sesión."
        403 -> "No autorizado"
        404 -> "Recurso no encontrado"
        409 -> "Conflicto: el recurso ya existe"
        in 500..599 -> "Error del servidor. Intenta más tarde."
        else -> "Error: $code"
    }

    suspend fun getQuinielaDetalle(id: Long): Result<QuinielaDetalleDTO> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getQuinielaDetalle(id)
                if (response.isSuccessful) {
                    response.body()?.let { Result.Success(it) } ?: Result.Error("Respuesta vacía")
                } else {
                    Result.Error(parseError(response))
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "Error de conexión")
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
                    Result.Error(parseError(response))
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "Error de conexión")
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
                    Result.Error(parseError(response))
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "Error de conexión")
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
                    Result.Error(parseError(response))
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "Error de conexión")
            }
        }
    }
}