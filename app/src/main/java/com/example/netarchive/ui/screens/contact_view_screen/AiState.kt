package com.example.netarchive.ui.screens.contact_view_screen

sealed class AiState {
    object Idle : AiState()
    object Loading : AiState()
    data class Success(val suggestions: List<String>,val copiedIndex: Int? = null) : AiState()
    data class Error(val message: String) : AiState()
    object Disabled : AiState()
}