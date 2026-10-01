package com.example.koin.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.koin.databinding.ActivityRoleSelectionBinding

class RoleSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoleSelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoleSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cardParent.setOnClickListener {
            startActivity(Intent(this, ParentLoginActivity::class.java))
        }

        binding.cardChild.setOnClickListener {
            startActivity(Intent(this, FamilyCodeActivity::class.java))
        }
    }
}
