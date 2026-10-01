package com.example.koin.data.repository

import android.content.Context
import com.example.koin.data.local.LocalStorage
import com.example.koin.data.model.AppCategoryType
import com.example.koin.data.model.DailyScore
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ScoreRepository(context: Context) {

    private val localStorage = LocalStorage(context)

    suspend fun saveDailyScore(dailyScore: DailyScore): Result<DailyScore> {
        return try {
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("daily_scores").document(dailyScore.id).set(dailyScore).await()
            } catch (e: Exception) {
                // Local fallback
            }

            localStorage.saveDailyScore(dailyScore)
            Result.success(dailyScore)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDailyScoresForChild(childId: String): List<DailyScore> {
        return try {
            val localList = localStorage.getDailyScores(childId)
            try {
                val db = FirebaseFirestore.getInstance()
                val snapshot = db.collection("daily_scores")
                    .whereEqualTo("childId", childId)
                    .get().await()

                val remoteList = mutableListOf<DailyScore>()
                val docs = snapshot.documents
                for (i in 0 until docs.size) {
                    val doc = docs[i] as com.google.firebase.firestore.DocumentSnapshot
                    val item = doc.toObject(DailyScore::class.java) as? DailyScore
                    if (item != null) remoteList.add(item)
                }

                if (remoteList.isNotEmpty()) {
                    remoteList.forEach { localStorage.saveDailyScore(it) }
                    return remoteList
                }
            } catch (e: Exception) {
                // Local fallback
            }
            localList
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveAppCategoryOverride(familyId: String, packageName: String, categoryType: AppCategoryType): Result<Boolean> {
        return try {
            localStorage.saveAppCategoryOverride(familyId, packageName, categoryType)
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("app_overrides").document("${familyId}_$packageName")
                    .set(mapOf("familyId" to familyId, "packageName" to packageName, "categoryType" to categoryType.name))
                    .await()
            } catch (e: Exception) {
                // Local fallback
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAppCategoryOverrides(familyId: String): Map<String, AppCategoryType> {
        return localStorage.getAppCategoryOverrides(familyId)
    }
}
