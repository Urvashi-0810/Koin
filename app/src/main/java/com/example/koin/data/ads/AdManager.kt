package com.example.koin.data.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import com.example.koin.R
import com.example.koin.data.subscriptions.RevenueCatManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdManager {

    private const val TAG = "AdManager"

    // Google AdMob Test Ad Unit IDs
    const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    const val TEST_NATIVE_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"

    private var rewardedAd: RewardedAd? = null
    private var isRewardedAdLoading = false

    // --- BANNER AD INTEGRATION ---
    fun loadBannerAd(context: Context, container: ViewGroup): AdView {
        if (RevenueCatManager.isAdFreeUser) {
            container.visibility = View.GONE
            return AdView(context)
        }

        container.visibility = View.VISIBLE
        container.removeAllViews()
        val adView = AdView(context).apply {
            setAdSize(AdSize.BANNER)
            adUnitId = TEST_BANNER_AD_UNIT_ID
        }

        container.addView(adView)

        val adRequest = AdRequest.Builder().build()
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Log.d(TAG, "Banner ad loaded successfully")
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(TAG, "Banner ad failed to load: ${adError.message}")
            }
        }
        adView.loadAd(adRequest)
        return adView
    }

    // --- REWARDED VIDEO AD INTEGRATION ---
    fun preloadRewardedAd(context: Context, onLoaded: (() -> Unit)? = null) {
        if (RevenueCatManager.isAdFreeUser) return
        if (rewardedAd != null || isRewardedAdLoading) return

        isRewardedAdLoading = true
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            context,
            TEST_REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isRewardedAdLoading = false
                    Log.d(TAG, "Rewarded video ad loaded successfully")
                    onLoaded?.invoke()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedAd = null
                    isRewardedAdLoading = false
                    Log.e(TAG, "Rewarded video ad failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    fun isRewardedAdReady(): Boolean = rewardedAd != null || RevenueCatManager.isAdFreeUser

    fun showRewardedAd(activity: Activity, onUserEarnedReward: (rewardAmount: Int) -> Unit) {
        if (RevenueCatManager.isAdFreeUser) {
            Toast.makeText(activity, "👑 Pro Member! Instant +50 Bonus Points Claimed!", Toast.LENGTH_SHORT).show()
            onUserEarnedReward(50)
            return
        }

        val currentAd = rewardedAd
        if (currentAd != null) {
            currentAd.show(activity) { rewardItem ->
                Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                onUserEarnedReward(rewardItem.amount)
            }
            rewardedAd = null
            preloadRewardedAd(activity)
        } else {
            Toast.makeText(activity, "Ad loading. Rewarding bonus points!", Toast.LENGTH_SHORT).show()
            onUserEarnedReward(50)
            preloadRewardedAd(activity)
        }
    }

    // --- NATIVE AD INTEGRATION ---
    fun loadNativeAd(context: Context, container: ViewGroup) {
        if (RevenueCatManager.isAdFreeUser) {
            container.visibility = View.GONE
            return
        }

        container.visibility = View.VISIBLE
        val adLoader = AdLoader.Builder(context, TEST_NATIVE_AD_UNIT_ID)
            .forNativeAd { nativeAd ->
                container.removeAllViews()
                val inflater = LayoutInflater.from(context)
                val adView = inflater.inflate(R.layout.layout_native_ad, container, false) as NativeAdView
                populateNativeAdView(nativeAd, adView)
                container.addView(adView)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG, "Native ad failed to load: ${adError.message}")
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
        val headlineView = adView.findViewById<TextView>(R.id.ad_headline)
        val bodyView = adView.findViewById<TextView>(R.id.ad_body)
        val callToActionView = adView.findViewById<Button>(R.id.ad_call_to_action)
        val iconView = adView.findViewById<ImageView>(R.id.ad_app_icon)
        val advertiserView = adView.findViewById<TextView>(R.id.ad_advertiser)
        val starsView = adView.findViewById<RatingBar>(R.id.ad_stars)

        adView.headlineView = headlineView
        adView.bodyView = bodyView
        adView.callToActionView = callToActionView
        adView.iconView = iconView
        adView.advertiserView = advertiserView
        adView.starRatingView = starsView

        headlineView.text = nativeAd.headline

        if (nativeAd.body == null) {
            bodyView.visibility = View.INVISIBLE
        } else {
            bodyView.visibility = View.VISIBLE
            bodyView.text = nativeAd.body
        }

        if (nativeAd.callToAction == null) {
            callToActionView.visibility = View.INVISIBLE
        } else {
            callToActionView.visibility = View.VISIBLE
            callToActionView.text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            iconView.visibility = View.GONE
        } else {
            iconView.setImageDrawable(nativeAd.icon?.drawable)
            iconView.visibility = View.VISIBLE
        }

        if (nativeAd.starRating == null) {
            starsView.visibility = View.GONE
        } else {
            starsView.rating = nativeAd.starRating!!.toFloat()
            starsView.visibility = View.VISIBLE
        }

        if (nativeAd.advertiser == null) {
            advertiserView.visibility = View.INVISIBLE
        } else {
            advertiserView.text = nativeAd.advertiser
            advertiserView.visibility = View.VISIBLE
        }

        adView.setNativeAd(nativeAd)
    }
}
