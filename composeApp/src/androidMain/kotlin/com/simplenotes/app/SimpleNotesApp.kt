package com.simplenotes.app

import android.app.Application
import com.simplenotes.app.di.commonModule
import com.simplenotes.app.di.platformModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class SimpleNotesApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@SimpleNotesApp)
            modules(commonModule, platformModule)
        }
    }
}
