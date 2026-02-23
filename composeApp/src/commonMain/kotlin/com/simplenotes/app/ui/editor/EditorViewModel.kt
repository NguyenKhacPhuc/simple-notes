package com.simplenotes.app.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simplenotes.app.data.repository.NoteRepository
import com.simplenotes.app.domain.model.Note
import com.simplenotes.app.domain.model.SyncStatus
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class EditorState(
    val noteId: String? = null,
    val title: String = "",
    val body: String = "",
    val isSaving: Boolean = false,
    val isNew: Boolean = true,
)

sealed interface EditorIntent {
    data class Load(val id: String) : EditorIntent
    data object CreateNew : EditorIntent
    data class UpdateTitle(val title: String) : EditorIntent
    data class UpdateBody(val body: String) : EditorIntent
    data object Save : EditorIntent
}

class EditorViewModel(
    private val repository: NoteRepository,
    private val client: SupabaseClient,
) : ViewModel() {
    private val _state = MutableStateFlow(EditorState())
    val state = _state.asStateFlow()
    private var autoSaveJob: Job? = null

    fun send(intent: EditorIntent) {
        viewModelScope.launch { reduce(intent) }
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun reduce(intent: EditorIntent) {
        when (intent) {
            is EditorIntent.Load -> {
                val note = repository.getById(intent.id)
                if (note != null) {
                    _state.update {
                        it.copy(
                            noteId = note.id,
                            title = note.title,
                            body = note.body,
                            isNew = false,
                        )
                    }
                }
            }
            is EditorIntent.CreateNew -> {
                _state.update {
                    it.copy(
                        noteId = Uuid.random().toString(),
                        title = "",
                        body = "",
                        isNew = true,
                    )
                }
            }
            is EditorIntent.UpdateTitle -> {
                _state.update { it.copy(title = intent.title) }
                scheduleAutoSave()
            }
            is EditorIntent.UpdateBody -> {
                _state.update { it.copy(body = intent.body) }
                scheduleAutoSave()
            }
            is EditorIntent.Save -> saveNote()
        }
    }

    private fun scheduleAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(1000)
            saveNote()
        }
    }

    private fun saveNote() {
        val s = _state.value
        val noteId = s.noteId ?: return
        if (s.title.isBlank() && s.body.isBlank()) return

        val userId = client.auth.currentUserOrNull()?.id ?: return
        val now = Clock.System.now()

        val note = Note(
            id = noteId,
            userId = userId,
            title = s.title.ifBlank { s.body.take(50) },
            body = s.body,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncStatus.PENDING,
        )
        repository.save(note)
        _state.update { it.copy(isSaving = false, isNew = false) }
    }
}
