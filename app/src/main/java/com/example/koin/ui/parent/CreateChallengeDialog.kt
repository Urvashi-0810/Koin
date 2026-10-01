package com.example.koin.ui.parent

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import com.example.koin.data.model.Challenge
import com.example.koin.data.model.ChallengeStatus
import com.example.koin.data.model.ChallengeType
import com.example.koin.databinding.DialogCreateChallengeBinding
import java.util.UUID

class CreateChallengeDialog(
    context: Context,
    private val familyId: String,
    private val onChallengeCreated: (Challenge) -> Unit
) : Dialog(context) {

    private lateinit var binding: DialogCreateChallengeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogCreateChallengeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val desc = binding.etDescription.text.toString().trim()
            val rewardStr = binding.etReward.text.toString().trim()
            val daysStr = binding.etDays.text.toString().trim()

            if (title.isEmpty()) return@setOnClickListener

            val reward = rewardStr.toDoubleOrNull() ?: 500.0
            val days = daysStr.toIntOrNull() ?: 7

            val challenge = Challenge(
                id = "ch_" + UUID.randomUUID().toString().take(8),
                familyId = familyId,
                title = title,
                description = desc,
                challengeType = ChallengeType.BEHAVIOR,
                rewardAmount = reward,
                totalDaysOrTarget = days,
                completedDays = 0,
                status = ChallengeStatus.PENDING_ACCEPTANCE
            )

            onChallengeCreated(challenge)
            dismiss()
        }
    }
}
