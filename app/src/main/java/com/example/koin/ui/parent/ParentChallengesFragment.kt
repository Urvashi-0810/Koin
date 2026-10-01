package com.example.koin.ui.parent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.koin.databinding.FragmentParentChallengesBinding
import com.example.koin.ui.adapters.ChallengeAdapter
import com.example.koin.ui.adapters.TaskAdapter
import com.example.koin.ui.viewmodel.ChallengeTaskViewModel
import com.example.koin.ui.viewmodel.ParentDashboardViewModel

class ParentChallengesFragment : Fragment() {

    private var _binding: FragmentParentChallengesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ParentDashboardViewModel by activityViewModels()
    private val challengeTaskViewModel: ChallengeTaskViewModel by activityViewModels()

    private lateinit var challengeAdapter: ChallengeAdapter
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentParentChallengesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        challengeAdapter = ChallengeAdapter(isParentView = true, onActionClicked = { challenge ->
            challengeTaskViewModel.markChallengeProgressByParent(challenge, markComplete = true)
        })

        taskAdapter = TaskAdapter(isParentView = true, onActionClicked = { task ->
            challengeTaskViewModel.confirmTaskByParent(task)
        })

        binding.fabAdd.setOnClickListener {
            val familyId = viewModel.family.value?.id ?: return@setOnClickListener
            CreateChallengeDialog(requireContext(), familyId) { newChallenge ->
                challengeTaskViewModel.createChallenge(newChallenge)
            }.show()
        }

        viewModel.family.observe(viewLifecycleOwner) { family ->
            if (family != null) {
                challengeTaskViewModel.loadData(family.id)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
