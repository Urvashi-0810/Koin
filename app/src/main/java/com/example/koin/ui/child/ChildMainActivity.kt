package com.example.koin.ui.child

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.koin.R
import com.example.koin.data.local.LocalStorage
import com.example.koin.databinding.ActivityChildMainBinding
import com.example.koin.databinding.DialogProfileBinding
import com.example.koin.ui.auth.RoleSelectionActivity
import com.example.koin.ui.viewmodel.AuthViewModel

class ChildMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChildMainBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChildMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbarMenu()

        if (savedInstanceState == null) {
            loadFragment(ChildDashboardFragment())
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_child_dashboard -> loadFragment(ChildDashboardFragment())
                R.id.nav_child_challenges -> loadFragment(ChildChallengesFragment())
                R.id.nav_child_leaderboard -> loadFragment(ChildLeaderboardFragment())
                else -> false
            }
        }
    }

    private fun setupToolbarMenu() {
        binding.toolbar.inflateMenu(R.menu.top_app_bar_menu)
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_upgrade_pro -> {
                    startActivity(Intent(this, com.example.koin.ui.subscriptions.PaywallActivity::class.java))
                    true
                }
                R.id.action_profile -> {
                    showProfileDialog()
                    true
                }
                R.id.action_switch_role -> {
                    confirmAndLogout()
                    true
                }
                else -> false
            }
        }
    }

    private fun showProfileDialog() {
        val localStorage = LocalStorage(this)
        val user = localStorage.getCurrentUser()
        val family = user?.familyId?.let { localStorage.getFamilyById(it) }

        val dialogBinding = DialogProfileBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.tvProfileName.text = user?.name ?: "Child Account"
        dialogBinding.tvProfileRole.text = getString(R.string.profile_role_child)
        dialogBinding.tvProfileRole.setBackgroundResource(R.drawable.bg_tier_excellent)
        dialogBinding.tvProfileRole.setTextColor(ContextCompat.getColor(this, R.color.tier_excellent))

        val parentInfo = family?.parentName?.let { "Parent: $it" } ?: "Child Mode"
        dialogBinding.tvProfileDetails.text = parentInfo

        val code = family?.familyCode ?: ""
        if (code.isNotEmpty()) {
            dialogBinding.cardDialogFamilyCode.visibility = View.VISIBLE
            dialogBinding.tvProfileFamilyCode.text = code
            dialogBinding.btnCopyCodeInDialog.setOnClickListener {
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Family Code", code)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, R.string.code_copied_toast, Toast.LENGTH_SHORT).show()
            }
        } else {
            dialogBinding.cardDialogFamilyCode.visibility = View.GONE
        }

        dialogBinding.btnSwitchRole.setOnClickListener {
            dialog.dismiss()
            confirmAndLogout()
        }

        dialogBinding.btnCloseProfile.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun confirmAndLogout() {
        AlertDialog.Builder(this)
            .setTitle(R.string.switch_role_confirm_title)
            .setMessage(R.string.switch_role_confirm_msg)
            .setPositiveButton("Switch Role") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        authViewModel.logout()
        Toast.makeText(this, "Logged out. Select Parent or Child role.", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, RoleSelectionActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun loadFragment(fragment: Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
        return true
    }
}
