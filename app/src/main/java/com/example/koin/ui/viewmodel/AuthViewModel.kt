package com.example.koin.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.koin.data.model.Family
import com.example.koin.data.model.User
import com.example.koin.data.model.UserRole
import com.example.koin.data.repository.AuthRepository
import com.example.koin.data.repository.FamilyRepository
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application)
    private val familyRepository = FamilyRepository(application)

    private val _currentUser = MutableLiveData<User?>(authRepository.getCurrentUser())
    val currentUser: LiveData<User?> = _currentUser

    private val _authState = MutableLiveData<AuthState>(AuthState.Idle)
    val authState: LiveData<AuthState> = _authState

    private val _familyState = MutableLiveData<FamilyState>(FamilyState.Idle)
    val familyState: LiveData<FamilyState> = _familyState

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        data class Success(val user: User) : AuthState()
        data class Error(val message: String) : AuthState()
    }

    sealed class FamilyState {
        object Idle : FamilyState()
        object Loading : FamilyState()
        data class Success(val family: Family) : FamilyState()
        data class Error(val message: String) : FamilyState()
    }

    fun registerParent(name: String, email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.registerParent(name, email, pass)
            if (result.isSuccess) {
                val user = result.getOrNull()!!
                _currentUser.value = user
                _authState.value = AuthState.Success(user)
                // Initialize family for parent
                setupFamilyForParent(user)
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }

    fun loginParent(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.loginParent(email, pass)
            if (result.isSuccess) {
                val user = result.getOrNull()!!
                _currentUser.value = user
                _authState.value = AuthState.Success(user)
                setupFamilyForParent(user)
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    private fun setupFamilyForParent(parent: User) {
        viewModelScope.launch {
            _familyState.value = FamilyState.Loading
            val res = familyRepository.getOrCreateFamilyForParent(parent)
            if (res.isSuccess) {
                _familyState.value = FamilyState.Success(res.getOrNull()!!)
            } else {
                _familyState.value = FamilyState.Error(res.exceptionOrNull()?.message ?: "Family setup failed")
            }
        }
    }

    fun linkChildWithFamilyCode(childName: String, code: String) {
        viewModelScope.launch {
            _familyState.value = FamilyState.Loading
            val familyRes = familyRepository.findFamilyByCode(code.trim())
            if (familyRes.isFailure) {
                _familyState.value = FamilyState.Error(familyRes.exceptionOrNull()?.message ?: "Invalid code")
                return@launch
            }

            val family = familyRes.getOrNull()!!
            val childRes = authRepository.registerChild(childName, family.id)
            if (childRes.isFailure) {
                _familyState.value = FamilyState.Error("Failed to create child profile.")
                return@launch
            }

            val child = childRes.getOrNull()!!
            _currentUser.value = child
            _authState.value = AuthState.Success(child)

            val updatedFamilyRes = familyRepository.addChildToFamily(family, child)
            if (updatedFamilyRes.isSuccess) {
                _familyState.value = FamilyState.Success(updatedFamilyRes.getOrNull()!!)
            } else {
                _familyState.value = FamilyState.Error(updatedFamilyRes.exceptionOrNull()?.message ?: "Link failed")
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _currentUser.value = null
        _authState.value = AuthState.Idle
        _familyState.value = FamilyState.Idle
    }
}
