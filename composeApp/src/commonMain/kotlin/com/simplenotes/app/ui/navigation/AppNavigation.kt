package com.simplenotes.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.simplenotes.app.ui.auth.AuthScreen
import com.simplenotes.app.ui.auth.AuthViewModel
import com.simplenotes.app.ui.editor.EditorScreen
import com.simplenotes.app.ui.notes.NoteListScreen
import com.simplenotes.app.ui.settings.SettingsScreen
import org.koin.compose.viewmodel.koinViewModel

object Routes {
    const val AUTH = "auth"
    const val NOTES = "notes"
    const val EDITOR = "editor/{noteId}"
    const val EDITOR_NEW = "editor/new"
    const val SETTINGS = "settings"

    fun editor(noteId: String) = "editor/$noteId"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = koinViewModel()
    val authState by authViewModel.state.collectAsState()

    val startDestination = if (authState.user != null) Routes.NOTES else Routes.AUTH

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.AUTH) {
            AuthScreen(
                viewModel = authViewModel,
                onAuthSuccess = {
                    navController.navigate(Routes.NOTES) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.NOTES) {
            NoteListScreen(
                onNoteClick = { noteId -> navController.navigate(Routes.editor(noteId)) },
                onNewNote = { navController.navigate(Routes.EDITOR_NEW) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(
            Routes.EDITOR,
            arguments = listOf(navArgument("noteId") { type = NavType.StringType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")
            EditorScreen(
                noteId = if (noteId == "new") null else noteId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onSignOut = {
                    navController.navigate(Routes.AUTH) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
