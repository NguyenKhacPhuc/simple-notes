package com.simplenotes.app.data.local

import com.simplenotes.app.db.SimpleNotesDb

class SyncMetaDao(private val db: SimpleNotesDb) {

    fun get(key: String): String? =
        db.syncMetaQueries.get(key).executeAsOneOrNull()

    fun set(key: String, value: String) {
        db.syncMetaQueries.set(key, value)
    }
}
