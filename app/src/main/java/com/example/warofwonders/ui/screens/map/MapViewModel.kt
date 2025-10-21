package com.example.warofwonders.ui.screens.map

import androidx.lifecycle.ViewModel
import com.example.warofwonders.data.model.LocationData
import com.example.warofwonders.data.repository.LocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class MapViewModel(
    private val locationRepository: LocationRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState : StateFlow<MapUiState> = _uiState

    fun updatePermissionStatus(granted: Boolean) {
        _uiState.update { state ->
            state.copy(permissionStatus = granted)
        }
    }

    fun updateLocation(data: LocationData) {
        _uiState.update { state ->
            state.copy(
                currentLocation = data,
            )
        }
    }

    fun updateSearchQuery(data: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = data,
            )
        }
    }

    fun toggleLocationUpdates() {
        val updating = _uiState.value.locationUpdates
        if (updating) stopLocationUpdates() else startLocationUpdates()
    }

    private fun startLocationUpdates() {
        _uiState.update { it.copy(locationUpdates = true) }
        locationRepository.startLocationUpdates { location ->
            updateLocation(location)
        }
    }

    private fun stopLocationUpdates() {
        locationRepository.stopLocationUpdates()
        _uiState.update { it.copy(locationUpdates = false) }
    }

}
