package com.example.example.presentation

import com.example.example.domain.model.AuthorizationResponse

sealed interface AuthorizationStatus {
    data object Idle : AuthorizationStatus
    data object Loading : AuthorizationStatus
    data class Success(val response: AuthorizationResponse) : AuthorizationStatus
    data class Rejected(val response: AuthorizationResponse) : AuthorizationStatus
    data class NetworkError(val message: String) : AuthorizationStatus
}
