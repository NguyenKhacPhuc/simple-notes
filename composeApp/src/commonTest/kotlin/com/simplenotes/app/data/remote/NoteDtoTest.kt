package com.simplenotes.app.data.remote

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NoteDtoTest {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private fun makeDto() = NoteDto(
        id = "note-1",
        userId = "user-1",
        title = "Test",
        content = "Body text",
        isDeleted = false,
        syncVersion = 1,
        createdAt = "2026-01-01T00:00:00Z",
        updatedAt = "2026-01-01T12:00:00Z",
    )

    @Test
    fun serializeAndDeserialize() {
        val dto = makeDto()
        val serialized = json.encodeToString(NoteDto.serializer(), dto)
        val deserialized = json.decodeFromString(NoteDto.serializer(), serialized)
        assertEquals(dto, deserialized)
    }

    @Test
    fun jsonFieldNamesUseSnakeCase() {
        val dto = makeDto()
        val serialized = json.encodeToString(NoteDto.serializer(), dto)
        assertTrue(serialized.contains("\"user_id\""))
        assertTrue(serialized.contains("\"is_deleted\""))
        assertTrue(serialized.contains("\"sync_version\""))
        assertTrue(serialized.contains("\"created_at\""))
        assertTrue(serialized.contains("\"updated_at\""))
    }

    @Test
    fun deserializeFromSnakeCaseJson() {
        val raw = """
            {
                "id": "n1",
                "user_id": "u1",
                "title": "Hello",
                "content": "World",
                "is_deleted": false,
                "sync_version": 3,
                "created_at": "2026-01-01T00:00:00Z",
                "updated_at": "2026-01-02T00:00:00Z"
            }
        """.trimIndent()
        val dto = json.decodeFromString(NoteDto.serializer(), raw)
        assertEquals("n1", dto.id)
        assertEquals("u1", dto.userId)
        assertEquals("Hello", dto.title)
        assertEquals("World", dto.content)
        assertFalse(dto.isDeleted)
        assertEquals(3L, dto.syncVersion)
    }

    @Test
    fun defaultValues() {
        val raw = """
            {
                "id": "n1",
                "user_id": "u1",
                "title": "T",
                "content": "C",
                "created_at": "2026-01-01T00:00:00Z",
                "updated_at": "2026-01-01T00:00:00Z"
            }
        """.trimIndent()
        val dto = json.decodeFromString(NoteDto.serializer(), raw)
        assertFalse(dto.isDeleted)
        assertEquals(1L, dto.syncVersion)
    }
}
