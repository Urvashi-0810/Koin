package com.example.koin.data.repository

import android.content.Context
import com.example.koin.data.local.LocalStorage
import com.example.koin.data.model.Challenge
import com.example.koin.data.model.ChallengeStatus
import com.example.koin.data.model.ParentGesture
import com.example.koin.data.model.Task
import com.example.koin.data.model.TaskStatus
import com.example.koin.domain.NudgeManager
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ChallengeTaskRepository(context: Context) {

    private val localStorage = LocalStorage(context)

    // --- Challenges ---
    suspend fun createChallenge(challenge: Challenge): Result<Challenge> {
        return try {
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("challenges").document(challenge.id).set(challenge).await()
            } catch (e: Exception) {
                // Local fallback
            }
            localStorage.saveChallenge(challenge)
            Result.success(challenge)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getChallengesForFamily(familyId: String): List<Challenge> {
        return try {
            val localList = localStorage.getChallenges(familyId)
            try {
                val db = FirebaseFirestore.getInstance()
                val snapshot = db.collection("challenges")
                    .whereEqualTo("familyId", familyId)
                    .get().await()

                val remoteList = mutableListOf<Challenge>()
                val docs = snapshot.documents
                for (i in 0 until docs.size) {
                    val doc = docs[i] as com.google.firebase.firestore.DocumentSnapshot
                    val item = doc.toObject(Challenge::class.java) as? Challenge
                    if (item != null) remoteList.add(item)
                }

                if (remoteList.isNotEmpty()) {
                    remoteList.forEach { localStorage.saveChallenge(it) }
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

    suspend fun updateChallengeStatus(challengeId: String, familyId: String, newStatus: ChallengeStatus): Result<Challenge> {
        return try {
            val challenges = localStorage.getChallenges(familyId)
            val existing = challenges.find { it.id == challengeId }
                ?: return Result.failure(Exception("Challenge not found"))

            val updated = existing.copy(status = newStatus)
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("challenges").document(challengeId).update("status", newStatus.name).await()
            } catch (e: Exception) {
                // Local fallback
            }

            localStorage.saveChallenge(updated)
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun incrementChallengeProgress(challengeId: String, familyId: String): Result<Challenge> {
        return try {
            val challenges = localStorage.getChallenges(familyId)
            val existing = challenges.find { it.id == challengeId }
                ?: return Result.failure(Exception("Challenge not found"))

            val newCompleted = Math.min(existing.completedDays + 1, existing.totalDaysOrTarget)
            val isFullyDone = newCompleted >= existing.totalDaysOrTarget
            val newStatus = if (isFullyDone) ChallengeStatus.COMPLETED else existing.status

            val updated = existing.copy(completedDays = newCompleted, status = newStatus)

            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("challenges").document(challengeId)
                    .update(mapOf("completedDays" to newCompleted, "status" to newStatus.name)).await()
            } catch (e: Exception) {
                // Local fallback
            }

            localStorage.saveChallenge(updated)
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendChallengeNudge(challengeId: String, familyId: String, proofNote: String = ""): Result<Challenge> {
        return try {
            val challenges = localStorage.getChallenges(familyId)
            val existing = challenges.find { it.id == challengeId }
                ?: return Result.failure(Exception("Challenge not found"))

            val (canSend, errorMessage) = NudgeManager.canSendNudge(
                currentCount = existing.nudgeCount,
                lastNudgeTimestamp = existing.lastNudgeTimestamp
            )

            if (!canSend) {
                return Result.failure(Exception(errorMessage ?: "Nudge limit reached."))
            }

            val (newCount, newTimestamp) = NudgeManager.getUpdatedNudgeMetadata(
                currentCount = existing.nudgeCount,
                lastNudgeTimestamp = existing.lastNudgeTimestamp
            )

            val updated = existing.copy(
                nudgeCount = newCount,
                lastNudgeTimestamp = newTimestamp,
                childProofNote = proofNote.ifBlank { existing.childProofNote }
            )

            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("challenges").document(challengeId)
                    .update(
                        mapOf(
                            "nudgeCount" to newCount,
                            "lastNudgeTimestamp" to newTimestamp,
                            "childProofNote" to updated.childProofNote
                        )
                    ).await()
            } catch (e: Exception) {
                // Local fallback
            }

            localStorage.saveChallenge(updated)
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Tasks ---
    suspend fun createTask(task: Task): Result<Task> {
        return try {
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("tasks").document(task.id).set(task).await()
            } catch (e: Exception) {
                // Local fallback
            }
            localStorage.saveTask(task)
            Result.success(task)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTasksForFamily(familyId: String): List<Task> {
        return try {
            val localList = localStorage.getTasks(familyId)
            try {
                val db = FirebaseFirestore.getInstance()
                val snapshot = db.collection("tasks")
                    .whereEqualTo("familyId", familyId)
                    .get().await()

                val remoteList = mutableListOf<Task>()
                val docs = snapshot.documents
                for (i in 0 until docs.size) {
                    val doc = docs[i] as com.google.firebase.firestore.DocumentSnapshot
                    val item = doc.toObject(Task::class.java) as? Task
                    if (item != null) remoteList.add(item)
                }

                if (remoteList.isNotEmpty()) {
                    remoteList.forEach { localStorage.saveTask(it) }
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

    suspend fun updateTaskStatus(taskId: String, familyId: String, newStatus: TaskStatus): Result<Task> {
        return try {
            val tasks = localStorage.getTasks(familyId)
            val existing = tasks.find { it.id == taskId }
                ?: return Result.failure(Exception("Task not found"))

            val updated = existing.copy(status = newStatus)
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("tasks").document(taskId).update("status", newStatus.name).await()
            } catch (e: Exception) {
                // Local fallback
            }

            localStorage.saveTask(updated)
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendTaskNudge(taskId: String, familyId: String): Result<Task> {
        return try {
            val tasks = localStorage.getTasks(familyId)
            val existing = tasks.find { it.id == taskId }
                ?: return Result.failure(Exception("Task not found"))

            val (canSend, errorMessage) = NudgeManager.canSendNudge(
                currentCount = existing.nudgeCount,
                lastNudgeTimestamp = existing.lastNudgeTimestamp
            )

            if (!canSend) {
                return Result.failure(Exception(errorMessage ?: "Nudge limit reached."))
            }

            val (newCount, newTimestamp) = NudgeManager.getUpdatedNudgeMetadata(
                currentCount = existing.nudgeCount,
                lastNudgeTimestamp = existing.lastNudgeTimestamp
            )

            val updated = existing.copy(
                nudgeCount = newCount,
                lastNudgeTimestamp = newTimestamp,
                status = TaskStatus.COMPLETED_BY_CHILD
            )

            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("tasks").document(taskId)
                    .update(
                        mapOf(
                            "nudgeCount" to newCount,
                            "lastNudgeTimestamp" to newTimestamp,
                            "status" to TaskStatus.COMPLETED_BY_CHILD.name
                        )
                    ).await()
            } catch (e: Exception) {
                // Local fallback
            }

            localStorage.saveTask(updated)
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Parent Gesture / Well Done ---
    suspend fun sendParentGesture(gesture: ParentGesture): Result<ParentGesture> {
        return try {
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("parent_gestures").document(gesture.id).set(gesture).await()
            } catch (e: Exception) {
                // Local fallback
            }
            localStorage.saveParentGesture(gesture)
            Result.success(gesture)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getParentGesturesForChild(childId: String): List<ParentGesture> {
        return localStorage.getParentGestures(childId)
    }
}
