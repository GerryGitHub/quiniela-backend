package com.quiniela.app.repository

import com.quiniela.app.api.ApiService
import com.quiniela.app.api.RetrofitClient
import com.quiniela.app.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GrupoRepository(private val apiService: ApiService = RetrofitClient.apiService) {

    suspend fun getGrupos(): Result<TablaGruposDTO> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getGrupos()
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