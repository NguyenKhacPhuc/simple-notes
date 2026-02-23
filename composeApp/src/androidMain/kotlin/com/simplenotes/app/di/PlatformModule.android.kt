package com.simplenotes.app.di

import com.simplenotes.app.data.local.DriverFactory
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val platformModule = module {
    single { DriverFactory(androidContext()) }
}
