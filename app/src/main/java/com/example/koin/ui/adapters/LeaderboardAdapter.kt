package com.example.koin.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.koin.data.model.User
import com.example.koin.databinding.ItemLeaderboardBinding

class LeaderboardAdapter(
    private var children: List<User> = emptyList()
) : RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder>() {

    fun updateData(newChildren: List<User>) {
        this.children = newChildren.sortedByDescending { it.totalBonusPercentage }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val binding = ItemLeaderboardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LeaderboardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        holder.bind(children[position], position + 1)
    }

    override fun getItemCount(): Int = children.size

    inner class LeaderboardViewHolder(private val binding: ItemLeaderboardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(child: User, rank: Int) {
            binding.tvRank.text = rank.toString()
            binding.tvName.text = child.name
            binding.tvBonus.text = "+${child.totalBonusPercentage.toInt()}% Bonus"

            val badgeText = if (child.badges.isNotEmpty()) {
                child.badges.joinToString(" • ")
            } else {
                "🏆 0 Challenges Completed"
            }
            binding.tvBadges.text = badgeText
        }
    }
}
