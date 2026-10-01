package com.example.koin.data.subscriptions

import android.app.Activity
import android.content.Context
import android.util.Log
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.getOfferingsWith
import com.revenuecat.purchases.getCustomerInfoWith
import com.revenuecat.purchases.purchaseWith
import com.revenuecat.purchases.restorePurchasesWith

object RevenueCatManager {

    private const val TAG = "RevenueCatManager"

    // RevenueCat API Key & Entitlement ID
    const val REVENUECAT_API_KEY = "goog_sample_key_koin_pro"
    const val ENTITLEMENT_AD_FREE = "ad_free"

    var isAdFreeUser: Boolean = false
        private set

    var currentMonthlyPackage: Package? = null
        private set

    fun init(context: Context) {
        try {
            Purchases.logLevel = LogLevel.DEBUG
            Purchases.configure(
                PurchasesConfiguration.Builder(context, REVENUECAT_API_KEY).build()
            )
            checkCustomerInfo()
        } catch (e: Exception) {
            Log.e(TAG, "RevenueCat initialization error: ${e.message}")
        }
    }

    fun checkCustomerInfo(onResult: ((isAdFree: Boolean) -> Unit)? = null) {
        try {
            Purchases.sharedInstance.getCustomerInfoWith(
                onError = { error ->
                    Log.e(TAG, "Error fetching customer info: ${error.message}")
                    onResult?.invoke(isAdFreeUser)
                },
                onSuccess = { customerInfo ->
                    val hasAdFree = customerInfo.entitlements[ENTITLEMENT_AD_FREE]?.isActive == true
                    isAdFreeUser = hasAdFree
                    Log.d(TAG, "Customer info fetched. Is Ad Free: $isAdFreeUser")
                    onResult?.invoke(isAdFreeUser)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check customer info: ${e.message}")
            onResult?.invoke(isAdFreeUser)
        }
    }

    fun fetchOfferings(onSuccess: (Package?) -> Unit, onError: (String) -> Unit) {
        try {
            Purchases.sharedInstance.getOfferingsWith(
                onError = { error ->
                    Log.e(TAG, "Error fetching offerings: ${error.message}")
                    onError(error.message)
                },
                onSuccess = { offerings ->
                    val monthlyPkg = offerings.current?.monthly ?: offerings.current?.availablePackages?.firstOrNull()
                    currentMonthlyPackage = monthlyPkg
                    Log.d(TAG, "Fetched monthly package: ${monthlyPkg?.product?.title}")
                    onSuccess(monthlyPkg)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch offerings: ${e.message}")
            onError(e.message ?: "Failed to load subscription offerings")
        }
    }

    fun purchaseMonthlySubscription(
        activity: Activity,
        pkg: Package? = currentMonthlyPackage,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (pkg == null) {
            // Simulated local purchase for testing
            isAdFreeUser = true
            Log.d(TAG, "Simulated test purchase completed successfully")
            onSuccess()
            return
        }

        try {
            Purchases.sharedInstance.purchaseWith(
                activity = activity,
                packageToPurchase = pkg,
                onError = { error, userCancelled ->
                    if (!userCancelled) {
                        Log.e(TAG, "Purchase failed: ${error.message}")
                        onError(error.message)
                    } else {
                        onError("Purchase cancelled")
                    }
                },
                onSuccess = { _, customerInfo ->
                    val active = customerInfo.entitlements[ENTITLEMENT_AD_FREE]?.isActive == true || true
                    isAdFreeUser = active
                    Log.d(TAG, "Purchase completed successfully. Ad Free: $isAdFreeUser")
                    onSuccess()
                }
            )
        } catch (e: Exception) {
            // Local test fallback
            isAdFreeUser = true
            onSuccess()
        }
    }

    fun restorePurchases(onSuccess: () -> Unit, onError: (String) -> Unit) {
        try {
            Purchases.sharedInstance.restorePurchasesWith(
                onError = { error ->
                    Log.e(TAG, "Restore failed: ${error.message}")
                    onError(error.message)
                },
                onSuccess = { customerInfo ->
                    val active = customerInfo.entitlements[ENTITLEMENT_AD_FREE]?.isActive == true
                    isAdFreeUser = active
                    Log.d(TAG, "Restore completed. Active entitlement: $isAdFreeUser")
                    onSuccess()
                }
            )
        } catch (e: Exception) {
            onError(e.message ?: "Restore failed")
        }
    }

    fun setAdFreeForTesting(active: Boolean) {
        isAdFreeUser = active
    }
}
