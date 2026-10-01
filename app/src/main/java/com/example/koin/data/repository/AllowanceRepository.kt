package com.example.koin.data.repository

import android.content.Context
import com.example.koin.data.local.LocalStorage
import com.example.koin.data.model.PayoutSummary
import com.example.koin.domain.AllowanceCalculator
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AllowanceRepository(context: Context) {

    private val localStorage = LocalStorage(context)
    private val scoreRepository = ScoreRepository(context)
    private val challengeTaskRepository = ChallengeTaskRepository(context)

    suspend fun getMonthlyPayoutSummary(
        childId: String,
        childName: String,
        familyId: String,
        baseAllowance: Double,
        monthYear: String = SimpleDateFormat("MMMM yyyy", Locale.US).format(Date())
    ): PayoutSummary {
        val scores = scoreRepository.getDailyScoresForChild(childId)
        val challenges = challengeTaskRepository.getChallengesForFamily(familyId).filter { it.childId == childId }
        val tasks = challengeTaskRepository.getTasksForFamily(familyId).filter { it.childId == childId }

        val summary = AllowanceCalculator.calculateMonthlyPayout(
            childId = childId,
            childName = childName,
            monthYear = monthYear,
            baseAllowance = baseAllowance,
            dailyScores = scores,
            challenges = challenges,
            tasks = tasks
        )

        localStorage.savePayoutSummary(summary)
        return summary
    }

    suspend fun confirmPayout(payoutSummary: PayoutSummary): Result<PayoutSummary> {
        return try {
            val confirmed = payoutSummary.copy(
                isConfirmedByParent = true,
                confirmedTimestamp = System.currentTimeMillis()
            )

            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("payouts").document(confirmed.id).set(confirmed).await()
            } catch (e: Exception) {
                // Local fallback
            }

            localStorage.savePayoutSummary(confirmed)
            Result.success(confirmed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getPayoutHistory(childId: String): List<PayoutSummary> {
        return localStorage.getPayoutSummaries(childId)
    }
}
