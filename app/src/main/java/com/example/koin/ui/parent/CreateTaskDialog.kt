package com.example.koin.ui.parent

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import com.example.koin.data.model.Task
import com.example.koin.data.model.TaskStatus
import com.example.koin.databinding.DialogCreateTaskBinding
import java.util.UUID

class CreateTaskDialog(
    context: Context,
    private val familyId: String,
    private val onTaskCreated: (Task) -> Unit
) : Dialog(context) {

    private lateinit var binding: DialogCreateTaskBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogCreateTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSaveTask.setOnClickListener {
            val title = binding.etTaskTitle.text.toString().trim()
            val rewardStr = binding.etTaskReward.text.toString().trim()

            if (title.isEmpty()) return@setOnClickListener

            val reward = rewardStr.toDoubleOrNull() ?: 50.0

            val task = Task(
                id = "task_" + UUID.randomUUID().toString().take(8),
                familyId = familyId,
                title = title,
                rewardAmount = reward,
                status = TaskStatus.PENDING
            )

            onTaskCreated(task)
            dismiss()
        }
    }
}
