package com.simplenotes.app

import androidx.compose.runtime.Composable
import com.simplenotes.app.ui.theme.SimpleNotesTheme
import com.simplenotes.app.ui.navigation.AppNavigation
import org.koin.compose.KoinContext

@Composable
fun App() {
    KoinContext {
        SimpleNotesTheme {
            AppNavigation()
        }
    }
}
