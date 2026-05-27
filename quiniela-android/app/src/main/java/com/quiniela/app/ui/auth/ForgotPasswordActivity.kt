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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnBackToLogin.setOnClickListener { finish() }
        binding.btnSend.setOnClickListener { sendInstructions() }
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

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun sendInstructions() {
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
                    showSuccess("Revisa tu correo. Te enviamos un código de verificación.")
                    binding.btnSend.visibility = View.GONE
                    binding.etEmail.isEnabled = false
                    binding.btnBackToLogin.text = "Ingresar código"
                    binding.btnBackToLogin.setOnClickListener {
                        startActivity(Intent(this@ForgotPasswordActivity, ResetPasswordActivity::class.java).apply {
                            putExtra("email", email)
                        })
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
