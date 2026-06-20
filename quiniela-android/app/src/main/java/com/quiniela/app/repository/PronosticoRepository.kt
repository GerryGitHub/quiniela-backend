package com.quiniela.app.repository

import com.google.gson.Gson
import com.quiniela.app.api.ApiService
import com.quiniela.app.api.RetrofitClient
import com.quiniela.app.model.CrearPronosticosBatchRequest
import com.quiniela.app.model.CrearPronosticosBatchResponse
import com.quiniela.app.model.MisPronosticosDTO
import com.quiniela.app.model.PronosticoItemRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PronosticoRepository(private val apiService: ApiService = RetrofitClient.apiService) {

    private fun parseError(response: retrofit2.Response<*>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            val json = Gson().fromJson(errorBody, Map::class.java)
            json["error"]?.toString() ?: getMessageForCode(response.code())
        } catch (e: Exception) {
            getMessageForCode(response.code())
        }
    }

    private fun getMessageForCode(code: Int): String = when (code) {
        400 -> "Solicitud inválida"
        401 -> "Tu sesión expiró"
        403 -> "No autorizado"
        404 -> "Recurso no encontrado"
        in 500..599 -> "Error del servidor"
        else -> "Error: $code"
    }

    suspend fun getMisPronosticos(): Result<MisPronosticosDTO> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getMisPronosticos()
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

    suspend fun getMisPronosticosByQuiniela(quinielaId: Long): Result<MisPronosticosDTO> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getMisPronosticosByQuiniela(quinielaId)
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

    suspend fun getPronosticosByUser(quinielaId: Long, usuarioId: Long): Result<MisPronosticosDTO> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getPronosticosByUser(quinielaId, usuarioId)
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

    suspend fun crearPronosticosBatch(
        idQuiniela: Long,
        pronosticos: List<PronosticoItemRequest>
    ): Result<CrearPronosticosBatchResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = CrearPronosticosBatchRequest(idQuiniela, pronosticos)
                val response = apiService.crearPronosticosBatch(request)
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