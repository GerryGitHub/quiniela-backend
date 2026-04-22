package com.quiniela.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.quiniela.app.databinding.ActivityLoginBinding
import com.quiniela.app.repository.AuthRepository
import com.quiniela.app.repository.Result
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val authRepository = AuthRepository()
    private lateinit var biometricPrompt: BiometricPrompt

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBiometric()
        checkBiometricAvailability()
        
        binding.btnLogin.setOnClickListener { login() }
        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        
        binding.btnBiometric.setOnClickListener {
            showBiometricPrompt()
        }
    }

    private fun setupBiometric() {
        val executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    loginWithSavedCredentials()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                        errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        Toast.makeText(this@LoginActivity, "Error: $errString", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(this@LoginActivity, "Autenticación fallida", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun checkBiometricAvailability() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                binding.btnBiometric.visibility = View.VISIBLE
            }
            else -> {
                binding.btnBiometric.visibility = View.GONE
            }
        }
    }

    private fun showBiometricPrompt() {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Iniciar sesión")
            .setSubtitle("Usa tu huella o rostro para iniciar sesión")
            .setNegativeButtonText("Usar contraseña")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .build()
        biometricPrompt.authenticate(promptInfo)
    }

    private fun loginWithSavedCredentials() {
        val prefs = getSharedPreferences("quiniela_prefs", MODE_PRIVATE)
        val email = prefs.getString("saved_email", null)
        val password = prefs.getString("saved_password", null)

        if (email != null && password != null) {
            binding.etEmail.setText(email)
            binding.etPassword.setText(password)
            login()
        } else {
            Toast.makeText(this, "Primero inicia sesión con correo y contraseña", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveCredentials(email: String, password: String) {
        val prefs = getSharedPreferences("quiniela_prefs", MODE_PRIVATE)
        prefs.edit().apply {
            putString("saved_email", email)
            putString("saved_password", password)
            apply()
        }
    }

    private fun login() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            when (val result = authRepository.login(email, password)) {
                is Result.Success -> {
                    saveCredentials(email, password)
                    startActivity(Intent(this@LoginActivity, com.quiniela.app.MainActivity::class.java))
                    finish()
                }
                is Result.Error -> {
                    Toast.makeText(this@LoginActivity, result.message, Toast.LENGTH_LONG).show()
                }
            }
            binding.progressBar.visibility = View.GONE
        }
    }
}