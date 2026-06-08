package com.quiniela.backend.service

interface EmailService {
    fun sendVerificationEmail(email: String, nombre: String, token: String)
    fun sendPasswordResetEmail(email: String, nombre: String, token: String)
}
