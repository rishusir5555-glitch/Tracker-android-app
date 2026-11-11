package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.BusRecord
import com.example.data.model.DemoSeedData
import com.example.data.model.SchoolRecord
import com.example.ui.components.GpsStatusPill
import com.example.ui.components.InteractiveSchoolBusMapCanvas
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedPale
import com.example.ui.theme.AsphaltBlack
import com.example.ui.theme.AsphaltSlate
import com.example.ui.theme.BusOrange
import com.example.ui.theme.BusYellow
import com.example.ui.theme.BusYellowPale
import com.example.ui.theme.ConnectedGreen
import com.example.ui.theme.ConnectedGreenPale
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SkyBlueDeep
import com.example.ui.theme.SkyBluePale

private enum class ParentSubScreen {
    HOME,
    LIVE_MAP,
    SCHOOL_BUS_SELECTOR
}

@Composable
fun ParentExperienceRoot(
    schools: List<SchoolRecord>,
    buses: List<BusRecord>,
    selectedSchoolCode: String,
    selectedBusNumber: String,
    parentPhone: String,
    simulatedProgress: Float,
    onSelectSchoolAndBus: (String, String) -> Unit,
    onVerifyDemoOtp: (String, String) -> Boolean,
    onSimulateStep: () -> Unit,
    onUpdateBusDemoState: (BusRecord) -> Unit
) {
    var currentSubScreen by remember { mutableStateOf(ParentSubScreen.HOME) }
    var showMobileOtpDialog by remember { mutableStateOf(false) }
    var selectedChildId by remember { mutableStateOf(DemoSeedData.sampleChildren.first().id) }

    // Authorized active schools only for parents
    val activeSchools = remember(schools) {
        schools.filter { it.isActive }.ifEmpty { schools }
    }
    val currentSchool = activeSchools.find { it.schoolCode == selectedSchoolCode }
        ?: activeSchools.firstOrNull()

    val schoolBuses = buses.filter { it.schoolId == (currentSchool?.schoolCode ?: selectedSchoolCode) }
        .ifEmpty { buses }

    val assignedBus = schoolBuses.find { it.busNumber == selectedBusNumber }
        ?: schoolBuses.firstOrNull()
        ?: DemoSeedData.defaultBuses("demo").first()

    when (currentSubScreen) {
        ParentSubScreen.HOME -> {
            ParentHomeScreen(
                currentSchool = currentSchool,
                assignedBus = assignedBus,
                parentPhone = parentPhone,
                selectedChildId = selectedChildId,
                onSelectChild = { child ->
                    selectedChildId = child.id
                    onSelectSchoolAndBus(child.schoolCode, child.assignedBusNumber)
                },
                onOpenLiveTracking = { currentSubScreen = ParentSubScreen.LIVE_MAP },
                onOpenSchoolBusSelector = { currentSubScreen = ParentSubScreen.SCHOOL_BUS_SELECTOR },
                onOpenMobileOtpDialog = { showMobileOtpDialog = true }
            )
        }

        ParentSubScreen.LIVE_MAP -> {
            BackHandler { currentSubScreen = ParentSubScreen.HOME }
            ParentLiveTrackingScreen(
                schoolName = currentSchool?.name ?: assignedBus.schoolName,
                bus = assignedBus,
                simulatedProgress = simulatedProgress,
                onSimulateStep = onSimulateStep,
                onChangeDemoGpsState = { newState ->
                    onUpdateBusDemoState(
                        assignedBus.copy(
                            gpsStatus = newState,
                            speedKmh = when (newState) {
                                "Connected" -> 34L
                                "Location Updating" -> 21L
                                else -> 0L
                            },
                            lastUpdatedText = when (newState) {
                                "Connected" -> "Just now (DEMO)"
                                "Location Updating" -> "Refreshing Fix... (DEMO)"
                                "Disconnected" -> "Offline 12m ago (DEMO)"
                                else -> "Signal Unavailable (DEMO)"
                            }
                        )
                    )
                },
                onBack = { currentSubScreen = ParentSubScreen.HOME }
            )
        }

        ParentSubScreen.SCHOOL_BUS_SELECTOR -> {
            BackHandler { currentSubScreen = ParentSubScreen.HOME }
            ParentSchoolAndBusSelectorScreen(
                authorizedSchools = activeSchools,
                allBuses = buses,
                initialSchoolCode = currentSchool?.schoolCode ?: selectedSchoolCode,
                initialBusNumber = assignedBus.busNumber,
                onConfirmSelection = { schoolCode, busNum ->
                    onSelectSchoolAndBus(schoolCode, busNum)
                    currentSubScreen = ParentSubScreen.HOME
                },
                onBack = { currentSubScreen = ParentSubScreen.HOME }
            )
        }
    }

    if (showMobileOtpDialog) {
        ParentDemoOtpDialog(
            initialPhone = parentPhone,
            onDismiss = { showMobileOtpDialog = false },
            onVerify = { phone, otp ->
                if (onVerifyDemoOtp(phone, otp)) {
                    showMobileOtpDialog = false
                }
            }
        )
    }
}

@Composable
private fun ParentHomeScreen(
    currentSchool: SchoolRecord?,
    assignedBus: BusRecord,
    parentPhone: String,
    selectedChildId: String,
    onSelectChild: (com.example.data.model.ChildSampleProfile) -> Unit,
    onOpenLiveTracking: () -> Unit,
    onOpenSchoolBusSelector: () -> Unit,
    onOpenMobileOtpDialog: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Friendly Welcome Card + Mobile OTP Onboarding Badge
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = BusYellowPale),
            border = BorderStroke(2.dp, AsphaltBlack),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Namaste, Parent! 👋",
                            style = MaterialTheme.typography.headlineMedium,
                            color = AsphaltBlack
                        )
                        Text(
                            text = "Mobile: $parentPhone • Verified (DEMO)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AsphaltSlate
                        )
                    }
                    OutlinedButton(
                        onClick = onOpenMobileOtpDialog,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.5.dp, AsphaltBlack),
                        modifier = Modifier.testTag("parent_otp_login_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = "Verify Mobile OTP",
                            modifier = Modifier.size(16.dp),
                            tint = AsphaltBlack
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Mobile OTP",
                            style = MaterialTheme.typography.labelLarge,
                            color = AsphaltBlack
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Multi-Child Selector Strip
                Text(
                    text = "Children Linked to Your Parent Account:",
                    style = MaterialTheme.typography.labelMedium,
                    color = AsphaltSlate,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DemoSeedData.sampleChildren.forEach { child ->
                        val isSelected = child.id == selectedChildId
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = if (isSelected) BusYellow else PureWhite,
                            border = BorderStroke(if (isSelected) 2.dp else 1.dp, AsphaltBlack),
                            modifier = Modifier
                                .clickable { onSelectChild(child) }
                                .testTag("select_child_${child.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChildCare,
                                    contentDescription = child.childName,
                                    tint = AsphaltBlack,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = child.childName,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = AsphaltBlack,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${child.gradeSection} • ${child.assignedBusNumber}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = AsphaltSlate
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Assigned School & Bus Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            border = BorderStroke(2.5.dp, AsphaltBlack),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = CircleShape,
                            color = SkyBluePale,
                            border = BorderStroke(1.5.dp, AsphaltBlack),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = "School",
                                    tint = SkyBlueDeep
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = currentSchool?.name ?: assignedBus.schoolName,
                                style = MaterialTheme.typography.titleLarge,
                                color = AsphaltBlack,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Authorized School ID: ${currentSchool?.schoolCode ?: assignedBus.schoolId}",
                                style = MaterialTheme.typography.labelMedium,
                                color = SkyBlueDeep
                            )
                        }
                    }
                    GpsStatusPill(status = assignedBus.gpsStatus)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Assigned Bus Details Box
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = SkyBluePale,
                    border = BorderStroke(1.5.dp, AsphaltBlack.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsBus,
                                    contentDescription = "Assigned Bus",
                                    tint = BusOrange
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Assigned Bus: ${assignedBus.busNumber}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = AsphaltBlack,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = BusYellow,
                                border = BorderStroke(1.dp, AsphaltBlack)
                            ) {
                                Text(
                                    text = assignedBus.registrationNumber,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AsphaltBlack,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text(
                            text = assignedBus.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AsphaltBlack,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Driver Name",
                                tint = AsphaltSlate,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Driver: ${assignedBus.driverName} (${assignedBus.driverPhone})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AsphaltSlate
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "Last Updated",
                                tint = AsphaltSlate,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Last Update: ${assignedBus.lastUpdatedText} • Speed: ${assignedBus.speedKmh} km/h",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AsphaltSlate
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // HUGE Primary Action: VIEW LIVE LOCATION
                Button(
                    onClick = onOpenLiveTracking,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(62.dp)
                        .testTag("view_live_location_button"),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BusYellow,
                        contentColor = AsphaltBlack
                    ),
                    border = BorderStroke(2.5.dp, AsphaltBlack),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "View Live Location",
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "VIEW LIVE BUS LOCATION",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onOpenSchoolBusSelector,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("change_school_bus_button"),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(2.dp, AsphaltBlack)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Change School or Bus",
                        tint = AsphaltBlack
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Search & Select School / Assigned Bus",
                        style = MaterialTheme.typography.labelLarge,
                        color = AsphaltBlack
                    )
                }
            }
        }
    }
}

@Composable
private fun ParentLiveTrackingScreen(
    schoolName: String,
    bus: BusRecord,
    simulatedProgress: Float,
    onSimulateStep: () -> Unit,
    onChangeDemoGpsState: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header with Back Button + Live Indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = BusYellow,
                    border = BorderStroke(2.dp, AsphaltBlack)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("back_from_live_map_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Parent Home",
                            tint = AsphaltBlack
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "LIVE BUS TRACKING",
                        style = MaterialTheme.typography.titleLarge,
                        color = AsphaltBlack,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = schoolName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AsphaltSlate
                    )
                }
            }
            GpsStatusPill(status = bus.gpsStatus)
        }

        // Large Interactive 3D Map Canvas
        InteractiveSchoolBusMapCanvas(
            bus = bus,
            simulatedProgress = simulatedProgress,
            onSimulateStep = onSimulateStep,
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
        )

        // Demo State Preview Bar (Allows testing Connected / Disconnected / Location Updating / Location Unavailable)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = BusYellowPale),
            border = BorderStroke(1.5.dp, AsphaltBlack)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "DEMO GPS STATE SIMULATOR (Preview All Required States):",
                    style = MaterialTheme.typography.labelMedium,
                    color = AsphaltBlack,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "Connected",
                        "Location Updating",
                        "Disconnected",
                        "Location Unavailable"
                    ).forEach { stateOption ->
                        val active = bus.gpsStatus == stateOption
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (active) BusYellow else PureWhite,
                            border = BorderStroke(if (active) 2.dp else 1.dp, AsphaltBlack),
                            modifier = Modifier
                                .clickable { onChangeDemoGpsState(stateOption) }
                                .testTag("demo_gps_state_${stateOption.lowercase().replace(" ", "_")}")
                        ) {
                            Text(
                                text = "GPS $stateOption",
                                style = MaterialTheme.typography.labelMedium,
                                color = AsphaltBlack,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }
        }

        // Detailed Telemetry & Driver Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            border = BorderStroke(2.dp, AsphaltBlack)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TelemetryStatBox(
                        label = "Bus Number",
                        value = bus.busNumber,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    TelemetryStatBox(
                        label = "Live Speed",
                        value = "${bus.speedKmh} km/h",
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    TelemetryStatBox(
                        label = "Last Updated",
                        value = bus.lastUpdatedText,
                        modifier = Modifier.weight(1f)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SkyBluePale,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Driver: ${bus.driverName}",
                            style = MaterialTheme.typography.titleMedium,
                            color = AsphaltBlack,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Driver Helpline: ${bus.driverPhone} • Plate: ${bus.registrationNumber}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AsphaltSlate
                        )
                        Text(
                            text = "Route Stops: ${bus.routeSummary}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SkyBlueDeep
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TelemetryStatBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = BusYellowPale,
        border = BorderStroke(1.dp, AsphaltBlack.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = AsphaltSlate
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = AsphaltBlack,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ParentSchoolAndBusSelectorScreen(
    authorizedSchools: List<SchoolRecord>,
    allBuses: List<BusRecord>,
    initialSchoolCode: String,
    initialBusNumber: String,
    onConfirmSelection: (String, String) -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var pickedSchoolCode by remember { mutableStateOf(initialSchoolCode) }
    var pickedBusNumber by remember { mutableStateOf(initialBusNumber) }

    val filteredSchools = authorizedSchools.filter {
        searchQuery.isBlank() ||
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.schoolCode.contains(searchQuery, ignoreCase = true)
    }

    val busesForPickedSchool = allBuses.filter { it.schoolId == pickedSchoolCode }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = BusYellow,
                border = BorderStroke(2.dp, AsphaltBlack)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = AsphaltBlack
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Select School & Assigned Bus",
                    style = MaterialTheme.typography.titleLarge,
                    color = AsphaltBlack,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Showing only authorized active schools",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AsphaltSlate
                )
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search Authorized School Name or Code") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search School")
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("parent_school_search_input"),
            shape = RoundedCornerShape(18.dp),
            singleLine = true
        )

        Text(
            text = "1. Select Your Child's School:",
            style = MaterialTheme.typography.titleMedium,
            color = AsphaltBlack,
            fontWeight = FontWeight.Bold
        )

        filteredSchools.forEach { school ->
            val selected = school.schoolCode == pickedSchoolCode
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        pickedSchoolCode = school.schoolCode
                        val firstBus = allBuses.firstOrNull { it.schoolId == school.schoolCode }
                        if (firstBus != null) {
                            pickedBusNumber = firstBus.busNumber
                        }
                    },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selected) BusYellowPale else PureWhite
                ),
                border = BorderStroke(if (selected) 2.5.dp else 1.dp, AsphaltBlack)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = school.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = AsphaltBlack,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${school.schoolCode} • ${school.address}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AsphaltSlate
                        )
                    }
                    if (selected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selected School",
                            tint = ConnectedGreen
                        )
                    }
                }
            }
        }

        Text(
            text = "2. Select Assigned Bus for $pickedSchoolCode:",
            style = MaterialTheme.typography.titleMedium,
            color = AsphaltBlack,
            fontWeight = FontWeight.Bold
        )

        if (busesForPickedSchool.isEmpty()) {
            Text(
                text = "No buses registered for this school yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = AsphaltSlate
            )
        } else {
            busesForPickedSchool.forEach { bus ->
                val selected = bus.busNumber == pickedBusNumber
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { pickedBusNumber = bus.busNumber },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected) SkyBluePale else PureWhite
                    ),
                    border = BorderStroke(if (selected) 2.5.dp else 1.dp, AsphaltBlack)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${bus.busNumber} (${bus.registrationNumber})",
                                style = MaterialTheme.typography.titleMedium,
                                color = AsphaltBlack,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = bus.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = AsphaltSlate
                            )
                            Text(
                                text = "Driver: ${bus.driverName}",
                                style = MaterialTheme.typography.labelMedium,
                                color = SkyBlueDeep
                            )
                        }
                        GpsStatusPill(status = bus.gpsStatus)
                    }
                }
            }
        }

        Button(
            onClick = { onConfirmSelection(pickedSchoolCode, pickedBusNumber) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("save_parent_school_bus_button"),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BusYellow,
                contentColor = AsphaltBlack
            ),
            border = BorderStroke(2.dp, AsphaltBlack)
        ) {
            Text(
                text = "Save & Remember Selection",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ParentDemoOtpDialog(
    initialPhone: String,
    onDismiss: () -> Unit,
    onVerify: (String, String) -> Unit
) {
    var phone by remember { mutableStateOf(initialPhone) }
    var otp by remember { mutableStateOf("123456") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Parent Mobile Login (DEMO OTP)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Simple mobile number onboarding for Parents (No compulsory email required). Note: SMS gateway is in DEMO mode — use sample OTP 123456.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AsphaltSlate
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Parent Mobile Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = otp,
                    onValueChange = { otp = it },
                    label = { Text("6-Digit OTP (DEMO: 123456)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onVerify(phone, otp) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = BusYellow,
                    contentColor = AsphaltBlack
                )
            ) {
                Text("Verify DEMO OTP", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
