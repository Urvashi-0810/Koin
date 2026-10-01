package com.example.koin.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.koin.databinding.ActivityFamilyCodeBinding
import com.example.koin.ui.child.ChildMainActivity
import com.example.koin.ui.viewmodel.AuthViewModel

class FamilyCodeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFamilyCodeBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFamilyCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()

        binding.btnLink.setOnClickListener {
            val childName = binding.etChildName.text.toString().trim()
            val code = binding.etFamilyCode.text.toString().trim()

            if (childName.isEmpty()) {
                Toast.makeText(this, "Please enter your first name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (code.length != 6) {
                Toast.makeText(this, "Family code must be 6 digits", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.linkChildWithFamilyCode(childName, code)
        }
    }

    private fun setupObservers() {
        viewModel.familyState.observe(this) { state ->
            when (state) {
                is AuthViewModel.FamilyState.Loading -> {
                    binding.pbLoading.visibility = View.VISIBLE
                    binding.btnLink.isEnabled = false
                }
                is AuthViewModel.FamilyState.Success -> {
                    binding.pbLoading.visibility = View.GONE
                    Toast.makeText(this, "Linked to family ${state.family.parentName}!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, ChildMainActivity::class.java))
                    finishAffinity()
                }
                is AuthViewModel.FamilyState.Error -> {
                    binding.pbLoading.visibility = View.GONE
                    binding.btnLink.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                else -> {
                    binding.pbLoading.visibility = View.GONE
                    binding.btnLink.isEnabled = true
                }
            }
        }
    }
}
