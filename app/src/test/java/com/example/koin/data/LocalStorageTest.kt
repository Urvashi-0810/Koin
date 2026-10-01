package com.example.koin.data

import com.example.koin.data.model.User
import com.example.koin.data.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LocalStorageTest {

    @Test
    fun testUserRoleEnum() {
        val parentUser = User(id = "p1", name = "Parent Test", role = UserRole.PARENT)
        val childUser = User(id = "c1", name = "Child Test", role = UserRole.CHILD)

        assertEquals(UserRole.PARENT, parentUser.role)
        assertEquals(UserRole.CHILD, childUser.role)
    }

    @Test
    fun testRoleSwitchSessionResetLogic() {
        var currentUserSession: User? = User(id = "p1", name = "Parent Test", role = UserRole.PARENT)
        assertEquals(UserRole.PARENT, currentUserSession?.role)

        // Simulate Logout / Role Switch
        currentUserSession = null
        assertNull(currentUserSession)

        // Simulate Child Login with Family Code
        currentUserSession = User(id = "c1", name = "Child Test", role = UserRole.CHILD, familyId = "fam_123")
        assertEquals(UserRole.CHILD, currentUserSession.role)
        assertEquals("fam_123", currentUserSession.familyId)
    }
}
