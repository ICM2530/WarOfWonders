package com.example.warofwonders.ui.screens.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.taller2.data.repository.GeoCoderRepository
import com.android.taller2.data.repository.RouteRepository
import com.example.warofwonders.R
import com.example.warofwonders.data.model.ClanData
import com.example.warofwonders.data.model.LocationData
import com.example.warofwonders.data.model.MarkerData
import com.example.warofwonders.data.repository.InterestPointRepository
import com.example.warofwonders.data.repository.LocationRepository
import com.example.warofwonders.data.source.hardware.LightSensorDataSource
import com.example.warofwonders.data.source.hardware.BarometerSensorDataSource
import com.example.warofwonders.data.source.hardware.TemperatureSensorDataSource
import com.example.warofwonders.data.source.hardware.MagnetometerDataSource
import com.example.warofwonders.ui.model.Criatura
import com.example.warofwonders.ui.model.InventarioViewModel
import com.example.warofwonders.ui.model.Recurso
import com.example.warofwonders.ui.model.TipoCriatura
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MapViewModel(
    private val locationRepository: LocationRepository,
    private val geoCoderRepository: GeoCoderRepository,
    private val routeRepository: RouteRepository,
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
    private val clanesDb = FirebaseDatabase.getInstance().getReference("clanes")

    private val realtimeDB = FirebaseDatabase.getInstance().reference
    private var criaturasDisponibles: List<Criatura> = emptyList()

    private val recursosDb = FirebaseDatabase.getInstance().getReference("recursos")
    private var recursosDisponibles: List<Recurso> = emptyList()


    init {
        _uiState.update {
            it.copy(
                currentLocation = LocationData(4.634243207620236, -74.06992472665623),
            )
        }

        loadInterestPoints()
        loadCreaturesFromFirebaseRealtime()

        observarClanesRealtime()

        viewModelScope.launch {
            inventarioVM.cargarInventario()
        }

        cargarRecursosRealtime()
        iniciarDetectorRecursos()
    }

    // Nuevo de Clanes
    private fun observarClanesRealtime() {
        clanesDb.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = snapshot.children.mapNotNull { it.getValue(ClanData::class.java) }

                _uiState.update { it.copy(clans = lista) }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    // Cosas de Mapas

    fun toggleLocationUpdates() {
        val updating = !_uiState.value.isUpdatingLocation
        if (updating) startLocationUpdates() else stopLocationUpdates()

        _uiState.update { state ->
            state.copy(
                isUpdatingLocation = updating,
                cameraTarget = if (updating)
                    LatLng(state.currentLocation.latitude, state.currentLocation.longitude)
                else null,
                cameraZoom = 18f
            )
        }
    }

    fun updatePlaceQuery(newQuery: String) {
        _uiState.update { it.copy(placeQuery = newQuery) }
    }

    fun searchPlaceQuery(query: String) {
        viewModelScope.launch {
            val location = geoCoderRepository.getLocationFromAddress(address = query)
            location?.let { addTargetMarker(it) }
        }
    }

    fun addTargetMarker(location: LatLng) {
        clearMap()
        val address =
            geoCoderRepository.getAddressFromLocation(location) ?: "Ubicación desconocida"

        val newMarker = MarkerData(
            position = location,
            title = address,
            snippet = "%.5f, %.5f".format(location.latitude, location.longitude),
            iconResId = null
        )

        _uiState.update {
            it.copy(
                targetMarker = newMarker,
                cameraTarget = location,
                cameraZoom = 16f
            )
        }

        val current = _uiState.value.currentLocation
        loadRouteFromPoints(listOf(LatLng(current.latitude, current.longitude), location))
    }

    fun clearMap() {
        _uiState.update {
            it.copy(
                targetMarker = null,
                routePoints = emptyList()
            )
        }
    }

    fun loadRouteFromPoints(points: List<LatLng>) {
        viewModelScope.launch {
            val routePoints = routeRepository.fetchRouteGeoJson(points)
            _uiState.update { it.copy(routePoints = routePoints) }
        }
    }

    fun updatePermissionStatus(granted: Boolean) {
        _uiState.update { it.copy(permissionStatus = granted) }
    }

    private fun updateLocation(locData: LocationData) {
        _uiState.update { state ->
            state.copy(
                currentLocation = locData,
            )
        }
    }

    fun startLocationUpdates() {
        locationRepository.startLocationUpdates { locationData ->
            updateLocation(locationData)
        }
    }

    fun stopLocationUpdates() {
        locationRepository.stopLocationUpdates()
    }

    private fun loadInterestPoints() {
        viewModelScope.launch(Dispatchers.IO) {
            val puntos = interestPointRepository.readJSONFile()
            _uiState.update { it.copy(staticMarkers = puntos) }
        }
    }

    // Otros

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
            val styleRes = if (lux < 100) R.raw.map_dark else R.raw.map_light
            _uiState.update { it.copy(mapStyleRes = styleRes) }
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



    // -------------------- RECURSOS --------------------

    private fun cargarRecursosRealtime() {
        recursosDb.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = snapshot.children.mapNotNull { it.getValue(Recurso::class.java) }
                recursosDisponibles = lista
            }

            override fun onCancelled(error: DatabaseError) { }
        })
    }

    fun puntoDentro(location: LatLng, polygon: List<LatLng>): Boolean {
        return com.google.maps.android.PolyUtil.containsLocation(location, polygon, true)
    }


    private fun iniciarDetectorRecursos() {
        viewModelScope.launch {
            while (true) {

                delay(30 * 1000) // 30 segundos



                val location = _uiState.value.currentLocation
                val clans = _uiState.value.clans

                val uid = auth.currentUser?.uid ?: continue

                val userClanId = inventarioVM.inventario.value.clanid
                if (userClanId.isBlank()) continue

                val clanJugador = clans.find { it.id.equals(userClanId, ignoreCase = true) } ?: continue

                val poligono = clanJugador.zona.map { LatLng(it.latitude, it.longitude) }

                val dentro = puntoDentro(LatLng(location.latitude, location.longitude), poligono)
                if (!dentro) continue

                lanzarRecursoAleatorio()
            }
        }
    }


    private fun lanzarRecursoAleatorio() {
        if (recursosDisponibles.isEmpty()) return

        val inventarioActual = inventarioVM.inventario.value.recursos

        // filtra recursos que no sean armadura ya existente ni criaturas
        val recursosFiltrados = recursosDisponibles.filter { recurso ->
            (recurso.tipo != "armadura" || inventarioActual.none { it.nombre == recurso.nombre }) &&
                    recurso.tipo != "criatura"
        }

        if (recursosFiltrados.isEmpty()) return

        val recurso = recursosFiltrados.random()

        _uiState.update {
            it.copy(
                recursoEncontrado = recurso,
                mostrarPopupRecurso = true
            )
        }
    }



    fun capturarRecursoDesdeUI() {
        val recurso = _uiState.value.recursoEncontrado ?: return

        viewModelScope.launch {
            inventarioVM.agregarRecurso(recurso)

            _uiState.update {
                it.copy(
                    mostrarPopupRecurso = false,
                    recursoEncontrado = null
                )
            }
        }
    }


    fun rechazarRecurso() {
        _uiState.update {
            it.copy(
                mostrarPopupRecurso = false,
                recursoEncontrado = null
            )
        }
    }






}
