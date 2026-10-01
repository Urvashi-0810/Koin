package com.example.koin.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.koin.data.model.AppCategoryType
import com.example.koin.data.model.Challenge
import com.example.koin.data.model.DailyScore
import com.example.koin.data.model.Family
import com.example.koin.data.model.ParentGesture
import com.example.koin.data.model.PayoutSummary
import com.example.koin.data.model.Task
import com.example.koin.data.model.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LocalStorage(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("koin_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_CURRENT_USER = "current_user"
        private const val KEY_FAMILY = "family_"
        private const val KEY_DAILY_SCORES = "daily_scores_"
        private const val KEY_CHALLENGES = "challenges_"
        private const val KEY_TASKS = "tasks_"
        private const val KEY_APP_OVERRIDES = "app_overrides_"
        private const val KEY_GESTURES = "gestures_"
        private const val KEY_PAYOUTS = "payouts_"
    }

    // --- User Session ---
    fun saveCurrentUser(user: User) {
        prefs.edit().putString(KEY_CURRENT_USER, gson.toJson(user)).apply()
    }

    fun getCurrentUser(): User? {
        val json = prefs.getString(KEY_CURRENT_USER, null) ?: return null
        return try {
            gson.fromJson(json, User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun clearUserSession() {
        prefs.edit().remove(KEY_CURRENT_USER).apply()
    }

    // --- Family ---
    fun saveFamily(family: Family) {
        prefs.edit().putString(KEY_FAMILY + family.id, gson.toJson(family)).apply()
        prefs.edit().putString("family_code_" + family.familyCode, family.id).apply()
    }

    fun getFamilyById(familyId: String): Family? {
        val json = prefs.getString(KEY_FAMILY + familyId, null) ?: return null
        return try {
            gson.fromJson(json, Family::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun getFamilyByCode(code: String): Family? {
        val familyId = prefs.getString("family_code_$code", null) ?: return null
        return getFamilyById(familyId)
    }

    // --- Daily Scores ---
    fun saveDailyScore(dailyScore: DailyScore) {
        val list = getDailyScores(dailyScore.childId).toMutableList()
        list.removeAll { it.id == dailyScore.id || it.dateString == dailyScore.dateString }
        list.add(dailyScore)
        prefs.edit().putString(KEY_DAILY_SCORES + dailyScore.childId, gson.toJson(list)).apply()
    }

    fun getDailyScores(childId: String): List<DailyScore> {
        val json = prefs.getString(KEY_DAILY_SCORES + childId, null) ?: return emptyList()
        val type = object : TypeToken<List<DailyScore>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- Challenges ---
    fun saveChallenge(challenge: Challenge) {
        val list = getChallenges(challenge.familyId).toMutableList()
        list.removeAll { it.id == challenge.id }
        list.add(challenge)
        prefs.edit().putString(KEY_CHALLENGES + challenge.familyId, gson.toJson(list)).apply()
    }

    fun getChallenges(familyId: String): List<Challenge> {
        val json = prefs.getString(KEY_CHALLENGES + familyId, null) ?: return emptyList()
        val type = object : TypeToken<List<Challenge>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- Tasks ---
    fun saveTask(task: Task) {
        val list = getTasks(task.familyId).toMutableList()
        list.removeAll { it.id == task.id }
        list.add(task)
        prefs.edit().putString(KEY_TASKS + task.familyId, gson.toJson(list)).apply()
    }

    fun getTasks(familyId: String): List<Task> {
        val json = prefs.getString(KEY_TASKS + familyId, null) ?: return emptyList()
        val type = object : TypeToken<List<Task>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- App Category Overrides ---
    fun saveAppCategoryOverride(familyId: String, packageName: String, categoryType: AppCategoryType) {
        val overrides = getAppCategoryOverrides(familyId).toMutableMap()
        overrides[packageName] = categoryType
        prefs.edit().putString(KEY_APP_OVERRIDES + familyId, gson.toJson(overrides)).apply()
    }

    fun getAppCategoryOverrides(familyId: String): Map<String, AppCategoryType> {
        val json = prefs.getString(KEY_APP_OVERRIDES + familyId, null) ?: return emptyMap()
        val type = object : TypeToken<Map<String, AppCategoryType>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    // --- Parent Gestures ---
    fun saveParentGesture(gesture: ParentGesture) {
        val list = getParentGestures(gesture.childId).toMutableList()
        list.removeAll { it.id == gesture.id }
        list.add(gesture)
        prefs.edit().putString(KEY_GESTURES + gesture.childId, gson.toJson(list)).apply()
    }

    fun getParentGestures(childId: String): List<ParentGesture> {
        val json = prefs.getString(KEY_GESTURES + childId, null) ?: return emptyList()
        val type = object : TypeToken<List<ParentGesture>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- Payout Summaries ---
    fun savePayoutSummary(payout: PayoutSummary) {
        val list = getPayoutSummaries(payout.childId).toMutableList()
        list.removeAll { it.id == payout.id }
        list.add(payout)
        prefs.edit().putString(KEY_PAYOUTS + payout.childId, gson.toJson(list)).apply()
    }

    fun getPayoutSummaries(childId: String): List<PayoutSummary> {
        val json = prefs.getString(KEY_PAYOUTS + childId, null) ?: return emptyList()
        val type = object : TypeToken<List<PayoutSummary>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
