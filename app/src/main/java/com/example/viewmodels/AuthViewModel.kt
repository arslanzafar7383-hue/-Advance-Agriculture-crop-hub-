package com.example.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.models.LoginRequest
import com.example.models.RegisterRequest
import com.example.models.User
import com.example.repositories.CropHubRepository
import com.example.utils.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val isLoggedIn: Boolean = false,
    val currentBaseUrl: String = "",
    val serverConnectionStatus: String = "Checking connection...",
    val isServerConnected: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AuthViewModel(
    private val repository: CropHubRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(
            user = preferencesManager.currentUser,
            isLoggedIn = preferencesManager.isLoggedIn,
            currentBaseUrl = preferencesManager.baseUrl,
            isServerConnected = preferencesManager.isServerConnected
        )
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        testServerConnection()
    }

    fun testServerConnection(customUrl: String? = null) {
        viewModelScope.launch {
            if (!customUrl.isNullOrBlank()) {
                preferencesManager.baseUrl = customUrl
            }
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                currentBaseUrl = preferencesManager.baseUrl,
                serverConnectionStatus = "Pinging MySQL Backend..."
            )
            val result = repository.testConnection()
            result.fold(
                onSuccess = { msg ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        serverConnectionStatus = "Connected (MySQL Live)",
                        isServerConnected = true,
                        successMessage = "Connected to XAMPP MySQL backend!"
                    )
                },
                onFailure = { err ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        serverConnectionStatus = "Standby (Local Cache Mode)",
                        isServerConnected = false,
                        errorMessage = "Server offline: Using verified on-device database cache"
                    )
                }
            )
        }
    }

    fun login(identifier: String, pass: String) {
        if (identifier.isBlank() || pass.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter email and password")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.login(LoginRequest(identifier, pass))
            result.fold(
                onSuccess = { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        user = user,
                        isLoggedIn = true,
                        successMessage = "Welcome, ${user.fullName}!"
                    )
                },
                onFailure = { err ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = err.message ?: "Authentication failed"
                    )
                }
            )
        }
    }

    fun register(fullName: String, email: String, phone: String, pass: String, userType: String) {
        if (fullName.isBlank() || email.isBlank() || phone.isBlank() || pass.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please fill in all registration fields")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.register(RegisterRequest(fullName, email, phone, pass, userType))
            result.fold(
                onSuccess = { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        user = user,
                        isLoggedIn = true,
                        successMessage = "Account registered as $userType!"
                    )
                },
                onFailure = { err ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = err.message ?: "Registration failed"
                    )
                }
            )
        }
    }

    fun logout() {
        preferencesManager.logout()
        _uiState.value = _uiState.value.copy(
            user = null,
            isLoggedIn = false,
            successMessage = "Logged out successfully"
        )
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
