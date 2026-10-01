package com.example.koin.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.koin.R
import com.example.koin.data.model.DailyScore
import com.example.koin.data.model.ScoreTier
import com.example.koin.data.model.User
import com.example.koin.databinding.ItemChildCardBinding

class ChildCardAdapter(
    private var children: List<User> = emptyList(),
    private var scores: Map<String, DailyScore> = emptyMap(),
    private val onViewSummaryClicked: (User) -> Unit,
    private val onSendGestureClicked: (User) -> Unit
) : RecyclerView.Adapter<ChildCardAdapter.ChildViewHolder>() {

    fun updateData(newChildren: List<User>, newScores: Map<String, DailyScore>) {
        this.children = newChildren
        this.scores = newScores
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
        val binding = ItemChildCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChildViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChildViewHolder, position: Int) {
        holder.bind(children[position])
    }

    override fun getItemCount(): Int = children.size

    inner class ChildViewHolder(private val binding: ItemChildCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(child: User) {
            binding.tvChildName.text = child.name
            val score = scores[child.id]

            if (score != null) {
                binding.tvWeightedHours.text = "${score.totalWeightedHours} weighted hrs today"
                binding.tvTierBadge.text = "${score.tier.displayName} (${if (score.bonusOrDeductionPercent >= 0) "+" else ""}${score.bonusOrDeductionPercent.toInt()}%)"

                val (bgRes, textColorRes) = when (score.tier) {
                    ScoreTier.EXCELLENT -> Pair(R.drawable.bg_tier_excellent, R.color.tier_excellent)
                    ScoreTier.GOOD -> Pair(R.drawable.bg_tier_good, R.color.tier_good)
                    ScoreTier.FAIR -> Pair(R.drawable.bg_tier_fair, R.color.tier_fair)
                    ScoreTier.POOR -> Pair(R.drawable.bg_tier_poor, R.color.tier_poor)
                }

                binding.tvTierBadge.setBackgroundResource(bgRes)
                binding.tvTierBadge.setTextColor(ContextCompat.getColor(binding.root.context, textColorRes))
            } else {
                binding.tvWeightedHours.text = "No score recorded today"
                binding.tvTierBadge.text = "GOOD (0%)"
                binding.tvTierBadge.setBackgroundResource(R.drawable.bg_tier_good)
            }

            binding.tvStreak.text = "🔥 ${child.currentStreak}-day Excellent streak"

            binding.btnViewSummary.setOnClickListener { onViewSummaryClicked(child) }
            binding.btnSendGesture.setOnClickListener { onSendGestureClicked(child) }
        }
    }
}
