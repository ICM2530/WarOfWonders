package com.example.warofwonders.ui.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.warofwonders.data.repository.GeoRepository
import com.example.warofwonders.data.repository.InterestPointRepository
import com.example.warofwonders.data.repository.LocationRepository
import com.example.warofwonders.data.source.hardware.LightSensorDataSource
import com.example.warofwonders.data.source.hardware.BarometerSensorDataSource
import com.example.warofwonders.data.source.hardware.TemperatureSensorDataSource
import com.example.warofwonders.data.source.hardware.MagnetometerDataSource
import com.example.warofwonders.ui.model.Criatura
import com.example.warofwonders.ui.model.InventarioViewModel
import com.example.warofwonders.ui.model.TipoCriatura
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

import com.google.maps.android.PolyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.OkHttpClient
import okhttp3.Request

class MapViewModel(
    private val locationRepository: LocationRepository,
    private val geoRepository: GeoRepository,
    private val barometerSensorDataSource: BarometerSensorDataSource,
    private val temperatureSensorDataSource: TemperatureSensorDataSource,
    private val magnetometerDataSource: MagnetometerDataSource,
    private val lightSensorDataSource: LightSensorDataSource,
    private val interestPointRepository: InterestPointRepository,
    private val inventarioVM: InventarioViewModel

) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState




    private val auth = FirebaseAuth.getInstance()

    private val realtimeDB = FirebaseDatabase.getInstance().reference
    private var criaturasDisponibles: List<Criatura> = emptyList()

    init {
        loadInterestPoints()
        loadCreaturesFromFirebaseRealtime()

        viewModelScope.launch {
            inventarioVM.cargarInventario()
        }
    }

    //cargar las criaturas desde la base de datos pero realtime ahora
    private fun loadCreaturesFromFirebaseRealtime() {
        viewModelScope.launch(Dispatchers.IO) {
            realtimeDB.child("criaturas_disponibles").get().addOnSuccessListener { snapshot ->
                val lista = mutableListOf<Criatura>()
                snapshot.children.forEach { child ->
                    val criatura = child.getValue(Criatura::class.java)
                    if (criatura != null) lista.add(criatura)
                }
                criaturasDisponibles = lista
            }
        }
    }

    //cargar las criaturas segun el sensor

    suspend fun detectarCriaturasPorSensor(tipo: TipoCriatura, inventarioVM: InventarioViewModel) {
        val ref = realtimeDB.child("criaturas_disponibles")

        val snapshot = ref.get().await()
        val lista = snapshot.children.mapNotNull { it.getValue(Criatura::class.java) }

        val filtradas = lista.filter { it.tipo.equals(tipo.name, ignoreCase = true) }

        if (filtradas.isEmpty()) return

        val inventarioActual = inventarioVM.inventario.value.criaturas

        // escogemos una criatura del tipo SIEMPRE
        val seleccionada = filtradas.random()

        // Verificar si ya existe en el inventario
        val yaExiste = inventarioActual.any { it.nombre == seleccionada.nombre }

        if (yaExiste) {
            // Mostrar la misma criatura repetida
            _uiState.update {
                it.copy(
                    alreadyOwnedCreature = true,
                    criaturaDetectada = seleccionada
                )
            }
            return
        }

        inventarioVM.agregarCriatura(seleccionada)



        _uiState.update {
            it.copy(
                criaturaDetectada = seleccionada
            )
        }
    }







    fun captureColdCreature() {
        _uiState.value = _uiState.value.copy(
            coldCreatureCaptured = true,
            coldCreatureFound = false
        )

        viewModelScope.launch {
            detectarCriaturasPorSensor(TipoCriatura.FRIO, inventarioVM)
        }
    }


    fun captureHotCreature() {
        _uiState.value = _uiState.value.copy(
            hotCreatureCaptured = true,
            hotCreatureFound = false
        )

        viewModelScope.launch {
            detectarCriaturasPorSensor(TipoCriatura.CALOR, inventarioVM)
        }
    }

    fun captureMediumCreature() {
        _uiState.value = _uiState.value.copy(
            mediumCreatureCaptured = true,
            mediumCreatureFound = false
        )

        viewModelScope.launch {
            detectarCriaturasPorSensor(TipoCriatura.MEDIO, inventarioVM)
        }
    }


    fun capturePressureCreature() {
        _uiState.value = _uiState.value.copy(
            pressureCreatureCaptured = true,
            pressureCreatureFound = false
        )

        viewModelScope.launch {
            detectarCriaturasPorSensor(TipoCriatura.PRESION, inventarioVM)
        }
    }


    private fun loadInterestPoints() {
        viewModelScope.launch(Dispatchers.IO) {
            val puntos = interestPointRepository.readJSONFile()
            _uiState.update { it.copy(staticMarkers = puntos) }
        }
    }

    fun updatePermissionStatus(granted: Boolean) {
        _uiState.update { it.copy(permissionStatus = granted) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleLocationUpdates() {
        val updating = _uiState.value.locationUpdates
        if (updating) stopLocationUpdates() else startLocationUpdates()
    }

    private fun startLocationUpdates() {
        _uiState.update { it.copy(locationUpdates = true, isCameraFollowing = true) }
        locationRepository.startLocationUpdates { location ->
            _uiState.update { state ->
                state.copy(
                    currentLocation = location,
                )
            }
        }
    }

    private fun stopLocationUpdates() {
        locationRepository.stopLocationUpdates()
        _uiState.update { it.copy(locationUpdates = false, isCameraFollowing = false) }
    }

    fun searchLocation() {
        val query = _uiState.value.searchQuery
        if (query.isBlank()) return

        val origin = LatLng(
            _uiState.value.currentLocation.latitude,
            _uiState.value.currentLocation.longitude
        )

        _uiState.update { it.copy(isSearching = true) }

        viewModelScope.launch(Dispatchers.IO) {
            val result = geoRepository.getLocationFromAddress(query)
            result?.let { destination ->
                _uiState.update { state ->
                    state.copy(
                        searchMarker = destination,
                        routePoints = emptyList(),
                    )
                }

                if (origin.latitude != 0.0 && origin.longitude != 0.0) {
                    val route = fetchRoute(origin, destination)
                    _uiState.update { state -> state.copy(routePoints = route) }
                }
            }
            _uiState.update { it.copy(isSearching = false) }
        }
    }

    fun onMapLongClick(latLng: LatLng) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { state ->
                state.copy(
                    clickMarker = latLng,
                    routePoints = emptyList()
                )
            }

            val origin = LatLng(
                _uiState.value.currentLocation.latitude,
                _uiState.value.currentLocation.longitude
            )

            if (origin.latitude != 0.0 && origin.longitude != 0.0) {
                val route = fetchRoute(origin, latLng)
                _uiState.update { state -> state.copy(routePoints = route) }
            }
        }
    }

    private fun fetchRoute(origin: LatLng, destination: LatLng): List<LatLng> {
        return try {
            val client = OkHttpClient()
            val url = "https://router.project-osrm.org/route/v1/driving/" +
                    "${origin.longitude},${origin.latitude};${destination.longitude},${destination.latitude}" +
                    "?overview=full&geometries=polyline"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return emptyList()
            val json = org.json.JSONObject(body)
            val routes = json.getJSONArray("routes")
            if (routes.length() > 0) {
                val geometry = routes.getJSONObject(0).getString("geometry")
                PolyUtil.decode(geometry)
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clearMarkers() {
        _uiState.update { it.copy(
            searchMarker = null,
            clickMarker = null,
            routePoints = emptyList(),
            isCameraFollowing = false
        ) }
    }

    fun startBarometerSensor() {
        barometerSensorDataSource.startListening { hPa ->
            val isHigh = hPa > 1000
            if (isHigh != _uiState.value.isHigh) {
                _uiState.update { it.copy(isHigh = isHigh) }
            }
        }
    }

    fun stopBarometerSensor() {
        barometerSensorDataSource.stopListening()
    }

    fun startTemperatureSensor() {
        temperatureSensorDataSource.startListening { cel ->

            val isCold = cel < 15
            val isHot = cel > 30
            val isMedium = cel in 15.0..30.0

            val current = _uiState.value

            if (isCold != current.isCold ||
                isHot != current.isHot ||
                isMedium != current.isMedium
            ) {
                _uiState.update {
                    it.copy(
                        isCold = isCold,
                        isHot = isHot,
                        isMedium = isMedium
                    )
                }
            }
        }
    }


    fun stopTemperatureSensor() {
        temperatureSensorDataSource.stopListening()
    }

    fun startMagnetometerSensor() {
        magnetometerDataSource.startListening { magn ->
            val isMagn = magn > 60
            if (isMagn != _uiState.value.isMagn) {
                _uiState.update { it.copy(isMagn = isMagn) }
            }
        }
    }

    fun stopMagnetometerSensor() {
        magnetometerDataSource.stopListening()
    }

    fun startLightSensor() {
        lightSensorDataSource.startListening { lux ->
            val isDark = lux < 2000f
            if (isDark != _uiState.value.isDarkMap) {
                _uiState.update { it.copy(isDarkMap = isDark) }
            }
        }
    }

    fun stopLightSensor() {
        lightSensorDataSource.stopListening()
    }

    override fun onCleared() {
        stopLightSensor()
        stopBarometerSensor()
        stopTemperatureSensor()
        stopMagnetometerSensor()
        stopLocationUpdates()
        super.onCleared()
    }

    //show alert

    fun showPressureCreatureAlert(show: Boolean) {
        _uiState.value = _uiState.value.copy(pressureCreatureFound = show)
    }

    fun showColdCreatureAlert(show: Boolean) {
        _uiState.value = _uiState.value.copy(coldCreatureFound = show)
    }

    fun showHotCreatureAlert(show: Boolean) {
        _uiState.value = _uiState.value.copy(hotCreatureFound = show)
    }

    fun showMediumCreatureAlert(show: Boolean) {
        _uiState.value = _uiState.value.copy(mediumCreatureFound = show)
    }




    fun findArmor(show: Boolean) {
        _uiState.value = _uiState.value.copy(armorFound = show)
    }

    fun captureArmor() {
        _uiState.value = _uiState.value.copy(armorCaptured = true, armorFound = false)
    }

    fun resetAlreadyOwned() {
        _uiState.update {
            it.copy(
                alreadyOwnedCreature = false,
                criaturaDetectada = null
            )
        }
    }

}
