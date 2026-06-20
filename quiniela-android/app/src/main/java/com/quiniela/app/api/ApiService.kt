package com.quiniela.app.api

import com.quiniela.app.model.ActualizarPartidoRequest
import com.quiniela.app.model.AuthResponse
import com.quiniela.app.model.CrearPronosticoRequest
import com.quiniela.app.model.CrearPronosticosBatchRequest
import com.quiniela.app.model.CrearPronosticosBatchResponse
import com.quiniela.app.model.CrearQuinielaRequest
import com.quiniela.app.model.LeaderboardEntryDTO
import com.quiniela.app.model.ForgotPasswordRequest
import com.quiniela.app.model.LoginRequest
import com.quiniela.app.model.MessageResponse
import com.quiniela.app.model.RefreshTokenRequest
import com.quiniela.app.model.RefreshTokenResponse
import com.quiniela.app.model.ResendVerificationRequest
import com.quiniela.app.model.VerifyRegistrationOtpRequest
import com.quiniela.app.model.ResetPasswordRequest
import com.quiniela.app.model.MisPronosticosDTO
import com.quiniela.app.model.PartidoDTO
import com.quiniela.app.model.PronosticoDTO
import com.quiniela.app.model.PronosticosPorPartidoDTO
import com.quiniela.app.model.QuinielaDTO
import com.quiniela.app.model.QuinielaDetalleDTO
import com.quiniela.app.model.QuinielaResumenDTO
import com.quiniela.app.model.RegisterRequest
import com.quiniela.app.model.TablaGruposDTO
import com.quiniela.app.model.UnirseQuinielaRequest
import com.quiniela.app.model.UsuarioPerfilDTO
import com.quiniela.app.model.BracketPreviewDTO
import com.quiniela.app.model.EliminatoriasStatusDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<MessageResponse>

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

    @GET("pronosticos/quiniela/{quinielaId}")
    suspend fun getMisPronosticosByQuiniela(@Path("quinielaId") quinielaId: Long): Response<MisPronosticosDTO>

    @GET("pronosticos/quiniela/{quinielaId}/partido/{partidoId}")
    suspend fun getPronosticosPorPartido(
        @Path("quinielaId") quinielaId: Long,
        @Path("partidoId") partidoId: Long
    ): Response<PronosticosPorPartidoDTO>

    @POST("pronosticos")
    suspend fun crearPronostico(@Body request: CrearPronosticoRequest): Response<PronosticoDTO>

    @POST("pronosticos/batch")
    suspend fun crearPronosticosBatch(@Body request: CrearPronosticosBatchRequest): Response<CrearPronosticosBatchResponse>

    @GET("quinielas")
    suspend fun getQuinielas(): Response<List<QuinielaResumenDTO>>

    @GET("quinielas/{id}")
    suspend fun getQuinielaDetalle(@Path("id") id: Long): Response<QuinielaDetalleDTO>

    @GET("quinielas/{id}/leaderboard")
    suspend fun getLeaderboard(@Path("id") id: Long): Response<List<LeaderboardEntryDTO>>

    @POST("quinielas")
    suspend fun crearQuiniela(@Body request: CrearQuinielaRequest): Response<QuinielaDTO>

    @POST("quinielas/join")
    suspend fun unirseQuiniela(@Body request: UnirseQuinielaRequest): Response<QuinielaDTO>

    @GET("api/grupos")
    suspend fun getGrupos(): Response<TablaGruposDTO>

    @GET("api/resultados/partidos")
    suspend fun getResultados(): Response<List<PartidoDTO>>

    @GET("api/resultados/en-vivo")
    suspend fun getPartidosEnVivo(): Response<List<PartidoDTO>>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<MessageResponse>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<MessageResponse>

    @POST("auth/verify-registration-otp")
    suspend fun verifyRegistrationOtp(@Body request: VerifyRegistrationOtpRequest): Response<MessageResponse>

    @POST("auth/resend-verification")
    suspend fun resendVerification(@Body request: ResendVerificationRequest): Response<MessageResponse>

    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshTokenRequest): Response<RefreshTokenResponse>

    @GET("pronosticos/quiniela/{quinielaId}/usuario/{usuarioId}")
    suspend fun getPronosticosByUser(
        @Path("quinielaId") quinielaId: Long,
        @Path("usuarioId") usuarioId: Long
    ): Response<MisPronosticosDTO>

    @GET("api/eliminatorias/preview")
    suspend fun getEliminatoriasPreview(): Response<BracketPreviewDTO>

    @GET("api/eliminatorias/status")
    suspend fun getEliminatoriasStatus(): Response<EliminatoriasStatusDTO>
}