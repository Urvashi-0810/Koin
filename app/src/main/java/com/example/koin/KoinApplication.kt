package com.example.koin

import android.app.Application
import com.example.koin.data.subscriptions.RevenueCatManager
import com.google.android.gms.ads.MobileAds

class KoinApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize Google Mobile Ads (AdMob) SDK
        MobileAds.initialize(this) {
            // SDK Initialization complete
        }

        // Initialize RevenueCat SDK
        RevenueCatManager.init(this)
    }
}
