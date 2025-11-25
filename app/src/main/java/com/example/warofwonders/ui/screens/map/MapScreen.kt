package com.example.warofwonders.ui.screens.map

import android.Manifest
import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import coil.compose.AsyncImage
import com.example.warofwonders.ui.components.AlertDialogPopup
import com.example.warofwonders.ui.screens.map.components.TextFieldSearch
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.example.warofwonders.R
import com.example.warofwonders.ui.screens.map.components.CreatureAlert
import com.example.warofwonders.ui.screens.map.components.FloatingButton
import com.example.warofwonders.ui.screens.map.components.FriendsBottomSheet
import com.example.warofwonders.ui.screens.map.components.ImageIconButton
import com.example.warofwonders.ui.shared.utils.bitmapDescriptorFromVector
import com.example.warofwonders.ui.shared.utils.distanceBetween
import com.example.warofwonders.ui.shared.utils.isPermissionGranted
import com.example.warofwonders.ui.theme.Cyan
import com.example.warofwonders.ui.theme.Gray
import com.example.warofwonders.ui.theme.Green
import com.example.warofwonders.ui.theme.White
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberUpdatedMarkerState

@Composable
fun MapScreen(
    viewModel: MapViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION

    LaunchedEffect(Unit) {
        viewModel.updatePermissionStatus(isPermissionGranted(context, locationPermission))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.updatePermissionStatus(granted)
        if (granted) viewModel.toggleLocationUpdates()
    }

    var showRationale by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        viewModel.startLightSensor()
        viewModel.startBarometerSensor()
        viewModel.startTemperatureSensor()
        viewModel.startMagnetometerSensor()
        onDispose { viewModel.stopLightSensor()
            viewModel.stopBarometerSensor()
            viewModel.stopTemperatureSensor()
            viewModel.stopMagnetometerSensor()}
    }

    MapScreenContent(
        uiState = uiState,
        onPlaceTextFieldChange = { newText -> viewModel.updatePlaceQuery(newText) },
        onSearchPlace = { newQuery -> viewModel.searchPlaceQuery(newQuery) },
        onRequestPermission = {
            if (shouldShowRequestPermissionRationale(context as Activity, locationPermission)) {
                showRationale = true
            } else {
                permissionLauncher.launch(locationPermission)
            }
        },
        onClickLocationUpdates = {
            if (uiState.permissionStatus) {
                viewModel.toggleLocationUpdates()
            } else {
                permissionLauncher.launch(locationPermission)
            }
        },
        onMapClick = { viewModel.clearMap() },
        onMapLongClick = { pos -> viewModel.addTargetMarker(pos) },
        viewModel = viewModel
    )

    if (showRationale) {
        AlertDialogPopup(
            title = "Permission Required",
            message = "This app needs your location permission to work properly.",
            onAccept = {
                showRationale = false
                permissionLauncher.launch(locationPermission)
            },
            onCancel = { showRationale = false }
        )
    }
}

@Composable
fun MapScreenContent(
    uiState: MapUiState,
    onPlaceTextFieldChange: (String) -> Unit,
    onSearchPlace: (String) -> Unit,
    onRequestPermission: () -> Unit,
    onClickLocationUpdates: () -> Unit,
    onMapClick: () -> Unit,
    onMapLongClick: (LatLng) -> Unit,
    viewModel: MapViewModel
) {
    val context = LocalContext.current
    val location = LatLng(uiState.currentLocation.latitude, uiState.currentLocation.longitude)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(location, uiState.cameraZoom)
    }

    var uiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                compassEnabled = false,
                mapToolbarEnabled = false,
                zoomControlsEnabled = false,
            )
        )
    }

    var showFriendsModal by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.cameraTarget) {
        uiState.cameraTarget?.let { target ->
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(target, uiState.cameraZoom),
                durationMs = 1000
            )
        }
    }

    Box(Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = uiSettings,
            properties = MapProperties(
                mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, uiState.mapStyleRes)
            ),
            onMapClick = {
                onMapClick()
                viewModel.stopSelectedFriendListener()
            },
            onMapLongClick = { pos -> onMapLongClick(pos) }
        ) {
            Marker(
                state = rememberUpdatedMarkerState(position = location),
                icon = bitmapDescriptorFromVector(
                    context,
                    if (uiState.isUpdatingLocation) R.drawable.twotone_circle_blue
                    else R.drawable.twotone_circle_gray,
                    maxDp = 18f
                )
            )

            uiState.targetMarker?.let { marker ->
                Marker(
                    state = rememberUpdatedMarkerState(position = marker.position),
                    title = marker.title,
                    snippet = marker.snippet
                )
            }

            if (uiState.routePoints.isNotEmpty()) {
                Polyline(
                    points = uiState.routePoints,
                    color = Cyan,
                    width = 8f
                )
            }

            val clanColors = listOf(
                Color(0xFF489ECC),
                Color(0xFF66DC4B),
            )

            uiState.clans.forEachIndexed { index, clan ->
                val points = clan.zona.map { LatLng(it.latitude, it.longitude) }
                val color = clanColors[index % clanColors.size]

                Polygon(
                    points = points,
                    fillColor = color.copy(alpha = 0.15f),
                    strokeColor = color,
                    strokeWidth = 3f,
                    clickable = true,
                    onClick = {
                        Toast.makeText(context, clan.nombre, Toast.LENGTH_SHORT).show()
                    }
                )
            }

            uiState.interestPoint.forEach { poi ->
                Marker(
                    state = rememberUpdatedMarkerState(
                        position = LatLng(poi.lat, poi.lng)
                    ),
                    title = poi.name,
                    snippet = poi.address,
                    icon = poi.icon?.let { bitmap ->
                        BitmapDescriptorFactory.fromBitmap(bitmap)
                    }
                )
            }

            uiState.selectedFriendMarker?.let { friend ->
                Marker(
                    state = rememberUpdatedMarkerState(
                        position = LatLng(friend.latitude ?: 0.0, friend.longitude ?: 0.0)
                    ),
                    title = "${friend.name} ${friend.lastname}",
                    snippet = friend.email,
                    icon = bitmapDescriptorFromVector(context, R.drawable.gemaamazul, maxDp = 28f)
                )
            }
        }

        Column(
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp).wrapContentSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FloatingButton(
                onClick = {
                    showFriendsModal = true
                    viewModel.observeFriendsRealtime()
                },
                modifier = Modifier.size(62.dp),
                icon = Icons.Default.People,
                contentDescription = "Ver amigos activos",
                contentColor = White,
                backgroundImage = painterResource(id = R.drawable.chatbutton)
            )

            FloatingButton(
                onClick = {
                    viewModel.loadInterestPoints()
                },
                modifier = Modifier.size(62.dp),
                icon = Icons.Default.TravelExplore,
                contentDescription = "Start Updating Location",
                contentColor = White,
                backgroundImage = painterResource(id = R.drawable.chatbutton)
            )

            FloatingButton(
                onClick = {
                    if (uiState.permissionStatus) onClickLocationUpdates()
                    else onRequestPermission()
                },
                modifier = Modifier.size(62.dp),
                icon = Icons.Default.MyLocation,
                contentDescription = "Start Updating Location",
                contentColor = if (uiState.isUpdatingLocation) Cyan else White,
                backgroundImage = painterResource(id = R.drawable.chatbutton)
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp, horizontal = 36.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.End
        ) {
            TextFieldSearch(
                place = uiState.placeQuery,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                placeholderText = "Search",
                onPlaceChange = { onPlaceTextFieldChange(it) },
                onSearchAction = { onSearchPlace(it) },
                backgroundImage = painterResource(id = R.drawable.textfield_image)
            )

            ImageIconButton(
                onClick = { viewModel.setUserActiveState(!uiState.isActive) },
                modifier = Modifier.size(72.dp, 52.dp),
                backgroundImage = painterResource(id = R.drawable.slots),
                icon = if (uiState.isActive) Icons.Default.ToggleOn else Icons.Default.ToggleOff,
                iconTint = if (uiState.isActive) Green else Gray,
                iconSize = 42.dp
            )
        }

        if (showFriendsModal) {
            FriendsBottomSheet(
                uiState = uiState,
                onClose = {
                    showFriendsModal = false
                    viewModel.stopFriendsListener()
                },
                onShowFriendOnMap = { friendUid ->
                    showFriendsModal = false
                    viewModel.observeSelectedFriendRealtime(friendUid)
                    viewModel.stopFriendsListener()
                }
            )
        }
    }

    if (uiState.alreadyOwnedCreature) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFc79e63),
                tonalElevation = 4.dp,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .widthIn(min = 260.dp, max = 320.dp)
                    .wrapContentHeight()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "¡YA TIENES ESTA CRIATURA!",
                        color = Color.Black
                    )

                    Text(
                        text = "Esta criatura ya pertenece a tu inventario.",
                        color = Color.Black,
                        modifier = Modifier.padding(top = 8.dp)
                    )


                    uiState.criaturaDetectada?.let { criatura ->

                        AsyncImage(
                            model = criatura.imagen,
                            contentDescription = criatura.nombre,
                            modifier = Modifier
                                .padding(top = 12.dp)
                                .size(140.dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Button(
                        modifier = Modifier.padding(top = 16.dp),
                        onClick = { viewModel.resetAlreadyOwned() }
                    ) {
                        Text("Aceptar")
                    }
                }
            }
        }
    }


    when {
        uiState.pressureCreatureFound -> CreatureAlert(
            message = "¡HA APARECIDO UNA CRIATURA DE PRESION ALTA!",
            onDismiss = { viewModel.showPressureCreatureAlert(false) },
            onAccept = {
                viewModel.capturePressureCreature()
                viewModel.showPressureCreatureAlert(false)
            }
        )

        uiState.coldCreatureFound -> CreatureAlert(
            message = "¡HA APARECIDO UNA CRIATURA BIEN COOL!",
            onDismiss = { viewModel.showColdCreatureAlert(false) },
            onAccept = {
                viewModel.captureColdCreature()
                viewModel.showColdCreatureAlert(false)
            }
        )

        uiState.hotCreatureFound -> CreatureAlert(
            message = "¡HA APARECIDO UNA CRIATURA BIEN ARDIENTE!",
            onDismiss = { viewModel.showHotCreatureAlert(false) },
            onAccept = {
                viewModel.captureHotCreature()
                viewModel.showHotCreatureAlert(false)
            }
        )

        uiState.mediumCreatureFound -> CreatureAlert(
            message = "¡HA APARECIDO UNA CRIATURA DE CLIMA MEDIO!",
            onDismiss = { viewModel.showMediumCreatureAlert(false) },
            onAccept = {
                viewModel.captureMediumCreature()
                viewModel.showMediumCreatureAlert(false)
            }
        )

        uiState.armorFound -> CreatureAlert(
            message = "¡HAN APARECIDO UNAS PODEROSAS ARMADURAS!",
            onDismiss = { viewModel.findArmor(false) },
            onAccept = {
                viewModel.captureArmor()
                viewModel.findArmor(false)
            }
        )
    }

    if (uiState.mostrarPopupRecurso) {
        AlertDialog(
            onDismissRequest = { viewModel.rechazarRecurso() },
            containerColor = Color(0x66C79E63),
            title = {
                Text(
                    "¡Recurso encontrado!",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        uiState.recursoEncontrado?.nombre ?: "",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    val imagenUrl = uiState.recursoEncontrado?.imagen ?: ""

                    if (imagenUrl.isNotEmpty()) {
                        AsyncImage(
                            model = imagenUrl, // URL de Firebase Storage
                            contentDescription = uiState.recursoEncontrado?.nombre,
                            modifier = Modifier
                                .padding(top = 12.dp)
                                .size(120.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.capturarRecursoDesdeUI() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF342711),
                        contentColor = Color.White
                    )
                ) {
                    Text("Tomar recurso")
                }
            },
            dismissButton = {
                Button(
                    onClick = { viewModel.rechazarRecurso() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF342711),
                        contentColor = Color.White
                    )
                ) {
                    Text("Rechazar")
                }
            }
        )
    }

    LaunchedEffect(uiState.isHigh) {
        if (uiState.isHigh) {
            viewModel.showPressureCreatureAlert(true)
        }
    }

    LaunchedEffect(uiState.isCold) {
        if (uiState.isCold) {
            viewModel.showColdCreatureAlert(true)
        }
    }

    LaunchedEffect(uiState.isHot) {
        if (uiState.isHot) {
            viewModel.showHotCreatureAlert(true)
        }
    }

    LaunchedEffect(uiState.isMagn) {
        if (uiState.isMagn) {
            viewModel.findArmor(true)
        }
    }

    LaunchedEffect(uiState.isMedium) {
        if (uiState.isMedium) {
            viewModel.showMediumCreatureAlert(true)
        }
    }
}