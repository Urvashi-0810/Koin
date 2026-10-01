package com.example.koin.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.koin.databinding.ActivityParentLoginBinding
import com.example.koin.ui.parent.ParentMainActivity
import com.example.koin.ui.viewmodel.AuthViewModel

class ParentLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityParentLoginBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityParentLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()

        binding.btnSubmit.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (name.isNotEmpty()) {
                viewModel.registerParent(name, email, pass)
            } else {
                viewModel.loginParent(email, pass)
            }
        }
    }

    private fun setupObservers() {
        viewModel.authState.observe(this) { state ->
            when (state) {
                is AuthViewModel.AuthState.Loading -> {
                    binding.pbLoading.visibility = View.VISIBLE
                    binding.btnSubmit.isEnabled = false
                }
                is AuthViewModel.AuthState.Success -> {
                    binding.pbLoading.visibility = View.GONE
                    Toast.makeText(this, "Welcome, ${state.user.name}!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, ParentMainActivity::class.java))
                    finishAffinity()
                }
                is AuthViewModel.AuthState.Error -> {
                    binding.pbLoading.visibility = View.GONE
                    binding.btnSubmit.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                else -> {
                    binding.pbLoading.visibility = View.GONE
                    binding.btnSubmit.isEnabled = true
                }
            }
        }
    }
}
