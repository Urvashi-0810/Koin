package com.example.koin.data.subscriptions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RevenueCatManagerTest {

    @Test
    fun testRevenueCatConstants() {
        assertEquals("goog_sample_key_koin_pro", RevenueCatManager.REVENUECAT_API_KEY)
        assertEquals("ad_free", RevenueCatManager.ENTITLEMENT_AD_FREE)
    }

    @Test
    fun testAdFreeTestingToggle() {
        RevenueCatManager.setAdFreeForTesting(false)
        assertFalse(RevenueCatManager.isAdFreeUser)

        RevenueCatManager.setAdFreeForTesting(true)
        assertTrue(RevenueCatManager.isAdFreeUser)

        RevenueCatManager.setAdFreeForTesting(false)
        assertFalse(RevenueCatManager.isAdFreeUser)
    }
}
