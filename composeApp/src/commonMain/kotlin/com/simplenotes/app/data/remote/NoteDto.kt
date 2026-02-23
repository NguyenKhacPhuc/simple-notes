package com.simplenotes.app.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NoteDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    val title: String,
    val content: String,
    @SerialName("is_deleted") val isDeleted: Boolean = false,
    @SerialName("sync_version") val syncVersion: Long = 1,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)
