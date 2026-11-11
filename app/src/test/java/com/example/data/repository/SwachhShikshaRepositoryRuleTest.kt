package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SwachhShikshaRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createSchoolAndBus_validPayload_succeedsAndEmitsInFlow() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = SwachhShikshaRepository(firestore)

        val schoolResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createSchool(
                schoolCode = "SCH-0001",
                name = "School A",
                address = "New Delhi",
                ownerName = "Rajesh Sharma",
                ownerMobile = "+91 9811011223",
                loginId = "owner@schoola.in",
                busCount = 1L,
                isActive = true
            )
        }
        assertTrue(schoolResult.isSuccess)
        val schoolId = schoolResult.getOrThrow()

        val emittedSchools = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeSchools().first { list -> list.any { it.id == schoolId } }
        }
        assertTrue(emittedSchools.any { it.id == schoolId })
    }

    @Test
    fun getSchoolById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val aliceRepo = SwachhShikshaRepository(firestore)
        val schoolId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createSchool(
                schoolCode = "SCH-0001",
                name = "Alice Private School",
                address = "Dwarka",
                ownerName = "Alice",
                ownerMobile = "9811000000",
                loginId = "alice@school.in"
            ).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = SwachhShikshaRepository(firestore)
        val bobResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getSchoolById(schoolId)
        }
        assertTrue(bobResult.isFailure)
        val firestoreException = bobResult.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull(firestoreException)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException?.code)
    }

    @Test
    fun observeSchools_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = SwachhShikshaRepository(firestore)

        var caught: Throwable? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeSchools("unauth_uid").first()
            }
        } catch (e: Throwable) {
            caught = e
        }
        assertNotNull(caught)
        val firestoreException = caught?.unwrapFirestoreException()
        assertNotNull(firestoreException)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException?.code)
    }

    private companion object {
        const val ALICE_EMAIL = "alice@swachhshiksha.test"
        const val BOB_EMAIL = "bob@swachhshiksha.test"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
