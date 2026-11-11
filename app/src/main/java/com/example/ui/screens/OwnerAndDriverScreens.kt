package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SettingsInputAntenna
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.model.BusRecord
import com.example.data.model.DemoSeedData
import com.example.data.model.GpsProviderConfig
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
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

@Composable
fun SchoolOwnerPanelScreen(
    schools: List<SchoolRecord>,
    allBuses: List<BusRecord>,
    allGpsConfigs: List<GpsProviderConfig>,
    ownerTenantSchoolCode: String,
    simulatedProgress: Float,
    onSwitchOwnerTenant: (String) -> Unit,
    onSaveBus: (BusRecord?, String, String, String, String, String, String, String, String, String, String, String, Long) -> Unit,
    onDeleteBus: (BusRecord) -> Unit,
    onSaveAndTestGpsConfig: (String?, String, String, String, String, String, String, Boolean) -> Unit,
    onDisconnectGpsConfig: (GpsProviderConfig) -> Unit,
    onSimulateStep: () -> Unit
) {
    val currentSchool = schools.find { it.schoolCode == ownerTenantSchoolCode }
        ?: schools.firstOrNull()
        ?: DemoSeedData.defaultSchools("demo").first()

    // Strict Multi-School Tenant Isolation: Owner sees ONLY their own school's buses and GPS devices
    val tenantBuses = allBuses.filter { it.schoolId == currentSchool.schoolCode }
    val tenantGpsConfigs = allGpsConfigs.filter { it.schoolId == currentSchool.schoolCode }

    var searchQuery by remember { mutableStateOf("") }
    var ownerSubTab by remember { mutableStateOf(0) } // 0 = Buses & Live Map, 1 = GPS Device Management
    var selectedMapBus by remember(currentSchool.schoolCode, tenantBuses.size) {
        mutableStateOf(tenantBuses.firstOrNull())
    }
    var busToEdit by remember { mutableStateOf<BusRecord?>(null) }
    var showBusDialog by remember { mutableStateOf(false) }
    var busToDelete by remember { mutableStateOf<BusRecord?>(null) }
    var showGpsDialog by remember { mutableStateOf(false) }
    var gpsToEdit by remember { mutableStateOf<GpsProviderConfig?>(null) }

    val connectedCount = tenantBuses.count { it.gpsStatus == "Connected" || it.gpsStatus == "Location Updating" }
    val disconnectedCount = tenantBuses.size - connectedCount
    val activeTrackingCount = tenantBuses.count { it.speedKmh > 0L || it.gpsStatus == "Connected" }

    val filteredBuses = tenantBuses.filter {
        searchQuery.isBlank() ||
            it.busNumber.contains(searchQuery, ignoreCase = true) ||
            it.driverName.contains(searchQuery, ignoreCase = true) ||
            it.displayName.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Tenant Account Switcher Card (Demonstrates School A Owner vs School B Owner vs School C Owner isolation)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SkyBluePale),
            border = BorderStroke(2.dp, AsphaltBlack)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "SCHOOL OWNER TENANT ACCOUNT (Isolated SaaS Data):",
                    style = MaterialTheme.typography.labelMedium,
                    color = SkyBlueDeep,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    schools.forEach { school ->
                        val isSelected = school.schoolCode == currentSchool.schoolCode
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isSelected) BusYellow else PureWhite,
                            border = BorderStroke(if (isSelected) 2.dp else 1.dp, AsphaltBlack),
                            modifier = Modifier
                                .clickable { onSwitchOwnerTenant(school.schoolCode) }
                                .testTag("owner_tenant_${school.schoolCode.lowercase()}")
                        ) {
                            Text(
                                text = "${school.schoolCode}: ${school.name.substringBefore("–").trim()} Owner",
                                style = MaterialTheme.typography.labelLarge,
                                color = AsphaltBlack,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. School Owner Header & KPI Summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            border = BorderStroke(2.5.dp, AsphaltBlack),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = BusYellow,
                        border = BorderStroke(2.dp, AsphaltBlack),
                        modifier = Modifier.size(50.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = "School Logo",
                                tint = AsphaltBlack
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentSchool.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = AsphaltBlack,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Owner: ${currentSchool.ownerName} • Tenant ID: ${currentSchool.schoolCode}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AsphaltSlate
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OwnerKpiTile(
                        title = "Total Buses",
                        count = "${tenantBuses.size}",
                        bgColor = BusYellowPale,
                        modifier = Modifier.weight(1f)
                    )
                    OwnerKpiTile(
                        title = "GPS Connected",
                        count = "$connectedCount",
                        bgColor = ConnectedGreenPale,
                        modifier = Modifier.weight(1f)
                    )
                    OwnerKpiTile(
                        title = "Disconnected",
                        count = "$disconnectedCount",
                        bgColor = AlertRedPale,
                        modifier = Modifier.weight(1f)
                    )
                    OwnerKpiTile(
                        title = "Active Trips",
                        count = "$activeTrackingCount",
                        bgColor = SkyBluePale,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Sub-Navigation Tabs: Bus Management & Live Map vs GPS Management
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { ownerSubTab = 0 },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("owner_tab_buses"),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (ownerSubTab == 0) BusYellow else BusYellowPale,
                    contentColor = AsphaltBlack
                ),
                border = BorderStroke(2.dp, AsphaltBlack)
            ) {
                Icon(imageVector = Icons.Default.DirectionsBus, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Fleet & Live Map", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { ownerSubTab = 1 },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("owner_tab_gps"),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (ownerSubTab == 1) BusYellow else BusYellowPale,
                    contentColor = AsphaltBlack
                ),
                border = BorderStroke(2.dp, AsphaltBlack)
            ) {
                Icon(imageVector = Icons.Default.SettingsInputAntenna, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("GPS Devices", fontWeight = FontWeight.Bold)
            }
        }

        if (ownerSubTab == 0) {
            // Live Location Preview of Selected Bus in Owner's School
            val mapBus = selectedMapBus ?: tenantBuses.firstOrNull()
            if (mapBus != null) {
                Text(
                    text = "Live Location Preview: ${mapBus.busNumber} (${mapBus.displayName})",
                    style = MaterialTheme.typography.titleMedium,
                    color = AsphaltBlack,
                    fontWeight = FontWeight.Bold
                )
                InteractiveSchoolBusMapCanvas(
                    bus = mapBus,
                    simulatedProgress = simulatedProgress,
                    onSimulateStep = onSimulateStep,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search ${currentSchool.schoolCode} Buses") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = {
                        busToEdit = null
                        showBusDialog = true
                    },
                    modifier = Modifier
                        .height(56.dp)
                        .testTag("owner_add_bus_button"),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BusYellow,
                        contentColor = AsphaltBlack
                    ),
                    border = BorderStroke(2.dp, AsphaltBlack)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Bus")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Bus", fontWeight = FontWeight.Bold)
                }
            }

            filteredBuses.forEach { bus ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(2.dp, AsphaltBlack)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${bus.busNumber} • ${bus.registrationNumber}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = AsphaltBlack,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = bus.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AsphaltSlate
                                )
                            }
                            GpsStatusPill(status = bus.gpsStatus)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Driver: ${bus.driverName} (${bus.driverPhone})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AsphaltBlack
                        )
                        Text(
                            text = "Assigned GPS: ${bus.assignedGpsDeviceId.ifEmpty { "None" }} • Provider: ${bus.gpsProviderName.ifEmpty { "Unassigned" }}",
                            style = MaterialTheme.typography.labelMedium,
                            color = SkyBlueDeep
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(
                                onClick = { selectedMapBus = bus },
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.5.dp, AsphaltBlack)
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Track on Map", color = AsphaltBlack)
                            }
                            Row {
                                IconButton(
                                    onClick = {
                                        busToEdit = bus
                                        showBusDialog = true
                                    }
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Bus", tint = SkyBlueDeep)
                                }
                                IconButton(
                                    onClick = { busToDelete = bus }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Bus", tint = AlertRed)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Owner GPS Management Tab
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Authorized GPS Connections (${currentSchool.schoolCode})",
                        style = MaterialTheme.typography.titleMedium,
                        color = AsphaltBlack,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Connect existing vehicle GPS trackers & test synchronization",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AsphaltSlate
                    )
                }
                Button(
                    onClick = {
                        gpsToEdit = null
                        showGpsDialog = true
                    },
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BusYellow,
                        contentColor = AsphaltBlack
                    ),
                    border = BorderStroke(2.dp, AsphaltBlack),
                    modifier = Modifier.testTag("owner_connect_gps_button")
                ) {
                    Icon(Icons.Default.Link, contentDescription = "Connect GPS")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Connect GPS", fontWeight = FontWeight.Bold)
                }
            }

            if (tenantGpsConfigs.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = BusYellowPale)
                ) {
                    Text(
                        text = "No GPS hardware configured for ${currentSchool.schoolCode} yet. Tap 'Connect GPS' to bind a tracker.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                tenantGpsConfigs.forEach { cfg ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        border = BorderStroke(2.dp, AsphaltBlack)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${cfg.busId} • ${cfg.deviceId}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = AsphaltBlack,
                                    fontWeight = FontWeight.Bold
                                )
                                GpsStatusPill(status = cfg.status)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Provider: ${cfg.providerName} (${cfg.connectionMethod})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AsphaltBlack
                            )
                            Text(
                                text = "Last Sync: ${cfg.lastSyncText} • Location: ${cfg.lastLocationText}",
                                style = MaterialTheme.typography.labelMedium,
                                color = AsphaltSlate
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        onSaveAndTestGpsConfig(
                                            cfg.id,
                                            cfg.schoolId,
                                            cfg.busId,
                                            cfg.providerName,
                                            cfg.connectionMethod,
                                            cfg.apiBaseUrl,
                                            cfg.deviceId,
                                            true
                                        )
                                    },
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text("Test Connection", color = SkyBlueDeep, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = { onDisconnectGpsConfig(cfg) },
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(Icons.Default.LinkOff, contentDescription = null, modifier = Modifier.size(16.dp), tint = AlertRed)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Disconnect", color = AlertRed)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showBusDialog) {
        BusFormDialog(
            existingBus = busToEdit,
            lockedSchoolCode = currentSchool.schoolCode,
            lockedSchoolName = currentSchool.name,
            availableSchools = listOf(currentSchool),
            onDismiss = { showBusDialog = false },
            onSave = { schoolId, schoolName, busNum, regNum, dispName, drvName, drvPhone, gpsId, gpsProv, status, route, speed ->
                onSaveBus(
                    busToEdit,
                    schoolId,
                    schoolName,
                    busNum,
                    regNum,
                    dispName,
                    drvName,
                    drvPhone,
                    gpsId,
                    gpsProv,
                    status,
                    route,
                    speed
                )
                showBusDialog = false
            }
        )
    }

    if (busToDelete != null) {
        AlertDialog(
            onDismissRequest = { busToDelete = null },
            title = { Text("Delete Bus ${busToDelete?.busNumber}?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to remove ${busToDelete?.busNumber} from ${currentSchool.name}?") },
            confirmButton = {
                Button(
                    onClick = {
                        busToDelete?.let(onDeleteBus)
                        busToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed, contentColor = PureWhite)
                ) {
                    Text("Delete Bus")
                }
            },
            dismissButton = {
                TextButton(onClick = { busToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showGpsDialog) {
        GpsAdapterConfigDialog(
            existingConfig = gpsToEdit,
            defaultSchoolId = currentSchool.schoolCode,
            availableBuses = tenantBuses,
            onDismiss = { showGpsDialog = false },
            onSaveAndTest = { id, schId, busId, prov, method, url, devId, hasCreds ->
                onSaveAndTestGpsConfig(id, schId, busId, prov, method, url, devId, hasCreds)
                showGpsDialog = false
            }
        )
    }
}

@Composable
private fun OwnerKpiTile(
    title: String,
    count: String,
    bgColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = bgColor,
        border = BorderStroke(1.5.dp, AsphaltBlack)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count,
                style = MaterialTheme.typography.headlineMedium,
                color = AsphaltBlack,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = AsphaltSlate
            )
        }
    }
}

/**
 * Section 9: Driver Mobile Location Mode (Alternative Location Source for buses without hardware GPS).
 * Uses real Android FusedLocationProviderClient with foreground runtime permission request.
 */
@SuppressLint("MissingPermission")
@Composable
fun DriverMobileLocationScreen(
    buses: List<BusRecord>,
    driverBusNumber: String,
    driverTripActive: Boolean,
    driverLastCoords: String,
    onSelectDriverBus: (String) -> Unit,
    onStartTripWithLocation: (Double, Double, Long, Boolean) -> Unit,
    onStopTrip: () -> Unit
) {
    val context = LocalContext.current
    var permissionStatusText by remember { mutableStateOf("Foreground location permission required when starting trip") }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            permissionStatusText = "Foreground Location Permission Granted"
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val speedKmh = (location.speed * 3.6f).toLong().coerceAtLeast(24L)
                        onStartTripWithLocation(location.latitude, location.longitude, speedKmh, true)
                    } else {
                        onStartTripWithLocation(28.5672, 77.2100, 30L, false)
                    }
                }
                .addOnFailureListener {
                    onStartTripWithLocation(28.5672, 77.2100, 30L, false)
                }
        } else {
            permissionStatusText = "Permission denied — using clearly labeled DEMO coordinates"
            onStartTripWithLocation(28.5672, 77.2100, 28L, false)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = BusYellowPale),
            border = BorderStroke(2.5.dp, AsphaltBlack)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "🚌 Driver Mobile GPS Transmitter",
                    style = MaterialTheme.typography.headlineMedium,
                    color = AsphaltBlack,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Alternative location source for school buses without dedicated hardware GPS trackers. Only transmits coordinates while an authorized trip is active.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AsphaltSlate
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            border = BorderStroke(2.dp, AsphaltBlack)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "1. Select Your Assigned Bus:",
                    style = MaterialTheme.typography.titleMedium,
                    color = AsphaltBlack,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    buses.forEach { bus ->
                        val selected = bus.busNumber == driverBusNumber
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (selected) BusYellow else SkyBluePale,
                            border = BorderStroke(if (selected) 2.dp else 1.dp, AsphaltBlack),
                            modifier = Modifier.clickable { onSelectDriverBus(bus.busNumber) }
                        ) {
                            Text(
                                text = "${bus.busNumber} (${bus.schoolId})",
                                style = MaterialTheme.typography.labelLarge,
                                color = AsphaltBlack,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (driverTripActive) ConnectedGreenPale else SkyBluePale,
                    border = BorderStroke(1.5.dp, AsphaltBlack),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Trip Status: ${if (driverTripActive) "ACTIVE TRANSMITTING" else "IDLE"}",
                                style = MaterialTheme.typography.titleMedium,
                                color = AsphaltBlack,
                                fontWeight = FontWeight.Bold
                            )
                            GpsStatusPill(status = if (driverTripActive) "Connected" else "Disconnected")
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Last Coordinates: $driverLastCoords",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AsphaltSlate
                        )
                        Text(
                            text = permissionStatusText,
                            style = MaterialTheme.typography.labelMedium,
                            color = SkyBlueDeep
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            val hasFine = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasFine) {
                                val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                                fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                                    .addOnSuccessListener { loc ->
                                        if (loc != null) {
                                            onStartTripWithLocation(loc.latitude, loc.longitude, 32L, true)
                                        } else {
                                            onStartTripWithLocation(28.5672, 77.2100, 32L, false)
                                        }
                                    }
                                    .addOnFailureListener {
                                        onStartTripWithLocation(28.5672, 77.2100, 32L, false)
                                    }
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                        enabled = !driverTripActive,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .testTag("driver_start_trip_button"),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ConnectedGreen,
                            contentColor = PureWhite
                        ),
                        border = BorderStroke(2.dp, AsphaltBlack)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Start Trip")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("START TRIP", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onStopTrip,
                        enabled = driverTripActive,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .testTag("driver_stop_trip_button"),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AlertRed,
                            contentColor = PureWhite
                        ),
                        border = BorderStroke(2.dp, AsphaltBlack)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = "Stop Trip")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("STOP TRIP", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun BusFormDialog(
    existingBus: BusRecord?,
    lockedSchoolCode: String?,
    lockedSchoolName: String?,
    availableSchools: List<SchoolRecord>,
    onDismiss: () -> Unit,
    onSave: (
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
    ) -> Unit
) {
    var schoolId by remember { mutableStateOf(existingBus?.schoolId ?: lockedSchoolCode ?: availableSchools.firstOrNull()?.schoolCode ?: "SCH-0001") }
    var busNumber by remember { mutableStateOf(existingBus?.busNumber ?: "BUS-105") }
    var registrationNumber by remember { mutableStateOf(existingBus?.registrationNumber ?: "DL-1PC-8805") }
    var displayName by remember { mutableStateOf(existingBus?.displayName ?: "Route 5: Central Park – School Campus") }
    var driverName by remember { mutableStateOf(existingBus?.driverName ?: "Rajendra Prasad") }
    var driverPhone by remember { mutableStateOf(existingBus?.driverPhone ?: "+91 98101 55005") }
    var assignedGpsDeviceId by remember { mutableStateOf(existingBus?.assignedGpsDeviceId ?: "IMEI-86420105") }
    var gpsProviderName by remember { mutableStateOf(existingBus?.gpsProviderName ?: DemoSeedData.supportedProviders.first()) }
    var gpsStatus by remember { mutableStateOf(existingBus?.gpsStatus ?: "Connected") }
    var routeSummary by remember { mutableStateOf(existingBus?.routeSummary ?: "Central Park -> Metro Gate -> School Campus") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val resolvedSchoolName = lockedSchoolName
        ?: availableSchools.find { it.schoolCode == schoolId }?.name
        ?: "School $schoolId"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingBus == null) "Add School Bus" else "Edit Bus ${existingBus.busNumber}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (validationError != null) {
                    Text(
                        text = validationError ?: "",
                        color = AlertRed,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (lockedSchoolCode == null && availableSchools.isNotEmpty()) {
                    Text("Assign to School:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        availableSchools.forEach { sch ->
                            val sel = sch.schoolCode == schoolId
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (sel) BusYellow else SkyBluePale,
                                border = BorderStroke(1.dp, AsphaltBlack),
                                modifier = Modifier.clickable { schoolId = sch.schoolCode }
                            ) {
                                Text(
                                    text = sch.schoolCode,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = busNumber,
                    onValueChange = { busNumber = it },
                    label = { Text("Bus Number (e.g. BUS-105)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = registrationNumber,
                    onValueChange = { registrationNumber = it },
                    label = { Text("Registration Plate") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Bus Route Display Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = driverName,
                    onValueChange = { driverName = it },
                    label = { Text("Driver Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = driverPhone,
                    onValueChange = { driverPhone = it },
                    label = { Text("Driver Mobile Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = assignedGpsDeviceId,
                    onValueChange = { assignedGpsDeviceId = it },
                    label = { Text("Assigned GPS Device ID / IMEI") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = routeSummary,
                    onValueChange = { routeSummary = it },
                    label = { Text("Route Stops (use -> between stops)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (busNumber.isBlank() || registrationNumber.isBlank() || driverName.isBlank()) {
                        validationError = "Bus number, registration number, and driver name are required."
                    } else {
                        onSave(
                            schoolId,
                            resolvedSchoolName,
                            busNumber,
                            registrationNumber,
                            displayName,
                            driverName,
                            driverPhone,
                            assignedGpsDeviceId,
                            gpsProviderName,
                            gpsStatus,
                            routeSummary,
                            if (gpsStatus == "Connected") 32L else 0L
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BusYellow, contentColor = AsphaltBlack)
            ) {
                Text("Save Bus", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun GpsAdapterConfigDialog(
    existingConfig: GpsProviderConfig?,
    defaultSchoolId: String,
    availableBuses: List<BusRecord>,
    onDismiss: () -> Unit,
    onSaveAndTest: (
        existingId: String?,
        schoolId: String,
        busId: String,
        providerName: String,
        connectionMethod: String,
        apiBaseUrl: String,
        deviceId: String,
        hasCredentials: Boolean
    ) -> Unit
) {
    var schoolId by remember { mutableStateOf(existingConfig?.schoolId ?: defaultSchoolId) }
    var busId by remember { mutableStateOf(existingConfig?.busId ?: availableBuses.firstOrNull()?.busNumber ?: "BUS-101") }
    var providerName by remember { mutableStateOf(existingConfig?.providerName ?: DemoSeedData.supportedProviders.first()) }
    var connectionMethod by remember { mutableStateOf(existingConfig?.connectionMethod ?: DemoSeedData.gpsConnectionMethods.first()) }
    var apiBaseUrl by remember { mutableStateOf(existingConfig?.apiBaseUrl ?: "https://api.trackmate-gateway.example/v2/telemetry") }
    var deviceId by remember { mutableStateOf(existingConfig?.deviceId ?: "IMEI-86420999") }
    var hasAuthorizedToken by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Universal GPS Adapter Configuration",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Select GPS Provider Adapter:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DemoSeedData.supportedProviders.forEach { prov ->
                        val sel = prov == providerName
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (sel) BusYellow else SkyBluePale,
                            border = BorderStroke(1.dp, AsphaltBlack),
                            modifier = Modifier.clickable { providerName = prov }
                        ) {
                            Text(
                                text = prov,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Text(
                    text = "Connection Method:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DemoSeedData.gpsConnectionMethods.forEach { method ->
                        val sel = method == connectionMethod
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (sel) BusYellowPale else PureWhite,
                            border = BorderStroke(1.dp, AsphaltBlack),
                            modifier = Modifier.clickable { connectionMethod = method }
                        ) {
                            Text(
                                text = method,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = schoolId,
                    onValueChange = { schoolId = it },
                    label = { Text("School ID (e.g. SCH-0001)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = busId,
                    onValueChange = { busId = it },
                    label = { Text("Assigned Bus Number (e.g. BUS-101)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = deviceId,
                    onValueChange = { deviceId = it },
                    label = { Text("Hardware Device ID / IMEI") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = apiBaseUrl,
                    onValueChange = { apiBaseUrl = it },
                    label = { Text("Provider Gateway Endpoint URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = hasAuthorizedToken,
                        onCheckedChange = { hasAuthorizedToken = it }
                    )
                    Text(
                        text = "Backend Secret / API Token Configured (Uncheck to test Invalid Credentials)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveAndTest(
                        existingConfig?.id,
                        schoolId,
                        busId,
                        providerName,
                        connectionMethod,
                        apiBaseUrl,
                        deviceId,
                        hasAuthorizedToken
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = BusYellow, contentColor = AsphaltBlack)
            ) {
                Text("Test & Save Adapter", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
