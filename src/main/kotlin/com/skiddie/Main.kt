package com.skiddie

import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.skiddie.language.LanguageRegistry
import com.skiddie.ui.SkiddieApp
import com.skiddie.ui.theme.SkiddieTheme

fun main() = application {
    LanguageRegistry.load()

    val windowState = rememberWindowState(width = 1200.dp, height = 800.dp)
    var windowTitle by remember { mutableStateOf("Skiddie - Untitled") }

    Window(
        onCloseRequest = ::exitApplication,
        title = windowTitle,
        state = windowState
    ) {
        SkiddieTheme {
            SkiddieApp(
                onTitleChange = { filePath ->
                    windowTitle = "Skiddie - $filePath"
                }
            )
        }
    }
}
