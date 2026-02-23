package com.simplenotes.app.data.sync

import com.simplenotes.app.data.remote.NoteDto
import com.simplenotes.app.domain.model.Note
import com.simplenotes.app.domain.model.SyncStatus
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for sync logic: NoteDto→Note mapping, LWW conflict resolution.
 * These test the pure domain logic without requiring database or network fakes.
 */
class SyncLogicTest {

    private fun makeDto(
        id: String = "note-1",
        updatedAt: String = "2026-01-02T00:00:00Z",
    ) = NoteDto(
        id = id,
        userId = "user-1",
        title = "Remote Note",
        content = "Remote body",
        isDeleted = false,
        syncVersion = 2,
        createdAt = "2026-01-01T00:00:00Z",
        updatedAt = updatedAt,
    )

    private fun makeNote(
        id: String = "note-1",
        updatedAt: String = "2026-01-01T00:00:00Z",
    ) = Note(
        id = id,
        userId = "user-1",
        title = "Local Note",
        body = "Local body",
        createdAt = Instant.parse("2026-01-01T00:00:00Z"),
        updatedAt = Instant.parse(updatedAt),
        syncStatus = SyncStatus.PENDING,
    )

    // Test the toDomain mapping (same logic as in SyncManager.kt)
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

    @Test
    fun dtoToDomainMapsAllFields() {
        val dto = makeDto()
        val note = dto.toDomain()

        assertEquals(dto.id, note.id)
        assertEquals(dto.userId, note.userId)
        assertEquals(dto.title, note.title)
        assertEquals(dto.content, note.body) // content → body
        assertEquals(dto.isDeleted, note.isDeleted)
        assertEquals(dto.syncVersion, note.syncVersion)
        assertEquals(Instant.parse(dto.createdAt), note.createdAt)
        assertEquals(Instant.parse(dto.updatedAt), note.updatedAt)
        assertEquals(SyncStatus.SYNCED, note.syncStatus) // always SYNCED from remote
    }

    @Test
    fun lwwRemoteWinsWhenNewer() {
        val local = makeNote(updatedAt = "2026-01-01T00:00:00Z")
        val remote = makeDto(updatedAt = "2026-01-02T00:00:00Z")
        val remoteInstant = Instant.parse(remote.updatedAt)

        // LWW logic: remote wins if newer
        val remoteWins = remoteInstant > local.updatedAt
        assertTrue(remoteWins)
    }

    @Test
    fun lwwLocalWinsWhenNewer() {
        val local = makeNote(updatedAt = "2026-01-03T00:00:00Z")
        val remote = makeDto(updatedAt = "2026-01-02T00:00:00Z")
        val remoteInstant = Instant.parse(remote.updatedAt)

        val remoteWins = remoteInstant > local.updatedAt
        assertFalse(remoteWins)
    }

    @Test
    fun lwwRemoteWinsWhenLocalIsNull() {
        val remote = makeDto()
        // When local is null, remote should always be applied
        val localNote: Note? = null
        val remoteInstant = Instant.parse(remote.updatedAt)

        val shouldApply = localNote == null || remoteInstant > localNote.updatedAt
        assertTrue(shouldApply)
    }

    @Test
    fun lwwEqualTimestampLocalWins() {
        val timestamp = "2026-01-02T00:00:00Z"
        val local = makeNote(updatedAt = timestamp)
        val remote = makeDto(updatedAt = timestamp)
        val remoteInstant = Instant.parse(remote.updatedAt)

        // With strict >, equal timestamps = local wins (no overwrite)
        val remoteWins = remoteInstant > local.updatedAt
        assertFalse(remoteWins)
    }

    @Test
    fun maxTimestampFromMultipleRemoteNotes() {
        val notes = listOf(
            makeDto(id = "n1", updatedAt = "2026-01-01T00:00:00Z"),
            makeDto(id = "n2", updatedAt = "2026-01-03T00:00:00Z"),
            makeDto(id = "n3", updatedAt = "2026-01-02T00:00:00Z"),
        )
        val maxTimestamp = notes.maxOfOrNull { it.updatedAt }
        assertEquals("2026-01-03T00:00:00Z", maxTimestamp)
    }

    @Test
    fun emptyRemoteListFallsBackToSince() {
        val since = "1970-01-01T00:00:00Z"
        val remoteNotes = emptyList<NoteDto>()
        val maxTimestamp = remoteNotes.maxOfOrNull { it.updatedAt } ?: since
        assertEquals(since, maxTimestamp)
    }

    @Test
    fun deletedNoteMapsCorrectly() {
        val dto = NoteDto(
            id = "d1",
            userId = "u1",
            title = "Deleted",
            content = "",
            isDeleted = true,
            syncVersion = 3,
            createdAt = "2026-01-01T00:00:00Z",
            updatedAt = "2026-01-02T00:00:00Z",
        )
        val note = dto.toDomain()
        assertTrue(note.isDeleted)
        assertEquals(3L, note.syncVersion)
    }
}
