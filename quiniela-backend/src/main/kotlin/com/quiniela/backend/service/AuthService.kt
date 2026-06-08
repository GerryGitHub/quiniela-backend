package com.quiniela.backend.service

import com.quiniela.backend.dto.*

interface AuthService {
    fun register(request: RegisterRequest): RegisterResponse
    fun verifyRegistrationOtp(request: VerifyRegistrationOtpRequest): MessageResponse
    fun login(request: LoginRequest): AuthResponse
    fun refreshAccessToken(request: RefreshTokenRequest): RefreshTokenResponse
    fun resendVerification(request: ResendVerificationRequest): MessageResponse
}
