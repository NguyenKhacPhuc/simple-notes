package com.simplenotes.app.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simplenotes.app.data.repository.NoteRepository
import com.simplenotes.app.data.sync.SyncManager
import com.simplenotes.app.domain.model.Note
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotesState(
    val notes: List<Note> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface NotesIntent {
    data class Search(val query: String) : NotesIntent
    data class Delete(val id: String) : NotesIntent
    data object Refresh : NotesIntent
}

class NotesViewModel(
    private val repository: NoteRepository,
    private val syncManager: SyncManager,
) : ViewModel() {
    private val _state = MutableStateFlow(NotesState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllNotes().collect { notes ->
                _state.update { it.copy(notes = notes, isLoading = false) }
            }
        }
    }

    fun send(intent: NotesIntent) {
        viewModelScope.launch { reduce(intent) }
    }

    private suspend fun reduce(intent: NotesIntent) {
        when (intent) {
            is NotesIntent.Search -> {
                _state.update { it.copy(query = intent.query) }
                if (intent.query.isBlank()) {
                    // Flow collection handles showing all notes
                    return
                }
                val results = repository.search(intent.query)
                _state.update { it.copy(notes = results) }
            }
            is NotesIntent.Delete -> {
                repository.delete(intent.id)
            }
            is NotesIntent.Refresh -> {
                _state.update { it.copy(isLoading = true) }
                try {
                    syncManager.sync()
                } catch (_: Exception) { }
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}
