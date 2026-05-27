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
        val token = intent.getStringExtra("token") ?: ""
        val newPassword = binding.etNewPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        if (token.isEmpty()) {
            showError("El enlace de recuperación no es válido")
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
            when (val result = authRepository.resetPassword(token, newPassword)) {
                is Result.Success -> {
                    showSuccess(result.data)
                    binding.btnReset.visibility = View.GONE
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
