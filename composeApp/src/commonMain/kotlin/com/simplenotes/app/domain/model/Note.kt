package com.simplenotes.app.domain.model

import kotlinx.datetime.Instant

data class Note(
    val id: String,
    val userId: String,
    val title: String,
    val body: String,
    val isDeleted: Boolean = false,
    val syncVersion: Long = 1,
    val createdAt: Instant,
    val updatedAt: Instant,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
)

enum class SyncStatus {
    SYNCED, PENDING, CONFLICT
}
