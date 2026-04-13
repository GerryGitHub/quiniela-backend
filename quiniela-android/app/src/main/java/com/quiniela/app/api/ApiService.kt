package com.quiniela.app.api

import com.quiniela.app.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("auth/me")
    suspend fun getPerfil(): Response<UsuarioPerfilDTO>

    @GET("partidos")
    suspend fun getPartidos(
        @Query("fecha") fecha: String? = null,
        @Query("fase") fase: String? = null
    ): Response<List<PartidoDTO>>

    @GET("partidos/{id}")
    suspend fun getPartidoDetalle(@Path("id") id: Long): Response<PartidoDTO>

    @PATCH("partidos/{id}")
    suspend fun actualizarPartido(
        @Path("id") id: Long,
        @Body request: ActualizarPartidoRequest
    ): Response<PartidoDTO>

    @GET("pronosticos/mis-pronosticos")
    suspend fun getMisPronosticos(): Response<MisPronosticosDTO>

    @GET("pronosticos/partido/{id}")
    suspend fun getPronosticosPorPartido(@Path("id") id: Long): Response<PronosticosPorPartidoDTO>

    @POST("pronosticos")
    suspend fun crearPronostico(@Body request: CrearPronosticoRequest): Response<PronosticoDTO>

    @POST("pronosticos/batch")
    suspend fun crearPronosticosBatch(@Body request: CrearPronosticosBatchRequest): Response<CrearPronosticosBatchResponse>

    @GET("grupos")
    suspend fun getGrupos(): Response<TablaGruposDTO>

    @GET("resultados")
    suspend fun getResultados(): Response<List<PartidoDTO>>
}