package com.example.koin.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.koin.data.model.PayoutSummary
import com.example.koin.data.repository.AllowanceRepository
import kotlinx.coroutines.launch

class AllowanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AllowanceRepository(application)

    private val _payoutSummary = MutableLiveData<PayoutSummary?>()
    val payoutSummary: LiveData<PayoutSummary?> = _payoutSummary

    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> = _toastMessage

    fun loadPayoutSummary(childId: String, childName: String, familyId: String, baseAllowance: Double) {
        viewModelScope.launch {
            val summary = repository.getMonthlyPayoutSummary(childId, childName, familyId, baseAllowance)
            _payoutSummary.value = summary
        }
    }

    fun confirmPayout() {
        val summary = _payoutSummary.value ?: return
        viewModelScope.launch {
            val res = repository.confirmPayout(summary)
            if (res.isSuccess) {
                _payoutSummary.value = res.getOrNull()
                _toastMessage.value = "Payout confirmed and recorded! 🎉"
            } else {
                _toastMessage.value = res.exceptionOrNull()?.message ?: "Failed to confirm payout"
            }
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}
