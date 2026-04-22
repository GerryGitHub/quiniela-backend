package com.quiniela.app.repository

import com.quiniela.app.api.ApiService
import com.quiniela.app.api.RetrofitClient
import com.quiniela.app.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PronosticoRepository(private val apiService: ApiService = RetrofitClient.apiService) {

    suspend fun getMisPronosticos(): Result<MisPronosticosDTO> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getMisPronosticos()
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

    suspend fun getMisPronosticosByQuiniela(quinielaId: Long): Result<MisPronosticosDTO> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getMisPronosticosByQuiniela(quinielaId)
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
                    Result.Error("Error: ${response.code()}")
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "Error desconocido")
            }
        }
    }
}