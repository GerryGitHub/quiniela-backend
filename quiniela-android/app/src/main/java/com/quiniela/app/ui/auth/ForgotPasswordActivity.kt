package com.quiniela.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.quiniela.app.databinding.ActivityForgotPasswordBinding
import com.quiniela.app.repository.AuthRepository
import com.quiniela.app.repository.Result
import kotlinx.coroutines.launch

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding
    private val authRepository = AuthRepository()
    private var currentEmail = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnBackToLogin.setOnClickListener { finish() }
        binding.btnSend.setOnClickListener { sendCode() }
        binding.btnReset.setOnClickListener { resetPassword() }
    }

    private fun showError(message: String) {
        hideSuccess()
        binding.tvError.text = message
        binding.layoutError.visibility = View.VISIBLE
        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.duration = 300
        binding.layoutError.startAnimation(fadeIn)
    }

    private fun hideError() {
        binding.layoutError.visibility = View.GONE
    }

    private fun showSuccess(message: String) {
        hideError()
        binding.tvSuccess.text = message
        binding.layoutSuccess.visibility = View.VISIBLE
        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.duration = 300
        binding.layoutSuccess.startAnimation(fadeIn)
    }

    private fun hideSuccess() {
        binding.layoutSuccess.visibility = View.GONE
    }

    private fun showResetForm() {
        binding.tvSubtitle.text = "Revisa tu correo. Ingresa el código y tu nueva contraseña."
        binding.tilEmail.visibility = View.GONE
        binding.btnSend.visibility = View.GONE
        binding.layoutResetFields.visibility = View.VISIBLE
        binding.tvEmail.text = currentEmail
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun sendCode() {
        val email = binding.etEmail.text.toString().trim()

        if (email.isEmpty()) {
            showError("Ingresa tu correo electrónico")
            return
        }

        if (!isValidEmail(email)) {
            showError("Ingresa un correo electrónico válido")
            return
        }

        hideError()
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            when (val result = authRepository.forgotPassword(email)) {
                is Result.Success -> {
                    currentEmail = email
                    showSuccess("Revisa tu correo. Te enviamos un código de verificación.")
                    showResetForm()
                }
                is Result.Error -> {
                    showError(result.message)
                }
            }
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun resetPassword() {
        val code = binding.etCode.text.toString().trim()
        val newPassword = binding.etNewPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        if (code.length != 6) {
            showError("Ingresa el código de 6 dígitos")
            return
        }

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showError("Completa todos los campos")
            return
        }

        if (newPassword.length < 6) {
            showError("La contraseña debe tener al menos 6 caracteres")
            return
        }

        if (newPassword != confirmPassword) {
            showError("Las contraseñas no coinciden")
            return
        }

        hideError()
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            when (val result = authRepository.resetPassword(currentEmail, code, newPassword)) {
                is Result.Success -> {
                    startActivity(Intent(this@ForgotPasswordActivity, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        putExtra("reset_success", true)
                    })
                    finish()
                }
                is Result.Error -> {
                    showError(result.message)
                }
            }
            binding.progressBar.visibility = View.GONE
        }
    }
}
