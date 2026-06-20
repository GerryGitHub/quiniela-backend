package com.quiniela.app.repository

import com.quiniela.app.api.RetrofitClient
import com.quiniela.app.model.BracketPreviewDTO

class EliminatoriasRepository {
    private val api = RetrofitClient.apiService

    suspend fun getPreview(): Result<BracketPreviewDTO> {
        return try {
            val response = api.getEliminatoriasPreview()
            if (response.isSuccessful) {
                Result.Success(response.body()!!)
            } else {
                Result.Error("Error al cargar eliminatorias")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error de conexión")
        }
    }
}
