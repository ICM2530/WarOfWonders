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
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
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
import com.example.warofwonders.data.service.CombatService
import com.example.warofwonders.data.model.Combatant
import com.google.firebase.Firebase
import com.google.firebase.database.*

class MapViewModel(
    private val locationRepository: LocationRepository,
    private val geoRepository: GeoRepository,
    private val barometerSensorDataSource: BarometerSensorDataSource,
    private val temperatureSensorDataSource: TemperatureSensorDataSource,
    private val magnetometerDataSource: MagnetometerDataSource,
    private val lightSensorDataSource: LightSensorDataSource,
    private val interestPointRepository: InterestPointRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    //IMPORTANTE: Lo siguiente son variables provicionales para la gestion de combates y territorios, si es necesario, cambiar despues
    // Referencias a Realtime DB usadas para ubicacion de jugadores, usuarios y almacenar territorios de clanes
    private val realtime = Firebase.database
    private val userLocationsRef: DatabaseReference = realtime.getReference("user_locations")
    private val usersRef: DatabaseReference = realtime.getReference("users")
    private val clansTerritoryRef: DatabaseReference = realtime.getReference("clans_territory")

    private val combatService = CombatService()
    //IMPORTANTE: Aca terminan las variables provisionales

    private var criaturasDisponibles: List<Criatura> = emptyList()

    init {
        loadInterestPoints()
        loadCreaturesFromFirebase()
    }

    //cargar las criaturas desde la base de datos
    private fun loadCreaturesFromFirebase() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val snapshot = firestore.collection("criaturas_disponibles").get().await()
                criaturasDisponibles = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Criatura::class.java)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    //cargar las criaturas segun el tipo

    private fun capturarCriaturaPorTipo(tipo: String) {
        val lista = criaturasDisponibles.filter { it.tipo.equals(tipo, ignoreCase = true) }
        if (lista.isEmpty()) return

        val seleccionada = lista.random()
        guardarCriaturaEnInventario(seleccionada)
    }

    private fun guardarCriaturaEnInventario(criatura: Criatura) {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                firestore.collection("usuarios")
                    .document(userId)
                    .collection("criaturas")
                    .document(criatura.id.ifEmpty { System.currentTimeMillis().toString() })
                    .set(criatura)
                    .await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun captureColdCreature() {
        _uiState.value = _uiState.value.copy(coldCreatureCaptured = true, coldCreatureFound = false)
        capturarCriaturaPorTipo("FRIO")
    }

    fun captureHotCreature() {
        _uiState.value = _uiState.value.copy(hotCreatureCaptured = true, hotCreatureFound = false)
        capturarCriaturaPorTipo("CALOR")
    }

    fun capturePressureCreature() {
        _uiState.value = _uiState.value.copy(pressureCreatureCaptured = true, pressureCreatureFound = false)
        capturarCriaturaPorTipo("PRESION")
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
            //IMPORTANTE: Lo siguiente son funciones provisionales para la gestion de combates y territorios, si es necesario, cambiar despues
            // publicar mi ubicacion a la realtime DB
            val uid = auth.currentUser?.uid
            uid?.let { id ->
                try {
                    val locMap = mapOf(
                        "lat" to location.latitude,
                        "lng" to location.longitude,
                        "ts" to System.currentTimeMillis(),
                        "uid" to id
                    )
                    userLocationsRef.child(id).setValue(locMap)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // revisar si hay jugadores y territorios cercanos
            uid?.let { id ->
                checkNearbyPlayersAndTerritories(id, location.latitude, location.longitude)
            }
        }
    }

    private fun checkNearbyPlayersAndTerritories(currentUid: String, lat: Double, lng: Double) {
        // Revisar las ubicaciones de otros jugadores una vez
        userLocationsRef.get().addOnSuccessListener { snapshot ->
            for (child in snapshot.children) {
                val otherUid = child.key ?: continue
                if (otherUid == currentUid) continue
                val oLat = child.child("lat").getValue(Double::class.java) ?: continue
                val oLng = child.child("lng").getValue(Double::class.java) ?: continue
                val dist = distanceMeters(lat, lng, oLat, oLng)
                if (dist <= 20.0) {
                    // iniciar el combate: marcar encuentro en el estado para que la UI navegue a Combat
                    val already = _uiState.value.encounterAttackerId != null || _uiState.value.encounterDefenderId != null
                    if (!already) {
                        _uiState.update { it.copy(encounterAttackerId = currentUid, encounterDefenderId = otherUid) }
                    }
                    break
                }
            }
        }

        // Revisar puntos de interes para los territorios de clanes
        _uiState.value.staticMarkers.forEach { punto ->
            val distToPoint = distanceMeters(lat, lng, punto.lat, punto.lng)
            if (distToPoint <= 50.0 && punto.clanesPeleando.isNotEmpty()) {
                // clan defesnor (el primero en la lista)
                val defenderClan = punto.clanesPeleando.first()
                // iniciar combate contra un clan defensor (animal)
                initiateTerritoryIncursion(currentUid, punto, defenderClan)
            }
        }
    }

    private fun initiatePvPCombat(attackerUid: String, defenderUid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // obtener info basica del usuario (level, monedas, clan) del realtime DB
                val attackerSnap = usersRef.child(attackerUid).get().await()
                val defenderSnap = usersRef.child(defenderUid).get().await()

                val atkLevel = (attackerSnap.child("nivel").getValue(Int::class.java) ?: 1)
                val defLevel = (defenderSnap.child("nivel").getValue(Int::class.java) ?: 1)
                val atkName = attackerSnap.child("name").getValue(String::class.java) ?: "Player"
                val defName = defenderSnap.child("name").getValue(String::class.java) ?: "Player"
                val atkClan = attackerSnap.child("clan").getValue(String::class.java)
                val defClan = defenderSnap.child("clan").getValue(String::class.java)
                val atkResources = (attackerSnap.child("monedas").getValue(Int::class.java) ?: 0)
                val defResources = (defenderSnap.child("monedas").getValue(Int::class.java) ?: 0)

                val attacker = Combatant(
                    id = attackerUid,
                    name = atkName,
                    clan = atkClan,
                    level = atkLevel,
                    attack = 5 + atkLevel * 3,
                    maxHealth = 100 + atkLevel * 10,
                    resources = atkResources
                )

                val defender = Combatant(
                    id = defenderUid,
                    name = defName,
                    clan = defClan,
                    level = defLevel,
                    attack = 5 + defLevel * 3,
                    maxHealth = 100 + defLevel * 10,
                    resources = defResources
                )

                val result = combatService.fight(attacker, defender)

                // Aplicar transferencia de recursos: decrementar al perdedor, incrementar al ganador
                applyResourceTransfer(result)

                // actualizar uiState brevemente
                _uiState.update { it.copy() }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun initiateTerritoryIncursion(attackerUid: String, punto: com.example.warofwonders.data.model.PuntoInteres, defenderClan: com.example.warofwonders.data.model.Clan) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // info del atacante
                val attackerSnap = usersRef.child(attackerUid).get().await()
                val atkLevel = (attackerSnap.child("nivel").getValue(Int::class.java) ?: 1)
                val atkName = attackerSnap.child("name").getValue(String::class.java) ?: "Player"
                val atkClan = attackerSnap.child("clan").getValue(String::class.java)
                val atkResources = (attackerSnap.child("monedas").getValue(Int::class.java) ?: 0)

                val attacker = Combatant(
                    id = attackerUid,
                    name = atkName,
                    clan = atkClan,
                    level = atkLevel,
                    attack = 5 + atkLevel * 3,
                    maxHealth = 100 + atkLevel * 10,
                    resources = atkResources
                )

                // el animal defensor esta basado en el poder del clan
                val defPower = defenderClan.poder
                val defender = Combatant(
                    id = "clan_defender_${defenderClan.nombre}_${punto.id}",
                    name = "Defender of ${defenderClan.nombre}",
                    clan = defenderClan.nombre,
                    level = (defPower / 2).coerceAtLeast(1),
                    attack = 5 + defPower * 2,
                    maxHealth = 120 + defPower * 15,
                    resources = 0
                )

                val result = combatService.fight(attacker, defender)

                if (result.winnerId == attacker.id) {
                    // el atacante gana: el territorio del clan se expande y por consiguiente se le reduce al defensor
                    val expandMeters = 50L
                    val loserClanName = defenderClan.nombre
                    val winnerClanName = attacker.clan ?: ""
                    if (winnerClanName.isNotBlank()) {
                        adjustClanTerritory(winnerClanName, expandMeters)
                        adjustClanTerritory(loserClanName, -expandMeters)
                    }
                }

                // la transferencia de recursos no aplica para defensores NPC
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun adjustClanTerritory(clanName: String, deltaMeters: Long) {
        // revisar el territorio existente y ajustar el radio
        clansTerritoryRef.child(clanName).get().addOnSuccessListener { snap ->
            val centerLat = snap.child("centerLat").getValue(Double::class.java) ?: 0.0
            val centerLng = snap.child("centerLng").getValue(Double::class.java) ?: 0.0
            val radius = (snap.child("radiusMeters").getValue(Long::class.java) ?: 200L)
            val newRadius = (radius + deltaMeters).coerceAtLeast(50L)
            val map = mapOf(
                "centerLat" to centerLat,
                "centerLng" to centerLng,
                "radiusMeters" to newRadius
            )
            clansTerritoryRef.child(clanName).setValue(map)
        }.addOnFailureListener {
            // crear un territorio por defecto si no existia
            val default = mapOf("centerLat" to 0.0, "centerLng" to 0.0, "radiusMeters" to 200L)
            clansTerritoryRef.child(clanName).setValue(default)
        }
    }

    private fun applyResourceTransfer(result: com.example.warofwonders.data.model.CombatResult) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val winnerRef = usersRef.child(result.winnerId)
                val loserRef = usersRef.child(result.loserId)

                // leer cantidades actuales
                val winnerSnap = winnerRef.get().await()
                val loserSnap = loserRef.get().await()
                val winnerCoins = (winnerSnap.child("monedas").getValue(Int::class.java) ?: 0)
                val loserCoins = (loserSnap.child("monedas").getValue(Int::class.java) ?: 0)

                val transfer = result.resourcesTransferred.coerceAtMost(loserCoins)
                winnerRef.child("monedas").setValue(winnerCoins + transfer)
                loserRef.child("monedas").setValue(loserCoins - transfer)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371000.0 // metros
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
        //IMPORTANTE: Aca terminan las funciones provisionales
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
            if (isCold != _uiState.value.isCold) {
                _uiState.update { it.copy(isCold = isCold) }
            }
            else if (isHot != _uiState.value.isHot) {
                _uiState.update { it.copy(isHot = isHot) }
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

    fun clearEncounter() {
        _uiState.update { it.copy(encounterAttackerId = null, encounterDefenderId = null) }
    }


    fun showPressureCreatureAlert(show: Boolean) {
        _uiState.value = _uiState.value.copy(pressureCreatureFound = show)
    }


    fun showColdCreatureAlert(show: Boolean) {
        _uiState.value = _uiState.value.copy(coldCreatureFound = show)
    }



    fun showHotCreatureAlert(show: Boolean) {
        _uiState.value = _uiState.value.copy(hotCreatureFound = show)
    }



    fun findArmor(show: Boolean) {
        _uiState.value = _uiState.value.copy(armorFound = show)
    }

    fun captureArmor() {
        _uiState.value = _uiState.value.copy(armorCaptured = true, armorFound = false)
    }
}
