package com.example.koin.ui.parent

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.koin.databinding.ActivityPayoutSummaryBinding
import com.example.koin.ui.viewmodel.AllowanceViewModel

class ParentPayoutSummaryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPayoutSummaryBinding
    private val viewModel: AllowanceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPayoutSummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val childId = intent.getStringExtra("CHILD_ID") ?: ""
        val childName = intent.getStringExtra("CHILD_NAME") ?: "Child"
        val familyId = intent.getStringExtra("FAMILY_ID") ?: ""
        val baseAllowance = intent.getDoubleExtra("BASE_ALLOWANCE", 1000.0)

        setupObservers()

        binding.btnConfirmPayout.setOnClickListener {
            viewModel.confirmPayout()
        }

        viewModel.loadPayoutSummary(childId, childName, familyId, baseAllowance)
    }

    private fun setupObservers() {
        viewModel.payoutSummary.observe(this) { summary ->
            if (summary != null) {
                binding.tvBaseAmount.text = "₹${summary.baseAllowance.toInt()}"
                binding.tvBonusLabel.text = "Daily Score Bonus (+${summary.bonusPercentage.toInt()}%)"
                binding.tvBonusAmount.text = "+₹${(summary.baseAllowance * (summary.bonusPercentage / 100.0)).toInt()}"

                binding.tvDeductionLabel.text = "Daily Score Deduction (-${summary.deductionPercentage.toInt()}%)"
                binding.tvDeductionAmount.text = "-₹${(summary.baseAllowance * (summary.deductionPercentage / 100.0)).toInt()}"

                binding.tvChallengeAmount.text = "+₹${summary.challengeEarnings.toInt()}"
                binding.tvTaskAmount.text = "+₹${summary.taskEarnings.toInt()}"

                binding.tvFinalTotal.text = "₹${summary.finalPayout.toInt()}"

                if (summary.isConfirmedByParent) {
                    binding.btnConfirmPayout.text = "Confirmed & Recorded ✓"
                    binding.btnConfirmPayout.isEnabled = false
                }
            }
        }

        viewModel.toastMessage.observe(this) { msg ->
            msg?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearToast()
            }
        }
    }
}
