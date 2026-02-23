package com.simplenotes.app.di

import com.simplenotes.app.Config
import com.simplenotes.app.data.local.NoteDao
import com.simplenotes.app.data.local.SyncMetaDao
import com.simplenotes.app.data.local.SyncQueueDao
import com.simplenotes.app.data.remote.RemoteNoteApi
import com.simplenotes.app.data.remote.createAppSupabaseClient
import com.simplenotes.app.data.repository.NoteRepository
import com.simplenotes.app.data.sync.SyncManager
import com.simplenotes.app.db.SimpleNotesDb
import com.simplenotes.app.ui.auth.AuthViewModel
import com.simplenotes.app.ui.editor.EditorViewModel
import com.simplenotes.app.ui.notes.NotesViewModel
import com.simplenotes.app.ui.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val commonModule = module {
    single {
        createAppSupabaseClient(
            url = Config.supabaseUrl,
            key = Config.supabaseAnonKey,
        )
    }
    single { SimpleNotesDb(get<com.simplenotes.app.data.local.DriverFactory>().createDriver()) }
    single { NoteDao(get()) }
    single { SyncQueueDao(get()) }
    single { SyncMetaDao(get()) }
    single { RemoteNoteApi(get()) }
    single { NoteRepository(get(), get()) }
    single { SyncManager(get(), get(), get(), get()) }

    viewModelOf(::AuthViewModel)
    viewModelOf(::NotesViewModel)
    viewModelOf(::EditorViewModel)
    viewModelOf(::SettingsViewModel)
}
