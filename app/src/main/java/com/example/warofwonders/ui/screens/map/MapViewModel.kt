package com.example.warofwonders.ui.screens.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.taller2.data.repository.GeoCoderRepository
import com.android.taller2.data.repository.RouteRepository
import com.example.warofwonders.R
import com.example.warofwonders.data.model.ClanData
import com.example.warofwonders.data.model.Friend
import com.example.warofwonders.data.model.InterestPointData
import com.example.warofwonders.data.model.LocationData
import com.example.warofwonders.data.model.MarkerData
import com.example.warofwonders.data.repository.LocationRepository
import com.example.warofwonders.data.source.hardware.LightSensorDataSource
import com.example.warofwonders.data.source.hardware.BarometerSensorDataSource
import com.example.warofwonders.data.source.hardware.TemperatureSensorDataSource
import com.example.warofwonders.data.source.hardware.MagnetometerDataSource
import com.example.warofwonders.data.source.local.JsonManagerDataSource
import com.example.warofwonders.data.source.remote.RestVolleyDataSource
import com.example.warofwonders.ui.model.Criatura
import com.example.warofwonders.ui.model.InventarioViewModel
import com.example.warofwonders.ui.model.Recurso
import com.example.warofwonders.ui.model.TipoCriatura
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.warofwonders.ui.screens.map.MapUser

class MapViewModel(
    private val locationRepository: LocationRepository,
    private val geoCoderRepository: GeoCoderRepository,
    private val routeRepository: RouteRepository,
    private val barometerSensorDataSource: BarometerSensorDataSource,
    private val temperatureSensorDataSource: TemperatureSensorDataSource,
    private val magnetometerDataSource: MagnetometerDataSource,
    private val lightSensorDataSource: LightSensorDataSource,
    private val restVolleyDataSource: RestVolleyDataSource,
    private val inventarioVM: InventarioViewModel,
    private val jsonManager: JsonManagerDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState

    private val auth = FirebaseAuth.getInstance()
    private val realtimeDB = FirebaseDatabase.getInstance().reference
    private val clanesDb = realtimeDB.child("clanes")
    private val recursosDb = realtimeDB.child("recursos")
    private var friendsListener: ValueEventListener? = null
    private var friendsRefForListener: DatabaseReference? = null
    private val friendChildListeners = mutableMapOf<String, ValueEventListener>()
    private var selectedFriendListener: ValueEventListener? = null
    private var clanMembersListeners = mutableMapOf<String, ValueEventListener>()

    private var criaturasDisponibles: List<Criatura> = emptyList()
    private var recursosDisponibles: List<Recurso> = emptyList()
    

    init {
        loadUserLastLocation()
        loadUserActiveState()
        observarClanesRealtime()
        loadCreaturesFromFirebaseRealtime()
        cargarRecursosRealtime()
        iniciarDetectorRecursos()
        iniciarDetectorPvP()
        loadSavedInterestPoints()

        viewModelScope.launch {
            inventarioVM.cargarInventario()
        }
    }

    fun clearAllSavedInterestPoints() {
        val delete = jsonManager.deleteJsonIfExists()
        if (delete) {
            _uiState.update { state ->
                state.copy(interestPoint = emptyList())
            }
        }
    }

    private fun loadUserLastLocation() {
        val currentUser = auth.currentUser ?: return
        val locationRef = realtimeDB.child("users/${currentUser.uid}/lastLocation")

        locationRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lat = snapshot.child("latitude").getValue(Double::class.java)
                val lng = snapshot.child("longitude").getValue(Double::class.java)
                val alt = snapshot.child("altitude").getValue(Double::class.java) ?: 0.0

                if (lat != null && lng != null) {
                    _uiState.update { state ->
                        state.copy(
                            currentLocation = LocationData(lat, lng, alt),
                            cameraTarget = LatLng(lat, lng)
                        )
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MapViewModel", "Error cargando lastLocation: ${error.message}")
            }
        })
    }

    fun saveSelectedInterestPoint(point: InterestPointData) {
        jsonManager.saveInterestPoint(point)
    }

    private fun loadSavedInterestPoints() {
        val savedPoints = jsonManager.readInterestPoints()
        _uiState.update { it.copy(interestPoint = savedPoints) }
    }

    fun visitPoi() {
        viewModelScope.launch {
            val db = FirebaseDatabase.getInstance().reference
            val userRef = db.child("users").child(auth.currentUser?.uid ?: return@launch)
            val snapshot = userRef.get().await()

            val currentExp = (snapshot.child("xp").value as? Number)?.toInt() ?: 0
            val currentCoins = (snapshot.child("coins").value as? Number)?.toInt() ?: 0

            val updates = mapOf(
                "xp" to (currentExp + 20),
                "coins" to (currentCoins + 50)
            )
            userRef.updateChildren(updates)
        }
    }

    fun seleccionarClan(clan: ClanData) {
        _uiState.update {
            it.copy(
                clanSeleccionado = clan,
            )
        }

        cargarMiembrosClanRealtime(clan)
    }

    fun cerrarClanInfo() {
        clanMembersListeners.forEach { (uid, listener) ->
            realtimeDB.child("users").child(uid).removeEventListener(listener)
        }
        clanMembersListeners.clear()

        _uiState.update {
            it.copy(
                clanSeleccionado = null,
                clanMiembrosList = emptyList()
            )
        }
    }

    fun cargarMiembrosClanRealtime(clan: ClanData) {
        clanMembersListeners.forEach { (uid, listener) ->
            realtimeDB.child("users").child(uid).removeEventListener(listener)
        }
        clanMembersListeners.clear()

        val miembrosIds = clan.miembros.keys.toList()
        if (miembrosIds.isEmpty()) {
            _uiState.update { it.copy(clanMiembrosList = emptyList()) }
            return
        }

        val listaMiembros = mutableListOf<Friend>()

        miembrosIds.forEach { uid ->
            val userRef = realtimeDB.child("users").child(uid)

            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.child("name").getValue(String::class.java) ?: ""
                    val lastname = snapshot.child("lastname").getValue(String::class.java) ?: ""
                    val email = snapshot.child("email").getValue(String::class.java) ?: ""
                    val profileImage = snapshot.child("profileImage").getValue(String::class.java)
                    val active = snapshot.child("active").getValue(Boolean::class.java) ?: false

                    val latitude = snapshot.child("lastLocation/latitude").getValue(Double::class.java)
                    val longitude = snapshot.child("lastLocation/longitude").getValue(Double::class.java)

                    val friend = Friend(
                        uid = uid,
                        name = name,
                        lastname = lastname,
                        email = email,
                        profileImage = profileImage,
                        active = active,
                        latitude = latitude,
                        longitude = longitude
                    )

                    val index = listaMiembros.indexOfFirst { it.uid == uid }
                    if (index >= 0) {
                        listaMiembros[index] = friend
                    } else {
                        listaMiembros.add(friend)
                    }

                    _uiState.update { it.copy(clanMiembrosList = listaMiembros.toList()) }
                }

                override fun onCancelled(error: DatabaseError) {}
            }

            userRef.addValueEventListener(listener)
            clanMembersListeners[uid] = listener
        }
    }

    private fun observarClanesRealtime() {
        clanesDb.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = snapshot.children.mapNotNull { child ->
                    try {
                        child.getValue(ClanData::class.java)
                    } catch (e: Exception) {
                        Log.w("MapViewModel", "Error deserializing clan from snapshot: ${e.message}")
                        null
                    }
                }

                _uiState.update { state ->
                    val clanActual = state.clanSeleccionado
                    val clanActualizado =
                        clanActual?.id?.let { id -> lista.find { it.id == id } }

                    state.copy(
                        clans = lista,
                        clanSeleccionado = clanActualizado,
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun loadInterestPoints() {
        if (_uiState.value.interestPoint.size < 20) {
            _uiState.update { it.copy(interestPoint = emptyList()) }
            viewModelScope.launch {
                restVolleyDataSource.loadInterestPoints { list ->
                    _uiState.update { it.copy(interestPoint = list) }
                }
            }
        } else {
            _uiState.update { it.copy(interestPoint = emptyList()) }
            loadSavedInterestPoints()
        }
    }

    fun setUserActiveState(active: Boolean) {
        val currentUser = auth.currentUser ?: return
        val userRef = realtimeDB.child("users/${currentUser.uid}")
        userRef.child("active").setValue(active)
            .addOnSuccessListener { _uiState.update { it.copy(isActive = active) } }
            .addOnFailureListener { e ->
                Log.e("MapViewModel", "Error actualizando estado activo: ${e.message}")
            }
    }

    private fun loadUserActiveState() {
        val currentUser = auth.currentUser ?: return
        val userActiveRef = realtimeDB.child("users/${currentUser.uid}/active")
        userActiveRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val active = snapshot.getValue(Boolean::class.java) ?: false
                _uiState.update { it.copy(isActive = active) }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MapViewModel", "Error cargando estado activo: ${error.message}")
            }
        })
    }

    fun observeFriendsRealtime() {
        val currentUser = auth.currentUser ?: return
        val userFriendsRef = realtimeDB.child("users/${currentUser.uid}/friends")

        friendsRefForListener = userFriendsRef

        friendsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val friendsList = mutableListOf<Friend>()

                friendChildListeners.forEach { (uid, listener) ->
                    realtimeDB.child("users").child(uid).removeEventListener(listener)
                }
                friendChildListeners.clear()

                snapshot.children.forEach { friendChild ->
                    val friendUid = friendChild.key ?: return@forEach
                    val isFriend = friendChild.getValue(Boolean::class.java) ?: false
                    if (!isFriend) return@forEach

                    val friendRef = realtimeDB.child("users").child(friendUid)
                    val childListener = object : ValueEventListener {
                        override fun onDataChange(friendSnapshot: DataSnapshot) {
                            val name = friendSnapshot.child("name").getValue(String::class.java) ?: ""
                            val lastname = friendSnapshot.child("lastname").getValue(String::class.java) ?: ""
                            val email = friendSnapshot.child("email").getValue(String::class.java) ?: ""
                            val profileImage = friendSnapshot.child("profileImage").getValue(String::class.java)
                            val active = friendSnapshot.child("active").getValue(Boolean::class.java) ?: false
                            val latitude = friendSnapshot.child("lastLocation").child("latitude").getValue(Double::class.java)
                            val longitude = friendSnapshot.child("lastLocation").child("longitude").getValue(Double::class.java)

                            val existingIndex = friendsList.indexOfFirst { it.uid == friendUid }
                            val friendData = Friend(
                                uid = friendUid,
                                name = name,
                                lastname = lastname,
                                email = email,
                                profileImage = profileImage,
                                active = active,
                                latitude = latitude,
                                longitude = longitude
                            )
                            if (existingIndex >= 0) {
                                friendsList[existingIndex] = friendData
                            } else {
                                friendsList.add(friendData)
                            }

                            _uiState.update { it.copy(friendsList = friendsList.toList()) }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    }

                    friendRef.addValueEventListener(childListener)
                    friendChildListeners[friendUid] = childListener
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        userFriendsRef.addValueEventListener(friendsListener!!)
    }

    fun stopFriendsListener() {
        friendsListener?.let { listener ->
            friendsRefForListener?.removeEventListener(listener)
            friendsListener = null
            friendsRefForListener = null
        }
        friendChildListeners.forEach { (uid, listener) ->
            realtimeDB.child("users").child(uid).removeEventListener(listener)
        }
        friendChildListeners.clear()
    }

    fun observeSelectedFriendRealtime(friendUid: String) {
        stopSelectedFriendListener()

        val friendRef = realtimeDB.child("users").child(friendUid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val uid = snapshot.key ?: return
                val name = snapshot.child("name").value as? String ?: ""
                val lastname = snapshot.child("lastname").value as? String ?: ""
                val email = snapshot.child("email").value as? String ?: ""
                val profileImage = snapshot.child("profileImage").value as? String
                val active = snapshot.child("active").value as? Boolean ?: false

                val latitude = snapshot.child("lastLocation").child("latitude").value as? Double
                val longitude = snapshot.child("lastLocation").child("longitude").value as? Double

                val friend = Friend(
                    uid = uid,
                    name = name,
                    lastname = lastname,
                    email = email,
                    profileImage = profileImage,
                    active = active,
                    latitude = latitude,
                    longitude = longitude
                )

                _uiState.update { current ->
                    current.copy(selectedFriendMarker = friend)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MapViewModel", "Error escuchando amigo: ${error.message}")
            }
        }

        friendRef.addValueEventListener(listener)
        selectedFriendListener = listener
    }

    fun stopSelectedFriendListener() {
        selectedFriendListener?.let { listener ->
            _uiState.value.selectedFriendMarker?.uid?.let { uid ->
                realtimeDB.child("users").child(uid).removeEventListener(listener)
            }
        }
        selectedFriendListener = null
        _uiState.update { it.copy(selectedFriendMarker = Friend()) }
    }

    // --- Cosas de Mapas ---
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
            val location = geoCoderRepository.getLocationFromAddress(query)
            location?.let { addTargetMarker(it) }
        }
    }

    fun addTargetMarker(location: LatLng) {
        clearMap()
        val address = geoCoderRepository.getAddressFromLocation(location) ?: "Ubicación desconocida"
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
        _uiState.update { it.copy(targetMarker = null, routePoints = emptyList()) }
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
        _uiState.update { it.copy(currentLocation = locData) }

        val currentUser = auth.currentUser ?: return
        val locationMap = mapOf(
            "latitude" to locData.latitude,
            "longitude" to locData.longitude,
            "altitude" to locData.altitude
        )
        realtimeDB.child("users/${currentUser.uid}/lastLocation").setValue(locationMap)
            .addOnFailureListener { e ->
                Log.e("MapViewModel", "Error updating lastLocation: ${e.message}")
            }
    }

    fun startLocationUpdates() {
        locationRepository.startLocationUpdates { updateLocation(it) }
    }

    fun stopLocationUpdates() {
        locationRepository.stopLocationUpdates()
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

    fun clearEncounter() {
        _uiState.update { it.copy(encounterAttackerId = null, encounterDefenderId = null) }
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











    // -------------------- DETECCIÓN DE ENCUENTROS PvP --------------------

    private fun iniciarDetectorPvP() {
        viewModelScope.launch {
            while (true) {
                delay(5 * 1000) // Verificar cada 5 segundos

                // Si ya hay un encuentro activo, no buscar otro
                if (_uiState.value.encounterAttackerId != null || _uiState.value.encounterDefenderId != null) {
                    delay(1000)
                    continue
                }

                val currentUser = auth.currentUser ?: continue
                val currentLocation = _uiState.value.currentLocation
                val PVP_RANGE = 0.00009 // ~10 metros en grados (aproximado) — reducido desde 0.00018

                try {
                    // Leer todas las ubicaciones de usuarios
                    val usersLocationsRef = realtimeDB.child("users")
                    val snapshot = usersLocationsRef.get().await()

                    var nearbyPlayerId: String? = null
                    var minDistance = Double.MAX_VALUE

                    snapshot.children.forEach { userSnapshot ->
                        val userId = userSnapshot.key ?: return@forEach
                        if (userId == currentUser.uid) return@forEach // Ignorar tu propio usuario

                        val lastLocation = userSnapshot.child("lastLocation")
                        val lat = lastLocation.child("latitude").getValue(Double::class.java)
                        val lng = lastLocation.child("longitude").getValue(Double::class.java)
                        val active = userSnapshot.child("active").getValue(Boolean::class.java) ?: false

                        if (lat == null || lng == null || !active) return@forEach

                        // Calcular distancia euclideana en grados
                        val dLat = lat - currentLocation.latitude
                        val dLng = lng - currentLocation.longitude
                        val distance = kotlin.math.sqrt(dLat * dLat + dLng * dLng)

                        // Mantener el jugador más cercano dentro del rango
                        if (distance <= PVP_RANGE && distance < minDistance) {
                            minDistance = distance
                            nearbyPlayerId = userId
                        }
                    }


                    // Actualizar lista de usuarios cercanos para UI (incluye simulados)
                    val nearbyList = mutableListOf<MapUser>()
                    // agregar desde snapshot (limit simple)
                    snapshot.children.forEach { s2 ->
                        val uid2 = s2.key ?: return@forEach
                        // IMPORTANTE: Excluir al usuario actual para no permitir auto-encuentros
                        if (uid2 == currentUser.uid) return@forEach
                        
                        val lastLoc2 = s2.child("lastLocation")
                        val lat2 = lastLoc2.child("latitude").getValue(Double::class.java)
                        val lng2 = lastLoc2.child("longitude").getValue(Double::class.java)
                        val active2 = s2.child("active").getValue(Boolean::class.java) ?: false
                        if (lat2 != null && lng2 != null && active2) {
                            val mu = MapUser(uid = uid2, displayName = s2.child("displayName").getValue(String::class.java) ?: "Player", clanId = s2.child("clan").getValue(String::class.java) ?: "", nivel = s2.child("nivel").getValue(Int::class.java) ?: 1, coins = s2.child("coins").getValue(Long::class.java) ?: 0L, lastLocation = LocationData(lat2, lng2), active = active2)
                            nearbyList.add(mu)
                        }
                    }


                    // Actualizar banderas de territorio enemigo
                    val clans = _uiState.value.clans
                    val insideEnemy = checkInsideEnemyTerritory(currentLocation, clans)
                    _uiState.update { it.copy(nearbyUsers = nearbyList, insideEnemyTerritory = insideEnemy) }

                    // Si encontramos un jugador cercano, solo lo reportamos en la lista nearbyUsers
                    if (nearbyPlayerId != null) {
                        Log.d("MapViewModel", "Jugador cercano detectado (no iniciar encuentro automáticamente). UID: $nearbyPlayerId a ${minDistance * 111000}m")
                        // No iniciar encuentro automáticamente — el usuario debe pulsar 'Atacar' en la UI.
                    }
                } catch (e: Exception) {
                    Log.e("MapViewModel", "Error en detector PvP: ${e.message}")
                }
            }
        }
    }

    // Comprueba si la ubicación actual está dentro del territorio de un clan diferente al del jugador
    private fun checkInsideEnemyTerritory(currentLocation: LocationData, clans: List<ClanData>): Boolean {
        val userClanId = inventarioVM.inventario.value.clanid
        if (userClanId.isBlank()) return false

        clans.forEach { clan ->
            val polygon = clan.zona.map { LatLng(it.latitude, it.longitude) }
            val inside = puntoDentro(LatLng(currentLocation.latitude, currentLocation.longitude), polygon)
            if (inside && !clan.id.equals(userClanId, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    

    // Acción de atacar al enemigo más cercano en la lista nearbyUsers
    fun attackNearestEnemy() {
        val list = _uiState.value.nearbyUsers
        if (list.isEmpty()) return
        val current = _uiState.value.currentLocation
        var nearest: MapUser? = null
        var minDist = Double.MAX_VALUE
        list.forEach { u ->
            val dLat = u.lastLocation.latitude - current.latitude
            val dLng = u.lastLocation.longitude - current.longitude
            val dist = kotlin.math.sqrt(dLat * dLat + dLng * dLng)
            if (dist < minDist) {
                minDist = dist
                nearest = u
            }
        }
        nearest?.let { attackUser(it) }
    }

    // Atacar un usuario (si es simulado, escribir temporalmente en Realtime DB para que CombatViewModel pueda leerlo)
    fun attackUser(target: MapUser) {
        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                // iniciar encuentro inmediato
                _uiState.update { it.copy(encounterAttackerId = currentUser.uid, encounterDefenderId = target.uid) }
            } catch (e: Exception) {
                Log.e("MapViewModel", "Error al preparar ataque: ${e.message}")
            }
        }
    }

}
