package com.example.koin.ui.parent

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.koin.R
import com.example.koin.data.model.User
import com.example.koin.databinding.FragmentParentDashboardBinding
import com.example.koin.ui.adapters.ChildCardAdapter
import com.example.koin.ui.viewmodel.ChallengeTaskViewModel
import com.example.koin.ui.viewmodel.ParentDashboardViewModel

class ParentDashboardFragment : Fragment() {

    private var _binding: FragmentParentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ParentDashboardViewModel by activityViewModels()
    private val challengeTaskViewModel: ChallengeTaskViewModel by activityViewModels()

    private lateinit var adapter: ChildCardAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentParentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()

        binding.btnEditAllowance.setOnClickListener {
            showEditAllowanceDialog()
        }

        binding.btnCopyCode.setOnClickListener {
            val code = viewModel.family.value?.familyCode ?: binding.tvFamilyCode.text.toString()
            if (code.isNotEmpty() && code != "------") {
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Family Code", code)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(requireContext(), R.string.code_copied_toast, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.loadData()
    }

    private fun setupRecyclerView() {
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

        binding.rvChildren.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChildren.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.family.observe(viewLifecycleOwner) { family ->
            if (family != null) {
                binding.tvFamilyCode.text = family.familyCode
                binding.tvBaseAllowance.text = "Base Monthly Allowance: ${family.currencySymbol}${family.baseAllowance.toInt()}"
            }
        }

        viewModel.childrenScores.observe(viewLifecycleOwner) { scoresMap ->
            val family = viewModel.family.value
            if (family != null) {
                // Mock user list if children list in family
                val children = family.childIds.map { id ->
                    User(id = id, name = "Child $id", familyId = family.id, currentStreak = 5)
                }
                adapter.updateData(children, scoresMap)
            }
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearToast()
            }
        }

        challengeTaskViewModel.toastMessage.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                challengeTaskViewModel.clearToast()
            }
        }
    }

    private fun showEditAllowanceDialog() {
        val etInput = EditText(requireContext()).apply {
            hint = "e.g. 1000"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Base Monthly Allowance")
            .setView(etInput)
            .setPositiveButton("Save") { _, _ ->
                val text = etInput.text.toString().trim()
                if (text.isNotEmpty()) {
                    val newBase = text.toDoubleOrNull() ?: 1000.0
                    viewModel.updateBaseAllowance(newBase)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
