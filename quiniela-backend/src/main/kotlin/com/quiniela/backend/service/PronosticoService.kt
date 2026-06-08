package com.quiniela.backend.service

import com.quiniela.backend.dto.*

interface PronosticoService {
    fun getMisPronosticos(quinielaId: Long, email: String): MisPronosticosDTO
    fun getTodosMisPronosticos(email: String): MisPronosticosDTO
    fun crearOActualizarPronostico(request: CrearPronosticoRequest, email: String): PronosticoDTO
    fun guardarPronosticosBatch(request: CrearPronosticosBatchRequest, email: String): CrearPronosticosBatchResponse
    fun getPronosticosPorPartido(quinielaId: Long, partidoId: Long, email: String): PronosticosPorPartidoDTO
}
