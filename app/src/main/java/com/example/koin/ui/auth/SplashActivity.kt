package com.example.koin.ui.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.koin.data.local.LocalStorage
import com.example.koin.data.model.UserRole
import com.example.koin.databinding.ActivitySplashBinding
import com.example.koin.ui.child.ChildMainActivity
import com.example.koin.ui.parent.ParentMainActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler(Looper.getMainLooper()).postDelayed({
            checkUserSession()
        }, 1500)
    }

    private fun checkUserSession() {
        val localStorage = LocalStorage(this)
        val user = localStorage.getCurrentUser()

        if (user == null) {
            startActivity(Intent(this, RoleSelectionActivity::class.java))
        } else {
            when (user.role) {
                UserRole.PARENT -> startActivity(Intent(this, ParentMainActivity::class.java))
                UserRole.CHILD -> startActivity(Intent(this, ChildMainActivity::class.java))
            }
        }
        finish()
    }
}
