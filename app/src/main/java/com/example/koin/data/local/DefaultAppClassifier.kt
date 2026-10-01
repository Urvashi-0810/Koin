package com.example.koin.data.local

import com.example.koin.data.model.AppCategoryType

object DefaultAppClassifier {

    private val socialMediaPackages = setOf(
        "com.instagram.android",
        "com.snapchat.android",
        "com.twitter.android",
        "com.zhiliaoapp.musically", // TikTok
        "com.facebook.katana",
        "com.facebook.orca",
        "com.reddit.frontpage",
        "com.pinterest",
        "com.linkedin.android",
        "com.instagram.barcelona" // Threads
    )

    private val streamingPackages = setOf(
        "com.google.android.youtube",
        "com.netflix.mediaclient",
        "com.amazon.avod.thirdpartyclient",
        "com.disney.disneyplus",
        "in.startv.hotstar",
        "tv.twitch.android.app",
        "com.spotify.music",
        "com.google.android.apps.youtube.music"
    )

    private val productivityPackages = setOf(
        "com.duolingo",
        "notion.id",
        "org.khanacademy.android",
        "com.google.android.apps.docs",
        "com.google.android.apps.sheets",
        "com.google.android.apps.slides",
        "com.google.android.apps.classroom",
        "com.quizlet.quizletandroid",
        "org.coursera.android",
        "com.google.android.calendar",
        "com.google.android.keep"
    )

    fun getCategoryType(packageName: String): AppCategoryType {
        val lowerPkg = packageName.lowercase()
        return when {
            socialMediaPackages.any { lowerPkg.contains(it) } -> AppCategoryType.SOCIAL_MEDIA
            streamingPackages.any { lowerPkg.contains(it) } -> AppCategoryType.STREAMING
            productivityPackages.any { lowerPkg.contains(it) } -> AppCategoryType.PRODUCTIVITY
            else -> AppCategoryType.NEUTRAL
        }
    }
}
