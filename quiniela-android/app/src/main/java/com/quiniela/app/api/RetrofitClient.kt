package com.quiniela.app.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // Production: https://api.gjapps.com/
    private const val BASE_URL = "https://api.gjapps.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
            TokenManager.getToken()?.let { token ->
                request.addHeader("Authorization", "Bearer $token")
            }
            chain.proceed(request.build())
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
    private var token: String? = null
    private var refreshToken: String? = null
    private var usuarioEmail: String? = null
    private var usuarioNombre: String? = null

    fun setToken(token: String?) {
        this.token = token
    }

    fun getToken(): String? = token

    fun clearToken() {
        token = null
    }

    fun setRefreshToken(refreshToken: String?) {
        this.refreshToken = refreshToken
    }

    fun getRefreshToken(): String? = refreshToken

    fun clearRefreshToken() {
        refreshToken = null
    }

    fun setUsuario(email: String?, nombre: String?) {
        usuarioEmail = email
        usuarioNombre = nombre
    }

    fun getUsuarioEmail(): String? = usuarioEmail

    fun getUsuarioNombre(): String? = usuarioNombre

    fun clearAll() {
        token = null
        refreshToken = null
        usuarioEmail = null
        usuarioNombre = null
    }
}