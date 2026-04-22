package com.quiniela.app.repository

import com.quiniela.app.api.ApiService
import com.quiniela.app.api.RetrofitClient
import com.quiniela.app.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PartidoRepository(private val apiService: ApiService = RetrofitClient.apiService) {

    suspend fun getPartidos(fecha: String? = null, fase: String? = null): Result<List<PartidoDTO>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getPartidos(fecha, fase)
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

suspend fun getResultados(): Result<List<PartidoDTO>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getResultados()
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

    suspend fun getPartidosEnVivo(): Result<List<PartidoDTO>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getPartidosEnVivo()
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