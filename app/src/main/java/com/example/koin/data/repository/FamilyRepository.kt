package com.example.koin.data.repository

import android.content.Context
import com.example.koin.data.local.LocalStorage
import com.example.koin.data.model.Family
import com.example.koin.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID
import kotlin.random.Random

class FamilyRepository(context: Context) {

    private val localStorage = LocalStorage(context)

    companion object {
        const val MAX_CHILDREN_PER_FAMILY = 5
    }

    suspend fun getOrCreateFamilyForParent(parentUser: User): Result<Family> {
        return try {
            if (parentUser.familyId.isNotEmpty()) {
                val existing = localStorage.getFamilyById(parentUser.familyId)
                if (existing != null) return Result.success(existing)
            }

            // Generate 6-digit code
            val familyCode = Random.nextInt(100000, 999999).toString()
            val familyId = "fam_" + UUID.randomUUID().toString().take(8)

            val family = Family(
                id = familyId,
                familyCode = familyCode,
                parentId = parentUser.id,
                parentName = parentUser.name,
                baseAllowance = 1000.0,
                currencySymbol = "₹"
            )

            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("families").document(familyId).set(family).await()
                db.collection("family_codes").document(familyCode).set(mapOf("familyId" to familyId)).await()
            } catch (e: Exception) {
                // Local fallback
            }

            localStorage.saveFamily(family)

            val updatedParent = parentUser.copy(familyId = familyId)
            localStorage.saveCurrentUser(updatedParent)

            Result.success(family)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun findFamilyByCode(familyCode: String): Result<Family> {
        return try {
            var family: Family? = localStorage.getFamilyByCode(familyCode)

            if (family == null) {
                try {
                    val db = FirebaseFirestore.getInstance()
                    val codeDoc = db.collection("family_codes").document(familyCode).get().await()
                    val familyId = codeDoc.getString("familyId")
                    if (!familyId.isNullOrEmpty()) {
                        val familyDoc = db.collection("families").document(familyId).get().await()
                        family = familyDoc.toObject(Family::class.java) as? Family
                    }
                } catch (e: Exception) {
                    // Local fallback
                }
            }

            if (family == null) {
                return Result.failure(Exception("Invalid 6-digit family code. Please check and try again."))
            }

            if (family.childIds.size >= MAX_CHILDREN_PER_FAMILY) {
                return Result.failure(Exception("This family already has the maximum limit of $MAX_CHILDREN_PER_FAMILY children."))
            }

            localStorage.saveFamily(family)
            Result.success(family)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addChildToFamily(family: Family, childUser: User): Result<Family> {
        return try {
            if (family.childIds.size >= MAX_CHILDREN_PER_FAMILY) {
                return Result.failure(Exception("Maximum limit of $MAX_CHILDREN_PER_FAMILY children reached."))
            }

            if (!family.childIds.contains(childUser.id)) {
                val updatedChildIds = family.childIds + childUser.id
                val updatedFamily = family.copy(childIds = updatedChildIds)

                try {
                    val db = FirebaseFirestore.getInstance()
                    db.collection("families").document(family.id).update("childIds", updatedChildIds).await()
                } catch (e: Exception) {
                    // Local fallback
                }

                localStorage.saveFamily(updatedFamily)
                return Result.success(updatedFamily)
            }

            Result.success(family)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateBaseAllowance(familyId: String, newBaseAllowance: Double, currencySymbol: String = "₹"): Result<Family> {
        return try {
            val existing = localStorage.getFamilyById(familyId)
                ?: return Result.failure(Exception("Family not found"))

            val updated = existing.copy(baseAllowance = newBaseAllowance, currencySymbol = currencySymbol)

            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("families").document(familyId)
                    .update(mapOf("baseAllowance" to newBaseAllowance, "currencySymbol" to currencySymbol)).await()
            } catch (e: Exception) {
                // Local fallback
            }

            localStorage.saveFamily(updated)
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
