package com.example.ui.components

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.BuildConfig
import com.example.data.models.AbiaCorridorData
import com.example.ui.theme.*
import com.example.utils.LocationTracker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Display modes for the Abia State Keke Transit Corridor map.
 */
enum class MapDisplayMode {
    GOOGLE_MAPS,
    VECTOR_TRANSIT
}

/**
 * Modern Transit Map View for Abia State Keke Transit Corridors.
 * Provides dual-engine architecture:
 * 1. Google Maps SDK (when MAPS_API_KEY is configured in AI Studio Secrets panel).
 * 2. High-Performance Interactive Vector Transit Canvas (instant offline fallback with full route,
 *    stops, active keke fleet, and GPS telemetry, avoiding API authorization errors).
 */
@Composable
fun GoogleTransitMapView(
    originMarket: String,
    originLgaId: String,
    destinationMarket: String,
    destinationLgaId: String,
    intermediateStops: List<String> = emptyList(),
    userLiveLat: Double? = null,
    userLiveLng: Double? = null,
    onMarketSelected: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    // Detect if a valid, non-placeholder Google Maps API key has been supplied in BuildConfig
    val isMapsKeyConfigured = remember {
        val key = BuildConfig.MAPS_API_KEY.trim()
        key.isNotEmpty() &&
            !key.equals("YOUR_GOOGLE_MAPS_API_KEY", ignoreCase = true) &&
            !key.startsWith("YOUR_") &&
            !key.contains("MOCK")
    }

    // Default to Google Maps if key is configured, or Vector Transit if key is pending/placeholder
    var currentMapMode by remember {
        mutableStateOf(if (isMapsKeyConfigured) MapDisplayMode.GOOGLE_MAPS else MapDisplayMode.VECTOR_TRANSIT)
    }

    var showKeySetupDialog by remember { mutableStateOf(false) }

    var hasLocationPermission by remember {
        mutableStateOf(LocationTracker.hasLocationPermission(context))
    }

    var liveUserCoords by remember(userLiveLat, userLiveLng) {
        mutableStateOf(
            if (userLiveLat != null && userLiveLng != null) LatLng(userLiveLat, userLiveLng) else null
        )
    }

    var isLocating by remember { mutableStateOf(false) }
    var selectedMarkerSnippet by remember { mutableStateOf<String?>(null) }
    var showNearbyKekes by remember { mutableStateOf(true) }

    // Resolve LatLng coordinates for selected Origin and Destination
    val originCoords = remember(originMarket, originLgaId) {
        val (lat, lng) = AbiaCorridorData.resolveCoordinates(originMarket, originLgaId)
        LatLng(lat, lng)
    }

    val destinationCoords = remember(destinationMarket, destinationLgaId) {
        val (lat, lng) = AbiaCorridorData.resolveCoordinates(destinationMarket, destinationLgaId)
        LatLng(lat, lng)
    }

    // Resolve intermediate stops
    val intermediateLatLngList = remember(intermediateStops, originLgaId) {
        intermediateStops.map { stopName ->
            val (lat, lng) = AbiaCorridorData.resolveCoordinates(stopName, originLgaId)
            Pair(stopName, LatLng(lat, lng))
        }
    }

    // Route polyline sequence (Origin -> Intermediate stops -> Destination)
    val routePoints = remember(originCoords, destinationCoords, intermediateLatLngList) {
        val list = mutableListOf<LatLng>()
        list.add(originCoords)
        intermediateLatLngList.forEach { list.add(it.second) }
        list.add(destinationCoords)
        list
    }

    // Camera initial position between origin and destination
    val midLat = (originCoords.latitude + destinationCoords.latitude) / 2.0
    val midLng = (originCoords.longitude + destinationCoords.longitude) / 2.0
    val initialCenter = LatLng(midLat, midLng)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialCenter, 13f)
    }

    // Vector Canvas Zoom & Pan state
    var vectorScale by remember { mutableFloatStateOf(1.0f) }
    var vectorPanOffset by remember { mutableStateOf(Offset.Zero) }

    val vectorTransformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        vectorScale = (vectorScale * zoomChange).coerceIn(0.6f, 3.5f)
        vectorPanOffset += offsetChange
    }

    // Center camera on user's current coordinates
    val centerOnMyLocation: () -> Unit = {
        coroutineScope.launch {
            if (!LocationTracker.hasLocationPermission(context)) {
                hasLocationPermission = false
                return@launch
            }

            hasLocationPermission = true
            isLocating = true

            // Immediately animate to existing known coordinates if available
            liveUserCoords?.let { current ->
                if (currentMapMode == MapDisplayMode.GOOGLE_MAPS) {
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLngZoom(current, 16.5f),
                        durationMs = 700
                    )
                } else {
                    vectorScale = 1.6f
                    vectorPanOffset = Offset.Zero
                }
                selectedMarkerSnippet = "My Location: ${String.format(Locale.US, "%.4f", current.latitude)}°, ${String.format(Locale.US, "%.4f", current.longitude)}°"
            }

            // Request fresh live device coordinates via LocationTracker
            val freshLoc = LocationTracker.getLiveDeviceLocation(context)
            isLocating = false

            if (freshLoc != null) {
                val freshPos = LatLng(freshLoc.latitude, freshLoc.longitude)
                liveUserCoords = freshPos
                selectedMarkerSnippet = "My Location: ${String.format(Locale.US, "%.4f", freshLoc.latitude)}°, ${String.format(Locale.US, "%.4f", freshLoc.longitude)}° • Abia State"

                if (currentMapMode == MapDisplayMode.GOOGLE_MAPS) {
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLngZoom(freshPos, 16.5f),
                        durationMs = 800
                    )
                } else {
                    vectorScale = 1.8f
                    vectorPanOffset = Offset.Zero
                }
            } else if (liveUserCoords == null) {
                if (currentMapMode == MapDisplayMode.GOOGLE_MAPS) {
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLngZoom(originCoords, 14.5f),
                        durationMs = 600
                    )
                } else {
                    vectorScale = 1.0f
                    vectorPanOffset = Offset.Zero
                }
                Toast.makeText(context, "Acquiring GPS fix... Focused near corridor pick-up ($originMarket)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            perms[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission = granted
        if (granted) {
            centerOnMyLocation()
        } else {
            Toast.makeText(context, "Location permission required to center map on your position", Toast.LENGTH_SHORT).show()
        }
    }

    // Google Maps camera bounds fitting
    LaunchedEffect(originCoords, destinationCoords, intermediateLatLngList, currentMapMode) {
        if (currentMapMode == MapDisplayMode.GOOGLE_MAPS) {
            try {
                val builder = LatLngBounds.Builder()
                builder.include(originCoords)
                builder.include(destinationCoords)
                intermediateLatLngList.forEach { builder.include(it.second) }
                userLiveLat?.let { uLat ->
                    userLiveLng?.let { uLng ->
                        builder.include(LatLng(uLat, uLng))
                    }
                }
                val bounds = builder.build()
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngBounds(bounds, 120),
                    durationMs = 800
                )
            } catch (_: Exception) {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(originCoords, 13f),
                    durationMs = 600
                )
            }
        }
    }

    var mapType by remember { mutableStateOf(MapType.NORMAL) }

    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            compassEnabled = true,
            myLocationButtonEnabled = false,
            mapToolbarEnabled = false,
            rotationGesturesEnabled = true,
            scrollGesturesEnabled = true,
            tiltGesturesEnabled = false,
            zoomGesturesEnabled = true
        )
    }

    val mapProperties = remember(mapType, hasLocationPermission) {
        MapProperties(
            isMyLocationEnabled = hasLocationPermission,
            mapType = mapType,
            minZoomPreference = 8f,
            maxZoomPreference = 19f
        )
    }

    // Simulated commercial Keke tricycles operating along the corridor
    val nearbyTricycles = remember(originCoords) {
        listOf(
            Triple("Keke ATRWAN #102", LatLng(originCoords.latitude + 0.0035, originCoords.longitude + 0.0028), "Plate: ABA-419-KU • 2 mins away"),
            Triple("Keke ATRWAN #218", LatLng(originCoords.latitude - 0.0042, originCoords.longitude + 0.0031), "Plate: ABA-882-KU • 4 mins away"),
            Triple("Keke ATRWAN #305", LatLng(originCoords.latitude + 0.0020, originCoords.longitude - 0.0045), "Plate: UMU-114-TR • 3 mins away"),
            Triple("Keke ATRWAN #410", LatLng(destinationCoords.latitude - 0.0025, destinationCoords.longitude - 0.0020), "Plate: ABA-309-NG • At Drop-off Park")
        )
    }

    // Pulse animation for Vector Canvas keke fleet & user telemetry
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_anim")
    val pulseRadarRadius by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radar_pulse"
    )

    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, AsphaltDivider, RoundedCornerShape(16.dp))
            .testTag("google_transit_map_container")
    ) {
        if (currentMapMode == MapDisplayMode.GOOGLE_MAPS) {
            // GOOGLE MAPS SDK VIEW
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = uiSettings
            ) {
                // Origin Pick-up Marker (Emerald Green)
                Marker(
                    state = rememberMarkerState(position = originCoords),
                    title = "PICK-UP: $originMarket",
                    snippet = "Corridor Origin Point • Abia State",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
                    onClick = {
                        selectedMarkerSnippet = "PICK-UP: $originMarket (${String.format(Locale.US, "%.4f", originCoords.latitude)}°, ${String.format(Locale.US, "%.4f", originCoords.longitude)}°)"
                        false
                    }
                )

                // Intermediate Stops Markers (Orange)
                intermediateLatLngList.forEachIndexed { index, (stopName, stopLatLng) ->
                    Marker(
                        state = rememberMarkerState(position = stopLatLng),
                        title = "Stop ${index + 1}: $stopName",
                        snippet = "Intermediate Passenger Drop / Errand",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE),
                        onClick = {
                            selectedMarkerSnippet = "STOP ${index + 1}: $stopName"
                            false
                        }
                    )
                }

                // Destination Drop-off Marker (Azure Blue)
                Marker(
                    state = rememberMarkerState(position = destinationCoords),
                    title = "DROP-OFF: $destinationMarket",
                    snippet = "Corridor Destination Point • Abia State",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                    onClick = {
                        selectedMarkerSnippet = "DROP-OFF: $destinationMarket (${String.format(Locale.US, "%.4f", destinationCoords.latitude)}°, ${String.format(Locale.US, "%.4f", destinationCoords.longitude)}°)"
                        false
                    }
                )

                // User Hardware GPS Location (Rose marker + radar circle)
                val userDisplayLocation = liveUserCoords ?: if (userLiveLat != null && userLiveLng != null) LatLng(userLiveLat, userLiveLng) else null
                if (userDisplayLocation != null) {
                    Marker(
                        state = rememberMarkerState(position = userDisplayLocation),
                        title = "Your Current Position",
                        snippet = "Live GPS Telemetry • Abia State",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE),
                        onClick = {
                            selectedMarkerSnippet = "Your Location (${String.format(Locale.US, "%.4f", userDisplayLocation.latitude)}°, ${String.format(Locale.US, "%.4f", userDisplayLocation.longitude)}°)"
                            false
                        }
                    )
                    Circle(
                        center = userDisplayLocation,
                        radius = 80.0,
                        fillColor = EmeraldGreen.copy(alpha = 0.2f),
                        strokeColor = EmeraldGreen,
                        strokeWidth = 2f
                    )
                }

                // Nearby Commercial Tricycles
                if (showNearbyKekes) {
                    nearbyTricycles.forEach { (label, kekePos, details) ->
                        Marker(
                            state = rememberMarkerState(position = kekePos),
                            title = label,
                            snippet = details,
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW),
                            onClick = {
                                selectedMarkerSnippet = "$label — $details"
                                false
                            }
                        )
                    }
                }

                // Tricycle Corridor Polyline
                Polyline(
                    points = routePoints,
                    color = SafetyAmber,
                    width = 12f,
                    geodesic = true
                )
                Polyline(
                    points = routePoints,
                    color = SafetyAmberDark.copy(alpha = 0.6f),
                    width = 18f,
                    geodesic = true
                )
            }
        } else {
            // INTERACTIVE VECTOR TRANSIT CANVAS (Offline Resilience & Zero Auth Failures)
            val allBoundsPoints = remember(originCoords, destinationCoords, intermediateLatLngList, liveUserCoords) {
                val list = mutableListOf(originCoords, destinationCoords)
                intermediateLatLngList.forEach { list.add(it.second) }
                liveUserCoords?.let { list.add(it) }
                list
            }

            val minLat = allBoundsPoints.minOf { it.latitude }
            val maxLat = allBoundsPoints.maxOf { it.latitude }
            val minLng = allBoundsPoints.minOf { it.longitude }
            val maxLng = allBoundsPoints.maxOf { it.longitude }

            val latSpan = (maxLat - minLat).coerceAtLeast(0.008)
            val lngSpan = (maxLng - minLng).coerceAtLeast(0.008)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AsphaltBlack)
                    .transformable(state = vectorTransformState)
                    .pointerInput(Unit) {
                        detectTapGestures {
                            selectedMarkerSnippet = null
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val padding = 56f
                    val availableW = w - (padding * 2)
                    val availableH = h - (padding * 2)

                    fun project(pos: LatLng): Offset {
                        val normX = ((pos.longitude - minLng) / lngSpan).toFloat().coerceIn(0f, 1f)
                        val normY = (1f - ((pos.latitude - minLat) / latSpan).toFloat()).coerceIn(0f, 1f)
                        val rawX = padding + (normX * availableW)
                        val rawY = padding + (normY * availableH)
                        val centerX = w / 2f
                        val centerY = h / 2f
                        val finalX = centerX + (rawX - centerX) * vectorScale + vectorPanOffset.x
                        val finalY = centerY + (rawY - centerY) * vectorScale + vectorPanOffset.y
                        return Offset(finalX, finalY)
                    }

                    // Grid lines
                    val gridSize = 40f * vectorScale
                    var gx = (w / 2f + vectorPanOffset.x) % gridSize
                    while (gx < w) {
                        drawLine(Color(0xFF1E1E1E), Offset(gx, 0f), Offset(gx, h), strokeWidth = 1f)
                        gx += gridSize
                    }
                    var gy = (h / 2f + vectorPanOffset.y) % gridSize
                    while (gy < h) {
                        drawLine(Color(0xFF1E1E1E), Offset(0f, gy), Offset(w, gy), strokeWidth = 1f)
                        gy += gridSize
                    }

                    // Corridor polyline
                    val projectedPath = Path()
                    val pOrigin = project(originCoords)
                    projectedPath.moveTo(pOrigin.x, pOrigin.y)
                    intermediateLatLngList.forEach {
                        val p = project(it.second)
                        projectedPath.lineTo(p.x, p.y)
                    }
                    val pDest = project(destinationCoords)
                    projectedPath.lineTo(pDest.x, pDest.y)

                    // Outer amber glow
                    drawPath(
                        path = projectedPath,
                        color = SafetyAmberDark.copy(alpha = 0.45f),
                        style = Stroke(width = 10f * vectorScale)
                    )
                    // Core route line
                    drawPath(
                        path = projectedPath,
                        color = SafetyAmber,
                        style = Stroke(width = 4.5f * vectorScale)
                    )

                    // Origin Node (Emerald Green)
                    drawCircle(
                        color = EmeraldGreen.copy(alpha = 0.28f),
                        radius = 18f * vectorScale,
                        center = pOrigin
                    )
                    drawCircle(
                        color = EmeraldGreen,
                        radius = 7.5f * vectorScale,
                        center = pOrigin
                    )

                    // Intermediate Stops (Safety Amber / Orange)
                    intermediateLatLngList.forEachIndexed { idx, stop ->
                        val p = project(stop.second)
                        drawCircle(
                            color = SafetyAmber.copy(alpha = 0.25f),
                            radius = 14f * vectorScale,
                            center = p
                        )
                        drawCircle(
                            color = SafetyAmber,
                            radius = 5.5f * vectorScale,
                            center = p
                        )
                    }

                    // Destination Node (Azure Blue)
                    drawCircle(
                        color = Color(0xFF0091EA).copy(alpha = 0.3f),
                        radius = 18f * vectorScale,
                        center = pDest
                    )
                    drawCircle(
                        color = Color(0xFF0091EA),
                        radius = 7.5f * vectorScale,
                        center = pDest
                    )

                    // Nearby Keke Tricycles
                    if (showNearbyKekes) {
                        nearbyTricycles.forEach { (_, kekePos, _) ->
                            val p = project(kekePos)
                            drawCircle(
                                color = SafetyAmber.copy(alpha = 0.35f),
                                radius = pulseRadarRadius * vectorScale,
                                center = p
                            )
                            drawCircle(
                                color = SafetyAmber,
                                radius = 4f * vectorScale,
                                center = p
                            )
                        }
                    }

                    // User GPS Telemetry (Rose / Emerald)
                    val userPt = liveUserCoords ?: if (userLiveLat != null && userLiveLng != null) LatLng(userLiveLat, userLiveLng) else null
                    if (userPt != null) {
                        val p = project(userPt)
                        drawCircle(
                            color = EmeraldGreen.copy(alpha = 0.2f),
                            radius = (pulseRadarRadius + 8f) * vectorScale,
                            center = p
                        )
                        drawCircle(
                            color = Color(0xFFFF4081),
                            radius = 6.5f * vectorScale,
                            center = p
                        )
                    }

                    // Node Labels
                    val labelStyle = TextStyle(
                        color = HighContrastWhite,
                        fontSize = (10f * vectorScale).coerceIn(9f, 13f).sp,
                        fontWeight = FontWeight.Bold
                    )
                    val originLayout = textMeasurer.measure(text = "Pick-up: $originMarket", style = labelStyle)
                    drawText(originLayout, topLeft = Offset(pOrigin.x + 10f, pOrigin.y - 10f))

                    val destLayout = textMeasurer.measure(text = "Drop-off: $destinationMarket", style = labelStyle)
                    drawText(destLayout, topLeft = Offset(pDest.x + 10f, pDest.y - 10f))
                }
            }
        }

        // Top Status Header Overlay
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp),
            color = AsphaltDark.copy(alpha = 0.94f),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SafetyAmber.copy(alpha = 0.6f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (currentMapMode == MapDisplayMode.GOOGLE_MAPS) EmeraldGreen else SafetyAmber)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (currentMapMode == MapDisplayMode.GOOGLE_MAPS) "GOOGLE MAPS SDK • ABIA" else "VECTOR TRANSIT MAP • ABIA",
                    color = HighContrastWhite,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // Top Controls: Mode Switcher, Map Type, Fleet toggle & Key Info
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Mode Switcher (Google Maps SDK <-> Vector Canvas)
            IconButton(
                onClick = {
                    currentMapMode = if (currentMapMode == MapDisplayMode.GOOGLE_MAPS) {
                        MapDisplayMode.VECTOR_TRANSIT
                    } else {
                        MapDisplayMode.GOOGLE_MAPS
                    }
                },
                modifier = Modifier
                    .size(34.dp)
                    .background(AsphaltDark.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                    .border(1.dp, SafetyAmber.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .testTag("switch_map_mode_button")
            ) {
                Icon(
                    imageVector = if (currentMapMode == MapDisplayMode.GOOGLE_MAPS) Icons.Default.AltRoute else Icons.Default.Map,
                    contentDescription = "Switch Map Engine",
                    tint = SafetyAmber,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Google Maps Type Toggle (only in Google Maps mode)
            if (currentMapMode == MapDisplayMode.GOOGLE_MAPS) {
                IconButton(
                    onClick = {
                        mapType = if (mapType == MapType.NORMAL) MapType.HYBRID else MapType.NORMAL
                    },
                    modifier = Modifier
                        .size(34.dp)
                        .background(AsphaltDark.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                        .testTag("toggle_map_type_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "Switch Map Layer",
                        tint = SafetyAmber,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Fleet visibility toggle
            IconButton(
                onClick = { showNearbyKekes = !showNearbyKekes },
                modifier = Modifier
                    .size(34.dp)
                    .background(AsphaltDark.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                    .testTag("toggle_keke_fleet_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ElectricRickshaw,
                    contentDescription = "Toggle Nearby Fleet",
                    tint = if (showNearbyKekes) EmeraldGreenLight else MutedSilver,
                    modifier = Modifier.size(18.dp)
                )
            }

            // API Key Info / Setup Guide button
            IconButton(
                onClick = { showKeySetupDialog = true },
                modifier = Modifier
                    .size(34.dp)
                    .background(AsphaltDark.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                    .testTag("maps_key_info_button")
            ) {
                Icon(
                    imageVector = Icons.Default.VpnKey,
                    contentDescription = "Maps Key Guide",
                    tint = if (isMapsKeyConfigured) EmeraldGreenLight else SafetyAmber,
                    modifier = Modifier.size(17.dp)
                )
            }
        }

        // Bottom Controls: My Location, Re-center Route, & Zoom
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // 'My Location' button (Centers camera/canvas on user coordinates)
            IconButton(
                onClick = {
                    if (!hasLocationPermission) {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    } else {
                        centerOnMyLocation()
                    }
                },
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        if (liveUserCoords != null) EmeraldGreen.copy(alpha = 0.25f) else AsphaltCard,
                        RoundedCornerShape(8.dp)
                    )
                    .border(
                        1.dp,
                        if (liveUserCoords != null) EmeraldGreen else AsphaltDivider,
                        RoundedCornerShape(8.dp)
                    )
                    .testTag("my_location_button")
            ) {
                if (isLocating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = EmeraldGreen,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "My Location",
                        tint = if (liveUserCoords != null) EmeraldGreenLight else SafetyAmber,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Re-center & Fit Corridor
            IconButton(
                onClick = {
                    if (currentMapMode == MapDisplayMode.GOOGLE_MAPS) {
                        coroutineScope.launch {
                            try {
                                val builder = LatLngBounds.Builder()
                                builder.include(originCoords)
                                builder.include(destinationCoords)
                                intermediateLatLngList.forEach { builder.include(it.second) }
                                cameraPositionState.animate(
                                    update = CameraUpdateFactory.newLatLngBounds(builder.build(), 120),
                                    durationMs = 600
                                )
                            } catch (_: Exception) {
                                cameraPositionState.animate(
                                    update = CameraUpdateFactory.newLatLngZoom(originCoords, 14f)
                                )
                            }
                        }
                    } else {
                        vectorScale = 1.0f
                        vectorPanOffset = Offset.Zero
                    }
                },
                modifier = Modifier
                    .size(36.dp)
                    .background(AsphaltCard, RoundedCornerShape(8.dp))
                    .border(1.dp, AsphaltDivider, RoundedCornerShape(8.dp))
                    .testTag("center_route_button")
            ) {
                Icon(
                    imageVector = Icons.Default.GpsFixed,
                    contentDescription = "Fit Route",
                    tint = SafetyAmber,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Zoom In
            IconButton(
                onClick = {
                    if (currentMapMode == MapDisplayMode.GOOGLE_MAPS) {
                        coroutineScope.launch {
                            cameraPositionState.animate(
                                update = CameraUpdateFactory.zoomIn(),
                                durationMs = 300
                            )
                        }
                    } else {
                        vectorScale = (vectorScale * 1.25f).coerceAtMost(3.5f)
                    }
                },
                modifier = Modifier
                    .size(36.dp)
                    .background(AsphaltCard, RoundedCornerShape(8.dp))
                    .border(1.dp, AsphaltDivider, RoundedCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Zoom In",
                    tint = HighContrastWhite,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Zoom Out
            IconButton(
                onClick = {
                    if (currentMapMode == MapDisplayMode.GOOGLE_MAPS) {
                        coroutineScope.launch {
                            cameraPositionState.animate(
                                update = CameraUpdateFactory.zoomOut(),
                                durationMs = 300
                            )
                        }
                    } else {
                        vectorScale = (vectorScale / 1.25f).coerceAtLeast(0.6f)
                    }
                },
                modifier = Modifier
                    .size(36.dp)
                    .background(AsphaltCard, RoundedCornerShape(8.dp))
                    .border(1.dp, AsphaltDivider, RoundedCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Zoom Out",
                    tint = HighContrastWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Active Marker Tooltip Banner
        AnimatedVisibility(
            visible = selectedMarkerSnippet != null,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
                .fillMaxWidth(0.75f),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                color = AsphaltDark.copy(alpha = 0.95f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = selectedMarkerSnippet ?: "",
                        color = HighContrastWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { selectedMarkerSnippet = null },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = MutedSilver,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }

    // Google Maps Key Setup & Diagnostics Modal
    if (showKeySetupDialog) {
        Dialog(onDismissRequest = { showKeySetupDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = AsphaltDark,
                border = androidx.compose.foundation.BorderStroke(1.dp, SafetyAmber),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = SafetyAmber,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Google Maps SDK Setup",
                            style = MaterialTheme.typography.titleMedium,
                            color = HighContrastWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "To enable satellite/road tiles via Google Maps SDK v2, add your API key in the AI Studio Secrets panel under MAPS_API_KEY.",
                        color = MutedSilver,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Surface(
                        color = AsphaltBlack,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Package Name:",
                                color = MutedSilver,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "com.aistudio.kekego.trptxa",
                                color = SafetyAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "SHA-1 Certificate Fingerprint:",
                                color = MutedSilver,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "A9:5D:46:DD:FC:93:B8:77:A8:68:84:31:4A:19:6E:97:7F:B6:C1:60",
                                color = EmeraldGreenLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(
                                    AnnotatedString(
                                        "Package: com.aistudio.kekego.trptxa\nSHA-1: A9:5D:46:DD:FC:93:B8:77:A8:68:84:31:4A:19:6E:97:7F:B6:C1:60\nEnv Variable: MAPS_API_KEY"
                                    )
                                )
                                Toast.makeText(context, "Credentials copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy Details", fontSize = 11.sp)
                        }

                        Button(
                            onClick = { showKeySetupDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = SafetyAmber, contentColor = AsphaltBlack),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Done", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
