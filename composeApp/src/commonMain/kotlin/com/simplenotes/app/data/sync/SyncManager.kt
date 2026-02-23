package com.simplenotes.app.data.sync

import com.simplenotes.app.data.local.NoteDao
import com.simplenotes.app.data.local.SyncMetaDao
import com.simplenotes.app.data.local.SyncQueueDao
import com.simplenotes.app.data.remote.NoteDto
import com.simplenotes.app.data.remote.RemoteNoteApi
import com.simplenotes.app.domain.model.Note
import com.simplenotes.app.domain.model.SyncStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json

class SyncManager(
    private val noteDao: NoteDao,
    private val syncQueueDao: SyncQueueDao,
    private val syncMetaDao: SyncMetaDao,
    private val remoteApi: RemoteNoteApi,
) {
    suspend fun sync() {
        pushPendingChanges()
        pullRemoteChanges()
    }

    private suspend fun pushPendingChanges() {
        val pending = syncQueueDao.allPending()
        for (entry in pending) {
            try {
                val dto = Json.decodeFromString<NoteDto>(entry.payload)
                remoteApi.upsertNote(dto)
                syncQueueDao.delete(entry.id)
                noteDao.updateSyncStatus(entry.note_id, SyncStatus.SYNCED)
            } catch (_: Exception) {
                // Will retry on next sync
            }
        }
    }

    private suspend fun pullRemoteChanges() {
        val since = syncMetaDao.get("lastSyncTimestamp") ?: "1970-01-01T00:00:00Z"
        try {
            val remoteNotes = remoteApi.pullChanges(since)
            for (dto in remoteNotes) {
                val localNote = noteDao.getById(dto.id)
                val remoteInstant = Instant.parse(dto.updatedAt)
                // LWW: remote wins if newer
                if (localNote == null || remoteInstant > localNote.updatedAt) {
                    noteDao.upsert(dto.toDomain())
                }
            }
            val maxTimestamp = remoteNotes.maxOfOrNull { it.updatedAt } ?: since
            syncMetaDao.set("lastSyncTimestamp", maxTimestamp)
        } catch (_: Exception) {
            // Will retry on next sync
        }
    }
}

private fun NoteDto.toDomain() = Note(
    id = id,
    userId = userId,
    title = title,
    body = content,
    isDeleted = isDeleted,
    syncVersion = syncVersion,
    createdAt = Instant.parse(createdAt),
    updatedAt = Instant.parse(updatedAt),
    syncStatus = SyncStatus.SYNCED,
)
