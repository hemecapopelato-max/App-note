package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.NoteDetailScreen
import com.example.ui.NoteViewModel
import com.example.ui.NotesScreen
import com.example.ui.theme.NotelyTheme

sealed interface Screen {
    object NotesList : Screen
    data class NoteDetail(val noteId: Long?) : Screen
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NotelyTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NotelyApp()
                }
            }
        }
    }
}

@Composable
fun NotelyApp(
    viewModel: NoteViewModel = viewModel()
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.NotesList) }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "screen_transition"
    ) { screen ->
        when (screen) {
            is Screen.NotesList -> {
                NotesScreen(
                    viewModel = viewModel,
                    onNoteClick = { noteId ->
                        currentScreen = Screen.NoteDetail(noteId)
                    },
                    onAddNoteClick = {
                        currentScreen = Screen.NoteDetail(null)
                    }
                )
            }
            is Screen.NoteDetail -> {
                NoteDetailScreen(
                    noteId = screen.noteId,
                    viewModel = viewModel,
                    onBack = {
                        currentScreen = Screen.NotesList
                    }
                )
            }
        }
    }
}
