package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.model.AppRole
import com.example.data.model.DemoSeedData
import com.example.data.repository.SwachhShikshaRepository
import com.example.ui.components.SwachhShikshaSplashScreen
import com.example.ui.screens.DriverMobileLocationScreen
import com.example.ui.screens.GeminiBusAssistantDialog
import com.example.ui.screens.ParentExperienceRoot
import com.example.ui.screens.SchoolOwnerPanelScreen
import com.example.ui.screens.SuperAdminPanelScreen
import com.example.ui.screens.SwachhShikshaAuthScreen
import com.example.ui.screens.TopBrandAndRoleSwitcherBar
import com.example.ui.screens.signOutUser
import com.example.ui.theme.AsphaltBlack
import com.example.ui.theme.BusYellow
import com.example.ui.theme.BusYellowPale
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PureWhite
import com.example.util.configureAppCheck
import com.example.viewmodel.DataUiState
import com.example.viewmodel.SwachhShikshaViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Mandatory App Check configuration with launch intent before any UI or Firebase calls
        configureAppCheck(this, intent)

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SwachhShikshaRootApp()
            }
        }
    }
}

@Composable
fun SwachhShikshaRootApp() {
    var showSplash by rememberSaveable { mutableStateOf(true) }
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    if (showSplash) {
        SwachhShikshaSplashScreen(
            onSplashFinished = { showSplash = false }
        )
    } else if (currentUser == null) {
        SwachhShikshaAuthScreen(
            onAuthSuccess = { currentUser = Firebase.auth.currentUser }
        )
    } else {
        val uid = currentUser!!.uid
        key(uid) {
            SwachhShikshaAuthenticatedWorkspace(currentUserId = uid)
        }
    }
}

@Composable
fun SwachhShikshaAuthenticatedWorkspace(
    currentUserId: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val viewModel: SwachhShikshaViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                SwachhShikshaViewModel(SwachhShikshaRepository(db), currentUserId)
            }
        }
    )

    val activeRole by viewModel.activeRole.collectAsStateWithLifecycle()
    val selectedSchoolCode by viewModel.selectedSchoolCode.collectAsStateWithLifecycle()
    val selectedBusNumber by viewModel.selectedBusNumber.collectAsStateWithLifecycle()
    val ownerTenantSchoolCode by viewModel.ownerTenantSchoolCode.collectAsStateWithLifecycle()
    val simulatedProgress by viewModel.simulatedRouteProgress.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBannerMessage.collectAsStateWithLifecycle()
    val parentPhone by viewModel.parentPhoneNumber.collectAsStateWithLifecycle()
    val driverTripActive by viewModel.driverTripActive.collectAsStateWithLifecycle()
    val driverBusNumber by viewModel.driverBusNumber.collectAsStateWithLifecycle()
    val driverLastCoords by viewModel.driverLastCoords.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isAiGenerating by viewModel.isAiGenerating.collectAsStateWithLifecycle()

    val schoolsState by viewModel.schoolsState.collectAsStateWithLifecycle()
    val busesState by viewModel.busesState.collectAsStateWithLifecycle()
    val gpsConfigsState by viewModel.gpsConfigsState.collectAsStateWithLifecycle()

    val schools = (schoolsState as? DataUiState.Success)?.data
        ?: DemoSeedData.defaultSchools(currentUserId)
    val buses = (busesState as? DataUiState.Success)?.data
        ?: DemoSeedData.defaultBuses(currentUserId)
    val gpsConfigs = (gpsConfigsState as? DataUiState.Success)?.data
        ?: DemoSeedData.defaultGpsConfigs(currentUserId)

    var showAiAssistantModal by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopBrandAndRoleSwitcherBar(
                activeRole = activeRole,
                onSelectRole = { viewModel.switchRole(it) },
                onOpenAiAssistant = { showAiAssistantModal = true },
                onSignOut = {
                    signOutUser(
                        credentialManager = credentialManager,
                        scope = scope
                    )
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = PureWhite,
                tonalElevation = 8.dp
            ) {
                AppRole.entries.forEach { role ->
                    val selected = activeRole == role
                    val icon = when (role) {
                        AppRole.PARENT -> Icons.Default.FamilyRestroom
                        AppRole.SCHOOL_OWNER -> Icons.Default.School
                        AppRole.SUPER_ADMIN -> Icons.Default.AdminPanelSettings
                        AppRole.DRIVER -> Icons.Default.PhoneAndroid
                    }
                    NavigationBarItem(
                        selected = selected,
                        onClick = { viewModel.switchRole(role) },
                        icon = {
                            Icon(imageVector = icon, contentDescription = role.label)
                        },
                        label = {
                            Text(
                                text = role.label,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AsphaltBlack,
                            selectedTextColor = AsphaltBlack,
                            indicatorColor = BusYellow
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Toast/Status Banner
            AnimatedVisibility(visible = statusBanner != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = BusYellowPale,
                    border = BorderStroke(1.5.dp, AsphaltBlack)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = statusBanner ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AsphaltBlack,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { viewModel.clearBanner() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss Banner",
                                tint = AsphaltBlack
                            )
                        }
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when (activeRole) {
                    AppRole.PARENT -> {
                        ParentExperienceRoot(
                            schools = schools,
                            buses = buses,
                            selectedSchoolCode = selectedSchoolCode,
                            selectedBusNumber = selectedBusNumber,
                            parentPhone = parentPhone,
                            simulatedProgress = simulatedProgress,
                            onSelectSchoolAndBus = { sch, bus ->
                                viewModel.selectParentSchoolAndBus(sch, bus)
                            },
                            onVerifyDemoOtp = { phone, otp ->
                                viewModel.updateParentPhoneAndVerifyDemoOtp(phone, otp)
                            },
                            onSimulateStep = { viewModel.advanceSimulatedBusManually() },
                            onUpdateBusDemoState = { updatedBus ->
                                viewModel.saveBus(
                                    existingBus = updatedBus,
                                    schoolId = updatedBus.schoolId,
                                    schoolName = updatedBus.schoolName,
                                    busNumber = updatedBus.busNumber,
                                    registrationNumber = updatedBus.registrationNumber,
                                    displayName = updatedBus.displayName,
                                    driverName = updatedBus.driverName,
                                    driverPhone = updatedBus.driverPhone,
                                    assignedGpsDeviceId = updatedBus.assignedGpsDeviceId,
                                    gpsProviderName = updatedBus.gpsProviderName,
                                    gpsStatus = updatedBus.gpsStatus,
                                    routeSummary = updatedBus.routeSummary,
                                    speedKmh = updatedBus.speedKmh
                                )
                            }
                        )
                    }

                    AppRole.SCHOOL_OWNER -> {
                        SchoolOwnerPanelScreen(
                            schools = schools,
                            allBuses = buses,
                            allGpsConfigs = gpsConfigs,
                            ownerTenantSchoolCode = ownerTenantSchoolCode,
                            simulatedProgress = simulatedProgress,
                            onSwitchOwnerTenant = { viewModel.setOwnerTenantSchool(it) },
                            onSaveBus = { existing, schId, schName, busNum, regNum, disp, drvName, drvPhone, gpsId, prov, status, route, speed ->
                                viewModel.saveBus(existing, schId, schName, busNum, regNum, disp, drvName, drvPhone, gpsId, prov, status, route, speed)
                            },
                            onDeleteBus = { viewModel.deleteBus(it) },
                            onSaveAndTestGpsConfig = { id, schId, busId, prov, method, url, devId, hasCreds ->
                                viewModel.saveAndTestGpsConfig(id, schId, busId, prov, method, url, devId, hasCreds)
                            },
                            onDisconnectGpsConfig = { viewModel.disconnectGpsConfig(it) },
                            onSimulateStep = { viewModel.advanceSimulatedBusManually() }
                        )
                    }

                    AppRole.SUPER_ADMIN -> {
                        SuperAdminPanelScreen(
                            schools = schools,
                            buses = buses,
                            gpsConfigs = gpsConfigs,
                            simulatedProgress = simulatedProgress,
                            onSaveSchool = { existing, code, name, addr, owner, mobile, login, active ->
                                viewModel.saveSchool(existing, code, name, addr, owner, mobile, login, active)
                            },
                            onToggleSchoolActive = { viewModel.toggleSchoolActive(it) },
                            onDeleteSchool = { viewModel.deleteSchool(it) },
                            onSaveBus = { existing, schId, schName, busNum, regNum, disp, drvName, drvPhone, gpsId, prov, status, route, speed ->
                                viewModel.saveBus(existing, schId, schName, busNum, regNum, disp, drvName, drvPhone, gpsId, prov, status, route, speed)
                            },
                            onDeleteBus = { viewModel.deleteBus(it) },
                            onSaveAndTestGpsConfig = { id, schId, busId, prov, method, url, devId, hasCreds ->
                                viewModel.saveAndTestGpsConfig(id, schId, busId, prov, method, url, devId, hasCreds)
                            },
                            onDisconnectGpsConfig = { viewModel.disconnectGpsConfig(it) },
                            onSimulateStep = { viewModel.advanceSimulatedBusManually() }
                        )
                    }

                    AppRole.DRIVER -> {
                        DriverMobileLocationScreen(
                            buses = buses,
                            driverBusNumber = driverBusNumber,
                            driverTripActive = driverTripActive,
                            driverLastCoords = driverLastCoords,
                            onSelectDriverBus = { viewModel.setDriverBus(it) },
                            onStartTripWithLocation = { lat, lng, speed, isLive ->
                                viewModel.startDriverTripWithLocation(lat, lng, speed, isLive)
                            },
                            onStopTrip = { viewModel.stopDriverTrip() }
                        )
                    }
                }
            }
        }
    }

    if (showAiAssistantModal) {
        GeminiBusAssistantDialog(
            messages = chatMessages,
            isGenerating = isAiGenerating,
            onSendMessage = { viewModel.sendChatMessage(it) },
            onDismiss = { showAiAssistantModal = false }
        )
    }
}
