package com.example.koin.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.koin.data.model.Task
import com.example.koin.data.model.TaskStatus
import com.example.koin.databinding.ItemTaskBinding

class TaskAdapter(
    private var tasks: List<Task> = emptyList(),
    private val isParentView: Boolean,
    private val onActionClicked: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    fun updateData(newTasks: List<Task>) {
        this.tasks = newTasks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position])
    }

    override fun getItemCount(): Int = tasks.size

    inner class TaskViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.tvTaskTitle.text = task.title
            binding.tvTaskReward.text = "+₹${task.rewardAmount.toInt()}"

            if (isParentView) {
                when (task.status) {
                    TaskStatus.PENDING -> {
                        binding.tvTaskStatus.text = "In Progress"
                        binding.btnTaskAction.text = "Confirm & Add to Payout"
                        binding.btnTaskAction.isEnabled = true
                    }
                    TaskStatus.COMPLETED_BY_CHILD -> {
                        binding.tvTaskStatus.text = "Nudge Sent by Child 🔔"
                        binding.btnTaskAction.text = "Confirm & Add to Payout"
                        binding.btnTaskAction.isEnabled = true
                    }
                    TaskStatus.CONFIRMED_BY_PARENT -> {
                        binding.tvTaskStatus.text = "Confirmed & Paid 🎉"
                        binding.btnTaskAction.text = "Confirmed ✓"
                        binding.btnTaskAction.isEnabled = false
                    }
                }
            } else {
                when (task.status) {
                    TaskStatus.PENDING -> {
                        binding.tvTaskStatus.text = "Pending"
                        binding.btnTaskAction.text = "Mark Done & Nudge 🔔"
                        binding.btnTaskAction.isEnabled = true
                    }
                    TaskStatus.COMPLETED_BY_CHILD -> {
                        binding.tvTaskStatus.text = "Nudge Sent 🔔"
                        binding.btnTaskAction.text = "Nudge Sent"
                        binding.btnTaskAction.isEnabled = false
                    }
                    TaskStatus.CONFIRMED_BY_PARENT -> {
                        binding.tvTaskStatus.text = "Confirmed & Paid 🎉"
                        binding.btnTaskAction.text = "Paid ✓"
                        binding.btnTaskAction.isEnabled = false
                    }
                }
            }

            binding.btnTaskAction.setOnClickListener { onActionClicked(task) }
        }
    }
}
