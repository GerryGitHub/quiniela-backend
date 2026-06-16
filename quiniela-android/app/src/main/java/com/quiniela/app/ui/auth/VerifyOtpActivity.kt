package com.quiniela.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.quiniela.app.databinding.ActivityVerifyOtpBinding
import com.quiniela.app.repository.AuthRepository
import com.quiniela.app.repository.Result
import kotlinx.coroutines.launch

class VerifyOtpActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVerifyOtpBinding
    private val authRepository = AuthRepository()
    private var currentEmail = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityVerifyOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentEmail = intent.getStringExtra("email") ?: ""

        binding.btnBack.setOnClickListener { finish() }
        binding.btnBackToLogin.setOnClickListener { finish() }
        binding.btnVerify.setOnClickListener { verifyOtp() }
        binding.btnResend.setOnClickListener { resendCode() }

        if (currentEmail.isNotEmpty()) {
            binding.tvEmail.text = currentEmail
            binding.tvEmail.visibility = View.VISIBLE
        }
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

    private fun verifyOtp() {
        val code = binding.etCode.text.toString().trim()

        if (code.length != 6) {
            showError("Ingresa el código de 6 dígitos")
            return
        }

        if (currentEmail.isEmpty()) {
            showError("Error: correo no disponible")
            return
        }

        hideError()
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            when (val result = authRepository.verifyRegistrationOtp(currentEmail, code)) {
                is Result.Success -> {
                    startActivity(Intent(this@VerifyOtpActivity, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        putExtra("verified_success", true)
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

    private fun resendCode() {
        if (currentEmail.isEmpty()) {
            showError("Error: correo no disponible")
            return
        }

        hideError()
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            when (val result = authRepository.resendVerification(currentEmail)) {
                is Result.Success -> {
                    showSuccess("Revisa tu correo. Te enviamos un nuevo código de verificación.")
                }
                is Result.Error -> {
                    showError(result.message)
                }
            }
            binding.progressBar.visibility = View.GONE
        }
    }
}
