package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppRole
import com.example.data.model.BusRecord
import com.example.data.model.ChatMessage
import com.example.data.model.DemoSeedData
import com.example.data.model.GpsProviderConfig
import com.example.data.model.SchoolRecord
import com.example.data.model.UserProfile
import com.example.data.repository.SwachhShikshaRepository
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

sealed interface DataUiState<out T> {
    data object Loading : DataUiState<Nothing>
    data class Success<T>(val data: T) : DataUiState<T>
    data class Error(val message: String) : DataUiState<Nothing>
}

class SwachhShikshaViewModel(
    private val repository: SwachhShikshaRepository,
    private val currentUserId: String
) : ViewModel() {

    private val _activeRole = MutableStateFlow(AppRole.PARENT)
    val activeRole: StateFlow<AppRole> = _activeRole.asStateFlow()

    private val _selectedSchoolCode = MutableStateFlow("SCH-0001")
    val selectedSchoolCode: StateFlow<String> = _selectedSchoolCode.asStateFlow()

    private val _selectedBusNumber = MutableStateFlow("BUS-101")
    val selectedBusNumber: StateFlow<String> = _selectedBusNumber.asStateFlow()

    private val _ownerTenantSchoolCode = MutableStateFlow("SCH-0001")
    val ownerTenantSchoolCode: StateFlow<String> = _ownerTenantSchoolCode.asStateFlow()

    private val _simulatedRouteProgress = MutableStateFlow(0.22f)
    val simulatedRouteProgress: StateFlow<Float> = _simulatedRouteProgress.asStateFlow()

    private val _statusBannerMessage = MutableStateFlow<String?>(null)
    val statusBannerMessage: StateFlow<String?> = _statusBannerMessage.asStateFlow()

    private val _isOtpVerifiedInDemo = MutableStateFlow(true)
    val isOtpVerifiedInDemo: StateFlow<Boolean> = _isOtpVerifiedInDemo.asStateFlow()

    private val _parentPhoneNumber = MutableStateFlow("+91 98110 22334")
    val parentPhoneNumber: StateFlow<String> = _parentPhoneNumber.asStateFlow()

    private val _driverTripActive = MutableStateFlow(false)
    val driverTripActive: StateFlow<Boolean> = _driverTripActive.asStateFlow()

    private val _driverBusNumber = MutableStateFlow("BUS-101")
    val driverBusNumber: StateFlow<String> = _driverBusNumber.asStateFlow()

    private val _driverLastCoords = MutableStateFlow("Waiting for trip start...")
    val driverLastCoords: StateFlow<String> = _driverLastCoords.asStateFlow()

    // Gemini Multi-Turn Chat & Route Grounding Assistant
    private val _chatMessages = MutableStateFlow(
        listOf(
            ChatMessage(
                id = "welcome_1",
                isUser = false,
                text = "Namaste! I am the Swachh Shiksha Smart Bus & Route Assistant (powered by Gemini 3.8 Flash). Ask me about bus stops, school campus routes, landmark directions, or Universal GPS adapter troubleshooting!",
                timestampLabel = "Ready"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiGenerating = MutableStateFlow(false)
    val isAiGenerating: StateFlow<Boolean> = _isAiGenerating.asStateFlow()

    private val aiModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            systemInstruction = content {
                text(
                    """
                    You are the Swachh Shiksha Smart School Bus & Route Assistant for a multi-school GPS tracking platform in India.
                    - When a Parent asks: Give clear, reassuring, simple answers about school bus safety, route landmarks, pickup stops (e.g., Green Park, Hauz Khas, Dwarka, Greater Noida, Jaipur), and what GPS states mean (Connected, Disconnected, Location Updating).
                    - When a School Owner asks: Help with bus fleet assignments, driver safety checklists, and connecting existing GPS trackers.
                    - When a Super Admin asks: Provide technical guidance on the Universal GPS Integration Manager adapters (REST API, Webhook, Teltonika/GT06 Gateway Protocol, Device ID Mapping, and Driver Mobile GPS).
                    Always include accurate geographic & landmark context for Indian cities and school zones.
                    """.trimIndent()
                )
            }
        )
    }

    private val chatSession by lazy {
        aiModel.startChat(
            history = listOf(
                content("user") { text("Hello Swachh Shiksha Assistant.") },
                content("model") {
                    text("Namaste! How can I help you with school bus live tracking, route landmarks, or GPS device management today?")
                }
            )
        )
    }

    val userProfileState: StateFlow<DataUiState<UserProfile?>> = repository.observeUserProfile(currentUserId)
        .map<UserProfile?, DataUiState<UserProfile?>> { profile ->
            if (profile != null) {
                _activeRole.value = AppRole.fromCode(profile.activeRole)
                if (profile.selectedSchoolId.isNotBlank()) {
                    _selectedSchoolCode.value = profile.selectedSchoolId
                }
                if (profile.selectedBusId.isNotBlank()) {
                    _selectedBusNumber.value = profile.selectedBusId
                }
                if (profile.phone.isNotBlank()) {
                    _parentPhoneNumber.value = profile.phone
                }
            }
            DataUiState.Success(profile)
        }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            emit(DataUiState.Error(rootError.message ?: "Failed to load profile"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = DataUiState.Loading
        )

    val schoolsState: StateFlow<DataUiState<List<SchoolRecord>>> = repository.observeSchools(currentUserId)
        .map<List<SchoolRecord>, DataUiState<List<SchoolRecord>>> { list ->
            DataUiState.Success(list.ifEmpty { DemoSeedData.defaultSchools(currentUserId) })
        }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            emit(DataUiState.Error(rootError.message ?: "Failed to observe schools"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = DataUiState.Success(DemoSeedData.defaultSchools(currentUserId))
        )

    val busesState: StateFlow<DataUiState<List<BusRecord>>> = repository.observeBuses(currentUserId)
        .map<List<BusRecord>, DataUiState<List<BusRecord>>> { list ->
            DataUiState.Success(list.ifEmpty { DemoSeedData.defaultBuses(currentUserId) })
        }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            emit(DataUiState.Error(rootError.message ?: "Failed to observe buses"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = DataUiState.Success(DemoSeedData.defaultBuses(currentUserId))
        )

    val gpsConfigsState: StateFlow<DataUiState<List<GpsProviderConfig>>> = repository.observeGpsConfigs(currentUserId)
        .map<List<GpsProviderConfig>, DataUiState<List<GpsProviderConfig>>> { list ->
            DataUiState.Success(list.ifEmpty { DemoSeedData.defaultGpsConfigs(currentUserId) })
        }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            emit(DataUiState.Error(rootError.message ?: "Failed to observe GPS configs"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = DataUiState.Success(DemoSeedData.defaultGpsConfigs(currentUserId))
        )

    init {
        viewModelScope.launch {
            try {
                repository.ensureSeededWorkspace()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Seed check warning: ${e.message}")
            }
        }
        // Smooth animated bus coordinate progression for DEMO connected buses
        viewModelScope.launch {
            while (isActive) {
                delay(2200L)
                val next = _simulatedRouteProgress.value + 0.045f
                _simulatedRouteProgress.value = if (next > 0.96f) 0.08f else next
            }
        }
    }

    fun advanceSimulatedBusManually() {
        val next = _simulatedRouteProgress.value + 0.12f
        _simulatedRouteProgress.value = if (next > 0.96f) 0.10f else next
    }

    fun clearBanner() {
        _statusBannerMessage.value = null
    }

    fun switchRole(role: AppRole) {
        _activeRole.value = role
        viewModelScope.launch {
            try {
                repository.saveUserProfile(
                    displayName = "Swachh Shiksha User",
                    phone = _parentPhoneNumber.value,
                    activeRole = role,
                    selectedSchoolId = _selectedSchoolCode.value,
                    selectedBusId = _selectedBusNumber.value
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Role sync warning: ${e.message}")
            }
        }
    }

    fun selectParentSchoolAndBus(schoolCode: String, busNumber: String) {
        _selectedSchoolCode.value = schoolCode
        _selectedBusNumber.value = busNumber
        _statusBannerMessage.value = "Selected $schoolCode • $busNumber"
        viewModelScope.launch {
            try {
                repository.saveUserProfile(
                    displayName = "Parent Account",
                    phone = _parentPhoneNumber.value,
                    activeRole = _activeRole.value,
                    selectedSchoolId = schoolCode,
                    selectedBusId = busNumber
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Selection sync warning: ${e.message}")
            }
        }
    }

    fun updateParentPhoneAndVerifyDemoOtp(phone: String, otp: String): Boolean {
        return if (phone.trim().length >= 10 && otp.trim().length >= 4) {
            _parentPhoneNumber.value = phone.trim()
            _isOtpVerifiedInDemo.value = true
            _statusBannerMessage.value = "DEMO Mobile OTP Verified for $phone"
            viewModelScope.launch {
                try {
                    repository.saveUserProfile(
                        displayName = "Parent Account",
                        phone = phone.trim(),
                        activeRole = _activeRole.value,
                        selectedSchoolId = _selectedSchoolCode.value,
                        selectedBusId = _selectedBusNumber.value
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Phone save warning: ${e.message}")
                }
            }
            true
        } else {
            _statusBannerMessage.value = "Please enter a valid 10-digit mobile number and 4-6 digit DEMO OTP."
            false
        }
    }

    fun setOwnerTenantSchool(schoolCode: String) {
        _ownerTenantSchoolCode.value = schoolCode
        _statusBannerMessage.value = "Switched School Owner view to tenant $schoolCode"
    }

    // Super Admin: Add / Edit / Toggle / Delete School
    fun saveSchool(
        existingSchool: SchoolRecord?,
        schoolCode: String,
        name: String,
        address: String,
        ownerName: String,
        ownerMobile: String,
        loginId: String,
        isActive: Boolean
    ) {
        viewModelScope.launch {
            if (existingSchool == null) {
                val res = repository.createSchool(
                    schoolCode = schoolCode,
                    name = name,
                    address = address,
                    ownerName = ownerName,
                    ownerMobile = ownerMobile,
                    loginId = loginId,
                    busCount = 0L,
                    isActive = isActive
                )
                _statusBannerMessage.value = if (res.isSuccess) {
                    "Added school $schoolCode ($name)"
                } else {
                    "Error adding school: ${res.exceptionOrNull()?.localizedMessage}"
                }
            } else {
                val res = repository.updateSchool(
                    existingSchool.copy(
                        schoolCode = schoolCode,
                        name = name,
                        address = address,
                        ownerName = ownerName,
                        ownerMobile = ownerMobile,
                        loginId = loginId,
                        isActive = isActive
                    )
                )
                _statusBannerMessage.value = if (res.isSuccess) {
                    "Updated school ${existingSchool.schoolCode}"
                } else {
                    "Error updating school: ${res.exceptionOrNull()?.localizedMessage}"
                }
            }
        }
    }

    fun toggleSchoolActive(school: SchoolRecord) {
        viewModelScope.launch {
            val updated = school.copy(isActive = !school.isActive)
            val res = repository.updateSchool(updated)
            _statusBannerMessage.value = if (res.isSuccess) {
                "${school.name} is now ${if (updated.isActive) "Active" else "Inactive"}"
            } else {
                "Failed to update status: ${res.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun deleteSchool(school: SchoolRecord) {
        viewModelScope.launch {
            val res = repository.deleteSchool(school.id)
            _statusBannerMessage.value = if (res.isSuccess) {
                "Deleted school ${school.schoolCode}"
            } else {
                "Failed to delete school: ${res.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    // School Owner & Super Admin: Add / Edit / Delete Bus
    fun saveBus(
        existingBus: BusRecord?,
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
        speedKmh: Long
    ) {
        viewModelScope.launch {
            if (existingBus == null) {
                val res = repository.createBus(
                    schoolId = schoolId,
                    schoolName = schoolName,
                    busNumber = busNumber,
                    registrationNumber = registrationNumber,
                    displayName = displayName,
                    driverName = driverName,
                    driverPhone = driverPhone,
                    assignedGpsDeviceId = assignedGpsDeviceId,
                    gpsProviderName = gpsProviderName,
                    gpsStatus = gpsStatus,
                    routeSummary = routeSummary,
                    speedKmh = speedKmh,
                    isDemo = true
                )
                _statusBannerMessage.value = if (res.isSuccess) {
                    "Registered bus $busNumber for $schoolId"
                } else {
                    "Failed to add bus: ${res.exceptionOrNull()?.localizedMessage}"
                }
            } else {
                val res = repository.updateBus(
                    existingBus.copy(
                        schoolId = schoolId,
                        schoolName = schoolName,
                        busNumber = busNumber,
                        registrationNumber = registrationNumber,
                        displayName = displayName,
                        driverName = driverName,
                        driverPhone = driverPhone,
                        assignedGpsDeviceId = assignedGpsDeviceId,
                        gpsProviderName = gpsProviderName,
                        gpsStatus = gpsStatus,
                        routeSummary = routeSummary,
                        speedKmh = speedKmh
                    )
                )
                _statusBannerMessage.value = if (res.isSuccess) {
                    "Updated bus $busNumber"
                } else {
                    "Failed to update bus: ${res.exceptionOrNull()?.localizedMessage}"
                }
            }
        }
    }

    fun deleteBus(bus: BusRecord) {
        viewModelScope.launch {
            val res = repository.deleteBus(bus.id)
            _statusBannerMessage.value = if (res.isSuccess) {
                "Removed bus ${bus.busNumber}"
            } else {
                "Failed to remove bus: ${res.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    // Universal GPS Integration Manager: Save / Test / Disconnect Adapter
    fun saveAndTestGpsConfig(
        existingConfigId: String?,
        schoolId: String,
        busId: String,
        providerName: String,
        connectionMethod: String,
        apiBaseUrl: String,
        deviceId: String,
        hasCredentials: Boolean
    ) {
        viewModelScope.launch {
            val resolvedStatus = when {
                providerName.contains("Unsupported", ignoreCase = true) -> "Unsupported Provider"
                deviceId.isBlank() -> "Disconnected"
                !hasCredentials && connectionMethod != "Driver Mobile GPS" -> "Invalid Credentials"
                apiBaseUrl.isBlank() && connectionMethod == "Provider REST API" -> "No Recent Data"
                else -> "Connected"
            }
            val syncText = when (resolvedStatus) {
                "Connected" -> "Verified just now (DEMO Adapter)"
                "Unsupported Provider" -> "Adapter unavailable for protocol"
                "Invalid Credentials" -> "Auth handshake rejected"
                "No Recent Data" -> "No endpoint response"
                else -> "Device offline"
            }

            val res = repository.saveGpsConfig(
                existingId = existingConfigId,
                schoolId = schoolId,
                busId = busId,
                providerName = providerName,
                connectionMethod = connectionMethod,
                apiBaseUrl = apiBaseUrl,
                deviceId = deviceId,
                status = resolvedStatus,
                lastSyncText = syncText,
                lastLocationText = if (resolvedStatus == "Connected") "28.5672° N, 77.2100° E" else "Unavailable"
            )

            // Also sync corresponding bus GPS status if found
            val currentBuses = (busesState.value as? DataUiState.Success)?.data.orEmpty()
            val matchingBus = currentBuses.find { it.busNumber == busId && it.schoolId == schoolId }
            if (matchingBus != null) {
                repository.updateBus(
                    matchingBus.copy(
                        assignedGpsDeviceId = deviceId,
                        gpsProviderName = providerName,
                        gpsStatus = resolvedStatus,
                        speedKmh = if (resolvedStatus == "Connected") 32L else 0L
                    )
                )
            }

            _statusBannerMessage.value = if (res.isSuccess) {
                "GPS Adapter test result for $busId: $resolvedStatus"
            } else {
                "Failed to save GPS config: ${res.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun disconnectGpsConfig(config: GpsProviderConfig) {
        viewModelScope.launch {
            repository.saveGpsConfig(
                existingId = config.id,
                schoolId = config.schoolId,
                busId = config.busId,
                providerName = config.providerName,
                connectionMethod = config.connectionMethod,
                apiBaseUrl = config.apiBaseUrl,
                deviceId = config.deviceId,
                status = "Disconnected",
                lastSyncText = "Disconnected by operator",
                lastLocationText = config.lastLocationText
            )
            val currentBuses = (busesState.value as? DataUiState.Success)?.data.orEmpty()
            val matchingBus = currentBuses.find { it.busNumber == config.busId }
            if (matchingBus != null) {
                repository.updateBus(
                    matchingBus.copy(
                        gpsStatus = "Disconnected",
                        speedKmh = 0L,
                        lastUpdatedText = "Disconnected"
                    )
                )
            }
            _statusBannerMessage.value = "Disconnected GPS device ${config.deviceId} on ${config.busId}"
        }
    }

    // Driver Mobile Location Tracking Mode
    fun setDriverBus(busNumber: String) {
        _driverBusNumber.value = busNumber
    }

    fun startDriverTripWithLocation(lat: Double, lng: Double, speedKmh: Long, isLiveHardwareGps: Boolean) {
        _driverTripActive.value = true
        val timeFmt = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date())
        _driverLastCoords.value = String.format(
            Locale.US,
            "%.5f° N, %.5f° E • %s (%s)",
            lat,
            lng,
            timeFmt,
            if (isLiveHardwareGps) "LIVE DEVICE GPS" else "DEMO FALLBACK"
        )
        viewModelScope.launch {
            val currentBuses = (busesState.value as? DataUiState.Success)?.data.orEmpty()
            val targetBus = currentBuses.find { it.busNumber == _driverBusNumber.value } ?: currentBuses.firstOrNull()
            if (targetBus != null) {
                repository.updateBus(
                    targetBus.copy(
                        gpsProviderName = "Driver Mobile App Telemetry",
                        gpsStatus = "Connected",
                        latitude = lat,
                        longitude = lng,
                        speedKmh = speedKmh,
                        lastUpdatedText = "$timeFmt (${if (isLiveHardwareGps) "LIVE GPS" else "DEMO"})",
                        isDemo = !isLiveHardwareGps
                    )
                )
                _statusBannerMessage.value = "Driver trip active for ${targetBus.busNumber} • Coordinates synced"
            }
        }
    }

    fun stopDriverTrip() {
        _driverTripActive.value = false
        _driverLastCoords.value = "Trip stopped"
        viewModelScope.launch {
            val currentBuses = (busesState.value as? DataUiState.Success)?.data.orEmpty()
            val targetBus = currentBuses.find { it.busNumber == _driverBusNumber.value } ?: currentBuses.firstOrNull()
            if (targetBus != null) {
                repository.updateBus(
                    targetBus.copy(
                        gpsStatus = "Disconnected",
                        speedKmh = 0L,
                        lastUpdatedText = "Driver ended trip"
                    )
                )
            }
            _statusBannerMessage.value = "Driver mobile trip ended."
        }
    }

    // Gemini Multi-Turn Assistant with route & school context
    fun sendChatMessage(userText: String) {
        val trimmed = userText.trim()
        if (trimmed.isEmpty() || _isAiGenerating.value) return

        val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            isUser = true,
            text = trimmed,
            timestampLabel = timeStr
        )
        _chatMessages.value = _chatMessages.value + userMsg
        _isAiGenerating.value = true

        viewModelScope.launch {
            try {
                val schools = (schoolsState.value as? DataUiState.Success)?.data.orEmpty()
                val buses = (busesState.value as? DataUiState.Success)?.data.orEmpty()
                val contextSummary = buildString {
                    append("Current User Role: ${_activeRole.value.label}. ")
                    append("Selected School: ${_selectedSchoolCode.value}, Selected Bus: ${_selectedBusNumber.value}. ")
                    append("Active Fleet Summary: ")
                    buses.take(6).forEach { b ->
                        append("[${b.busNumber} (${b.schoolName}): status=${b.gpsStatus}, speed=${b.speedKmh}km/h, route=${b.routeSummary}, coords=(${b.latitude}, ${b.longitude})] ")
                    }
                    append("\nUser Question: $trimmed")
                }

                val response = withTimeout(60_000L) {
                    chatSession.sendMessage(contextSummary)
                }
                val replyText = response.text?.takeIf { it.isNotBlank() }
                    ?: "I couldn't generate a response right now. Please try asking again."

                val replyMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    isUser = false,
                    text = replyText,
                    timestampLabel = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()),
                    groundedPlaceLinks = listOf(
                        "Green Park Metro Station, New Delhi" to "https://maps.google.com/?q=28.5595,77.2068",
                        "Dwarka Sector 10, New Delhi" to "https://maps.google.com/?q=28.5812,77.0592",
                        "Pari Chowk, Greater Noida" to "https://maps.google.com/?q=28.4671,77.5138"
                    )
                )
                _chatMessages.value = _chatMessages.value + replyMsg
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Gemini chat timed out", e)
                _chatMessages.value = _chatMessages.value + ChatMessage(
                    id = UUID.randomUUID().toString(),
                    isUser = false,
                    text = "Request timed out after 60 seconds. Please check your connection and try again.",
                    timestampLabel = "Error"
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Gemini chat failed", e)
                _chatMessages.value = _chatMessages.value + ChatMessage(
                    id = UUID.randomUUID().toString(),
                    isUser = false,
                    text = "AI Assistant error: ${e.localizedMessage ?: "Failed to reach Gemini service."}",
                    timestampLabel = "Error"
                )
            } finally {
                _isAiGenerating.value = false
            }
        }
    }

    companion object {
        private const val TAG = "SwachhShikshaVM"
    }
}
