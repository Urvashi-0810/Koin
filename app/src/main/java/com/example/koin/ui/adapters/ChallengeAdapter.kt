package com.example.koin.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.koin.data.model.Challenge
import com.example.koin.data.model.ChallengeStatus
import com.example.koin.databinding.ItemChallengeBinding

class ChallengeAdapter(
    private var challenges: List<Challenge> = emptyList(),
    private val isParentView: Boolean,
    private val onActionClicked: (Challenge) -> Unit
) : RecyclerView.Adapter<ChallengeAdapter.ChallengeViewHolder>() {

    fun updateData(newChallenges: List<Challenge>) {
        this.challenges = newChallenges
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeViewHolder {
        val binding = ItemChallengeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChallengeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChallengeViewHolder, position: Int) {
        holder.bind(challenges[position])
    }

    override fun getItemCount(): Int = challenges.size

    inner class ChallengeViewHolder(private val binding: ItemChallengeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(challenge: Challenge) {
            binding.tvTitle.text = challenge.title
            binding.tvDescription.text = challenge.description

            val rewardText = if (challenge.rewardAmount > 0) {
                "+₹${challenge.rewardAmount.toInt()}"
            } else {
                "+${challenge.rewardPercentageBonus.toInt()}%"
            }
            binding.tvReward.text = rewardText

            binding.pbProgress.max = challenge.totalDaysOrTarget
            binding.pbProgress.progress = challenge.completedDays
            binding.tvProgressText.text = "${challenge.completedDays}/${challenge.totalDaysOrTarget} days"

            if (isParentView) {
                when (challenge.status) {
                    ChallengeStatus.PENDING_ACCEPTANCE -> {
                        binding.btnAction.text = "Pending Acceptance"
                        binding.btnAction.isEnabled = false
                    }
                    ChallengeStatus.ACTIVE -> {
                        binding.btnAction.text = "Mark Progress ✓"
                        binding.btnAction.isEnabled = true
                    }
                    ChallengeStatus.COMPLETED -> {
                        binding.btnAction.text = "Completed 🎉"
                        binding.btnAction.isEnabled = false
                    }
                    ChallengeStatus.DECLINED -> {
                        binding.btnAction.text = "Incomplete"
                        binding.btnAction.isEnabled = false
                    }
                }
            } else {
                when (challenge.status) {
                    ChallengeStatus.PENDING_ACCEPTANCE -> {
                        binding.btnAction.text = "Accept Challenge"
                        binding.btnAction.isEnabled = true
                    }
                    ChallengeStatus.ACTIVE -> {
                        binding.btnAction.text = "Send Nudge 🔔"
                        binding.btnAction.isEnabled = true
                    }
                    ChallengeStatus.COMPLETED -> {
                        binding.btnAction.text = "Completed 🎉"
                        binding.btnAction.isEnabled = false
                    }
                    ChallengeStatus.DECLINED -> {
                        binding.btnAction.text = "Incomplete"
                        binding.btnAction.isEnabled = false
                    }
                }
            }

            binding.btnAction.setOnClickListener { onActionClicked(challenge) }
        }
    }
}
