package com.example.koin.ui.subscriptions

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.koin.data.subscriptions.RevenueCatManager
import com.example.koin.databinding.ActivityPaywallBinding

class PaywallActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaywallBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaywallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        loadSubscriptionOfferings()
    }

    private fun setupListeners() {
        binding.btnClosePaywall.setOnClickListener {
            finish()
        }

        binding.btnSubscribeMonthly.setOnClickListener {
            binding.pbPaywallLoading.visibility = View.VISIBLE
            binding.btnSubscribeMonthly.isEnabled = false

            RevenueCatManager.purchaseMonthlySubscription(
                activity = this,
                onSuccess = {
                    binding.pbPaywallLoading.visibility = View.GONE
                    binding.btnSubscribeMonthly.isEnabled = true
                    Toast.makeText(
                        this,
                        "🎉 Congratulations! You are now a Koin Pro member! All Ads Removed.",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                },
                onError = { errorMsg ->
                    binding.pbPaywallLoading.visibility = View.GONE
                    binding.btnSubscribeMonthly.isEnabled = true
                    Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
                }
            )
        }

        binding.btnRestorePurchases.setOnClickListener {
            binding.pbPaywallLoading.visibility = View.VISIBLE
            RevenueCatManager.restorePurchases(
                onSuccess = {
                    binding.pbPaywallLoading.visibility = View.GONE
                    if (RevenueCatManager.isAdFreeUser) {
                        Toast.makeText(this, "Pro Membership Restored! Ads Removed.", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        Toast.makeText(this, "No active Pro subscription found.", Toast.LENGTH_SHORT).show()
                    }
                },
                onError = { error ->
                    binding.pbPaywallLoading.visibility = View.GONE
                    Toast.makeText(this, "Restore failed: $error", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun loadSubscriptionOfferings() {
        RevenueCatManager.fetchOfferings(
            onSuccess = { pkg ->
                pkg?.let {
                    binding.tvPlanPrice.text = "₹199 / month"
                }
            },
            onError = {
                // Keep default price presentation
            }
        )
    }
}
