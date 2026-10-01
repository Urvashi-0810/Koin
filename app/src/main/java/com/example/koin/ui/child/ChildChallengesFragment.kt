package com.example.koin.ui.child

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.koin.data.model.ChallengeStatus
import com.example.koin.databinding.FragmentChildChallengesBinding
import com.example.koin.ui.adapters.ChallengeAdapter
import com.example.koin.ui.adapters.TaskAdapter
import com.example.koin.ui.viewmodel.ChallengeTaskViewModel
import com.example.koin.ui.viewmodel.ChildDashboardViewModel

class ChildChallengesFragment : Fragment() {

    private var _binding: FragmentChildChallengesBinding? = null
    private val binding get() = _binding!!

    private val childViewModel: ChildDashboardViewModel by activityViewModels()
    private val challengeTaskViewModel: ChallengeTaskViewModel by activityViewModels()

    private lateinit var challengeAdapter: ChallengeAdapter
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChildChallengesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        challengeAdapter = ChallengeAdapter(isParentView = false) { challenge ->
            if (challenge.status == ChallengeStatus.PENDING_ACCEPTANCE) {
                challengeTaskViewModel.acceptChallenge(challenge)
            } else {
                challengeTaskViewModel.sendChallengeNudge(challenge)
            }
        }

        taskAdapter = TaskAdapter(isParentView = false) { task ->
            challengeTaskViewModel.sendTaskNudge(task)
        }

        val user = childViewModel.currentUser.value
        if (user != null) {
            challengeTaskViewModel.loadData(user.familyId)
        }

        challengeTaskViewModel.challenges.observe(viewLifecycleOwner) { challenges ->
            challengeAdapter.updateData(challenges)
        }

        challengeTaskViewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            taskAdapter.updateData(tasks)
        }

        challengeTaskViewModel.toastMessage.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                challengeTaskViewModel.clearToast()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
