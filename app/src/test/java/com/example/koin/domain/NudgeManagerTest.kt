package com.example.koin.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NudgeManagerTest {

    @Test
    fun testNudgeLimit_Max4Per12Hours() {
        val now = System.currentTimeMillis()

        // 3 nudges within 12 hours -> allowed
        val (canSend1, _) = NudgeManager.canSendNudge(
            currentCount = 3,
            lastNudgeTimestamp = now - 1000 * 60 * 60, // 1 hour ago
            nowTimestamp = now
        )
        assertTrue(canSend1)

        // 4 nudges within 12 hours -> blocked
        val (canSend2, error2) = NudgeManager.canSendNudge(
            currentCount = 4,
            lastNudgeTimestamp = now - 1000 * 60 * 60, // 1 hour ago
            nowTimestamp = now
        )
        assertFalse(canSend2)
        assertTrue(error2!!.contains("Maximum 4 nudges reached"))

        // 4 nudges but 13 hours passed -> count resets, allowed
        val (canSend3, _) = NudgeManager.canSendNudge(
            currentCount = 4,
            lastNudgeTimestamp = now - 13 * 60 * 60 * 1000L, // 13 hours ago
            nowTimestamp = now
        )
        assertTrue(canSend3)
    }

    @Test
    fun testUpdateNudgeMetadata() {
        val now = System.currentTimeMillis()
        val (newCount, newTimestamp) = NudgeManager.getUpdatedNudgeMetadata(
            currentCount = 2,
            lastNudgeTimestamp = now - 1000,
            nowTimestamp = now
        )
        assertEquals(3, newCount)
        assertEquals(now, newTimestamp)
    }
}
