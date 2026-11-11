package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

enum class AppRole(val code: String, val label: String, val badgeHindi: String) {
    PARENT("PARENT", "Parent App", "अभिभावक"),
    SCHOOL_OWNER("SCHOOL_OWNER", "School Owner", "स्कूल संचालक"),
    SUPER_ADMIN("SUPER_ADMIN", "Super Admin", "सुपर एडमिन"),
    DRIVER("DRIVER", "Driver Mode", "बस चालक");

    companion object {
        fun fromCode(code: String): AppRole = entries.find { it.code == code } ?: PARENT
    }
}

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "Parent User",
    val phone: String = "+91 9876543210",
    val activeRole: String = AppRole.PARENT.code,
    val selectedSchoolId: String = "SCH-0001",
    val selectedBusId: String = "BUS-A1",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class SchoolRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val schoolCode: String = "SCH-0001",
    val name: String = "",
    val address: String = "",
    val ownerName: String = "",
    val ownerMobile: String = "",
    val loginId: String = "",
    val busCount: Long = 0L,
    @get:PropertyName("isActive") @set:PropertyName("isActive") var isActive: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class BusRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val schoolId: String = "SCH-0001",
    val schoolName: String = "",
    val busNumber: String = "",
    val registrationNumber: String = "",
    val displayName: String = "",
    val driverName: String = "",
    val driverPhone: String = "",
    val assignedGpsDeviceId: String = "",
    val gpsProviderName: String = "",
    val gpsStatus: String = "Connected",
    val speedKmh: Long = 0L,
    val latitude: Double = 28.6139,
    val longitude: Double = 77.2090,
    val routeSummary: String = "",
    val lastUpdatedText: String = "Just now (DEMO)",
    @get:PropertyName("isDemo") @set:PropertyName("isDemo") var isDemo: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class GpsProviderConfig(
    @DocumentId val id: String = "",
    val userId: String = "",
    val schoolId: String = "SCH-0001",
    val busId: String = "",
    val providerName: String = "",
    val connectionMethod: String = "Provider REST API",
    val apiBaseUrl: String = "",
    val deviceId: String = "",
    val status: String = "Connected",
    val lastSyncText: String = "Synced 10s ago (DEMO)",
    val lastLocationText: String = "28.6139° N, 77.2090° E",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class ChildSampleProfile(
    val id: String,
    val childName: String,
    val gradeSection: String,
    val schoolCode: String,
    val schoolName: String,
    val assignedBusNumber: String,
    val stopName: String,
    val pickupTime: String
)

data class RouteCheckpoint(
    val name: String,
    val timeLabel: String,
    val latOffset: Float,
    val lngOffset: Float
)

data class ChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val timestampLabel: String,
    val groundedPlaceLinks: List<Pair<String, String>> = emptyList()
)

object DemoSeedData {
    val gpsConnectionMethods = listOf(
        "Provider REST API",
        "Provider Webhook",
        "Secure Gateway Protocol (GT06/Teltonika)",
        "Device ID Mapping",
        "Authorized Provider Account",
        "Driver Mobile GPS"
    )

    val supportedProviders = listOf(
        "TrackMate India REST v2",
        "Teltonika FMB920 Gateway",
        "Concox GT06N Cloud Webhook",
        "LocoNav Fleet Adapter",
        "Driver Mobile App Telemetry",
        "Legacy Custom Tracker (Unsupported)"
    )

    val gpsStatusCategories = listOf(
        "Connected",
        "Disconnected",
        "Location Updating",
        "Location Unavailable",
        "Invalid Credentials",
        "Unsupported Provider",
        "No Recent Data"
    )

    val sampleChildren = listOf(
        ChildSampleProfile(
            id = "CHD-1",
            childName = "Aarav Sharma",
            gradeSection = "Class 4 - Section A",
            schoolCode = "SCH-0001",
            schoolName = "School A – Swachh Vidya Mandir",
            assignedBusNumber = "BUS-101",
            stopName = "Green Park Metro Gate 2",
            pickupTime = "07:25 AM"
        ),
        ChildSampleProfile(
            id = "CHD-2",
            childName = "Ananya Sharma",
            gradeSection = "Class 1 - Section B",
            schoolCode = "SCH-0001",
            schoolName = "School A – Swachh Vidya Mandir",
            assignedBusNumber = "BUS-102",
            stopName = "Hauz Khas Enclave",
            pickupTime = "07:35 AM"
        ),
        ChildSampleProfile(
            id = "CHD-3",
            childName = "Vihaan Verma",
            gradeSection = "Class 6 - Section C",
            schoolCode = "SCH-0002",
            schoolName = "School B – Little Scholars Academy",
            assignedBusNumber = "BUS-201",
            stopName = "Dwarka Sector 10 Park",
            pickupTime = "07:15 AM"
        )
    )

    fun defaultSchools(userId: String): List<SchoolRecord> = listOf(
        SchoolRecord(
            id = "sch_0001",
            userId = userId,
            schoolCode = "SCH-0001",
            name = "School A – Swachh Vidya Mandir",
            address = "Sector 14, Ring Road, New Delhi 110016",
            ownerName = "Dr. Rajeshwar Prasad",
            ownerMobile = "+91 98110 22334",
            loginId = "owner.schoola@swachhshiksha.in",
            busCount = 4L,
            isActive = true
        ),
        SchoolRecord(
            id = "sch_0002",
            userId = userId,
            schoolCode = "SCH-0002",
            name = "School B – Little Scholars Academy",
            address = "Plot 8, Dwarka Sub-City, New Delhi 110075",
            ownerName = "Meenakshi Kulkarni",
            ownerMobile = "+91 98201 44556",
            loginId = "owner.schoolb@swachhshiksha.in",
            busCount = 3L,
            isActive = true
        ),
        SchoolRecord(
            id = "sch_0003",
            userId = userId,
            schoolCode = "SCH-0003",
            name = "School C – Sunrise Global Public School",
            address = "Knowledge Park III, Greater Noida 201308",
            ownerName = "Vikramaditya Singh",
            ownerMobile = "+91 98712 88990",
            loginId = "owner.schoolc@swachhshiksha.in",
            busCount = 2L,
            isActive = true
        ),
        SchoolRecord(
            id = "sch_0004",
            userId = userId,
            schoolCode = "SCH-0004",
            name = "School D – Harmony Heritage School",
            address = "Civil Lines, Jaipur, Rajasthan 302006",
            ownerName = "Sunita Rathore",
            ownerMobile = "+91 94140 66778",
            loginId = "owner.schoold@swachhshiksha.in",
            busCount = 1L,
            isActive = false
        )
    )

    fun defaultBuses(userId: String): List<BusRecord> = listOf(
        // School A (4 buses)
        BusRecord(
            id = "bus_101",
            userId = userId,
            schoolId = "SCH-0001",
            schoolName = "School A – Swachh Vidya Mandir",
            busNumber = "BUS-101",
            registrationNumber = "DL-1PC-8801",
            displayName = "Route 1: Green Park – AIIMS – Campus",
            driverName = "Ramesh Kumar Yadav",
            driverPhone = "+91 98101 11001",
            assignedGpsDeviceId = "IMEI-86420101",
            gpsProviderName = "TrackMate India REST v2",
            gpsStatus = "Connected",
            speedKmh = 34L,
            latitude = 28.5672,
            longitude = 77.2100,
            routeSummary = "Safdarjung -> Green Park -> Hauz Khas -> School A Campus",
            lastUpdatedText = "2 sec ago (DEMO)",
            isDemo = true
        ),
        BusRecord(
            id = "bus_102",
            userId = userId,
            schoolId = "SCH-0001",
            schoolName = "School A – Swachh Vidya Mandir",
            busNumber = "BUS-102",
            registrationNumber = "DL-1PC-8802",
            displayName = "Route 2: Vasant Kunj – IIT Gate – Campus",
            driverName = "Suresh Chandra",
            driverPhone = "+91 98101 11002",
            assignedGpsDeviceId = "IMEI-86420102",
            gpsProviderName = "Teltonika FMB920 Gateway",
            gpsStatus = "Connected",
            speedKmh = 28L,
            latitude = 28.5450,
            longitude = 77.1920,
            routeSummary = "Vasant Vihar -> IIT Main Gate -> SDA Market -> School A Campus",
            lastUpdatedText = "5 sec ago (DEMO)",
            isDemo = true
        ),
        BusRecord(
            id = "bus_103",
            userId = userId,
            schoolId = "SCH-0001",
            schoolName = "School A – Swachh Vidya Mandir",
            busNumber = "BUS-103",
            registrationNumber = "DL-1PC-8803",
            displayName = "Route 3: Lajpat Nagar – South Ex – Campus",
            driverName = "Mahendra Pal",
            driverPhone = "+91 98101 11003",
            assignedGpsDeviceId = "IMEI-86420103",
            gpsProviderName = "Concox GT06N Cloud Webhook",
            gpsStatus = "Location Updating",
            speedKmh = 22L,
            latitude = 28.5733,
            longitude = 77.2432,
            routeSummary = "Lajpat Nagar Central -> Defense Colony -> South Ex -> School A",
            lastUpdatedText = "8 sec ago (DEMO)",
            isDemo = true
        ),
        BusRecord(
            id = "bus_104",
            userId = userId,
            schoolId = "SCH-0001",
            schoolName = "School A – Swachh Vidya Mandir",
            busNumber = "BUS-104",
            registrationNumber = "DL-1PC-8804",
            displayName = "Route 4: Saket – Malviya Nagar – Campus",
            driverName = "Harishankar Tiwari",
            driverPhone = "+91 98101 11004",
            assignedGpsDeviceId = "IMEI-86420104",
            gpsProviderName = "LocoNav Fleet Adapter",
            gpsStatus = "Disconnected",
            speedKmh = 0L,
            latitude = 28.5245,
            longitude = 77.2066,
            routeSummary = "Saket Metro -> Malviya Nagar -> Panchsheel -> School A",
            lastUpdatedText = "18 min ago (DEMO)",
            isDemo = true
        ),
        // School B (3 buses)
        BusRecord(
            id = "bus_201",
            userId = userId,
            schoolId = "SCH-0002",
            schoolName = "School B – Little Scholars Academy",
            busNumber = "BUS-201",
            registrationNumber = "DL-1PC-9201",
            displayName = "Route B1: Dwarka Sec 6 – Sec 10 – Campus",
            driverName = "Balwinder Singh",
            driverPhone = "+91 98102 22001",
            assignedGpsDeviceId = "IMEI-86420201",
            gpsProviderName = "TrackMate India REST v2",
            gpsStatus = "Connected",
            speedKmh = 36L,
            latitude = 28.5921,
            longitude = 77.0460,
            routeSummary = "Dwarka Sec 6 -> Sec 10 Metro -> Sec 12 -> School B Campus",
            lastUpdatedText = "3 sec ago (DEMO)",
            isDemo = true
        ),
        BusRecord(
            id = "bus_202",
            userId = userId,
            schoolId = "SCH-0002",
            schoolName = "School B – Little Scholars Academy",
            busNumber = "BUS-202",
            registrationNumber = "DL-1PC-9202",
            displayName = "Route B2: Janakpuri – Uttam Nagar – Campus",
            driverName = "Pradeep Verma",
            driverPhone = "+91 98102 22002",
            assignedGpsDeviceId = "IMEI-86420202",
            gpsProviderName = "Teltonika FMB920 Gateway",
            gpsStatus = "Connected",
            speedKmh = 31L,
            latitude = 28.6219,
            longitude = 77.0878,
            routeSummary = "Janakpuri District Ctr -> Dabri Mor -> Dwarka -> School B",
            lastUpdatedText = "4 sec ago (DEMO)",
            isDemo = true
        ),
        BusRecord(
            id = "bus_203",
            userId = userId,
            schoolId = "SCH-0002",
            schoolName = "School B – Little Scholars Academy",
            busNumber = "BUS-203",
            registrationNumber = "DL-1PC-9203",
            displayName = "Route B3: Palam Vihar – Bijwasan – Campus",
            driverName = "Dinesh Rawat",
            driverPhone = "+91 98102 22003",
            assignedGpsDeviceId = "IMEI-86420203",
            gpsProviderName = "Legacy Custom Tracker (Unsupported)",
            gpsStatus = "Unsupported Provider",
            speedKmh = 0L,
            latitude = 28.5562,
            longitude = 77.0600,
            routeSummary = "Palam Colony ->Sec 21 Metro -> School B Campus",
            lastUpdatedText = "No stream (DEMO)",
            isDemo = true
        ),
        // School C (2 buses)
        BusRecord(
            id = "bus_301",
            userId = userId,
            schoolId = "SCH-0003",
            schoolName = "School C – Sunrise Global Public School",
            busNumber = "BUS-301",
            registrationNumber = "UP-16BT-3301",
            displayName = "Route C1: Pari Chowk – Alpha 1 – Campus",
            driverName = "Munna Lal Gupta",
            driverPhone = "+91 98103 33001",
            assignedGpsDeviceId = "IMEI-86420301",
            gpsProviderName = "Concox GT06N Cloud Webhook",
            gpsStatus = "Connected",
            speedKmh = 40L,
            latitude = 28.4671,
            longitude = 77.5138,
            routeSummary = "Pari Chowk -> Alpha Commercial Belt -> Knowledge Park III",
            lastUpdatedText = "1 sec ago (DEMO)",
            isDemo = true
        ),
        BusRecord(
            id = "bus_302",
            userId = userId,
            schoolId = "SCH-0003",
            schoolName = "School C – Sunrise Global Public School",
            busNumber = "BUS-302",
            registrationNumber = "UP-16BT-3302",
            displayName = "Route C2: Noida Sec 137 – गौर सिटी – Campus",
            driverName = "Kamleshwar Nath",
            driverPhone = "+91 98103 33002",
            assignedGpsDeviceId = "IMEI-86420302",
            gpsProviderName = "LocoNav Fleet Adapter",
            gpsStatus = "Invalid Credentials",
            speedKmh = 0L,
            latitude = 28.5021,
            longitude = 77.4082,
            routeSummary = "Noida Sec 137 -> Surajpur -> Sunrise Global Campus",
            lastUpdatedText = "Auth error (DEMO)",
            isDemo = true
        ),
        // School D (1 bus)
        BusRecord(
            id = "bus_401",
            userId = userId,
            schoolId = "SCH-0004",
            schoolName = "School D – Harmony Heritage School",
            busNumber = "BUS-401",
            registrationNumber = "RJ-14PA-4401",
            displayName = "Route D1: Vaishali Nagar – C-Scheme – Campus",
            driverName = "Bhanwar Lal Meena",
            driverPhone = "+91 98104 44001",
            assignedGpsDeviceId = "DRV-MOB-401",
            gpsProviderName = "Driver Mobile App Telemetry",
            gpsStatus = "No Recent Data",
            speedKmh = 0L,
            latitude = 26.9124,
            longitude = 75.7873,
            routeSummary = "Vaishali Circle -> Ajmer Pulia -> Civil Lines Campus",
            lastUpdatedText = "Yesterday (DEMO)",
            isDemo = true
        )
    )

    fun defaultGpsConfigs(userId: String): List<GpsProviderConfig> = listOf(
        GpsProviderConfig(
            id = "gps_cfg_1",
            userId = userId,
            schoolId = "SCH-0001",
            busId = "BUS-101",
            providerName = "TrackMate India REST v2",
            connectionMethod = "Provider REST API",
            apiBaseUrl = "https://api.trackmate-gateway.example/v2/telemetry",
            deviceId = "IMEI-86420101",
            status = "Connected",
            lastSyncText = "2 sec ago (DEMO)",
            lastLocationText = "28.5672° N, 77.2100° E (Green Park)"
        ),
        GpsProviderConfig(
            id = "gps_cfg_2",
            userId = userId,
            schoolId = "SCH-0001",
            busId = "BUS-102",
            providerName = "Teltonika FMB920 Gateway",
            connectionMethod = "Secure Gateway Protocol (GT06/Teltonika)",
            apiBaseUrl = "wss://gateway.swachhshiksha.example/teltonika",
            deviceId = "IMEI-86420102",
            status = "Connected",
            lastSyncText = "5 sec ago (DEMO)",
            lastLocationText = "28.5450° N, 77.1920° E (IIT Gate)"
        ),
        GpsProviderConfig(
            id = "gps_cfg_3",
            userId = userId,
            schoolId = "SCH-0001",
            busId = "BUS-104",
            providerName = "LocoNav Fleet Adapter",
            connectionMethod = "Provider Webhook",
            apiBaseUrl = "https://hooks.swachhshiksha.example/loconav",
            deviceId = "IMEI-86420104",
            status = "Disconnected",
            lastSyncText = "18 min ago (DEMO)",
            lastLocationText = "28.5245° N, 77.2066° E (Saket)"
        ),
        GpsProviderConfig(
            id = "gps_cfg_4",
            userId = userId,
            schoolId = "SCH-0002",
            busId = "BUS-201",
            providerName = "TrackMate India REST v2",
            connectionMethod = "Provider REST API",
            apiBaseUrl = "https://api.trackmate-gateway.example/v2/telemetry",
            deviceId = "IMEI-86420201",
            status = "Connected",
            lastSyncText = "3 sec ago (DEMO)",
            lastLocationText = "28.5921° N, 77.0460° E (Dwarka Sec 10)"
        ),
        GpsProviderConfig(
            id = "gps_cfg_5",
            userId = userId,
            schoolId = "SCH-0002",
            busId = "BUS-203",
            providerName = "Legacy Custom Tracker (Unsupported)",
            connectionMethod = "Device ID Mapping",
            apiBaseUrl = "https://legacy-tracker.example/v1",
            deviceId = "IMEI-86420203",
            status = "Unsupported Provider",
            lastSyncText = "Adapter unavailable",
            lastLocationText = "Unsupported protocol format"
        ),
        GpsProviderConfig(
            id = "gps_cfg_6",
            userId = userId,
            schoolId = "SCH-0003",
            busId = "BUS-302",
            providerName = "LocoNav Fleet Adapter",
            connectionMethod = "Authorized Provider Account",
            apiBaseUrl = "https://api.loconav-bridge.example/v1/vehicles",
            deviceId = "IMEI-86420302",
            status = "Invalid Credentials",
            lastSyncText = "HTTP 401 Token Expired",
            lastLocationText = "28.5021° N, 77.4082° E (Stale)"
        )
    )
}
