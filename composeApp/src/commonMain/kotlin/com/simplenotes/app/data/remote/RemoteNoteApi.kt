package com.simplenotes.app.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class RemoteNoteApi(private val client: SupabaseClient) {

    suspend fun pullChanges(since: String): List<NoteDto> =
        client.from("notes")
            .select {
                filter { gt("updated_at", since) }
            }
            .decodeList()

    suspend fun upsertNote(note: NoteDto) {
        client.from("notes").upsert(note)
    }

    suspend fun searchNotes(query: String): List<NoteDto> =
        client.postgrest.rpc(
            "search_notes",
            buildJsonObject { put("query", query) }
        ).decodeList()
}
