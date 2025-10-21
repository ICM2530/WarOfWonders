package com.example.warofwonders.ui.screens.map

import android.icu.text.StringSearch
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

    fun updateSearchQuery(search: String) {
        _uiState.update { state ->
            state.copy(searchQuery = search)
        }
    }

    private fun updateLocation(data: LocationData) {
        _uiState.update { state ->
            state.copy(
                currentLocation = data
            )
        }
    }

    fun startLocationUpdates() {
        _uiState.update { state ->
            state.copy(locationUpdates = true)
        }
        locationRepository.startLocationUpdates { newData ->
            updateLocation(data = newData)
        }
    }

    fun stopLocationUpdates() {
        locationRepository.stopLocationUpdates()
        _uiState.update { state ->
            state.copy(locationUpdates = false)
        }
    }
}