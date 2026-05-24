package com.quiniela.app.repository

import com.google.gson.Gson
import com.quiniela.app.api.ApiService
import com.quiniela.app.api.RetrofitClient
import com.quiniela.app.model.TablaGruposDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GrupoRepository(private val apiService: ApiService = RetrofitClient.apiService) {

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

    suspend fun getGrupos(): Result<TablaGruposDTO> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getGrupos()
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