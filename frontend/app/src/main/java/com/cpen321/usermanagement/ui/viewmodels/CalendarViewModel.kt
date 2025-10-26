package com.cpen321.usermanagement.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cpen321.usermanagement.data.remote.api.RetrofitClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CalendarUiState(
    val isConnected: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class CalendarViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private val calendarInterface = RetrofitClient.calendarInterface

    init {
        loadCalendarStatus()
    }

    fun loadCalendarStatus() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val response = calendarInterface.getCalendarStatus()
                if (response.isSuccessful) {
                    val status = response.body()
                    _uiState.value = _uiState.value.copy(
                        isConnected = status?.connected == true,
                        isLoading = false
                    )
                    Log.d("CalendarViewModel", "Calendar status loaded: connected=${status?.connected}")
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to load calendar status"
                    )
                    Log.e("CalendarViewModel", "Failed to load status: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error loading calendar status: ${e.message}"
                )
                Log.e("CalendarViewModel", "Error loading calendar status", e)
            }
        }
    }

    fun getAuthorizationUrl(onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val response = calendarInterface.getAuthorizationUrl()
                if (response.isSuccessful) {
                    val authUrl = response.body()?.authUrl
                    if (authUrl != null) {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        onSuccess(authUrl)
                        Log.d("CalendarViewModel", "Authorization URL obtained")
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Failed to get authorization URL"
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to get authorization URL"
                    )
                    Log.e("CalendarViewModel", "Failed to get auth URL: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error getting authorization URL: ${e.message}"
                )
                Log.e("CalendarViewModel", "Error getting authorization URL", e)
            }
        }
    }

    fun handleAuthorizationSuccess() {
        _uiState.value = _uiState.value.copy(
            successMessage = "Google Calendar connected successfully!",
            isConnected = true
        )
        // Reload status to confirm
        loadCalendarStatus()
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
