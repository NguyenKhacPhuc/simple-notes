package com.simplenotes.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthState(
    val user: String? = null, // user email if logged in
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface AuthIntent {
    data class SignIn(val email: String, val password: String) : AuthIntent
    data class SignUp(val email: String, val password: String) : AuthIntent
    data object SignOut : AuthIntent
    data object CheckSession : AuthIntent
}

class AuthViewModel(private val client: SupabaseClient) : ViewModel() {
    private val _state = MutableStateFlow(AuthState())
    val state = _state.asStateFlow()

    init {
        send(AuthIntent.CheckSession)
    }

    fun send(intent: AuthIntent) {
        viewModelScope.launch { reduce(intent) }
    }

    private suspend fun reduce(intent: AuthIntent) {
        when (intent) {
            is AuthIntent.CheckSession -> {
                try {
                    val session = client.auth.currentSessionOrNull()
                    _state.update { it.copy(user = session?.user?.email) }
                } catch (_: Exception) { }
            }
            is AuthIntent.SignIn -> {
                _state.update { it.copy(isLoading = true, error = null) }
                try {
                    client.auth.signInWith(Email) {
                        email = intent.email
                        password = intent.password
                    }
                    val email = client.auth.currentUserOrNull()?.email
                    _state.update { it.copy(user = email, isLoading = false) }
                } catch (e: Exception) {
                    _state.update { it.copy(isLoading = false, error = e.message ?: "Sign in failed") }
                }
            }
            is AuthIntent.SignUp -> {
                _state.update { it.copy(isLoading = true, error = null) }
                try {
                    client.auth.signUpWith(Email) {
                        email = intent.email
                        password = intent.password
                    }
                    val email = client.auth.currentUserOrNull()?.email
                    _state.update { it.copy(user = email, isLoading = false) }
                } catch (e: Exception) {
                    _state.update { it.copy(isLoading = false, error = e.message ?: "Sign up failed") }
                }
            }
            is AuthIntent.SignOut -> {
                try {
                    client.auth.signOut()
                    _state.update { AuthState() }
                } catch (_: Exception) { }
            }
        }
    }
}
