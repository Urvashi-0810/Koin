package com.example.koin.ui.child

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.koin.R
import com.example.koin.data.local.UsageStatsHelper
import com.example.koin.data.model.ScoreTier
import com.example.koin.databinding.FragmentChildDashboardBinding
import com.example.koin.ui.adapters.AppCategoryAdapter
import com.example.koin.ui.viewmodel.ChildDashboardViewModel

class ChildDashboardFragment : Fragment() {

    private var _binding: FragmentChildDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChildDashboardViewModel by activityViewModels()
    private lateinit var appAdapter: AppCategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChildDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appAdapter = AppCategoryAdapter { _, _ -> }
        binding.rvTodayApps.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTodayApps.adapter = appAdapter

        binding.btnGrantPermission.setOnClickListener {
            startActivity(UsageStatsHelper.getUsagePermissionIntent())
        }

        setupObservers()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshDashboard()
    }

    private fun setupObservers() {
        viewModel.hasUsagePermission.observe(viewLifecycleOwner) { hasPermission ->
            binding.cardPermission.visibility = if (hasPermission) View.GONE else View.VISIBLE
        }

        viewModel.todayScore.observe(viewLifecycleOwner) { score ->
            if (score != null) {
                binding.tvScoreHours.text = "${score.totalWeightedHours}"
                binding.tvRawHours.text = "Raw screen time: ${score.rawHours} hrs"
                binding.tvTierBadge.text = "${score.tier.displayName} (${if (score.bonusOrDeductionPercent >= 0) "+" else ""}${score.bonusOrDeductionPercent.toInt()}%)"

                val (bgRes, textColorRes) = when (score.tier) {
                    ScoreTier.EXCELLENT -> Pair(R.drawable.bg_tier_excellent, R.color.tier_excellent)
                    ScoreTier.GOOD -> Pair(R.drawable.bg_tier_good, R.color.tier_good)
                    ScoreTier.FAIR -> Pair(R.drawable.bg_tier_fair, R.color.tier_fair)
                    ScoreTier.POOR -> Pair(R.drawable.bg_tier_poor, R.color.tier_poor)
                }

                binding.tvTierBadge.setBackgroundResource(bgRes)
                binding.tvTierBadge.setTextColor(ContextCompat.getColor(requireContext(), textColorRes))
            }
        }

        viewModel.currentStreak.observe(viewLifecycleOwner) { streak ->
            binding.tvStreakCounter.text = "🔥 $streak Consecutive Excellent Days!"
        }

        viewModel.todayApps.observe(viewLifecycleOwner) { apps ->
            appAdapter.updateData(apps)
        }

        viewModel.parentGestures.observe(viewLifecycleOwner) { gestures ->
            if (gestures.isNotEmpty()) {
                val latest = gestures.last()
                binding.cardParentGesture.visibility = View.VISIBLE
                binding.tvGestureMessage.text = "🎉 Parent says: '${latest.message}'"
            } else {
                binding.cardParentGesture.visibility = View.GONE
            }
        }

        viewModel.showStreakCelebration.observe(viewLifecycleOwner) { show ->
            if (show) {
                StreakCelebrationDialog(requireContext()) {
                    viewModel.dismissStreakCelebration()
                }.show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
