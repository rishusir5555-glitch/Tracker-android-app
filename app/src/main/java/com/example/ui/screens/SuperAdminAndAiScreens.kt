package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SettingsInputAntenna
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.BusRecord
import com.example.data.model.ChatMessage
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
import com.example.ui.theme.WarningAmberPale

@Composable
fun SuperAdminPanelScreen(
    schools: List<SchoolRecord>,
    buses: List<BusRecord>,
    gpsConfigs: List<GpsProviderConfig>,
    simulatedProgress: Float,
    onSaveSchool: (SchoolRecord?, String, String, String, String, String, String, Boolean) -> Unit,
    onToggleSchoolActive: (SchoolRecord) -> Unit,
    onDeleteSchool: (SchoolRecord) -> Unit,
    onSaveBus: (BusRecord?, String, String, String, String, String, String, String, String, String, String, String, Long) -> Unit,
    onDeleteBus: (BusRecord) -> Unit,
    onSaveAndTestGpsConfig: (String?, String, String, String, String, String, String, Boolean) -> Unit,
    onDisconnectGpsConfig: (GpsProviderConfig) -> Unit,
    onSimulateStep: () -> Unit
) {
    var activeTab by remember { mutableIntStateOf(0) } // 0 = Schools, 1 = All Buses, 2 = Universal GPS Manager
    var schoolSearch by remember { mutableStateOf("") }
    var busSearch by remember { mutableStateOf("") }
    var busStatusFilter by remember { mutableStateOf("All") }

    var schoolToEdit by remember { mutableStateOf<SchoolRecord?>(null) }
    var showSchoolModal by remember { mutableStateOf(false) }
    var schoolToDelete by remember { mutableStateOf<SchoolRecord?>(null) }

    var busToEdit by remember { mutableStateOf<BusRecord?>(null) }
    var showBusModal by remember { mutableStateOf(false) }
    var busToInspectOnMap by remember { mutableStateOf<BusRecord?>(null) }

    var gpsToEdit by remember { mutableStateOf<GpsProviderConfig?>(null) }
    var showGpsModal by remember { mutableStateOf(false) }

    val activeSchoolsCount = schools.count { it.isActive }
    val inactiveSchoolsCount = schools.size - activeSchoolsCount
    val connectedBusesCount = buses.count { it.gpsStatus == "Connected" || it.gpsStatus == "Location Updating" }
    val disconnectedBusesCount = buses.size - connectedBusesCount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Master Platform Owner Header & 8-Metric Dashboard
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = AsphaltBlack),
            border = BorderStroke(2.5.dp, BusYellow)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Super Admin",
                            tint = BusYellow,
                            modifier = Modifier.size(30.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "SUPER ADMIN MASTER DASHBOARD",
                                style = MaterialTheme.typography.titleLarge,
                                color = BusYellow,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Multi-School SaaS & Universal GPS Integration Control",
                                style = MaterialTheme.typography.labelMedium,
                                color = PureWhite.copy(alpha = 0.85f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Row 1 of KPIs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AdminMetricCard("Total Schools", "${schools.size}", BusYellow, Modifier.weight(1f))
                    AdminMetricCard("Active Schools", "$activeSchoolsCount", ConnectedGreen, Modifier.weight(1f))
                    AdminMetricCard("Inactive", "$inactiveSchoolsCount", AlertRed, Modifier.weight(1f))
                    AdminMetricCard("Total Buses", "${buses.size}", SkyBlue, Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Row 2 of KPIs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AdminMetricCard("GPS Connected", "$connectedBusesCount", ConnectedGreen, Modifier.weight(1f))
                    AdminMetricCard("GPS Offline", "$disconnectedBusesCount", AlertRed, Modifier.weight(1f))
                    AdminMetricCard("Live Sessions", "$connectedBusesCount", BusOrange, Modifier.weight(1f))
                    AdminMetricCard("Parent A/Cs", "${DemoSeedData.sampleChildren.size + 142}", SkyBlue, Modifier.weight(1f))
                }
            }
        }

        // 2. Super Admin Section Switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                0 to "🏫 School Management",
                1 to "🚌 All Schools Buses",
                2 to "📡 Universal GPS Manager"
            ).forEach { (idx, title) ->
                val selected = activeTab == idx
                Button(
                    onClick = { activeTab = idx },
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selected) BusYellow else BusYellowPale,
                        contentColor = AsphaltBlack
                    ),
                    border = BorderStroke(2.dp, AsphaltBlack),
                    modifier = Modifier.testTag("super_admin_tab_$idx")
                ) {
                    Text(text = title, fontWeight = FontWeight.Bold)
                }
            }
        }

        when (activeTab) {
            0 -> {
                // TAB 0: SCHOOL MANAGEMENT
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = schoolSearch,
                        onValueChange = { schoolSearch = it },
                        label = { Text("Search School Name or ID") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            schoolToEdit = null
                            showSchoolModal = true
                        },
                        modifier = Modifier
                            .height(56.dp)
                            .testTag("super_admin_add_school_button"),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BusYellow,
                            contentColor = AsphaltBlack
                        ),
                        border = BorderStroke(2.dp, AsphaltBlack)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add School")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add School", fontWeight = FontWeight.Bold)
                    }
                }

                val filteredSchools = schools.filter {
                    schoolSearch.isBlank() ||
                        it.name.contains(schoolSearch, ignoreCase = true) ||
                        it.schoolCode.contains(schoolSearch, ignoreCase = true) ||
                        it.ownerName.contains(schoolSearch, ignoreCase = true)
                }

                filteredSchools.forEach { school ->
                    val schoolBusCount = buses.count { it.schoolId == school.schoolCode }
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
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = BusYellow,
                                    border = BorderStroke(1.5.dp, AsphaltBlack)
                                ) {
                                    Text(
                                        text = school.schoolCode,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = AsphaltBlack,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (school.isActive) "Active" else "Inactive",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (school.isActive) ConnectedGreen else AlertRed,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Switch(
                                        checked = school.isActive,
                                        onCheckedChange = { onToggleSchoolActive(school) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = school.name,
                                style = MaterialTheme.typography.titleLarge,
                                color = AsphaltBlack,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Address: ${school.address}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AsphaltSlate
                            )
                            Text(
                                text = "Owner: ${school.ownerName} (${school.ownerMobile})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AsphaltBlack
                            )
                            Text(
                                text = "Login ID: ${school.loginId} • Registered Buses: $schoolBusCount",
                                style = MaterialTheme.typography.labelMedium,
                                color = SkyBlueDeep
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        schoolToEdit = school
                                        showSchoolModal = true
                                    },
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Edit")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedButton(
                                    onClick = { schoolToDelete = school },
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, AlertRed)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AlertRed, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Delete", color = AlertRed)
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // TAB 1: ALL SCHOOLS BUS MANAGEMENT
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = busSearch,
                        onValueChange = { busSearch = it },
                        label = { Text("Search by School Name or Bus Number") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            busToEdit = null
                            showBusModal = true
                        },
                        modifier = Modifier.height(56.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BusYellow,
                            contentColor = AsphaltBlack
                        ),
                        border = BorderStroke(2.dp, AsphaltBlack)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Bus", fontWeight = FontWeight.Bold)
                    }
                }

                // Filter by GPS Status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (listOf("All") + DemoSeedData.gpsStatusCategories).forEach { statusFilter ->
                        val selected = busStatusFilter == statusFilter
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (selected) BusYellow else PureWhite,
                            border = BorderStroke(if (selected) 2.dp else 1.dp, AsphaltBlack),
                            modifier = Modifier.clickable { busStatusFilter = statusFilter }
                        ) {
                            Text(
                                text = statusFilter,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                val filteredBuses = buses.filter { bus ->
                    val matchesStatus = busStatusFilter == "All" || bus.gpsStatus == busStatusFilter
                    val matchesQuery = busSearch.isBlank() ||
                        bus.busNumber.contains(busSearch, ignoreCase = true) ||
                        bus.schoolName.contains(busSearch, ignoreCase = true) ||
                        bus.schoolId.contains(busSearch, ignoreCase = true) ||
                        bus.driverName.contains(busSearch, ignoreCase = true)
                    matchesStatus && matchesQuery
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
                                        text = "${bus.busNumber} • ${bus.schoolName}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = AsphaltBlack,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Tenant: ${bus.schoolId} • Reg: ${bus.registrationNumber}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = SkyBlueDeep
                                    )
                                }
                                GpsStatusPill(status = bus.gpsStatus)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Route: ${bus.displayName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AsphaltBlack
                            )
                            Text(
                                text = "Driver: ${bus.driverName} (${bus.driverPhone}) • GPS ID: ${bus.assignedGpsDeviceId}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AsphaltSlate
                            )
                            Text(
                                text = "Provider: ${bus.gpsProviderName} • Last Update: ${bus.lastUpdatedText}",
                                style = MaterialTheme.typography.labelMedium,
                                color = AsphaltSlate
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                OutlinedButton(
                                    onClick = { busToInspectOnMap = bus },
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.5.dp, AsphaltBlack)
                                ) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Live Location", color = AsphaltBlack)
                                }
                                Row {
                                    IconButton(
                                        onClick = {
                                            busToEdit = bus
                                            showBusModal = true
                                        }
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Bus", tint = SkyBlueDeep)
                                    }
                                    IconButton(onClick = { onDeleteBus(bus) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Bus", tint = AlertRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // TAB 2: UNIVERSAL GPS INTEGRATION MANAGER
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SkyBluePale),
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
                                    text = "Universal GPS Integration Manager",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = AsphaltBlack,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Scalable Adapter Architecture: REST API • Webhook • GT06/Teltonika Gateway • Device ID Mapping • Driver Mobile",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AsphaltSlate
                                )
                            }
                            Button(
                                onClick = {
                                    gpsToEdit = null
                                    showGpsModal = true
                                },
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BusYellow,
                                    contentColor = AsphaltBlack
                                ),
                                border = BorderStroke(2.dp, AsphaltBlack),
                                modifier = Modifier.testTag("add_universal_gps_adapter_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Adapter", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                gpsConfigs.forEach { cfg ->
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
                                        text = cfg.providerName,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = AsphaltBlack,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "School: ${cfg.schoolId} • Bus: ${cfg.busId} • Device: ${cfg.deviceId}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = SkyBlueDeep
                                    )
                                }
                                GpsStatusPill(status = cfg.status)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Method: ${cfg.connectionMethod}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AsphaltBlack
                            )
                            Text(
                                text = "Gateway URL: ${cfg.apiBaseUrl.ifEmpty { "Managed via Backend Gateway" }}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AsphaltSlate
                            )
                            Text(
                                text = "Last Sync: ${cfg.lastSyncText} • Last Coords: ${cfg.lastLocationText}",
                                style = MaterialTheme.typography.labelMedium,
                                color = AsphaltSlate
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        gpsToEdit = cfg
                                        showGpsModal = true
                                    },
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text("Configure / Test")
                                }
                                OutlinedButton(
                                    onClick = { onDisconnectGpsConfig(cfg) },
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, AlertRed)
                                ) {
                                    Icon(Icons.Default.LinkOff, contentDescription = null, tint = AlertRed, modifier = Modifier.size(16.dp))
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

    if (showSchoolModal) {
        SchoolFormDialog(
            existingSchool = schoolToEdit,
            nextSchoolIndex = schools.size + 1,
            onDismiss = { showSchoolModal = false },
            onSave = { code, name, addr, owner, mobile, login, active ->
                onSaveSchool(schoolToEdit, code, name, addr, owner, mobile, login, active)
                showSchoolModal = false
            }
        )
    }

    if (schoolToDelete != null) {
        AlertDialog(
            onDismissRequest = { schoolToDelete = null },
            title = { Text("Delete ${schoolToDelete?.schoolCode}?", fontWeight = FontWeight.Bold) },
            text = { Text("Confirm deleting ${schoolToDelete?.name}? All tenant access for this school will be revoked.") },
            confirmButton = {
                Button(
                    onClick = {
                        schoolToDelete?.let(onDeleteSchool)
                        schoolToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed, contentColor = PureWhite)
                ) {
                    Text("Confirm Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { schoolToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showBusModal) {
        BusFormDialog(
            existingBus = busToEdit,
            lockedSchoolCode = null,
            lockedSchoolName = null,
            availableSchools = schools,
            onDismiss = { showBusModal = false },
            onSave = { schId, schName, busNum, regNum, disp, drvName, drvPhone, gpsId, prov, status, route, speed ->
                onSaveBus(busToEdit, schId, schName, busNum, regNum, disp, drvName, drvPhone, gpsId, prov, status, route, speed)
                showBusModal = false
            }
        )
    }

    if (busToInspectOnMap != null) {
        AlertDialog(
            onDismissRequest = { busToInspectOnMap = null },
            title = {
                Text(
                    text = "Live Bus Telemetry: ${busToInspectOnMap?.busNumber}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    InteractiveSchoolBusMapCanvas(
                        bus = busToInspectOnMap!!,
                        simulatedProgress = simulatedProgress,
                        onSimulateStep = onSimulateStep,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    )
                    Text(
                        text = "School: ${busToInspectOnMap?.schoolName} (${busToInspectOnMap?.schoolId})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Driver: ${busToInspectOnMap?.driverName} • ${busToInspectOnMap?.driverPhone}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { busToInspectOnMap = null },
                    colors = ButtonDefaults.buttonColors(containerColor = BusYellow, contentColor = AsphaltBlack)
                ) {
                    Text("Close Preview", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showGpsModal) {
        GpsAdapterConfigDialog(
            existingConfig = gpsToEdit,
            defaultSchoolId = schools.firstOrNull()?.schoolCode ?: "SCH-0001",
            availableBuses = buses,
            onDismiss = { showGpsModal = false },
            onSaveAndTest = { id, schId, busId, prov, method, url, devId, hasCreds ->
                onSaveAndTestGpsConfig(id, schId, busId, prov, method, url, devId, hasCreds)
                showGpsModal = false
            }
        )
    }
}

@Composable
private fun AdminMetricCard(
    label: String,
    value: String,
    accentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = PureWhite.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = accentColor,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = PureWhite
            )
        }
    }
}

@Composable
private fun SchoolFormDialog(
    existingSchool: SchoolRecord?,
    nextSchoolIndex: Int,
    onDismiss: () -> Unit,
    onSave: (
        schoolCode: String,
        name: String,
        address: String,
        ownerName: String,
        ownerMobile: String,
        loginId: String,
        isActive: Boolean
    ) -> Unit
) {
    val defaultCode = String.format("SCH-%04d", nextSchoolIndex)
    var schoolCode by remember { mutableStateOf(existingSchool?.schoolCode ?: defaultCode) }
    var name by remember { mutableStateOf(existingSchool?.name ?: "School E – National Model School") }
    var address by remember { mutableStateOf(existingSchool?.address ?: "Sector 62, Noida, UP") }
    var ownerName by remember { mutableStateOf(existingSchool?.ownerName ?: "Anurag Mishra") }
    var ownerMobile by remember { mutableStateOf(existingSchool?.ownerMobile ?: "+91 98115 77889") }
    var loginId by remember { mutableStateOf(existingSchool?.loginId ?: "owner.schoole@swachhshiksha.in") }
    var isActive by remember { mutableStateOf(existingSchool?.isActive ?: true) }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingSchool == null) "Register New School Tenant" else "Edit ${existingSchool.schoolCode}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (errorText != null) {
                    Text(text = errorText ?: "", color = AlertRed, style = MaterialTheme.typography.bodyMedium)
                }
                OutlinedTextField(
                    value = schoolCode,
                    onValueChange = { schoolCode = it },
                    label = { Text("Unique School ID (e.g. SCH-0005)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("School Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("School Campus Address") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    label = { Text("School Owner Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = ownerMobile,
                    onValueChange = { ownerMobile = it },
                    label = { Text("Owner Mobile Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = loginId,
                    onValueChange = { loginId = it },
                    label = { Text("Owner Login Credential ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (schoolCode.isBlank() || name.isBlank() || ownerName.isBlank()) {
                        errorText = "School Code, School Name, and Owner Name are required."
                    } else {
                        onSave(schoolCode, name, address, ownerName, ownerMobile, loginId, isActive)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BusYellow, contentColor = AsphaltBlack)
            ) {
                Text("Save School", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Gemini 3.8 Flash Multi-Turn Chat & Route Grounding Assistant Modal
 */
@Composable
fun GeminiBusAssistantDialog(
    messages: List<ChatMessage>,
    isGenerating: Boolean,
    onSendMessage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var inputPrompt by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            color = PureWhite,
            border = BorderStroke(2.5.dp, AsphaltBlack)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = BusYellow,
                            border = BorderStroke(1.5.dp, AsphaltBlack),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.SmartToy, contentDescription = null, tint = AsphaltBlack)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Swachh Shiksha AI Route Assistant",
                                style = MaterialTheme.typography.titleLarge,
                                color = AsphaltBlack,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Powered by Gemini 3.8 Flash • Multi-Turn Fleet & Maps Advisor",
                                style = MaterialTheme.typography.labelMedium,
                                color = SkyBlueDeep
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close AI Assistant")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Suggestion Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "Where is BUS-101 right now?",
                        "Explain Universal GPS Adapter states",
                        "Best pickup stops near Green Park & Dwarka"
                    ).forEach { suggestion ->
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = SkyBluePale,
                            border = BorderStroke(1.dp, SkyBlueDeep),
                            modifier = Modifier.clickable { onSendMessage(suggestion) }
                        ) {
                            Text(
                                text = suggestion,
                                style = MaterialTheme.typography.labelMedium,
                                color = SkyBlueDeep,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Chat Thread
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    messages.forEach { msg ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = if (msg.isUser) Alignment.CenterEnd else Alignment.CenterStart
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (msg.isUser) BusYellow else BusYellowPale,
                                border = BorderStroke(1.5.dp, AsphaltBlack),
                                modifier = Modifier.fillMaxWidth(0.88f)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = if (msg.isUser) "You • ${msg.timestampLabel}" else "🚌 Swachh Shiksha AI • ${msg.timestampLabel}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = AsphaltSlate,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = msg.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = AsphaltBlack
                                    )
                                }
                            }
                        }
                    }

                    if (isGenerating) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = SkyBlueDeep
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gemini 3.8 Flash is analyzing route & fleet telemetry...",
                                style = MaterialTheme.typography.labelMedium,
                                color = AsphaltSlate
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Input Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputPrompt,
                        onValueChange = { inputPrompt = it },
                        placeholder = { Text("Ask about bus status, route stops, or GPS adapters...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ai_chat_input"),
                        shape = RoundedCornerShape(18.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (inputPrompt.isNotBlank()) {
                                onSendMessage(inputPrompt)
                                inputPrompt = ""
                            }
                        },
                        enabled = !isGenerating && inputPrompt.isNotBlank(),
                        modifier = Modifier
                            .height(54.dp)
                            .testTag("ai_chat_send_button"),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BusYellow,
                            contentColor = AsphaltBlack
                        ),
                        border = BorderStroke(2.dp, AsphaltBlack)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                    }
                }
            }
        }
    }
}
