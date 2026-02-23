package com.simplenotes.app

import com.simplenotes.app.di.commonModule
import com.simplenotes.app.di.platformModule
import org.koin.core.context.startKoin

fun initKoin() {
    startKoin {
        modules(commonModule, platformModule)
    }
}
