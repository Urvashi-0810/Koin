package com.example.koin.ui.child

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import com.example.koin.databinding.DialogStreakCelebrationBinding

class StreakCelebrationDialog(
    context: Context,
    private val onDismiss: () -> Unit
) : Dialog(context) {

    private lateinit var binding: DialogStreakCelebrationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogStreakCelebrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCelebrate.setOnClickListener {
            onDismiss()
            dismiss()
        }
    }
}
