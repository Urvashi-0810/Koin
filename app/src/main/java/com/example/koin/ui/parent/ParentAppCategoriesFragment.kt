package com.example.koin.ui.parent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.koin.data.model.AppCategory
import com.example.koin.data.model.AppCategoryType
import com.example.koin.databinding.FragmentParentAppCategoriesBinding
import com.example.koin.ui.adapters.AppCategoryAdapter
import com.example.koin.ui.viewmodel.ParentDashboardViewModel

class ParentAppCategoriesFragment : Fragment() {

    private var _binding: FragmentParentAppCategoriesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ParentDashboardViewModel by activityViewModels()
    private lateinit var adapter: AppCategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentParentAppCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AppCategoryAdapter { packageName, newCategory ->
            viewModel.reclassifyApp(packageName, newCategory)
        }

        binding.rvAppCategories.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAppCategories.adapter = adapter

        // Sample list of popular apps
        val sampleApps = listOf(
            AppCategory("com.instagram.android", "Instagram", AppCategoryType.SOCIAL_MEDIA, rawUsageMinutes = 120),
            AppCategory("com.google.android.youtube", "YouTube", AppCategoryType.STREAMING, rawUsageMinutes = 60),
            AppCategory("com.duolingo", "Duolingo", AppCategoryType.PRODUCTIVITY, rawUsageMinutes = 45),
            AppCategory("com.google.android.apps.maps", "Maps", AppCategoryType.NEUTRAL, rawUsageMinutes = 20)
        )
        adapter.updateData(sampleApps)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
