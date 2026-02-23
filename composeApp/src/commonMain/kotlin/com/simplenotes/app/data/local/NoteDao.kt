package com.simplenotes.app.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.simplenotes.app.db.NoteEntity
import com.simplenotes.app.db.SimpleNotesDb
import com.simplenotes.app.domain.model.Note
import com.simplenotes.app.domain.model.SyncStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

class NoteDao(private val db: SimpleNotesDb) {

    fun getAllNotes(): Flow<List<Note>> =
        db.noteQueries.getAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities -> entities.map { it.toDomain() } }

    fun getById(id: String): Note? =
        db.noteQueries.getById(id).executeAsOneOrNull()?.toDomain()

    fun upsert(note: Note) {
        db.noteQueries.upsert(
            id = note.id,
            user_id = note.userId,
            title = note.title,
            body = note.body,
            is_deleted = if (note.isDeleted) 1L else 0L,
            sync_version = note.syncVersion,
            created_at = note.createdAt.toString(),
            updated_at = note.updatedAt.toString(),
            sync_status = note.syncStatus.name,
        )
    }

    fun markDeleted(id: String, updatedAt: String) {
        db.noteQueries.markDeleted(updatedAt, id)
    }

    fun updateSyncStatus(id: String, status: SyncStatus) {
        db.noteQueries.updateSyncStatus(status.name, id)
    }

    fun search(query: String): List<Note> =
        db.noteQueries.searchNotes(query).executeAsList().map { it.toDomain() }

    fun deleteAll() = db.noteQueries.deleteAll()
}

private fun NoteEntity.toDomain() = Note(
    id = id,
    userId = user_id,
    title = title,
    body = body,
    isDeleted = is_deleted != 0L,
    syncVersion = sync_version,
    createdAt = Instant.parse(created_at),
    updatedAt = Instant.parse(updated_at),
    syncStatus = SyncStatus.valueOf(sync_status),
)
