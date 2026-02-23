package com.simplenotes.app.data.repository

import com.simplenotes.app.data.local.NoteDao
import com.simplenotes.app.data.local.SyncQueueDao
import com.simplenotes.app.data.remote.NoteDto
import com.simplenotes.app.domain.model.Note
import com.simplenotes.app.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class NoteRepository(
    private val noteDao: NoteDao,
    private val syncQueueDao: SyncQueueDao,
) {
    fun getAllNotes(): Flow<List<Note>> = noteDao.getAllNotes()

    fun getById(id: String): Note? = noteDao.getById(id)

    fun save(note: Note) {
        val updated = note.copy(
            updatedAt = Clock.System.now(),
            syncStatus = SyncStatus.PENDING
        )
        noteDao.upsert(updated)
        enqueueSync(updated, if (noteDao.getById(note.id) != null) "UPDATE" else "CREATE")
    }

    fun delete(id: String) {
        val now = Clock.System.now().toString()
        noteDao.markDeleted(id, now)
        val note = noteDao.getById(id) ?: return
        enqueueSync(note, "DELETE")
    }

    fun search(query: String): List<Note> {
        if (query.isBlank()) return emptyList()
        return noteDao.search(query)
    }

    private fun enqueueSync(note: Note, action: String) {
        val dto = NoteDto(
            id = note.id,
            userId = note.userId,
            title = note.title,
            content = note.body,
            isDeleted = note.isDeleted,
            syncVersion = note.syncVersion,
            createdAt = note.createdAt.toString(),
            updatedAt = note.updatedAt.toString(),
        )
        syncQueueDao.enqueue(
            noteId = note.id,
            action = action,
            payload = Json.encodeToString(dto),
            queuedAt = Clock.System.now().toString(),
        )
    }
}
