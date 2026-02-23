package com.simplenotes.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsState(
    val email: String? = null,
    val signedOut: Boolean = false,
)

sealed interface SettingsIntent {
    data object SignOut : SettingsIntent
}

class SettingsViewModel(private val client: SupabaseClient) : ViewModel() {
    private val _state = MutableStateFlow(SettingsState())
    val state = _state.asStateFlow()

    init {
        _state.update { it.copy(email = client.auth.currentUserOrNull()?.email) }
    }

    fun send(intent: SettingsIntent) {
        viewModelScope.launch { reduce(intent) }
    }

    private suspend fun reduce(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.SignOut -> {
                try {
                    client.auth.signOut()
                    _state.update { it.copy(signedOut = true) }
                } catch (_: Exception) { }
            }
        }
    }
}
