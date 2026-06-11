package com.quiniela.backend.service

import com.quiniela.backend.domain.ForgotPasswordCommand
import com.quiniela.backend.domain.ResetPasswordCommand
import com.quiniela.backend.dto.*

interface PasswordResetService {
    fun forgotPassword(command: ForgotPasswordCommand): MessageResponse
    fun resetPassword(command: ResetPasswordCommand): MessageResponse
}
