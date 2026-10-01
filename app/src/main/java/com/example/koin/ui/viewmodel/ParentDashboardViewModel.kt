package com.example.koin.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.koin.data.model.AppCategoryType
import com.example.koin.data.model.DailyScore
import com.example.koin.data.model.Family
import com.example.koin.data.model.User
import com.example.koin.data.repository.AuthRepository
import com.example.koin.data.repository.FamilyRepository
import com.example.koin.data.repository.ScoreRepository
import kotlinx.coroutines.launch

class ParentDashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application)
    private val familyRepository = FamilyRepository(application)
    private val scoreRepository = ScoreRepository(application)

    private val _family = MutableLiveData<Family?>()
    val family: LiveData<Family?> = _family

    private val _childrenScores = MutableLiveData<Map<String, DailyScore>>()
    val childrenScores: LiveData<Map<String, DailyScore>> = _childrenScores

    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> = _toastMessage

    fun loadData() {
        val currentUser = authRepository.getCurrentUser() ?: return
        viewModelScope.launch {
            val familyRes = familyRepository.getOrCreateFamilyForParent(currentUser)
            if (familyRes.isSuccess) {
                val fam = familyRes.getOrNull()!!
                _family.value = fam
                loadChildrenScores(fam.childIds)
            }
        }
    }

    private suspend fun loadChildrenScores(childIds: List<String>) {
        val scoresMap = mutableMapOf<String, DailyScore>()
        for (childId in childIds) {
            val scores = scoreRepository.getDailyScoresForChild(childId)
            val latest = scores.maxByOrNull { it.dateString }
            if (latest != null) {
                scoresMap[childId] = latest
            }
        }
        _childrenScores.value = scoresMap
    }

    fun updateBaseAllowance(newAllowance: Double) {
        val fam = _family.value ?: return
        viewModelScope.launch {
            val res = familyRepository.updateBaseAllowance(fam.id, newAllowance)
            if (res.isSuccess) {
                _family.value = res.getOrNull()
                _toastMessage.value = "Base allowance updated to ${fam.currencySymbol}$newAllowance"
            } else {
                _toastMessage.value = res.exceptionOrNull()?.message ?: "Failed to update allowance"
            }
        }
    }

    fun reclassifyApp(packageName: String, newCategory: AppCategoryType) {
        val fam = _family.value ?: return
        viewModelScope.launch {
            val res = scoreRepository.saveAppCategoryOverride(fam.id, packageName, newCategory)
            if (res.isSuccess) {
                _toastMessage.value = "Updated $packageName to ${newCategory.displayName}"
            }
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}
