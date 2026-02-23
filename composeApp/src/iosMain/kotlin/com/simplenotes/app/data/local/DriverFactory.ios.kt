package com.simplenotes.app.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.simplenotes.app.db.SimpleNotesDb

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(SimpleNotesDb.Schema, "simplenotes.db")
    }
}
