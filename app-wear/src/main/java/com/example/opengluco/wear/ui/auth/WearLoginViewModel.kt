package com.example.opengluco.wear.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.opengluco.core.data.OpenGlucoRepository
import com.example.opengluco.core.data.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface WearLoginUiState {
    object Idle : WearLoginUiState
    object Loading : WearLoginUiState
    object Success : WearLoginUiState
    data class Error(val message: String) : WearLoginUiState
}

class WearLoginViewModel(
    private val repository: OpenGlucoRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WearLoginUiState>(WearLoginUiState.Idle)
    val uiState: StateFlow<WearLoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = WearLoginUiState.Error("Introduce email y contraseña")
            return
        }

        viewModelScope.launch {
            _uiState.value = WearLoginUiState.Loading
            val result = repository.login(email.trim(), password.trim())

            result.fold(
                onSuccess = { loginData ->
                    val token = loginData.authTicket?.token.orEmpty()
                    val userId = loginData.user?.id.orEmpty()

                    // Guardar sesión para auto-login
                    preferencesRepository.saveAuthSession(email.trim(), token, userId)

                    _uiState.value = WearLoginUiState.Success
                },
                onFailure = { error ->
                    _uiState.value = WearLoginUiState.Error(error.message ?: "Credenciales incorrectas")
                }
            )
        }
    }
}
