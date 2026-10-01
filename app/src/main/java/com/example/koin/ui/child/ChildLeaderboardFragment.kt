package com.example.koin.ui.child

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.koin.data.model.User
import com.example.koin.databinding.FragmentChildLeaderboardBinding
import com.example.koin.ui.adapters.LeaderboardAdapter
import com.example.koin.ui.viewmodel.ChildDashboardViewModel

class ChildLeaderboardFragment : Fragment() {

    private var _binding: FragmentChildLeaderboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChildDashboardViewModel by activityViewModels()
    private lateinit var adapter: LeaderboardAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChildLeaderboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = LeaderboardAdapter()
        binding.rvLeaderboard.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLeaderboard.adapter = adapter

        val user = viewModel.currentUser.value
        if (user != null) {
            val sampleSiblings = listOf(
                user.copy(name = user.name.ifEmpty { "Arjun" }, totalBonusPercentage = 20.0, badges = listOf("🏆 10 Challenges", "🔥 7-Day Streak Hero")),
                User(id = "s1", name = "Ananya", totalBonusPercentage = 15.0, badges = listOf("🏆 5 Challenges"))
            )
            adapter.updateData(sampleSiblings)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
