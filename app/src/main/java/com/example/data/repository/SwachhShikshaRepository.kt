package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.AppRole
import com.example.data.model.BusRecord
import com.example.data.model.DemoSeedData
import com.example.data.model.GpsProviderConfig
import com.example.data.model.SchoolRecord
import com.example.data.model.UserProfile
import com.example.util.OperationType
import com.example.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class SwachhShikshaRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeUserProfile(userId: String = requireUserId()): Flow<UserProfile?> = flow {
        val path = "users/$userId"
        emitAll(
            db.collection("users").document(userId)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    } else {
                        null
                    }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    fun observeSchools(userId: String = requireUserId()): Flow<List<SchoolRecord>> = flow {
        val path = "schools"
        emitAll(
            db.collection("schools")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(SchoolRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.schoolCode }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeBuses(userId: String = requireUserId()): Flow<List<BusRecord>> = flow {
        val path = "buses"
        emitAll(
            db.collection("buses")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(BusRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.busNumber }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeGpsConfigs(userId: String = requireUserId()): Flow<List<GpsProviderConfig>> = flow {
        val path = "gps_configs"
        emitAll(
            db.collection("gps_configs")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(GpsProviderConfig::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.busId }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun ensureSeededWorkspace() {
        val uid = requireUserId()
        val userDocRef = db.collection("users").document(uid)
        try {
            val existingProfile = userDocRef.get().await()
            if (!existingProfile.exists()) {
                val displayName = auth.currentUser?.displayName?.takeIf { it.isNotBlank() } ?: "Parent & Admin Demo"
                val profilePayload = mapOf(
                    "userId" to uid,
                    "displayName" to displayName.take(100),
                    "phone" to "+91 98110 22334",
                    "activeRole" to AppRole.PARENT.code,
                    "selectedSchoolId" to "SCH-0001",
                    "selectedBusId" to "BUS-101",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                userDocRef.set(profilePayload).await()
            }

            val existingSchools = db.collection("schools")
                .whereEqualTo("userId", uid)
                .limit(1)
                .get()
                .await()

            if (existingSchools.isEmpty) {
                for (school in DemoSeedData.defaultSchools(uid)) {
                    val docId = "${uid}_${school.id}".replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(120)
                    val schoolRef = db.collection("schools").document(docId)
                    schoolRef.set(
                        mapOf(
                            "userId" to uid,
                            "schoolCode" to school.schoolCode,
                            "name" to school.name,
                            "address" to school.address,
                            "ownerName" to school.ownerName,
                            "ownerMobile" to school.ownerMobile,
                            "loginId" to school.loginId,
                            "busCount" to school.busCount,
                            "isActive" to school.isActive,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }

                for (bus in DemoSeedData.defaultBuses(uid)) {
                    val docId = "${uid}_${bus.id}".replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(120)
                    val busRef = db.collection("buses").document(docId)
                    busRef.set(
                        mapOf(
                            "userId" to uid,
                            "schoolId" to bus.schoolId,
                            "schoolName" to bus.schoolName,
                            "busNumber" to bus.busNumber,
                            "registrationNumber" to bus.registrationNumber,
                            "displayName" to bus.displayName,
                            "driverName" to bus.driverName,
                            "driverPhone" to bus.driverPhone,
                            "assignedGpsDeviceId" to bus.assignedGpsDeviceId,
                            "gpsProviderName" to bus.gpsProviderName,
                            "gpsStatus" to bus.gpsStatus,
                            "speedKmh" to bus.speedKmh,
                            "latitude" to bus.latitude,
                            "longitude" to bus.longitude,
                            "routeSummary" to bus.routeSummary,
                            "lastUpdatedText" to bus.lastUpdatedText,
                            "isDemo" to bus.isDemo,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }

                for (cfg in DemoSeedData.defaultGpsConfigs(uid)) {
                    val docId = "${uid}_${cfg.id}".replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(120)
                    val cfgRef = db.collection("gps_configs").document(docId)
                    cfgRef.set(
                        mapOf(
                            "userId" to uid,
                            "schoolId" to cfg.schoolId,
                            "busId" to cfg.busId,
                            "providerName" to cfg.providerName,
                            "connectionMethod" to cfg.connectionMethod,
                            "apiBaseUrl" to cfg.apiBaseUrl,
                            "deviceId" to cfg.deviceId,
                            "status" to cfg.status,
                            "lastSyncText" to cfg.lastSyncText,
                            "lastLocationText" to cfg.lastLocationText,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "users/$uid")
            throw e
        }
    }

    suspend fun saveUserProfile(
        displayName: String,
        phone: String,
        activeRole: AppRole,
        selectedSchoolId: String,
        selectedBusId: String
    ) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        try {
            val snap = docRef.get().await()
            if (!snap.exists()) {
                docRef.set(
                    mapOf(
                        "userId" to uid,
                        "displayName" to displayName.trim().ifEmpty { "User" }.take(100),
                        "phone" to phone.trim().take(30),
                        "activeRole" to activeRole.code,
                        "selectedSchoolId" to selectedSchoolId.take(128),
                        "selectedBusId" to selectedBusId.take(128),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.update(
                    mapOf(
                        "displayName" to displayName.trim().ifEmpty { "User" }.take(100),
                        "phone" to phone.trim().take(30),
                        "activeRole" to activeRole.code,
                        "selectedSchoolId" to selectedSchoolId.take(128),
                        "selectedBusId" to selectedBusId.take(128),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun createSchool(
        schoolCode: String,
        name: String,
        address: String,
        ownerName: String,
        ownerMobile: String,
        loginId: String,
        busCount: Long = 0L,
        isActive: Boolean = true
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docId = "sch_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("schools").document(docId)
        val payload = mapOf(
            "userId" to uid,
            "schoolCode" to schoolCode.trim().take(40),
            "name" to name.trim().take(120),
            "address" to address.trim().take(250),
            "ownerName" to ownerName.trim().take(100),
            "ownerMobile" to ownerMobile.trim().take(30),
            "loginId" to loginId.trim().take(80),
            "busCount" to busCount.coerceIn(0L, 1000L),
            "isActive" to isActive,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateSchool(school: SchoolRecord): Result<Unit> = runCatching {
        requireUserId()
        require(school.id.isNotBlank()) { "School ID must not be blank" }
        val docRef = db.collection("schools").document(school.id)
        try {
            docRef.update(
                mapOf(
                    "schoolCode" to school.schoolCode.trim().take(40),
                    "name" to school.name.trim().take(120),
                    "address" to school.address.trim().take(250),
                    "ownerName" to school.ownerName.trim().take(100),
                    "ownerMobile" to school.ownerMobile.trim().take(30),
                    "loginId" to school.loginId.trim().take(80),
                    "busCount" to school.busCount.coerceIn(0L, 1000L),
                    "isActive" to school.isActive,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteSchool(schoolId: String): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("schools").document(schoolId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    suspend fun getSchoolById(schoolId: String): Result<SchoolRecord?> = runCatching {
        val docRef = db.collection("schools").document(schoolId)
        try {
            val snap = docRef.get().await()
            snap.toObject(SchoolRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    suspend fun createBus(
        schoolId: String,
        schoolName: String,
        busNumber: String,
        registrationNumber: String,
        displayName: String,
        driverName: String,
        driverPhone: String,
        assignedGpsDeviceId: String,
        gpsProviderName: String,
        gpsStatus: String,
        routeSummary: String,
        latitude: Double = 28.5672,
        longitude: Double = 77.2100,
        speedKmh: Long = 0L,
        isDemo: Boolean = true
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docId = "bus_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("buses").document(docId)
        val payload = mapOf(
            "userId" to uid,
            "schoolId" to schoolId.trim().take(128),
            "schoolName" to schoolName.trim().take(120),
            "busNumber" to busNumber.trim().take(40),
            "registrationNumber" to registrationNumber.trim().take(40),
            "displayName" to displayName.trim().take(100),
            "driverName" to driverName.trim().take(100),
            "driverPhone" to driverPhone.trim().take(30),
            "assignedGpsDeviceId" to assignedGpsDeviceId.trim().take(80),
            "gpsProviderName" to gpsProviderName.trim().take(80),
            "gpsStatus" to gpsStatus,
            "speedKmh" to speedKmh.coerceIn(0L, 200L),
            "latitude" to latitude.coerceIn(-90.0, 90.0),
            "longitude" to longitude.coerceIn(-180.0, 180.0),
            "routeSummary" to routeSummary.trim().ifEmpty { "Main Route -> School Campus" }.take(200),
            "lastUpdatedText" to if (isDemo) "Just added (DEMO)" else "Live Driver GPS",
            "isDemo" to isDemo,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateBus(bus: BusRecord): Result<Unit> = runCatching {
        requireUserId()
        require(bus.id.isNotBlank()) { "Bus ID must not be blank" }
        val docRef = db.collection("buses").document(bus.id)
        try {
            docRef.update(
                mapOf(
                    "schoolId" to bus.schoolId.trim().take(128),
                    "schoolName" to bus.schoolName.trim().take(120),
                    "busNumber" to bus.busNumber.trim().take(40),
                    "registrationNumber" to bus.registrationNumber.trim().take(40),
                    "displayName" to bus.displayName.trim().take(100),
                    "driverName" to bus.driverName.trim().take(100),
                    "driverPhone" to bus.driverPhone.trim().take(30),
                    "assignedGpsDeviceId" to bus.assignedGpsDeviceId.trim().take(80),
                    "gpsProviderName" to bus.gpsProviderName.trim().take(80),
                    "gpsStatus" to bus.gpsStatus,
                    "speedKmh" to bus.speedKmh.coerceIn(0L, 200L),
                    "latitude" to bus.latitude.coerceIn(-90.0, 90.0),
                    "longitude" to bus.longitude.coerceIn(-180.0, 180.0),
                    "routeSummary" to bus.routeSummary.trim().ifEmpty { "School Route" }.take(200),
                    "lastUpdatedText" to bus.lastUpdatedText.take(100),
                    "isDemo" to bus.isDemo,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteBus(busId: String): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("buses").document(busId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    suspend fun saveGpsConfig(
        existingId: String?,
        schoolId: String,
        busId: String,
        providerName: String,
        connectionMethod: String,
        apiBaseUrl: String,
        deviceId: String,
        status: String,
        lastSyncText: String,
        lastLocationText: String
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val isNew = existingId.isNullOrBlank()
        val docId = if (isNew) {
            "gps_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        } else {
            existingId!!
        }
        val docRef = db.collection("gps_configs").document(docId)
        try {
            if (isNew) {
                docRef.set(
                    mapOf(
                        "userId" to uid,
                        "schoolId" to schoolId.trim().take(128),
                        "busId" to busId.trim().take(128),
                        "providerName" to providerName.trim().take(100),
                        "connectionMethod" to connectionMethod.trim().take(80),
                        "apiBaseUrl" to apiBaseUrl.trim().take(250),
                        "deviceId" to deviceId.trim().take(80),
                        "status" to status,
                        "lastSyncText" to lastSyncText.trim().take(100),
                        "lastLocationText" to lastLocationText.trim().take(120),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.update(
                    mapOf(
                        "schoolId" to schoolId.trim().take(128),
                        "busId" to busId.trim().take(128),
                        "providerName" to providerName.trim().take(100),
                        "connectionMethod" to connectionMethod.trim().take(80),
                        "apiBaseUrl" to apiBaseUrl.trim().take(250),
                        "deviceId" to deviceId.trim().take(80),
                        "status" to status,
                        "lastSyncText" to lastSyncText.trim().take(100),
                        "lastLocationText" to lastLocationText.trim().take(120),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            docId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(
                e,
                if (isNew) OperationType.CREATE else OperationType.UPDATE,
                docRef.path
            )
            throw e
        }
    }
}
