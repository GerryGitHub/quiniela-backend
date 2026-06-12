package com.quiniela.app.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    var sessionExpired = false
        private set

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
            TokenManager.getToken()?.let { token ->
                request.addHeader("Authorization", "Bearer $token")
            }
            chain.proceed(request.build())
        }
        .addInterceptor { chain ->
            val response = chain.proceed(chain.request())
            if (response.code == 401 || response.code == 403) {
                TokenManager.clearAll()
                sessionExpired = true
            }
            response
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}

object TokenManager {
    private const val PREF_NAME = "quiniela_session"
    private const val KEY_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_EMAIL = "usuario_email"
    private const val KEY_NOMBRE = "usuario_nombre"

    private var prefs: android.content.SharedPreferences? = null

    fun init(context: android.content.Context) {
        prefs = context.getSharedPreferences(PREF_NAME, android.content.Context.MODE_PRIVATE)
    }

    private fun p() = prefs ?: throw IllegalStateException("TokenManager not initialized")

    fun setToken(token: String?) { p().edit().putString(KEY_TOKEN, token).apply() }
    fun getToken(): String? = p().getString(KEY_TOKEN, null)
    fun clearToken() { p().edit().remove(KEY_TOKEN).apply() }

    fun setRefreshToken(refreshToken: String?) { p().edit().putString(KEY_REFRESH_TOKEN, refreshToken).apply() }
    fun getRefreshToken(): String? = p().getString(KEY_REFRESH_TOKEN, null)
    fun clearRefreshToken() { p().edit().remove(KEY_REFRESH_TOKEN).apply() }

    fun setUsuario(email: String?, nombre: String?) {
        p().edit().putString(KEY_EMAIL, email).putString(KEY_NOMBRE, nombre).apply()
    }
    fun getUsuarioEmail(): String? = p().getString(KEY_EMAIL, null)
    fun getUsuarioNombre(): String? = p().getString(KEY_NOMBRE, null)

    fun clearAll() {
        p().edit()
            .remove(KEY_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_EMAIL)
            .remove(KEY_NOMBRE)
            .apply()
    }
}