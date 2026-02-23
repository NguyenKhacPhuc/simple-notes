package com.simplenotes.app.domain.model

import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NoteTest {

    private fun makeNote(
        id: String = "test-id",
        title: String = "Test Title",
        body: String = "Test body",
        isDeleted: Boolean = false,
        syncStatus: SyncStatus = SyncStatus.SYNCED,
    ) = Note(
        id = id,
        userId = "user-1",
        title = title,
        body = body,
        isDeleted = isDeleted,
        syncVersion = 1,
        createdAt = Instant.parse("2026-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2026-01-01T00:00:00Z"),
        syncStatus = syncStatus,
    )

    @Test
    fun defaultValuesAreCorrect() {
        val note = makeNote()
        assertFalse(note.isDeleted)
        assertEquals(1L, note.syncVersion)
        assertEquals(SyncStatus.SYNCED, note.syncStatus)
    }

    @Test
    fun copyPreservesFields() {
        val note = makeNote()
        val updated = note.copy(title = "New Title", syncStatus = SyncStatus.PENDING)
        assertEquals("New Title", updated.title)
        assertEquals(SyncStatus.PENDING, updated.syncStatus)
        assertEquals(note.id, updated.id)
        assertEquals(note.body, updated.body)
    }

    @Test
    fun syncStatusEnumValues() {
        assertEquals(3, SyncStatus.entries.size)
        assertEquals(SyncStatus.SYNCED, SyncStatus.valueOf("SYNCED"))
        assertEquals(SyncStatus.PENDING, SyncStatus.valueOf("PENDING"))
        assertEquals(SyncStatus.CONFLICT, SyncStatus.valueOf("CONFLICT"))
    }

    @Test
    fun equalityByValue() {
        val a = makeNote(id = "1")
        val b = makeNote(id = "1")
        assertEquals(a, b)
    }

    @Test
    fun differentIdsNotEqual() {
        val a = makeNote(id = "1")
        val b = makeNote(id = "2")
        assertFalse(a == b)
    }
}
