package com.example.koin.data.repository

import android.content.Context
import com.example.koin.data.local.LocalStorage
import com.example.koin.data.model.User
import com.example.koin.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AuthRepository(context: Context) {

    private val localStorage = LocalStorage(context)

    fun getCurrentUser(): User? = localStorage.getCurrentUser()

    fun logout() {
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (e: Exception) {
            // Ignored if offline
        }
        localStorage.clearUserSession()
    }

    suspend fun registerParent(name: String, email: String, password: String): Result<User> {
        return try {
            var userId = "parent_" + UUID.randomUUID().toString().take(8)

            try {
                val auth = FirebaseAuth.getInstance()
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                authResult.user?.uid?.let { userId = it }
            } catch (e: Exception) {
                // Local fallback
            }

            val user = User(
                id = userId,
                name = name,
                email = email,
                role = UserRole.PARENT
            )

            try {
                FirebaseFirestore.getInstance().collection("users").document(userId).set(user).await()
            } catch (e: Exception) {
                // Local fallback
            }

            localStorage.saveCurrentUser(user)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginParent(email: String, password: String): Result<User> {
        return try {
            var userId = ""
            val parentName = email.substringBefore('@').replaceFirstChar { it.uppercase() }

            try {
                val auth = FirebaseAuth.getInstance()
                auth.signInWithEmailAndPassword(email, password).await()
                userId = auth.currentUser?.uid ?: ""

                if (userId.isNotEmpty()) {
                    val doc = FirebaseFirestore.getInstance().collection("users").document(userId).get().await()
                    val remoteUser = doc.toObject(User::class.java)
                    if (remoteUser != null) {
                        localStorage.saveCurrentUser(remoteUser)
                        return Result.success(remoteUser)
                    }
                }
            } catch (e: Exception) {
                // Local fallback
            }

            if (userId.isEmpty()) {
                userId = "parent_" + Math.abs(email.hashCode())
            }

            val user = User(
                id = userId,
                name = parentName,
                email = email,
                role = UserRole.PARENT
            )
            localStorage.saveCurrentUser(user)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerChild(childName: String, familyId: String): Result<User> {
        return try {
            val childId = "child_" + UUID.randomUUID().toString().take(8)
            val user = User(
                id = childId,
                name = childName,
                role = UserRole.CHILD,
                familyId = familyId
            )

            try {
                FirebaseFirestore.getInstance().collection("users").document(childId).set(user).await()
            } catch (e: Exception) {
                // Local fallback
            }

            localStorage.saveCurrentUser(user)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
