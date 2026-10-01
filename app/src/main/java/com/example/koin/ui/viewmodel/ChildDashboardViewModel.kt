package com.example.koin.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.koin.data.local.UsageStatsHelper
import com.example.koin.data.model.AppCategory
import com.example.koin.data.model.DailyScore
import com.example.koin.data.model.ParentGesture
import com.example.koin.data.model.ScoreTier
import com.example.koin.data.model.User
import com.example.koin.data.repository.AuthRepository
import com.example.koin.data.repository.ChallengeTaskRepository
import com.example.koin.data.repository.ScoreRepository
import com.example.koin.domain.ScoreEngine
import kotlinx.coroutines.launch

class ChildDashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application)
    private val scoreRepository = ScoreRepository(application)
    private val challengeTaskRepository = ChallengeTaskRepository(application)

    private val _currentUser = MutableLiveData<User?>(authRepository.getCurrentUser())
    val currentUser: LiveData<User?> = _currentUser

    private val _hasUsagePermission = MutableLiveData<Boolean>()
    val hasUsagePermission: LiveData<Boolean> = _hasUsagePermission

    private val _todayApps = MutableLiveData<List<AppCategory>>()
    val todayApps: LiveData<List<AppCategory>> = _todayApps

    private val _todayScore = MutableLiveData<DailyScore?>()
    val todayScore: LiveData<DailyScore?> = _todayScore

    private val _currentStreak = MutableLiveData<Int>()
    val currentStreak: LiveData<Int> = _currentStreak

    private val _showStreakCelebration = MutableLiveData<Boolean>()
    val showStreakCelebration: LiveData<Boolean> = _showStreakCelebration

    private val _parentGestures = MutableLiveData<List<ParentGesture>>()
    val parentGestures: LiveData<List<ParentGesture>> = _parentGestures

    fun refreshDashboard() {
        val user = authRepository.getCurrentUser() ?: return
        _currentUser.value = user

        val hasPermission = UsageStatsHelper.hasUsagePermission(getApplication())
        _hasUsagePermission.value = hasPermission

        viewModelScope.launch {
            val overrides = scoreRepository.getAppCategoryOverrides(user.familyId)
            val apps = if (hasPermission) {
                UsageStatsHelper.getTodayAppUsages(getApplication(), overrides)
            } else {
                emptyList()
            }
            _todayApps.value = apps

            val todayDailyScore = ScoreEngine.createDailyScore(user.id, apps)
            _todayScore.value = todayDailyScore
            scoreRepository.saveDailyScore(todayDailyScore)

            // Get history & calculate streak
            val historyScores = scoreRepository.getDailyScoresForChild(user.id)
            val streak = ScoreEngine.calculateCurrentStreak(historyScores)
            _currentStreak.value = streak

            // Check 7-day streak trigger
            if (streak == 7 && todayDailyScore.tier == ScoreTier.EXCELLENT) {
                _showStreakCelebration.value = true
            }

            // Load parent gestures
            val gestures = challengeTaskRepository.getParentGesturesForChild(user.id)
            _parentGestures.value = gestures
        }
    }

    fun dismissStreakCelebration() {
        _showStreakCelebration.value = false
    }
}
