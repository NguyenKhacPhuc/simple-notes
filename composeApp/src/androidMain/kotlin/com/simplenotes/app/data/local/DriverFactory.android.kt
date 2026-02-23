package com.simplenotes.app.data.local

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.simplenotes.app.db.SimpleNotesDb

actual class DriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(SimpleNotesDb.Schema, context, "simplenotes.db")
    }
}
