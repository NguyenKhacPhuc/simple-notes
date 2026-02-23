package com.simplenotes.app.data.local

import com.simplenotes.app.db.SimpleNotesDb
import com.simplenotes.app.db.SyncQueueEntry

class SyncQueueDao(private val db: SimpleNotesDb) {

    fun allPending(): List<SyncQueueEntry> =
        db.syncQueueQueries.allPending().executeAsList()

    fun enqueue(noteId: String, action: String, payload: String, queuedAt: String) {
        db.syncQueueQueries.insert(noteId, action, payload, queuedAt)
    }

    fun delete(id: Long) {
        db.syncQueueQueries.deleteEntry(id)
    }

    fun deleteAll() = db.syncQueueQueries.deleteAll()
}
