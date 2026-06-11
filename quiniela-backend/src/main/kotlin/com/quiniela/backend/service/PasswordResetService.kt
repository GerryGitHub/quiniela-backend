package com.quiniela.backend.service

import com.quiniela.backend.dto.*

interface PasswordResetService {
    fun forgotPassword(request: ForgotPasswordRequest): MessageResponse
    fun resetPassword(request: ResetPasswordRequest): MessageResponse
}
