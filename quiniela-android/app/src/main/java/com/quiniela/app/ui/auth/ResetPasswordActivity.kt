package com.quiniela.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.quiniela.app.databinding.ActivityResetPasswordBinding
import com.quiniela.app.repository.AuthRepository
import com.quiniela.app.repository.Result
import kotlinx.coroutines.launch

class ResetPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResetPasswordBinding
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvEmail.text = intent.getStringExtra("email") ?: ""
        binding.btnBackToLogin.setOnClickListener { finish() }
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

    private fun resetPassword() {
        val email = intent.getStringExtra("email") ?: ""
        val code = binding.etCode.text.toString().trim()
        val newPassword = binding.etNewPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        if (email.isEmpty()) {
            showError("Correo no encontrado. Solicita un nuevo código.")
            return
        }

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
            when (val result = authRepository.resetPassword(email, code, newPassword)) {
                is Result.Success -> {
                    showSuccess(result.data)
                    binding.btnReset.visibility = View.GONE
                    binding.etCode.isEnabled = false
                    binding.etNewPassword.isEnabled = false
                    binding.etConfirmPassword.isEnabled = false
                    binding.btnBackToLogin.text = "Ir a inicio de sesión"
                    binding.btnBackToLogin.setOnClickListener {
                        startActivity(Intent(this@ResetPasswordActivity, LoginActivity::class.java))
                        finish()
                    }
                }
                is Result.Error -> {
                    showError(result.message)
                }
            }
            binding.progressBar.visibility = View.GONE
        }
    }
}
