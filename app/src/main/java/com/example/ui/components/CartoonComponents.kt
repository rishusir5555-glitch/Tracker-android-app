package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.BusRecord
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedPale
import com.example.ui.theme.AsphaltBlack
import com.example.ui.theme.AsphaltSlate
import com.example.ui.theme.BusOrange
import com.example.ui.theme.BusYellow
import com.example.ui.theme.BusYellowDark
import com.example.ui.theme.BusYellowPale
import com.example.ui.theme.ConnectedGreen
import com.example.ui.theme.ConnectedGreenPale
import com.example.ui.theme.PencilPink
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SkyBlueDeep
import com.example.ui.theme.SkyBluePale
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberPale
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/**
 * Custom 3D Cartoon Brand Badge featuring the yellow school bus with two happy kids
 * and the colorful 3D pencil carrying the Hindi text "स्वच्छ शिक्षा".
 */
@Composable
fun SwachhShikshaBrandHeaderBadge(
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 48.dp else 64.dp)
                .shadow(6.dp, RoundedCornerShape(18.dp))
                .clip(RoundedCornerShape(18.dp))
                .background(BusYellow)
                .border(2.dp, AsphaltBlack, RoundedCornerShape(18.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_swachh_shiksha_hero_1794373916250),
                contentDescription = "Swachh Shiksha 3D School Bus and Pencil Logo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "SWACHH SHIKSHA",
                    style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                    color = AsphaltBlack,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                // 3D Colorful Pencil Pill with "स्वच्छ शिक्षा"
                Surface(
                    shape = RoundedCornerShape(50),
                    color = SkyBlue,
                    border = BorderStroke(1.5.dp, AsphaltBlack),
                    shadowElevation = 3.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(PencilPink)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "✏️ स्वच्छ शिक्षा",
                            style = MaterialTheme.typography.labelMedium,
                            color = PureWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Text(
                text = "SMART SCHOOL BUS TRACKING",
                style = MaterialTheme.typography.labelMedium,
                color = SkyBlueDeep,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Animated 3D Splash Screen showing:
 * - 3D yellow school bus with two children sitting inside
 * - Colorful pencil with "स्वच्छ शिक्षा"
 * - Brand name SWACHH SHIKSHA
 * - Subtitle: SMART SCHOOL BUS TRACKING
 * - Smooth bus movement animation and clean loading transition
 */
@Composable
fun SwachhShikshaSplashScreen(
    onSplashFinished: () -> Unit
) {
    var progress by remember { mutableFloatStateOf(0f) }
    val infiniteTransition = rememberInfiniteTransition(label = "splash_bus")
    val busOffsetX by infiniteTransition.animateFloat(
        initialValue = -18f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bus_x"
    )
    val busBounceY by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bus_y"
    )

    LaunchedEffect(Unit) {
        val steps = 25
        for (i in 1..steps) {
            delay(55L)
            progress = i / steps.toFloat()
        }
        delay(200L)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(SkyBluePale, BusYellowPale, PureWhite)
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Animated 3D Hero Card with Bus + Two Children + Colorful Pencil ("स्वच्छ शिक्षा")
            Card(
                modifier = Modifier
                    .size(width = 300.dp, height = 230.dp)
                    .offset(x = busOffsetX.dp, y = busBounceY.dp)
                    .shadow(16.dp, RoundedCornerShape(32.dp)),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = BusYellow),
                border = BorderStroke(3.dp, AsphaltBlack)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_swachh_shiksha_hero_1794373916250),
                        contentDescription = "3D Yellow School Bus with Two Children and Colorful Pencil स्वच्छ शिक्षा",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Colorful Pencil Ribbon Badge overlay at bottom
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(10.dp),
                        shape = RoundedCornerShape(50),
                        color = AsphaltBlack.copy(alpha = 0.88f),
                        border = BorderStroke(1.5.dp, BusYellow)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🚌 2 Happy Kids  •  ✏️ स्वच्छ शिक्षा",
                                color = BusYellow,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 3D Drawn Pencil Banner with Hindi text "स्वच्छ शिक्षा"
            Surface(
                shape = RoundedCornerShape(50),
                color = BusOrange,
                border = BorderStroke(2.dp, AsphaltBlack),
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(PencilPink)
                            .border(1.dp, AsphaltBlack, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "स्वच्छ शिक्षा",
                        style = MaterialTheme.typography.titleLarge,
                        color = PureWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "▶",
                        color = BusYellow,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SWACHH SHIKSHA",
                style = MaterialTheme.typography.displayLarge,
                color = AsphaltBlack,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "SMART SCHOOL BUS TRACKING",
                style = MaterialTheme.typography.titleMedium,
                color = SkyBlueDeep,
                letterSpacing = 1.4.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(240.dp)
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(50))
                        .border(1.5.dp, AsphaltBlack, RoundedCornerShape(50)),
                    color = BusYellow,
                    trackColor = PureWhite
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Starting Multi-School GPS Platform...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AsphaltSlate
                )
            }
        }
    }
}

@Composable
fun GpsStatusPill(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, dotColor) = when (status) {
        "Connected" -> Triple(ConnectedGreenPale, ConnectedGreen, ConnectedGreen)
        "Location Updating" -> Triple(SkyBluePale, SkyBlueDeep, SkyBlue)
        "Disconnected", "Location Unavailable" -> Triple(AlertRedPale, AlertRed, AlertRed)
        "Invalid Credentials", "Unsupported Provider", "No Recent Data" ->
            Triple(WarningAmberPale, WarningAmber, WarningAmber)
        else -> Triple(BusYellowPale, AsphaltBlack, BusOrange)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = bgColor,
        border = BorderStroke(1.5.dp, dotColor.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = status,
                style = MaterialTheme.typography.labelMedium,
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Interactive 3D Cartoon Map Canvas showing:
 * - Animated yellow school bus marker moving smoothly along the route when connected
 * - Bus route path with checkpoints & school destination pin
 * - Zoom In / Zoom Out / Recenter Location controls
 * - Live Tracking Indicator + Speed + GPS Connection status + DEMO / LIVE transparency badge
 */
@Composable
fun InteractiveSchoolBusMapCanvas(
    bus: BusRecord,
    simulatedProgress: Float,
    onSimulateStep: () -> Unit,
    modifier: Modifier = Modifier
) {
    var zoomLevel by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    val isMovingStatus = bus.gpsStatus == "Connected" || bus.gpsStatus == "Location Updating"
    val animatedProgress by animateFloatAsState(
        targetValue = if (isMovingStatus) simulatedProgress else 0.35f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "bus_route_progress"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "radar_pulse")
    val radarPulse by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_radius"
    )

    val routeStops = remember(bus.routeSummary) {
        bus.routeSummary.split("->").map { it.trim() }.filter { it.isNotEmpty() }
            .ifEmpty { listOf("Pickup Stop 1", "City Crossing", "Main Square", "School Campus") }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(2.5.dp, AsphaltBlack),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE5F4FD))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        zoomLevel = (zoomLevel * zoom).coerceIn(0.75f, 2.5f)
                        panOffset = Offset(
                            x = (panOffset.x + pan.x).coerceIn(-220f, 220f),
                            y = (panOffset.y + pan.y).coerceIn(-180f, 180f)
                        )
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // 1. Soft Cartoon Map Blocks & Parks background
                drawRect(color = Color(0xFFE6F4FA))

                // Green cartoon park zones
                drawRoundRect(
                    color = Color(0xFFC8E6C9).copy(alpha = 0.7f),
                    topLeft = Offset(w * 0.06f + panOffset.x * 0.4f, h * 0.12f + panOffset.y * 0.4f),
                    size = Size(w * 0.32f * zoomLevel, h * 0.26f * zoomLevel),
                    cornerRadius = CornerRadius(32f, 32f)
                )
                drawRoundRect(
                    color = Color(0xFFFFF9C4).copy(alpha = 0.75f),
                    topLeft = Offset(w * 0.56f + panOffset.x * 0.4f, h * 0.52f + panOffset.y * 0.4f),
                    size = Size(w * 0.36f * zoomLevel, h * 0.32f * zoomLevel),
                    cornerRadius = CornerRadius(32f, 32f)
                )

                // Subtle city grid lines
                val gridStep = 70f * zoomLevel
                var gx = (panOffset.x % gridStep)
                while (gx < w) {
                    drawLine(
                        color = Color(0xFFB3E5FC).copy(alpha = 0.65f),
                        start = Offset(gx, 0f),
                        end = Offset(gx, h),
                        strokeWidth = 2f
                    )
                    gx += gridStep
                }
                var gy = (panOffset.y % gridStep)
                while (gy < h) {
                    drawLine(
                        color = Color(0xFFB3E5FC).copy(alpha = 0.65f),
                        start = Offset(0f, gy),
                        end = Offset(w, gy),
                        strokeWidth = 2f
                    )
                    gy += gridStep
                }

                // 2. Define 4 key route waypoints scaled by zoom & pan
                val center = Offset(w / 2f + panOffset.x, h / 2f + panOffset.y)
                val p0 = Offset(center.x - w * 0.34f * zoomLevel, center.y + h * 0.24f * zoomLevel)
                val p1 = Offset(center.x - w * 0.10f * zoomLevel, center.y - h * 0.12f * zoomLevel)
                val p2 = Offset(center.x + w * 0.14f * zoomLevel, center.y + h * 0.10f * zoomLevel)
                val p3 = Offset(center.x + w * 0.34f * zoomLevel, center.y - h * 0.22f * zoomLevel)

                val routePath = Path().apply {
                    moveTo(p0.x, p0.y)
                    lineTo(p1.x, p1.y)
                    lineTo(p2.x, p2.y)
                    lineTo(p3.x, p3.y)
                }

                // Road asphalt outer stroke
                drawPath(
                    path = routePath,
                    color = AsphaltSlate,
                    style = Stroke(width = 30f * zoomLevel, cap = StrokeCap.Round)
                )
                // Road yellow center dashed line
                drawPath(
                    path = routePath,
                    color = BusYellow,
                    style = Stroke(
                        width = 5f * zoomLevel,
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 14f), 0f)
                    )
                )

                // Draw stop circles along route
                val points = listOf(p0, p1, p2, p3)
                points.forEachIndexed { idx, pt ->
                    val isSchool = idx == points.lastIndex
                    drawCircle(
                        color = AsphaltBlack,
                        radius = (if (isSchool) 18f else 12f) * zoomLevel,
                        center = pt
                    )
                    drawCircle(
                        color = if (isSchool) ConnectedGreen else PureWhite,
                        radius = (if (isSchool) 13f else 8f) * zoomLevel,
                        center = pt
                    )
                }

                // Calculate current bus position along piecewise linear path [p0 -> p1 -> p2 -> p3]
                val clamped = animatedProgress.coerceIn(0f, 1f)
                val segFloat = clamped * 3f
                val segIndex = segFloat.toInt().coerceIn(0, 2)
                val localT = (segFloat - segIndex).coerceIn(0f, 1f)
                val startPt = points[segIndex]
                val endPt = points[segIndex + 1]
                val busPos = Offset(
                    x = startPt.x + (endPt.x - startPt.x) * localT,
                    y = startPt.y + (endPt.y - startPt.y) * localT
                )

                // 3. Draw pulsing GPS signal ring around the bus
                if (isMovingStatus) {
                    drawCircle(
                        color = BusYellow.copy(alpha = (1f - radarPulse) * 0.55f),
                        radius = (26f + 46f * radarPulse) * zoomLevel,
                        center = busPos
                    )
                }

                // 4. Draw 3D Cartoon Yellow School Bus Marker on Canvas
                val busW = 58f * zoomLevel
                val busH = 36f * zoomLevel
                // Shadow
                drawRoundRect(
                    color = AsphaltBlack.copy(alpha = 0.28f),
                    topLeft = Offset(busPos.x - busW / 2f + 4f, busPos.y - busH / 2f + 6f),
                    size = Size(busW, busH),
                    cornerRadius = CornerRadius(14f * zoomLevel, 14f * zoomLevel)
                )
                // Bus border
                drawRoundRect(
                    color = AsphaltBlack,
                    topLeft = Offset(busPos.x - busW / 2f - 3f, busPos.y - busH / 2f - 3f),
                    size = Size(busW + 6f, busH + 6f),
                    cornerRadius = CornerRadius(14f * zoomLevel, 14f * zoomLevel)
                )
                // Bus body
                drawRoundRect(
                    color = if (isMovingStatus) BusYellow else Color(0xFFBDBDBD),
                    topLeft = Offset(busPos.x - busW / 2f, busPos.y - busH / 2f),
                    size = Size(busW, busH),
                    cornerRadius = CornerRadius(12f * zoomLevel, 12f * zoomLevel)
                )
                // Sky-blue windows showing 2 children silhouettes
                drawRoundRect(
                    color = SkyBluePale,
                    topLeft = Offset(busPos.x - busW * 0.36f, busPos.y - busH * 0.32f),
                    size = Size(busW * 0.32f, busH * 0.38f),
                    cornerRadius = CornerRadius(5f * zoomLevel, 5f * zoomLevel)
                )
                drawRoundRect(
                    color = SkyBluePale,
                    topLeft = Offset(busPos.x + busW * 0.04f, busPos.y - busH * 0.32f),
                    size = Size(busW * 0.32f, busH * 0.38f),
                    cornerRadius = CornerRadius(5f * zoomLevel, 5f * zoomLevel)
                )
                // Black wheels
                drawCircle(
                    color = AsphaltBlack,
                    radius = 7f * zoomLevel,
                    center = Offset(busPos.x - busW * 0.26f, busPos.y + busH * 0.48f)
                )
                drawCircle(
                    color = AsphaltBlack,
                    radius = 7f * zoomLevel,
                    center = Offset(busPos.x + busW * 0.26f, busPos.y + busH * 0.48f)
                )
            }

            // Top Overlay: Live Tracking Status Bar + DEMO Label
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GpsStatusPill(status = bus.gpsStatus)

                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (bus.isDemo) AsphaltBlack else ConnectedGreen,
                    border = BorderStroke(1.5.dp, BusYellow)
                ) {
                    Text(
                        text = if (bus.isDemo) "DEMO SIMULATED GPS" else "LIVE DEVICE GPS",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (bus.isDemo) BusYellow else PureWhite,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            // Right-side Map Controls: Zoom In, Zoom Out, Recenter, Advance Bus
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = PureWhite,
                    border = BorderStroke(1.5.dp, AsphaltBlack),
                    shadowElevation = 4.dp
                ) {
                    IconButton(
                        onClick = { zoomLevel = (zoomLevel + 0.2f).coerceAtMost(2.5f) },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("map_zoom_in_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Zoom In Map",
                            tint = AsphaltBlack
                        )
                    }
                }
                Surface(
                    shape = CircleShape,
                    color = PureWhite,
                    border = BorderStroke(1.5.dp, AsphaltBlack),
                    shadowElevation = 4.dp
                ) {
                    IconButton(
                        onClick = { zoomLevel = (zoomLevel - 0.2f).coerceAtLeast(0.75f) },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("map_zoom_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Zoom Out Map",
                            tint = AsphaltBlack
                        )
                    }
                }
                Surface(
                    shape = CircleShape,
                    color = BusYellow,
                    border = BorderStroke(1.5.dp, AsphaltBlack),
                    shadowElevation = 4.dp
                ) {
                    IconButton(
                        onClick = {
                            zoomLevel = 1.0f
                            panOffset = Offset.Zero
                            onSimulateStep()
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("map_recenter_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Recenter Bus Location",
                            tint = AsphaltBlack
                        )
                    }
                }
            }

            // Bottom Overlay inside Map: Route Stops & Coordinates Telemetry Strip
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(20.dp),
                color = PureWhite.copy(alpha = 0.95f),
                border = BorderStroke(1.5.dp, AsphaltBlack),
                shadowElevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DirectionsBus,
                                contentDescription = "Bus Number",
                                tint = BusOrange,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${bus.busNumber} • ${bus.registrationNumber}",
                                style = MaterialTheme.typography.titleMedium,
                                color = AsphaltBlack,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "Bus Speed",
                                tint = SkyBlueDeep,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${bus.speedKmh} km/h",
                                style = MaterialTheme.typography.titleMedium,
                                color = SkyBlueDeep,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Route: ${routeStops.joinToString(" ➔ ")}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AsphaltSlate,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = String.format(
                            "Coords: %.4f° N, %.4f° E  •  Updated: %s",
                            bus.latitude + (animatedProgress * 0.008),
                            bus.longitude + (animatedProgress * 0.008),
                            bus.lastUpdatedText
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = AsphaltSlate
                    )
                }
            }
        }
    }
}
