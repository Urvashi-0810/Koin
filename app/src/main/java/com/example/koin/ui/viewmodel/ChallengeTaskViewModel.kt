package com.example.koin.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.koin.data.model.Challenge
import com.example.koin.data.model.ChallengeStatus
import com.example.koin.data.model.ParentGesture
import com.example.koin.data.model.Task
import com.example.koin.data.model.TaskStatus
import com.example.koin.data.repository.AuthRepository
import com.example.koin.data.repository.ChallengeTaskRepository
import kotlinx.coroutines.launch
import java.util.UUID

class ChallengeTaskViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application)
    private val repository = ChallengeTaskRepository(application)

    private val _challenges = MutableLiveData<List<Challenge>>()
    val challenges: LiveData<List<Challenge>> = _challenges

    private val _tasks = MutableLiveData<List<Task>>()
    val tasks: LiveData<List<Task>> = _tasks

    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> = _toastMessage

    fun loadData(familyId: String) {
        viewModelScope.launch {
            _challenges.value = repository.getChallengesForFamily(familyId)
            _tasks.value = repository.getTasksForFamily(familyId)
        }
    }

    // --- Parent Actions ---
    fun createChallenge(challenge: Challenge) {
        viewModelScope.launch {
            val res = repository.createChallenge(challenge)
            if (res.isSuccess) {
                _toastMessage.value = "Challenge created!"
                loadData(challenge.familyId)
            } else {
                _toastMessage.value = res.exceptionOrNull()?.message ?: "Failed to create challenge"
            }
        }
    }

    fun createTask(task: Task) {
        viewModelScope.launch {
            val res = repository.createTask(task)
            if (res.isSuccess) {
                _toastMessage.value = "Task created!"
                loadData(task.familyId)
            } else {
                _toastMessage.value = res.exceptionOrNull()?.message ?: "Failed to create task"
            }
        }
    }

    fun markChallengeProgressByParent(challenge: Challenge, markComplete: Boolean) {
        viewModelScope.launch {
            if (markComplete) {
                val res = repository.incrementChallengeProgress(challenge.id, challenge.familyId)
                if (res.isSuccess) {
                    _toastMessage.value = "Progress updated!"
                    loadData(challenge.familyId)
                }
            } else {
                val res = repository.updateChallengeStatus(challenge.id, challenge.familyId, ChallengeStatus.DECLINED)
                if (res.isSuccess) {
                    _toastMessage.value = "Challenge marked incomplete."
                    loadData(challenge.familyId)
                }
            }
        }
    }

    fun confirmTaskByParent(task: Task) {
        viewModelScope.launch {
            val res = repository.updateTaskStatus(task.id, task.familyId, TaskStatus.CONFIRMED_BY_PARENT)
            if (res.isSuccess) {
                _toastMessage.value = "Task reward confirmed! Added to payout."
                loadData(task.familyId)
            }
        }
    }

    fun sendParentGesture(childId: String) {
        val user = authRepository.getCurrentUser() ?: return
        val gesture = ParentGesture(
            id = "g_" + UUID.randomUUID().toString().take(8),
            childId = childId,
            parentId = user.id,
            message = "Well done! Keep up the great work! 🎉"
        )
        viewModelScope.launch {
            val res = repository.sendParentGesture(gesture)
            if (res.isSuccess) {
                _toastMessage.value = "Sent 'Well done!' to child!"
            }
        }
    }

    // --- Child Actions ---
    fun acceptChallenge(challenge: Challenge) {
        viewModelScope.launch {
            val res = repository.updateChallengeStatus(challenge.id, challenge.familyId, ChallengeStatus.ACTIVE)
            if (res.isSuccess) {
                _toastMessage.value = "Challenge accepted! Good luck!"
                loadData(challenge.familyId)
            }
        }
    }

    fun sendChallengeNudge(challenge: Challenge, proofNote: String = "") {
        viewModelScope.launch {
            val res = repository.sendChallengeNudge(challenge.id, challenge.familyId, proofNote)
            if (res.isSuccess) {
                _toastMessage.value = "Nudge sent to parent! 🔔"
                loadData(challenge.familyId)
            } else {
                _toastMessage.value = res.exceptionOrNull()?.message ?: "Could not send nudge"
            }
        }
    }

    fun sendTaskNudge(task: Task) {
        viewModelScope.launch {
            val res = repository.sendTaskNudge(task.id, task.familyId)
            if (res.isSuccess) {
                _toastMessage.value = "Task marked done & nudge sent to parent! 🔔"
                loadData(task.familyId)
            } else {
                _toastMessage.value = res.exceptionOrNull()?.message ?: "Could not send nudge"
            }
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}
