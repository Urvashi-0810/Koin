package com.example.koin.ui.parent

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.koin.data.model.User
import com.example.koin.databinding.FragmentParentChildrenBinding
import com.example.koin.ui.adapters.ChildCardAdapter
import com.example.koin.ui.viewmodel.ChallengeTaskViewModel
import com.example.koin.ui.viewmodel.ParentDashboardViewModel

class ParentChildrenFragment : Fragment() {

    private var _binding: FragmentParentChildrenBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ParentDashboardViewModel by activityViewModels()
    private val challengeTaskViewModel: ChallengeTaskViewModel by activityViewModels()

    private lateinit var adapter: ChildCardAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentParentChildrenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ChildCardAdapter(
            onViewSummaryClicked = { child ->
                val intent = Intent(requireContext(), ParentPayoutSummaryActivity::class.java).apply {
                    putExtra("CHILD_ID", child.id)
                    putExtra("CHILD_NAME", child.name)
                    putExtra("FAMILY_ID", child.familyId)
                    putExtra("BASE_ALLOWANCE", viewModel.family.value?.baseAllowance ?: 1000.0)
                }
                startActivity(intent)
            },
            onSendGestureClicked = { child ->
                challengeTaskViewModel.sendParentGesture(child.id)
            }
        )

        binding.rvChildrenList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChildrenList.adapter = adapter

        viewModel.family.observe(viewLifecycleOwner) { family ->
            if (family != null) {
                val children = family.childIds.map { id ->
                    User(id = id, name = "Child $id", familyId = family.id, currentStreak = 5)
                }
                adapter.updateData(children, viewModel.childrenScores.value ?: emptyMap())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
