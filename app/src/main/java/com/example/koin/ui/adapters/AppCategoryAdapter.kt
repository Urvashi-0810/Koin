package com.example.koin.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.koin.data.model.AppCategory
import com.example.koin.data.model.AppCategoryType
import com.example.koin.databinding.ItemAppCategoryBinding

class AppCategoryAdapter(
    private var apps: List<AppCategory> = emptyList(),
    private val onCategoryChanged: (packageName: String, newCategory: AppCategoryType) -> Unit
) : RecyclerView.Adapter<AppCategoryAdapter.AppViewHolder>() {

    fun updateData(newApps: List<AppCategory>) {
        this.apps = newApps
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemAppCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(apps[position])
    }

    override fun getItemCount(): Int = apps.size

    inner class AppViewHolder(private val binding: ItemAppCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(app: AppCategory) {
            binding.tvAppName.text = app.appName
            binding.tvPackageName.text = "${app.packageName} (${app.rawUsageMinutes} mins)"

            val categories = AppCategoryType.values()
            val adapter = ArrayAdapter(
                binding.root.context,
                android.R.layout.simple_spinner_item,
                categories.map { "${it.displayName} (${it.multiplier}x)" }
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCategory.adapter = adapter

            binding.spinnerCategory.setSelection(app.categoryType.ordinal)

            binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selectedCategory = categories[position]
                    if (selectedCategory != app.categoryType) {
                        onCategoryChanged(app.packageName, selectedCategory)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }
}
